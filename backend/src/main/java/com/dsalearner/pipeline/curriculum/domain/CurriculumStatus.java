package com.dsalearner.pipeline.curriculum.domain;

/**
 * Status values for cf_curricula.curriculum_status.
 */
public enum CurriculumStatus {
    DRAFT,
    BLUEPRINT_PENDING,
    BLUEPRINT_GENERATED,
    BLUEPRINT_VALIDATED,
    GENERATION_IN_PROGRESS,
    CURRICULUM_QA_PENDING,
    CURRICULUM_QA_FAILED,
    CURRICULUM_QA_PASSED,
    APPROVED,
    ARCHIVED;

    public boolean isTerminal() {
        return this == APPROVED || this == ARCHIVED;
    }

    public boolean isGenerating() {
        return this == GENERATION_IN_PROGRESS;
    }
}
