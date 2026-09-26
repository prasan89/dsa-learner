package com.dsalearner.academy.dto;

import java.util.List;
import java.util.UUID;

public record AcademyCurriculumResponse(
        UUID curriculumId,
        String languageCode,
        String displayName,
        List<LevelSummaryDto> levels
) {}
