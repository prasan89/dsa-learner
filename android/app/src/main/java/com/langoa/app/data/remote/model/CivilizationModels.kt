package com.langoa.app.data.remote.model

data class CivilizationStateResponse(
    val id: String,
    val userId: String,
    val languageCode: String,
    val name: String,
    val totalXp: Int,
    val level: Int,
    val coins: Int,
    val food: Int,
    val materials: Int,
    val civPower: Int,
    val population: Int,
    val buildings: List<BuildingInstanceDto>
)

data class BuildingInstanceDto(
    val id: String,
    val buildingType: String,
    val name: String,
    val level: Int,
    val isUnlocked: Boolean,
    val coinCost: Int,
    val foodCost: Int,
    val materialsCost: Int,
    val civPowerGrant: Int,
    val description: String,
    val xpRequirement: Int
)

data class BuildBuildingRequest(
    val languageCode: String,
    val buildingType: String
)

data class LessonRewardResponse(
    val lessonId: String,
    val xpEarned: Int,
    val coinsEarned: Int,
    val foodEarned: Int,
    val materialsEarned: Int,
    val civPowerEarned: Int,
    val isPerfect: Boolean,
    val streakBonus: Boolean
)

data class AvailableBuildingsResponse(
    val languageCode: String,
    val availableBuildings: List<BuildingInstanceDto>
)
