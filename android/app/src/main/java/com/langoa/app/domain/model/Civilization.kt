package com.langoa.app.domain.model

data class Civilization(
    val id: String,
    val languageCode: String,
    val name: String,
    val civilizationTier: String,
    val tierLevel: Int,
    val totalXp: Long,
    val totalLessonsCompleted: Int,
    val balances: Map<String, Long>,
    val buildings: List<Building> = emptyList(),
    val decorations: List<Decoration> = emptyList(),
    val unlockedExpansionSlots: List<Int> = emptyList()
) {
    val coins: Long get() = balances["COINS"] ?: 0L
    val food: Long get() = balances["FOOD"] ?: 0L
    val materials: Long get() = balances["MATERIALS"] ?: 0L
    val wood: Long get() = balances["WOOD"] ?: 0L
    val gems: Long get() = balances["GEMS"] ?: 0L
    val xp: Long get() = balances["XP"] ?: 0L
    val civilizationPower: Long get() = balances["CIVILIZATION_POWER"] ?: 0L
}
