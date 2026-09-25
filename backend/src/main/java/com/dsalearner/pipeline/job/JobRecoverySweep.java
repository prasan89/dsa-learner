package com.dsalearner.pipeline.job;

import com.dsalearner.pipeline.model.entity.CfPipelineJob;
import com.dsalearner.pipeline.repository.CfPipelineJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.Duration;
import java.util.List;

/**
 * Recovers jobs that were saved to the database but never pushed to Redis.
 *
 * Root cause this addresses:
 *   DB INSERT (QUEUED) succeeds → Redis RPUSH fails → job is stuck forever
 *
 * Recovery mechanism (minimal, idempotent):
 *   Every 60 seconds, find jobs with status=QUEUED created > STALE_THRESHOLD_SECONDS ago.
 *   Push their IDs back onto Redis.
 *
 * Idempotency:
 *   Re-pushing a job ID that is already in Redis causes a duplicate delivery.
 *   Duplicate deliveries are handled safely by JobClaimService.claim(), which
 *   issues an atomic UPDATE...WHERE status IN ('QUEUED','RETRYING').
 *   The second delivery loses the race and is silently skipped.
 *
 * Safety:
 *   RUNNING jobs are NOT re-enqueued (they are already being processed).
 *   SUCCEEDED/FAILED jobs have status != QUEUED and are never returned.
 *   Only QUEUED jobs older than the threshold are touched.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JobRecoverySweep {

    /** Jobs that remain QUEUED for longer than this threshold are considered orphaned. */
    static final long STALE_THRESHOLD_SECONDS = 60L;

    private final CfPipelineJobRepository jobRepository;
    private final StringRedisTemplate redisTemplate;

    @Scheduled(fixedDelay = 60_000)
    public void recoverStaleJobs() {
        Instant threshold = Instant.now().minus(Duration.ofSeconds(STALE_THRESHOLD_SECONDS));
        List<CfPipelineJob> stale = jobRepository.findStaleQueuedJobs(threshold);

        if (stale.isEmpty()) return;

        log.info("JobRecoverySweep: re-enqueuing {} stale QUEUED jobs", stale.size());
        for (CfPipelineJob job : stale) {
            redisTemplate.opsForList().rightPush(
                    RedisContentJobQueue.QUEUE_KEY, job.getId().toString());
            log.info("JobRecoverySweep: re-enqueued jobId={} lessonId={} createdAt={}",
                    job.getId(), job.getLessonId(), job.getCreatedAt());
        }
    }
}
