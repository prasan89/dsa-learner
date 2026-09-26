package com.dsalearner.academy.dto;

import com.dsalearner.academy.model.domain.ExperiencePlan;
import com.dsalearner.academy.model.domain.ExperiencePlanStep;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Wire DTO representation of an ExperiencePlan for the Academy REST API.
 * Produced by mapping an ExperiencePlan record; safe for JSON serialization.
 */
public record ExperiencePlanDto(
        UUID lessonId,
        UUID lessonVersionId,
        int lessonVersion,
        String lessonTitle,
        String cefrLevel,
        String languageCode,
        String unitDisplayName,
        int totalSteps,
        int totalExercises,
        List<StepDto> steps
) {
    public record StepDto(
            int index,
            String type,
            Map<String, Object> payload,
            String audioKey,
            boolean isExercise
    ) {}

    public static ExperiencePlanDto from(ExperiencePlan plan) {
        List<StepDto> steps = plan.steps().stream()
                .map(s -> new StepDto(
                        s.index(),
                        s.type().name(),
                        s.payload(),
                        s.audioKey(),
                        s.isExercise()
                ))
                .toList();
        return new ExperiencePlanDto(
                plan.lessonId(),
                plan.lessonVersionId(),
                plan.lessonVersion(),
                plan.lessonTitle(),
                plan.cefrLevel(),
                plan.languageCode(),
                plan.unitDisplayName(),
                plan.totalSteps(),
                plan.totalExercises(),
                steps
        );
    }
}
