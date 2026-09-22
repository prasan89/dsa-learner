package com.dsalearner.dto.response;

import java.util.UUID;

public record HintResponse(
        UUID id,
        int level,
        String label,
        String content,
        boolean unlocked
) {}
