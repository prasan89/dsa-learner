package com.dsalearner.pipeline.agent;

import java.util.UUID;

/**
 * Standard input envelope passed to every agent execution.
 */
public record AgentInput<T>(
        UUID agentRunId,
        UUID lessonId,
        int lessonVersion,
        String inputHash,      // SHA-256 — idempotency key
        T payload,
        AgentContext context
) {
    public record AgentContext(
            String domainCode,
            String languageCode,     // null for DSA
            String cefrLevel,        // null for non-CEFR domains
            String stableRef,        // e.g. 'de-a1-u01-l01'
            int retryCount,
            int maxRetries,
            UUID promptId,
            int promptVersion,
            String modelConfigKey
    ) {}
}
