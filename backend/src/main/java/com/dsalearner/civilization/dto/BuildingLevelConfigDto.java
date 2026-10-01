package com.dsalearner.civilization.dto;

public record BuildingLevelConfigDto(
        int level,
        String displayName,
        long coinCost,
        long foodCost,
        long materialCost,
        long woodCost,
        int requiredLessonsCompleted,
        long requiredXp,
        boolean affordable
) {}
