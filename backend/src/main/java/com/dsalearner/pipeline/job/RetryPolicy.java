package com.dsalearner.pipeline.job;

import com.dsalearner.pipeline.provider.LlmProviderException;

/**
 * Classifies exceptions as retryable (transient) or non-retryable (deterministic).
 *
 * Retryable: temporary provider failure, timeout, network error.
 * Non-retryable: bad input, invalid state, validation failure, missing config.
 */
public final class RetryPolicy {

    private RetryPolicy() {}

    public static boolean isRetryable(Throwable e) {
        if (e instanceof LlmProviderException lpe) {
            return lpe.isRetryable();
        }
        // Non-retryable deterministic failures
        if (e instanceof IllegalStateException) return false;
        if (e instanceof IllegalArgumentException) return false;
        if (e instanceof com.dsalearner.exception.NotFoundException) return false;
        if (e instanceof com.dsalearner.pipeline.exception.InvalidTransitionException) return false;
        if (e instanceof com.dsalearner.pipeline.exception.PromptNotFoundException) return false;
        if (e instanceof com.dsalearner.pipeline.exception.ModelConfigNotFoundException) return false;
        if (e instanceof com.dsalearner.pipeline.exception.DomainNotFoundException) return false;
        // Default: retryable (unknown transient errors)
        return true;
    }
}
