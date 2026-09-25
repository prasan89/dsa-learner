package com.dsalearner.pipeline.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Runs all registered ValidatorRules in order.
 * Must complete in < 200ms — zero LLM cost.
 * Any ERROR severity finding causes overall failure.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeterministicValidator {

    private final List<ValidatorRule> rules;

    public ValidationResult validate(Map<String, Object> content, String domainCode, String languageCode) {
        List<ValidationIssue> allIssues = new ArrayList<>();

        for (ValidatorRule rule : rules) {
            try {
                List<ValidationIssue> ruleIssues = rule.check(content, domainCode, languageCode);
                allIssues.addAll(ruleIssues);
            } catch (Exception e) {
                log.error("Validator rule {} threw: {}", rule.code(), e.getMessage());
                allIssues.add(ValidationIssue.error(
                        "RULE_INTERNAL_ERROR",
                        rule.code(),
                        "Validator rule threw unexpected exception: " + e.getMessage()
                ));
            }
        }

        boolean passed = allIssues.stream().noneMatch(ValidationIssue::isError);
        log.debug("Validation for domain={} lang={}: passed={} issues={}",
                domainCode, languageCode, passed, allIssues.size());

        return passed
                ? new ValidationResult(true, allIssues)   // include warnings even on pass
                : ValidationResult.fail(allIssues);
    }
}
