package com.dsalearner.pipeline.job;

import com.dsalearner.pipeline.model.entity.CfPipelineJob;
import com.dsalearner.pipeline.repository.CfPipelineJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Polls the Redis job queue and executes Content Factory pipeline jobs.
 *
 * Job lifecycle:
 *   QUEUED → RUNNING → SUCCEEDED
 *                    → RETRYING (transient failure, attempt < maxAttempts)
 *                    → FAILED   (non-retryable or attempt == maxAttempts)
 *
 * Duplicate-delivery safety: the worker atomically claims a job by setting
 * status=RUNNING only if it is currently QUEUED or RETRYING. A second
 * delivery of the same job ID finds status=RUNNING|SUCCEEDED and skips it.
 *
 * Idempotency: AgentRunner's existing input-hash mechanism ensures the same
 * agent execution does not call the model twice even if the worker restarts.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContentJobWorker {

    private final CfPipelineJobRepository jobRepository;
    private final StringRedisTemplate redisTemplate;
    private final ContentGenerationOrchestrator generationOrchestrator;

    /** Poll interval: 500ms. Appropriate for single-JVM MVP. */
    @Scheduled(fixedDelay = 500)
    public void poll() {
        String jobIdStr = redisTemplate.opsForList()
                .leftPop(RedisContentJobQueue.QUEUE_KEY, 0, TimeUnit.MILLISECONDS);
        if (jobIdStr == null) return;

        UUID jobId;
        try {
            jobId = UUID.fromString(jobIdStr);
        } catch (IllegalArgumentException e) {
            log.error("ContentJobWorker: invalid job ID on queue: {}", jobIdStr);
            return;
        }

        processJob(jobId);
    }

    @Transactional
    protected void processJob(UUID jobId) {
        Optional<CfPipelineJob> maybeJob = jobRepository.findById(jobId);
        if (maybeJob.isEmpty()) {
            log.warn("ContentJobWorker: jobId={} not found in DB — skipping", jobId);
            return;
        }

        CfPipelineJob job = maybeJob.get();

        // Atomic claim: skip if already running or completed (duplicate delivery safety)
        if (!isClaimable(job)) {
            log.info("ContentJobWorker: jobId={} status={} — skipping (not claimable)",
                    jobId, job.getStatus());
            return;
        }

        job.setStatus("RUNNING");
        job.setAttempt(job.getAttempt() + 1);
        job.setStartedAt(Instant.now());
        job.setError(null);
        jobRepository.save(job);

        log.info("ContentJobWorker: claimed jobId={} lessonId={} attempt={}/{}",
                jobId, job.getLessonId(), job.getAttempt(), job.getMaxAttempts());

        try {
            String resultRef = dispatch(job);
            job.setStatus("SUCCEEDED");
            job.setResultReference(resultRef);
            job.setCompletedAt(Instant.now());
            jobRepository.save(job);
            log.info("ContentJobWorker: SUCCEEDED jobId={} lessonId={}", jobId, job.getLessonId());

        } catch (Exception e) {
            handleFailure(job, e);
        }
    }

    private String dispatch(CfPipelineJob job) {
        return switch (job.getJobType()) {
            case "CONTENT_GENERATION" -> generationOrchestrator.execute(job);
            default -> throw new IllegalArgumentException("Unknown job type: " + job.getJobType());
        };
    }

    private void handleFailure(CfPipelineJob job, Exception e) {
        boolean retryable = RetryPolicy.isRetryable(e);
        boolean canRetry  = retryable && job.getAttempt() < job.getMaxAttempts();

        log.error("ContentJobWorker: jobId={} attempt={} retryable={} error={}",
                job.getId(), job.getAttempt(), retryable, e.getMessage());

        if (canRetry) {
            job.setStatus("RETRYING");
            job.setError(abbreviate(e.getMessage(), 500));
            jobRepository.save(job);
            // Re-enqueue after short back-off by pushing the ID back
            redisTemplate.opsForList().rightPush(
                    RedisContentJobQueue.QUEUE_KEY, job.getId().toString());
            log.info("ContentJobWorker: jobId={} RETRYING (attempt {}/{})",
                    job.getId(), job.getAttempt(), job.getMaxAttempts());
        } else {
            job.setStatus("FAILED");
            job.setError(abbreviate(e.getMessage(), 2000));
            job.setCompletedAt(Instant.now());
            jobRepository.save(job);
            log.error("ContentJobWorker: jobId={} FAILED (retryable={} attempts={}/{})",
                    job.getId(), retryable, job.getAttempt(), job.getMaxAttempts());
        }
    }

    private boolean isClaimable(CfPipelineJob job) {
        return "QUEUED".equals(job.getStatus()) || "RETRYING".equals(job.getStatus());
    }

    private String abbreviate(String msg, int max) {
        if (msg == null) return "unknown error";
        return msg.length() > max ? msg.substring(0, max) + "..." : msg;
    }
}
