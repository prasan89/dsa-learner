package com.dsalearner.academy.dto;

import com.dsalearner.academy.model.entity.LearnerLessonProgress;

import java.time.Instant;
import java.util.UUID;

/**
 * Read-only DTO for a learner's lesson progress, safe to expose in REST responses.
 */
public record LessonProgressDto(
        UUID lessonId,
        String status,
        int stepIndex,
        Integer score,
        Instant startedAt,
        Instant completedAt,
        Instant lastInteractionAt
) {
    public static LessonProgressDto from(LearnerLessonProgress p) {
        return new LessonProgressDto(
                p.getLessonId(),
                p.getStatus(),
                p.getStepIndex(),
                p.getScore() != null ? p.getScore().intValue() : null,
                p.getStartedAt(),
                p.getCompletedAt(),
                p.getLastInteractionAt()
        );
    }
}
