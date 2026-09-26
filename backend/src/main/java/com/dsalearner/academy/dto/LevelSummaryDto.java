package com.dsalearner.academy.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record LevelSummaryDto(
        String cefrLevel,
        String displayName,
        int ordinal,
        String status,
        int lessonsTotal,
        int lessonsCompleted,
        BigDecimal avgScore,
        Instant unlockedAt,
        Instant completedAt,
        List<UnitDto> units
) {}
