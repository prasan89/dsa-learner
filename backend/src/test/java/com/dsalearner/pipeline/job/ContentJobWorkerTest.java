package com.dsalearner.pipeline.job;

import com.dsalearner.pipeline.exception.InvalidTransitionException;
import com.dsalearner.pipeline.model.entity.CfPipelineJob;
import com.dsalearner.pipeline.provider.LlmProviderException;
import com.dsalearner.pipeline.repository.CfPipelineJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests ContentJobWorker via the new JobClaimService+atomic-claim path.
 *
 * Atomic claim semantics are verified by controlling what claimService.claim() returns:
 *   - empty()  → another worker already owns the job (claim failed → skip)
 *   - present  → this worker owns the job (proceed)
 *
 * This mirrors what the DB UPDATE...WHERE actually does in production.
 */
@ExtendWith(MockitoExtension.class)
class ContentJobWorkerTest {

    @Mock JobClaimService claimService;
    @Mock StringRedisTemplate redisTemplate;
    @Mock ContentGenerationOrchestrator orchestrator;
    @Mock ListOperations<String, String> listOps;

    @InjectMocks ContentJobWorker worker;

    private final UUID lessonId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        when(redisTemplate.opsForList()).thenReturn(listOps);
    }

    // ─── Basic polling ─────────────────────────────────────────────────────

    @Test
    void pollDoesNothingWhenQueueIsEmpty() {
        when(listOps.leftPop(any(), anyLong(), any(TimeUnit.class))).thenReturn(null);
        worker.poll();
        verify(claimService, never()).claim(any());
    }

    @Test
    void invalidUuidOnQueueIsSkipped() {
        when(listOps.leftPop(any(), anyLong(), any(TimeUnit.class))).thenReturn("not-a-uuid");
        worker.poll();
        verify(claimService, never()).claim(any());
    }

    // ─── Successful execution ──────────────────────────────────────────────

    @Test
    void successfulExecutionMarksJobSucceeded() {
        UUID jobId = UUID.randomUUID();
        CfPipelineJob job = buildJob(jobId, "RUNNING", 1, 3);

        when(listOps.leftPop(any(), anyLong(), any(TimeUnit.class))).thenReturn(jobId.toString());
        when(claimService.claim(jobId)).thenReturn(Optional.of(job));
        when(orchestrator.execute(any())).thenReturn("run-ref-123");

        worker.poll();

        verify(claimService).markSucceeded(eq(jobId), eq("run-ref-123"));
        verify(claimService, never()).markFailed(any(), any());
        verify(claimService, never()).markRetrying(any(), any(), any(), any());
    }

    // ─── Atomic claim / duplicate delivery ────────────────────────────────

    @Test
    void claimFailureSkipsExecution() {
        UUID jobId = UUID.randomUUID();

        when(listOps.leftPop(any(), anyLong(), any(TimeUnit.class))).thenReturn(jobId.toString());
        // Claim returns empty → another worker already owns this job
        when(claimService.claim(jobId)).thenReturn(Optional.empty());

        worker.poll();

        verify(orchestrator, never()).execute(any());
        verify(claimService, never()).markSucceeded(any(), any());
        verify(claimService, never()).markFailed(any(), any());
    }

    /**
     * Concurrent duplicate delivery test:
     * Worker A claims job → succeeds.
     * Worker B receives the same job ID from Redis → claim returns empty → skipped.
     *
     * We can't spawn real threads because claimService is a mock, but we simulate
     * the conditional-update semantics by having the second call return Optional.empty().
     * This verifies that the worker code correctly handles the 0-rows-updated case.
     */
    @Test
    void secondDeliveryOfSameJobIsSkippedWhenClaimFails() {
        UUID jobId = UUID.randomUUID();
        CfPipelineJob job = buildJob(jobId, "RUNNING", 1, 3);

        when(listOps.leftPop(any(), anyLong(), any(TimeUnit.class)))
                .thenReturn(jobId.toString())   // first delivery
                .thenReturn(jobId.toString())   // second delivery (duplicate)
                .thenReturn(null);              // queue drains

        // First claim: worker A wins
        when(claimService.claim(jobId))
                .thenReturn(Optional.of(job))   // worker A
                .thenReturn(Optional.empty());  // worker B — DB UPDATE returned 0 rows

        when(orchestrator.execute(any())).thenReturn("run-ref");

        // Simulate worker A's poll
        worker.poll();
        // Simulate worker B's poll (duplicate delivery)
        worker.poll();

        // Orchestrator called exactly once (only worker A executed)
        verify(orchestrator, times(1)).execute(any());
        // markSucceeded called exactly once
        verify(claimService, times(1)).markSucceeded(any(), any());
    }

    // ─── Transient failure → RETRYING ─────────────────────────────────────

    @Test
    void transientFailureMovesToRetrying() {
        UUID jobId = UUID.randomUUID();
        CfPipelineJob job = buildJob(jobId, "RUNNING", 1, 3);

        when(listOps.leftPop(any(), anyLong(), any(TimeUnit.class))).thenReturn(jobId.toString());
        when(claimService.claim(jobId)).thenReturn(Optional.of(job));
        when(orchestrator.execute(any())).thenThrow(new LlmProviderException("timeout", true));

        worker.poll();

        verify(claimService).markRetrying(eq(jobId), contains("timeout"),
                eq(RedisContentJobQueue.QUEUE_KEY), any());
        verify(claimService, never()).markSucceeded(any(), any());
        verify(claimService, never()).markFailed(any(), any());
    }

    // ─── Max attempts → FAILED (no more retries even if retryable) ────────

    @Test
    void maxAttemptsReachedMarksFailed() {
        UUID jobId = UUID.randomUUID();
        // attempt == maxAttempts → canRetry is false even for retryable errors
        CfPipelineJob job = buildJob(jobId, "RUNNING", 3, 3);

        when(listOps.leftPop(any(), anyLong(), any(TimeUnit.class))).thenReturn(jobId.toString());
        when(claimService.claim(jobId)).thenReturn(Optional.of(job));
        when(orchestrator.execute(any())).thenThrow(new LlmProviderException("timeout", true));

        worker.poll();

        verify(claimService).markFailed(eq(jobId), any());
        verify(claimService, never()).markRetrying(any(), any(), any(), any());
    }

    // ─── Non-retryable → FAILED immediately ───────────────────────────────

    @Test
    void nonRetryableErrorMarksFailed() {
        UUID jobId = UUID.randomUUID();
        CfPipelineJob job = buildJob(jobId, "RUNNING", 1, 3);

        when(listOps.leftPop(any(), anyLong(), any(TimeUnit.class))).thenReturn(jobId.toString());
        when(claimService.claim(jobId)).thenReturn(Optional.of(job));
        when(orchestrator.execute(any())).thenThrow(
                new InvalidTransitionException("Invalid state transition"));

        worker.poll();

        verify(claimService).markFailed(eq(jobId), any());
        verify(claimService, never()).markRetrying(any(), any(), any(), any());
    }

    // ─── Retry succeeds on second attempt ─────────────────────────────────

    @Test
    void retrySucceedsOnSecondAttempt() {
        UUID jobId = UUID.randomUUID();
        // First attempt (attempt=1): transient failure → RETRYING
        CfPipelineJob firstAttempt  = buildJob(jobId, "RUNNING", 1, 3);
        // Second attempt (attempt=2): success
        CfPipelineJob secondAttempt = buildJob(jobId, "RUNNING", 2, 3);

        when(listOps.leftPop(any(), anyLong(), any(TimeUnit.class)))
                .thenReturn(jobId.toString())  // first delivery
                .thenReturn(jobId.toString())  // retry delivery
                .thenReturn(null);

        when(claimService.claim(jobId))
                .thenReturn(Optional.of(firstAttempt))   // first attempt wins
                .thenReturn(Optional.of(secondAttempt)); // retry wins

        when(orchestrator.execute(any()))
                .thenThrow(new LlmProviderException("timeout", true))  // attempt 1 fails
                .thenReturn("run-ref-success");                         // attempt 2 succeeds

        worker.poll(); // attempt 1 → RETRYING
        worker.poll(); // attempt 2 → SUCCEEDED

        verify(claimService).markRetrying(eq(jobId), any(), any(), any());
        verify(claimService).markSucceeded(eq(jobId), eq("run-ref-success"));
        verify(orchestrator, times(2)).execute(any());
    }

    // ─── Unknown job type ──────────────────────────────────────────────────

    @Test
    void unknownJobTypeMarksFailed() {
        UUID jobId = UUID.randomUUID();
        CfPipelineJob job = buildJob(jobId, "RUNNING", 1, 3);
        job.setJobType("UNKNOWN_TYPE");

        when(listOps.leftPop(any(), anyLong(), any(TimeUnit.class))).thenReturn(jobId.toString());
        when(claimService.claim(jobId)).thenReturn(Optional.of(job));

        worker.poll();

        verify(claimService).markFailed(eq(jobId), any());
    }

    private CfPipelineJob buildJob(UUID id, String status, int attempt, int maxAttempts) {
        return CfPipelineJob.builder()
                .id(id).lessonId(lessonId).lessonVersion(1)
                .jobType("CONTENT_GENERATION")
                .status(status).attempt(attempt).maxAttempts(maxAttempts)
                .payload(Map.of("topic", "Greetings"))
                .build();
    }
}
