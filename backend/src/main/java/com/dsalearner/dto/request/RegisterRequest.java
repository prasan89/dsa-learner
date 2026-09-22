package com.dsalearner.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 2, max = 100) String name,
        @NotBlank @Email String email,
        @NotBlank
        @Size(min = 8, max = 100, message = "Password must be 8–100 characters")
        @Pattern(
            regexp = ".*\\d.*",
            message = "Password must contain at least one number"
        )
        String password
) {}
