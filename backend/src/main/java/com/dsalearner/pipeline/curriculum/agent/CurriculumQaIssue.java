package com.dsalearner.pipeline.curriculum.agent;

import java.util.List;

/**
 * Issue from curriculum QA (level or cross-curriculum).
 */
public record CurriculumQaIssue(
        String severity,           // ERROR, WARNING, INFO
        String fromLevel,          // for cross-level issues
        String toLevel,
        String cefrLevel,          // for single-level issues
        Integer lessonPosition,
        String stableRef,
        String field,
        String message,
        String suggestion
) {}
