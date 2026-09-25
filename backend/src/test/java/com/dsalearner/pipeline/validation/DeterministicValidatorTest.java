package com.dsalearner.pipeline.validation;

import com.dsalearner.pipeline.validation.rules.CefrEnumRule;
import com.dsalearner.pipeline.validation.rules.SchemaRequiredFieldsRule;
import com.dsalearner.pipeline.validation.rules.WordCountRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DeterministicValidatorTest {

    private DeterministicValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DeterministicValidator(List.of(
                new SchemaRequiredFieldsRule(),
                new CefrEnumRule(),
                new WordCountRule()
        ));
    }

    @Test
    void passesValidA1Content() {
        Map<String, Object> content = Map.of(
                "title",     "Greetings in German",
                "cefrLevel", "A1",
                "content",   "Hallo! Guten Morgen! Guten Tag! " + "word ".repeat(60)
        );
        ValidationResult result = validator.validate(content, "language", "de");
        assertTrue(result.passed(), "Expected pass but got: " + result.issues());
    }

    @Test
    void failsWhenRequiredFieldMissing() {
        Map<String, Object> content = Map.of(
                "title",     "Missing cefrLevel lesson",
                "content",   "Some content here."
        );
        ValidationResult result = validator.validate(content, "language", "de");
        assertFalse(result.passed());
        assertTrue(result.errors().stream().anyMatch(i -> "SCHEMA_REQUIRED_FIELDS".equals(i.code())));
    }

    @Test
    void failsOnInvalidCefrLevel() {
        Map<String, Object> content = Map.of(
                "title",     "Bad CEFR",
                "cefrLevel", "Z9",
                "content",   "Some content here."
        );
        ValidationResult result = validator.validate(content, "language", "de");
        assertFalse(result.passed());
        assertTrue(result.errors().stream().anyMatch(i -> "CEFR_INVALID".equals(i.code())));
    }

    @Test
    void warnsWhenContentTooShort() {
        Map<String, Object> content = Map.of(
                "title",     "Too short",
                "cefrLevel", "A1",
                "content",   "Short."
        );
        ValidationResult result = validator.validate(content, "language", "de");
        // Word count is WARNING not ERROR — passed = true
        assertTrue(result.passed());
        assertTrue(result.warnings().stream().anyMatch(i -> "WORD_COUNT_OOB".equals(i.code())));
    }

    @Test
    void dsaSkipsCefrAndWordCountRules() {
        Map<String, Object> content = Map.of(
                "title",   "Array Basics",
                "content", "Arrays are contiguous memory blocks."
        );
        ValidationResult result = validator.validate(content, "dsa", null);
        assertTrue(result.passed(), "DSA should skip CEFR and word count: " + result.issues());
    }

    @Test
    void failsWhenTitleMissingForDsa() {
        Map<String, Object> content = Map.of(
                "content", "Some DSA content."
        );
        ValidationResult result = validator.validate(content, "dsa", null);
        assertFalse(result.passed());
    }

    @Test
    void validatorRuleExceptionIsCaughtAsError() {
        ValidatorRule buggyRule = new ValidatorRule() {
            @Override public String code() { return "BUGGY"; }
            @Override public List<ValidationIssue> check(Map<String, Object> c, String d, String l) {
                throw new RuntimeException("bug!");
            }
        };
        DeterministicValidator v = new DeterministicValidator(List.of(buggyRule));
        ValidationResult result = v.validate(Map.of("title", "x", "cefrLevel", "A1", "content", "y"),
                "language", "de");
        assertFalse(result.passed());
        assertTrue(result.errors().stream().anyMatch(i -> "RULE_INTERNAL_ERROR".equals(i.code())));
    }
}
