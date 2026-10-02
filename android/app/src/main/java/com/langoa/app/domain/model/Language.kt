package com.langoa.app.domain.model

data class Language(
    val code: String,
    val displayName: String,
    val flagEmoji: String,
    val isAvailable: Boolean,
    val lessonCount: Int = 0
)

val SUPPORTED_LANGUAGES = listOf(
    Language("de", "German", "🇩🇪", true),
    Language("hi", "Hindi", "🇮🇳", true),
    Language("kn", "Kannada", "🇮🇳", true)
)
