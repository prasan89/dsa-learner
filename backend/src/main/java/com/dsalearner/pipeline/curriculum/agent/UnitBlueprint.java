package com.dsalearner.pipeline.curriculum.agent;

import java.util.List;

/**
 * A unit within a level in the blueprint.
 */
public record UnitBlueprint(
        int ordinal,
        String label,
        String theme,
        String learningGoal,
        List<LessonPlanSlot> lessons
) {}
