package com.langoa.app.domain.model

data class Lesson(
    val id: String,
    val title: String,
    val description: String,
    val languageCode: String,
    val unitNumber: Int,
    val lessonNumber: Int,
    val isCompleted: Boolean = false,
    val isLocked: Boolean = false,
    val xpReward: Int = 10,
    val exerciseCount: Int = 0,
    val exercises: List<Exercise> = emptyList()
)
