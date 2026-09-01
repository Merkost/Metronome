package com.merkost.metronome.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.merkost.metronome.screens.MainScreen
import com.merkost.metronome.screens.PracticePresetsScreen
import com.merkost.metronome.screens.PracticeSetsScreen
import com.merkost.metronome.screens.SettingsScreen
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

private fun forwardNavigationTransition(): ContentTransform =
    (
        slideInHorizontally(
            animationSpec = spring(
                stiffness = Spring.StiffnessMedium,
                dampingRatio = Spring.DampingRatioMediumBouncy,
            ),
            initialOffsetX = { it / 3 },
        ) + fadeIn(
            animationSpec = spring(
                stiffness = Spring.StiffnessMedium,
                dampingRatio = Spring.DampingRatioMediumBouncy,
            ),
        )
    ) togetherWith (
        slideOutHorizontally(
            animationSpec = spring(
                stiffness = Spring.StiffnessMedium,
                dampingRatio = Spring.DampingRatioMediumBouncy,
            ),
            targetOffsetX = { -it / 5 },
        ) + fadeOut(
            animationSpec = spring(
                stiffness = Spring.StiffnessMedium,
                dampingRatio = Spring.DampingRatioMediumBouncy,
            ),
        )
    )

private fun backwardNavigationTransition(): ContentTransform =
    (
        slideInHorizontally(
            animationSpec = spring(
                stiffness = Spring.StiffnessMedium,
                dampingRatio = Spring.DampingRatioMediumBouncy,
            ),
            initialOffsetX = { -it / 5 },
        ) + fadeIn(
            animationSpec = spring(
                stiffness = Spring.StiffnessMedium,
                dampingRatio = Spring.DampingRatioMediumBouncy,
            ),
        )
    ) togetherWith (
        slideOutHorizontally(
            animationSpec = spring(
                stiffness = Spring.StiffnessMedium,
                dampingRatio = Spring.DampingRatioMediumBouncy,
            ),
            targetOffsetX = { it / 3 },
        ) + fadeOut(
            animationSpec = spring(
                stiffness = Spring.StiffnessMedium,
                dampingRatio = Spring.DampingRatioMediumBouncy,
            ),
        )
    )

@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(
        mainNavigationSavedStateConfiguration,
        MainDestination.Main,
    )
    val navigator = remember(backStack) { AppNavigator(backStack) }

    NavDisplay(
        backStack = backStack,
        onBack = { navigator.goBack() },
        transitionSpec = { forwardNavigationTransition() },
        popTransitionSpec = { backwardNavigationTransition() },
        predictivePopTransitionSpec = { backwardNavigationTransition() },
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
