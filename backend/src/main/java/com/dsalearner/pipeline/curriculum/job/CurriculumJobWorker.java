package com.dsalearner.pipeline.curriculum.job;

import com.dsalearner.pipeline.model.entity.CfCurriculumPipelineJob;
import com.dsalearner.pipeline.repository.CfCurriculumPipelineJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Polls the curriculum job queue (separate Redis key from lesson jobs)
 * and executes curriculum pipeline jobs: blueprint generation, level QA, coherence QA.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CurriculumJobWorker {

    static final String QUEUE_KEY = "cf:curriculum:jobs";

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

        Optional<CfCurriculumPipelineJob> maybeClaimed = claim(jobId);
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

        markSucceeded(jobId, resultRef);
        log.info("CurriculumJobWorker: SUCCEEDED jobId={}", jobId);
    }

    private String dispatch(CfCurriculumPipelineJob job) {
        return switch (job.getJobType()) {
            case "CURRICULUM_BLUEPRINT"  -> blueprintOrchestrator.execute(job);
            case "CURRICULUM_LEVEL_QA"  -> levelQaOrchestrator.execute(job);
            case "CURRICULUM_COHERENCE_QA" -> coherenceQaOrchestrator.execute(job);
            default -> throw new IllegalArgumentException("Unknown curriculum job type: " + job.getJobType());
        };
    }

    @Transactional
    public Optional<CfCurriculumPipelineJob> claim(UUID jobId) {
        int claimed = jobRepository.claimJob(jobId);
        if (claimed == 0) return Optional.empty();
        CfCurriculumPipelineJob job = jobRepository.findById(jobId).orElse(null);
        if (job != null) {
            job.setStartedAt(Instant.now());
            jobRepository.save(job);
        }
        return Optional.ofNullable(job);
    }

    @Transactional
    public void markSucceeded(UUID jobId, String resultRef) {
        jobRepository.markSucceeded(jobId, resultRef);
    }

    @Transactional
    public void markFailed(UUID jobId, String error) {
        jobRepository.markFailed(jobId, abbreviate(error, 2000));
    }

    private void handleFailure(CfCurriculumPipelineJob job, Exception e) {
        boolean canRetry = job.getAttempt() < job.getMaxAttempts();
        log.error("CurriculumJobWorker: jobId={} attempt={} error={}",
                job.getId(), job.getAttempt(), e.getMessage(), e);

        if (canRetry) {
            markRetrying(job.getId(), abbreviate(e.getMessage(), 500));
            redisTemplate.opsForList().rightPush(QUEUE_KEY, job.getId().toString());
            log.info("CurriculumJobWorker: jobId={} RETRYING ({}/{})",
                    job.getId(), job.getAttempt(), job.getMaxAttempts());
        } else {
            markFailed(job.getId(), abbreviate(e.getMessage(), 2000));
        }
    }

    @Transactional
    public void markRetrying(UUID jobId, String error) {
        jobRepository.markRetrying(jobId, error);
    }

    /**
     * Enqueue a new curriculum job and push its ID to Redis.
     * Returns the saved job.
     */
    @Transactional
    public CfCurriculumPipelineJob enqueue(CfCurriculumPipelineJob job) {
        CfCurriculumPipelineJob saved = jobRepository.save(job);
        redisTemplate.opsForList().rightPush(QUEUE_KEY, saved.getId().toString());
        log.info("CurriculumJobWorker: enqueued jobId={} curriculumId={} type={}",
                saved.getId(), saved.getCurriculumId(), saved.getJobType());
        return saved;
    }

    private String abbreviate(String msg, int max) {
        if (msg == null) return "unknown error";
        return msg.length() > max ? msg.substring(0, max) + "..." : msg;
    }

    /** Expose repository for scheduler queries. */
    public CfCurriculumPipelineJobRepository getJobRepository() {
        return jobRepository;
    }
}
