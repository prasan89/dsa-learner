package com.langoa.app.domain.model

data class Building(
    val id: String,
    val buildingType: String,
    val name: String,
    val level: Int,
    val isUnlocked: Boolean,
    val coinCost: Int,
    val foodCost: Int,
    val materialsCost: Int,
    val civPowerGrant: Int,
    val description: String = "",
    val xpRequirement: Int = 0
)
