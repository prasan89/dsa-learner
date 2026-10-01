package com.dsalearner.academy.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record LessonCompletionResponse(
        UUID lessonId,
        String lessonStatus,
        int score,
        Instant completedAt,
        boolean nextLevelUnlocked,
        String nextCefrLevel,
        long xpEarned,
        long coinsEarned,
        long foodEarned,
        long materialsEarned,
        long civilizationPowerEarned,
        Map<String, Long> newBalances,
        boolean tierUpgraded,
        String newCivilizationTier
) {}
