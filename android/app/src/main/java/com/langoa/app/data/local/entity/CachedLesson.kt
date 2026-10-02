package com.langoa.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_lessons")
data class CachedLesson(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val languageCode: String,
    val unitNumber: Int,
    val lessonNumber: Int,
    val isCompleted: Boolean,
    val isLocked: Boolean,
    val xpReward: Int,
    val exerciseCount: Int,
    val exercisesJson: String = "",
    val cachedAt: Long = System.currentTimeMillis(),
    val cachedExercisesAt: Long = 0L
)
