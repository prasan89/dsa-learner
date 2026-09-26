package com.dsalearner.academy.model.domain;

/**
 * Enum of all step types that can appear in an ExperiencePlan.
 * Each type maps to a distinct frontend component in the STEP_REGISTRY.
 */
public enum StepType {
    /** A listen-and-read card presenting the lesson narrative with optional audio. */
    NARRATIVE,
    /** A vocabulary flashcard with target word, translation, and example sentence. */
    VOCABULARY_CARD,
    /** A grammar explanation with rule, examples, and pattern table. */
    GRAMMAR_EXPLANATION,
    /** Multiple-choice exercise with one correct answer. */
    MULTIPLE_CHOICE,
    /** Fill-in-the-blank exercise. */
    FILL_IN_BLANK,
    /** Translation exercise (L1 → L2 or L2 → L1). */
    TRANSLATION,
    /** Listening comprehension exercise — play audio, answer a question. */
    LISTENING,
    /** A mid-lesson summary card consolidating vocabulary introduced so far. */
    VOCABULARY_SUMMARY,
    /** End-of-lesson review card with score and recommendations. */
    LESSON_REVIEW
}
