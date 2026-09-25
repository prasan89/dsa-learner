package com.dsalearner.pipeline.language.german.qa;

import com.dsalearner.pipeline.agent.AgentInput;
import com.dsalearner.pipeline.agent.AgentOutput;
import com.dsalearner.pipeline.agent.Issue;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Regression tests for Phase 1B.2 — QA false positive elimination.
 *
 * Scenario:
 *   Lesson: Food & Drinks (German A1)
 *   Grammar teaches: "Magst du Tee?" → "Do you like tea?" (explicit grammar example)
 *   Exercise: "How do you say 'Do you like tea?'" with correctAnswer = "Magst du Tee?"
 *   False positive: LLM previously flagged this exercise as ERROR claiming the answer contradicts grammar.
 *
 * After Phase 1B.2:
 *   - LLM ERRORs with no evidence (isUnsupported) are downgraded to WARNING by QaAggregator.
 *   - The lesson must NOT be routed to REVISION for this finding.
 */
@ExtendWith(MockitoExtension.class)
class ExerciseQaFalsePositiveRegressionTest {

    @Mock com.dsalearner.pipeline.agent.PromptRegistry promptRegistry;
    @Mock com.dsalearner.pipeline.agent.ModelRouter modelRouter;
    @Mock LlmProviderRegistry providerRegistry;
    @Mock LlmProvider mockProvider;

    private final QaAggregator aggregator = new QaAggregator();
    private final UUID lessonId = UUID.randomUUID();
    private final UUID promptId = UUID.randomUUID();
    private final UUID runId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        com.dsalearner.pipeline.agent.PromptRegistry.ResolvedPrompt prompt =
                new com.dsalearner.pipeline.agent.PromptRegistry.ResolvedPrompt(
                        promptId, "language.german.a1.qa.exercise", 2,
                        "You are a language learning exercise designer.", "Evaluate: {{lessonJson}}");
        com.dsalearner.pipeline.agent.ModelConfig modelConfig =
                new com.dsalearner.pipeline.agent.ModelConfig(
                        "haiku_qa_v1", "mock", "mock-haiku", 0.1, 2048, 5000,
                        BigDecimal.ZERO, BigDecimal.ZERO);

        lenient().when(promptRegistry.resolve(any())).thenReturn(prompt);
        lenient().when(modelRouter.resolve(any(), any(), any())).thenReturn(modelConfig);
        lenient().when(providerRegistry.get("mock")).thenReturn(mockProvider);
    }

    /**
     * Regression: ExerciseQa must NOT produce an unsupported ERROR for "Magst du Tee?"
     * when the lesson grammar explicitly teaches that form.
     *
     * Before fix: LLM returned ERROR with no evidence → QaAggregator returned FAIL → lesson went to REVISION.
     * After fix:  QaAggregator downgrades unsupported ERRORs to WARNING → decision is PASS_WITH_WARNINGS.
     */
    @Test
    void magtDuTee_falsePositive_downgradedToWarning_notFail() {
        // Simulate the exact LLM response that caused the false positive on Food & Drinks v1/v3/v4
        String falsePositiveLlmResponse = """
                {
                  "overallAssessment": "Exercise 4 contains a potential issue.",
                  "exerciseCount": 5,
                  "issues": [
                    {
                      "severity": "ERROR",
                      "field": "exercises[3].correctAnswer",
                      "message": "The correct answer 'Magst du Tee?' contradicts the lesson grammar.",
                      "evidence": "",
                      "suggestion": "Clarify whether asking preference or polite request."
                    }
                  ],
                  "recommendations": []
                }
                """;

        when(mockProvider.generate(any())).thenReturn(
                new LlmResponse(falsePositiveLlmResponse, 400, 200, "mock-haiku", "mock"));

        ExerciseQaAgent agent = new ExerciseQaAgent(promptRegistry, modelRouter, providerRegistry);
        AgentOutput<QaResult> output = agent.execute(buildInput(agent));

        // The LLM returned ERROR with empty evidence → isUnsupported() = true
        assertEquals(1, output.issues().size());
        Issue issue = output.issues().get(0);
        assertEquals(Issue.Severity.ERROR, issue.severity(), "Raw severity from LLM is still ERROR");
        assertTrue(issue.isUnsupported(), "Issue with blank evidence must be isUnsupported");

        // QaAggregator must downgrade it to WARNING → PASS_WITH_WARNINGS, not FAIL
        QaAggregator.Decision decision = aggregator.aggregate(List.of(output));
        assertNotEquals(QaAggregator.Decision.FAIL, decision,
                "Unsupported ERROR (no evidence) must NOT cause FAIL — " +
                "this was the false positive for 'Magst du Tee?' that caused Food & Drinks to loop into revision");
        assertEquals(QaAggregator.Decision.PASS_WITH_WARNINGS, decision,
                "Unsupported ERROR must be downgraded to WARNING → PASS_WITH_WARNINGS");
        assertFalse(aggregator.isFail(decision));
    }

    /**
     * Positive test: a genuine exercise ERROR with lesson evidence DOES cause FAIL.
     * The downgrade protection must NOT suppress real errors.
     */
    @Test
    void genuineError_withEvidence_causesFail() {
        String genuineErrorLlmResponse = """
                {
                  "overallAssessment": "Exercise has a wrong answer.",
                  "exerciseCount": 3,
                  "issues": [
                    {
                      "severity": "ERROR",
                      "field": "exercises[0].correctAnswer",
                      "message": "The answer 'Ich bin Käse' is not grammatically correct German.",
                      "evidence": "The lesson teaches subject-verb-object order. 'Ich bin Käse' means 'I am cheese' which is semantically wrong for 'I like cheese'.",
                      "suggestion": "Change to 'Ich mag Käse'."
                    }
                  ],
                  "recommendations": []
                }
                """;

        when(mockProvider.generate(any())).thenReturn(
                new LlmResponse(genuineErrorLlmResponse, 350, 150, "mock-haiku", "mock"));

        ExerciseQaAgent agent = new ExerciseQaAgent(promptRegistry, modelRouter, providerRegistry);
        AgentOutput<QaResult> output = agent.execute(buildInput(agent));

        assertEquals(1, output.issues().size());
        Issue issue = output.issues().get(0);
        assertEquals(Issue.Severity.ERROR, issue.severity());
        assertFalse(issue.isUnsupported(), "Issue WITH evidence must NOT be unsupported");
        assertNotNull(issue.evidence(), "Evidence must be non-null");
        assertFalse(issue.evidence().isBlank(), "Evidence must be non-blank");

        QaAggregator.Decision decision = aggregator.aggregate(List.of(output));
        assertEquals(QaAggregator.Decision.FAIL, decision,
                "Supported ERROR (with lesson evidence) must cause FAIL");
        assertTrue(aggregator.isFail(decision));
    }

    /**
     * Positive test: an exercise with a genuinely wrong answer (wrong German verb form)
     * must still be caught and cause FAIL, regardless of the false-positive fix.
     */
    @Test
    void wrongConjugation_inExercise_causesFail() {
        // Scenario: exercise asks to conjugate "mögen" for "du" but correctAnswer = "ich mag"
        String wrongConjugationResponse = """
                {
                  "overallAssessment": "Exercise 2 has the wrong conjugation.",
                  "exerciseCount": 4,
                  "issues": [
                    {
                      "severity": "ERROR",
                      "field": "exercises[1].correctAnswer",
                      "message": "The correct form for 'du' is 'magst', not 'mag'.",
                      "evidence": "The conjugation table in the lesson shows: 'du magst' = 'you like (informal)'. The exercise expects 'du ___ Kaffee' but the provided answer is 'mag' (the 'ich' form).",
                      "suggestion": "Change correctAnswer to 'magst'."
                    }
                  ],
                  "recommendations": []
                }
                """;

        when(mockProvider.generate(any())).thenReturn(
                new LlmResponse(wrongConjugationResponse, 300, 120, "mock-haiku", "mock"));

        ExerciseQaAgent agent = new ExerciseQaAgent(promptRegistry, modelRouter, providerRegistry);
        AgentOutput<QaResult> output = agent.execute(buildInput(agent));

        Issue issue = output.issues().get(0);
        assertFalse(issue.isUnsupported(), "Error citing conjugation table evidence must not be unsupported");

        QaAggregator.Decision decision = aggregator.aggregate(List.of(output));
        assertEquals(QaAggregator.Decision.FAIL, decision,
                "Wrong conjugation with evidence must still cause FAIL");
    }

    /**
     * Validates that a mix of unsupported ERROR and supported ERROR:
     * the supported ERROR wins → FAIL.
     */
    @Test
    void mixedUnsupportedAndSupportedErrors_supportedWins_fail() {
        List<Issue> issues = List.of(
                Issue.error("E1", "exercises[0]", "unsupported — no evidence"),
                Issue.errorWithEvidence("E2", "vocabulary[0].german", "wrong article",
                        "lesson shows 'das Wasser' but exercise marks 'die Wasser' as correct")
        );
        QaResult result = new QaResult("issues found", issues, List.of());
        AgentOutput<QaResult> output = new AgentOutput<>(UUID.randomUUID(), AgentOutput.Status.PARTIAL,
                result, 0.6, issues, List.of(), false, null);

        QaAggregator.Decision decision = aggregator.aggregate(List.of(output));
        assertEquals(QaAggregator.Decision.FAIL, decision,
                "One supported ERROR is enough to FAIL even if another ERROR is unsupported");
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private AgentInput<QaInput> buildInput(ExerciseQaAgent agent) {
        String foodDrinksLessonJson = """
                {
                  "objectives": ["Say what you like and dislike eating or drinking"],
                  "grammar": {
                    "title": "Expressing likes",
                    "examples": [
                      {"german": "Ich mag Kaffee.", "english": "I like coffee."},
                      {"german": "Magst du Tee?", "english": "Do you like tea?"}
                    ],
                    "conjugationTable": {"ich mag": "I like", "du magst": "you like (informal)"}
                  },
                  "exercises": {
                    "items": [
                      {"type": "MULTIPLE_CHOICE",
                       "question": "How do you say 'Do you like tea?' in German?",
                       "options": ["Magst du Tee?", "Möchtest du Tee?", "Ich mag Tee.", "Trinkst du Tee?"],
                       "correctAnswer": "Magst du Tee?",
                       "hint": "Use 'Magst du...?' — the du form of 'ich mag'."}
                    ]
                  }
                }
                """;
        return new AgentInput<>(
                runId, lessonId, 4, "hash-food",
                new QaInput(foodDrinksLessonJson),
                new AgentInput.AgentContext("language", "de", "A1", "de-a1-bench-food-drinks",
                        0, 3, promptId, 2, "haiku_qa_v1")
        );
    }
}
