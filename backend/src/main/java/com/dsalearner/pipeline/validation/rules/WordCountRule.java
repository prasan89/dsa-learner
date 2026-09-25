package com.dsalearner.pipeline.validation.rules;

import com.dsalearner.pipeline.validation.ValidationIssue;
import com.dsalearner.pipeline.validation.ValidatorRule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Checks that the content body word count is within bounds for the declared CEFR level.
 */
@Component
public class WordCountRule implements ValidatorRule {

    @Override
    public String code() { return "WORD_COUNT_OOB"; }

    @Override
    public List<ValidationIssue> check(Map<String, Object> content, String domainCode, String languageCode) {
        if ("dsa".equals(domainCode)) return List.of();

        Object body = content.get("content");
        if (body == null) return List.of();

        int words = body.toString().split("\\s+").length;
        Object level = content.get("cefrLevel");
        if (level == null) return List.of();

        int[] bounds = bounds(level.toString().toUpperCase());
        if (words < bounds[0]) {
            return List.of(ValidationIssue.warning(code(), "content",
                    "Content too short for %s: %d words (min %d)".formatted(level, words, bounds[0])));
        }
        if (words > bounds[1]) {
            return List.of(ValidationIssue.warning(code(), "content",
                    "Content too long for %s: %d words (max %d)".formatted(level, words, bounds[1])));
        }
        return List.of();
    }

    private int[] bounds(String level) {
        return switch (level) {
            case "A1" -> new int[]{50, 300};
            case "A2" -> new int[]{100, 500};
            case "B1" -> new int[]{200, 800};
            case "B2" -> new int[]{300, 1200};
            case "C1" -> new int[]{400, 1800};
            case "C2" -> new int[]{500, 2500};
            default   -> new int[]{50, 3000};
        };
    }
}
