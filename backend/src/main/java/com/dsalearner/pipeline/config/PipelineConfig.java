package com.dsalearner.pipeline.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class PipelineConfig {
    // Phase 0: async execution is configured but agents run synchronously in tests.
    // Phase 1: will add ThreadPoolTaskExecutor with configurable pool sizes.
}
