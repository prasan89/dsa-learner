package com.dsalearner.pipeline.provider;

/**
 * Provider-agnostic interface for LLM text generation.
 *
 * Agents never call providers directly — they go through ModelRouter → LlmProvider.
 * This decouples agent logic from Anthropic/OpenAI SDK changes.
 */
public interface LlmProvider {

    /** Unique provider name matching ModelConfig.provider(), e.g. "anthropic", "mock". */
    String providerName();

    /**
     * Execute a generation request.
     *
     * @throws LlmProviderException on transient or permanent provider failure
     */
    LlmResponse generate(LlmRequest request);
}
