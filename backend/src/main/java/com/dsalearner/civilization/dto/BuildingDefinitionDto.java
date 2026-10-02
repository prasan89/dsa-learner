package com.dsalearner.civilization.dto;

import java.util.List;
import java.util.UUID;

public record BuildingDefinitionDto(
        UUID id,
        String buildingType,
        String displayName,
        String description,
        int maxLevel,
        String assetRef,
        List<BuildingLevelConfigDto> levels,
        boolean isUnlocked,
        boolean canBuild,
        int currentLevel
) {}
