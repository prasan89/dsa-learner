package com.dsalearner.pipeline.curriculum.agent;

import java.util.List;

/**
 * A single lesson slot in the blueprint.
 */
public record LessonPlanSlot(
        int position,
        String stableRef,
        String title,
        String topic,
        String lessonType,
        String difficulty,
        List<String> skillFocus,
        String learningObjectives,
        String communicationGoals,
        List<String> grammarHints,
        List<String> vocabHints,
        List<String> prerequisiteStableRefs,
        List<String> reviewTargets
) {}
