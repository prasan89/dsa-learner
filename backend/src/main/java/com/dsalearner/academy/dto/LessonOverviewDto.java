package com.dsalearner.academy.dto;

import com.dsalearner.pipeline.model.entity.CfCurriculumLessonPlan;
import com.dsalearner.pipeline.model.entity.CfLesson;
import com.dsalearner.pipeline.model.entity.CfLessonVersion;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record LessonOverviewDto(
        UUID lessonId,
        String stableRef,
        String title,
        String cefrLevel,
        String languageCode,
        String unitDisplayName,
        int estimatedMinutes,
        String canDo,
        List<String> objectives,
        List<String> skillFocus,
        long xpReward,
        long coinsReward,
        int exerciseCount
) {
    @SuppressWarnings("unchecked")
    public static LessonOverviewDto from(CfLesson lesson, CfLessonVersion version, CfCurriculumLessonPlan plan) {
        Map<String, Object> content = version.getContent() != null ? version.getContent() : Map.of();
        Map<String, Object> exercises = version.getExercises() != null ? version.getExercises() : Map.of();

        Map<String, Object> metadata = content.containsKey("metadata")
                ? (Map<String, Object>) content.get("metadata") : Map.of();

        int estimatedMinutes = metadata.containsKey("estimatedMinutes")
                ? ((Number) metadata.get("estimatedMinutes")).intValue() : 15;
        String canDo = metadata.containsKey("canDo") ? (String) metadata.get("canDo") : "";

        List<String> objectives = content.containsKey("objectives")
                ? (List<String>) content.get("objectives") : List.of();

        Map<String, Object> rewards = content.containsKey("rewards")
                ? (Map<String, Object>) content.get("rewards") : Map.of();
        long xpReward = rewards.containsKey("xp") ? ((Number) rewards.get("xp")).longValue() : 100L;
        long coinsReward = rewards.containsKey("coins") ? ((Number) rewards.get("coins")).longValue() : 50L;

        List<Map<String, Object>> exerciseItems = exercises.containsKey("items")
                ? (List<Map<String, Object>>) exercises.get("items") : List.of();

        List<String> skillFocus = lesson.getSkillFocus() != null ? List.of(lesson.getSkillFocus()) : List.of();

        return new LessonOverviewDto(
                lesson.getId(),
                lesson.getStableRef(),
                lesson.getTitle(),
                lesson.getCefrLevel(),
                lesson.getLanguageCode(),
                plan != null ? plan.getUnitDisplayName() : null,
                estimatedMinutes,
                canDo,
                objectives,
                skillFocus,
                xpReward,
                coinsReward,
                exerciseItems.size()
        );
    }
}
