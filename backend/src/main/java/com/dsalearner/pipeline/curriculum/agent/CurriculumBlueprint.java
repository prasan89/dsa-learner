package com.dsalearner.pipeline.curriculum.agent;

import java.util.List;

/**
 * Full curriculum blueprint output from the BlueprintAgent.
 */
public record CurriculumBlueprint(
        String curriculumDisplayName,
        String languageCode,
        List<LevelBlueprint> levels
) {
    public int totalLessons() {
        return levels == null ? 0 : levels.stream().mapToInt(LevelBlueprint::totalLessons).sum();
    }
}
