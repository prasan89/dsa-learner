package com.dsalearner.pipeline.curriculum.job;

import com.dsalearner.pipeline.curriculum.service.CurriculumDependencyService;
import com.dsalearner.pipeline.curriculum.service.CurriculumLevelService;
import com.dsalearner.pipeline.curriculum.service.CurriculumService;
import com.dsalearner.pipeline.domain.ContentStatus;
import com.dsalearner.pipeline.model.entity.CfCurriculum;
import com.dsalearner.pipeline.model.entity.CfCurriculumLevel;
import com.dsalearner.pipeline.model.entity.CfCurriculumLessonPlan;
import com.dsalearner.pipeline.model.entity.CfCurriculumPipelineJob;
import com.dsalearner.pipeline.repository.CfCurriculumLessonPlanRepository;
import com.dsalearner.pipeline.repository.CfCurriculumPipelineJobRepository;
import com.dsalearner.pipeline.repository.CfLessonRepository;
import com.dsalearner.pipeline.repository.CfPipelineJobRepository;
import com.dsalearner.pipeline.service.PipelineJobService;
import com.dsalearner.pipeline.service.PipelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Extracted from CurriculumBatchScheduler so that @Transactional is applied via
 * Spring AOP proxy (self-calls in the same class are not intercepted).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CurriculumBatchProcessor {

    private final CfCurriculumLessonPlanRepository lessonPlanRepository;
    private final CfCurriculumPipelineJobRepository jobRepository;
    private final CfLessonRepository lessonRepository;
    private final CfPipelineJobRepository pipelineJobRepository;
    private final CurriculumService curriculumService;
    private final CurriculumLevelService levelService;
    private final CurriculumDependencyService dependencyService;
    private final PipelineService pipelineService;
    private final PipelineJobService pipelineJobService;
    private final CurriculumJobStore jobStore;

    @Transactional
    public void processCurriculum(CfCurriculum curriculum) {
        UUID curriculumId = curriculum.getId();
        int batchSize = curriculum.getBatchSize();

        List<CfCurriculumLevel> activeLevels = levelService.findEligibleLevels(curriculumId);

        // Start eligible PLANNED levels
        for (CfCurriculumLevel level : activeLevels) {
            if ("PLANNED".equals(level.getLevelStatus())) {
                levelService.startGeneration(level.getId(), "batch_scheduler");
                dependencyService.refreshBlockedStatus(level.getId());
            }
        }

        // Fetch all GENERATION_IN_PROGRESS levels and dispatch batches
        List<CfCurriculumLevel> generatingLevels = curriculumService.getLevels(curriculumId).stream()
                .filter(l -> "GENERATION_IN_PROGRESS".equals(l.getLevelStatus()))
                .toList();

        for (CfCurriculumLevel level : generatingLevels) {
            dispatchBatch(curriculum, level, batchSize);
            sweepQaPending(curriculum, level);
            checkLevelCompletion(curriculum, level);
        }

        checkCurriculumCompletion(curriculum);
    }

    private void dispatchBatch(CfCurriculum curriculum, CfCurriculumLevel level, int batchSize) {
        List<CfCurriculumLessonPlan> unblocked = dependencyService.resolveUnblocked(level.getId());
        int dispatched = 0;

        for (CfCurriculumLessonPlan plan : unblocked) {
            if (dispatched >= batchSize) break;
            if (plan.getLessonId() != null) continue;

            int claimed = lessonPlanRepository.compareAndSetStatus(
                    plan.getId(), "QUEUED", "GENERATING");
            if (claimed == 0) continue;

            try {
                dispatchPlan(curriculum, level, plan);
                dispatched++;
            } catch (Exception e) {
                log.error("CurriculumBatchProcessor: failed to dispatch plan {} — {}",
                        plan.getStableRef(), e.getMessage(), e);
                lessonPlanRepository.compareAndSetStatus(plan.getId(), "GENERATING", "QUEUED");
            }
        }

        if (dispatched > 0) {
            log.info("CurriculumBatchProcessor: dispatched {} plans for level {} ({})",
                    dispatched, level.getCefrLevel(), curriculum.getLanguageCode());
        }
    }

    private void dispatchPlan(CfCurriculum curriculum, CfCurriculumLevel level, CfCurriculumLessonPlan plan) {
        UUID lessonId;
        try {
            var lesson = pipelineService.createLesson(
                    plan.getStableRef(),
                    curriculum.getDomainCode(),
                    curriculum.getLanguageCode(),
                    level.getCefrLevel(),
                    plan.getTitle(),
                    "batch_scheduler"
            );
            lessonId = lesson.getId();
        } catch (com.dsalearner.exception.ConflictException e) {
            var existing = lessonRepository.findByStableRef(plan.getStableRef())
                    .orElseThrow(() -> new RuntimeException("Lesson exists but lookup failed: " + plan.getStableRef()));
            lessonId = existing.getId();
        }

        curriculumService.provisionLesson(plan.getId(), lessonId);
        pipelineService.plan(lessonId, "batch_scheduler");

        String curriculumContext = buildCurriculumContext(curriculum, level, plan);

        pipelineJobService.submitContentGeneration(
                lessonId, 1,
                Map.of(
                        "stableRef", plan.getStableRef(),
                        "curriculumContext", curriculumContext != null ? curriculumContext : ""
                ),
                "batch_scheduler"
        );

        log.info("CurriculumBatchProcessor: dispatched plan {} → lessonId={}", plan.getStableRef(), lessonId);
    }

    private String buildCurriculumContext(CfCurriculum curriculum, CfCurriculumLevel level,
                                          CfCurriculumLessonPlan plan) {
        StringBuilder sb = new StringBuilder();
        sb.append("Curriculum: ").append(curriculum.getDisplayName()).append("\n");
        sb.append("Level: ").append(level.getCefrLevel()).append(" ").append(level.getDisplayName()).append("\n");
        sb.append("Position: ").append(plan.getPosition()).append("\n");
        sb.append("Unit learning goal: ").append(
                plan.getLearningObjectives() != null ? plan.getLearningObjectives() : "").append("\n");
        if (plan.getGrammarTargets() != null && plan.getGrammarTargets().length > 0) {
            sb.append("Grammar focus: ").append(String.join(", ", plan.getGrammarTargets())).append("\n");
        }
        if (plan.getVocabTargets() != null && plan.getVocabTargets().length > 0) {
            sb.append("Vocabulary targets: ").append(String.join(", ", plan.getVocabTargets())).append("\n");
        }
        return sb.toString();
    }

    /**
     * Sweeps QA_PENDING lessons that have no active QA_CONTENT job and submits one.
     * Covers lessons that reached QA_PENDING before the auto-submit was added to
     * ContentGenerationOrchestrator, and acts as a safety net for any future gaps.
     */
    private void sweepQaPending(CfCurriculum curriculum, CfCurriculumLevel level) {
        lessonRepository.findByLanguageCodeAndContentStatus(curriculum.getLanguageCode(), ContentStatus.QA_PENDING)
                .stream()
                .filter(l -> {
                    var plan = lessonPlanRepository.findByLessonId(l.getId());
                    if (plan.isEmpty() || !level.getId().equals(plan.get().getLevelId())) return false;
                    boolean activeQa = pipelineJobRepository
                            .findByLessonIdAndJobTypeAndStatusIn(l.getId(), "QA_CONTENT",
                                    List.of("QUEUED", "RUNNING"))
                            .isPresent();
                    return !activeQa;
                })
                .forEach(l -> {
                    try {
                        pipelineJobService.submitQaContent(l.getId(), l.getCurrentVersion(), Map.of(),
                                "batch_scheduler:sweep");
                        log.info("CurriculumBatchProcessor: submitted QA sweep for lessonId={}", l.getId());
                    } catch (Exception e) {
                        log.warn("CurriculumBatchProcessor: QA sweep skip lessonId={} — {}",
                                l.getId(), e.getMessage());
                    }
                });
    }

    private void checkLevelCompletion(CfCurriculum curriculum, CfCurriculumLevel level) {
        if (!curriculumService.isLevelGenerationComplete(level.getId())) return;

        boolean qaAlreadyQueued = jobRepository
                .findByCurriculumIdAndJobTypeAndStatus(curriculum.getId(), "CURRICULUM_LEVEL_QA", "QUEUED")
                .stream().anyMatch(j -> level.getId().equals(j.getLevelId()));
        if (qaAlreadyQueued) return;

        CfCurriculumPipelineJob qaJob = CfCurriculumPipelineJob.builder()
                .curriculumId(curriculum.getId())
                .levelId(level.getId())
                .jobType("CURRICULUM_LEVEL_QA")
                .payload(Map.of(
                        "languageCode", curriculum.getLanguageCode(),
                        "languageDisplayName", curriculum.getDisplayName(),
                        "domainCode", curriculum.getDomainCode()
                ))
                .build();
        jobStore.enqueue(qaJob);
        log.info("CurriculumBatchProcessor: enqueued CURRICULUM_LEVEL_QA for level {}", level.getCefrLevel());
    }

    private void checkCurriculumCompletion(CfCurriculum curriculum) {
        if (!levelService.isCurriculumGenerationComplete(curriculum.getId())) return;

        boolean coherenceAlreadyQueued = !jobRepository
                .findByCurriculumIdAndJobTypeAndStatus(curriculum.getId(), "CURRICULUM_COHERENCE_QA", "QUEUED")
                .isEmpty();
        if (coherenceAlreadyQueued) return;

        CfCurriculumPipelineJob coherenceJob = CfCurriculumPipelineJob.builder()
                .curriculumId(curriculum.getId())
                .jobType("CURRICULUM_COHERENCE_QA")
                .payload(Map.of(
                        "languageCode", curriculum.getLanguageCode(),
                        "languageDisplayName", curriculum.getDisplayName(),
                        "domainCode", curriculum.getDomainCode()
                ))
                .build();
        jobStore.enqueue(coherenceJob);
        log.info("CurriculumBatchProcessor: enqueued CURRICULUM_COHERENCE_QA for curriculum {}",
                curriculum.getId());
    }
}
