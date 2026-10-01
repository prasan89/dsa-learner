package com.langoa.app.domain.model

data class BuildingLevelConfig(
    val level: Int,
    val coinCost: Long,
    val foodCost: Long,
    val materialCost: Long,
    val woodCost: Long,
    val requiredLessonsCompleted: Int,
    val requiredXp: Long
)

data class BuildingDefinition(
    val buildingType: String,
    val displayName: String,
    val description: String,
    val maxLevel: Int,
    val isUnlocked: Boolean,
    val canBuild: Boolean,
    val currentLevel: Int,
    val levels: List<BuildingLevelConfig>
) {
    val nextLevelConfig: BuildingLevelConfig?
        get() = levels.firstOrNull { it.level == currentLevel + 1 }
}
