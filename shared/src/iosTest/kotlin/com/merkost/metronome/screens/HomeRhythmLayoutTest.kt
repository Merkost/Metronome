package com.merkost.metronome.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.merkost.metronome.components.MainShortcuts
import com.merkost.metronome.model.ClickSound
import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.MetronomeState
import com.merkost.metronome.model.Subdivision
import com.merkost.metronome.model.TimeSignature
import com.merkost.metronome.ui.theme.AppColorScheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class HomeRhythmLayoutTest {
    @Test
    fun practiceAndSoundHaveEqualHeightWhenThePracticeCaptionWraps() = runComposeUiTest {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                MaterialTheme(colorScheme = AppColorScheme.MELROSE.lightColor) {
                    Box(Modifier.width(324.dp).background(MaterialTheme.colorScheme.surface).testTag("viewport")) {
                        MainShortcuts(ClickSound.WOOD, {}, {})
                    }
                }
            }
        }
        mainClock.advanceTimeBy(1000)
        waitForIdle()
        val practice = onNode(hasText("Practice")).fetchSemanticsNode().boundsInRoot
        val sound = onNode(hasText("Sound")).fetchSemanticsNode().boundsInRoot
        assertEquals(practice.height, sound.height, 0.5f, "The two home shortcuts must have equal height")
    }

    @Test
    fun scrollEndKeepsBreathingRoomBelowTheLastHomeControls() = runComposeUiTest {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                MaterialTheme {
                    Box(Modifier.width(360.dp).height(400.dp).testTag("viewport")) {
                        MainScrollableContent(Modifier.fillMaxSize()) {
                            repeat(6) { Box(Modifier.height(120.dp)) }
                            MainShortcuts(ClickSound.WOOD, {}, {}, Modifier.testTag("shortcuts"))
                        }
                    }
                }
            }
        }
        onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.ScrollBy)).performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, 10000f) }
        mainClock.advanceTimeBy(2000)
        waitForIdle()
        val viewport = onNodeWithTag("viewport").fetchSemanticsNode().boundsInRoot
        val shortcuts = onNodeWithTag("shortcuts").fetchSemanticsNode().boundsInRoot
        assertTrue(viewport.bottom - shortcuts.bottom >= 31f, "Scroll end must leave32dp below the last controls; gap=${viewport.bottom-shortcuts.bottom}")
    }
    @Test
    fun shortcutSizingSurvivesSoundChangesAndLargeRtlText() = runComposeUiTest {
        mainClock.autoAdvance = false
        val selected = mutableStateOf(ClickSound.WOOD)
        var practiceCalls = 0
        var soundCalls = 0
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, 2f), LocalLayoutDirection provides LayoutDirection.Rtl) {
                MaterialTheme(colorScheme = AppColorScheme.MELROSE.darkColor) {
                    Box(Modifier.width(284.dp).background(MaterialTheme.colorScheme.surface).testTag("viewport")) {
                        MainShortcuts(selected.value, { practiceCalls++ }, { soundCalls++ })
                    }
                }
            }
        }
        mainClock.advanceTimeBy(2000); waitForIdle()
        val titlePixels = onNode(hasText("Practice"), useUnmergedTree = true).captureToImage().toPixelMap()
        val backdrop = titlePixels[0, 0]
        var strongestInk = 0f
        for (y in 0 until titlePixels.height) for (x in 0 until titlePixels.width) {
            val pixel = titlePixels[x, y]
            strongestInk = maxOf(strongestInk, kotlin.math.abs(pixel.red - backdrop.red), kotlin.math.abs(pixel.green - backdrop.green), kotlin.math.abs(pixel.blue - backdrop.blue))
        }
        assertTrue(strongestInk >= 0.45f, "Dark shortcut titles must paint readable foreground ink. difference=$strongestInk")
        val initialPractice = onNode(hasText("Practice")).fetchSemanticsNode().boundsInRoot
        val initialSound = onNode(hasText("Sound")).fetchSemanticsNode().boundsInRoot
        assertEquals(initialPractice.height, initialSound.height, 0.5f)
        ClickSound.entries.forEach { sound ->
            runOnUiThread { selected.value = sound }
            mainClock.advanceTimeBy(64); waitForIdle()
            assertEquals(initialPractice, onNode(hasText("Practice")).fetchSemanticsNode().boundsInRoot)
            assertEquals(initialSound, onNode(hasText("Sound")).fetchSemanticsNode().boundsInRoot)
            mainClock.advanceTimeBy(2000); waitForIdle()
            val nodes = listOf(onNode(hasText("Practice"), useUnmergedTree = true), onNode(hasText(sound.displayName), useUnmergedTree = true), onNode(hasText("Timer & trainers"), useUnmergedTree = true))
            nodes.forEach { node ->
                val layouts = mutableListOf<TextLayoutResult>()
                node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
                val layout = layouts.single()
                assertTrue(layout.multiParagraph.intrinsics.minIntrinsicWidth <= layout.size.width + 0.5f, "Shortcut words must fit without forced breaks")
                assertTrue(!layout.multiParagraph.didExceedMaxLines)
                assertTrue(layout.multiParagraph.height <= layout.size.height + 0.5f)
            }
        }
        onNode(hasText("Practice")).performClick()
        onNode(hasText("Sound")).performClick()
        assertEquals(1, practiceCalls)
        assertEquals(1, soundCalls)
    }

    @Test
    fun rhythmControlsKeepTheirOrderAndSeparateSelectionActions() = runComposeUiTest {
        mainClock.autoAdvance = false
        val state = mutableStateOf(MetronomeState())
        val countIn = mutableStateOf(false)
        var countCalls = 0
        var meterCalls = 0
        var subdivisionCalls = 0
        var beatCalls = 0
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                MaterialTheme(colorScheme = AppColorScheme.MELROSE.lightColor) {
                    Box(Modifier.width(360.dp).height(800.dp).background(MaterialTheme.colorScheme.surface).testTag("viewport")) {
                        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)) {
                            RhythmContent(state.value, -1, countIn.value,
                                { countCalls++; countIn.value = it },
                                { meterCalls++; state.value = state.value.copy(timeSignature = it, beats = it.defaultBeats) },
                                { subdivisionCalls++; state.value = state.value.copy(subdivision = it) },
                                { index, beat -> beatCalls++; state.value = state.value.copy(beats = state.value.beats.toMutableList().apply { set(index, beat.next()) }) })
                        }
                    }
                }
            }
        }
        mainClock.advanceTimeBy(2000); waitForIdle()
        val meterTop = onNode(hasText("Time signature")).fetchSemanticsNode().boundsInRoot.top
        val beatsTop = onNode(hasText("Beat pattern")).fetchSemanticsNode().boundsInRoot.top
        val subdivisionTop = onNode(hasText("Subdivision")).fetchSemanticsNode().boundsInRoot.top
        val countTop = onNode(hasContentDescription("Count-in. One bar before playback.")).fetchSemanticsNode().boundsInRoot.top
        assertTrue(meterTop < beatsTop && beatsTop < subdivisionTop && subdivisionTop < countTop)
        onNode(hasText("7/8")).performClick()
        mainClock.advanceTimeBy(2000); waitForIdle()
        assertEquals(1, meterCalls)
        assertEquals(TimeSignature.SEVEN_EIGHT, state.value.timeSignature)
        onNode(hasContentDescription("Beat 1: accented")).performClick()
        mainClock.advanceTimeBy(2000); waitForIdle()
        assertEquals(1, beatCalls)
        assertEquals(Beat.LOW, state.value.beats.first())
        onNode(hasText("Triplets")).performClick()
        assertEquals(1, subdivisionCalls)
        assertEquals(Subdivision.TRIPLET, state.value.subdivision)
        onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.ScrollBy)).performSemanticsAction(SemanticsActions.ScrollBy) { it(0f,10000f) }
        mainClock.advanceTimeBy(2000); waitForIdle()
        onNode(hasContentDescription("Count-in. One bar before playback.")).performClick()
        assertTrue(countIn.value)
        assertEquals(1, countCalls)
    }

    @Test
    fun largeRtlRhythmRemainsScrollableWithReachableCountIn() = runComposeUiTest {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f,2f), LocalLayoutDirection provides LayoutDirection.Rtl) {
                MaterialTheme(colorScheme = AppColorScheme.MELROSE.darkColor) {
                    Box(Modifier.width(320.dp).height(640.dp).background(MaterialTheme.colorScheme.surface).testTag("viewport")) {
                        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)) {
                            RhythmContent(MetronomeState(timeSignature = TimeSignature.SEVEN_EIGHT, beats = TimeSignature.SEVEN_EIGHT.defaultBeats), -1, true, {}, {}, {}, { _, _ -> })
                        }
                    }
                }
            }
        }
        mainClock.advanceTimeBy(2000); waitForIdle()
        onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.ScrollBy)).performSemanticsAction(SemanticsActions.ScrollBy) { it(0f,10000f) }
        mainClock.advanceTimeBy(2000); waitForIdle()
        onNode(hasContentDescription("Count-in. One bar before playback.")).assertIsDisplayed()
        val viewport = onNodeWithTag("viewport").fetchSemanticsNode().boundsInRoot
        val toggle = onNode(hasContentDescription("Count-in. One bar before playback.")).fetchSemanticsNode().boundsInRoot
        assertTrue(toggle.left >= viewport.left && toggle.right <= viewport.right && toggle.top >= viewport.top && toggle.bottom <= viewport.bottom)
    }


}
