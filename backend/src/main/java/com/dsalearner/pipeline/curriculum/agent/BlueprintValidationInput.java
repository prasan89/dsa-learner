package com.dsalearner.pipeline.curriculum.agent;

/**
 * Input for the blueprint validator agent.
 */
public record BlueprintValidationInput(
        String blueprintJson,
        String languageCode,
        String languageDisplayName,
        String cefrLevels,
        String deterministicSummary
) {}
