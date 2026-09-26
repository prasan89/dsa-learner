package com.dsalearner.academy.model.domain;

import com.dsalearner.pipeline.model.entity.CfLesson;
import com.dsalearner.pipeline.model.entity.CfLessonVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ExperiencePlanBuilderTest {

    private ExperiencePlanBuilder builder;
    private UUID lessonId;
    private UUID versionId;

    @BeforeEach
    void setUp() {
        builder = new ExperiencePlanBuilder();
        lessonId = UUID.randomUUID();
        versionId = UUID.randomUUID();
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private CfLesson lesson() {
        CfLesson l = new CfLesson();
        l.setId(lessonId);
        l.setTitle("Greetings and Introductions");
        l.setCefrLevel("A1");
        l.setLanguageCode("de");
        return l;
    }

    private CfLessonVersion version(
            Map<String, Object> content,
            Map<String, Object> vocabulary,
            Map<String, Object> grammar,
            Map<String, Object> exercises) {
        CfLessonVersion v = new CfLessonVersion();
        v.setId(versionId);
        v.setLessonId(lessonId);
        v.setVersion(1);
        v.setContent(content);
        v.setVocabulary(vocabulary);
        v.setGrammar(grammar);
        v.setExercises(exercises);
        return v;
    }

    private Map<String, Object> section(String text) {
        return Map.of("text", text, "title", "Section");
    }

    private Map<String, Object> vocabItem(String word, String translation) {
        return Map.of("word", word, "translation", translation, "example", "Example sentence.");
    }

    private Map<String, Object> grammarRule(String title, String explanation) {
        return Map.of("title", title, "explanation", explanation, "examples", List.of());
    }

    private Map<String, Object> multipleChoice(String question) {
        return Map.of(
                "type", "MULTIPLE_CHOICE",
                "question", question,
                "options", List.of("A", "B", "C", "D"),
                "correctIndex", 0);
    }

    private Map<String, Object> fillInBlank(String question) {
        return Map.of("type", "FILL_IN_BLANK", "question", question, "answer", "Hallo");
    }

    private Map<String, Object> translation(String question) {
        return Map.of("type", "TRANSLATION", "question", question,
                "sourceText", "Hello", "targetText", "Hallo");
    }

    private Map<String, Object> fullContent(int sections) {
        List<Map<String, Object>> secs = new ArrayList<>();
        for (int i = 0; i < sections; i++) secs.add(section("Text " + i));
        return Map.of("sections", secs);
    }

    private Map<String, Object> fullVocab(int count) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < count; i++) items.add(vocabItem("word" + i, "trans" + i));
        return Map.of("items", items);
    }

    private Map<String, Object> fullGrammar(int count) {
        List<Map<String, Object>> rules = new ArrayList<>();
        for (int i = 0; i < count; i++) rules.add(grammarRule("Rule " + i, "Explanation " + i));
        return Map.of("rules", rules);
    }

    private Map<String, Object> fullExercises(int count) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < count; i++) items.add(multipleChoice("Question " + i + "?"));
        return Map.of("items", items);
    }

    // ── 1. null guards ─────────────────────────────────────────────────────

    @Test
    void nullLesson_throws() {
        CfLessonVersion v = version(fullContent(1), fullVocab(1), fullGrammar(1), fullExercises(1));
        assertThatThrownBy(() -> builder.build(null, v, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lesson must not be null");
    }

    @Test
    void nullVersion_throws() {
        assertThatThrownBy(() -> builder.build(lesson(), null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("version must not be null");
    }

    @Test
    void nullContent_throws() {
        CfLessonVersion v = version(null, fullVocab(1), fullGrammar(1), fullExercises(1));
        assertThatThrownBy(() -> builder.build(lesson(), v, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("version.content must not be null");
    }

    @Test
    void nullVocabulary_throws() {
        CfLessonVersion v = version(fullContent(1), null, fullGrammar(1), fullExercises(1));
        assertThatThrownBy(() -> builder.build(lesson(), v, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("version.vocabulary must not be null");
    }

    @Test
    void nullExercises_throws() {
        CfLessonVersion v = version(fullContent(1), fullVocab(1), fullGrammar(1), null);
        assertThatThrownBy(() -> builder.build(lesson(), v, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("version.exercises must not be null");
    }

    // ── 2. plan metadata ───────────────────────────────────────────────────

    @Test
    void planCarriesLessonMetadata() {
        CfLessonVersion v = version(fullContent(1), fullVocab(1), fullGrammar(1), fullExercises(1));
        ExperiencePlan plan = builder.build(lesson(), v, "Unit 1: Greetings");

        assertThat(plan.lessonId()).isEqualTo(lessonId);
        assertThat(plan.lessonVersionId()).isEqualTo(versionId);
        assertThat(plan.lessonVersion()).isEqualTo(1);
        assertThat(plan.lessonTitle()).isEqualTo("Greetings and Introductions");
        assertThat(plan.cefrLevel()).isEqualTo("A1");
        assertThat(plan.languageCode()).isEqualTo("de");
        assertThat(plan.unitDisplayName()).isEqualTo("Unit 1: Greetings");
    }

    @Test
    void nullUnitDisplayName_isAccepted() {
        CfLessonVersion v = version(fullContent(1), fullVocab(1), fullGrammar(1), fullExercises(1));
        ExperiencePlan plan = builder.build(lesson(), v, null);
        assertThat(plan.unitDisplayName()).isNull();
    }

    // ── 3. step ordering and indices ──────────────────────────────────────

    @Test
    void stepIndicesAreContiguousFromZero() {
        CfLessonVersion v = version(fullContent(2), fullVocab(3), fullGrammar(1), fullExercises(2));
        ExperiencePlan plan = builder.build(lesson(), v, null);

        for (int i = 0; i < plan.totalSteps(); i++) {
            assertThat(plan.stepAt(i).index()).isEqualTo(i);
        }
    }

    @Test
    void lastStepIsAlwaysLessonReview() {
        CfLessonVersion v = version(fullContent(1), fullVocab(1), fullGrammar(1), fullExercises(1));
        ExperiencePlan plan = builder.build(lesson(), v, null);

        assertThat(plan.stepAt(plan.totalSteps() - 1).type()).isEqualTo(StepType.LESSON_REVIEW);
    }

    @Test
    void narrativeStepsAppearBeforeExercises() {
        CfLessonVersion v = version(fullContent(2), fullVocab(2), fullGrammar(1), fullExercises(2));
        ExperiencePlan plan = builder.build(lesson(), v, null);

        int lastNarrativeIdx = -1;
        int firstExerciseIdx = Integer.MAX_VALUE;
        for (ExperiencePlanStep step : plan.steps()) {
            if (step.type() == StepType.NARRATIVE) lastNarrativeIdx = step.index();
            if (step.isExercise() && step.index() < firstExerciseIdx) firstExerciseIdx = step.index();
        }
        assertThat(lastNarrativeIdx).isLessThan(firstExerciseIdx);
    }

    // ── 4. NARRATIVE steps ─────────────────────────────────────────────────

    @Test
    void narrativeStepCountMatchesSections() {
        CfLessonVersion v = version(fullContent(3), fullVocab(0), fullGrammar(0), fullExercises(1));
        ExperiencePlan plan = builder.build(lesson(), v, null);

        long count = plan.steps().stream().filter(s -> s.type() == StepType.NARRATIVE).count();
        assertThat(count).isEqualTo(3);
    }

    @Test
    void narrativePayloadContainsSectionText() {
        Map<String, Object> content = Map.of("sections", List.of(section("Hallo, wie geht es dir?")));
        CfLessonVersion v = version(content, fullVocab(0), fullGrammar(0), fullExercises(1));
        ExperiencePlan plan = builder.build(lesson(), v, null);

        ExperiencePlanStep narrative = plan.steps().stream()
                .filter(s -> s.type() == StepType.NARRATIVE)
                .findFirst().orElseThrow();
        assertThat(narrative.payload()).containsKey("text");
        assertThat(narrative.payload().get("text")).isEqualTo("Hallo, wie geht es dir?");
    }

    // ── 5. VOCABULARY_CARD steps ───────────────────────────────────────────

    @Test
    void vocabularyCardCountMatchesVocabItems() {
        CfLessonVersion v = version(fullContent(1), fullVocab(4), fullGrammar(0), fullExercises(1));
        ExperiencePlan plan = builder.build(lesson(), v, null);

        long count = plan.steps().stream().filter(s -> s.type() == StepType.VOCABULARY_CARD).count();
        assertThat(count).isEqualTo(4);
    }

    @Test
    void vocabCardPayloadHasWordAndTranslation() {
        Map<String, Object> vocab = Map.of("items", List.of(vocabItem("Hallo", "Hello")));
        CfLessonVersion v = version(fullContent(1), vocab, fullGrammar(0), fullExercises(1));
        ExperiencePlan plan = builder.build(lesson(), v, null);

        ExperiencePlanStep card = plan.steps().stream()
                .filter(s -> s.type() == StepType.VOCABULARY_CARD)
                .findFirst().orElseThrow();
        assertThat(card.payload().get("word")).isEqualTo("Hallo");
        assertThat(card.payload().get("translation")).isEqualTo("Hello");
    }

    // ── 6. VOCABULARY_SUMMARY ──────────────────────────────────────────────

    @Test
    void vocabSummaryAppearsWhenThreeOrMoreItems() {
        CfLessonVersion v = version(fullContent(1), fullVocab(3), fullGrammar(0), fullExercises(1));
        ExperiencePlan plan = builder.build(lesson(), v, null);

        long count = plan.steps().stream().filter(s -> s.type() == StepType.VOCABULARY_SUMMARY).count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    void vocabSummaryAbsentWhenFewerThanThreeItems() {
        CfLessonVersion v = version(fullContent(1), fullVocab(2), fullGrammar(0), fullExercises(1));
        ExperiencePlan plan = builder.build(lesson(), v, null);

        long count = plan.steps().stream().filter(s -> s.type() == StepType.VOCABULARY_SUMMARY).count();
        assertThat(count).isEqualTo(0);
    }

    @Test
    void vocabSummaryPayloadContainsAllItems() {
        CfLessonVersion v = version(fullContent(1), fullVocab(5), fullGrammar(0), fullExercises(1));
        ExperiencePlan plan = builder.build(lesson(), v, null);

        ExperiencePlanStep summary = plan.steps().stream()
                .filter(s -> s.type() == StepType.VOCABULARY_SUMMARY)
                .findFirst().orElseThrow();
        assertThat(summary.payload().get("count")).isEqualTo(5);
    }

    // ── 7. GRAMMAR_EXPLANATION steps ──────────────────────────────────────

    @Test
    void grammarExplanationCountMatchesRules() {
        CfLessonVersion v = version(fullContent(1), fullVocab(0), fullGrammar(2), fullExercises(1));
        ExperiencePlan plan = builder.build(lesson(), v, null);

        long count = plan.steps().stream().filter(s -> s.type() == StepType.GRAMMAR_EXPLANATION).count();
        assertThat(count).isEqualTo(2);
    }

    // ── 8. exercise type resolution ───────────────────────────────────────

    @Test
    void multipleChoiceExerciseResolvedCorrectly() {
        Map<String, Object> exercises = Map.of("items", List.of(multipleChoice("What does Hallo mean?")));
        CfLessonVersion v = version(fullContent(1), fullVocab(0), fullGrammar(0), exercises);
        ExperiencePlan plan = builder.build(lesson(), v, null);

        boolean found = plan.steps().stream().anyMatch(s -> s.type() == StepType.MULTIPLE_CHOICE);
        assertThat(found).isTrue();
    }

    @Test
    void fillInBlankExerciseResolvedCorrectly() {
        Map<String, Object> exercises = Map.of("items", List.of(fillInBlank("___, wie geht es dir?")));
        CfLessonVersion v = version(fullContent(1), fullVocab(0), fullGrammar(0), exercises);
        ExperiencePlan plan = builder.build(lesson(), v, null);

        boolean found = plan.steps().stream().anyMatch(s -> s.type() == StepType.FILL_IN_BLANK);
        assertThat(found).isTrue();
    }

    @Test
    void translationExerciseResolvedCorrectly() {
        Map<String, Object> exercises = Map.of("items", List.of(translation("Translate: Hello")));
        CfLessonVersion v = version(fullContent(1), fullVocab(0), fullGrammar(0), exercises);
        ExperiencePlan plan = builder.build(lesson(), v, null);

        boolean found = plan.steps().stream().anyMatch(s -> s.type() == StepType.TRANSLATION);
        assertThat(found).isTrue();
    }

    @Test
    void unknownExerciseTypeFallsBackToMultipleChoice() {
        Map<String, Object> ex = new HashMap<>();
        ex.put("type", "WEIRD_FUTURE_TYPE");
        ex.put("question", "Question?");
        ex.put("options", List.of("A", "B"));
        ex.put("correctIndex", 0);
        Map<String, Object> exercises = Map.of("items", List.of(ex));
        CfLessonVersion v = version(fullContent(1), fullVocab(0), fullGrammar(0), exercises);
        ExperiencePlan plan = builder.build(lesson(), v, null);

        boolean found = plan.steps().stream().anyMatch(s -> s.type() == StepType.MULTIPLE_CHOICE);
        assertThat(found).isTrue();
    }

    // ── 9. totalExercises and totalSteps ──────────────────────────────────

    @Test
    void totalExercisesCountsOnlyExerciseSteps() {
        CfLessonVersion v = version(fullContent(1), fullVocab(1), fullGrammar(1), fullExercises(3));
        ExperiencePlan plan = builder.build(lesson(), v, null);

        assertThat(plan.totalExercises()).isEqualTo(3);
    }

    @Test
    void totalStepsIncludesAllStepTypes() {
        // 2 narrative + 3 vocab cards + 1 grammar + 1 vocab summary + 2 exercises + 1 review = 10
        CfLessonVersion v = version(fullContent(2), fullVocab(3), fullGrammar(1), fullExercises(2));
        ExperiencePlan plan = builder.build(lesson(), v, null);

        assertThat(plan.totalSteps()).isEqualTo(plan.steps().size());
        assertThat(plan.totalSteps()).isGreaterThanOrEqualTo(10);
    }

    // ── 10. empty blobs ────────────────────────────────────────────────────

    @Test
    void emptyVocabProducesNoVocabCards() {
        CfLessonVersion v = version(fullContent(1), Map.of("items", List.of()), fullGrammar(0), fullExercises(1));
        ExperiencePlan plan = builder.build(lesson(), v, null);

        long count = plan.steps().stream().filter(s -> s.type() == StepType.VOCABULARY_CARD).count();
        assertThat(count).isEqualTo(0);
    }

    @Test
    void emptyGrammarProducesNoGrammarSteps() {
        CfLessonVersion v = version(fullContent(1), fullVocab(1), Map.of("rules", List.of()), fullExercises(1));
        ExperiencePlan plan = builder.build(lesson(), v, null);

        long count = plan.steps().stream().filter(s -> s.type() == StepType.GRAMMAR_EXPLANATION).count();
        assertThat(count).isEqualTo(0);
    }

    @Test
    void emptySectionsProducesNoNarrativeSteps() {
        CfLessonVersion v = version(Map.of("sections", List.of()), fullVocab(1), fullGrammar(0), fullExercises(1));
        ExperiencePlan plan = builder.build(lesson(), v, null);

        long count = plan.steps().stream().filter(s -> s.type() == StepType.NARRATIVE).count();
        assertThat(count).isEqualTo(0);
    }

    // ── 11. determinism ────────────────────────────────────────────────────

    @Test
    void builderIsDeterministic() {
        CfLessonVersion v = version(fullContent(2), fullVocab(4), fullGrammar(2), fullExercises(3));
        ExperiencePlan plan1 = builder.build(lesson(), v, "Unit 1");
        ExperiencePlan plan2 = builder.build(lesson(), v, "Unit 1");

        assertThat(plan1.totalSteps()).isEqualTo(plan2.totalSteps());
        for (int i = 0; i < plan1.totalSteps(); i++) {
            assertThat(plan1.stepAt(i).type()).isEqualTo(plan2.stepAt(i).type());
        }
    }

    // ── 12. audio manifest ─────────────────────────────────────────────────

    @Test
    void audioKeyPopulatedFromManifest() {
        Map<String, Object> audio = Map.of("section_0", "https://cdn.example.com/audio/s0.mp3");
        CfLessonVersion v = version(fullContent(1), fullVocab(0), fullGrammar(0), fullExercises(1));
        v.setAudioManifest(audio);
        ExperiencePlan plan = builder.build(lesson(), v, null);

        ExperiencePlanStep narrative = plan.steps().stream()
                .filter(s -> s.type() == StepType.NARRATIVE)
                .findFirst().orElseThrow();
        assertThat(narrative.audioKey()).isEqualTo("https://cdn.example.com/audio/s0.mp3");
    }

    @Test
    void nullAudioManifestProducesNullAudioKey() {
        CfLessonVersion v = version(fullContent(1), fullVocab(1), fullGrammar(0), fullExercises(1));
        v.setAudioManifest(null);
        ExperiencePlan plan = builder.build(lesson(), v, null);

        plan.steps().stream()
                .filter(s -> s.type() == StepType.NARRATIVE || s.type() == StepType.VOCABULARY_CARD)
                .forEach(s -> assertThat(s.audioKey()).isNull());
    }

    // ── 13. performance ────────────────────────────────────────────────────

    @Test
    void buildCompletesUnder10msForTypicalLesson() {
        // Typical German lesson: 4 sections, 10 vocab, 2 grammar rules, 5 exercises
        CfLessonVersion v = version(fullContent(4), fullVocab(10), fullGrammar(2), fullExercises(5));

        long start = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            builder.build(lesson(), v, "Unit 1");
        }
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        // 100 builds must complete in under 1000ms → < 10ms per build
        assertThat(elapsedMs).isLessThan(1000L);
    }
}
