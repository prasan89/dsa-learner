package com.dsalearner.pipeline.validation.rules;

import com.dsalearner.pipeline.validation.ValidationIssue;
import com.dsalearner.pipeline.validation.ValidatorRule;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Deterministic cross-validation for language lesson exercises.
 *
 * Checks performed (zero LLM cost):
 *  1. MULTIPLE_CHOICE — correctAnswer must be one of the declared options.
 *  2. MULTIPLE_CHOICE — correctAnswer must appear verbatim in grammar examples or
 *     vocabulary examples so an answer the lesson itself teaches is never flagged wrong.
 *  3. FILL_IN_BLANK / TRANSLATION — correctAnswer must be non-blank.
 *
 * Only runs for domainCode == "language". Skips non-language lessons silently.
 */
@Component
public class ExerciseCrossValidationRule implements ValidatorRule {

    @Override
    public String code() { return "EXERCISE_CROSS_VALIDATION"; }

    @Override
    @SuppressWarnings("unchecked")
    public List<ValidationIssue> check(Map<String, Object> content, String domainCode, String languageCode) {
        if (!"language".equals(domainCode)) return List.of();

        List<ValidationIssue> issues = new ArrayList<>();

        Map<String, Object> exercisesBlock = (Map<String, Object>) content.get("exercises");
        if (exercisesBlock == null) return issues;

        List<Map<String, Object>> items = (List<Map<String, Object>>) exercisesBlock.get("items");
        if (items == null || items.isEmpty()) return issues;

        // Collect lesson text that forms the "allowed answer corpus"
        String lessonText = collectLessonText(content).toLowerCase();

        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> ex = items.get(i);
            String type          = str(ex, "type");
            String correctAnswer = str(ex, "correctAnswer");
            String field         = "exercises[" + i + "].correctAnswer";

            if (correctAnswer == null || correctAnswer.isBlank()) {
                if ("MULTIPLE_CHOICE".equals(type) || "FILL_IN_BLANK".equals(type) || "TRANSLATION".equals(type)) {
                    issues.add(ValidationIssue.error(code(), field, "Exercise has no correctAnswer"));
                }
                continue;
            }

            if ("MULTIPLE_CHOICE".equals(type)) {
                List<String> options = (List<String>) ex.get("options");
                if (options != null && !options.isEmpty()) {
                    boolean answerInOptions = options.stream()
                            .anyMatch(o -> o != null && o.trim().equalsIgnoreCase(correctAnswer.trim()));
                    if (!answerInOptions) {
                        issues.add(ValidationIssue.error(code(), field,
                                "correctAnswer '" + correctAnswer + "' is not present in the options list"));
                    }
                }

                // Check: answer must appear in lesson grammar examples or vocabulary
                // This guards against the false-positive where a valid lesson phrase is flagged
                boolean evidencedInLesson = lessonText.contains(correctAnswer.trim().toLowerCase());
                if (!evidencedInLesson) {
                    issues.add(ValidationIssue.warning(code(), field,
                            "correctAnswer '" + correctAnswer
                            + "' could not be found in lesson grammar or vocabulary; verify it is actually taught"));
                }
            }
        }

        return issues;
    }

    @SuppressWarnings("unchecked")
    private String collectLessonText(Map<String, Object> content) {
        StringBuilder sb = new StringBuilder();

        // Grammar examples
        Map<String, Object> grammar = (Map<String, Object>) content.get("grammar");
        if (grammar != null) {
            appendString(sb, grammar.get("explanation"));
            List<Map<String, Object>> examples = (List<Map<String, Object>>) grammar.get("examples");
            if (examples != null) {
                for (Map<String, Object> ex : examples) {
                    appendString(sb, ex.get("german"));
                    appendString(sb, ex.get("english"));
                }
            }
            // conjugation table values
            Object table = grammar.get("conjugationTable");
            if (table instanceof Map<?, ?> m) {
                m.forEach((k, v) -> { appendString(sb, k); appendString(sb, v); });
            }
        }

        // Vocabulary items
        Map<String, Object> vocabBlock = (Map<String, Object>) content.get("vocabulary");
        if (vocabBlock != null) {
            List<Map<String, Object>> vocabItems = (List<Map<String, Object>>) vocabBlock.get("items");
            if (vocabItems != null) {
                for (Map<String, Object> v : vocabItems) {
                    appendString(sb, v.get("german"));
                    appendString(sb, v.get("english"));
                    appendString(sb, v.get("example"));
                }
            }
        }

        // Content examples
        Map<String, Object> contentBlock = (Map<String, Object>) content.get("content");
        if (contentBlock != null) {
            List<Map<String, Object>> examples = (List<Map<String, Object>>) contentBlock.get("examples");
            if (examples != null) {
                for (Map<String, Object> ex : examples) {
                    appendString(sb, ex.get("german"));
                    appendString(sb, ex.get("english"));
                }
            }
        }

        return sb.toString();
    }

    private void appendString(StringBuilder sb, Object v) {
        if (v instanceof String s) sb.append(' ').append(s).append(' ');
    }

    private String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v instanceof String s ? s : null;
    }
}
