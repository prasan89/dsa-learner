package com.dsalearner.pipeline.agent;

import java.math.BigDecimal;

/**
 * Provider- and model-specific configuration resolved by ModelRouter.
 */
public record ModelConfig(
        String configKey,
        String provider,
        String modelId,
        double temperature,
        int maxTokens,
        int timeoutMs,
        BigDecimal costPer1kInputUsd,
        BigDecimal costPer1kOutputUsd
) {
    public double estimateCost(int inputTokens, int outputTokens) {
        double inputCost  = costPer1kInputUsd.doubleValue()  * inputTokens  / 1000.0;
        double outputCost = costPer1kOutputUsd.doubleValue() * outputTokens / 1000.0;
        return inputCost + outputCost;
    }

    public boolean isMock() { return "mock".equals(provider); }
}
