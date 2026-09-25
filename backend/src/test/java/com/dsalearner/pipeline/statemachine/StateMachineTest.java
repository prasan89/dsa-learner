package com.dsalearner.pipeline.statemachine;

import com.dsalearner.pipeline.domain.ContentStatus;
import com.dsalearner.pipeline.domain.PublicationStatus;
import com.dsalearner.pipeline.exception.InvalidTransitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class StateMachineTest {

    private final StateMachine sm = new StateMachine();

    // ─── Valid content transitions ────────────────────────────────────────

    @ParameterizedTest
    @CsvSource({
        "DRAFT,        PLANNED",
        "PLANNED,      GENERATING",
        "GENERATING,   GENERATED",
        "GENERATED,    VALIDATION_PENDING",
        "VALIDATION_PENDING, QA_PENDING",
        "VALIDATION_PENDING, VALIDATION_FAILED",
        "VALIDATION_FAILED,  REVISION",
        "QA_PENDING,   QA_PASSED",
        "QA_PENDING,   QA_FAILED",
        "QA_FAILED,    REVISION",
        "QA_FAILED,    HUMAN_REVIEW_REQUIRED",
        "REVISION,     GENERATING",
        "REVISION,     QA_PENDING",
        "REVISION,     HUMAN_REVIEW_REQUIRED",
        "QA_PASSED,    APPROVED",
        "QA_PASSED,    HUMAN_REVIEW_REQUIRED",
        "HUMAN_REVIEW_REQUIRED, APPROVED",
        "HUMAN_REVIEW_REQUIRED, REVISION",
        "APPROVED,     ARCHIVED"
    })
    void validContentTransitions(String fromStr, String toStr) {
        ContentStatus from = ContentStatus.valueOf(fromStr.trim());
        ContentStatus to   = ContentStatus.valueOf(toStr.trim());
        assertEquals(to, sm.transition(from, to, "test"));
    }

    // ─── Invalid content transitions ─────────────────────────────────────

    @ParameterizedTest
    @CsvSource({
        "DRAFT,     QA_PENDING",
        "DRAFT,     QA_PENDING",
        "DRAFT,     APPROVED",
        "DRAFT,     DRAFT",            // Blocker 4 regression: CREATE event must not go through state machine
        "DRAFT,     GENERATING",       // Blocker 3 regression: must be PLANNED before GENERATING
        "GENERATING,GENERATING",       // Blocker 3 regression: retry must not re-attempt this transition
        "PLANNED,   APPROVED",
        "PLANNED,   QA_PENDING",
        "GENERATED, QA_PENDING",      // must go through VALIDATION_PENDING first
        "APPROVED,  DRAFT",
        "ARCHIVED,  DRAFT",
        "QA_PASSED, VALIDATION_FAILED"
    })
    void invalidContentTransitions(String fromStr, String toStr) {
        ContentStatus from = ContentStatus.valueOf(fromStr.trim());
        ContentStatus to   = ContentStatus.valueOf(toStr.trim());
        assertThrows(InvalidTransitionException.class,
                () -> sm.transition(from, to, "test"),
                "Expected rejection of " + from + " → " + to);
    }

    // ─── Valid publication transitions ────────────────────────────────────

    @Test
    void unpublishedToPublished() {
        assertEquals(PublicationStatus.PUBLISHED,
                sm.transitionPublication(PublicationStatus.UNPUBLISHED, PublicationStatus.PUBLISHED, "publish"));
    }

    @Test
    void publishedToSuperseded() {
        assertEquals(PublicationStatus.SUPERSEDED,
                sm.transitionPublication(PublicationStatus.PUBLISHED, PublicationStatus.SUPERSEDED, "supersede"));
    }

    @Test
    void publishedToRolledBack() {
        assertEquals(PublicationStatus.ROLLED_BACK,
                sm.transitionPublication(PublicationStatus.PUBLISHED, PublicationStatus.ROLLED_BACK, "rollback"));
    }

    @Test
    void supersededBackToPublished() {
        assertEquals(PublicationStatus.PUBLISHED,
                sm.transitionPublication(PublicationStatus.SUPERSEDED, PublicationStatus.PUBLISHED, "rollback-reactivate"));
    }

    // ─── Invalid publication transitions ─────────────────────────────────

    @Test
    void cannotGoFromUnpublishedToSuperseded() {
        assertThrows(InvalidTransitionException.class,
                () -> sm.transitionPublication(PublicationStatus.UNPUBLISHED, PublicationStatus.SUPERSEDED, "bad"));
    }

    @Test
    void cannotGoFromArchivedToAnything() {
        assertThrows(InvalidTransitionException.class,
                () -> sm.transition(ContentStatus.ARCHIVED, ContentStatus.DRAFT, "bad"));
    }

    // ─── allowedTransitions query ─────────────────────────────────────────

    @Test
    void allowedTransitionsForDraft() {
        var allowed = sm.allowedTransitions(ContentStatus.DRAFT);
        assertTrue(allowed.contains(ContentStatus.PLANNED));
        assertFalse(allowed.contains(ContentStatus.APPROVED));
    }

    @Test
    void canTransitionReturnsFalseForInvalid() {
        assertFalse(sm.canTransition(ContentStatus.DRAFT, ContentStatus.APPROVED));
    }
}
