package com.dsalearner.pipeline.curriculum.job;

import com.dsalearner.pipeline.curriculum.service.CurriculumDependencyService;
import com.dsalearner.pipeline.curriculum.service.CurriculumLevelService;
import com.dsalearner.pipeline.curriculum.service.CurriculumService;
import com.dsalearner.pipeline.model.entity.CfCurriculum;
import com.dsalearner.pipeline.model.entity.CfCurriculumLevel;
import com.dsalearner.pipeline.model.entity.CfCurriculumLessonPlan;
import com.dsalearner.pipeline.model.entity.CfCurriculumPipelineJob;
import com.dsalearner.pipeline.repository.CfCurriculumLessonPlanRepository;
import com.dsalearner.pipeline.repository.CfCurriculumRepository;
import com.dsalearner.pipeline.repository.CfLessonRepository;
import com.dsalearner.pipeline.service.PipelineJobService;
import com.dsalearner.pipeline.service.PipelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Periodic scheduler that:
 *  1. Finds active curriculum levels in GENERATION_IN_PROGRESS.
 *  2. Refreshes PLANNED→QUEUED/BLOCKED status for their lesson plans.
 *  3. Claims up to batchSize QUEUED plans atomically via compareAndSetStatus.
 *  4. Creates a CfLesson + enqueues a CONTENT_GENERATION job for each claimed plan.
 *  5. Checks for level completion → submits CURRICULUM_LEVEL_QA.
 *  6. Checks for full curriculum completion → submits CURRICULUM_COHERENCE_QA.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CurriculumBatchScheduler {

    private final CfCurriculumRepository curriculumRepository;
    private final CfCurriculumLessonPlanRepository lessonPlanRepository;
    private final CfLessonRepository lessonRepository;
    private final CurriculumService curriculumService;
    private final CurriculumLevelService levelService;
    private final CurriculumDependencyService dependencyService;
    private final PipelineService pipelineService;
    private final PipelineJobService pipelineJobService;
    private final CurriculumJobWorker curriculumJobWorker;

    @Scheduled(fixedDelay = 30_000)
    public void tick() {
        // Process all curricula that are actively generating
        List<CfCurriculum> active = curriculumRepository.findByCurriculumStatus("GENERATION_IN_PROGRESS");
        for (CfCurriculum curriculum : active) {
            try {
                processCurriculum(curriculum);
            } catch (Exception e) {
                log.error("CurriculumBatchScheduler: error processing curriculum {} — {}",
                        curriculum.getId(), e.getMessage(), e);
            }
        }
    }

    @Transactional
    public void processCurriculum(CfCurriculum curriculum) {
        UUID curriculumId = curriculum.getId();
        int batchSize = curriculum.getBatchSize();

        List<CfCurriculumLevel> activeLevels = levelService.findEligibleLevels(curriculumId);

        // Start eligible PLANNED levels
        for (CfCurriculumLevel level : activeLevels) {
            if ("PLANNED".equals(level.getLevelStatus())) {
                levelService.startGeneration(level.getId(), "batch_scheduler");
                // Seed all PLANNED plans as QUEUED/BLOCKED based on dependencies
                dependencyService.refreshBlockedStatus(level.getId());
            }
        }

        // Fetch all GENERATION_IN_PROGRESS levels and dispatch batches
        List<CfCurriculumLevel> generatingLevels = curriculumService.getLevels(curriculumId).stream()
                .filter(l -> "GENERATION_IN_PROGRESS".equals(l.getLevelStatus()))
                .toList();

        for (CfCurriculumLevel level : generatingLevels) {
            dispatchBatch(curriculum, level, batchSize);
            checkLevelCompletion(curriculum, level);
        }

        // Check if full curriculum generation is complete
        checkCurriculumCompletion(curriculum);
    }

    private void dispatchBatch(CfCurriculum curriculum, CfCurriculumLevel level, int batchSize) {
        // Resolve unblocked QUEUED plans
        List<CfCurriculumLessonPlan> unblocked = dependencyService.resolveUnblocked(level.getId());
        int dispatched = 0;

        for (CfCurriculumLessonPlan plan : unblocked) {
            if (dispatched >= batchSize) break;
            if (plan.getLessonId() != null) continue; // already provisioned

            // Atomic claim via compareAndSetStatus
            int claimed = lessonPlanRepository.compareAndSetStatus(
                    plan.getId(), "QUEUED", "GENERATING");
            if (claimed == 0) continue; // another instance claimed it

            try {
                dispatchPlan(curriculum, level, plan);
                dispatched++;
            } catch (Exception e) {
                log.error("CurriculumBatchScheduler: failed to dispatch plan {} — {}",
                        plan.getStableRef(), e.getMessage(), e);
                // Reset to QUEUED so it can be retried
                lessonPlanRepository.compareAndSetStatus(plan.getId(), "GENERATING", "QUEUED");
            }
        }

        if (dispatched > 0) {
            log.info("CurriculumBatchScheduler: dispatched {} plans for level {} ({})",
                    dispatched, level.getCefrLevel(), curriculum.getLanguageCode());
        }
    }

    private void dispatchPlan(CfCurriculum curriculum, CfCurriculumLevel level, CfCurriculumLessonPlan plan) {
        // Create a CfLesson for this plan if not already created
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
            // Lesson already exists — look it up
            var existing = lessonRepository.findByStableRef(plan.getStableRef())
                    .orElseThrow(() -> new RuntimeException("Lesson exists but lookup failed: " + plan.getStableRef()));
            lessonId = existing.getId();
        }

        // Record the lesson against the plan
        curriculumService.provisionLesson(plan.getId(), lessonId);

        // Transition lesson to PLANNED
        pipelineService.plan(lessonId, "batch_scheduler");

        // Build curriculum context for the generation prompt
        String curriculumContext = buildCurriculumContext(curriculum, level, plan);

        // Enqueue CONTENT_GENERATION job
        pipelineJobService.submitContentGeneration(
                lessonId, 1,
                Map.of(
                        "stableRef", plan.getStableRef(),
                        "curriculumContext", curriculumContext != null ? curriculumContext : ""
                ),
                "batch_scheduler"
        );

        log.info("CurriculumBatchScheduler: dispatched plan {} → lessonId={}", plan.getStableRef(), lessonId);
    }

    private String buildCurriculumContext(CfCurriculum curriculum, CfCurriculumLevel level,
                                           CfCurriculumLessonPlan plan) {
        // Compact context string injected into content generation to maintain coherence
        StringBuilder sb = new StringBuilder();
        sb.append("Curriculum: ").append(curriculum.getDisplayName()).append("\n");
        sb.append("Level: ").append(level.getCefrLevel()).append(" ").append(level.getDisplayName()).append("\n");
        sb.append("Position: ").append(plan.getPosition()).append("\n");
        sb.append("Unit learning goal: ").append(plan.getLearningObjectives() != null
                ? plan.getLearningObjectives() : "").append("\n");
        if (plan.getGrammarTargets() != null && plan.getGrammarTargets().length > 0) {
            sb.append("Grammar focus: ").append(String.join(", ", plan.getGrammarTargets())).append("\n");
        }
        if (plan.getVocabTargets() != null && plan.getVocabTargets().length > 0) {
            sb.append("Vocabulary targets: ").append(String.join(", ", plan.getVocabTargets())).append("\n");
        }
        return sb.toString();
    }

    private void checkLevelCompletion(CfCurriculum curriculum, CfCurriculumLevel level) {
        if (!curriculumService.isLevelGenerationComplete(level.getId())) return;

        // Enqueue CURRICULUM_LEVEL_QA job
        boolean qaAlreadyQueued = curriculumJobWorker.getJobRepository()
                .findByCurriculumIdAndJobTypeAndStatus(curriculum.getId(), "CURRICULUM_LEVEL_QA", "QUEUED")
                .stream().anyMatch(j -> level.getId().equals(j.getLevelId()));
        if (qaAlreadyQueued) return;

        com.dsalearner.pipeline.model.entity.CfCurriculumPipelineJob qaJob =
                com.dsalearner.pipeline.model.entity.CfCurriculumPipelineJob.builder()
                        .curriculumId(curriculum.getId())
                        .levelId(level.getId())
                        .jobType("CURRICULUM_LEVEL_QA")
                        .payload(Map.of(
                                "languageCode", curriculum.getLanguageCode(),
                                "languageDisplayName", curriculum.getDisplayName(),
                                "domainCode", curriculum.getDomainCode()
                        ))
                        .build();
        curriculumJobWorker.enqueue(qaJob);
        log.info("CurriculumBatchScheduler: enqueued CURRICULUM_LEVEL_QA for level {}",
                level.getCefrLevel());
    }

    private void checkCurriculumCompletion(CfCurriculum curriculum) {
        if (!levelService.isCurriculumGenerationComplete(curriculum.getId())) return;

        // All levels QA-passed → submit coherence QA
        boolean coherenceAlreadyQueued = !curriculumJobWorker.getJobRepository()
                .findByCurriculumIdAndJobTypeAndStatus(curriculum.getId(), "CURRICULUM_COHERENCE_QA", "QUEUED")
                .isEmpty();
        if (coherenceAlreadyQueued) return;

        com.dsalearner.pipeline.model.entity.CfCurriculumPipelineJob coherenceJob =
                com.dsalearner.pipeline.model.entity.CfCurriculumPipelineJob.builder()
                        .curriculumId(curriculum.getId())
                        .jobType("CURRICULUM_COHERENCE_QA")
                        .payload(Map.of(
                                "languageCode", curriculum.getLanguageCode(),
                                "languageDisplayName", curriculum.getDisplayName(),
                                "domainCode", curriculum.getDomainCode()
                        ))
                        .build();
        curriculumJobWorker.enqueue(coherenceJob);
        log.info("CurriculumBatchScheduler: enqueued CURRICULUM_COHERENCE_QA for curriculum {}",
                curriculum.getId());
    }
}
