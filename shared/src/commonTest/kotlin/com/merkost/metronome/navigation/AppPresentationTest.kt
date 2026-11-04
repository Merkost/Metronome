package com.merkost.metronome.navigation

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppPresentationTest {
    @Test
    fun wideCompactScreensRetainTheirMobileNavigation() {
        assertFalse(usesDesktopSidebar(AppPresentation.Compact, 1440f))
        assertFalse(usesDesktopSidebar(AppPresentation.Compact, 840f))
    }

    @Test
    fun desktopNavigationChangesAtTheDocumentedWindowBoundary() {
        assertFalse(usesDesktopSidebar(AppPresentation.Desktop, 520f))
        assertFalse(usesDesktopSidebar(AppPresentation.Desktop, 839f))
        assertTrue(usesDesktopSidebar(AppPresentation.Desktop, 840f))
        assertTrue(usesDesktopSidebar(AppPresentation.Desktop, 1440f))
    }

    @Test
    fun repeatedDesktopDestinationChangesKeepASingleInstrumentRoot() {
        val destinations = listOf(
            MainDestination.Settings,
            MainDestination.PracticePresets,
            MainDestination.PracticeSets,
            MainDestination.Main,
        )
        val stack = mutableListOf<NavKey>(MainDestination.Main, MainDestination.Settings, MainDestination.PracticeSets)
        repeat(100) {
            destinations.forEach { destination ->
                navigateDesktop(stack, destination)
                assertEquals(MainDestination.Main, stack.first())
                assertEquals(destination, stack.last())
                assertEquals(if (destination == MainDestination.Main) 1 else 2, stack.size)
                assertEquals(stack.distinct(), stack)
            }
        }
    }

    @Test
    fun spaceChangesPlaybackOnceOnReleaseInDesktopPresentation() {
        assertNull(instrumentKeyAction(AppPresentation.Desktop, Key.Spacebar, KeyEventType.KeyDown, false))
        assertEquals(InstrumentKeyAction.TogglePlayback, instrumentKeyAction(AppPresentation.Desktop, Key.Spacebar, KeyEventType.KeyUp, false))
        assertNull(instrumentKeyAction(AppPresentation.Compact, Key.Spacebar, KeyEventType.KeyUp, false))
    }

    @Test
    fun tempoArrowKeysRespondOnPressAndDoNotHandleOtherTyping() {
        assertEquals(InstrumentKeyAction.IncreaseTempo, instrumentKeyAction(AppPresentation.Desktop, Key.DirectionUp, KeyEventType.KeyDown, false))
        assertEquals(InstrumentKeyAction.DecreaseTempo, instrumentKeyAction(AppPresentation.Desktop, Key.DirectionDown, KeyEventType.KeyDown, false))
        assertNull(instrumentKeyAction(AppPresentation.Desktop, Key.DirectionUp, KeyEventType.KeyUp, false))
        assertNull(instrumentKeyAction(AppPresentation.Desktop, Key.A, KeyEventType.KeyDown, false))
        assertNull(instrumentKeyAction(AppPresentation.Compact, Key.DirectionDown, KeyEventType.KeyDown, false))
    }

    @Test
    fun modifiedKeysRemainAvailableForTextSelectionAndNativeMenus() {
        listOf(Key.Spacebar, Key.DirectionUp, Key.DirectionDown).forEach { key ->
            listOf(KeyEventType.KeyDown, KeyEventType.KeyUp).forEach { type ->
                assertNull(instrumentKeyAction(AppPresentation.Desktop, key, type, true))
            }
        }
    }
}
