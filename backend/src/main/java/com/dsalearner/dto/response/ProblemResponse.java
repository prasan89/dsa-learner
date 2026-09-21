package com.dsalearner.dto.response;

import com.dsalearner.model.enums.Difficulty;

import java.util.List;
import java.util.UUID;

public record ProblemResponse(
        UUID id,
        String slug,
        String title,
        Difficulty difficulty,
        String description,
        String constraints,
        String examples,
        List<String> tags,
        List<PatternSummary> patterns,
        boolean solved
) {
    public record PatternSummary(UUID id, String slug, String name) {}
}
