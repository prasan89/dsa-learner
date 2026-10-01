package com.dsalearner.academy.dto;

import java.time.Instant;
import java.util.List;
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
        long woodEarned,
        long civilizationPowerEarned,
        Map<String, Long> newBalances,
        boolean tierUpgraded,
        String newCivilizationTier,
        List<String> unlockedBuildingTypes,
        List<String> completedQuestKeys,
        List<String> unlockedAchievementKeys
) {}
