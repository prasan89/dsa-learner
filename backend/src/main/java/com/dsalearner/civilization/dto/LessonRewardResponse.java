package com.dsalearner.civilization.dto;

import java.util.List;
import java.util.Map;

public record LessonRewardResponse(
        long xpEarned,
        long coinsEarned,
        long foodEarned,
        long materialsEarned,
        long civilizationPowerEarned,
        Map<String, Long> newBalances,
        String newTier,
        boolean tierUpgraded,
        List<String> unlockedBuildingTypes
) {}
