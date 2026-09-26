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
 *   1. NARRATIVE  (from content.sections[] — each section becomes one step)
 *   2. VOCABULARY_CARD  (one per vocabulary.items[] entry, interleaved with narrative)
 *   3. GRAMMAR_EXPLANATION  (one per grammar.rules[] entry)
 *   4. VOCABULARY_SUMMARY  (if >= 3 vocabulary items)
 *   5. MULTIPLE_CHOICE / FILL_IN_BLANK / TRANSLATION  (from exercises.items[])
 *   6. LESSON_REVIEW  (always last)
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

        // 1. Narrative sections — each section card
        List<Map<String, Object>> sections = extractList(content, "sections");
        List<Map<String, Object>> vocabItems = extractList(vocabulary, "items");
        List<Map<String, Object>> grammarRules = extractList(grammar, "rules");
        List<Map<String, Object>> exerciseItems = extractList(exercises, "items");

        int vocabPerSection = sections.isEmpty() ? 0 : Math.max(1, vocabItems.size() / Math.max(1, sections.size()));
        int vocabOffset = 0;

        for (int si = 0; si < sections.size(); si++) {
            Map<String, Object> section = sections.get(si);
            String audioKey = audioKeyFor(audio, "section_" + si);
            steps.add(ExperiencePlanStep.withAudio(
                    steps.size(), StepType.NARRATIVE, buildNarrativePayload(section, si), audioKey));

            // Interleave vocabulary cards after each narrative section
            int limit = (si == sections.size() - 1)
                    ? vocabItems.size()
                    : Math.min(vocabOffset + vocabPerSection, vocabItems.size());
            while (vocabOffset < limit) {
                Map<String, Object> item = vocabItems.get(vocabOffset);
                String vocabAudioKey = audioKeyFor(audio, "vocab_" + vocabOffset);
                steps.add(ExperiencePlanStep.withAudio(
                        steps.size(), StepType.VOCABULARY_CARD, buildVocabPayload(item, vocabOffset), vocabAudioKey));
                vocabOffset++;
            }
        }

        // 2. Any remaining vocabulary items not yet placed (if sections list is empty)
        while (vocabOffset < vocabItems.size()) {
            Map<String, Object> item = vocabItems.get(vocabOffset);
            String vocabAudioKey = audioKeyFor(audio, "vocab_" + vocabOffset);
            steps.add(ExperiencePlanStep.withAudio(
                    steps.size(), StepType.VOCABULARY_CARD, buildVocabPayload(item, vocabOffset), vocabAudioKey));
            vocabOffset++;
        }

        // 3. Grammar explanations
        for (int gi = 0; gi < grammarRules.size(); gi++) {
            steps.add(ExperiencePlanStep.of(
                    steps.size(), StepType.GRAMMAR_EXPLANATION,
                    buildGrammarPayload(grammarRules.get(gi), gi)));
        }

        // 4. Vocabulary summary (when >= 3 items were presented)
        if (vocabItems.size() >= 3) {
            steps.add(ExperiencePlanStep.of(
                    steps.size(), StepType.VOCABULARY_SUMMARY,
                    buildVocabSummaryPayload(vocabItems)));
        }

        // 5. Exercises
        for (int ei = 0; ei < exerciseItems.size(); ei++) {
            Map<String, Object> ex = exerciseItems.get(ei);
            StepType exType = resolveExerciseType(ex);
            steps.add(ExperiencePlanStep.of(steps.size(), exType, buildExercisePayload(ex, ei)));
        }

        // 6. Lesson review — always last
        steps.add(ExperiencePlanStep.of(
                steps.size(), StepType.LESSON_REVIEW,
                buildReviewPayload(lesson, exerciseItems.size())));

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

    private Map<String, Object> buildNarrativePayload(Map<String, Object> section, int index) {
        Map<String, Object> p = new HashMap<>(section);
        p.put("sectionIndex", index);
        return p;
    }

    private Map<String, Object> buildVocabPayload(Map<String, Object> item, int index) {
        Map<String, Object> p = new HashMap<>(item);
        p.put("vocabIndex", index);
        return p;
    }

    private Map<String, Object> buildGrammarPayload(Map<String, Object> rule, int index) {
        Map<String, Object> p = new HashMap<>(rule);
        p.put("ruleIndex", index);
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

    private Map<String, Object> buildReviewPayload(CfLesson lesson, int exerciseCount) {
        Map<String, Object> p = new HashMap<>();
        p.put("lessonTitle", lesson.getTitle());
        p.put("cefrLevel", lesson.getCefrLevel());
        p.put("exerciseCount", exerciseCount);
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
