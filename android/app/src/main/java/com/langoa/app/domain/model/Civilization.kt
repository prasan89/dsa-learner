package com.langoa.app.domain.model

data class Civilization(
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
    val buildings: List<Building> = emptyList(),
    val population: Int = 0
)
