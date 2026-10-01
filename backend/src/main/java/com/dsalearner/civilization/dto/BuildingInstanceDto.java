package com.dsalearner.civilization.dto;

import java.util.UUID;

public record BuildingInstanceDto(
        UUID id,
        String buildingType,
        String displayName,
        int level,
        int positionX,
        int positionY,
        String buildState,
        int rotationDeg,
        int widthTiles,
        int heightTiles
) {}
