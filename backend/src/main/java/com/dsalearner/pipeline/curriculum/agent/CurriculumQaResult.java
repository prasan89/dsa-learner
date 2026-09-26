package com.dsalearner.pipeline.curriculum.agent;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Output from curriculum-level QA agents.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CurriculumQaResult(
        String overallAssessment,
        @JsonAlias({"levelDecision", "qaDecision"})
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
