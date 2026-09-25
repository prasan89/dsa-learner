package com.dsalearner.pipeline.domain;

public enum ContentStatus {
    DRAFT,
    PLANNED,
    GENERATING,
    GENERATED,
    VALIDATION_PENDING,
    VALIDATION_FAILED,
    QA_PENDING,
    QA_FAILED,
    REVISION,
    QA_PASSED,
    HUMAN_REVIEW_REQUIRED,
    APPROVED,
    ARCHIVED
}
