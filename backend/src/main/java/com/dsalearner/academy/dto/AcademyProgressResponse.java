package com.dsalearner.academy.dto;

import java.util.List;
import java.util.UUID;

public record AcademyProgressResponse(
        UUID curriculumId,
        String languageCode,
        long totalLessonsCompleted,
        List<LevelProgressDto> levels
) {}
