package com.merkost.metronome.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import com.merkost.metronome.navigation.AppPresentation
import com.merkost.metronome.navigation.LocalAppPresentation
import com.merkost.metronome.navigation.MainDestination
import com.merkost.metronome.screens.SettingsActions
import com.merkost.metronome.screens.SettingsScreenContent
import com.merkost.metronome.screens.SettingsUiState
import com.merkost.metronome.ui.theme.AppColorScheme
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalTestApi::class)
class DesktopSettingsRenderingTest {
    @Test
    fun desktopSettingsKeepCountInAndMotionChoicesWithoutUnsupportedMobileControls() = runDesktopComposeUiTest(
        width = 1000,
        height = 900,
        effectContext = object : MotionDurationScale { override val scaleFactor = 1f },
        testTimeout = 30.seconds,
    ) {
        setContent {
            CompositionLocalProvider(LocalAppPresentation provides AppPresentation.Desktop, LocalDensity provides Density(1f, 1.5f)) {
                MaterialTheme(colorScheme = AppColorScheme.MELROSE.darkColor) {
                    DesktopAppShell(MainDestination.Settings, {}) {
                        SettingsScreenContent(
                            state = SettingsUiState(supportsHaptics = false, version = "1.5.0"),
                            actions = SettingsActions(),
                            upPress = {},
                        )
                    }
                }
            }
        }
        mainClock.advanceTimeBy(2_000)
        waitForIdle()
        onNode(hasText("Beat haptics")).assertDoesNotExist()
        onNode(hasText("Playback")).performScrollTo().performClick()
        mainClock.advanceTimeBy(2_000)
        waitForIdle()
        onNode(hasText("Count-in")).performScrollTo().assertIsDisplayed()
        listOf("Keep screen awake", "Background playback", "Live Activity", "Live Activities").forEach { label ->
            onNode(hasText(label)).assertDoesNotExist()
        }
        onNode(hasText("Beat display")).performScrollTo().performClick()
        mainClock.advanceTimeBy(2_000)
        waitForIdle()
        onNode(hasText("Choose how each beat moves.")).performScrollTo().assertIsDisplayed()
        onNode(hasText("Animations follow your device's motion preference.")).assertDoesNotExist()
    }
}
