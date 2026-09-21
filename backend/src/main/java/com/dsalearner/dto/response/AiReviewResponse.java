package com.dsalearner.dto.response;

public record AiReviewResponse(
        String timeComplexity,
        String spaceComplexity,
        String strengths,
        String improvements,
        String patternUsed,
        String optimizedApproach
) {}
