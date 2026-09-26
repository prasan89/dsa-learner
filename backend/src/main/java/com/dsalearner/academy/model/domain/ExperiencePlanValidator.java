package com.dsalearner.academy.model.domain;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates an ExperiencePlan for structural integrity before serving it to learners.
 *
 * Rules enforced:
 *  1.  Plan must not be null.
 *  2.  Plan must have at least one step.
 *  3.  Last step must be LESSON_REVIEW.
 *  4.  Step indices must be contiguous, starting at 0.
 *  5.  No step may have a null type.
 *  6.  No step may have a null or empty payload.
 *  7.  Each exercise step must have a non-blank "question" key in its payload.
 *  8.  Each MULTIPLE_CHOICE step must have a non-empty "options" list in its payload.
 *  9.  Each MULTIPLE_CHOICE step must have a "correctIndex" key in its payload.
 * 10.  Each FILL_IN_BLANK step must have a non-blank "answer" key in its payload.
 * 11.  Each TRANSLATION step must have non-blank "sourceText" and "targetText" keys.
 * 12.  Each VOCABULARY_CARD step must have "word" and "translation" keys.
 * 13.  lessonId must not be null.
 * 14.  lessonVersionId must not be null.
 * 15.  lessonVersion must be >= 1.
 * 16.  There must be at least one NARRATIVE step (lesson has no readable content otherwise).
 * 17.  There must be at least one exercise step (MULTIPLE_CHOICE / FILL_IN_BLANK / TRANSLATION).
 * 18.  VOCABULARY_SUMMARY step, if present, must have a non-empty "items" list.
 * 19.  NARRATIVE step must have a non-blank "text" or "content" key.
 * 20.  There must be at most one LESSON_REVIEW step (duplicate review cards are a builder bug).
 */
@Component
public class ExperiencePlanValidator {

    public ExperiencePlanValidationResult validate(ExperiencePlan plan) {
        List<String> v = new ArrayList<>();

        // Rule 1
        if (plan == null) {
            return ExperiencePlanValidationResult.failed(List.of("plan must not be null"));
        }

        // Rule 13
        if (plan.lessonId() == null) v.add("lessonId must not be null");
        // Rule 14
        if (plan.lessonVersionId() == null) v.add("lessonVersionId must not be null");
        // Rule 15
        if (plan.lessonVersion() < 1) v.add("lessonVersion must be >= 1, got " + plan.lessonVersion());

        // Rule 2
        if (plan.steps() == null || plan.steps().isEmpty()) {
            v.add("plan must have at least one step");
            return ExperiencePlanValidationResult.failed(v);
        }

        List<ExperiencePlanStep> steps = plan.steps();

        // Rule 3
        ExperiencePlanStep last = steps.get(steps.size() - 1);
        if (last.type() != StepType.LESSON_REVIEW) {
            v.add("last step must be LESSON_REVIEW, found " + last.type());
        }

        // Rule 4
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).index() != i) {
                v.add("step at position " + i + " has index=" + steps.get(i).index() + " (must be " + i + ")");
            }
        }

        int narrativeCount = 0;
        int exerciseCount = 0;
        int reviewCount = 0;

        for (ExperiencePlanStep step : steps) {
            // Rule 5
            if (step.type() == null) {
                v.add("step at index " + step.index() + " has null type");
                continue;
            }
            // Rule 6
            if (step.payload() == null || step.payload().isEmpty()) {
                v.add("step at index " + step.index() + " (" + step.type() + ") has empty payload");
            }

            switch (step.type()) {
                case NARRATIVE -> {
                    narrativeCount++;
                    // Rule 19
                    if (!hasNonBlankKey(step, "text") && !hasNonBlankKey(step, "content")) {
                        v.add("NARRATIVE step at index " + step.index() + " must have non-blank 'text' or 'content'");
                    }
                }
                case VOCABULARY_CARD -> {
                    // Rule 12
                    if (!hasKey(step, "word") || !hasKey(step, "translation")) {
                        v.add("VOCABULARY_CARD step at index " + step.index() + " must have 'word' and 'translation'");
                    }
                }
                case GRAMMAR_EXPLANATION -> {
                    // no additional payload rules beyond non-empty
                }
                case VOCABULARY_SUMMARY -> {
                    // Rule 18
                    Object items = step.payload().get("items");
                    if (!(items instanceof List<?> list) || list.isEmpty()) {
                        v.add("VOCABULARY_SUMMARY step at index " + step.index() + " must have non-empty 'items'");
                    }
                }
                case MULTIPLE_CHOICE -> {
                    exerciseCount++;
                    // Rule 7
                    if (!hasNonBlankKey(step, "question")) {
                        v.add("MULTIPLE_CHOICE step at index " + step.index() + " must have non-blank 'question'");
                    }
                    // Rule 8
                    Object opts = step.payload().get("options");
                    if (!(opts instanceof List<?> list) || list.isEmpty()) {
                        v.add("MULTIPLE_CHOICE step at index " + step.index() + " must have non-empty 'options'");
                    }
                    // Rule 9
                    if (!hasKey(step, "correctIndex")) {
                        v.add("MULTIPLE_CHOICE step at index " + step.index() + " must have 'correctIndex'");
                    }
                }
                case FILL_IN_BLANK -> {
                    exerciseCount++;
                    // Rule 7
                    if (!hasNonBlankKey(step, "question")) {
                        v.add("FILL_IN_BLANK step at index " + step.index() + " must have non-blank 'question'");
                    }
                    // Rule 10
                    if (!hasNonBlankKey(step, "answer")) {
                        v.add("FILL_IN_BLANK step at index " + step.index() + " must have non-blank 'answer'");
                    }
                }
                case TRANSLATION -> {
                    exerciseCount++;
                    // Rule 7
                    if (!hasNonBlankKey(step, "question")) {
                        v.add("TRANSLATION step at index " + step.index() + " must have non-blank 'question'");
                    }
                    // Rule 11
                    if (!hasNonBlankKey(step, "sourceText") || !hasNonBlankKey(step, "targetText")) {
                        v.add("TRANSLATION step at index " + step.index() + " must have 'sourceText' and 'targetText'");
                    }
                }
                case LESSON_REVIEW -> {
                    reviewCount++;
                }
            }
        }

        // Rule 16
        if (narrativeCount == 0) v.add("plan must have at least one NARRATIVE step");
        // Rule 17
        if (exerciseCount == 0) v.add("plan must have at least one exercise step");
        // Rule 20
        if (reviewCount > 1) v.add("plan must have at most one LESSON_REVIEW step, found " + reviewCount);

        return v.isEmpty()
                ? ExperiencePlanValidationResult.ok()
                : ExperiencePlanValidationResult.failed(v);
    }

    private boolean hasKey(ExperiencePlanStep step, String key) {
        return step.payload() != null && step.payload().containsKey(key) && step.payload().get(key) != null;
    }

    private boolean hasNonBlankKey(ExperiencePlanStep step, String key) {
        Object val = step.payload() == null ? null : step.payload().get(key);
        return val instanceof String s && !s.isBlank();
    }
}
