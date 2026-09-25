package com.dsalearner.pipeline.agent;

/**
 * An issue reported by a QA agent or validation rule.
 *
 * evidence: verbatim lesson text that the LLM cited to support this finding.
 *           Required for ERROR severity; null/empty means unsupported claim.
 */
public record Issue(
        String code,
        Severity severity,
        String field,
        String message,
        String suggestion,
        boolean cultural,
        String evidence
) {
    public enum Severity { ERROR, WARNING, INFO }

    public static Issue error(String code, String field, String message) {
        return new Issue(code, Severity.ERROR, field, message, null, false, null);
    }

    public static Issue errorWithEvidence(String code, String field, String message, String evidence) {
        return new Issue(code, Severity.ERROR, field, message, null, false, evidence);
    }

    public static Issue warning(String code, String field, String message) {
        return new Issue(code, Severity.WARNING, field, message, null, false, null);
    }

    public static Issue info(String code, String field, String message) {
        return new Issue(code, Severity.INFO, field, message, null, false, null);
    }

    public boolean isError() { return severity == Severity.ERROR; }

    /** True when this ERROR has no supporting evidence from the lesson content. */
    public boolean isUnsupported() {
        return severity == Severity.ERROR && (evidence == null || evidence.isBlank());
    }
}
