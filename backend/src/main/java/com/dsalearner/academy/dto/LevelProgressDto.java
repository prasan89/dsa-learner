package com.dsalearner.academy.dto;

import com.dsalearner.academy.model.entity.LearnerLevelProgress;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Read-only DTO for a learner's level progress, safe to expose in REST responses.
 */
public record LevelProgressDto(
        UUID curriculumId,
        String cefrLevel,
        String status,
        int lessonsTotal,
        int lessonsCompleted,
        BigDecimal avgScore,
        Instant unlockedAt,
        Instant completedAt
) {
    public static LevelProgressDto from(LearnerLevelProgress p) {
        return new LevelProgressDto(
                p.getCurriculumId(),
                p.getCefrLevel(),
                p.getStatus(),
                p.getLessonsTotal(),
                p.getLessonsCompleted(),
                p.getAvgScore(),
                p.getUnlockedAt(),
                p.getCompletedAt()
        );
    }
}
