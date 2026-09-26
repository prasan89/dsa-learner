package com.dsalearner.pipeline.curriculum.domain;

/**
 * Status values for cf_curriculum_levels.level_status.
 */
public enum LevelStatus {
    PLANNED,
    GENERATION_IN_PROGRESS,
    LEVEL_QA_PENDING,
    LEVEL_QA_FAILED,
    LEVEL_QA_PASSED,
    APPROVED,
    ARCHIVED;

    public boolean isTerminal() {
        return this == APPROVED || this == ARCHIVED;
    }

    public boolean isQaPassed() {
        return this == LEVEL_QA_PASSED || this == APPROVED;
    }
}
