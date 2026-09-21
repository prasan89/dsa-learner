package com.dsalearner.dto.response;

import java.util.UUID;

public record PatternMasteryResponse(
        UUID patternId,
        String patternSlug,
        String patternName,
        String status,
        double masteryScore,
        int problemsSolved
) {}
