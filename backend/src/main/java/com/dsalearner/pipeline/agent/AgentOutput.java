package com.dsalearner.pipeline.agent;

import java.util.List;
import java.util.UUID;

/**
 * Standard output envelope from every agent execution.
 */
public record AgentOutput<T>(
        UUID agentRunId,
        Status status,
        T output,
        double confidence,
        List<Issue> issues,
        List<String> recommendations,
        boolean culturalFlag,
        AgentMetadata metadata
) {
    public enum Status { SUCCEEDED, FAILED, PARTIAL }

    public record AgentMetadata(
            String modelConfigKey,
            String modelId,
            String provider,
            int inputTokens,
            int outputTokens,
            double estimatedCostUsd,
            long latencyMs,
            UUID promptId,
            int promptVersion,
            String agentVersion
    ) {}

    public boolean succeeded() { return status == Status.SUCCEEDED; }
    public boolean failed()    { return status == Status.FAILED; }

    public boolean hasErrors() {
        return issues != null && issues.stream().anyMatch(Issue::isError);
    }
}
