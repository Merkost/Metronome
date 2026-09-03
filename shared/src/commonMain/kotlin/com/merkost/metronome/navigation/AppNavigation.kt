package com.merkost.metronome.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.merkost.metronome.screens.AnimatedSplash
import com.merkost.metronome.screens.MainScreen
import com.merkost.metronome.screens.PracticePresetsScreen
import com.merkost.metronome.screens.PracticeSetsScreen
import com.merkost.metronome.screens.SettingsScreen
import com.merkost.metronome.ui.AppAnimations
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

internal val mainNavigationSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(baseClass = androidx.navigation3.runtime.NavKey::class) {
            subclass(serializer = MainDestination.Main.serializer())
            subclass(serializer = MainDestination.Settings.serializer())
            subclass(serializer = MainDestination.PracticePresets.serializer())
            subclass(serializer = MainDestination.PracticeSets.serializer())
        }
    }
}

@Composable
fun AppNavigation() {
    var splashComplete by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        AppNavigationHost()
        if (!splashComplete) {
            AnimatedSplash(onFinished = { splashComplete = true })
        }
    }
}

@Composable
private fun AppNavigationHost() {
    val backStack = rememberNavBackStack(
        mainNavigationSavedStateConfiguration,
        MainDestination.Main,
    )
    val navigator = remember(backStack) { AppNavigator(backStack) }

    NavDisplay(
        backStack = backStack,
        onBack = { navigator.goBack() },
        transitionSpec = { AppAnimations.forwardNavigation() },
        popTransitionSpec = { AppAnimations.backwardNavigation() },
        predictivePopTransitionSpec = { AppAnimations.backwardNavigation() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<MainDestination.Main> {
                MainScreen(
                    onSettingsClicked = { navigator.navigate(MainDestination.Settings) },
                    onPresetsClicked = { navigator.navigate(MainDestination.PracticePresets) },
                    onPracticeSetsClicked = { navigator.navigate(MainDestination.PracticeSets) },
                )
            }
            entry<MainDestination.PracticePresets> {
                PracticePresetsScreen(
                    upPress = { navigator.goBack() },
                    onOpenPracticeSets = {
                        navigator.navigateBackToOrPush(MainDestination.PracticeSets)
                    },
                )
            }
            entry<MainDestination.PracticeSets> {
                PracticeSetsScreen(
                    upPress = { navigator.goBack() },
                    onStart = { navigator.returnToMain() },
                    onOpenPresets = { navigator.navigate(MainDestination.PracticePresets) },
                )
            }
            entry<MainDestination.Settings> {
                SettingsScreen(upPress = { navigator.goBack() })
            }
        },
    )
}
