package com.dsalearner.pipeline.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Phase 1A async foundation.
 *
 * @EnableAsync  — allows @Async on service methods.
 * @EnableScheduling — enables @Scheduled on ContentJobWorker.poll().
 *
 * The pipelineExecutor thread pool isolates Content Factory work from the
 * HTTP thread pool and provides clean shutdown on application stop.
 *
 * Queue architecture:
 *   HTTP request → CF job saved to PostgreSQL → job ID pushed to Redis list
 *   ContentJobWorker (scheduled every 500ms) → pops from Redis → executes pipeline
 *
 * Single-JVM for Phase 1A. Horizontal scaling (multiple worker nodes sharing the
 * Redis queue) is safe because the worker atomically claims each job.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class PipelineConfig {

    @Bean("pipelineExecutor")
    public Executor pipelineExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("cf-pipeline-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
