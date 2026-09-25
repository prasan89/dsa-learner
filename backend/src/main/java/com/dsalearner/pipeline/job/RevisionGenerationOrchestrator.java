package com.dsalearner.pipeline.job;

import com.dsalearner.pipeline.agent.*;
import com.dsalearner.pipeline.domain.ContentStatus;
import com.dsalearner.pipeline.language.LessonContent;
import com.dsalearner.pipeline.language.RevisionGenerationInput;
import com.dsalearner.pipeline.model.entity.CfLesson;
import com.dsalearner.pipeline.model.entity.CfLessonVersion;
import com.dsalearner.pipeline.model.entity.CfPipelineJob;
import com.dsalearner.pipeline.repository.CfLessonRepository;
import com.dsalearner.pipeline.repository.CfLessonVersionRepository;
import com.dsalearner.pipeline.service.PipelineJobService;
import com.dsalearner.pipeline.statemachine.WorkflowOrchestrator;
import com.dsalearner.pipeline.validation.DeterministicValidator;
import com.dsalearner.pipeline.validation.ValidationResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Executes a REVISION_GENERATION pipeline job end-to-end.
 *
 * Flow:
 *   REVISION state (source version has revision_feedback)
 *     → create new lesson version (currentVersion + 1)
 *     → REVISION_GENERATING transition
 *     → revision agent (LLM call outside any transaction)
 *     → persist new version content
 *     → GENERATED transition
 *     → DeterministicValidator
 *     → PASS  → QA_PENDING → enqueue QA_CONTENT job for new version
 *     → FAIL  → VALIDATION_FAILED → increment revision count
 *                 → if escalated: HUMAN_REVIEW_REQUIRED
 *                 → else: REVISION (wait for next REVISION_GENERATION job)
 *
 * Idempotency key in AgentRunner:
 *   lessonId + ":" + newVersion + ":" + REVISION_GENERATOR + ":" + hash(payload)
 *
 * Transaction boundaries:
 *   - createNewVersion() — own @Transactional, committed before LLM call
 *   - All WorkflowOrchestrator transitions — each in its own @Transactional
 *   - LLM call runs with no DB connection held
 *   - persistContent() — saves via repository (implicit transaction per save)
 *
 * This method intentionally has NO @Transactional.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RevisionGenerationOrchestrator {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AgentRunner agentRunner;
    private final Agent<RevisionGenerationInput, LessonContent> revisionGenerationAgent;
    private final PromptRegistry promptRegistry;
    private final ModelRouter modelRouter;
    private final DeterministicValidator validator;
    private final WorkflowOrchestrator workflowOrchestrator;
    private final CfLessonRepository lessonRepository;
    private final CfLessonVersionRepository lessonVersionRepository;
    private final PipelineJobService pipelineJobService;

    /**
     * Executes revision generation for the given job.
     * Returns the new agent run ID as the job's resultReference.
     */
    public String execute(CfPipelineJob job) {
        UUID lessonId    = job.getLessonId();
        int  sourceVersion = job.getLessonVersion();  // version that failed QA

        CfLesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new com.dsalearner.exception.NotFoundException("Lesson not found: " + lessonId));

        // Read source version to get its content + revision_feedback
        CfLessonVersion sourceVersion_ = lessonVersionRepository.findByLessonIdAndVersion(lessonId, sourceVersion)
                .orElseThrow(() -> new com.dsalearner.exception.NotFoundException(
                        "LessonVersion not found: lessonId=%s version=%d".formatted(lessonId, sourceVersion)));

        String feedbackJson = extractRevisionFeedback(sourceVersion_, lessonId);
        String lessonJson   = buildLessonJson(sourceVersion_, lessonId);

        // Create the new lesson version row (increments lesson.currentVersion atomically)
        int newVersion = createNewVersion(lessonId, sourceVersion, lesson);

        log.info("RevisionGenerationOrchestrator: creating revision lessonId={} sourceVersion={} newVersion={}",
                lessonId, sourceVersion, newVersion);

        // Transition to GENERATING (skip if already there — idempotent on retry)
        if (lesson.getContentStatus() != ContentStatus.GENERATING) {
            workflowOrchestrator.applyContentTransition(
                    lessonId, ContentStatus.GENERATING, "START_REVISION_GENERATION",
                    "agent:" + AgentType.REVISION_GENERATOR, null, Map.of("newVersion", newVersion));
        }

        // Build agent input
        RevisionGenerationInput input = new RevisionGenerationInput(lessonJson, feedbackJson, sourceVersion);

        PromptRegistry.ResolvedPrompt prompt = promptRegistry.resolve("language.german.a1.revision_generation");
        ModelConfig modelConfig = modelRouter.resolve(
                AgentType.REVISION_GENERATOR,
                lesson.getDomainCode(),
                lesson.getLanguageCode());

        // Execute via AgentRunner (idempotency key: lessonId:newVersion:revision_generator:hash)
        AgentOutput<LessonContent> output = agentRunner.run(
                revisionGenerationAgent,
                lessonId, newVersion,
                lesson.getDomainCode(), lesson.getLanguageCode(),
                input,
                prompt.id(), prompt.version(), modelConfig.configKey()
        );

        LessonContent content = output.output();

        // Persist revised content into the new lesson version
        persistContent(lessonId, newVersion, content, output.agentRunId(), prompt, modelConfig);

        // Transition to GENERATED
        workflowOrchestrator.applyContentTransition(
                lessonId, ContentStatus.GENERATED, "REVISION_GENERATION_COMPLETE",
                "agent:" + AgentType.REVISION_GENERATOR, output.agentRunId(),
                Map.of("newVersion", newVersion, "sourceVersion", sourceVersion));

        // Run deterministic validation on the revised content
        Map<String, Object> validationPayload = buildValidationPayload(content);
        workflowOrchestrator.applyContentTransition(
                lessonId, ContentStatus.VALIDATION_PENDING, "SUBMIT_FOR_VALIDATION",
                "agent:validator", null, null);

        ValidationResult validation = validator.validate(
                validationPayload, lesson.getDomainCode(), lesson.getLanguageCode());

        if (validation.passed()) {
            workflowOrchestrator.applyContentTransition(
                    lessonId, ContentStatus.QA_PENDING, "VALIDATION_PASSED",
                    "agent:validator", null,
                    Map.of("issueCount", validation.issues().size(), "newVersion", newVersion));

            // Enqueue QA job for the new version — full 4-agent QA pass
            pipelineJobService.submitQaContent(lessonId, newVersion, Map.of(), "orchestrator:revision");

            log.info("RevisionGenerationOrchestrator: lessonId={} newVersion={} reached QA_PENDING, QA job enqueued",
                    lessonId, newVersion);
        } else {
            workflowOrchestrator.applyContentTransition(
                    lessonId, ContentStatus.VALIDATION_FAILED, "VALIDATION_FAILED",
                    "agent:validator", null,
                    Map.of("errors", validation.errors().size(), "newVersion", newVersion));

            // Validation failure on a revised version: re-enter REVISION so QaOrchestrator
            // can trigger another revision attempt. Revision count is NOT incremented here —
            // only QaOrchestrator increments it (on QA FAIL). Validation failures are internal
            // correctness checks, not QA decisions.
            workflowOrchestrator.applyContentTransition(
                    lessonId, ContentStatus.REVISION, "VALIDATION_FAILED_REROUTE",
                    "orchestrator:revision", null,
                    Map.of("failedVersion", newVersion));

            // Re-enqueue a fresh revision job pointing at the failed version so the
            // pipeline self-heals without human intervention.
            pipelineJobService.submitRevisionGeneration(lessonId, newVersion, Map.of(), "orchestrator:revision");

            log.warn("RevisionGenerationOrchestrator: VALIDATION_FAILED after revision, re-entering REVISION lessonId={}", lessonId);
        }

        return output.agentRunId().toString();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    @Transactional
    public int createNewVersion(UUID lessonId, int sourceVersion, CfLesson lesson) {
        // Re-load inside transaction to get latest currentVersion
        CfLesson freshLesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new com.dsalearner.exception.NotFoundException("Lesson not found: " + lessonId));

        int newVersion = freshLesson.getCurrentVersion() + 1;
        freshLesson.setCurrentVersion(newVersion);
        lessonRepository.save(freshLesson);

        CfLessonVersion newLv = CfLessonVersion.builder()
                .lessonId(lessonId)
                .version(newVersion)
                .parentVersion(sourceVersion)
                .contentStatus(ContentStatus.GENERATING.name())
                .build();
        lessonVersionRepository.save(newLv);

        return newVersion;
    }

    private void persistContent(UUID lessonId, int version, LessonContent content,
                                UUID agentRunId, PromptRegistry.ResolvedPrompt prompt,
                                ModelConfig modelConfig) {
        CfLessonVersion lv = lessonVersionRepository.findByLessonIdAndVersion(lessonId, version)
                .orElseThrow(() -> new com.dsalearner.exception.NotFoundException(
                        "LessonVersion not found: lessonId=%s version=%d".formatted(lessonId, version)));

        lv.setBlueprint(content.toBlueprintMap());
        lv.setContent(content.toContentMap());
        lv.setVocabulary(content.toVocabularyMap());
        lv.setGrammar(content.toGrammarMap());
        lv.setExercises(content.toExercisesMap());

        Map<String, Object> promptVersions = new HashMap<>(
                lv.getPromptVersions() != null ? lv.getPromptVersions() : Map.of());
        promptVersions.put(AgentType.REVISION_GENERATOR, prompt.version());
        lv.setPromptVersions(promptVersions);

        Map<String, Object> modelConfigs = new HashMap<>(
                lv.getModelConfigs() != null ? lv.getModelConfigs() : Map.of());
        modelConfigs.put(AgentType.REVISION_GENERATOR, modelConfig.configKey());
        lv.setModelConfigs(modelConfigs);

        Map<String, Object> generatorRunIds = new HashMap<>(
                lv.getGeneratorRunIds() != null ? lv.getGeneratorRunIds() : Map.of());
        generatorRunIds.put(AgentType.REVISION_GENERATOR, agentRunId.toString());
        lv.setGeneratorRunIds(generatorRunIds);

        lessonVersionRepository.save(lv);
    }

    private String buildLessonJson(CfLessonVersion lv, UUID lessonId) {
        Map<String, Object> lessonMap = new java.util.LinkedHashMap<>();
        if (lv.getContent()    != null) lessonMap.putAll(lv.getContent());
        if (lv.getVocabulary() != null) lessonMap.put("vocabulary", lv.getVocabulary());
        if (lv.getGrammar()    != null) lessonMap.put("grammar",    lv.getGrammar());
        if (lv.getExercises()  != null) lessonMap.put("exercises",  lv.getExercises());
        if (lv.getBlueprint()  != null) lessonMap.put("metadata",   lv.getBlueprint());

        try {
            return MAPPER.writeValueAsString(lessonMap);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "RevisionGenerationOrchestrator: failed to serialize lesson content for lessonId=" + lessonId, e);
        }
    }

    private String extractRevisionFeedback(CfLessonVersion lv, UUID lessonId) {
        if (lv.getRevisionFeedback() == null || lv.getRevisionFeedback().isEmpty()) {
            log.warn("RevisionGenerationOrchestrator: no revision_feedback for lessonId={} version={}; "
                    + "proceeding with empty feedback", lessonId, lv.getVersion());
            return "{\"issues\": []}";
        }
        try {
            return MAPPER.writeValueAsString(lv.getRevisionFeedback());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "RevisionGenerationOrchestrator: failed to serialize revision_feedback for lessonId=" + lessonId, e);
        }
    }

    private Map<String, Object> buildValidationPayload(LessonContent content) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title",      content.metadata().getOrDefault("topic", ""));
        payload.put("cefrLevel",  content.metadata().getOrDefault("cefrLevel", "A1"));
        payload.put("content",    content.toContentMap());
        payload.put("vocabulary", content.toVocabularyMap());
        payload.put("grammar",    content.toGrammarMap());
        payload.put("exercises",  content.toExercisesMap());
        return payload;
    }
}
