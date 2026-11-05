package com.merkost.metronome.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import com.merkost.metronome.components.EmptyState
import com.merkost.metronome.components.EmptyStateArt
import com.merkost.metronome.components.WeekStrip
import com.merkost.metronome.model.PracticeLog
import com.merkost.metronome.screens.WelcomeScreen
import com.merkost.metronome.ui.theme.AppColorScheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalTestApi::class)
class PersonalityRenderingTest {

    private val wednesday = 20_002L

    @Test
    fun welcomeOffersToHearOneBarAndReportsTheTap() = runDesktopComposeUiTest(testTimeout = 30.seconds) {
        var taps = 0
        setContent {
            MaterialTheme(colorScheme = AppColorScheme.MELROSE.lightColor) {
                WelcomeScreen(false, null, {}, {}, hearing = false, onHearBar = { taps++ })
            }
        }
        waitForIdle()
        onNode(hasText("Hear one bar")).assertExists()
        onNodeWithTag("welcomeHear").performClick()
        assertEquals(1, taps)
    }

    @Test
    fun welcomeShowsListeningWhileTheBarPlays() = runDesktopComposeUiTest(testTimeout = 30.seconds) {
        setContent {
            MaterialTheme(colorScheme = AppColorScheme.MELROSE.lightColor) {
                WelcomeScreen(false, null, {}, {}, hearing = true, onHearBar = {})
            }
        }
        waitForIdle()
        onNode(hasText("Listening…")).assertExists()
    }

    @Test
    fun welcomeHasNoHearButtonWhenNothingCanPlay() = runDesktopComposeUiTest(testTimeout = 30.seconds) {
        setContent {
            MaterialTheme(colorScheme = AppColorScheme.MELROSE.lightColor) {
                WelcomeScreen(false, null, {}, {})
            }
        }
        waitForIdle()
        onNode(hasText("Hear one bar")).assertDoesNotExist()
    }

    @Test
    fun weekStripReadsOutTheDaysPracticed() = runDesktopComposeUiTest(testTimeout = 30.seconds) {
        val week = PracticeLog.week(mapOf(wednesday to 120_000L, wednesday - 1 to 120_000L), wednesday)
        setContent {
            MaterialTheme(colorScheme = AppColorScheme.MELROSE.lightColor) { WeekStrip(week) }
        }
        waitForIdle()
        onNode(hasContentDescription("Practiced 2 days this week")).assertExists()
    }

    @Test
    fun emptyStateShowsItsTitleBodyAndAction() = runDesktopComposeUiTest(testTimeout = 30.seconds) {
        setContent {
            MaterialTheme(colorScheme = AppColorScheme.MELROSE.lightColor) {
                EmptyState(EmptyStateArt.Steps, "Practice in steps", "Chain presets into a routine.") {
                    androidx.compose.material3.Button(onClick = {}) { androidx.compose.material3.Text("Create set") }
                }
            }
        }
        waitForIdle()
        onNode(hasText("Practice in steps")).assertExists()
        onNode(hasText("Chain presets into a routine.")).assertExists()
        onNode(hasText("Create set")).assertExists()
    }
}
