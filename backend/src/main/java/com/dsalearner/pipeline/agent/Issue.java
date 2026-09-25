package com.dsalearner.pipeline.agent;

/**
 * An issue reported by a QA agent or validation rule.
 */
public record Issue(
        String code,
        Severity severity,
        String field,
        String message,
        String suggestion,
        boolean cultural
) {
    public enum Severity { ERROR, WARNING, INFO }

    public static Issue error(String code, String field, String message) {
        return new Issue(code, Severity.ERROR, field, message, null, false);
    }

    public static Issue warning(String code, String field, String message) {
        return new Issue(code, Severity.WARNING, field, message, null, false);
    }

    public static Issue info(String code, String field, String message) {
        return new Issue(code, Severity.INFO, field, message, null, false);
    }

    public boolean isError() { return severity == Severity.ERROR; }
}
