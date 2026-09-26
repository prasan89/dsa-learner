package com.dsalearner.pipeline.curriculum.agent;

import java.util.List;

/**
 * The blueprint output for one CEFR level.
 */
public record LevelBlueprint(
        String cefrLevel,
        String displayName,
        String rationale,
        List<UnitBlueprint> units
) {
    public int totalLessons() {
        return units == null ? 0
                : units.stream().mapToInt(u -> u.lessons() == null ? 0 : u.lessons().size()).sum();
    }
}
