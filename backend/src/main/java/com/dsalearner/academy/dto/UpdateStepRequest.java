package com.dsalearner.academy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateStepRequest(
        @NotNull @Min(0) Integer stepIndex
) {}
