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
import java.time.Duration;
import java.util.List;

/**
 * Recovers orphaned pipeline jobs every 60 seconds.
 *
 * Two recovery paths:
 *   1. QUEUED jobs never pushed to Redis (DB INSERT succeeded, RPUSH failed).
 *      Re-pushed to Redis; duplicate delivery safely handled by atomic claim.
 *   2. RUNNING jobs whose worker thread died (LLM timeout, OOM, crash) without
 *      marking the job terminal. Reset to QUEUED after RUNNING_TIMEOUT_MINUTES
 *      so the next attempt picks them up. Attempt counter is decremented to
 *      preserve retry budget.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JobRecoverySweep {

    static final long STALE_THRESHOLD_SECONDS = 120L;
    static final long RUNNING_TIMEOUT_MINUTES = 15L;

    private final CfPipelineJobRepository jobRepository;
    private final StringRedisTemplate redisTemplate;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void recoverStaleJobs() {
        recoverStaleQueued();
        recoverStuckRunning();
    }

    private void recoverStaleQueued() {
        Instant threshold = Instant.now().minus(Duration.ofSeconds(STALE_THRESHOLD_SECONDS));
        List<CfPipelineJob> stale = jobRepository.findStaleQueuedJobs(threshold);
        if (stale.isEmpty()) return;

        log.info("JobRecoverySweep: re-enqueuing {} stale QUEUED jobs", stale.size());
        for (CfPipelineJob job : stale) {
            redisTemplate.opsForList().rightPush(RedisContentJobQueue.QUEUE_KEY, job.getId().toString());
            log.info("JobRecoverySweep: re-enqueued jobId={} lessonId={} createdAt={}",
                    job.getId(), job.getLessonId(), job.getCreatedAt());
        }
    }

    private void recoverStuckRunning() {
        Instant threshold = Instant.now().minus(Duration.ofMinutes(RUNNING_TIMEOUT_MINUTES));
        int reset = jobRepository.resetStuckRunningJobs(threshold);
        if (reset == 0) return;

        log.warn("JobRecoverySweep: reset {} stuck RUNNING jobs to QUEUED", reset);
        // Re-push reset jobs to Redis so they are picked up immediately
        List<CfPipelineJob> resetJobs = jobRepository.findStaleQueuedJobs(
                Instant.now().minus(Duration.ofSeconds(1)));
        for (CfPipelineJob job : resetJobs) {
            redisTemplate.opsForList().rightPush(RedisContentJobQueue.QUEUE_KEY, job.getId().toString());
            log.warn("JobRecoverySweep: re-queued recovered jobId={} lessonId={}",
                    job.getId(), job.getLessonId());
        }
    }
}
