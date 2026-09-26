package com.dsalearner.academy.dto;

import java.time.Instant;
import java.util.UUID;

public record LessonCompletionResponse(
        UUID lessonId,
        String lessonStatus,
        int score,
        Instant completedAt,
        boolean nextLevelUnlocked,
        String nextCefrLevel
) {}
