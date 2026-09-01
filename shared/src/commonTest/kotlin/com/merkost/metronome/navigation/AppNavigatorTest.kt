package com.merkost.metronome.navigation

import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
class AppNavigatorTest {
    @Test
    fun everyDestinationHasASavedStateSerializer() {
        val destinations = listOf<NavKey>(
            MainDestination.Main,
            MainDestination.Settings,
            MainDestination.PracticePresets,
            MainDestination.PracticeSets,
        )

        destinations.forEach { destination ->
            assertNotNull(
                mainNavigationSavedStateConfiguration.serializersModule.getPolymorphic(
                    NavKey::class,
                    destination,
                ),
            )
        }
    }

    @Test
    fun navigationPushesTypedDestinationsWithoutDuplicatingTheTopEntry() {
        val backStack = mutableListOf<NavKey>(MainDestination.Main)
        val navigator = AppNavigator(backStack)

        navigator.navigate(MainDestination.Settings)
        navigator.navigate(MainDestination.Settings)

        assertEquals(
            listOf<NavKey>(MainDestination.Main, MainDestination.Settings),
            backStack,
        )
    }

    @Test
    fun backNeverRemovesTheRootDestination() {
        val backStack = mutableListOf<NavKey>(
            MainDestination.Main,
            MainDestination.PracticeSets,
        )
        val navigator = AppNavigator(backStack)

        assertTrue(navigator.goBack())
        assertFalse(navigator.goBack())
        assertEquals(listOf<NavKey>(MainDestination.Main), backStack)
    }

    @Test
    fun returnToMainClearsTheNestedPracticeFlow() {
        val backStack = mutableListOf<NavKey>(
            MainDestination.Main,
            MainDestination.PracticeSets,
            MainDestination.PracticePresets,
        )
        val navigator = AppNavigator(backStack)

        navigator.returnToMain()

        assertEquals(listOf<NavKey>(MainDestination.Main), backStack)
    }

    @Test
    fun resolutionNavigationReturnsToAnExistingDestination() {
        val backStack = mutableListOf<NavKey>(
            MainDestination.Main,
            MainDestination.PracticeSets,
            MainDestination.PracticePresets,
        )
        val navigator = AppNavigator(backStack)

        navigator.navigateBackToOrPush(MainDestination.PracticeSets)

        assertEquals(
            listOf<NavKey>(MainDestination.Main, MainDestination.PracticeSets),
            backStack,
        )
    }
}
