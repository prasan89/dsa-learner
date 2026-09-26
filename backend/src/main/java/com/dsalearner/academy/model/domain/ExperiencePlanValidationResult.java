package com.dsalearner.academy.model.domain;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Result of ExperiencePlanValidator.validate().
 * Immutable. Carries all violations found; valid() is true only when violations is empty.
 */
public record ExperiencePlanValidationResult(
        boolean valid,
        List<String> violations
) {
    public ExperiencePlanValidationResult {
        Objects.requireNonNull(violations, "violations must not be null");
        violations = Collections.unmodifiableList(violations);
    }

    public static ExperiencePlanValidationResult ok() {
        return new ExperiencePlanValidationResult(true, List.of());
    }

    public static ExperiencePlanValidationResult failed(List<String> violations) {
        if (violations.isEmpty()) throw new IllegalArgumentException("violations must not be empty for a failed result");
        return new ExperiencePlanValidationResult(false, violations);
    }
}
