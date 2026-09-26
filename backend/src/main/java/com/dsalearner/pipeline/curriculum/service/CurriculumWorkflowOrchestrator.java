package com.dsalearner.pipeline.curriculum.service;

import com.dsalearner.exception.InvalidTransitionException;
import com.dsalearner.pipeline.model.entity.CfCurriculum;
import com.dsalearner.pipeline.model.entity.CfCurriculumLevel;
import com.dsalearner.pipeline.model.entity.CfCurriculumWorkflowEvent;
import com.dsalearner.pipeline.repository.CfCurriculumLevelRepository;
import com.dsalearner.pipeline.repository.CfCurriculumRepository;
import com.dsalearner.pipeline.repository.CfCurriculumWorkflowEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Validates and applies curriculum and level state transitions.
 * Records an immutable audit event for every transition.
 * Mirrors the pattern of WorkflowOrchestrator for lessons.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CurriculumWorkflowOrchestrator {

    private final CfCurriculumRepository curriculumRepository;
    private final CfCurriculumLevelRepository levelRepository;
    private final CfCurriculumWorkflowEventRepository eventRepository;

    // ─── Curriculum transitions ───────────────────────────────────────────

    private static final Map<String, Set<String>> CURRICULUM_ALLOWED = Map.of(
            "DRAFT",                  Set.of("BLUEPRINT_PENDING"),
            "BLUEPRINT_PENDING",      Set.of("BLUEPRINT_GENERATED", "DRAFT"),
            "BLUEPRINT_GENERATED",    Set.of("BLUEPRINT_VALIDATED", "DRAFT"),
            "BLUEPRINT_VALIDATED",    Set.of("GENERATION_IN_PROGRESS"),
            "GENERATION_IN_PROGRESS", Set.of("CURRICULUM_QA_PENDING"),
            "CURRICULUM_QA_PENDING",  Set.of("CURRICULUM_QA_PASSED", "CURRICULUM_QA_FAILED"),
            "CURRICULUM_QA_FAILED",   Set.of("CURRICULUM_QA_PENDING", "GENERATION_IN_PROGRESS"),
            "CURRICULUM_QA_PASSED",   Set.of("APPROVED"),
            "APPROVED",               Set.of("ARCHIVED"),
            "ARCHIVED",               Set.of()
    );

    @Transactional
    public CfCurriculum applyTransition(UUID curriculumId,
                                         String toStatus,
                                         String trigger,
                                         String actor,
                                         UUID agentRunId,
                                         Map<String, Object> metadata) {
        CfCurriculum curriculum = curriculumRepository.findById(curriculumId)
                .orElseThrow(() -> new com.dsalearner.exception.NotFoundException("Curriculum not found: " + curriculumId));

        String fromStatus = curriculum.getCurriculumStatus();
        validateCurriculumTransition(fromStatus, toStatus, trigger);

        curriculum.setCurriculumStatus(toStatus);
        curriculum.setUpdatedAt(Instant.now());
        CfCurriculum saved = curriculumRepository.save(curriculum);

        recordEvent(curriculumId, null, fromStatus, toStatus, trigger, actor, agentRunId, metadata);
        log.info("Curriculum {} status: {} → {} [trigger={}]", curriculumId, fromStatus, toStatus, trigger);
        return saved;
    }

    @Transactional
    public CfCurriculum approve(UUID curriculumId, String actor) {
        CfCurriculum curriculum = curriculumRepository.findById(curriculumId)
                .orElseThrow(() -> new com.dsalearner.exception.NotFoundException("Curriculum not found: " + curriculumId));

        validateCurriculumTransition(curriculum.getCurriculumStatus(), "APPROVED", "human_approve");
        curriculum.setCurriculumStatus("APPROVED");
        curriculum.setPublishGatePassed(true);
        curriculum.setUpdatedAt(Instant.now());
        CfCurriculum saved = curriculumRepository.save(curriculum);

        recordEvent(curriculumId, null, "CURRICULUM_QA_PASSED", "APPROVED",
                "human_approve", actor, null, null);
        log.info("Curriculum {} APPROVED and publish gate opened by {}", curriculumId, actor);
        return saved;
    }

    // ─── Level transitions ────────────────────────────────────────────────

    private static final Map<String, Set<String>> LEVEL_ALLOWED = Map.of(
            "PLANNED",                  Set.of("GENERATION_IN_PROGRESS"),
            "GENERATION_IN_PROGRESS",   Set.of("LEVEL_QA_PENDING"),
            "LEVEL_QA_PENDING",         Set.of("LEVEL_QA_PASSED", "LEVEL_QA_FAILED"),
            "LEVEL_QA_FAILED",          Set.of("LEVEL_QA_PENDING", "GENERATION_IN_PROGRESS"),
            "LEVEL_QA_PASSED",          Set.of("APPROVED"),
            "APPROVED",                 Set.of("ARCHIVED"),
            "ARCHIVED",                 Set.of()
    );

    @Transactional
    public CfCurriculumLevel applyLevelTransition(UUID levelId,
                                                   String toStatus,
                                                   String trigger,
                                                   String actor,
                                                   UUID agentRunId,
                                                   Map<String, Object> metadata) {
        CfCurriculumLevel level = levelRepository.findById(levelId)
                .orElseThrow(() -> new com.dsalearner.exception.NotFoundException("Level not found: " + levelId));

        String fromStatus = level.getLevelStatus();
        validateLevelTransition(fromStatus, toStatus, trigger);

        level.setLevelStatus(toStatus);
        level.setUpdatedAt(Instant.now());
        CfCurriculumLevel saved = levelRepository.save(level);

        recordEvent(level.getCurriculumId(), levelId, fromStatus, toStatus, trigger, actor, agentRunId, metadata);
        log.info("Level {} ({}) status: {} → {} [trigger={}]",
                levelId, level.getCefrLevel(), fromStatus, toStatus, trigger);
        return saved;
    }

    @Transactional
    public CfCurriculumLevel approveLevel(UUID levelId, String actor) {
        CfCurriculumLevel level = levelRepository.findById(levelId)
                .orElseThrow(() -> new com.dsalearner.exception.NotFoundException("Level not found: " + levelId));

        validateLevelTransition(level.getLevelStatus(), "APPROVED", "human_approve");
        level.setLevelStatus("APPROVED");
        level.setUpdatedAt(Instant.now());
        CfCurriculumLevel saved = levelRepository.save(level);

        recordEvent(level.getCurriculumId(), levelId, "LEVEL_QA_PASSED", "APPROVED",
                "human_approve", actor, null, null);
        return saved;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────

    private void validateCurriculumTransition(String from, String to, String trigger) {
        Set<String> allowed = CURRICULUM_ALLOWED.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new InvalidTransitionException(
                    "Invalid curriculum transition %s → %s [trigger=%s]".formatted(from, to, trigger));
        }
    }

    private void validateLevelTransition(String from, String to, String trigger) {
        Set<String> allowed = LEVEL_ALLOWED.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new InvalidTransitionException(
                    "Invalid level transition %s → %s [trigger=%s]".formatted(from, to, trigger));
        }
    }

    private void recordEvent(UUID curriculumId, UUID levelId,
                              String fromStatus, String toStatus,
                              String trigger, String actor,
                              UUID agentRunId, Map<String, Object> metadata) {
        CfCurriculumWorkflowEvent event = CfCurriculumWorkflowEvent.builder()
                .curriculumId(curriculumId)
                .levelId(levelId)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .trigger(trigger)
                .actor(actor != null ? actor : "system")
                .agentRunId(agentRunId)
                .metadata(metadata)
                .occurredAt(Instant.now())
                .build();
        eventRepository.save(event);
    }
}
