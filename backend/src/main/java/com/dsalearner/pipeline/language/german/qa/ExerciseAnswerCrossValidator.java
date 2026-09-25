package com.dsalearner.pipeline.language.german.qa;

import com.dsalearner.pipeline.agent.Issue;

import java.util.*;

/**
 * Deterministic cross-validator that checks whether each exercise's correctAnswer
 * is grounded in the lesson content (grammar examples, content examples, vocabulary).
 *
 * This runs BEFORE LLM QA and produces a set of "cleared" answers — exercise answers
 * that are explicitly taught by the lesson. QaAggregator uses this evidence to
 * prevent LLM false-positive ERRORs for answers the lesson explicitly teaches.
 *
 * Zero LLM calls — pure string matching against lesson JSONB fields.
 */
public class ExerciseAnswerCrossValidator {

    /**
     * Result of the cross-validation for a single lesson version.
     *
     * @param clearedAnswers  Set of (exerciseIndex, correctAnswer) pairs that are
     *                        explicitly grounded in the lesson's taught forms.
     *                        An LLM ERROR for a cleared answer should be downgraded to WARNING.
     * @param taughtForms     All German forms found in grammar/examples/vocabulary — for diagnostics.
     */
    public record Result(
            Set<ClearedAnswer> clearedAnswers,
            Set<String> taughtForms
    ) {
        public boolean isCleared(int exerciseIndex, String correctAnswer) {
            return clearedAnswers.contains(new ClearedAnswer(exerciseIndex, normalise(correctAnswer)));
        }
    }

    public record ClearedAnswer(int exerciseIndex, String normalisedAnswer) {}

    /**
     * Validates exercise answers against lesson content.
     *
     * @param lessonContent  The merged lesson map passed to QA agents (content + vocabulary + grammar + exercises).
     */
    @SuppressWarnings("unchecked")
    public Result validate(Map<String, Object> lessonContent) {
        Set<String> taughtForms = new LinkedHashSet<>();

        // ── Collect grammar examples ───────────────────────────────────────────
        Map<String, Object> grammar = (Map<String, Object>) lessonContent.get("grammar");
        if (grammar != null) {
            List<Map<String, Object>> grammarExamples = (List<Map<String, Object>>) grammar.get("examples");
            if (grammarExamples != null) {
                for (Map<String, Object> ex : grammarExamples) {
                    addString(taughtForms, ex.get("german"));
                }
            }
            Map<String, Object> conjTable = (Map<String, Object>) grammar.get("conjugationTable");
            if (conjTable != null) {
                conjTable.keySet().forEach(k -> taughtForms.add(normalise(k)));
            }
        }

        // ── Collect content examples ───────────────────────────────────────────
        List<Map<String, Object>> contentExamples = (List<Map<String, Object>>) lessonContent.get("examples");
        if (contentExamples != null) {
            for (Map<String, Object> ex : contentExamples) {
                addString(taughtForms, ex.get("german"));
            }
        }

        // ── Collect vocabulary examples ────────────────────────────────────────
        Object vocabRaw = lessonContent.get("vocabulary");
        List<Map<String, Object>> vocabItems = null;
        if (vocabRaw instanceof Map<?,?> vocabMap) {
            vocabItems = (List<Map<String, Object>>) ((Map<?,?>) vocabMap).get("items");
        } else if (vocabRaw instanceof List<?>) {
            vocabItems = (List<Map<String, Object>>) vocabRaw;
        }
        if (vocabItems != null) {
            for (Map<String, Object> item : vocabItems) {
                addString(taughtForms, item.get("german"));
                addString(taughtForms, item.get("example"));
            }
        }

        // ── Check each exercise's correctAnswer ───────────────────────────────
        Set<ClearedAnswer> clearedAnswers = new LinkedHashSet<>();
        Object exercisesRaw = lessonContent.get("exercises");
        List<Map<String, Object>> exerciseItems = null;
        if (exercisesRaw instanceof Map<?,?> exMap) {
            exerciseItems = (List<Map<String, Object>>) ((Map<?,?>) exMap).get("items");
        } else if (exercisesRaw instanceof List<?>) {
            exerciseItems = (List<Map<String, Object>>) exercisesRaw;
        }

        if (exerciseItems != null) {
            for (int i = 0; i < exerciseItems.size(); i++) {
                Map<String, Object> exercise = exerciseItems.get(i);
                Object correctAnswerRaw = exercise.get("correctAnswer");
                if (correctAnswerRaw == null) continue;
                String correctAnswer = normalise(correctAnswerRaw.toString());

                // Check direct match in taught forms
                if (taughtForms.contains(correctAnswer)) {
                    clearedAnswers.add(new ClearedAnswer(i, correctAnswer));
                    continue;
                }

                // Check if correctAnswer is a single word that appears as a substring of any taught form
                // (handles conjugated-verb fill-in-blank answers like "mag" which appears in "Ich mag Kaffee.")
                boolean subwordMatch = taughtForms.stream().anyMatch(form ->
                        containsWholeWord(form, correctAnswer));
                if (subwordMatch) {
                    clearedAnswers.add(new ClearedAnswer(i, correctAnswer));
                }
            }
        }

        return new Result(Collections.unmodifiableSet(clearedAnswers),
                          Collections.unmodifiableSet(taughtForms));
    }

    private static void addString(Set<String> forms, Object value) {
        if (value instanceof String s && !s.isBlank()) {
            forms.add(normalise(s));
        }
    }

    /** Normalise by trimming, collapsing whitespace, and lowercasing for comparison. */
    static String normalise(String s) {
        return s.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** Returns true if {@code form} contains {@code word} as a whole word (space/punctuation boundary). */
    private static boolean containsWholeWord(String form, String word) {
        if (word.isBlank() || word.length() < 2) return false;
        String pattern = "(?<![\\wäöüÄÖÜß])" + java.util.regex.Pattern.quote(word) + "(?![\\wäöüÄÖÜß])";
        return java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(form).find();
    }
}
