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
import com.dsalearner.pipeline.validation.ValidationIssue;
import com.dsalearner.pipeline.validation.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevisionGenerationOrchestratorTest {

    @Mock AgentRunner agentRunner;
    @Mock Agent<RevisionGenerationInput, LessonContent> revisionGenerationAgent;
    @Mock PromptRegistry promptRegistry;
    @Mock ModelRouter modelRouter;
    @Mock DeterministicValidator validator;
    @Mock WorkflowOrchestrator workflowOrchestrator;
    @Mock CfLessonRepository lessonRepository;
    @Mock CfLessonVersionRepository lessonVersionRepository;
    @Mock PipelineJobService pipelineJobService;

    RevisionGenerationOrchestrator orchestrator;

    private final UUID lessonId   = UUID.randomUUID();
    private final UUID promptId   = UUID.randomUUID();
    private final UUID agentRunId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        orchestrator = new RevisionGenerationOrchestrator(
                agentRunner, revisionGenerationAgent, promptRegistry, modelRouter,
                validator, workflowOrchestrator, lessonRepository,
                lessonVersionRepository, pipelineJobService);

        lenient().when(revisionGenerationAgent.agentType()).thenReturn(AgentType.REVISION_GENERATOR);

        PromptRegistry.ResolvedPrompt prompt = new PromptRegistry.ResolvedPrompt(
                promptId, "language.german.a1.revision_generation", 1,
                "system", "{{lessonJson}}\n{{revisionFeedback}}");
        lenient().when(promptRegistry.resolve(any())).thenReturn(prompt);

        ModelConfig modelConfig = new ModelConfig(
                "sonnet_gen_v1", "mock", "mock-sonnet", 0.7, 4096, 60000,
                BigDecimal.ZERO, BigDecimal.ZERO);
        lenient().when(modelRouter.resolve(any(), any(), any())).thenReturn(modelConfig);
    }

    // ── Builders ─────────────────────────────────────────────────────────────

    private CfLesson buildLesson(ContentStatus status) {
        CfLesson l = new CfLesson();
        l.setId(lessonId);
        l.setDomainCode("language");
        l.setLanguageCode("de");
        l.setContentStatus(status);
        l.setCurrentVersion(1);
        l.setRevisionCount(0);
        l.setMaxRevisionAttempts(3);
        return l;
    }

    private CfLessonVersion buildSourceVersion(boolean hasFeedback) {
        CfLessonVersion lv = new CfLessonVersion();
        lv.setLessonId(lessonId);
        lv.setVersion(1);
        lv.setContent(Map.of("objectives", List.of("Learn to greet")));
        lv.setVocabulary(Map.of("items", List.of(Map.of("german", "Hallo", "english", "Hello"))));
        lv.setGrammar(Map.of("title", "sein"));
        lv.setExercises(Map.of("items", List.of()));
        lv.setBlueprint(Map.of("topic", "Greetings", "cefrLevel", "A1"));
        if (hasFeedback) {
            lv.setRevisionFeedback(Map.of("issues", List.of(
                    Map.of("severity", "ERROR", "field", "exercises[0]", "message", "Wrong answer"))));
        }
        return lv;
    }

    private CfLessonVersion buildNewVersion() {
        CfLessonVersion lv = new CfLessonVersion();
        lv.setLessonId(lessonId);
        lv.setVersion(2);
        lv.setContentStatus(ContentStatus.GENERATING.name());
        lv.setParentVersion(1);
        return lv;
    }

    private CfPipelineJob buildJob() {
        CfPipelineJob job = new CfPipelineJob();
        job.setId(UUID.randomUUID());
        job.setLessonId(lessonId);
        job.setLessonVersion(1);
        job.setJobType("REVISION_GENERATION");
        job.setPayload(Map.of());
        return job;
    }

    @SuppressWarnings("unchecked")
    private AgentOutput<LessonContent> buildAgentOutput() {
        LessonContent content = new LessonContent(
                Map.of("topic", "Greetings", "cefrLevel", "A1", "language", "de", "estimatedMinutes", 20),
                List.of("Learn to greet"),
                Map.of("intro", "In this lesson..."),
                List.of(Map.of("german", "Hallo", "english", "Hello")),
                Map.of("title", "sein"),
                List.of(Map.of("german", "Hallo!", "english", "Hello!")),
                List.of(Map.of("type", "TRANSLATION", "question", "Hello", "correctAnswer", "Hallo"))
        );
        return new AgentOutput<>(agentRunId, AgentOutput.Status.SUCCEEDED,
                content, 0.95, List.of(), List.of(), false,
                new AgentOutput.AgentMetadata("sonnet_gen_v1", "mock-sonnet", "mock",
                        1000, 2000, 0.0, 5000, promptId, 1, "1.0"));
    }

    private void stubCommonMocks(boolean withFeedback) {
        CfLesson lesson = buildLesson(ContentStatus.REVISION);
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonVersionRepository.findByLessonIdAndVersion(eq(lessonId), eq(1)))
                .thenReturn(Optional.of(buildSourceVersion(withFeedback)));
        when(lessonVersionRepository.findByLessonIdAndVersion(eq(lessonId), eq(2)))
                .thenReturn(Optional.of(buildNewVersion()));
        when(lessonVersionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(lessonRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        // doReturn bypasses generic type erasure check in Mockito
        doReturn(buildAgentOutput()).when(agentRunner).run(
                any(), any(), anyInt(), any(), any(), any(), any(), anyInt(), any());
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test
    void validationPass_transitionsToQaPendingAndEnqueuesQaJob() {
        stubCommonMocks(true);
        when(validator.validate(any(), any(), any())).thenReturn(ValidationResult.pass());

        String result = orchestrator.execute(buildJob());

        assertEquals(agentRunId.toString(), result);
        verify(workflowOrchestrator).applyContentTransition(
                eq(lessonId), eq(ContentStatus.QA_PENDING), any(), any(), any(), any());
        verify(pipelineJobService).submitQaContent(eq(lessonId), eq(2), any(), any());
        verify(workflowOrchestrator, never()).applyContentTransition(
                eq(lessonId), eq(ContentStatus.HUMAN_REVIEW_REQUIRED), any(), any(), any(), any());
    }

    @Test
    void validationFail_reEntersRevisionAndEnqueuesNewRevisionJob() {
        stubCommonMocks(true);
        when(validator.validate(any(), any(), any()))
                .thenReturn(ValidationResult.fail(List.of(
                        ValidationIssue.error("SCHEMA_ERROR", "exercises", "Missing exercises"))));

        orchestrator.execute(buildJob());

        verify(workflowOrchestrator).applyContentTransition(
                eq(lessonId), eq(ContentStatus.VALIDATION_FAILED), any(), any(), any(), any());
        verify(workflowOrchestrator).applyContentTransition(
                eq(lessonId), eq(ContentStatus.REVISION), any(), any(), any(), any());
        // Re-enqueues a revision job for the failed version (self-healing)
        verify(pipelineJobService).submitRevisionGeneration(eq(lessonId), anyInt(), any(), any());
        verify(pipelineJobService, never()).submitQaContent(any(), anyInt(), any(), any());
        // Does NOT increment revision count — only QA fail does that
        verify(workflowOrchestrator, never()).incrementRevisionCount(any(), any());
    }

    @Test
    void validationFail_doesNotCallIncrementRevisionCount() {
        stubCommonMocks(true);
        when(validator.validate(any(), any(), any()))
                .thenReturn(ValidationResult.fail(List.of(
                        ValidationIssue.error("SCHEMA_ERROR", "exercises", "Missing exercises"))));

        orchestrator.execute(buildJob());

        verify(workflowOrchestrator, never()).incrementRevisionCount(any(), any());
    }

    @Test
    void newVersionIsCreated_withParentVersionSet() {
        stubCommonMocks(true);
        when(validator.validate(any(), any(), any())).thenReturn(ValidationResult.pass());

        orchestrator.execute(buildJob());

        // Lesson currentVersion should be incremented to 2
        ArgumentCaptor<CfLesson> lessonCaptor = ArgumentCaptor.forClass(CfLesson.class);
        verify(lessonRepository, atLeastOnce()).save(lessonCaptor.capture());
        assertTrue(lessonCaptor.getAllValues().stream().anyMatch(l -> l.getCurrentVersion() == 2),
                "Expected lesson currentVersion to be incremented to 2");
    }

    @Test
    void revisionFeedbackIsPassedToAgent() {
        stubCommonMocks(true);
        when(validator.validate(any(), any(), any())).thenReturn(ValidationResult.pass());

        orchestrator.execute(buildJob());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<RevisionGenerationInput> inputCaptor =
                ArgumentCaptor.forClass(RevisionGenerationInput.class);
        verify(agentRunner).run(any(), any(), anyInt(), any(), any(),
                inputCaptor.capture(), any(), anyInt(), any());

        RevisionGenerationInput input = inputCaptor.getValue();
        assertNotNull(input.revisionFeedback(), "revision feedback must not be null");
        assertTrue(input.revisionFeedback().contains("ERROR"), "feedback should contain the QA error");
        assertEquals(1, input.sourceVersion(), "source version must be 1");
    }

    @Test
    void emptyRevisionFeedback_stillProceeds() {
        stubCommonMocks(false); // source version has NO revision_feedback
        when(validator.validate(any(), any(), any())).thenReturn(ValidationResult.pass());

        assertDoesNotThrow(() -> orchestrator.execute(buildJob()));
        verify(pipelineJobService).submitQaContent(eq(lessonId), eq(2), any(), any());
    }

    @Test
    void generatingTransitionIsSkipped_ifAlreadyGenerating() {
        CfLesson lesson = buildLesson(ContentStatus.GENERATING);
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonVersionRepository.findByLessonIdAndVersion(eq(lessonId), eq(1)))
                .thenReturn(Optional.of(buildSourceVersion(true)));
        when(lessonVersionRepository.findByLessonIdAndVersion(eq(lessonId), eq(2)))
                .thenReturn(Optional.of(buildNewVersion()));
        when(lessonVersionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(lessonRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doReturn(buildAgentOutput()).when(agentRunner).run(
                any(), any(), anyInt(), any(), any(), any(), any(), anyInt(), any());
        when(validator.validate(any(), any(), any())).thenReturn(ValidationResult.pass());

        orchestrator.execute(buildJob());

        verify(workflowOrchestrator, never()).applyContentTransition(
                eq(lessonId), eq(ContentStatus.GENERATING), any(), any(), any(), any());
    }

    @Test
    void qaJobUsesNewVersionNumber() {
        stubCommonMocks(true);
        when(validator.validate(any(), any(), any())).thenReturn(ValidationResult.pass());

        orchestrator.execute(buildJob());

        // QA job must target version 2 (the new revision), not version 1 (source)
        verify(pipelineJobService).submitQaContent(eq(lessonId), eq(2), any(), any());
    }
}
