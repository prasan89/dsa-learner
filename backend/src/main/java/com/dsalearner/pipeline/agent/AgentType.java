package com.dsalearner.pipeline.agent;

/**
 * All known agent type identifiers. Used in routing, run records, and prompt resolution.
 */
public final class AgentType {
    public static final String LESSON_PLANNER        = "lesson_planner";
    public static final String CONTENT_GENERATOR     = "content_generator";
    public static final String VOCABULARY_AGENT      = "vocabulary";
    public static final String GRAMMAR_AGENT         = "grammar";
    public static final String DIALOGUE_AGENT        = "dialogue";
    public static final String EXERCISE_GENERATOR    = "exercise_generator";

    // QA agents
    public static final String LINGUISTIC_QA         = "linguistic_qa";   // language-specific, prefixed: de_linguistic_qa
    public static final String CEFR_QA               = "cefr_qa";
    public static final String PEDAGOGY_QA           = "pedagogy_qa";
    public static final String EXERCISE_QA           = "exercise_qa";
    public static final String CONSISTENCY_QA        = "consistency_qa";

    // Revision
    public static final String REVISION_GENERATOR   = "revision_generator";

    // Post-QA
    public static final String EDITOR                = "editor";
    public static final String FINAL_GATE            = "final_gate";
    public static final String PUBLISHER             = "publisher";

    private AgentType() {}
}
