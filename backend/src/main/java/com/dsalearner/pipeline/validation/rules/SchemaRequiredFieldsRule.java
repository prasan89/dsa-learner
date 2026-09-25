package com.dsalearner.pipeline.validation.rules;

import com.dsalearner.pipeline.validation.ValidationIssue;
import com.dsalearner.pipeline.validation.ValidatorRule;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Verifies that all required top-level fields are present and non-null.
 */
@Component
public class SchemaRequiredFieldsRule implements ValidatorRule {

    private static final Set<String> LANGUAGE_REQUIRED = Set.of("title", "cefrLevel", "content");
    private static final Set<String> DSA_REQUIRED       = Set.of("title", "content");

    @Override
    public String code() { return "SCHEMA_REQUIRED_FIELDS"; }

    @Override
    public List<ValidationIssue> check(Map<String, Object> content, String domainCode, String languageCode) {
        List<ValidationIssue> issues = new ArrayList<>();
        Set<String> required = "dsa".equals(domainCode) ? DSA_REQUIRED : LANGUAGE_REQUIRED;

        for (String field : required) {
            if (!content.containsKey(field) || content.get(field) == null) {
                issues.add(ValidationIssue.error(code(), field,
                        "Required field '" + field + "' is missing or null"));
            }
        }
        return issues;
    }
}
