package com.dsalearner.pipeline.language.german.qa;

import com.dsalearner.pipeline.agent.Issue;

import java.util.List;

/**
 * Output returned by every QA agent after analysing a lesson.
 *
 * @param overallAssessment Short human-readable summary from the LLM.
 * @param issues            Structured issues found during evaluation.
 * @param recommendations   Free-text improvement recommendations.
 */
public record QaResult(
        String overallAssessment,
        List<Issue> issues,
        List<String> recommendations
) {
    public boolean hasErrors() {
        return issues != null && issues.stream().anyMatch(Issue::isError);
    }
}
