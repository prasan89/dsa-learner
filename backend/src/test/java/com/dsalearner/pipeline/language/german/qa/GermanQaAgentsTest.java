package com.dsalearner.pipeline.language.german.qa;

import com.dsalearner.pipeline.agent.*;
import com.dsalearner.pipeline.provider.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for all four German QA agents via the shared BaseGermanQaAgent logic.
 * MockLlmProvider.MOCK_QA_RESPONSE returns an empty issues array → SUCCEEDED with no errors.
 */
@ExtendWith(MockitoExtension.class)
class GermanQaAgentsTest {

    @Mock PromptRegistry promptRegistry;
    @Mock ModelRouter modelRouter;
    @Mock LlmProviderRegistry providerRegistry;
    @Mock LlmProvider mockProvider;

    GermanLanguageQaAgent languageAgent;
    CefrQaAgent           cefrAgent;
    ExerciseQaAgent       exerciseAgent;
    PedagogyQaAgent       pedagogyAgent;

    private final UUID lessonId = UUID.randomUUID();
    private final UUID promptId = UUID.randomUUID();
    private final UUID runId    = UUID.randomUUID();

    @BeforeEach
    void setup() {
        languageAgent = new GermanLanguageQaAgent(promptRegistry, modelRouter, providerRegistry);
        cefrAgent     = new CefrQaAgent(promptRegistry, modelRouter, providerRegistry);
        exerciseAgent = new ExerciseQaAgent(promptRegistry, modelRouter, providerRegistry);
        pedagogyAgent = new PedagogyQaAgent(promptRegistry, modelRouter, providerRegistry);

        PromptRegistry.ResolvedPrompt prompt = new PromptRegistry.ResolvedPrompt(
                promptId, "language.german.a1.qa.language", 1,
                "You are a German QA expert.",
                "Evaluate: {{lessonJson}}");

        ModelConfig modelConfig = new ModelConfig(
                "haiku_qa_v1", "mock", "mock-haiku", 0.1, 1024, 5000,
                BigDecimal.ZERO, BigDecimal.ZERO);

        lenient().when(promptRegistry.resolve(any())).thenReturn(prompt);
        lenient().when(modelRouter.resolve(any(), any(), any())).thenReturn(modelConfig);
        lenient().when(providerRegistry.get("mock")).thenReturn(mockProvider);
        lenient().when(mockProvider.generate(any())).thenReturn(
                new LlmResponse(MockLlmProvider.MOCK_QA_RESPONSE, 100, 80, "mock-haiku", "mock"));
    }

    private AgentInput<QaInput> buildInput(Agent<QaInput, QaResult> agent) {
        return new AgentInput<>(
                runId, lessonId, 1, "hash-abc",
                new QaInput("{\"title\":\"Greetings\"}"),
                new AgentInput.AgentContext("language", "de", "A1", "de-a1-u01",
                        0, 3, promptId, 1, "haiku_qa_v1")
        );
    }

    @Test
    void languageAgent_returnsAgentType() {
        assertEquals(AgentType.LINGUISTIC_QA, languageAgent.agentType());
    }

    @Test
    void cefrAgent_returnsAgentType() {
        assertEquals(AgentType.CEFR_QA, cefrAgent.agentType());
    }

    @Test
    void exerciseAgent_returnsAgentType() {
        assertEquals(AgentType.EXERCISE_QA, exerciseAgent.agentType());
    }

    @Test
    void pedagogyAgent_returnsAgentType() {
        assertEquals(AgentType.PEDAGOGY_QA, pedagogyAgent.agentType());
    }

    @Test
    void languageAgent_executesAndReturnsSucceeded() {
        AgentOutput<QaResult> output = languageAgent.execute(buildInput(languageAgent));

        assertEquals(AgentOutput.Status.SUCCEEDED, output.status());
        assertNotNull(output.output());
        assertFalse(output.output().hasErrors());
        assertTrue(output.issues().isEmpty());
        assertFalse(output.output().recommendations().isEmpty());
    }

    @Test
    void cefrAgent_executesAndReturnsSucceeded() {
        AgentOutput<QaResult> output = cefrAgent.execute(buildInput(cefrAgent));
        assertEquals(AgentOutput.Status.SUCCEEDED, output.status());
        assertFalse(output.output().hasErrors());
    }

    @Test
    void exerciseAgent_executesAndReturnsSucceeded() {
        AgentOutput<QaResult> output = exerciseAgent.execute(buildInput(exerciseAgent));
        assertEquals(AgentOutput.Status.SUCCEEDED, output.status());
        assertFalse(output.output().hasErrors());
    }

    @Test
    void pedagogyAgent_executesAndReturnsSucceeded() {
        AgentOutput<QaResult> output = pedagogyAgent.execute(buildInput(pedagogyAgent));
        assertEquals(AgentOutput.Status.SUCCEEDED, output.status());
        assertFalse(output.output().hasErrors());
    }

    @Test
    void languageAgent_withErrorInResponse_returnsPartial() {
        String responseWithError = """
                {
                  "overallAssessment": "Has grammar errors.",
                  "issues": [
                    {"severity": "ERROR", "field": "vocabulary[0].german", "message": "Wrong article", "suggestion": "Use 'der' instead of 'die'"}
                  ],
                  "recommendations": []
                }
                """;
        when(mockProvider.generate(any())).thenReturn(
                new LlmResponse(responseWithError, 100, 80, "mock-haiku", "mock"));

        AgentOutput<QaResult> output = languageAgent.execute(buildInput(languageAgent));

        assertEquals(AgentOutput.Status.PARTIAL, output.status());
        assertTrue(output.output().hasErrors());
        assertEquals(1, output.issues().size());
        assertEquals(Issue.Severity.ERROR, output.issues().get(0).severity());
    }

    @Test
    void languageAgent_withInvalidJson_throwsIllegalState() {
        when(mockProvider.generate(any())).thenReturn(
                new LlmResponse("not json at all", 10, 5, "mock-haiku", "mock"));

        assertThrows(IllegalStateException.class,
                () -> languageAgent.execute(buildInput(languageAgent)));
    }
}
