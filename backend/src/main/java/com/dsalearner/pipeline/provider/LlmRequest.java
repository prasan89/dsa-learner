package com.dsalearner.pipeline.provider;

/**
 * Provider-agnostic LLM request.
 * Agents construct this; the provider translates it to the wire format.
 */
public record LlmRequest(
        String modelId,
        String systemPrompt,
        String userPrompt,
        int maxTokens,
        double temperature,
        int timeoutMs
) {}
