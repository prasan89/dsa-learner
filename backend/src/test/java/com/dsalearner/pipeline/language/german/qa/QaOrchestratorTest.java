package com.dsalearner.pipeline.language.german.qa;

import com.dsalearner.pipeline.agent.*;
import com.dsalearner.pipeline.domain.ContentStatus;
import com.dsalearner.pipeline.model.entity.CfAgentRun;
import com.dsalearner.pipeline.model.entity.CfLesson;
import com.dsalearner.pipeline.model.entity.CfLessonVersion;
import com.dsalearner.pipeline.model.entity.CfPipelineJob;
import com.dsalearner.pipeline.repository.CfAgentRunRepository;
import com.dsalearner.pipeline.repository.CfLessonRepository;
import com.dsalearner.pipeline.repository.CfLessonVersionRepository;
import com.dsalearner.pipeline.service.PipelineJobService;
import com.dsalearner.pipeline.statemachine.WorkflowOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QaOrchestratorTest {

    @Mock AgentRunner agentRunner;
    @Mock GermanLanguageQaAgent languageAgent;
    @Mock CefrQaAgent cefrAgent;
    @Mock ExerciseQaAgent exerciseAgent;
    @Mock PedagogyQaAgent pedagogyAgent;
    @Mock QaAggregator qaAggregator;
    @Mock PromptRegistry promptRegistry;
    @Mock ModelRouter modelRouter;
    @Mock WorkflowOrchestrator workflowOrchestrator;
    @Mock CfLessonRepository lessonRepository;
    @Mock CfLessonVersionRepository lessonVersionRepository;
    @Mock CfAgentRunRepository agentRunRepository;
    @Mock PipelineJobService pipelineJobService;

    QaOrchestrator orchestrator;

    private final UUID lessonId = UUID.randomUUID();
    private final UUID promptId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        orchestrator = new QaOrchestrator(
                agentRunner, languageAgent, cefrAgent, exerciseAgent, pedagogyAgent,
                qaAggregator, promptRegistry, modelRouter, workflowOrchestrator,
                lessonRepository, lessonVersionRepository, agentRunRepository, pipelineJobService);

        lenient().when(languageAgent.agentType()).thenReturn(AgentType.LINGUISTIC_QA);
        lenient().when(cefrAgent.agentType()).thenReturn(AgentType.CEFR_QA);
        lenient().when(exerciseAgent.agentType()).thenReturn(AgentType.EXERCISE_QA);
        lenient().when(pedagogyAgent.agentType()).thenReturn(AgentType.PEDAGOGY_QA);

        PromptRegistry.ResolvedPrompt prompt = new PromptRegistry.ResolvedPrompt(
                promptId, "language.german.a1.qa.language", 1, "system qa", "eval {{lessonJson}}");
        lenient().when(promptRegistry.resolve(any())).thenReturn(prompt);

        ModelConfig modelConfig = new ModelConfig(
                "haiku_qa_v1", "mock", "mock-haiku", 0.1, 1024, 5000,
                BigDecimal.ZERO, BigDecimal.ZERO);
        lenient().when(modelRouter.resolve(any(), any(), any())).thenReturn(modelConfig);
    }

    private CfLesson buildLesson() {
        CfLesson lesson = new CfLesson();
        lesson.setId(lessonId);
        lesson.setDomainCode("language");
        lesson.setLanguageCode("de");
        lesson.setContentStatus(ContentStatus.QA_PENDING);
        lesson.setRevisionCount(0);
        lesson.setMaxRevisionAttempts(3);
        lesson.setCurrentVersion(1);
        return lesson;
    }

    private CfLessonVersion buildVersion() {
        CfLessonVersion lv = new CfLessonVersion();
        lv.setLessonId(lessonId);
        lv.setVersion(1);
        lv.setContent(Map.of("objectives", List.of("Learn greetings")));
        lv.setVocabulary(Map.of("items", List.of()));
        lv.setGrammar(Map.of("title", "sein"));
        lv.setExercises(Map.of("items", List.of()));
        lv.setBlueprint(Map.of("topic", "Greetings"));
        return lv;
    }

    private CfPipelineJob buildJob() {
        CfPipelineJob job = new CfPipelineJob();
        job.setId(UUID.randomUUID());
        job.setLessonId(lessonId);
        job.setLessonVersion(1);
        job.setJobType("QA_CONTENT");
        job.setPayload(Map.of());
        return job;
    }

    private AgentOutput<QaResult> passOutput(Agent<QaInput, QaResult> agent) {
        QaResult result = new QaResult("Good lesson.", List.of(), List.of("Minor suggestion"));
        return new AgentOutput<>(UUID.randomUUID(), AgentOutput.Status.SUCCEEDED,
                result, 0.95, List.of(), List.of(), false,
                new AgentOutput.AgentMetadata("haiku_qa_v1", "mock-haiku", "mock",
                        100, 80, 0.0, 500, promptId, 1, "1.0"));
    }

    private void stubAllAgentsPass() {
        when(agentRunner.run(same(languageAgent), any(), anyInt(), any(), any(), any(), any(), anyInt(), any()))
                .thenReturn(passOutput(languageAgent));
        when(agentRunner.run(same(cefrAgent), any(), anyInt(), any(), any(), any(), any(), anyInt(), any()))
                .thenReturn(passOutput(cefrAgent));
        when(agentRunner.run(same(exerciseAgent), any(), anyInt(), any(), any(), any(), any(), anyInt(), any()))
                .thenReturn(passOutput(exerciseAgent));
        when(agentRunner.run(same(pedagogyAgent), any(), anyInt(), any(), any(), any(), any(), anyInt(), any()))
                .thenReturn(passOutput(pedagogyAgent));
        when(agentRunRepository.findById(any())).thenReturn(Optional.of(new CfAgentRun()));
    }

    @Test
    void qaPass_transitionsToQaPassed() {
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(buildLesson()));
        when(lessonVersionRepository.findByLessonIdAndVersion(lessonId, 1))
                .thenReturn(Optional.of(buildVersion()));
        stubAllAgentsPass();
        when(qaAggregator.aggregate(any())).thenReturn(QaAggregator.Decision.PASS);
        when(qaAggregator.isFail(QaAggregator.Decision.PASS)).thenReturn(false);

        String result = orchestrator.execute(buildJob());

        assertNotNull(result);
        verify(workflowOrchestrator).applyContentTransition(
                eq(lessonId), eq(ContentStatus.QA_PASSED), eq("QA_PASSED"), any(), any(), any());
        verify(workflowOrchestrator, never()).applyContentTransition(
                eq(lessonId), eq(ContentStatus.QA_FAILED), any(), any(), any(), any());
    }

    @Test
    void qaPassWithWarnings_transitionsToQaPassed() {
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(buildLesson()));
        when(lessonVersionRepository.findByLessonIdAndVersion(lessonId, 1))
                .thenReturn(Optional.of(buildVersion()));
        stubAllAgentsPass();
        when(qaAggregator.aggregate(any())).thenReturn(QaAggregator.Decision.PASS_WITH_WARNINGS);
        when(qaAggregator.isFail(QaAggregator.Decision.PASS_WITH_WARNINGS)).thenReturn(false);

        orchestrator.execute(buildJob());

        verify(workflowOrchestrator).applyContentTransition(
                eq(lessonId), eq(ContentStatus.QA_PASSED), any(), any(), any(), any());
    }

    @Test
    void qaFail_withinRevisionLimit_transitionsToRevisionAndEnqueuesJob() {
        CfLesson lesson = buildLesson();
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonVersionRepository.findByLessonIdAndVersion(lessonId, 1))
                .thenReturn(Optional.of(buildVersion()));
        stubAllAgentsPass();
        when(qaAggregator.aggregate(any())).thenReturn(QaAggregator.Decision.FAIL);
        when(qaAggregator.isFail(QaAggregator.Decision.FAIL)).thenReturn(true);
        when(workflowOrchestrator.incrementRevisionCount(eq(lessonId), any())).thenReturn(false);

        orchestrator.execute(buildJob());

        verify(workflowOrchestrator).applyContentTransition(
                eq(lessonId), eq(ContentStatus.QA_FAILED), any(), any(), any(), any());
        verify(workflowOrchestrator).applyContentTransition(
                eq(lessonId), eq(ContentStatus.REVISION), any(), any(), any(), any());
        verify(workflowOrchestrator, never()).applyContentTransition(
                eq(lessonId), eq(ContentStatus.HUMAN_REVIEW_REQUIRED), any(), any(), any(), any());
        // Auto-enqueue: REVISION_GENERATION job must be submitted automatically
        verify(pipelineJobService).submitRevisionGeneration(eq(lessonId), anyInt(), any(), any());
    }

    @Test
    void qaFail_atRevisionLimit_escalatesToHumanReview() {
        CfLesson lesson = buildLesson();
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonVersionRepository.findByLessonIdAndVersion(lessonId, 1))
                .thenReturn(Optional.of(buildVersion()));
        stubAllAgentsPass();
        when(qaAggregator.aggregate(any())).thenReturn(QaAggregator.Decision.FAIL);
        when(qaAggregator.isFail(QaAggregator.Decision.FAIL)).thenReturn(true);
        when(workflowOrchestrator.incrementRevisionCount(eq(lessonId), any())).thenReturn(true);

        orchestrator.execute(buildJob());

        verify(workflowOrchestrator).applyContentTransition(
                eq(lessonId), eq(ContentStatus.QA_FAILED), any(), any(), any(), any());
        verify(workflowOrchestrator).applyContentTransition(
                eq(lessonId), eq(ContentStatus.HUMAN_REVIEW_REQUIRED), any(), any(), any(), any());
        verify(workflowOrchestrator, never()).applyContentTransition(
                eq(lessonId), eq(ContentStatus.REVISION), any(), any(), any(), any());
        // No revision job when escalating to HUMAN_REVIEW_REQUIRED
        verify(pipelineJobService, never()).submitRevisionGeneration(any(), anyInt(), any(), any());
    }

    @Test
    void allFourAgentsAreInvoked() {
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(buildLesson()));
        when(lessonVersionRepository.findByLessonIdAndVersion(lessonId, 1))
                .thenReturn(Optional.of(buildVersion()));
        stubAllAgentsPass();
        when(qaAggregator.aggregate(any())).thenReturn(QaAggregator.Decision.PASS);
        when(qaAggregator.isFail(any())).thenReturn(false);

        orchestrator.execute(buildJob());

        verify(agentRunner).run(same(languageAgent), any(), anyInt(), any(), any(), any(), any(), anyInt(), any());
        verify(agentRunner).run(same(cefrAgent), any(), anyInt(), any(), any(), any(), any(), anyInt(), any());
        verify(agentRunner).run(same(exerciseAgent), any(), anyInt(), any(), any(), any(), any(), anyInt(), any());
        verify(agentRunner).run(same(pedagogyAgent), any(), anyInt(), any(), any(), any(), any(), anyInt(), any());
    }

    @Test
    void resultContainsFourRunIds() {
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(buildLesson()));
        when(lessonVersionRepository.findByLessonIdAndVersion(lessonId, 1))
                .thenReturn(Optional.of(buildVersion()));
        stubAllAgentsPass();
        when(qaAggregator.aggregate(any())).thenReturn(QaAggregator.Decision.PASS);
        when(qaAggregator.isFail(any())).thenReturn(false);

        String resultRef = orchestrator.execute(buildJob());

        // Result is 4 UUIDs joined by commas
        String[] parts = resultRef.split(",");
        assertEquals(4, parts.length);
        for (String part : parts) {
            assertDoesNotThrow(() -> UUID.fromString(part.trim()));
        }
    }
}
