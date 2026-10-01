package com.dsalearner.civilization.dto;

public record MoveBuildingRequest(
        int positionX,
        int positionY,
        int rotationDeg
) {}
