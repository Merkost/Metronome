package com.merkost.metronome.navigation

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
        composable(MainDestinations.SETTINGS) {
            SettingsScreen(upPress = navController::popBackStack)
        }
    }
}
