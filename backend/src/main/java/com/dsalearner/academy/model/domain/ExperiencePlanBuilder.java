package com.dsalearner.academy.model.domain;

import com.dsalearner.pipeline.model.entity.CfLesson;
import com.dsalearner.pipeline.model.entity.CfLessonVersion;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure, stateless builder that projects a CfLessonVersion into an ExperiencePlan.
 *
 * Design constraints:
 * - Language-agnostic: reads JSON keys from lesson content, does not hard-code German.
 * - Content-driven: step count and step types are determined by what is present in the
 *   version's content/vocabulary/grammar/exercises JSON blobs.
 * - No I/O: all inputs arrive via parameters; no repository calls, no side effects.
 * - Deterministic: same inputs always produce the same plan.
 *
 * Step construction order:
 *   1. NARRATIVE        (intro: content.objectives + content.explanation.{intro,culturalNote})
 *   2. VOCABULARY_CARD  (one per vocabulary.items[] entry)
 *   3. GRAMMAR_EXPLANATION  (from grammar.{title,pattern,explanation,examples} — not grammar.rules[])
 *   4. NARRATIVE        (context: content.examples[] as dialogue cards)
 *   5. MULTIPLE_CHOICE / FILL_IN_BLANK  (first two exercises — practice before summary)
 *   6. VOCABULARY_SUMMARY  (if >= 3 vocabulary items)
 *   7. TRANSLATION / remaining exercises
 *   8. LESSON_REVIEW    (always last)
 */
@Component
public class ExperiencePlanBuilder {

    /**
     * Build an ExperiencePlan from a lesson + its current content version.
     *
     * @param lesson        the CfLesson entity (provides lessonId, title, CEFR, language)
     * @param version       the QA-passed CfLessonVersion (provides content blobs)
     * @param unitDisplayName  optional "Unit N: Theme" label (may be null)
     * @return a fully-populated, immutable ExperiencePlan
     * @throws IllegalArgumentException if required content blobs are missing
     */
    public ExperiencePlan build(CfLesson lesson, CfLessonVersion version, String unitDisplayName) {
        validateInputs(lesson, version);

        List<ExperiencePlanStep> steps = new ArrayList<>();

        Map<String, Object> content    = version.getContent();
        Map<String, Object> vocabulary = version.getVocabulary();
        Map<String, Object> grammar    = version.getGrammar();
        Map<String, Object> exercises  = version.getExercises();
        Map<String, Object> audio      = version.getAudioManifest();

        List<Map<String, Object>> vocabItems    = extractList(vocabulary, "items");
        List<Map<String, Object>> exerciseItems = extractList(exercises, "items");
        List<Map<String, Object>> contextExamples = extractList(content, "examples");

        // 1. Lesson intro — objectives + welcome text from content.objectives / content.explanation
        Map<String, Object> introPayload = buildIntroPayload(content);
        if (!introPayload.isEmpty()) {
            steps.add(ExperiencePlanStep.of(steps.size(), StepType.NARRATIVE, introPayload));
        }

        // 2. Vocabulary cards
        for (int vi = 0; vi < vocabItems.size(); vi++) {
            Map<String, Object> item = vocabItems.get(vi);
            String vocabAudioKey = audioKeyFor(audio, "vocab_" + vi);
            steps.add(ExperiencePlanStep.withAudio(
                    steps.size(), StepType.VOCABULARY_CARD, buildVocabPayload(item, vi), vocabAudioKey));
        }

        // 3. Grammar explanation — read directly from grammar object (not grammar.rules[])
        if (grammar != null && !grammar.isEmpty()) {
            steps.add(ExperiencePlanStep.of(
                    steps.size(), StepType.GRAMMAR_EXPLANATION,
                    buildGrammarFromObject(grammar)));
        }

        // 4. Context examples as a narrative step (conversational usage)
        if (!contextExamples.isEmpty()) {
            steps.add(ExperiencePlanStep.of(
                    steps.size(), StepType.NARRATIVE,
                    buildContextPayload(contextExamples)));
        }

        // 5. First two exercises (practice before summary)
        int splitAt = Math.min(2, exerciseItems.size());
        for (int ei = 0; ei < splitAt; ei++) {
            Map<String, Object> ex = exerciseItems.get(ei);
            steps.add(ExperiencePlanStep.of(steps.size(), resolveExerciseType(ex), buildExercisePayload(ex, ei)));
        }

        // 6. Vocabulary summary (when >= 3 items were presented)
        if (vocabItems.size() >= 3) {
            steps.add(ExperiencePlanStep.of(
                    steps.size(), StepType.VOCABULARY_SUMMARY,
                    buildVocabSummaryPayload(vocabItems)));
        }

        // 7. Remaining exercises (recall after summary)
        for (int ei = splitAt; ei < exerciseItems.size(); ei++) {
            Map<String, Object> ex = exerciseItems.get(ei);
            steps.add(ExperiencePlanStep.of(steps.size(), resolveExerciseType(ex), buildExercisePayload(ex, ei)));
        }

        // 8. Lesson review — always last
        steps.add(ExperiencePlanStep.of(
                steps.size(), StepType.LESSON_REVIEW,
                buildReviewPayload(lesson, content, exerciseItems.size())));

        return new ExperiencePlan(
                lesson.getId(),
                version.getId(),
                version.getVersion(),
                lesson.getTitle(),
                lesson.getCefrLevel(),
                lesson.getLanguageCode(),
                unitDisplayName,
                steps
        );
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private void validateInputs(CfLesson lesson, CfLessonVersion version) {
        if (lesson == null) throw new IllegalArgumentException("lesson must not be null");
        if (version == null) throw new IllegalArgumentException("version must not be null");
        if (version.getContent() == null) throw new IllegalArgumentException(
                "version.content must not be null for lessonId=" + lesson.getId());
        if (version.getVocabulary() == null) throw new IllegalArgumentException(
                "version.vocabulary must not be null for lessonId=" + lesson.getId());
        if (version.getGrammar() == null) throw new IllegalArgumentException(
                "version.grammar must not be null for lessonId=" + lesson.getId());
        if (version.getExercises() == null) throw new IllegalArgumentException(
                "version.exercises must not be null for lessonId=" + lesson.getId());
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractList(Map<String, Object> blob, String key) {
        if (blob == null) return List.of();
        Object val = blob.get(key);
        if (val instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildIntroPayload(Map<String, Object> content) {
        Map<String, Object> p = new HashMap<>();
        Object objectives = content.get("objectives");
        if (objectives != null) p.put("objectives", objectives);
        Object explanation = content.get("explanation");
        if (explanation instanceof Map<?,?> expMap) {
            Object intro = expMap.get("intro");
            if (intro != null) p.put("intro", intro);
            Object culturalNote = expMap.get("culturalNote");
            if (culturalNote != null) p.put("culturalNote", culturalNote);
        }
        p.put("sectionIndex", 0);
        return p;
    }

    private Map<String, Object> buildGrammarFromObject(Map<String, Object> grammar) {
        Map<String, Object> p = new HashMap<>(grammar);
        p.put("ruleIndex", 0);
        return p;
    }

    private Map<String, Object> buildContextPayload(List<Map<String, Object>> examples) {
        Map<String, Object> p = new HashMap<>();
        p.put("examples", examples);
        p.put("sectionIndex", 1);
        return p;
    }

    private Map<String, Object> buildVocabPayload(Map<String, Object> item, int index) {
        Map<String, Object> p = new HashMap<>(item);
        p.put("vocabIndex", index);
        return p;
    }

    private Map<String, Object> buildVocabSummaryPayload(List<Map<String, Object>> vocabItems) {
        Map<String, Object> p = new HashMap<>();
        p.put("items", vocabItems);
        p.put("count", vocabItems.size());
        return p;
    }

    private Map<String, Object> buildExercisePayload(Map<String, Object> ex, int index) {
        Map<String, Object> p = new HashMap<>(ex);
        p.put("exerciseIndex", index);
        return p;
    }

    private Map<String, Object> buildReviewPayload(CfLesson lesson, Map<String, Object> content, int exerciseCount) {
        Map<String, Object> p = new HashMap<>();
        p.put("lessonTitle", lesson.getTitle());
        p.put("cefrLevel", lesson.getCefrLevel());
        p.put("exerciseCount", exerciseCount);
        Object objectives = content.get("objectives");
        if (objectives != null) p.put("objectives", objectives);
        return p;
    }

    private StepType resolveExerciseType(Map<String, Object> ex) {
        String typeStr = (String) ex.getOrDefault("type", "");
        return switch (typeStr.toUpperCase()) {
            case "MULTIPLE_CHOICE" -> StepType.MULTIPLE_CHOICE;
            case "FILL_IN_BLANK"   -> StepType.FILL_IN_BLANK;
            case "TRANSLATION"     -> StepType.TRANSLATION;
            case "LISTENING"       -> StepType.LISTENING;
            default -> StepType.MULTIPLE_CHOICE;
        };
    }

    private String audioKeyFor(Map<String, Object> audioManifest, String key) {
        if (audioManifest == null) return null;
        Object val = audioManifest.get(key);
        return val instanceof String s ? s : null;
    }
}
