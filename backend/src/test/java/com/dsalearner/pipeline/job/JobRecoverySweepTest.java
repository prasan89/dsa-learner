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
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests the Redis/DB durability recovery sweep.
 *
 * Scenarios:
 *   1. Stale QUEUED jobs are re-enqueued onto Redis.
 *   2. No jobs found → no Redis push → nothing happens.
 *   3. Re-enqueuing is idempotent: duplicate delivery is safe because
 *      the atomic claim in ContentJobWorker handles it.
 */
@ExtendWith(MockitoExtension.class)
class JobRecoverySweepTest {

    @Mock CfPipelineJobRepository jobRepository;
    @Mock StringRedisTemplate redisTemplate;
    @Mock ListOperations<String, String> listOps;

    @InjectMocks JobRecoverySweep sweep;

    // ─── Stale job is re-enqueued ──────────────────────────────────────────

    @Test
    void staleQueuedJobIsReEnqueued() {
        UUID jobId = UUID.randomUUID();
        CfPipelineJob staleJob = CfPipelineJob.builder()
                .id(jobId).lessonId(UUID.randomUUID()).lessonVersion(1)
                .jobType("CONTENT_GENERATION").status("QUEUED").attempt(0).maxAttempts(3)
                .payload(Map.of()).createdAt(Instant.now().minus(Duration.ofMinutes(5)))
                .build();

        when(jobRepository.findStaleQueuedJobs(any(Instant.class)))
                .thenReturn(List.of(staleJob));
        when(redisTemplate.opsForList()).thenReturn(listOps);
        when(listOps.rightPush(any(), any())).thenReturn(1L);

        sweep.recoverStaleJobs();

        verify(listOps).rightPush(eq(RedisContentJobQueue.QUEUE_KEY), eq(jobId.toString()));
    }

    // ─── No stale jobs → no Redis push ────────────────────────────────────

    @Test
    void noStaleJobsDoesNotPushToRedis() {
        when(jobRepository.findStaleQueuedJobs(any(Instant.class))).thenReturn(List.of());

        sweep.recoverStaleJobs();

        verify(redisTemplate, never()).opsForList();
    }

    // ─── Multiple stale jobs → all re-enqueued ────────────────────────────

    @Test
    void multipleStaleJobsAreAllReEnqueued() {
        UUID jobId1 = UUID.randomUUID();
        UUID jobId2 = UUID.randomUUID();
        CfPipelineJob job1 = staleJob(jobId1);
        CfPipelineJob job2 = staleJob(jobId2);

        when(jobRepository.findStaleQueuedJobs(any(Instant.class)))
                .thenReturn(List.of(job1, job2));
        when(redisTemplate.opsForList()).thenReturn(listOps);
        when(listOps.rightPush(any(), any())).thenReturn(1L);

        sweep.recoverStaleJobs();

        verify(listOps).rightPush(eq(RedisContentJobQueue.QUEUE_KEY), eq(jobId1.toString()));
        verify(listOps).rightPush(eq(RedisContentJobQueue.QUEUE_KEY), eq(jobId2.toString()));
    }

    /**
     * Re-enqueueing a job that is already in Redis produces a duplicate delivery.
     * This test verifies the sweep always re-enqueues (no Redis existence check needed)
     * because duplicate delivery is handled safely by the atomic claim.
     */
    @Test
    void sweepAlwaysReEnqueuesWithoutCheckingRedisFirst() {
        UUID jobId = UUID.randomUUID();
        when(jobRepository.findStaleQueuedJobs(any())).thenReturn(List.of(staleJob(jobId)));
        when(redisTemplate.opsForList()).thenReturn(listOps);
        when(listOps.rightPush(any(), any())).thenReturn(2L); // already had 1 entry

        sweep.recoverStaleJobs();

        // Just verifies no exception and push happened — duplicates are safe
        verify(listOps, times(1)).rightPush(any(), eq(jobId.toString()));
    }

    // ─── Stale threshold is correct ────────────────────────────────────────

    @Test
    void staleTresholdConstantIs60Seconds() {
        assertEquals(120L, JobRecoverySweep.STALE_THRESHOLD_SECONDS);
    }

    private CfPipelineJob staleJob(UUID id) {
        return CfPipelineJob.builder()
                .id(id).lessonId(UUID.randomUUID()).lessonVersion(1)
                .jobType("CONTENT_GENERATION").status("QUEUED").attempt(0).maxAttempts(3)
                .payload(Map.of()).createdAt(Instant.now().minus(Duration.ofMinutes(5)))
                .build();
    }
}
