package com.dsalearner.civilization.dto;

import java.util.UUID;

public record CityExpansionDto(
        int expansionSlot,
        String displayName,
        String description,
        int gridXOffset,
        int gridYOffset,
        int gridWidth,
        int gridHeight,
        long coinCost,
        long woodCost,
        int requiredLessons,
        long requiredXp,
        String requiredCivTier,
        boolean unlocked,
        boolean canUnlock
) {}
