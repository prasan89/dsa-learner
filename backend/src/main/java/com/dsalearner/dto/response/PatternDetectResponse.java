package com.dsalearner.dto.response;

public record PatternDetectResponse(
        String patternName,
        String patternSlug,
        String explanation,
        String confidence
) {}
