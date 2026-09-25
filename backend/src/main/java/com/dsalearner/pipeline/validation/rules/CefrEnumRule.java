package com.dsalearner.pipeline.validation.rules;

import com.dsalearner.pipeline.validation.ValidationIssue;
import com.dsalearner.pipeline.validation.ValidatorRule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Verifies cefrLevel is a valid CEFR enum value when the domain uses CEFR.
 */
@Component
public class CefrEnumRule implements ValidatorRule {

    private static final Set<String> VALID = Set.of("A1", "A2", "B1", "B2", "C1", "C2");

    @Override
    public String code() { return "CEFR_INVALID"; }

    @Override
    public List<ValidationIssue> check(Map<String, Object> content, String domainCode, String languageCode) {
        if ("dsa".equals(domainCode)) return List.of(); // CEFR not applicable

        Object level = content.get("cefrLevel");
        if (level == null) return List.of(); // caught by SchemaRequiredFieldsRule

        if (!VALID.contains(level.toString().toUpperCase())) {
            return List.of(ValidationIssue.error(code(), "cefrLevel",
                    "Invalid CEFR level: '" + level + "'. Must be one of " + VALID));
        }
        return List.of();
    }
}
