package com.dsalearner.pipeline.job;

import com.dsalearner.pipeline.model.entity.CfPipelineJob;
import com.dsalearner.pipeline.repository.CfPipelineJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Redis-backed job queue.
 * Jobs are persisted to PostgreSQL first (durable), then the job ID is pushed
 * to a Redis list. The worker pops IDs from Redis and loads the job from DB.
 *
 * If Redis is unavailable the job record still exists in PostgreSQL and can be
 * recovered via a future sweep (Phase 1B+). For Phase 1A the Redis-based path
 * is the primary execution path.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisContentJobQueue implements ContentJobQueue {

    static final String QUEUE_KEY = "cf:pipeline:jobs";

    private final CfPipelineJobRepository jobRepository;
    private final StringRedisTemplate redisTemplate;

    @Override
    public UUID enqueue(CfPipelineJob job) {
        CfPipelineJob saved = jobRepository.save(job);
        redisTemplate.opsForList().rightPush(QUEUE_KEY, saved.getId().toString());
        log.info("ContentJobQueue: enqueued jobId={} lessonId={} type={}",
                saved.getId(), saved.getLessonId(), saved.getJobType());
        return saved.getId();
    }
}
