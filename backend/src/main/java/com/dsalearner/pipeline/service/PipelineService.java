package com.dsalearner.pipeline.service;

import com.dsalearner.exception.NotFoundException;
import com.dsalearner.pipeline.domain.ContentStatus;
import com.dsalearner.pipeline.domain.DomainRegistry;
import com.dsalearner.pipeline.domain.LanguageProfile;
import com.dsalearner.pipeline.model.entity.CfLesson;
import com.dsalearner.pipeline.model.entity.CfLessonVersion;
import com.dsalearner.pipeline.repository.CfLessonRepository;
import com.dsalearner.pipeline.repository.CfLessonVersionRepository;
import com.dsalearner.pipeline.repository.CfWorkflowEventRepository;
import com.dsalearner.pipeline.statemachine.WorkflowOrchestrator;
import com.dsalearner.pipeline.validation.DeterministicValidator;
import com.dsalearner.pipeline.validation.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Core pipeline service. Coordinates lesson lifecycle operations.
 * Does NOT contain business logic — delegates to StateMachine, Validator, Orchestrator.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PipelineService {

    private final CfLessonRepository lessonRepository;
    private final CfLessonVersionRepository versionRepository;
    private final CfWorkflowEventRepository eventRepository;
    private final WorkflowOrchestrator orchestrator;
    private final DeterministicValidator validator;
    private final DomainRegistry domainRegistry;

    @Transactional
    public CfLesson createLesson(String stableRef, String domainCode, String languageCode,
                                  String cefrLevel, String title, String actor) {
        if (lessonRepository.findByStableRef(stableRef).isPresent()) {
            throw new com.dsalearner.exception.ConflictException("Lesson already exists: " + stableRef);
        }

        CfLesson lesson = CfLesson.builder()
                .stableRef(stableRef)
                .domainCode(domainCode)
                .languageCode(languageCode)
                .cefrLevel(cefrLevel)
                .title(title)
                .contentStatus(ContentStatus.DRAFT)
                .build();
        lesson = lessonRepository.save(lesson);

        // Create initial version snapshot
        CfLessonVersion v1 = CfLessonVersion.builder()
                .lessonId(lesson.getId())
                .version(1)
                .contentStatus(ContentStatus.DRAFT.name())
                .build();
        versionRepository.save(v1);

        // Audit event
        orchestrator.applyContentTransition(lesson.getId(), ContentStatus.DRAFT,
                "CREATE", actor, null, Map.of("stableRef", stableRef));

        log.info("Created lesson stableRef={} domainCode={} by={}", stableRef, domainCode, actor);
        return lesson;
    }

    @Transactional
    public CfLesson plan(UUID lessonId, String actor) {
        return orchestrator.applyContentTransition(lessonId, ContentStatus.PLANNED,
                "PLAN", actor, null, null);
    }

    @Transactional
    public CfLesson startGeneration(UUID lessonId, String actor) {
        return orchestrator.applyContentTransition(lessonId, ContentStatus.GENERATING,
                "START_GENERATION", actor, null, null);
    }

    @Transactional
    public CfLesson markGenerated(UUID lessonId, String actor) {
        return orchestrator.applyContentTransition(lessonId, ContentStatus.GENERATED,
                "GENERATION_COMPLETE", actor, null, null);
    }

    /**
     * Runs deterministic validation. On pass → QA_PENDING. On fail → VALIDATION_FAILED.
     */
    @Transactional
    public ValidationResult submitForValidation(UUID lessonId, Map<String, Object> content, String actor) {
        CfLesson lesson = getLesson(lessonId);

        orchestrator.applyContentTransition(lessonId, ContentStatus.VALIDATION_PENDING,
                "SUBMIT_FOR_VALIDATION", actor, null, null);

        ValidationResult result = validator.validate(content, lesson.getDomainCode(), lesson.getLanguageCode());

        if (result.passed()) {
            orchestrator.applyContentTransition(lessonId, ContentStatus.QA_PENDING,
                    "VALIDATION_PASSED", actor, null,
                    Map.of("issueCount", result.issues().size()));
        } else {
            orchestrator.applyContentTransition(lessonId, ContentStatus.VALIDATION_FAILED,
                    "VALIDATION_FAILED", actor, null,
                    Map.of("errors", result.errors().size(), "warnings", result.warnings().size()));
        }
        return result;
    }

    @Transactional
    public CfLesson markQaPassed(UUID lessonId, String actor, UUID qaRunId) {
        return orchestrator.applyContentTransition(lessonId, ContentStatus.QA_PASSED,
                "QA_PASSED", actor, qaRunId, null);
    }

    @Transactional
    public CfLesson markQaFailed(UUID lessonId, String actor, UUID qaRunId) {
        boolean escalated = orchestrator.incrementRevisionCount(lessonId, actor);
        if (escalated) {
            return orchestrator.applyContentTransition(lessonId, ContentStatus.HUMAN_REVIEW_REQUIRED,
                    "ESCALATE_TO_HUMAN", actor, qaRunId,
                    Map.of("reason", "REVISION_LIMIT_EXCEEDED"));
        }
        return orchestrator.applyContentTransition(lessonId, ContentStatus.QA_FAILED,
                "QA_FAILED", actor, qaRunId, null);
    }

    @Transactional
    public CfLesson routeToRevision(UUID lessonId, String actor) {
        return orchestrator.applyContentTransition(lessonId, ContentStatus.REVISION,
                "ROUTE_TO_REVISION", actor, null, null);
    }

    @Transactional
    public CfLesson approve(UUID lessonId, String actor) {
        return orchestrator.applyContentTransition(lessonId, ContentStatus.APPROVED,
                "APPROVE", actor, null, null);
    }

    @Transactional
    public CfLesson reject(UUID lessonId, String actor, String notes) {
        return orchestrator.applyContentTransition(lessonId, ContentStatus.REVISION,
                "REJECT", actor, null, Map.of("notes", notes != null ? notes : ""));
    }

    public CfLesson getLesson(UUID lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Lesson not found: " + lessonId));
    }

    public List<CfLesson> getLessonsByDomain(String domainCode) {
        return lessonRepository.findAll().stream()
                .filter(l -> domainCode.equals(l.getDomainCode())).toList();
    }
}
