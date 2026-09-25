package com.dsalearner.pipeline.domain;

import java.util.Map;

/**
 * Language-specific configuration resolved from cf_language_profiles.
 * Decouples orchestrator and agent code from any particular language.
 */
public record LanguageProfile(
        String languageCode,
        String displayName,
        String domainCode,
        boolean cefrApplicable,
        boolean rtl,
        boolean active,
        String linguisticQaAgentType,
        String charValidationRegex,
        Map<String, String> promptIds,     // agentType → promptKey
        Map<String, Double> qaThresholds   // agentType → minPassScore
) {
    public String promptKey(String agentType) {
        return promptIds.getOrDefault(agentType, agentType);
    }

    public double qaThreshold(String agentType) {
        return qaThresholds.getOrDefault(agentType, 0.70);
    }
}
