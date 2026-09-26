package com.dsalearner.academy.model.domain;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A single immutable step within an ExperiencePlan.
 *
 * @param index    0-based position within the plan (stable, used for progress tracking)
 * @param type     the step type that determines which frontend component renders it
 * @param payload  content-specific data (vocabulary item, exercise, narrative text, etc.)
 * @param audioKey optional audio manifest key for NARRATIVE and VOCABULARY_CARD steps
 */
public record ExperiencePlanStep(
        int index,
        StepType type,
        Map<String, Object> payload,
        String audioKey
) {
    public ExperiencePlanStep {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(payload, "payload must not be null");
        payload = Collections.unmodifiableMap(payload);
        if (index < 0) throw new IllegalArgumentException("step index must be >= 0, got " + index);
    }

    public static ExperiencePlanStep of(int index, StepType type, Map<String, Object> payload) {
        return new ExperiencePlanStep(index, type, payload, null);
    }

    public static ExperiencePlanStep withAudio(int index, StepType type, Map<String, Object> payload, String audioKey) {
        return new ExperiencePlanStep(index, type, payload, audioKey);
    }

    /** Convenience: true for any exercise step that the user must answer. */
    public boolean isExercise() {
        return type == StepType.MULTIPLE_CHOICE
                || type == StepType.FILL_IN_BLANK
                || type == StepType.TRANSLATION;
    }
}
