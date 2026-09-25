package com.dsalearner.pipeline.language;

/**
 * Input for the revision generation agent.
 *
 * @param lessonJson       The original lesson JSON (full content to be revised)
 * @param revisionFeedback Structured QA feedback as JSON string: {"issues": [...]}
 * @param sourceVersion    The lesson version that failed QA (for idempotency key)
 */
public record RevisionGenerationInput(
        String lessonJson,
        String revisionFeedback,
        int    sourceVersion
) {}
