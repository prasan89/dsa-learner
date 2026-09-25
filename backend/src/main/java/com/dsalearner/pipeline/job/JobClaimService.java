package com.dsalearner.pipeline.job;

import com.dsalearner.pipeline.model.entity.CfPipelineJob;
import com.dsalearner.pipeline.repository.CfPipelineJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Handles atomic job claiming and terminal-state persistence as separate transactions.
 *
 * Two concerns are kept here so ContentJobWorker can call them through the Spring
 * proxy (ensuring @Transactional actually fires) while the LLM network call lives
 * outside any database transaction.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobClaimService {

    private final CfPipelineJobRepository jobRepository;

    /**
     * Atomically claims a job via a conditional UPDATE.
     *
     * Issues a single UPDATE...WHERE status IN ('QUEUED','RETRYING') at the
     * database level. Returns the loaded job if exactly one row was updated
     * (this worker owns it), or empty if another worker already claimed it.
     *
     * The attempt counter is incremented and error cleared in the same UPDATE
     * so the record is always consistent after the transaction commits.
     */
    @Transactional
    public Optional<CfPipelineJob> claim(UUID jobId) {
        int updated = jobRepository.claimJob(jobId, Instant.now());
        if (updated == 0) {
            return Optional.empty();
        }
        // Reload so the in-memory entity reflects the DB state (attempt++, status=RUNNING)
        return jobRepository.findById(jobId);
    }

    /**
     * Persists SUCCEEDED terminal state. Called after the LLM call completes,
     * outside the claim transaction.
     */
    @Transactional
    public void markSucceeded(UUID jobId, String resultReference) {
        jobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus("SUCCEEDED");
            job.setResultReference(resultReference);
            job.setCompletedAt(Instant.now());
            jobRepository.save(job);
        });
    }

    /**
     * Persists RETRYING state and re-enqueues via Redis. Called on transient failure.
     */
    @Transactional
    public void markRetrying(UUID jobId, String errorMessage,
                             String queueKey,
                             org.springframework.data.redis.core.StringRedisTemplate redisTemplate) {
        jobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus("RETRYING");
            job.setError(errorMessage);
            jobRepository.save(job);
            // Re-enqueue inside the same transaction boundary so status and queue entry are consistent
            redisTemplate.opsForList().rightPush(queueKey, jobId.toString());
        });
    }

    /**
     * Persists FAILED terminal state. Called on non-retryable failure or max attempts.
     */
    @Transactional
    public void markFailed(UUID jobId, String errorMessage) {
        jobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus("FAILED");
            job.setError(errorMessage);
            job.setCompletedAt(Instant.now());
            jobRepository.save(job);
        });
    }
}
