package com.langoa.app.data.remote.model

data class CurriculumDto(
    val languageCode: String,
    val units: List<UnitDto>
)

data class UnitDto(
    val id: String,
    val title: String,
    val description: String,
    val unitNumber: Int,
    val lessons: List<LessonDto>
)

data class LessonDto(
    val id: String,
    val title: String,
    val description: String,
    val lessonNumber: Int,
    val unitNumber: Int,
    val languageCode: String,
    val isCompleted: Boolean,
    val isLocked: Boolean,
    val xpReward: Int,
    val exerciseCount: Int
)

data class LessonDetailDto(
    val id: String,
    val title: String,
    val description: String,
    val languageCode: String,
    val unitNumber: Int,
    val lessonNumber: Int,
    val exercises: List<ExerciseDto>
)

data class ExerciseDto(
    val id: String,
    val type: String,
    val question: String,
    val options: List<String>?,
    val correctAnswer: String,
    val explanation: String?,
    val hint: String?,
    val wordBankItems: List<String>?,
    val prompt: String?
)

data class LessonCompletionRequest(
    val score: Int
)
