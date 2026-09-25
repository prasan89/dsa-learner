package com.dsalearner.pipeline.statemachine;

import com.dsalearner.pipeline.domain.ContentStatus;
import com.dsalearner.pipeline.domain.PublicationStatus;

/**
 * Named triggers for state transitions. Used in audit log and transition validation.
 */
public enum ContentTransition {

    // Content transitions
    PLAN,
    START_GENERATION,
    GENERATION_COMPLETE,
    SUBMIT_FOR_VALIDATION,
    VALIDATION_PASSED,
    VALIDATION_FAILED,
    SUBMIT_FOR_QA,
    QA_PASSED,
    QA_FAILED,
    ROUTE_TO_REVISION,
    REVISION_COMPLETE,
    ESCALATE_TO_HUMAN,
    APPROVE,
    REJECT,
    ARCHIVE,

    // Publication transitions
    PUBLISH,
    SCHEDULE,
    UNPUBLISH,
    SUPERSEDE,
    ROLLBACK;

    public boolean isContentTransition() {
        return this != PUBLISH && this != SCHEDULE && this != UNPUBLISH
                && this != SUPERSEDE && this != ROLLBACK;
    }

    public boolean isPublicationTransition() {
        return !isContentTransition();
    }
}
