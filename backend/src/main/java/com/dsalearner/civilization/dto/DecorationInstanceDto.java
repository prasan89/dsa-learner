package com.dsalearner.civilization.dto;

import java.util.UUID;

public record DecorationInstanceDto(
        UUID id,
        String decorationType,
        String displayName,
        int positionX,
        int positionY,
        int rotationDeg
) {}
