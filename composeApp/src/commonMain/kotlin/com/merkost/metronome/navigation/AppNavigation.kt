package com.merkost.metronome.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.merkost.metronome.screens.MainScreen
import com.merkost.metronome.screens.SettingsScreen

object MainDestinations {
    const val MAIN = "main"
    const val SETTINGS = "settings"
}

private const val NavDuration = 350

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MainDestinations.MAIN,
    ) {
        composable(MainDestinations.MAIN) {
            MainScreen(onSettingsClicked = { navController.navigate(MainDestinations.SETTINGS) })
        }
        composable(
            MainDestinations.SETTINGS,
            enterTransition = {
                slideInHorizontally(tween(NavDuration)) { it / 3 } + fadeIn(tween(NavDuration))
            },
            exitTransition = {
                slideOutHorizontally(tween(NavDuration)) { it / 3 } + fadeOut(tween(NavDuration))
            },
            popEnterTransition = {
                slideInHorizontally(tween(NavDuration)) { -it / 3 } + fadeIn(tween(NavDuration))
            },
            popExitTransition = {
                slideOutHorizontally(tween(NavDuration)) { it / 3 } + fadeOut(tween(NavDuration))
            },
        ) {
            SettingsScreen(upPress = navController::popBackStack)
        }
    }
}
