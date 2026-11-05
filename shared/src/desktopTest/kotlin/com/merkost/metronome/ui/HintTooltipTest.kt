package com.merkost.metronome.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.merkost.metronome.components.HintTooltip
import com.merkost.metronome.navigation.AppPresentation
import com.merkost.metronome.navigation.LocalAppPresentation
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalTestApi::class)
class HintTooltipTest {

    @Test
    fun hoveringShowsTheShortcutHintOnDesktop() = runDesktopComposeUiTest(testTimeout = 30.seconds) {
        setContent {
            MaterialTheme {
                CompositionLocalProvider(LocalAppPresentation provides AppPresentation.Desktop) {
                    HintTooltip("Pause · Space") {
                        Box(Modifier.size(80.dp).testTag("anchor")) { Text("Play") }
                    }
                }
            }
        }
        waitForIdle()
        onNodeWithTag("anchor", useUnmergedTree = true).performMouseInput { moveTo(center) }
        mainClock.advanceTimeBy(2_000)
        waitForIdle()
        onNode(hasText("Pause · Space")).assertExists()
    }

    @Test
    fun phonesNeverShowTheHint() = runDesktopComposeUiTest(testTimeout = 30.seconds) {
        setContent {
            MaterialTheme {
                HintTooltip("Pause · Space") {
                    Box(Modifier.size(80.dp).testTag("anchor")) { Text("Play") }
                }
            }
        }
        waitForIdle()
        onNodeWithTag("anchor", useUnmergedTree = true).performMouseInput { moveTo(center) }
        mainClock.advanceTimeBy(2_000)
        waitForIdle()
        onNode(hasText("Pause · Space")).assertDoesNotExist()
    }
}
