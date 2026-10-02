package com.langoa.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.langoa.app.auth.AuthEvent
import com.langoa.app.auth.AuthEventBus
import com.langoa.app.ui.screens.auth.LoginScreen
import com.langoa.app.ui.screens.build.BuildScreen
import com.langoa.app.ui.screens.home.HomeScreen
import com.langoa.app.ui.screens.language.LanguagePickerScreen
import com.langoa.app.ui.screens.learn.LearnScreen
import com.langoa.app.ui.screens.learn.LessonOverviewScreen
import com.langoa.app.ui.screens.learn.LessonScreen
import com.langoa.app.ui.screens.learn.RewardScreen
import com.langoa.app.ui.screens.paywall.PaywallScreen
import com.langoa.app.ui.screens.profile.ProfileScreen
import com.langoa.app.ui.screens.splash.SplashScreen
import com.langoa.app.ui.screens.world.WorldScreen
import com.langoa.app.ui.screens.shop.CoinShopScreen

@Composable
fun LangoaNavGraph(authEventBus: AuthEventBus) {
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        authEventBus.events.collect { event ->
            when (event) {
                is AuthEvent.SessionExpired -> {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val mainScreenRoutes = setOf(
        "home/{languageCode}",
        "learn/{languageCode}",
        "build/{languageCode}",
        Screen.World.route,
        "profile/{languageCode}"
    )

    val showBottomBar = currentDestination?.route in mainScreenRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val languageCode = navBackStackEntry?.arguments?.getString("languageCode") ?: "de"

                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentDestination?.hierarchy?.any { it.route == Screen.Home.route } == true,
                        onClick = {
                            navController.navigate(Screen.Home.createRoute(languageCode)) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Language, contentDescription = "Learn") },
                        label = { Text("Learn") },
                        selected = currentDestination?.hierarchy?.any { it.route == Screen.Learn.route } == true,
                        onClick = {
                            navController.navigate(Screen.Learn.createRoute(languageCode)) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Build, contentDescription = "Build") },
                        label = { Text("Build") },
                        selected = currentDestination?.hierarchy?.any { it.route == Screen.Build.route } == true,
                        onClick = {
                            navController.navigate(Screen.Build.createRoute(languageCode)) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Public, contentDescription = "World") },
                        label = { Text("World") },
                        selected = currentDestination?.hierarchy?.any { it.route == Screen.World.route } == true,
                        onClick = {
                            navController.navigate(Screen.World.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        selected = currentDestination?.hierarchy?.any { it.route == Screen.Profile.route } == true,
                        onClick = {
                            navController.navigate(Screen.Profile.createRoute(languageCode)) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToHome = { languageCode ->
                        navController.navigate(Screen.Home.createRoute(languageCode)) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToLanguagePicker = {
                        navController.navigate(Screen.LanguagePicker.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.LanguagePicker.route) {
                LanguagePickerScreen(
                    onLanguageSelected = { languageCode ->
                        navController.navigate(Screen.Home.createRoute(languageCode)) {
                            popUpTo(Screen.LanguagePicker.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = { languageCode ->
                        if (languageCode != null) {
                            navController.navigate(Screen.Home.createRoute(languageCode)) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.LanguagePicker.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(
                route = Screen.Home.route,
                arguments = listOf(navArgument("languageCode") { type = NavType.StringType })
            ) { backStackEntry ->
                val languageCode = backStackEntry.arguments?.getString("languageCode") ?: "de"
                HomeScreen(
                    languageCode = languageCode,
                    onNavigateToLesson = { lessonId ->
                        navController.navigate(Screen.Lesson.createRoute(languageCode, lessonId))
                    }
                )
            }

            composable(
                route = Screen.Learn.route,
                arguments = listOf(navArgument("languageCode") { type = NavType.StringType })
            ) { backStackEntry ->
                val languageCode = backStackEntry.arguments?.getString("languageCode") ?: "de"
                LearnScreen(
                    languageCode = languageCode,
                    onLessonClick = { lessonId ->
                        navController.navigate(Screen.LessonOverview.createRoute(languageCode, lessonId))
                    },
                    onPremiumLessonTap = {
                        navController.navigate(Screen.Paywall.route)
                    }
                )
            }

            composable(
                route = Screen.LessonOverview.route,
                arguments = listOf(
                    navArgument("languageCode") { type = NavType.StringType },
                    navArgument("lessonId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val languageCode = backStackEntry.arguments?.getString("languageCode") ?: "de"
                val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
                LessonOverviewScreen(
                    languageCode = languageCode,
                    lessonId = lessonId,
                    onBack = { navController.popBackStack() },
                    onStartLesson = {
                        navController.navigate(Screen.Lesson.createRoute(languageCode, lessonId))
                    }
                )
            }

            composable(
                route = Screen.Lesson.route,
                arguments = listOf(
                    navArgument("languageCode") { type = NavType.StringType },
                    navArgument("lessonId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val languageCode = backStackEntry.arguments?.getString("languageCode") ?: "de"
                val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
                LessonScreen(
                    languageCode = languageCode,
                    lessonId = lessonId,
                    onLessonComplete = { _ ->
                        navController.navigate(Screen.Reward.createRoute(languageCode, lessonId)) {
                            popUpTo(Screen.Lesson.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Reward.route,
                arguments = listOf(
                    navArgument("languageCode") { type = NavType.StringType },
                    navArgument("lessonId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val languageCode = backStackEntry.arguments?.getString("languageCode") ?: "de"
                val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
                RewardScreen(
                    languageCode = languageCode,
                    lessonId = lessonId,
                    onContinue = {
                        navController.navigate(Screen.Learn.createRoute(languageCode)) {
                            popUpTo(Screen.Reward.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Screen.Build.route,
                arguments = listOf(navArgument("languageCode") { type = NavType.StringType })
            ) { backStackEntry ->
                val languageCode = backStackEntry.arguments?.getString("languageCode") ?: "de"
                BuildScreen(languageCode = languageCode)
            }

            composable(Screen.World.route) {
                WorldScreen()
            }

            composable(
                route = Screen.Profile.route,
                arguments = listOf(navArgument("languageCode") { type = NavType.StringType })
            ) { backStackEntry ->
                val languageCode = backStackEntry.arguments?.getString("languageCode") ?: "de"
                ProfileScreen(
                    languageCode = languageCode,
                    onNavigateToPaywall = {
                        navController.navigate(Screen.Paywall.route)
                    },
                    onNavigateToCoinShop = {
                        navController.navigate(Screen.CoinShop.createRoute(languageCode))
                    }
                )
            }

            composable(
                route = Screen.CoinShop.route,
                arguments = listOf(navArgument("languageCode") { type = NavType.StringType })
            ) { backStackEntry ->
                val languageCode = backStackEntry.arguments?.getString("languageCode") ?: "de"
                CoinShopScreen(
                    languageCode = languageCode,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Paywall.route) {
                val languageCode = navBackStackEntry?.arguments?.getString("languageCode") ?: "de"
                PaywallScreen(
                    onBack = { navController.popBackStack() },
                    onUpgradeSuccess = {
                        navController.navigate(Screen.Learn.createRoute(languageCode)) {
                            popUpTo(Screen.Paywall.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
