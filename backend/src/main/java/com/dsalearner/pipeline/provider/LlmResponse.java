package com.dsalearner.pipeline.provider;

/**
 * Provider-agnostic LLM response.
 */
public record LlmResponse(
        String text,
        int inputTokens,
        int outputTokens,
        String modelId,
        String provider
) {}
