package com.dsalearner.pipeline.curriculum.job;

import com.dsalearner.pipeline.model.entity.CfCurriculumPipelineJob;
import com.dsalearner.pipeline.repository.CfCurriculumPipelineJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Polls the curriculum job queue (separate Redis key from lesson jobs)
 * and executes curriculum pipeline jobs: blueprint generation, level QA, coherence QA.
 *
 * All @Modifying / @Transactional operations are delegated to CurriculumJobStore so
 * Spring AOP proxy intercepts them correctly (self-calls in the same class are not
 * intercepted).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CurriculumJobWorker {

    static final String QUEUE_KEY = "cf:curriculum:jobs";

    private final CurriculumJobStore jobStore;
    private final CfCurriculumPipelineJobRepository jobRepository;
    private final StringRedisTemplate redisTemplate;
    private final CurriculumBlueprintOrchestrator blueprintOrchestrator;
    private final CurriculumLevelQaOrchestrator levelQaOrchestrator;
    private final CurriculumCoherenceQaOrchestrator coherenceQaOrchestrator;

    @Scheduled(fixedDelay = 2000)
    public void poll() {
        String jobIdStr = redisTemplate.opsForList().leftPop(QUEUE_KEY);
        if (jobIdStr == null) return;

        UUID jobId;
        try {
            jobId = UUID.fromString(jobIdStr);
        } catch (IllegalArgumentException e) {
            log.error("CurriculumJobWorker: invalid job ID on queue: {}", jobIdStr);
            return;
        }

        Optional<CfCurriculumPipelineJob> maybeClaimed = jobStore.claim(jobId);
        if (maybeClaimed.isEmpty()) {
            log.info("CurriculumJobWorker: jobId={} claim failed — already owned or completed", jobId);
            return;
        }

        CfCurriculumPipelineJob job = maybeClaimed.get();
        log.info("CurriculumJobWorker: claimed jobId={} curriculumId={} type={} attempt={}/{}",
                jobId, job.getCurriculumId(), job.getJobType(), job.getAttempt(), job.getMaxAttempts());

        String resultRef;
        try {
            resultRef = dispatch(job);
        } catch (Exception e) {
            handleFailure(job, e);
            return;
        }

        jobStore.markSucceeded(jobId, resultRef);
        log.info("CurriculumJobWorker: SUCCEEDED jobId={}", jobId);
    }

    private String dispatch(CfCurriculumPipelineJob job) {
        return switch (job.getJobType()) {
            case "CURRICULUM_BLUEPRINT"    -> blueprintOrchestrator.execute(job);
            case "CURRICULUM_LEVEL_QA"     -> levelQaOrchestrator.execute(job);
            case "CURRICULUM_COHERENCE_QA" -> coherenceQaOrchestrator.execute(job);
            default -> throw new IllegalArgumentException("Unknown curriculum job type: " + job.getJobType());
        };
    }

    private void handleFailure(CfCurriculumPipelineJob job, Exception e) {
        boolean canRetry = job.getAttempt() < job.getMaxAttempts();
        log.error("CurriculumJobWorker: jobId={} attempt={} error={}",
                job.getId(), job.getAttempt(), e.getMessage(), e);

        if (canRetry) {
            jobStore.markRetrying(job.getId(), e.getMessage());
            redisTemplate.opsForList().rightPush(QUEUE_KEY, job.getId().toString());
            log.info("CurriculumJobWorker: jobId={} RETRYING ({}/{})",
                    job.getId(), job.getAttempt(), job.getMaxAttempts());
        } else {
            jobStore.markFailed(job.getId(), e.getMessage());
        }
    }

    /**
     * Enqueue a new curriculum job. Delegates to CurriculumJobStore for the
     * @Transactional boundary.
     */
    public CfCurriculumPipelineJob enqueue(CfCurriculumPipelineJob job) {
        return jobStore.enqueue(job);
    }

    /** Expose repository for scheduler queries. */
    public CfCurriculumPipelineJobRepository getJobRepository() {
        return jobRepository;
    }
}
