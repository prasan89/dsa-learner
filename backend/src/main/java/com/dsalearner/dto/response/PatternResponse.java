package com.dsalearner.dto.response;

import java.util.List;
import java.util.UUID;

public record PatternResponse(
        UUID id,
        String slug,
        String name,
        String summary,
        List<String> recognitionClues,
        String templateCode,
        int order
) {}
