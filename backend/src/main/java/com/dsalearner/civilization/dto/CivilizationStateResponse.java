package com.dsalearner.civilization.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CivilizationStateResponse(
        UUID civilizationId,
        String languageCode,
        String name,
        String tier,
        int tierLevel,
        long totalXp,
        int totalLessonsCompleted,
        Map<String, Long> balances,
        List<BuildingInstanceDto> buildings
) {}
