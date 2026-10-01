package com.dsalearner.civilization.dto;

public record BuildingLevelConfigDto(
        int level,
        String displayName,
        long coinCost,
        long foodCost,
        long materialCost,
        int requiredLessonsCompleted,
        long requiredXp,
        boolean affordable
) {}
