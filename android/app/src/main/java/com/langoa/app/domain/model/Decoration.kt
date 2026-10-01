package com.langoa.app.domain.model

data class Decoration(
    val id: String,
    val decorationType: String,
    val displayName: String,
    val positionX: Int,
    val positionY: Int,
    val rotationDeg: Int = 0
)
