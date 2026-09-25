package com.dsalearner.pipeline.language;

import java.util.List;
import java.util.Map;

/**
 * Structured output from a content generation agent.
 * All fields map directly to CfLessonVersion JSONB columns.
 *
 * This is the contract the A1 generation prompt guarantees.
 * If the model does not return a valid structure, the agent throws.
 */
public record LessonContent(
        Map<String, Object> metadata,
        List<String>         objectives,
        Map<String, Object>  explanation,
        List<Map<String, Object>> vocabulary,
        Map<String, Object>  grammar,
        List<Map<String, Object>> examples,
        List<Map<String, Object>> exercises
) {
    /** Convert to the map fields expected by CfLessonVersion. */
    public Map<String, Object> toContentMap() {
        return Map.of(
                "objectives",  objectives,
                "explanation", explanation,
                "examples",    examples
        );
    }

    public Map<String, Object> toVocabularyMap() {
        return Map.of("items", vocabulary);
    }

    public Map<String, Object> toGrammarMap() {
        return grammar;
    }

    public Map<String, Object> toExercisesMap() {
        return Map.of("items", exercises);
    }

    public Map<String, Object> toBlueprintMap() {
        return Map.of(
                "topic",           metadata.getOrDefault("topic", ""),
                "cefrLevel",       metadata.getOrDefault("cefrLevel", "A1"),
                "language",        metadata.getOrDefault("language", "de"),
                "estimatedMinutes", metadata.getOrDefault("estimatedMinutes", 20)
        );
    }
}
