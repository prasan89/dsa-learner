package com.dsalearner.dto.response;

import com.dsalearner.model.enums.Difficulty;

import java.util.List;
import java.util.UUID;

public record ProblemSummaryResponse(
        UUID id,
        String slug,
        String title,
        Difficulty difficulty,
        List<String> tags,
        List<ProblemResponse.PatternSummary> patterns,
        double acceptanceRate,
        boolean solved,
        boolean locked
) {}
