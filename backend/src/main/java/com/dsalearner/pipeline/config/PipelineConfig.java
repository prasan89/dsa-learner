package com.dsalearner.pipeline.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Phase 0 async foundation.
 *
 * ## What exists now
 * - Spring @EnableAsync is active. PipelineService methods can be called asynchronously.
 * - Redis is already wired (StringRedisTemplate used in login rate limiting).
 *   The same Redis connection factory is available for queue use.
 * - No BullMQ / separate queue technology is introduced. The architecture
 *   intentionally defers queue workers to Phase 1.
 *
 * ## What Phase 1A will build on top of this
 * - A ThreadPoolTaskExecutor (configured here) for agent execution.
 * - Spring @Async on generation/QA methods in PipelineService so the HTTP
 *   request returns immediately and the pipeline runs in the background.
 * - A status-polling endpoint (/api/v1/pipeline/lessons/{id}/status) so callers
 *   can track async progress.
 * - Redis can optionally be used for job state if cross-node coordination is needed.
 *
 * ## Intentionally deferred
 * - BullMQ / dedicated job queue (deferred to Phase 2+ when batch generation is needed).
 * - Dead-letter handling, retry queues, job persistence.
 * - Worker process separation (single JVM for MVP; split when throughput demands it).
 */
@Configuration
@EnableAsync
public class PipelineConfig {
}
