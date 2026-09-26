package com.dsalearner.academy.dto;

import java.util.List;
import java.util.UUID;

public record UnitDto(
        UUID unitId,
        String displayName,
        int ordinal,
        List<LessonSummaryDto> lessons
) {}
