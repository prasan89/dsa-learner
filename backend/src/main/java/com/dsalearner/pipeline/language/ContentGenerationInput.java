package com.dsalearner.pipeline.language;

/**
 * Input payload for a content generation request.
 * curriculumContext is optional — null for standalone lessons.
 * When present, it carries compact context from the curriculum:
 *   unit label, position, prior lesson topics, grammar/vocab taught so far.
 */
public record ContentGenerationInput(
        String topic,
        String cefrLevel,
        String languageCode,
        String stableRef,
        String curriculumContext   // optional; null for standalone lessons
) {
    public ContentGenerationInput(String topic, String cefrLevel, String languageCode, String stableRef) {
        this(topic, cefrLevel, languageCode, stableRef, null);
    }
}
