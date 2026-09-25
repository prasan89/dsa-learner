package com.dsalearner.pipeline.domain;

import java.util.List;
import java.util.Map;

/**
 * Immutable descriptor for one content domain (language / dsa / ai_skills).
 * Loaded from the cf_domains table at startup via DomainRegistry.
 */
public record DomainPlugin(
        String domainCode,
        String displayName,
        boolean active,
        List<String> activeAgentTypes,
        Map<String, Object> pluginConfig
) {
    public boolean supports(String agentType) {
        return activeAgentTypes.contains(agentType);
    }
}
