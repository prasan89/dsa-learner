package com.dsalearner.pipeline.curriculum.agent;

import java.util.List;

/**
 * Output from curriculum-level QA agents.
 */
public record CurriculumQaResult(
        String overallAssessment,
        String decision,           // PASSED, FAILED, NEEDS_REVISION
        List<CurriculumQaIssue> issues,
        List<String> recommendations
) {
    public boolean passed() {
        return "PASSED".equals(decision);
    }

    public boolean hasErrors() {
        return issues != null && issues.stream().anyMatch(i -> "ERROR".equals(i.severity()));
    }
}
