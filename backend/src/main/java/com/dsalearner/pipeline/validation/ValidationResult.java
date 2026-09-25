package com.dsalearner.pipeline.validation;

import java.util.List;

public record ValidationResult(
        boolean passed,
        List<ValidationIssue> issues
) {
    public static ValidationResult pass() {
        return new ValidationResult(true, List.of());
    }

    public static ValidationResult fail(List<ValidationIssue> issues) {
        return new ValidationResult(false, issues);
    }

    public boolean hasErrors() {
        return issues.stream().anyMatch(ValidationIssue::isError);
    }

    public List<ValidationIssue> errors() {
        return issues.stream().filter(ValidationIssue::isError).toList();
    }

    public List<ValidationIssue> warnings() {
        return issues.stream()
                .filter(i -> i.severity() == ValidationSeverity.WARNING).toList();
    }
}
