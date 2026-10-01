package com.dsalearner.civilization.dto;

import java.util.UUID;

public record DecorationDefinitionDto(
        UUID id,
        String decorationType,
        String displayName,
        String description,
        long coinCost,
        long woodCost,
        String requiredCivTier,
        boolean isPremium,
        int widthTiles,
        int heightTiles
) {}
