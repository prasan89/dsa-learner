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
        boolean solved,
        int hintsCount,
        ContentDto content,
        List<FollowupDto> followups
) {
    public record PatternSummary(UUID id, String slug, String name) {}

    public record ContentDto(
            String intuition,
            String bruteForce,
            String bruteTime,
            String bruteSpace,
            String optimalApproach,
            String optimalTime,
            String optimalSpace,
            String pseudocode,
            String javaSolution,
            String commonMistakes,
            String recognitionNote
    ) {}

    public record FollowupDto(String question, String type) {}
}
