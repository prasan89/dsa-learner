package com.dsalearner.pipeline.validation.rules;

import com.dsalearner.pipeline.validation.ValidationIssue;
import com.dsalearner.pipeline.validation.ValidationSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ExerciseCrossValidationRule.
 *
 * Key regression: "Magst du Tee?" in a MULTIPLE_CHOICE exercise must NOT produce
 * an ERROR when the lesson grammar section includes that exact phrase.
 */
class ExerciseCrossValidationRuleTest {

    private ExerciseCrossValidationRule rule;

    @BeforeEach
    void setUp() {
        rule = new ExerciseCrossValidationRule();
    }

    @Test
    void skipsForDsaDomain() {
        Map<String, Object> content = Map.of("exercises", Map.of("items", List.of(
                Map.of("type", "MULTIPLE_CHOICE", "correctAnswer", "null", "options", List.of("null"))
        )));
        List<ValidationIssue> issues = rule.check(content, "dsa", null);
        assertTrue(issues.isEmpty(), "DSA lessons must be skipped entirely");
    }

    @Test
    void noExercisesBlock_returnsEmpty() {
        List<ValidationIssue> issues = rule.check(Map.of(), "language", "de");
        assertTrue(issues.isEmpty());
    }

    /**
     * Regression: "Magst du Tee?" appears in the grammar examples.
     * correctAnswer must be found in the lesson corpus → no ERROR, no WARNING.
     */
    @Test
    void regression_magtsDuTee_isInGrammarExamples_noIssues() {
        Map<String, Object> content = buildFoodLesson();

        List<ValidationIssue> issues = rule.check(content, "language", "de");

        boolean hasError = issues.stream().anyMatch(ValidationIssue::isError);
        assertFalse(hasError,
                "Magst du Tee? is taught in grammar examples — must not produce ERROR; issues=" + issues);
    }

    /**
     * Positive: correctAnswer not in options → ERROR.
     */
    @Test
    void correctAnswerNotInOptions_producesError() {
        Map<String, Object> content = buildContentWithExercise(Map.of(
                "type", "MULTIPLE_CHOICE",
                "question", "What is the capital?",
                "correctAnswer", "Berlin",
                "options", List.of("München", "Hamburg", "Köln")
        ));

        List<ValidationIssue> issues = rule.check(content, "language", "de");

        assertTrue(issues.stream().anyMatch(i ->
                "EXERCISE_CROSS_VALIDATION".equals(i.code()) && i.isError()),
                "Answer not in options must produce ERROR; issues=" + issues);
    }

    /**
     * Positive: correct answer IS in options → no options-mismatch ERROR.
     */
    @Test
    void correctAnswerInOptions_noOptionsMismatchError() {
        Map<String, Object> content = buildContentWithGrammarExample("Ich trinke Wasser.");
        Map<String, Object> lesson = addExercise(content, Map.of(
                "type", "MULTIPLE_CHOICE",
                "question", "Choose the correct sentence",
                "correctAnswer", "Ich trinke Wasser.",
                "options", List.of("Ich trinke Wasser.", "Ich essen Wasser.", "Du trinkt Wasser.")
        ));

        List<ValidationIssue> issues = rule.check(lesson, "language", "de");

        boolean hasOptionsMismatch = issues.stream().anyMatch(i ->
                i.isError() && i.message().contains("not present in the options list"));
        assertFalse(hasOptionsMismatch, "Valid answer in options must not produce options-mismatch ERROR");
    }

    /**
     * correctAnswer not in lesson text → WARNING (not ERROR), leaving final judgment to QA agent.
     */
    @Test
    void correctAnswerNotInLessonText_producesWarning() {
        Map<String, Object> content = buildContentWithGrammarExample("Ich heiße Anna.");
        Map<String, Object> lesson = addExercise(content, Map.of(
                "type", "MULTIPLE_CHOICE",
                "question", "Choose the right form",
                "correctAnswer", "Sie heißt Petra.",
                "options", List.of("Sie heißt Petra.", "Sie heiß Petra.", "Sie heißen Petra.")
        ));

        List<ValidationIssue> issues = rule.check(lesson, "language", "de");

        boolean hasWarning = issues.stream().anyMatch(i ->
                "EXERCISE_CROSS_VALIDATION".equals(i.code())
                && i.severity() == ValidationSeverity.WARNING);
        assertTrue(hasWarning,
                "Answer not found in lesson corpus should produce WARNING so QA agent can review; issues=" + issues);
    }

    /**
     * Missing correctAnswer for MULTIPLE_CHOICE → ERROR.
     */
    @Test
    void missingCorrectAnswer_producesError() {
        Map<String, Object> exercise = new HashMap<>();
        exercise.put("type", "MULTIPLE_CHOICE");
        exercise.put("question", "Choose?");
        exercise.put("options", List.of("A", "B"));
        // no correctAnswer key

        Map<String, Object> content = buildContentWithExercise(exercise);
        List<ValidationIssue> issues = rule.check(content, "language", "de");

        assertTrue(issues.stream().anyMatch(ValidationIssue::isError),
                "Missing correctAnswer must be flagged as ERROR; issues=" + issues);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /** Builds the minimal Food & Drinks lesson that contains "Magst du Tee?" in grammar examples. */
    private Map<String, Object> buildFoodLesson() {
        return Map.of(
                "grammar", Map.of(
                        "title", "Expressing likes and making polite requests",
                        "explanation", "Use ich mag to express preferences. Use ich möchte for polite requests.",
                        "examples", List.of(
                                Map.of("german", "Ich mag Kaffee.", "english", "I like coffee."),
                                Map.of("german", "Magst du Tee?", "english", "Do you like tea?"),
                                Map.of("german", "Ich möchte Wasser, bitte.", "english", "I would like water, please.")
                        ),
                        "conjugationTable", Map.of(
                                "ich mag", "I like",
                                "du magst", "you like (informal)",
                                "ich möchte", "I would like",
                                "du möchtest", "you would like (informal)"
                        )
                ),
                "vocabulary", Map.of("items", List.of(
                        Map.of("german", "der Tee", "english", "tea"),
                        Map.of("german", "der Kaffee", "english", "coffee")
                )),
                "exercises", Map.of("items", List.of(
                        Map.of(
                                "type", "MULTIPLE_CHOICE",
                                "question", "How do you say 'Do you like tea?' in German?",
                                "correctAnswer", "Magst du Tee?",
                                "options", List.of("Magst du Tee?", "Möchtest du Tee?", "Ich mag Tee.", "Trinkst du Tee nicht?")
                        )
                ))
        );
    }

    private Map<String, Object> buildContentWithGrammarExample(String germanExample) {
        return new HashMap<>(Map.of(
                "grammar", Map.of(
                        "examples", List.of(Map.of("german", germanExample, "english", "translation"))
                ),
                "vocabulary", Map.of("items", List.of()),
                "exercises", Map.of("items", List.of())
        ));
    }

    private Map<String, Object> addExercise(Map<String, Object> content, Map<String, Object> exercise) {
        Map<String, Object> mutable = new HashMap<>(content);
        mutable.put("exercises", Map.of("items", List.of(exercise)));
        return mutable;
    }

    private Map<String, Object> buildContentWithExercise(Map<String, Object> exercise) {
        return Map.of(
                "grammar", Map.of("examples", List.of()),
                "vocabulary", Map.of("items", List.of()),
                "exercises", Map.of("items", List.of(exercise))
        );
    }
}
