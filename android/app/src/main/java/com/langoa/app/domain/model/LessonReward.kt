package com.langoa.app.domain.model

data class LessonReward(
    val lessonId: String,
    val lessonStatus: String,
    val score: Int,
    val nextLevelUnlocked: Boolean = false,
    val nextCefrLevel: String? = null,
    val xpEarned: Long = 0,
    val coinsEarned: Long = 0,
    val foodEarned: Long = 0,
    val materialsEarned: Long = 0,
    val woodEarned: Long = 0,
    val civilizationPowerEarned: Long = 0,
    val newBalances: Map<String, Long> = emptyMap(),
    val tierUpgraded: Boolean = false,
    val newTier: String? = null,
    val unlockedBuildingTypes: List<String> = emptyList(),
    val completedQuestKeys: List<String> = emptyList(),
    val unlockedAchievementKeys: List<String> = emptyList(),
    // True when the lesson was completed offline — reward is an estimate pending server confirmation
    val isPending: Boolean = false
)
