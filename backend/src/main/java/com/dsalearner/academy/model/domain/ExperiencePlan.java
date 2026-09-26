package com.dsalearner.academy.model.domain;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable, fully-resolved lesson experience plan.
 *
 * Produced by ExperiencePlanBuilder from a CfLessonVersion.
 * Consumed by the Academy API and rendered step-by-step in the LessonPlayer.
 *
 * The plan is deliberately language-agnostic — the builder projects raw
 * lesson content JSON into typed steps; callers need not know German vs French.
 */
public record ExperiencePlan(
        UUID lessonId,
        UUID lessonVersionId,
        int lessonVersion,
        String lessonTitle,
        String cefrLevel,
        String languageCode,
        String unitDisplayName,
        List<ExperiencePlanStep> steps
) {
    public ExperiencePlan {
        Objects.requireNonNull(lessonId, "lessonId must not be null");
        Objects.requireNonNull(lessonVersionId, "lessonVersionId must not be null");
        Objects.requireNonNull(steps, "steps must not be null");
        steps = Collections.unmodifiableList(steps);
        if (lessonVersion < 1) throw new IllegalArgumentException("lessonVersion must be >= 1");
    }

    public int totalSteps() {
        return steps.size();
    }

    public int totalExercises() {
        return (int) steps.stream().filter(ExperiencePlanStep::isExercise).count();
    }

    public ExperiencePlanStep stepAt(int index) {
        if (index < 0 || index >= steps.size()) {
            throw new IndexOutOfBoundsException(
                    "step index " + index + " out of bounds for plan of size " + steps.size());
        }
        return steps.get(index);
    }

    public boolean isEmpty() {
        return steps.isEmpty();
    }
}
