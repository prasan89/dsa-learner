package com.dsalearner.pipeline.language;

/**
 * Input payload for a content generation request.
 * Passed through AgentRunner to the content generation agent.
 */
public record ContentGenerationInput(
        String topic,
        String cefrLevel,
        String languageCode,
        String stableRef
) {}
