package com.langoa.app.domain.model

data class LessonReward(
    val lessonId: String,
    val xpEarned: Int,
    val coinsEarned: Int,
    val foodEarned: Int,
    val materialsEarned: Int,
    val civPowerEarned: Int,
    val isPerfect: Boolean = false,
    val streakBonus: Boolean = false
)
