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
 * Regression and positive-error tests for QA accuracy (Phase 1B.2).
 *
 * Regression: "Magst du Tee?" must NOT be flagged as an ERROR.
 *   Rationale: the Food & Drinks lesson grammar section explicitly lists
 *   "Magst du Tee?" as an example, proving the correctAnswer is taught.
 *   A QA agent that flags it is producing a false positive.
 *
 * Positive-error tests ensure genuine errors (wrong answer not in lesson) still
 * reach ERROR severity so the revision loop continues to function.
 */
@ExtendWith(MockitoExtension.class)
class QaAccuracyRegressionTest {

    @Mock PromptRegistry promptRegistry;
    @Mock ModelRouter modelRouter;
    @Mock LlmProviderRegistry providerRegistry;
    @Mock LlmProvider mockProvider;

    ExerciseQaAgent exerciseAgent;
    QaAggregator    aggregator;

    private final UUID lessonId = UUID.randomUUID();
    private final UUID promptId = UUID.randomUUID();
    private final UUID runId    = UUID.randomUUID();

    @BeforeEach
    void setup() {
        exerciseAgent = new ExerciseQaAgent(promptRegistry, modelRouter, providerRegistry);
        aggregator    = new QaAggregator();

        PromptRegistry.ResolvedPrompt prompt = new PromptRegistry.ResolvedPrompt(
                promptId, "language.german.a1.qa.exercise", 2,
                "You are a language learning exercise designer...",
                "Evaluate: {{lessonJson}}");

        ModelConfig modelConfig = new ModelConfig(
                "haiku_qa_v1", "mock", "mock-haiku", 0.1, 1024, 5000,
                BigDecimal.ZERO, BigDecimal.ZERO);

        lenient().when(promptRegistry.resolve(any())).thenReturn(prompt);
        lenient().when(modelRouter.resolve(any(), any(), any())).thenReturn(modelConfig);
        lenient().when(providerRegistry.get("mock")).thenReturn(mockProvider);
    }

    // ── Regression: Magst du Tee? must NOT cause FAIL ──────────────────────

    /**
     * Simulates the false-positive from Phase 1B.1: exercise_qa flagged
     * "Magst du Tee?" as an ERROR even though the lesson grammar section
     * explicitly lists it as an example.
     *
     * With Phase 1B.2: the same LLM response now carries no "evidence" field,
     * so QaAggregator downgrades the ERROR to WARNING → result is PASS_WITH_WARNINGS,
     * not FAIL.
     */
    @Test
    void regression_magtsDuTee_withoutEvidence_doesNotCauseFail() {
        // LLM returns an ERROR for "Magst du Tee?" but provides NO evidence
        String llmErrorNoEvidence = """
                {
                  "overallAssessment": "Exercise 4 may have an issue.",
                  "exerciseCount": 5,
                  "issues": [
                    {
                      "severity": "ERROR",
                      "field": "exercises[3].correctAnswer",
                      "message": "The correct answer 'Magst du Tee?' contradicts the lesson grammar.",
                      "evidence": "",
                      "suggestion": "Clarify whether testing preference or request."
                    }
                  ],
                  "recommendations": []
                }
                """;
        when(mockProvider.generate(any()))
                .thenReturn(new LlmResponse(llmErrorNoEvidence, 200, 150, "mock-haiku", "mock"));

        AgentInput<QaInput> input = buildInput("{\"title\":\"Food and Drinks\"}");
        AgentOutput<QaResult> output = exerciseAgent.execute(input);

        // The issue is parsed as ERROR severity
        assertTrue(output.issues().stream().anyMatch(Issue::isError), "ERROR should be present in output");

        // But QaAggregator downgrades it because evidence is empty
        QaAggregator.Decision decision = aggregator.aggregate(List.of(output));
        assertNotEquals(QaAggregator.Decision.FAIL, decision,
                "Unsupported ERROR must not cause FAIL; expected PASS_WITH_WARNINGS but got " + decision);
        assertEquals(QaAggregator.Decision.PASS_WITH_WARNINGS, decision,
                "Unsupported ERROR should be downgraded to WARNING → PASS_WITH_WARNINGS");
    }

    /**
     * When the LLM provides evidence that genuinely contradicts the lesson,
     * the ERROR must survive and cause FAIL.
     */
    @Test
    void positiveError_errorWithEvidence_causesFail() {
        // LLM returns an ERROR with actual lesson text as evidence
        String llmErrorWithEvidence = """
                {
                  "overallAssessment": "Exercise 2 has a wrong answer.",
                  "exerciseCount": 5,
                  "issues": [
                    {
                      "severity": "ERROR",
                      "field": "exercises[1].correctAnswer",
                      "message": "correctAnswer 'ich sein' is not grammatically correct German.",
                      "evidence": "The lesson grammar section shows: \\"Ich bin\\" not \\"Ich sein\\".",
                      "suggestion": "Change correctAnswer to 'Ich bin'."
                    }
                  ],
                  "recommendations": []
                }
                """;
        when(mockProvider.generate(any()))
                .thenReturn(new LlmResponse(llmErrorWithEvidence, 200, 150, "mock-haiku", "mock"));

        AgentInput<QaInput> input = buildInput("{\"title\":\"Greetings\"}");
        AgentOutput<QaResult> output = exerciseAgent.execute(input);

        assertTrue(output.issues().stream().anyMatch(Issue::isError), "ERROR must be present");

        // Evidence is non-blank → ERROR is supported → FAIL is correct
        QaAggregator.Decision decision = aggregator.aggregate(List.of(output));
        assertEquals(QaAggregator.Decision.FAIL, decision,
                "Supported ERROR (with evidence) must still cause FAIL");
    }

    /**
     * QaAggregator.effectiveSeverity: ERROR with blank evidence → WARNING.
     */
    @Test
    void aggregator_unsupportedError_isDowngradedToWarning() {
        Issue unsupported = new Issue("E1", Issue.Severity.ERROR, "field", "msg", null, false, "");
        assertEquals(Issue.Severity.WARNING, aggregator.effectiveSeverity(unsupported),
                "Blank evidence must downgrade ERROR to WARNING");
    }

    /**
     * QaAggregator.effectiveSeverity: ERROR with real evidence keeps severity.
     */
    @Test
    void aggregator_supportedError_keepsErrorSeverity() {
        Issue supported = new Issue("E1", Issue.Severity.ERROR, "field", "msg", null, false, "lesson text here");
        assertEquals(Issue.Severity.ERROR, aggregator.effectiveSeverity(supported),
                "Non-blank evidence must keep ERROR severity");
    }

    /**
     * QaAggregator.effectiveSeverity: null evidence (field absent from LLM response) → WARNING.
     */
    @Test
    void aggregator_nullEvidence_isDowngradedToWarning() {
        Issue nullEvidence = new Issue("E1", Issue.Severity.ERROR, "field", "msg", null, false, null);
        assertEquals(Issue.Severity.WARNING, aggregator.effectiveSeverity(nullEvidence));
    }

    /**
     * All-pass scenario: no issues at all → PASS (unchanged baseline).
     */
    @Test
    void aggregator_noIssues_returnsPass() {
        when(mockProvider.generate(any()))
                .thenReturn(new LlmResponse(MockLlmProvider.MOCK_QA_RESPONSE, 100, 80, "mock-haiku", "mock"));

        AgentInput<QaInput> input = buildInput("{\"title\":\"Greetings\"}");
        AgentOutput<QaResult> output = exerciseAgent.execute(input);

        QaAggregator.Decision decision = aggregator.aggregate(List.of(output));
        assertEquals(QaAggregator.Decision.PASS, decision);
    }

    /**
     * Warning-only issues → PASS_WITH_WARNINGS (unchanged baseline).
     */
    @Test
    void aggregator_warningOnly_returnsPassWithWarnings() {
        Issue warning = new Issue("W1", Issue.Severity.WARNING, "f", "w", null, false, null);
        AgentOutput<QaResult> output = new AgentOutput<>(
                UUID.randomUUID(), AgentOutput.Status.SUCCEEDED,
                new QaResult("ok", List.of(warning), List.of()),
                0.9, List.of(warning), List.of(), false, null);

        assertEquals(QaAggregator.Decision.PASS_WITH_WARNINGS, aggregator.aggregate(List.of(output)));
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private AgentInput<QaInput> buildInput(String lessonJson) {
        return new AgentInput<>(
                runId, lessonId, 1, "hash-abc",
                new QaInput(lessonJson),
                new AgentInput.AgentContext("language", "de", "A1", "de-a1-bench-food-drinks",
                        0, 3, promptId, 2, "haiku_qa_v1")
        );
    }
}
