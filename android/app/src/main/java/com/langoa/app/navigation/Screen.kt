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
    object LessonOverview : Screen("lesson_overview/{languageCode}/{lessonId}") {
        fun createRoute(languageCode: String, lessonId: String) = "lesson_overview/$languageCode/$lessonId"
    }
    object Reward : Screen("reward/{languageCode}/{lessonId}") {
        fun createRoute(languageCode: String, lessonId: String) = "reward/$languageCode/$lessonId"
    }
    object Build : Screen("build/{languageCode}") {
        fun createRoute(languageCode: String) = "build/$languageCode"
    }
    object World : Screen("world")
    object Profile : Screen("profile/{languageCode}") {
        fun createRoute(languageCode: String) = "profile/$languageCode"
    }
    object Paywall : Screen("paywall")
    object CoinShop : Screen("coin_shop/{languageCode}") {
        fun createRoute(languageCode: String) = "coin_shop/$languageCode"
    }
}
