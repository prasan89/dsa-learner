package com.dsalearner.pipeline.language.german.qa;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for ExerciseAnswerCrossValidator. */
class ExerciseAnswerCrossValidatorTest {

    private final ExerciseAnswerCrossValidator validator = new ExerciseAnswerCrossValidator();

    // ── Taught-forms collection ──────────────────────────────────────────────

    @Test
    void grammarExamples_addedToTaughtForms() {
        Map<String, Object> lesson = Map.of(
                "grammar", Map.of("examples", List.of(
                        Map.of("german", "Ich mag Kaffee.", "english", "I like coffee."),
                        Map.of("german", "Magst du Tee?", "english", "Do you like tea?")
                )),
                "exercises", Map.of("items", List.of())
        );
        var result = validator.validate(lesson);
        assertTrue(result.taughtForms().contains("ich mag kaffee."), "Grammar example must be in taughtForms");
        assertTrue(result.taughtForms().contains("magst du tee?"), "Grammar example must be in taughtForms");
    }

    @Test
    void conjugationTable_keysAddedToTaughtForms() {
        Map<String, Object> lesson = Map.of(
                "grammar", Map.of("conjugationTable",
                        Map.of("ich mag", "I like", "du magst", "you like")),
                "exercises", Map.of("items", List.of())
        );
        var result = validator.validate(lesson);
        assertTrue(result.taughtForms().contains("ich mag"));
        assertTrue(result.taughtForms().contains("du magst"));
    }

    @Test
    void vocabulary_germanForms_addedToTaughtForms() {
        Map<String, Object> lesson = Map.of(
                "vocabulary", Map.of("items", List.of(
                        Map.of("german", "der Tee", "english", "the tea"),
                        Map.of("german", "der Kaffee", "english", "the coffee",
                                "example", "Ich mag Kaffee.")
                )),
                "exercises", Map.of("items", List.of())
        );
        var result = validator.validate(lesson);
        assertTrue(result.taughtForms().contains("der tee"));
        assertTrue(result.taughtForms().contains("der kaffee"));
        assertTrue(result.taughtForms().contains("ich mag kaffee."));
    }

    // ── Direct-match clearing ────────────────────────────────────────────────

    @Test
    void exercise_directMatchInGrammarExample_isCleared() {
        Map<String, Object> lesson = Map.of(
                "grammar", Map.of("examples", List.of(
                        Map.of("german", "Magst du Tee?", "english", "Do you like tea?")
                )),
                "exercises", Map.of("items", List.of(
                        Map.of("correctAnswer", "Magst du Tee?",
                               "question", "How do you say 'Do you like tea?'")
                ))
        );
        var result = validator.validate(lesson);
        assertTrue(result.isCleared(0, "Magst du Tee?"),
                "'Magst du Tee?' must be cleared because the grammar examples explicitly teach it");
    }

    @Test
    void exercise_notInAnyTaughtForm_isNotCleared() {
        Map<String, Object> lesson = Map.of(
                "grammar", Map.of("examples", List.of(
                        Map.of("german", "Ich mag Kaffee.", "english", "I like coffee.")
                )),
                "exercises", Map.of("items", List.of(
                        Map.of("correctAnswer", "Ich bin Käse.",
                               "question", "Say 'I am cheese'")
                ))
        );
        var result = validator.validate(lesson);
        assertFalse(result.isCleared(0, "Ich bin Käse."),
                "An answer with no corresponding taught form must NOT be cleared");
    }

    // ── Whole-word subword matching ──────────────────────────────────────────

    @Test
    void exercise_singleWordAnswer_wholeWordMatch_isCleared() {
        // Fill-in-blank: "Ich ___ Kaffee" → correctAnswer = "mag"
        // "mag" appears as a whole word inside the grammar example "ich mag kaffee."
        Map<String, Object> lesson = Map.of(
                "grammar", Map.of("examples", List.of(
                        Map.of("german", "Ich mag Kaffee.", "english", "I like coffee.")
                )),
                "exercises", Map.of("items", List.of(
                        Map.of("correctAnswer", "mag", "question", "Fill in: Ich ___ Kaffee.")
                ))
        );
        var result = validator.validate(lesson);
        assertTrue(result.isCleared(0, "mag"),
                "'mag' as a whole word inside a grammar example must be cleared");
    }

    @Test
    void exercise_partialSubstringOnly_notCleared() {
        // "mögen" is a substring of "mag" → should NOT match because "mag" is not in "mögen"
        // Inverse: "kaf" should NOT match as a whole word in "Kaffee"
        Map<String, Object> lesson = Map.of(
                "grammar", Map.of("examples", List.of(
                        Map.of("german", "Ich mag Kaffee.", "english", "I like coffee.")
                )),
                "exercises", Map.of("items", List.of(
                        Map.of("correctAnswer", "Kaff", "question", "Fill in first part")
                ))
        );
        var result = validator.validate(lesson);
        assertFalse(result.isCleared(0, "Kaff"),
                "'Kaff' is only a partial substring — not a whole word — must NOT be cleared");
    }

    // ── Index correctness ────────────────────────────────────────────────────

    @Test
    void clearedIndex_isExerciseSpecific() {
        // Exercise[0] has a taught answer; exercise[1] does not
        Map<String, Object> lesson = Map.of(
                "grammar", Map.of("examples", List.of(
                        Map.of("german", "Magst du Tee?", "english", "Do you like tea?")
                )),
                "exercises", Map.of("items", List.of(
                        Map.of("correctAnswer", "Magst du Tee?"),
                        Map.of("correctAnswer", "Ich schwimme gern.")  // not taught
                ))
        );
        var result = validator.validate(lesson);
        assertTrue(result.isCleared(0, "Magst du Tee?"), "exercises[0] cleared");
        assertFalse(result.isCleared(1, "Ich schwimme gern."), "exercises[1] not cleared");
        assertFalse(result.isCleared(0, "Ich schwimme gern."),
                "Cleared answer at wrong index must not match");
    }

    // ── Edge cases ───────────────────────────────────────────────────────────

    @Test
    void emptyLesson_returnsEmptyResult() {
        var result = validator.validate(Map.of());
        assertTrue(result.taughtForms().isEmpty());
        assertTrue(result.clearedAnswers().isEmpty());
    }

    @Test
    void exercisesAsList_stillWorks() {
        // Some lessons may store exercises directly as a list rather than {"items": [...]}
        Map<String, Object> lesson = Map.of(
                "grammar", Map.of("examples", List.of(
                        Map.of("german", "Ich mag Tee.", "english", "I like tea.")
                )),
                "exercises", List.of(
                        Map.of("correctAnswer", "Ich mag Tee.", "question", "Translate")
                )
        );
        var result = validator.validate(lesson);
        assertTrue(result.isCleared(0, "Ich mag Tee."),
                "Exercises stored as a bare list (not wrapped in 'items') must still be validated");
    }

    @Test
    void normalisationIsCaseInsensitive() {
        // Grammar stores "Magst du Tee?" but exercise correctAnswer has different capitalisation
        Map<String, Object> lesson = Map.of(
                "grammar", Map.of("examples", List.of(
                        Map.of("german", "Magst du Tee?", "english", "Do you like tea?")
                )),
                "exercises", Map.of("items", List.of(
                        Map.of("correctAnswer", "magst du tee?")
                ))
        );
        var result = validator.validate(lesson);
        assertTrue(result.isCleared(0, "magst du tee?"),
                "Normalisation must treat capitalisation differences as the same answer");
    }

    // ── QaAggregator integration with cross-validation ───────────────────────

    @Test
    void aggregator_clearedAnswer_withLlmError_downgradesEvenWithEvidence() {
        // Cross-validation clears exercises[0] correctAnswer = "Magst du Tee?"
        // LLM returns an ERROR WITH evidence — but cross-validation says it's taught
        // The cross-validation layer downgrades to WARNING (third protection layer)
        Map<String, Object> lesson = Map.of(
                "grammar", Map.of("examples", List.of(
                        Map.of("german", "Magst du Tee?", "english", "Do you like tea?")
                )),
                "exercises", Map.of("items", List.of(
                        Map.of("correctAnswer", "Magst du Tee?")
                ))
        );
        var crossValidation = validator.validate(lesson);
        assertTrue(crossValidation.isCleared(0, "Magst du Tee?"));

        // LLM error that cites evidence but still incorrectly flags a taught answer
        com.dsalearner.pipeline.agent.Issue errorWithEvidence =
                com.dsalearner.pipeline.agent.Issue.errorWithEvidence(
                        "EX_AMBIGUOUS",
                        "exercises[0].correctAnswer",
                        "The answer 'Magst du Tee?' is ambiguous — 'Möchtest du Tee?' would be preferred.",
                        "Grammar section shows 'mögen' conjugation.");

        QaAggregator aggregator = new QaAggregator();

        // Without cross-validation: this is a supported ERROR → FAIL
        com.dsalearner.pipeline.agent.AgentOutput<QaResult> output =
                new com.dsalearner.pipeline.agent.AgentOutput<>(
                        java.util.UUID.randomUUID(),
                        com.dsalearner.pipeline.agent.AgentOutput.Status.PARTIAL,
                        new QaResult("issues", List.of(errorWithEvidence), List.of()),
                        0.5, List.of(errorWithEvidence), List.of(), false, null);

        assertEquals(QaAggregator.Decision.FAIL, aggregator.aggregate(List.of(output)),
                "Without cross-validation, a supported ERROR causes FAIL");

        // With cross-validation: the cleared answer makes the cross-validator downgrade to WARNING → PASS_WITH_WARNINGS
        assertEquals(QaAggregator.Decision.PASS_WITH_WARNINGS,
                aggregator.aggregate(List.of(output), crossValidation),
                "With cross-validation, flagging a taught answer is downgraded to WARNING");
    }
}
