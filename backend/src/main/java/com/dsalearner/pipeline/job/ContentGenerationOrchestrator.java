package com.dsalearner.pipeline.job;

import com.dsalearner.pipeline.agent.*;
import com.dsalearner.pipeline.domain.ContentStatus;
import com.dsalearner.pipeline.domain.DomainRegistry;
import com.dsalearner.pipeline.domain.LanguageProfile;
import com.dsalearner.pipeline.language.ContentGenerationInput;
import com.dsalearner.pipeline.language.LessonContent;
import com.dsalearner.pipeline.model.entity.CfLesson;
import com.dsalearner.pipeline.model.entity.CfLessonVersion;
import com.dsalearner.pipeline.model.entity.CfPipelineJob;
import com.dsalearner.pipeline.repository.CfLessonRepository;
import com.dsalearner.pipeline.repository.CfLessonVersionRepository;
import com.dsalearner.pipeline.service.PipelineService;
import com.dsalearner.pipeline.statemachine.WorkflowOrchestrator;
import com.dsalearner.pipeline.validation.DeterministicValidator;
import com.dsalearner.pipeline.validation.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Executes a CONTENT_GENERATION pipeline job end-to-end.
 *
 * Flow:
 *   DRAFT/PLANNED → GENERATING → agent execution → GENERATED
 *     → DeterministicValidator → QA_PENDING (pass) / VALIDATION_FAILED (fail)
 *
 * Called by ContentJobWorker; not exposed via HTTP.
 *
 * ContentStatus and JobStatus remain separate throughout.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContentGenerationOrchestrator {

    private final AgentRunner agentRunner;
    private final Agent<ContentGenerationInput, LessonContent> contentGenerationAgent;
    private final PromptRegistry promptRegistry;
    private final ModelRouter modelRouter;
    private final DomainRegistry domainRegistry;
    private final DeterministicValidator validator;
    private final WorkflowOrchestrator workflowOrchestrator;
    private final CfLessonRepository lessonRepository;
    private final CfLessonVersionRepository lessonVersionRepository;

    /**
     * Executes content generation for the given job.
     * Returns the agent run ID (stored as resultReference on the job).
     */
    @Transactional
    public String execute(CfPipelineJob job) {
        UUID lessonId = job.getLessonId();
        int version   = job.getLessonVersion();

        CfLesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new com.dsalearner.exception.NotFoundException("Lesson not found: " + lessonId));

        // Transition to GENERATING
        workflowOrchestrator.applyContentTransition(
                lessonId, ContentStatus.GENERATING, "START_GENERATION", "agent:" + contentGenerationAgent.agentType(), null, null);

        // Build generation input from lesson metadata + job payload
        String topic = extractTopic(job, lesson);
        ContentGenerationInput input = new ContentGenerationInput(
                topic,
                lesson.getCefrLevel() != null ? lesson.getCefrLevel() : "A1",
                lesson.getLanguageCode() != null ? lesson.getLanguageCode() : "de",
                lesson.getStableRef()
        );

        // Resolve prompt and model for lineage recording
        PromptRegistry.ResolvedPrompt prompt = promptRegistry.resolve("language.german.a1.content_generation");
        ModelConfig modelConfig = modelRouter.resolve(
                contentGenerationAgent.agentType(),
                lesson.getDomainCode(),
                lesson.getLanguageCode());

        // Execute via AgentRunner (idempotency + cost tracking built-in)
        AgentOutput<LessonContent> output = agentRunner.run(
                contentGenerationAgent,
                lessonId, version,
                lesson.getDomainCode(), lesson.getLanguageCode(),
                input,
                prompt.id(), prompt.version(), modelConfig.configKey()
        );

        LessonContent content = output.output();

        // Persist generated content into the lesson version
        persistContent(lessonId, version, content, output.agentRunId(), prompt, modelConfig);

        // Transition to GENERATED
        workflowOrchestrator.applyContentTransition(
                lessonId, ContentStatus.GENERATED, "GENERATION_COMPLETE",
                "agent:" + contentGenerationAgent.agentType(), output.agentRunId(), null);

        // Run deterministic validation
        Map<String, Object> contentForValidation = buildValidationPayload(content);
        workflowOrchestrator.applyContentTransition(
                lessonId, ContentStatus.VALIDATION_PENDING, "SUBMIT_FOR_VALIDATION",
                "agent:validator", null, null);

        ValidationResult validation = validator.validate(
                contentForValidation, lesson.getDomainCode(), lesson.getLanguageCode());

        if (validation.passed()) {
            workflowOrchestrator.applyContentTransition(
                    lessonId, ContentStatus.QA_PENDING, "VALIDATION_PASSED",
                    "agent:validator", null,
                    Map.of("issueCount", validation.issues().size()));
            log.info("ContentGenerationOrchestrator: lessonId={} reached QA_PENDING", lessonId);
        } else {
            workflowOrchestrator.applyContentTransition(
                    lessonId, ContentStatus.VALIDATION_FAILED, "VALIDATION_FAILED",
                    "agent:validator", null,
                    Map.of("errors", validation.errors().size(), "warnings", validation.warnings().size()));
            log.warn("ContentGenerationOrchestrator: lessonId={} VALIDATION_FAILED errors={}",
                    lessonId, validation.errors().size());
        }

        return output.agentRunId().toString();
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

        // Record lineage
        Map<String, Object> promptVersions = new HashMap<>(
                lv.getPromptVersions() != null ? lv.getPromptVersions() : Map.of());
        promptVersions.put(contentGenerationAgent.agentType(), prompt.version());
        lv.setPromptVersions(promptVersions);

        Map<String, Object> modelConfigs = new HashMap<>(
                lv.getModelConfigs() != null ? lv.getModelConfigs() : Map.of());
        modelConfigs.put(contentGenerationAgent.agentType(), modelConfig.configKey());
        lv.setModelConfigs(modelConfigs);

        Map<String, Object> generatorRunIds = new HashMap<>(
                lv.getGeneratorRunIds() != null ? lv.getGeneratorRunIds() : Map.of());
        generatorRunIds.put(contentGenerationAgent.agentType(), agentRunId.toString());
        lv.setGeneratorRunIds(generatorRunIds);

        lessonVersionRepository.save(lv);
        log.debug("ContentGenerationOrchestrator: persisted content for lessonId={} version={}", lessonId, version);
    }

    private Map<String, Object> buildValidationPayload(LessonContent content) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("content",    content.toContentMap());
        payload.put("vocabulary", content.toVocabularyMap());
        payload.put("grammar",    content.toGrammarMap());
        payload.put("exercises",  content.toExercisesMap());
        return payload;
    }

    private String extractTopic(CfPipelineJob job, CfLesson lesson) {
        if (job.getPayload() != null && job.getPayload().containsKey("topic")) {
            return (String) job.getPayload().get("topic");
        }
        // Fall back to lesson title or default A1 topic
        return lesson.getTitle() != null ? lesson.getTitle() : "Greetings and Introductions";
    }
}
