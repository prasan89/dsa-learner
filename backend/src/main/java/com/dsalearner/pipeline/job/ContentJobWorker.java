package com.dsalearner.pipeline.job;

import com.dsalearner.pipeline.language.german.qa.QaOrchestrator;
import com.dsalearner.pipeline.model.entity.CfPipelineJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Polls the Redis job queue and executes Content Factory pipeline jobs.
 *
 * Job lifecycle:
 *   QUEUED → RUNNING → SUCCEEDED
 *                    → RETRYING (transient failure, attempt < maxAttempts)
 *                    → FAILED   (non-retryable or attempt == maxAttempts)
 *
 * Atomic duplicate-delivery safety:
 *   JobClaimService.claim() issues a single UPDATE...WHERE status IN ('QUEUED','RETRYING').
 *   Only the worker whose UPDATE affects 1 row owns the job.
 *   A second delivery of the same job ID gets 0 rows updated and is silently skipped.
 *   This remains safe across multiple JVM instances because the safety boundary is
 *   PostgreSQL row-level locking, not a JVM-local lock.
 *
 * Transaction separation:
 *   Claim and terminal-state persistence each run in their own short transaction.
 *   The LLM network call (via ContentGenerationOrchestrator) runs BETWEEN them,
 *   with no database connection held open during the external call.
 *
 * Idempotency:
 *   AgentRunner's existing input-hash mechanism prevents duplicate LLM calls
 *   even if the worker restarts mid-execution after claiming a job.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContentJobWorker {

    private final JobClaimService claimService;
    private final StringRedisTemplate redisTemplate;
    private final ContentGenerationOrchestrator generationOrchestrator;
    private final QaOrchestrator qaOrchestrator;

    @Scheduled(fixedDelay = 500)
    public void poll() {
        String jobIdStr = redisTemplate.opsForList()
                .leftPop(RedisContentJobQueue.QUEUE_KEY);
        if (jobIdStr == null) return;

        UUID jobId;
        try {
            jobId = UUID.fromString(jobIdStr);
        } catch (IllegalArgumentException e) {
            log.error("ContentJobWorker: invalid job ID on queue: {}", jobIdStr);
            return;
        }

        // ── 1. Atomic claim (own transaction; no DB connection held after commit) ──
        Optional<CfPipelineJob> maybeClaimed = claimService.claim(jobId);
        if (maybeClaimed.isEmpty()) {
            log.info("ContentJobWorker: jobId={} claim failed — already owned or completed, skipping", jobId);
            return;
        }

        CfPipelineJob job = maybeClaimed.get();
        log.info("ContentJobWorker: claimed jobId={} lessonId={} attempt={}/{}",
                jobId, job.getLessonId(), job.getAttempt(), job.getMaxAttempts());

        // ── 2. Execute (no DB transaction; LLM call is a pure network operation) ──
        String resultRef;
        try {
            resultRef = dispatch(job);
        } catch (Exception e) {
            handleFailure(job, e);
            return;
        }

        // ── 3. Persist success (own transaction) ──
        claimService.markSucceeded(jobId, resultRef);
        log.info("ContentJobWorker: SUCCEEDED jobId={} lessonId={}", jobId, job.getLessonId());
    }

    private String dispatch(CfPipelineJob job) {
        return switch (job.getJobType()) {
            case "CONTENT_GENERATION" -> generationOrchestrator.execute(job);
            case "QA_CONTENT"         -> qaOrchestrator.execute(job);
            default -> throw new IllegalArgumentException("Unknown job type: " + job.getJobType());
        };
    }

    private void handleFailure(CfPipelineJob job, Exception e) {
        boolean retryable = RetryPolicy.isRetryable(e);
        boolean canRetry  = retryable && job.getAttempt() < job.getMaxAttempts();

        log.error("ContentJobWorker: jobId={} attempt={} retryable={} error={}",
                job.getId(), job.getAttempt(), retryable, e.getMessage(), e);

        if (canRetry) {
            claimService.markRetrying(job.getId(), abbreviate(e.getMessage(), 500),
                    RedisContentJobQueue.QUEUE_KEY, redisTemplate);
            log.info("ContentJobWorker: jobId={} RETRYING (attempt {}/{})",
                    job.getId(), job.getAttempt(), job.getMaxAttempts());
        } else {
            claimService.markFailed(job.getId(), abbreviate(e.getMessage(), 2000));
            log.error("ContentJobWorker: jobId={} FAILED (retryable={} attempts={}/{})",
                    job.getId(), retryable, job.getAttempt(), job.getMaxAttempts());
        }
    }

    private String abbreviate(String msg, int max) {
        if (msg == null) return "unknown error";
        return msg.length() > max ? msg.substring(0, max) + "..." : msg;
    }
}
