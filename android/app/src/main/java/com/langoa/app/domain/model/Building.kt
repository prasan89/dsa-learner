package com.langoa.app.domain.model

data class Building(
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
