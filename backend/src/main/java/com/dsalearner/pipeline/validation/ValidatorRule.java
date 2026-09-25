package com.dsalearner.pipeline.validation;

import java.util.List;
import java.util.Map;

/**
 * A single deterministic validation check. No LLM calls allowed.
 */
public interface ValidatorRule {
    String code();
    List<ValidationIssue> check(Map<String, Object> content, String domainCode, String languageCode);
}
