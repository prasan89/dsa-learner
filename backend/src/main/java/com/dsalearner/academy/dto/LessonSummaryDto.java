package com.dsalearner.academy.dto;

import java.util.UUID;

public record LessonSummaryDto(
        UUID lessonId,
        String title,
        String status,
        int position,
        int stepIndex,
        Integer score
) {}
