package com.dsalearner.pipeline.curriculum.service;

import com.dsalearner.exception.ConflictException;
import com.dsalearner.exception.NotFoundException;
import com.dsalearner.pipeline.curriculum.agent.CurriculumBlueprint;
import com.dsalearner.pipeline.curriculum.agent.LevelBlueprint;
import com.dsalearner.pipeline.curriculum.agent.UnitBlueprint;
import com.dsalearner.pipeline.curriculum.agent.LessonPlanSlot;
import com.dsalearner.pipeline.model.entity.CfCurriculum;
import com.dsalearner.pipeline.model.entity.CfCurriculumLevel;
import com.dsalearner.pipeline.model.entity.CfCurriculumLessonPlan;
import com.dsalearner.pipeline.model.entity.CfCurriculumUnit;
import com.dsalearner.pipeline.model.entity.CfCurriculumVersion;
import com.dsalearner.pipeline.repository.CfCurriculumLessonPlanRepository;
import com.dsalearner.pipeline.repository.CfCurriculumLevelRepository;
import com.dsalearner.pipeline.repository.CfCurriculumRepository;
import com.dsalearner.pipeline.repository.CfCurriculumUnitRepository;
import com.dsalearner.pipeline.repository.CfCurriculumVersionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * CRUD + blueprint persistence + lesson plan provisioning for the curriculum layer.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CurriculumService {

    private final CfCurriculumRepository curriculumRepository;
    private final CfCurriculumLevelRepository levelRepository;
    private final CfCurriculumUnitRepository unitRepository;
    private final CfCurriculumLessonPlanRepository lessonPlanRepository;
    private final CfCurriculumVersionRepository versionRepository;
    private final CurriculumWorkflowOrchestrator orchestrator;
    private final ObjectMapper objectMapper;

    // ─── CRUD ────────────────────────────────────────────────────────────

    @Transactional
    public CfCurriculum save(CfCurriculum curriculum) {
        return curriculumRepository.save(curriculum);
    }

    public CfCurriculum getById(UUID id) {
        return curriculumRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Curriculum not found: " + id));
    }

    public Optional<CfCurriculum> findByStableRef(String stableRef) {
        return curriculumRepository.findByStableRef(stableRef);
    }

    public List<CfCurriculum> listByLanguage(String languageCode) {
        return curriculumRepository.findByLanguageCode(languageCode);
    }

    public List<CfCurriculumLevel> getLevels(UUID curriculumId) {
        return levelRepository.findByCurriculumIdOrderByOrdinal(curriculumId);
    }

    public CfCurriculumLevel getLevel(UUID levelId) {
        return levelRepository.findById(levelId)
                .orElseThrow(() -> new NotFoundException("Level not found: " + levelId));
    }

    public List<CfCurriculumLessonPlan> getPlansForLevel(UUID levelId) {
        return lessonPlanRepository.findByLevelIdOrderByPosition(levelId);
    }

    // ─── Blueprint persistence ────────────────────────────────────────────

    /**
     * Freeze a blueprint snapshot as an immutable version and expand it into
     * cf_curriculum_units + cf_curriculum_lesson_plans rows.
     * Idempotent if called again with the same version number (skips expansion).
     */
    @Transactional
    public CfCurriculumVersion persistBlueprint(UUID curriculumId,
                                                  CurriculumBlueprint blueprint,
                                                  String changeSummary) {
        CfCurriculum curriculum = getById(curriculumId);

        // Determine next version number
        int nextVersion = versionRepository
                .findFirstByCurriculumIdOrderByVersionDesc(curriculumId)
                .map(v -> v.getVersion() + 1)
                .orElse(1);

        // Serialize blueprint to Map for JSONB storage
        Map<String, Object> snapshot = objectMapper.convertValue(
                blueprint, new TypeReference<>() {});

        CfCurriculumVersion version = CfCurriculumVersion.builder()
                .curriculumId(curriculumId)
                .version(nextVersion)
                .blueprintSnapshot(snapshot)
                .changeSummary(changeSummary)
                .frozen(true)
                .build();
        versionRepository.save(version);

        // Expand blueprint into units + lesson plan rows
        expandBlueprint(curriculumId, blueprint);

        curriculum.setActiveVersion(nextVersion);
        curriculumRepository.save(curriculum);

        log.info("Blueprint v{} persisted for curriculum {} — {} total lessons",
                nextVersion, curriculumId, blueprint.totalLessons());
        return version;
    }

    private void expandBlueprint(UUID curriculumId, CurriculumBlueprint blueprint) {
        for (LevelBlueprint lb : blueprint.levels()) {
            CfCurriculumLevel level = levelRepository
                    .findByCurriculumIdAndCefrLevel(curriculumId, lb.cefrLevel())
                    .orElseThrow(() -> new NotFoundException(
                            "Level row missing for " + lb.cefrLevel() + " in curriculum " + curriculumId));

            // Skip if units already exist (idempotency)
            if (!lessonPlanRepository.findByLevelIdOrderByPosition(level.getId()).isEmpty()) {
                log.debug("Skipping expansion for level {} — rows already exist", lb.cefrLevel());
                continue;
            }

            int globalPosition = 1;
            for (UnitBlueprint ub : lb.units()) {
                CfCurriculumUnit unit = CfCurriculumUnit.builder()
                        .levelId(level.getId())
                        .curriculumId(curriculumId)
                        .ordinal(ub.ordinal())
                        .label(ub.label())
                        .theme(ub.theme())
                        .learningGoal(ub.learningGoal())
                        .build();
                unitRepository.save(unit);

                for (LessonPlanSlot slot : ub.lessons()) {
                    CfCurriculumLessonPlan plan = CfCurriculumLessonPlan.builder()
                            .curriculumId(curriculumId)
                            .levelId(level.getId())
                            .unitId(unit.getId())
                            .stableRef(slot.stableRef())
                            .position(globalPosition++)
                            .title(slot.title())
                            .topic(slot.topic())
                            .lessonType(slot.lessonType() != null ? slot.lessonType() : "LEARN")
                            .difficulty(slot.difficulty() != null ? slot.difficulty() : "FOUNDATION")
                            .skillFocus(slot.skillFocus() != null
                                    ? slot.skillFocus().toArray(new String[0]) : null)
                            .learningObjectives(slot.learningObjectives())
                            .communicationGoals(slot.communicationGoals())
                            .grammarTargets(slot.grammarHints() != null
                                    ? slot.grammarHints().toArray(new String[0]) : null)
                            .vocabTargets(slot.vocabHints() != null
                                    ? slot.vocabHints().toArray(new String[0]) : null)
                            .planStatus("PLANNED")
                            .build();
                    lessonPlanRepository.save(plan);
                }
            }

            level.setTargetLessonCount(lb.totalLessons());
            levelRepository.save(level);
        }
    }

    // ─── Lesson provisioning ──────────────────────────────────────────────

    /**
     * Record the lesson UUID produced by the content factory against its plan row.
     * Transitions the plan to GENERATING.
     */
    @Transactional
    public CfCurriculumLessonPlan provisionLesson(UUID planId, UUID lessonId) {
        CfCurriculumLessonPlan plan = lessonPlanRepository.findById(planId)
                .orElseThrow(() -> new NotFoundException("Lesson plan not found: " + planId));

        if (plan.getLessonId() != null && !plan.getLessonId().equals(lessonId)) {
            throw new ConflictException("Plan " + planId + " already provisioned with a different lesson");
        }

        plan.setLessonId(lessonId);
        plan.setPlanStatus("GENERATING");
        plan.setGenerationAttempt(plan.getGenerationAttempt() + 1);
        return lessonPlanRepository.save(plan);
    }

    /**
     * Sync a plan's status from the lesson it tracks (called after lesson QA events).
     */
    @Transactional
    public void syncPlanStatus(UUID planId, String newPlanStatus) {
        CfCurriculumLessonPlan plan = lessonPlanRepository.findById(planId)
                .orElseThrow(() -> new NotFoundException("Lesson plan not found: " + planId));
        plan.setPlanStatus(newPlanStatus);
        lessonPlanRepository.save(plan);
    }

    /**
     * Find the plan row for a given lesson (may be absent if lesson is standalone).
     */
    public Optional<CfCurriculumLessonPlan> findPlanByLessonId(UUID lessonId) {
        return lessonPlanRepository.findByLessonId(lessonId);
    }

    // ─── Progress queries ─────────────────────────────────────────────────

    /**
     * Returns true when all lesson plans for the level are in a terminal passing state.
     */
    public boolean isLevelGenerationComplete(UUID levelId) {
        List<String> terminalGood = List.of("QA_PASSED", "APPROVED", "PUBLISHED", "SKIPPED");
        long total   = lessonPlanRepository.countByLevelId(levelId);
        long passing = lessonPlanRepository.countByLevelIdAndPlanStatusIn(levelId, terminalGood);
        return total > 0 && passing == total;
    }

    /**
     * Returns QUEUED plans up to limit, skipping any that are BLOCKED.
     */
    public List<CfCurriculumLessonPlan> peekQueuedPlans(UUID levelId, int limit) {
        return lessonPlanRepository
                .findByLevelIdAndPlanStatusOrderByPosition(levelId, "QUEUED")
                .stream()
                .limit(limit)
                .toList();
    }
}
