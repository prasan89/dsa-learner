package com.dsalearner.pipeline.provider;

/**
 * Thrown when an LLM provider call fails.
 * Callers inspect {@link #isRetryable()} to decide retry vs. abort.
 */
public class LlmProviderException extends RuntimeException {

    private final boolean retryable;

    public LlmProviderException(String message, boolean retryable) {
        super(message);
        this.retryable = retryable;
    }

    public LlmProviderException(String message, boolean retryable, Throwable cause) {
        super(message, cause);
        this.retryable = retryable;
    }

    public boolean isRetryable() { return retryable; }
}
