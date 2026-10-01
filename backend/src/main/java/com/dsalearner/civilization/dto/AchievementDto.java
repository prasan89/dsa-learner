package com.dsalearner.civilization.dto;

import java.util.UUID;

public record AchievementDto(
        UUID achievementId,
        String achievementKey,
        String displayName,
        String description,
        String icon,
        int triggerValue,
        long xpReward,
        long coinReward,
        boolean unlocked
) {}
