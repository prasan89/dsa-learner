package com.dsalearner.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String name,
        String avatarUrl,
        Instant createdAt
) {}
