package com.dsalearner.academy.dto;

import com.dsalearner.academy.model.domain.ExperiencePlan;
import com.dsalearner.academy.model.entity.LearnerLessonProgress;

import java.time.Instant;
import java.util.UUID;

public record AcademyLessonResponse(
        UUID lessonId,
        String title,
        String cefrLevel,
        String languageCode,
        String unitDisplayName,
        String learnerStatus,
        int currentStepIndex,
        Integer score,
        Instant startedAt,
        Instant completedAt,
        ExperiencePlanDto experiencePlan
) {
    public static AcademyLessonResponse from(ExperiencePlan plan, LearnerLessonProgress progress) {
        return new AcademyLessonResponse(
                plan.lessonId(),
                plan.lessonTitle(),
                plan.cefrLevel(),
                plan.languageCode(),
                plan.unitDisplayName(),
                progress.getStatus(),
                progress.getStepIndex(),
                progress.getScore() != null ? (int) progress.getScore() : null,
                progress.getStartedAt(),
                progress.getCompletedAt(),
                ExperiencePlanDto.from(plan)
        );
    }
}
