package com.dsalearner.pipeline.curriculum.service;

import com.dsalearner.exception.ConflictException;
import com.dsalearner.pipeline.model.entity.CfCurriculumDependency;
import com.dsalearner.pipeline.model.entity.CfCurriculumLessonPlan;
import com.dsalearner.pipeline.repository.CfCurriculumDependencyRepository;
import com.dsalearner.pipeline.repository.CfCurriculumLessonPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DAG-based dependency engine.
 * Records plan → plan dependencies, detects cycles via topological sort,
 * and determines which plans are unblocked given the current completion set.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CurriculumDependencyService {

    private final CfCurriculumDependencyRepository dependencyRepository;
    private final CfCurriculumLessonPlanRepository lessonPlanRepository;

    // ─── Dependency recording ─────────────────────────────────────────────

    @Transactional
    public CfCurriculumDependency record(UUID planId, UUID requiredPlanId, String dependencyType) {
        // Skip self-dependency
        if (planId.equals(requiredPlanId)) return null;

        // Idempotency — skip if already present
        if (dependencyRepository.existsByLessonPlanIdAndRequiredPlanId(planId, requiredPlanId)) {
            return dependencyRepository.findByLessonPlanIdAndRequiredPlanId(planId, requiredPlanId)
                    .orElse(null);
        }

        CfCurriculumDependency dep = CfCurriculumDependency.builder()
                .lessonPlanId(planId)
                .requiredPlanId(requiredPlanId)
                .dependencyType(dependencyType != null ? dependencyType : "PREREQUISITE")
                .build();
        return dependencyRepository.save(dep);
    }

    @Transactional
    public void recordBatch(UUID curriculumId, Map<String, List<String>> stableRefDeps) {
        // Build stableRef → id map
        Map<String, UUID> refToId = lessonPlanRepository.findByCurriculumId(curriculumId)
                .stream()
                .collect(Collectors.toMap(
                        CfCurriculumLessonPlan::getStableRef,
                        CfCurriculumLessonPlan::getId));

        for (Map.Entry<String, List<String>> e : stableRefDeps.entrySet()) {
            UUID planId = refToId.get(e.getKey());
            if (planId == null) continue;
            for (String reqRef : e.getValue()) {
                UUID reqId = refToId.get(reqRef);
                if (reqId == null) continue;
                record(planId, reqId, "PREREQUISITE");
            }
        }

        // Validate no cycles were introduced
        validateNoCycles(curriculumId);
    }

    // ─── Cycle detection ──────────────────────────────────────────────────

    public void validateNoCycles(UUID curriculumId) {
        List<UUID> planIds = lessonPlanRepository.findByCurriculumId(curriculumId)
                .stream().map(CfCurriculumLessonPlan::getId).toList();

        Map<UUID, List<UUID>> adj = buildAdjacency(planIds);
        List<UUID> sorted = topoSort(planIds, adj);
        if (sorted == null) {
            throw new ConflictException("Cycle detected in curriculum " + curriculumId + " dependency graph");
        }
    }

    // ─── Topological ordering ─────────────────────────────────────────────

    /**
     * Returns plans in topological order (all prerequisites before dependents).
     * Returns null if a cycle is detected.
     */
    public List<UUID> topologicalOrder(UUID curriculumId) {
        List<UUID> planIds = lessonPlanRepository.findByCurriculumId(curriculumId)
                .stream().map(CfCurriculumLessonPlan::getId).toList();
        return topoSort(planIds, buildAdjacency(planIds));
    }

    // ─── Unblocked plan resolution ────────────────────────────────────────

    /**
     * Returns plans from the QUEUED set that have all prerequisites in a passing terminal state.
     */
    public List<CfCurriculumLessonPlan> resolveUnblocked(UUID levelId) {
        List<CfCurriculumLessonPlan> queued =
                lessonPlanRepository.findByLevelIdAndPlanStatusOrderByPosition(levelId, "QUEUED");
        if (queued.isEmpty()) return List.of();

        Set<String> passingStatuses = Set.of("QA_PASSED", "APPROVED", "PUBLISHED", "SKIPPED");

        List<CfCurriculumLessonPlan> unblocked = new ArrayList<>();
        for (CfCurriculumLessonPlan plan : queued) {
            List<UUID> requiredIds = dependencyRepository
                    .findByLessonPlanId(plan.getId())
                    .stream()
                    .map(CfCurriculumDependency::getRequiredPlanId)
                    .toList();

            boolean allSatisfied = requiredIds.isEmpty() || requiredIds.stream().allMatch(reqId -> {
                CfCurriculumLessonPlan req = lessonPlanRepository.findById(reqId).orElse(null);
                return req != null && passingStatuses.contains(req.getPlanStatus());
            });

            if (allSatisfied) {
                unblocked.add(plan);
            }
        }
        return unblocked;
    }

    /**
     * Update PLANNED plans to QUEUED or BLOCKED based on dependency state.
     * Called after each lesson transitions to a passing status.
     */
    @Transactional
    public void refreshBlockedStatus(UUID levelId) {
        List<CfCurriculumLessonPlan> planned =
                lessonPlanRepository.findByLevelIdAndPlanStatusOrderByPosition(levelId, "PLANNED");

        Set<String> passingStatuses = Set.of("QA_PASSED", "APPROVED", "PUBLISHED", "SKIPPED");

        for (CfCurriculumLessonPlan plan : planned) {
            List<UUID> requiredIds = dependencyRepository
                    .findByLessonPlanId(plan.getId())
                    .stream()
                    .map(CfCurriculumDependency::getRequiredPlanId)
                    .toList();

            boolean canQueue = requiredIds.isEmpty() || requiredIds.stream().allMatch(reqId -> {
                CfCurriculumLessonPlan req = lessonPlanRepository.findById(reqId).orElse(null);
                return req != null && passingStatuses.contains(req.getPlanStatus());
            });

            plan.setPlanStatus(canQueue ? "QUEUED" : "BLOCKED");
            lessonPlanRepository.save(plan);
        }
    }

    // ─── Internal helpers ─────────────────────────────────────────────────

    private Map<UUID, List<UUID>> buildAdjacency(List<UUID> planIds) {
        Map<UUID, List<UUID>> adj = new HashMap<>();
        for (UUID id : planIds) adj.put(id, new ArrayList<>());

        List<CfCurriculumDependency> all = dependencyRepository.findAllByLessonPlanIdIn(planIds);
        for (CfCurriculumDependency d : all) {
            adj.computeIfAbsent(d.getRequiredPlanId(), k -> new ArrayList<>())
               .add(d.getLessonPlanId());
        }
        return adj;
    }

    private List<UUID> topoSort(List<UUID> nodes, Map<UUID, List<UUID>> adj) {
        Map<UUID, Integer> inDegree = new HashMap<>();
        for (UUID n : nodes) inDegree.put(n, 0);
        for (List<UUID> neighbors : adj.values()) {
            for (UUID n : neighbors) inDegree.merge(n, 1, Integer::sum);
        }

        Queue<UUID> queue = new LinkedList<>();
        for (UUID n : nodes) {
            if (inDegree.get(n) == 0) queue.add(n);
        }

        List<UUID> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            UUID cur = queue.poll();
            result.add(cur);
            for (UUID neighbor : adj.getOrDefault(cur, List.of())) {
                int deg = inDegree.merge(neighbor, -1, Integer::sum);
                if (deg == 0) queue.add(neighbor);
            }
        }
        return result.size() == nodes.size() ? result : null; // null = cycle detected
    }
}
