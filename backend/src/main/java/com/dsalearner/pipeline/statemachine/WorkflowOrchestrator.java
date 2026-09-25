package com.dsalearner.pipeline.statemachine;

import com.dsalearner.pipeline.domain.ContentStatus;
import com.dsalearner.pipeline.domain.PublicationStatus;
import com.dsalearner.pipeline.model.entity.CfLesson;
import com.dsalearner.pipeline.model.entity.CfWorkflowEvent;
import com.dsalearner.pipeline.repository.CfLessonRepository;
import com.dsalearner.pipeline.repository.CfWorkflowEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Orchestrates lesson lifecycle: validates transitions, persists new status,
 * and records an immutable audit event for every transition.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowOrchestrator {

    private final StateMachine stateMachine;
    private final CfLessonRepository lessonRepository;
    private final CfWorkflowEventRepository eventRepository;

    /**
     * Applies a content status transition and records the audit event.
     */
    @Transactional
    public CfLesson applyContentTransition(UUID lessonId,
                                           ContentStatus to,
                                           String trigger,
                                           String actor,
                                           UUID agentRunId,
                                           Map<String, Object> metadata) {

        CfLesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new com.dsalearner.exception.NotFoundException("Lesson not found: " + lessonId));

        ContentStatus from = lesson.getContentStatus();
        ContentStatus validated = stateMachine.transition(from, to, trigger);

        lesson.setContentStatus(validated);
        lesson.setUpdatedAt(Instant.now());

        if (validated == ContentStatus.HUMAN_REVIEW_REQUIRED) {
            lesson.setHumanReviewFlag(true);
        }

        CfLesson saved = lessonRepository.save(lesson);

        recordEvent(lessonId, lesson.getCurrentVersion(), from.name(), to.name(),
                "content", trigger, actor, agentRunId, metadata);

        log.info("Lesson {} content: {} → {} [trigger={}]", lessonId, from, to, trigger);
        return saved;
    }

    /**
     * Applies a publication status transition and records the audit event.
     */
    @Transactional
    public CfLesson applyPublicationTransition(UUID lessonId,
                                               PublicationStatus to,
                                               String trigger,
                                               String actor,
                                               Map<String, Object> metadata) {

        CfLesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new com.dsalearner.exception.NotFoundException("Lesson not found: " + lessonId));

        PublicationStatus from = lesson.getPublicationStatus();
        PublicationStatus validated = stateMachine.transitionPublication(from, to, trigger);

        lesson.setPublicationStatus(validated);
        lesson.setUpdatedAt(Instant.now());

        CfLesson saved = lessonRepository.save(lesson);

        recordEvent(lessonId, lesson.getCurrentVersion(), from.name(), to.name(),
                "publication", trigger, actor, null, metadata);

        log.info("Lesson {} publication: {} → {} [trigger={}]", lessonId, from, to, trigger);
        return saved;
    }

    /**
     * Increments revision count. If limit is reached, automatically escalates to HUMAN_REVIEW_REQUIRED.
     * Returns whether escalation occurred.
     */
    @Transactional
    public boolean incrementRevisionCount(UUID lessonId, String actor) {
        CfLesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new com.dsalearner.exception.NotFoundException("Lesson not found: " + lessonId));

        int newCount = lesson.getRevisionCount() + 1;
        lesson.setRevisionCount(newCount);

        boolean escalated = newCount >= lesson.getMaxRevisionAttempts();
        if (escalated) {
            lesson.setHumanReviewFlag(true);
            lesson.setHumanReviewReason("REVISION_LIMIT_EXCEEDED (count=%d, max=%d)".formatted(newCount, lesson.getMaxRevisionAttempts()));
        }

        lessonRepository.save(lesson);
        return escalated;
    }

    private void recordEvent(UUID lessonId, int lessonVersion,
                             String fromStatus, String toStatus, String statusType,
                             String trigger, String actor, UUID agentRunId,
                             Map<String, Object> metadata) {
        CfWorkflowEvent event = CfWorkflowEvent.builder()
                .lessonId(lessonId)
                .lessonVersion(lessonVersion)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .statusType(statusType)
                .trigger(trigger)
                .actor(actor != null ? actor : "system")
                .agentRunId(agentRunId)
                .metadata(metadata)
                .occurredAt(Instant.now())
                .build();
        eventRepository.save(event);
    }
}
