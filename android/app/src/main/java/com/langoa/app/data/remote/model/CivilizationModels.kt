package com.langoa.app.data.remote.model

data class CivilizationStateResponse(
    val id: String,
    val languageCode: String,
    val name: String,
    val civilizationTier: String,
    val tierLevel: Int,
    val totalXp: Long,
    val totalLessonsCompleted: Int,
    val balances: Map<String, Long>,
    val buildings: List<BuildingInstanceDto>,
    val decorations: List<DecorationInstanceDto> = emptyList(),
    val unlockedExpansionSlots: List<Int> = emptyList()
)

data class BuildingInstanceDto(
    val id: String,
    val buildingType: String,
    val displayName: String,
    val level: Int,
    val positionX: Int,
    val positionY: Int,
    val buildState: String = "BUILT",
    val rotationDeg: Int = 0,
    val widthTiles: Int = 1,
    val heightTiles: Int = 1
)

data class DecorationInstanceDto(
    val id: String,
    val decorationType: String,
    val displayName: String,
    val positionX: Int,
    val positionY: Int,
    val rotationDeg: Int = 0
)

data class BuildBuildingRequest(
    val buildingType: String,
    val positionX: Int,
    val positionY: Int
)

data class UpgradeBuildingRequest(
    val buildingInstanceId: String
)

data class MoveBuildingRequest(
    val positionX: Int,
    val positionY: Int,
    val rotationDeg: Int = 0
)

data class LessonCompletionResponse(
    val lessonId: String,
    val lessonStatus: String,
    val score: Int,
    val completedAt: String? = null,
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
    val milestoneCoinsEarned: Long = 0
)

data class BuildingLevelConfigDto(
    val level: Int,
    val coinCost: Long,
    val foodCost: Long,
    val materialCost: Long,
    val woodCost: Long,
    val requiredLessonsCompleted: Int,
    val requiredXp: Long
)

data class BuildingDefinitionDto(
    val buildingType: String,
    val displayName: String,
    val description: String,
    val maxLevel: Int,
    val isUnlocked: Boolean,
    val canBuild: Boolean,
    val currentLevel: Int,
    val levels: List<BuildingLevelConfigDto>
)

data class QuestDto(
    val questId: String,
    val questKey: String,
    val displayName: String,
    val description: String,
    val questType: String,
    val targetCount: Int,
    val currentCount: Int,
    val completed: Boolean,
    val rewardClaimed: Boolean,
    val rewards: Map<String, Long>
)

data class AchievementDto(
    val achievementId: String,
    val achievementKey: String,
    val displayName: String,
    val description: String,
    val icon: String? = null,
    val triggerValue: Int,
    val xpReward: Long,
    val coinReward: Long,
    val unlocked: Boolean
)
