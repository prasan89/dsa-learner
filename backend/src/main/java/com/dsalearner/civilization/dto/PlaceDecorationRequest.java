package com.dsalearner.civilization.dto;

public record PlaceDecorationRequest(
        String decorationType,
        int positionX,
        int positionY,
        int rotationDeg
) {}
