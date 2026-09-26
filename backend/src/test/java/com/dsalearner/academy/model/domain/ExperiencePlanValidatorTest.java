package com.dsalearner.academy.model.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ExperiencePlanValidatorTest {

    private ExperiencePlanValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ExperiencePlanValidator();
    }

    private ExperiencePlan validPlan() {
        List<ExperiencePlanStep> steps = new ArrayList<>();
        steps.add(ExperiencePlanStep.of(0, StepType.NARRATIVE, Map.of("text", "Hallo!")));
        steps.add(ExperiencePlanStep.of(1, StepType.MULTIPLE_CHOICE, Map.of(
                "question", "What does Hallo mean?",
                "options", List.of("Hello", "Bye", "Thanks"),
                "correctIndex", 0)));
        steps.add(ExperiencePlanStep.of(2, StepType.LESSON_REVIEW, Map.of(
                "lessonTitle", "Greetings", "exerciseCount", 1)));
        return new ExperiencePlan(UUID.randomUUID(), UUID.randomUUID(), 1,
                "Greetings", "A1", "de", null, steps);
    }

    @Test
    void validPlan_passesValidation() {
        assertThat(validator.validate(validPlan()).valid()).isTrue();
    }

    @Test
    void nullPlan_failsWithMessage() {
        var result = validator.validate(null);
        assertThat(result.valid()).isFalse();
        assertThat(result.violations()).anyMatch(v -> v.contains("must not be null"));
    }

    @Test
    void emptySteps_failsRule2() {
        ExperiencePlan plan = new ExperiencePlan(UUID.randomUUID(), UUID.randomUUID(), 1,
                "T", "A1", "de", null, List.of());
        var result = validator.validate(plan);
        assertThat(result.valid()).isFalse();
        assertThat(result.violations()).anyMatch(v -> v.contains("at least one step"));
    }

    @Test
    void lastStepNotReview_failsRule3() {
        List<ExperiencePlanStep> steps = List.of(
                ExperiencePlanStep.of(0, StepType.NARRATIVE, Map.of("text", "Text")),
                ExperiencePlanStep.of(1, StepType.MULTIPLE_CHOICE, Map.of(
                        "question", "Q?", "options", List.of("A"), "correctIndex", 0)));
        ExperiencePlan plan = new ExperiencePlan(UUID.randomUUID(), UUID.randomUUID(), 1,
                "T", "A1", "de", null, steps);
        var result = validator.validate(plan);
        assertThat(result.violations()).anyMatch(v -> v.contains("last step must be LESSON_REVIEW"));
    }

    @Test
    void nonContiguousIndices_failsRule4() {
        List<ExperiencePlanStep> steps = List.of(
                ExperiencePlanStep.of(0, StepType.NARRATIVE, Map.of("text", "T")),
                ExperiencePlanStep.of(5, StepType.MULTIPLE_CHOICE, Map.of(
                        "question", "Q?", "options", List.of("A"), "correctIndex", 0)),
                ExperiencePlanStep.of(2, StepType.LESSON_REVIEW, Map.of("exerciseCount", 1)));
        ExperiencePlan plan = new ExperiencePlan(UUID.randomUUID(), UUID.randomUUID(), 1,
                "T", "A1", "de", null, steps);
        var result = validator.validate(plan);
        assertThat(result.valid()).isFalse();
        assertThat(result.violations()).anyMatch(v -> v.contains("index=5"));
    }

    @Test
    void multipleChoiceMissingQuestion_fails() {
        List<ExperiencePlanStep> steps = new ArrayList<>();
        steps.add(ExperiencePlanStep.of(0, StepType.NARRATIVE, Map.of("text", "T")));
        steps.add(ExperiencePlanStep.of(1, StepType.MULTIPLE_CHOICE, Map.of(
                "options", List.of("A"), "correctIndex", 0)));
        steps.add(ExperiencePlanStep.of(2, StepType.LESSON_REVIEW, Map.of("exerciseCount", 1)));
        ExperiencePlan plan = new ExperiencePlan(UUID.randomUUID(), UUID.randomUUID(), 1,
                "T", "A1", "de", null, steps);
        var result = validator.validate(plan);
        assertThat(result.violations()).anyMatch(v -> v.contains("MULTIPLE_CHOICE") && v.contains("question"));
    }

    @Test
    void multipleChoiceMissingOptions_fails() {
        List<ExperiencePlanStep> steps = new ArrayList<>();
        steps.add(ExperiencePlanStep.of(0, StepType.NARRATIVE, Map.of("text", "T")));
        steps.add(ExperiencePlanStep.of(1, StepType.MULTIPLE_CHOICE, Map.of(
                "question", "Q?", "correctIndex", 0)));
        steps.add(ExperiencePlanStep.of(2, StepType.LESSON_REVIEW, Map.of("exerciseCount", 1)));
        ExperiencePlan plan = new ExperiencePlan(UUID.randomUUID(), UUID.randomUUID(), 1,
                "T", "A1", "de", null, steps);
        var result = validator.validate(plan);
        assertThat(result.violations()).anyMatch(v -> v.contains("options"));
    }

    @Test
    void fillInBlankMissingAnswer_fails() {
        List<ExperiencePlanStep> steps = new ArrayList<>();
        steps.add(ExperiencePlanStep.of(0, StepType.NARRATIVE, Map.of("text", "T")));
        steps.add(ExperiencePlanStep.of(1, StepType.FILL_IN_BLANK, Map.of("question", "Q?")));
        steps.add(ExperiencePlanStep.of(2, StepType.LESSON_REVIEW, Map.of("exerciseCount", 1)));
        ExperiencePlan plan = new ExperiencePlan(UUID.randomUUID(), UUID.randomUUID(), 1,
                "T", "A1", "de", null, steps);
        var result = validator.validate(plan);
        assertThat(result.violations()).anyMatch(v -> v.contains("FILL_IN_BLANK") && v.contains("answer"));
    }

    @Test
    void translationMissingSourceText_fails() {
        List<ExperiencePlanStep> steps = new ArrayList<>();
        steps.add(ExperiencePlanStep.of(0, StepType.NARRATIVE, Map.of("text", "T")));
        steps.add(ExperiencePlanStep.of(1, StepType.TRANSLATION, Map.of(
                "question", "Q?", "targetText", "Hallo")));
        steps.add(ExperiencePlanStep.of(2, StepType.LESSON_REVIEW, Map.of("exerciseCount", 1)));
        ExperiencePlan plan = new ExperiencePlan(UUID.randomUUID(), UUID.randomUUID(), 1,
                "T", "A1", "de", null, steps);
        var result = validator.validate(plan);
        assertThat(result.violations()).anyMatch(v -> v.contains("sourceText"));
    }

    @Test
    void vocabCardMissingWord_fails() {
        List<ExperiencePlanStep> steps = new ArrayList<>();
        steps.add(ExperiencePlanStep.of(0, StepType.NARRATIVE, Map.of("text", "T")));
        steps.add(ExperiencePlanStep.of(1, StepType.VOCABULARY_CARD, Map.of("translation", "Hello")));
        steps.add(ExperiencePlanStep.of(2, StepType.MULTIPLE_CHOICE, Map.of(
                "question", "Q?", "options", List.of("A"), "correctIndex", 0)));
        steps.add(ExperiencePlanStep.of(3, StepType.LESSON_REVIEW, Map.of("exerciseCount", 1)));
        ExperiencePlan plan = new ExperiencePlan(UUID.randomUUID(), UUID.randomUUID(), 1,
                "T", "A1", "de", null, steps);
        var result = validator.validate(plan);
        assertThat(result.violations()).anyMatch(v -> v.contains("VOCABULARY_CARD") && v.contains("word"));
    }

    @Test
    void noNarrativeStep_failsRule16() {
        List<ExperiencePlanStep> steps = List.of(
                ExperiencePlanStep.of(0, StepType.MULTIPLE_CHOICE, Map.of(
                        "question", "Q?", "options", List.of("A"), "correctIndex", 0)),
                ExperiencePlanStep.of(1, StepType.LESSON_REVIEW, Map.of("exerciseCount", 1)));
        ExperiencePlan plan = new ExperiencePlan(UUID.randomUUID(), UUID.randomUUID(), 1,
                "T", "A1", "de", null, steps);
        var result = validator.validate(plan);
        assertThat(result.violations()).anyMatch(v -> v.contains("NARRATIVE"));
    }

    @Test
    void noExerciseStep_failsRule17() {
        List<ExperiencePlanStep> steps = List.of(
                ExperiencePlanStep.of(0, StepType.NARRATIVE, Map.of("text", "T")),
                ExperiencePlanStep.of(1, StepType.LESSON_REVIEW, Map.of("exerciseCount", 0)));
        ExperiencePlan plan = new ExperiencePlan(UUID.randomUUID(), UUID.randomUUID(), 1,
                "T", "A1", "de", null, steps);
        var result = validator.validate(plan);
        assertThat(result.violations()).anyMatch(v -> v.contains("exercise step"));
    }

    @Test
    void twoLessonReviewSteps_failsRule20() {
        List<ExperiencePlanStep> steps = new ArrayList<>();
        steps.add(ExperiencePlanStep.of(0, StepType.NARRATIVE, Map.of("text", "T")));
        steps.add(ExperiencePlanStep.of(1, StepType.MULTIPLE_CHOICE, Map.of(
                "question", "Q?", "options", List.of("A"), "correctIndex", 0)));
        steps.add(ExperiencePlanStep.of(2, StepType.LESSON_REVIEW, Map.of("exerciseCount", 1)));
        steps.add(ExperiencePlanStep.of(3, StepType.LESSON_REVIEW, Map.of("exerciseCount", 1)));
        ExperiencePlan plan = new ExperiencePlan(UUID.randomUUID(), UUID.randomUUID(), 1,
                "T", "A1", "de", null, steps);
        var result = validator.validate(plan);
        assertThat(result.violations()).anyMatch(v -> v.contains("at most one LESSON_REVIEW"));
    }

    @Test
    void lessonVersionZero_failsRule15() {
        List<ExperiencePlanStep> steps = List.of(
                ExperiencePlanStep.of(0, StepType.NARRATIVE, Map.of("text", "T")),
                ExperiencePlanStep.of(1, StepType.MULTIPLE_CHOICE, Map.of(
                        "question", "Q?", "options", List.of("A"), "correctIndex", 0)),
                ExperiencePlanStep.of(2, StepType.LESSON_REVIEW, Map.of("exerciseCount", 1)));
        assertThatThrownBy(() -> new ExperiencePlan(UUID.randomUUID(), UUID.randomUUID(), 0,
                "T", "A1", "de", null, steps))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void vocabularySummaryWithEmptyItems_fails() {
        List<ExperiencePlanStep> steps = new ArrayList<>();
        steps.add(ExperiencePlanStep.of(0, StepType.NARRATIVE, Map.of("text", "T")));
        steps.add(ExperiencePlanStep.of(1, StepType.VOCABULARY_SUMMARY, Map.of("items", List.of())));
        steps.add(ExperiencePlanStep.of(2, StepType.MULTIPLE_CHOICE, Map.of(
                "question", "Q?", "options", List.of("A"), "correctIndex", 0)));
        steps.add(ExperiencePlanStep.of(3, StepType.LESSON_REVIEW, Map.of("exerciseCount", 1)));
        ExperiencePlan plan = new ExperiencePlan(UUID.randomUUID(), UUID.randomUUID(), 1,
                "T", "A1", "de", null, steps);
        var result = validator.validate(plan);
        assertThat(result.violations()).anyMatch(v -> v.contains("VOCABULARY_SUMMARY"));
    }

    @Test
    void narrativeMissingTextAndContent_fails() {
        List<ExperiencePlanStep> steps = new ArrayList<>();
        steps.add(ExperiencePlanStep.of(0, StepType.NARRATIVE, Map.of("title", "Only title")));
        steps.add(ExperiencePlanStep.of(1, StepType.MULTIPLE_CHOICE, Map.of(
                "question", "Q?", "options", List.of("A"), "correctIndex", 0)));
        steps.add(ExperiencePlanStep.of(2, StepType.LESSON_REVIEW, Map.of("exerciseCount", 1)));
        ExperiencePlan plan = new ExperiencePlan(UUID.randomUUID(), UUID.randomUUID(), 1,
                "T", "A1", "de", null, steps);
        var result = validator.validate(plan);
        assertThat(result.violations()).anyMatch(v -> v.contains("NARRATIVE") && v.contains("text"));
    }

    @Test
    void multipleViolations_allReported() {
        // Missing narrative AND missing exercise — both violations should be present
        List<ExperiencePlanStep> steps = List.of(
                ExperiencePlanStep.of(0, StepType.LESSON_REVIEW, Map.of("exerciseCount", 0)));
        ExperiencePlan plan = new ExperiencePlan(UUID.randomUUID(), UUID.randomUUID(), 1,
                "T", "A1", "de", null, steps);
        var result = validator.validate(plan);
        assertThat(result.violations().size()).isGreaterThanOrEqualTo(2);
    }
}
