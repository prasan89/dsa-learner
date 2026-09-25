package com.dsalearner.pipeline.job;

import com.dsalearner.pipeline.model.entity.CfPipelineJob;
import com.dsalearner.pipeline.repository.CfPipelineJobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests atomic job claiming and terminal state transitions in JobClaimService.
 *
 * Key invariant: claimJob() returning 0 means the job was NOT claimed by this worker.
 * The repository's @Modifying UPDATE is the actual DB-level atomicity boundary;
 * here we verify that the service layer correctly interprets the row-count result.
 */
@ExtendWith(MockitoExtension.class)
class JobClaimServiceTest {

    @Mock CfPipelineJobRepository jobRepository;
    @InjectMocks JobClaimService claimService;

    private final UUID jobId = UUID.randomUUID();
    private final UUID lessonId = UUID.randomUUID();

    // ─── Successful claim ──────────────────────────────────────────────────

    @Test
    void claimReturnsJobWhenUpdateAffectsOneRow() {
        CfPipelineJob claimed = buildJob("RUNNING", 1);
        when(jobRepository.claimJob(eq(jobId), any(Instant.class))).thenReturn(1);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(claimed));

        Optional<CfPipelineJob> result = claimService.claim(jobId);

        assertTrue(result.isPresent());
        assertSame(claimed, result.get());
        verify(jobRepository).findById(jobId);
    }

    // ─── Failed claim (concurrent worker already owns it) ─────────────────

    @Test
    void claimReturnsEmptyWhenUpdateAffectsZeroRows() {
        when(jobRepository.claimJob(eq(jobId), any(Instant.class))).thenReturn(0);

        Optional<CfPipelineJob> result = claimService.claim(jobId);

        assertTrue(result.isEmpty());
        // Must NOT reload the job — no point if we don't own it
        verify(jobRepository, never()).findById(any());
    }

    /**
     * Proves the conditional-update semantics directly:
     * - First call: 1 row updated → claimed
     * - Second call with same jobId: 0 rows updated → not claimed
     *
     * In production, the DB enforces this because status is no longer
     * IN ('QUEUED','RETRYING') after the first claim (it's 'RUNNING').
     */
    @Test
    void concurrentClaimsOnlyOneSucceeds() {
        CfPipelineJob claimed = buildJob("RUNNING", 1);
        when(jobRepository.claimJob(eq(jobId), any(Instant.class)))
                .thenReturn(1)   // worker A wins
                .thenReturn(0);  // worker B loses (DB row no longer QUEUED/RETRYING)
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(claimed));

        Optional<CfPipelineJob> resultA = claimService.claim(jobId);
        Optional<CfPipelineJob> resultB = claimService.claim(jobId);

        assertTrue(resultA.isPresent(),  "Worker A must own the job");
        assertTrue(resultB.isEmpty(),    "Worker B must be rejected");
        // findById called only once — only for the successful claimer
        verify(jobRepository, times(1)).findById(jobId);
    }

    // ─── Terminal state persistence ────────────────────────────────────────

    @Test
    void markSucceededSetsStatusAndResultReference() {
        CfPipelineJob job = buildJob("RUNNING", 1);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        claimService.markSucceeded(jobId, "agent-run-abc");

        assertEquals("SUCCEEDED", job.getStatus());
        assertEquals("agent-run-abc", job.getResultReference());
        assertNotNull(job.getCompletedAt());
    }

    @Test
    void markFailedSetsStatusAndError() {
        CfPipelineJob job = buildJob("RUNNING", 1);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        claimService.markFailed(jobId, "bad response from LLM");

        assertEquals("FAILED", job.getStatus());
        assertEquals("bad response from LLM", job.getError());
        assertNotNull(job.getCompletedAt());
    }

    @Test
    void markRetryingSetsStatusAndEnqueues() {
        CfPipelineJob job = buildJob("RUNNING", 1);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ListOperations<String, String> listOps = mock(ListOperations.class);
        when(redis.opsForList()).thenReturn(listOps);
        when(listOps.rightPush(any(), any())).thenReturn(1L);

        claimService.markRetrying(jobId, "timeout", "cf:pipeline:jobs", redis);

        assertEquals("RETRYING", job.getStatus());
        assertEquals("timeout", job.getError());
        verify(listOps).rightPush(eq("cf:pipeline:jobs"), eq(jobId.toString()));
    }

    private CfPipelineJob buildJob(String status, int attempt) {
        return CfPipelineJob.builder()
                .id(jobId).lessonId(lessonId).lessonVersion(1)
                .jobType("CONTENT_GENERATION")
                .status(status).attempt(attempt).maxAttempts(3)
                .payload(Map.of())
                .build();
    }
}
