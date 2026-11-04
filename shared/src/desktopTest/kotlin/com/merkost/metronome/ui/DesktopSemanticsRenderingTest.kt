package com.merkost.metronome.ui

import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Volume2
import com.merkost.metronome.components.MetronomeBalls
import com.merkost.metronome.components.StatusStrip
import com.merkost.metronome.model.Beat
import com.merkost.metronome.screens.ActiveTimerContent
import com.merkost.metronome.ui.theme.AppColorScheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalTestApi::class)
class DesktopSemanticsRenderingTest {
    @Test
    fun beatDotReadsItsStateOnce() = runDesktopComposeUiTest(testTimeout = 30.seconds) {
        setContent {
            MaterialTheme(colorScheme = AppColorScheme.MELROSE.darkColor) {
                MetronomeBalls(
                    selectedIndex = 0,
                    beats = listOf(Beat.HIGH, Beat.LOW, Beat.LOW, Beat.LOW),
                    isPlaying = false,
                    animSpec = tween(0),
                    onBallClicked = { _, _ -> },
                )
            }
        }
        waitForIdle()
        val node = onNodeWithContentDescription("Beat 2")
        node.assertContentDescriptionEquals("Beat 2")
        assertEquals("normal", node.fetchSemanticsNode().config.getOrNull(SemanticsProperties.StateDescription))
    }

    @Test
    fun beatCustomActionSetsTheBeatDirectly() = runDesktopComposeUiTest(testTimeout = 30.seconds) {
        val set = mutableListOf<Pair<Int, Beat>>()
        val clicked = mutableListOf<Pair<Int, Beat>>()
        setContent {
            MaterialTheme(colorScheme = AppColorScheme.MELROSE.darkColor) {
                MetronomeBalls(
                    selectedIndex = 0,
                    beats = listOf(Beat.HIGH, Beat.LOW, Beat.LOW, Beat.LOW),
                    isPlaying = false,
                    animSpec = tween(0),
                    onBallClicked = { index, beat -> clicked += index to beat },
                    onSetBeat = { index, beat -> set += index to beat },
                )
            }
        }
        waitForIdle()
        val actions = onNodeWithContentDescription("Beat 2").fetchSemanticsNode().config
            .getOrNull(SemanticsActions.CustomActions).orEmpty()
        val accent = actions.first { it.label == "Set beat 2 to accented" }
        assertTrue(accent.action())
        assertEquals(listOf(1 to Beat.HIGH), set)
        assertTrue(clicked.isEmpty())
    }

    @Test
    fun beatTapActionNamesTheNextState() = runDesktopComposeUiTest(testTimeout = 30.seconds) {
        setContent {
            MaterialTheme(colorScheme = AppColorScheme.MELROSE.darkColor) {
                MetronomeBalls(
                    selectedIndex = 0,
                    beats = listOf(Beat.HIGH, Beat.LOW, Beat.LOW, Beat.LOW),
                    isPlaying = false,
                    animSpec = tween(0),
                    onBallClicked = { _, _ -> },
                )
            }
        }
        waitForIdle()
        val label = onNodeWithContentDescription("Beat 2").fetchSemanticsNode().config
            .getOrNull(SemanticsActions.OnClick)?.label
        assertEquals("Change to muted", label)
    }

    @Test
    fun gapStripExposesDescriptionAndEditLabel() = runDesktopComposeUiTest(testTimeout = 30.seconds) {
        setContent {
            MaterialTheme(colorScheme = AppColorScheme.MELROSE.darkColor) {
                StatusStrip(
                    icon = Lucide.Volume2,
                    title = "Click · bar 2/4",
                    caption = "4 + 2 bars",
                    progress = 0.3f,
                    accent = MaterialTheme.colorScheme.primary,
                    onClick = {},
                    onStop = {},
                    description = "Gap trainer, playing bar 2 of 4",
                    onClickLabel = "Edit gap trainer",
                    stopLabel = "Stop gap trainer",
                )
            }
        }
        waitForIdle()
        val strip = onNode(hasContentDescription("Gap trainer, playing bar 2 of 4"))
        strip.assertExists()
        assertEquals("Edit gap trainer", strip.fetchSemanticsNode().config.getOrNull(SemanticsActions.OnClick)?.label)
        onNode(hasContentDescription("Stop gap trainer")).assertExists()
    }

    @Test
    fun runningTimerOffersStopWhenTimeIsUp() = runDesktopComposeUiTest(testTimeout = 30.seconds) {
        val changes = mutableListOf<Boolean>()
        setContent {
            MaterialTheme(colorScheme = AppColorScheme.MELROSE.darkColor) {
                ActiveTimerContent(
                    isPlaying = true,
                    timerGoal = 60_000L,
                    timerRemaining = 30_000L,
                    onExtend = {},
                    onRestart = {},
                    onStop = {},
                    stopWhenTimerEnds = false,
                    onStopWhenTimerEndsChange = { changes += it },
                )
            }
        }
        waitForIdle()
        onNode(hasText("Stop when time is up")).assertExists()
        onNode(hasText("Finishes the bar in progress, then stops")).assertExists()
    }

    @Test
    fun finishedTimerThatStoppedPlaybackSaysSoInALiveRegion() = runDesktopComposeUiTest(testTimeout = 30.seconds) {
        setContent {
            MaterialTheme(colorScheme = AppColorScheme.MELROSE.darkColor) {
                ActiveTimerContent(
                    isPlaying = false,
                    timerGoal = 60_000L,
                    timerRemaining = 0L,
                    onExtend = {},
                    onRestart = {},
                    onStop = {},
                    stoppedPlayback = true,
                )
            }
        }
        mainClock.advanceTimeBy(1_000)
        waitForIdle()
        onNode(hasText("Stop when time is up")).assertDoesNotExist()
        val caption = onNode(hasText("1:00 practiced · stopped"))
        caption.assertExists()
        assertEquals(
            androidx.compose.ui.semantics.LiveRegionMode.Polite,
            caption.fetchSemanticsNode().config.getOrNull(SemanticsProperties.LiveRegion),
        )
    }
}
