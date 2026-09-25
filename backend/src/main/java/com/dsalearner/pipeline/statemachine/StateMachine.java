package com.dsalearner.pipeline.statemachine;

import com.dsalearner.pipeline.domain.ContentStatus;
import com.dsalearner.pipeline.domain.PublicationStatus;
import com.dsalearner.pipeline.exception.InvalidTransitionException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Validates and executes state transitions.
 * All valid transitions are explicitly enumerated — anything not listed is rejected.
 */
@Component
public class StateMachine {

    // from → valid set of (to)
    private static final Map<ContentStatus, Set<ContentStatus>> CONTENT_TRANSITIONS = Map.ofEntries(
            Map.entry(ContentStatus.DRAFT,                  Set.of(ContentStatus.PLANNED, ContentStatus.ARCHIVED)),
            Map.entry(ContentStatus.PLANNED,                Set.of(ContentStatus.GENERATING, ContentStatus.DRAFT)),
            Map.entry(ContentStatus.GENERATING,             Set.of(ContentStatus.GENERATED, ContentStatus.DRAFT)),
            Map.entry(ContentStatus.GENERATED,              Set.of(ContentStatus.VALIDATION_PENDING)),
            Map.entry(ContentStatus.VALIDATION_PENDING,     Set.of(ContentStatus.QA_PENDING, ContentStatus.VALIDATION_FAILED)),
            Map.entry(ContentStatus.VALIDATION_FAILED,      Set.of(ContentStatus.REVISION)),
            Map.entry(ContentStatus.QA_PENDING,             Set.of(ContentStatus.QA_PASSED, ContentStatus.QA_FAILED)),
            Map.entry(ContentStatus.QA_FAILED,              Set.of(ContentStatus.REVISION, ContentStatus.HUMAN_REVIEW_REQUIRED)),
            Map.entry(ContentStatus.REVISION,               Set.of(ContentStatus.QA_PENDING, ContentStatus.HUMAN_REVIEW_REQUIRED)),
            Map.entry(ContentStatus.QA_PASSED,              Set.of(ContentStatus.HUMAN_REVIEW_REQUIRED, ContentStatus.APPROVED)),
            Map.entry(ContentStatus.HUMAN_REVIEW_REQUIRED,  Set.of(ContentStatus.APPROVED, ContentStatus.REVISION, ContentStatus.ARCHIVED)),
            Map.entry(ContentStatus.APPROVED,               Set.of(ContentStatus.ARCHIVED)),
            Map.entry(ContentStatus.ARCHIVED,               Set.of())
    );

    private static final Map<PublicationStatus, Set<PublicationStatus>> PUBLICATION_TRANSITIONS = Map.ofEntries(
            Map.entry(PublicationStatus.UNPUBLISHED, Set.of(PublicationStatus.SCHEDULED, PublicationStatus.PUBLISHED)),
            Map.entry(PublicationStatus.SCHEDULED,   Set.of(PublicationStatus.PUBLISHED, PublicationStatus.UNPUBLISHED)),
            Map.entry(PublicationStatus.PUBLISHED,   Set.of(PublicationStatus.SUPERSEDED, PublicationStatus.ROLLED_BACK, PublicationStatus.UNPUBLISHED)),
            Map.entry(PublicationStatus.SUPERSEDED,  Set.of(PublicationStatus.PUBLISHED)),  // rollback re-activates
            Map.entry(PublicationStatus.ROLLED_BACK, Set.of())
    );

    /**
     * Validates and returns the new status. Throws on invalid transition.
     */
    public ContentStatus transition(ContentStatus from, ContentStatus to, String trigger) {
        Set<ContentStatus> allowed = CONTENT_TRANSITIONS.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new InvalidTransitionException(
                    "Invalid content transition: %s → %s (trigger=%s)".formatted(from, to, trigger));
        }
        return to;
    }

    /**
     * Validates and returns the new publication status. Throws on invalid transition.
     */
    public PublicationStatus transitionPublication(PublicationStatus from, PublicationStatus to, String trigger) {
        Set<PublicationStatus> allowed = PUBLICATION_TRANSITIONS.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new InvalidTransitionException(
                    "Invalid publication transition: %s → %s (trigger=%s)".formatted(from, to, trigger));
        }
        return to;
    }

    public boolean canTransition(ContentStatus from, ContentStatus to) {
        return CONTENT_TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    public boolean canTransitionPublication(PublicationStatus from, PublicationStatus to) {
        return PUBLICATION_TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    public Set<ContentStatus> allowedTransitions(ContentStatus from) {
        return CONTENT_TRANSITIONS.getOrDefault(from, Set.of());
    }
}
