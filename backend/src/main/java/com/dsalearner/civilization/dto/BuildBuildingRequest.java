package com.dsalearner.civilization.dto;

import jakarta.validation.constraints.NotBlank;

public record BuildBuildingRequest(
        @NotBlank String buildingType,
        int positionX,
        int positionY
) {}
