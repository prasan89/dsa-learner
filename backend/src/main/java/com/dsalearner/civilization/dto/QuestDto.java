package com.dsalearner.civilization.dto;

import java.util.UUID;

public record QuestDto(
        UUID questId,
        String questKey,
        String displayName,
        String description,
        String questType,
        int targetCount,
        int currentCount,
        boolean completed,
        boolean rewardClaimed,
        long xpReward,
        long coinReward,
        long foodReward,
        long materialReward
) {}
