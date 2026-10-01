package com.langoa.app.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object LanguagePicker : Screen("language_picker")
    object Login : Screen("login")
    object Home : Screen("home/{languageCode}") {
        fun createRoute(languageCode: String) = "home/$languageCode"
    }
    object Learn : Screen("learn/{languageCode}") {
        fun createRoute(languageCode: String) = "learn/$languageCode"
    }
    object Lesson : Screen("lesson/{languageCode}/{lessonId}") {
        fun createRoute(languageCode: String, lessonId: String) = "lesson/$languageCode/$lessonId"
    }
    object Reward : Screen("reward/{languageCode}/{lessonId}") {
        fun createRoute(languageCode: String, lessonId: String) = "reward/$languageCode/$lessonId"
    }
    object Build : Screen("build/{languageCode}") {
        fun createRoute(languageCode: String) = "build/$languageCode"
    }
    object World : Screen("world")
    object Profile : Screen("profile")
}
