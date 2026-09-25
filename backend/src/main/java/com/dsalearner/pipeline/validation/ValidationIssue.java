package com.dsalearner.pipeline.validation;

public record ValidationIssue(
        String code,
        ValidationSeverity severity,
        String field,
        String message
) {
    public boolean isError() { return severity == ValidationSeverity.ERROR; }

    public static ValidationIssue error(String code, String field, String message) {
        return new ValidationIssue(code, ValidationSeverity.ERROR, field, message);
    }

    public static ValidationIssue warning(String code, String field, String message) {
        return new ValidationIssue(code, ValidationSeverity.WARNING, field, message);
    }
}
