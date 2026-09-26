package com.dsalearner.pipeline.job;

import com.dsalearner.exception.NotFoundException;
import com.dsalearner.pipeline.agent.*;
import com.dsalearner.pipeline.domain.ContentStatus;
import com.dsalearner.pipeline.language.ContentGenerationInput;
import com.dsalearner.pipeline.language.LessonContent;
import com.dsalearner.pipeline.model.entity.CfLesson;
import com.dsalearner.pipeline.model.entity.CfLessonVersion;
import com.dsalearner.pipeline.model.entity.CfPipelineJob;
import com.dsalearner.pipeline.provider.*;
import com.dsalearner.pipeline.repository.CfLessonRepository;
import com.dsalearner.pipeline.repository.CfLessonVersionRepository;
import com.dsalearner.pipeline.statemachine.WorkflowOrchestrator;
import com.dsalearner.pipeline.validation.DeterministicValidator;
import com.dsalearner.pipeline.validation.ValidationResult;
import com.dsalearner.pipeline.validation.rules.CefrEnumRule;
import com.dsalearner.pipeline.validation.rules.SchemaRequiredFieldsRule;
import com.dsalearner.pipeline.validation.rules.WordCountRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests the ContentGenerationOrchestrator pipeline flow:
 *   DRAFT/PLANNED → GENERATING → GENERATED → VALIDATION_PENDING → QA_PENDING / VALIDATION_FAILED
 */
@ExtendWith(MockitoExtension.class)
class ContentGenerationOrchestratorTest {

    @Mock AgentRunner agentRunner;
    @Mock Agent<ContentGenerationInput, LessonContent> contentAgent;
    @Mock PromptRegistry promptRegistry;
    @Mock ModelRouter modelRouter;
    @Mock com.dsalearner.pipeline.domain.DomainRegistry domainRegistry;
    @Mock DeterministicValidator validator;
    @Mock WorkflowOrchestrator workflowOrchestrator;
    @Mock CfLessonRepository lessonRepository;
    @Mock CfLessonVersionRepository lessonVersionRepository;
    @Mock com.dsalearner.pipeline.service.PipelineJobService pipelineJobService;

    ContentGenerationOrchestrator orchestrator;

    private final UUID lessonId = UUID.randomUUID();
    private final UUID promptId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        orchestrator = new ContentGenerationOrchestrator(
                agentRunner, contentAgent, promptRegistry, modelRouter, domainRegistry,
                validator, workflowOrchestrator, lessonRepository, lessonVersionRepository,
                pipelineJobService);

        when(contentAgent.agentType()).thenReturn(AgentType.CONTENT_GENERATOR);

        PromptRegistry.ResolvedPrompt prompt = new PromptRegistry.ResolvedPrompt(
                promptId, "language.german.a1.content_generation", 1, "system", "text");
        when(promptRegistry.resolve(any())).thenReturn(prompt);

        ModelConfig modelConfig = new ModelConfig(
                "mock_v1", "mock", "mock-model", 0.3, 1000, 5000,
                BigDecimal.ZERO, BigDecimal.ZERO);
        when(modelRouter.resolve(any(), any(), any())).thenReturn(modelConfig);
    }

    @Test
    void successfulFlowReachesQaPending() {
        setupLessonAndVersion();
        setupSuccessfulAgentOutput();
        when(validator.validate(any(), any(), any())).thenReturn(
                new ValidationResult(true, List.of()));

        String resultRef = orchestrator.execute(buildJob());

        assertNotNull(resultRef);
        // Verify GENERATING transition
        verify(workflowOrchestrator).applyContentTransition(
                eq(lessonId), eq(ContentStatus.GENERATING), any(), any(), isNull(), isNull());
        // Verify GENERATED transition
        verify(workflowOrchestrator).applyContentTransition(
                eq(lessonId), eq(ContentStatus.GENERATED), any(), any(), any(), isNull());
        // Verify VALIDATION_PENDING
        verify(workflowOrchestrator).applyContentTransition(
                eq(lessonId), eq(ContentStatus.VALIDATION_PENDING), any(), any(), isNull(), isNull());
        // Verify QA_PENDING
        verify(workflowOrchestrator).applyContentTransition(
                eq(lessonId), eq(ContentStatus.QA_PENDING), any(), any(), isNull(), any());
    }

    @Test
    void validationFailureReachesValidationFailed() {        setupLessonAndVersion();
        setupSuccessfulAgentOutput();
        when(validator.validate(any(), any(), any())).thenReturn(
                ValidationResult.fail(List.of(
                        com.dsalearner.pipeline.validation.ValidationIssue.error(
                                "MISSING_CONTENT", "content", "required field missing"))));

        orchestrator.execute(buildJob());

        verify(workflowOrchestrator).applyContentTransition(
                eq(lessonId), eq(ContentStatus.VALIDATION_FAILED), any(), any(), isNull(), any());
        // QA_PENDING must NOT be reached
        verify(workflowOrchestrator, never()).applyContentTransition(
                eq(lessonId), eq(ContentStatus.QA_PENDING), any(), any(), any(), any());
    }

    @Test
    void contentIsPersistsToLessonVersion() {
        setupLessonAndVersion();
        setupSuccessfulAgentOutput();
        when(validator.validate(any(), any(), any())).thenReturn(
                new ValidationResult(true, List.of()));

        orchestrator.execute(buildJob());

        ArgumentCaptor<CfLessonVersion> captor = ArgumentCaptor.forClass(CfLessonVersion.class);
        verify(lessonVersionRepository, atLeastOnce()).save(captor.capture());
        CfLessonVersion saved = captor.getAllValues().get(0);
        assertNotNull(saved.getContent());
        assertNotNull(saved.getVocabulary());
        assertNotNull(saved.getGrammar());
        assertNotNull(saved.getExercises());
        assertNotNull(saved.getBlueprint());
        assertNotNull(saved.getGeneratorRunIds());
    }

    /**
     * Blocker 3 regression: PLANNED → GENERATING transition fires on first attempt.
     */
    @Test
    void firstAttemptTransitionsPlannedToGenerating() {
        setupLessonAndVersion();  // lesson is PLANNED
        setupSuccessfulAgentOutput();
        when(validator.validate(any(), any(), any())).thenReturn(new ValidationResult(true, List.of()));

        orchestrator.execute(buildJob());

        verify(workflowOrchestrator).applyContentTransition(
                eq(lessonId), eq(ContentStatus.GENERATING), eq("START_GENERATION"), any(), isNull(), isNull());
    }

    /**
     * Blocker 3 regression: on retry, lesson is already GENERATING.
     * The orchestrator must NOT attempt GENERATING→GENERATING through the state machine.
     * Generation must proceed normally and reach QA_PENDING.
     */
    @Test
    void retryWhileAlreadyGeneratingSkipsTransitionAndSucceeds() {
        // Lesson is already GENERATING (first attempt left it here after a mid-generation failure)
        CfLesson generatingLesson = CfLesson.builder()
                .id(lessonId).stableRef("de-a1-u01-l01")
                .domainCode("language").languageCode("de").cefrLevel("A1")
                .title("Greetings").contentStatus(ContentStatus.GENERATING)
                .currentVersion(1).build();
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(generatingLesson));
        when(workflowOrchestrator.applyContentTransition(any(), any(), any(), any(), any(), any()))
                .thenReturn(generatingLesson);

        CfLessonVersion version = CfLessonVersion.builder()
                .lessonId(lessonId).version(1).contentStatus("GENERATING").build();
        when(lessonVersionRepository.findByLessonIdAndVersion(lessonId, 1))
                .thenReturn(Optional.of(version));
        when(lessonVersionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        setupSuccessfulAgentOutput();
        when(validator.validate(any(), any(), any())).thenReturn(new ValidationResult(true, List.of()));

        String resultRef = orchestrator.execute(buildJob());

        assertNotNull(resultRef);

        // START_GENERATION must NOT be called (already GENERATING)
        verify(workflowOrchestrator, never()).applyContentTransition(
                eq(lessonId), eq(ContentStatus.GENERATING), any(), any(), any(), any());

        // Generation must reach GENERATED and QA_PENDING
        verify(workflowOrchestrator).applyContentTransition(
                eq(lessonId), eq(ContentStatus.GENERATED), any(), any(), any(), isNull());
        verify(workflowOrchestrator).applyContentTransition(
                eq(lessonId), eq(ContentStatus.QA_PENDING), any(), any(), isNull(), any());
    }

    /**
     * Blocker 3 regression: state machine strictness is preserved for genuinely invalid transitions.
     * Covered by StateMachineTest.invalidContentTransitions — DRAFT→GENERATING and
     * GENERATING→GENERATING are both in that parameterized test.
     */

    private void setupLessonAndVersion() {
        CfLesson lesson = CfLesson.builder()
                .id(lessonId).stableRef("de-a1-u01-l01")
                .domainCode("language").languageCode("de").cefrLevel("A1")
                .title("Greetings").contentStatus(ContentStatus.PLANNED)
                .currentVersion(1).build();
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(workflowOrchestrator.applyContentTransition(any(), any(), any(), any(), any(), any()))
                .thenReturn(lesson);

        CfLessonVersion version = CfLessonVersion.builder()
                .lessonId(lessonId).version(1)
                .contentStatus("PLANNED").build();
        when(lessonVersionRepository.findByLessonIdAndVersion(lessonId, 1))
                .thenReturn(Optional.of(version));
        when(lessonVersionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @SuppressWarnings("unchecked")
    private void setupSuccessfulAgentOutput() {
        // Parse mock lesson content
        LessonContent content = parseMockContent();
        UUID runId = UUID.randomUUID();
        AgentOutput<LessonContent> output = new AgentOutput<>(
                runId, AgentOutput.Status.SUCCEEDED, content, 0.95,
                List.of(), List.of(), false,
                new AgentOutput.AgentMetadata("mock_v1", "mock-model", "mock",
                        150, 800, 0.0, 100, promptId, 1, "1.0"));

        doReturn(output).when(agentRunner).run(any(), eq(lessonId), eq(1), any(), any(), any(), any(), anyInt(), any());
    }

    private LessonContent parseMockContent() {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String json = MockLlmProvider.MOCK_GERMAN_A1_LESSON.strip();
            int start = json.indexOf('{');
            int end = json.lastIndexOf('}');
            java.util.Map<String, Object> root = mapper.readValue(
                    json.substring(start, end + 1), new com.fasterxml.jackson.core.type.TypeReference<>() {});
            return new LessonContent(
                    (java.util.Map<String, Object>) root.get("metadata"),
                    (java.util.List<String>) root.get("objectives"),
                    (java.util.Map<String, Object>) root.get("explanation"),
                    (java.util.List<java.util.Map<String, Object>>) root.get("vocabulary"),
                    (java.util.Map<String, Object>) root.get("grammar"),
                    (java.util.List<java.util.Map<String, Object>>) root.get("examples"),
                    (java.util.List<java.util.Map<String, Object>>) root.get("exercises")
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CfPipelineJob buildJob() {
        return CfPipelineJob.builder()
                .id(UUID.randomUUID()).lessonId(lessonId).lessonVersion(1)
                .jobType("CONTENT_GENERATION").status("RUNNING").attempt(1).maxAttempts(3)
                .payload(Map.of("topic", "Greetings and Introductions"))
                .build();
    }

    /**
     * Regression: buildValidationPayload must include 'title' and 'cefrLevel' at the top level.
     * Previously these were missing, causing SchemaRequiredFieldsRule to always fail for
     * language-domain lessons.
     */
    @Test
    void validationPayloadIncludesTitleAndCefrLevel() {
        setupLessonAndVersion();
        setupSuccessfulAgentOutput();

        // Use the REAL validator rules — not a mock — to prove the payload is correct
        DeterministicValidator realValidator = new DeterministicValidator(List.of(
                new SchemaRequiredFieldsRule(), new CefrEnumRule(), new WordCountRule()));
        ContentGenerationOrchestrator realOrch = new ContentGenerationOrchestrator(
                agentRunner, contentAgent, promptRegistry, modelRouter, domainRegistry,
                realValidator, workflowOrchestrator, lessonRepository, lessonVersionRepository,
                pipelineJobService);

        String resultRef = realOrch.execute(buildJob());
        assertNotNull(resultRef);

        // With real rules, must reach QA_PENDING (not VALIDATION_FAILED)
        verify(workflowOrchestrator).applyContentTransition(
                eq(lessonId), eq(ContentStatus.QA_PENDING), any(), any(), isNull(), any());
        verify(workflowOrchestrator, never()).applyContentTransition(
                eq(lessonId), eq(ContentStatus.VALIDATION_FAILED), any(), any(), any(), any());
    }
}
