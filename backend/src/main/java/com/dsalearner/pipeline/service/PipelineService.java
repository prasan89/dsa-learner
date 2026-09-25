package com.dsalearner.pipeline.service;

import com.dsalearner.exception.NotFoundException;
import com.dsalearner.pipeline.domain.ContentStatus;
import com.dsalearner.pipeline.domain.DomainRegistry;
import com.dsalearner.pipeline.domain.LanguageProfile;
import com.dsalearner.pipeline.exception.FrozenVersionException;
import com.dsalearner.pipeline.model.entity.CfLesson;
import com.dsalearner.pipeline.model.entity.CfLessonVersion;
import com.dsalearner.pipeline.repository.CfLessonRepository;
import com.dsalearner.pipeline.repository.CfLessonVersionRepository;
import com.dsalearner.pipeline.repository.CfWorkflowEventRepository;
import com.dsalearner.pipeline.statemachine.WorkflowOrchestrator;
import com.dsalearner.pipeline.validation.DeterministicValidator;
import com.dsalearner.pipeline.validation.ValidationResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    // Used only for deep-copying JSONB map fields during version creation.
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

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

    /**
     * Freezes a lesson version, making it immutable.
     * A frozen version cannot have its content updated.
     * Published content must go through this path before being served.
     */
    @Transactional
    public CfLessonVersion freezeVersion(UUID lessonId, int version, String actor) {
        CfLessonVersion lessonVersion = versionRepository.findByLessonIdAndVersion(lessonId, version)
                .orElseThrow(() -> new NotFoundException(
                        "Lesson version not found: lessonId=%s version=%d".formatted(lessonId, version)));

        if (lessonVersion.isFrozen()) {
            throw new FrozenVersionException(
                    "Version %d of lesson %s is already frozen".formatted(version, lessonId));
        }

        lessonVersion.setFrozen(true);
        CfLessonVersion saved = versionRepository.save(lessonVersion);
        log.info("Frozen lessonId={} version={} by={}", lessonId, version, actor);
        return saved;
    }

    /**
     * Updates mutable content fields on a lesson version.
     * Throws FrozenVersionException if the version is frozen.
     */
    @Transactional
    public CfLessonVersion updateVersionContent(UUID lessonId, int version,
                                                 Map<String, Object> content, String actor) {
        CfLessonVersion lessonVersion = versionRepository.findByLessonIdAndVersion(lessonId, version)
                .orElseThrow(() -> new NotFoundException(
                        "Lesson version not found: lessonId=%s version=%d".formatted(lessonId, version)));

        if (lessonVersion.isFrozen()) {
            throw new FrozenVersionException(
                    "Cannot modify frozen version %d of lesson %s. Create a new version instead."
                            .formatted(version, lessonId));
        }

        if (content.containsKey("content")) {
            lessonVersion.setContent((Map<String, Object>) content.get("content"));
        }
        if (content.containsKey("blueprint")) {
            lessonVersion.setBlueprint((Map<String, Object>) content.get("blueprint"));
        }
        if (content.containsKey("vocabulary")) {
            lessonVersion.setVocabulary((Map<String, Object>) content.get("vocabulary"));
        }
        if (content.containsKey("grammar")) {
            lessonVersion.setGrammar((Map<String, Object>) content.get("grammar"));
        }
        if (content.containsKey("exercises")) {
            lessonVersion.setExercises((Map<String, Object>) content.get("exercises"));
        }

        CfLessonVersion saved = versionRepository.save(lessonVersion);
        log.info("Updated version content lessonId={} version={} by={}", lessonId, version, actor);
        return saved;
    }

    /**
     * Creates a new version for revision, inheriting the current version's content.
     *
     * Content fields (blueprint, content, vocabulary, grammar, exercises, audio_manifest)
     * are deep-copied via Jackson so v1 and v2 share no mutable object references.
     *
     * Lineage fields inherited as-is (read-only context for reviewers):
     *   prompt_versions, model_configs, generator_run_ids, revision_log, checksum
     *
     * qa_run_ids is intentionally NOT inherited: v2 has not been QA'd yet.
     * The new version starts unfrozen with REVISION status.
     *
     * v1 is never modified.
     */
    @Transactional
    public CfLessonVersion createNextVersion(UUID lessonId, String actor) {
        CfLesson lesson = getLesson(lessonId);
        int currentVersion = lesson.getCurrentVersion();
        int nextVersion = currentVersion + 1;

        CfLessonVersion source = versionRepository.findByLessonIdAndVersion(lessonId, currentVersion)
                .orElseThrow(() -> new NotFoundException(
                        "Current version not found: lessonId=%s version=%d".formatted(lessonId, currentVersion)));

        CfLessonVersion newVersion = CfLessonVersion.builder()
                .lessonId(lessonId)
                .version(nextVersion)
                .contentStatus(ContentStatus.REVISION.name())
                .frozen(false)
                // Deep-copy content fields so v1 and v2 share no mutable references.
                .blueprint(deepCopy(source.getBlueprint()))
                .content(deepCopy(source.getContent()))
                .vocabulary(deepCopy(source.getVocabulary()))
                .grammar(deepCopy(source.getGrammar()))
                .exercises(deepCopy(source.getExercises()))
                .audioManifest(deepCopy(source.getAudioManifest()))
                // Inherit lineage for reviewer context; qa_run_ids omitted (v2 not yet QA'd).
                .promptVersions(deepCopy(source.getPromptVersions()))
                .modelConfigs(deepCopy(source.getModelConfigs()))
                .generatorRunIds(deepCopy(source.getGeneratorRunIds()))
                .revisionLog(deepCopy(source.getRevisionLog()))
                .checksum(source.getChecksum())
                .build();
        versionRepository.save(newVersion);

        lesson.setCurrentVersion(nextVersion);
        lessonRepository.save(lesson);

        log.info("Created version {} for lessonId={} from v{} by={}", nextVersion, lessonId, currentVersion, actor);
        return newVersion;
    }

    /**
     * Deep-copies a JSONB map via Jackson serialization/deserialization.
     * Returns null when the source map is null (field not yet populated).
     * Guarantees v1 and v2 never share mutable Map references.
     */
    private Map<String, Object> deepCopy(Map<String, Object> source) {
        if (source == null) return null;
        try {
            return MAPPER.readValue(MAPPER.writeValueAsBytes(source), MAP_TYPE);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deep-copy version content map", e);
        }
    }
}
