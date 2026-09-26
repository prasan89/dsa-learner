package com.dsalearner.pipeline.curriculum.domain;

/**
 * Status values for cf_curriculum_lesson_plans.plan_status.
 */
public enum PlanStatus {
    PLANNED,
    BLOCKED,
    QUEUED,
    GENERATING,
    GENERATED,
    QA_PENDING,
    QA_FAILED,
    REVISION,
    QA_PASSED,
    APPROVED,
    PUBLISHED,
    SKIPPED,
    FAILED,
    HUMAN_REVIEW;

    public boolean isTerminal() {
        return this == QA_PASSED || this == APPROVED || this == PUBLISHED
                || this == SKIPPED || this == FAILED || this == HUMAN_REVIEW;
    }

    public boolean isSuccess() {
        return this == QA_PASSED || this == APPROVED || this == PUBLISHED;
    }
}
