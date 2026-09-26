package com.dsalearner.pipeline.curriculum.agent;

/**
 * Input for curriculum-level QA agents (both level QA and full curriculum QA).
 */
public record CurriculumQaInput(
        String languageCode,
        String languageDisplayName,
        String cefrLevel,               // null for cross-curriculum QA
        String levelSummaryJson,        // ordered lesson summaries for level QA
        String curriculumSummaryJson,   // per-level overviews for curriculum QA
        String deterministicCheckSummary
) {}
