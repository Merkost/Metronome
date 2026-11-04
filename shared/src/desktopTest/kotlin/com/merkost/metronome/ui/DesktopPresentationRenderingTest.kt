package com.merkost.metronome.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.merkost.metronome.components.AppBottomSheet
import com.merkost.metronome.components.AppTextField
import com.merkost.metronome.navigation.AppPresentation
import com.merkost.metronome.navigation.LocalAppPresentation
import com.merkost.metronome.navigation.LocalAppViewportSize
import com.merkost.metronome.navigation.MainDestination
import com.merkost.metronome.ui.theme.AppColorScheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalTestApi::class)
class DesktopPresentationRenderingTest {
    @Test
    fun wideWindowKeepsNavigationBesideTheInstrumentAndUpdatesSelection() = runDesktopComposeUiTest(width = 1000, height = 720, testTimeout = 20.seconds) {
        var selected by mutableStateOf<MainDestination>(MainDestination.Main)
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                MaterialTheme {
                    DesktopAppShell(selected, { selected = it }) {
                        Box(Modifier.fillMaxSize().testTag("instrumentPane"))
                    }
                }
            }
        }
        val labels = listOf("Metronome", "Presets", "Practice sets", "Settings")
        val tabs = labels.map { label ->
            onNode(hasContentDescription(label)).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        }
        tabs.forEach { assertTrue(it.height >= 47.5f && it.width >= 100f) }
        tabs.zipWithNext().forEach { (first, next) -> assertTrue(first.bottom <= next.top) }
        val pane = onNodeWithTag("instrumentPane").fetchSemanticsNode().boundsInRoot
        assertTrue(pane.left >= 200f && pane.width >= 790f)
        onNode(hasContentDescription("Presets")).performClick()
        waitForIdle()
        assertEquals(MainDestination.PracticePresets, selected)
        onNode(hasContentDescription("Presets")).assertIsSelected()
    }

    @Test
    fun compactRtlWindowKeepsEveryDestinationReachableAtLargeText() = runDesktopComposeUiTest(width = 520, height = 720, testTimeout = 20.seconds) {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, 1.8f), LocalLayoutDirection provides LayoutDirection.Rtl) {
                MaterialTheme(colorScheme = AppColorScheme.MELROSE.darkColor) {
                    DesktopAppShell(MainDestination.Settings, {}) {
                        Box(Modifier.fillMaxSize().testTag("settingsPane"))
                    }
                }
            }
        }
        val tabs = listOf("Metronome", "Presets", "Practice sets", "Settings").map { label ->
            onNode(hasContentDescription(label)).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        }
        tabs.forEach {
            assertTrue(it.height >= 47.5f && it.width >= 47.5f)
            assertTrue(it.left >= 0f && it.right <= 520.5f)
        }
        tabs.zipWithNext().forEach { (first, next) -> assertTrue(first.left >= next.right) }
        val pane = onNodeWithTag("settingsPane").fetchSemanticsNode().boundsInRoot
        assertTrue(pane.top >= tabs.maxOf { it.bottom })
        assertTrue(pane.width >= 519f && pane.height > 400f)
        onNode(hasContentDescription("Settings")).assertIsSelected()
    }

    @Test
    fun desktopDialogBoundsAndScrollKeepTheWholeFinalActionReachable() = dialogTest(fontScale = 1f, direction = LayoutDirection.Ltr)

    @Test
    fun largeRtlDesktopDialogKeepsEndPaddingAndDismissesOnlyOnce() = dialogTest(fontScale = 2f, direction = LayoutDirection.Rtl)

    @Test
    fun focusedTextInputOwnsSpacesWhileInstrumentShortcutsRemainInactive() = runDesktopComposeUiTest(width = 600, height = 400, testTimeout = 20.seconds) {
        val rootFocus = FocusRequester()
        var playbackToggles = 0
        var increases = 0
        var decreases = 0
        var input by mutableStateOf("")
        setContent {
            MaterialTheme {
                Box(
                    Modifier.fillMaxSize().testTag("keyboardInstrument")
                        .desktopInstrumentKeys(AppPresentation.Desktop, { playbackToggles++ }, { increases++ }, { decreases++ })
                        .focusRequester(rootFocus).focusable(),
                ) {
                    AppTextField(input, { input = it }, modifier = Modifier.fillMaxWidth().testTag("presetName"), label = "Preset name")
                }
            }
        }
        runOnUiThread { rootFocus.requestFocus() }
        waitForIdle()
        onNodeWithTag("keyboardInstrument").performKeyInput {
            pressKey(Key.Spacebar)
            pressKey(Key.DirectionUp)
            pressKey(Key.DirectionDown)
        }
        waitForIdle()
        assertEquals(1, playbackToggles)
        assertEquals(1, increases)
        assertEquals(1, decreases)
        onNodeWithTag("presetName").performClick()
        onNodeWithTag("presetName").performKeyInput { pressKey(Key.Spacebar) }
        waitForIdle()
        onNodeWithTag("presetName").assertIsFocused()
        assertEquals(1, playbackToggles, "Typing a space in a preset name must not start or stop the metronome")
        assertEquals(1, increases)
        assertEquals(1, decreases)
        onNodeWithTag("presetName").performTextInput("Practice A")
        waitForIdle()
        assertTrue(input.endsWith("Practice A"))
        assertEquals(1, playbackToggles)
        runOnUiThread { rootFocus.requestFocus() }
        waitForIdle()
        onNodeWithTag("keyboardInstrument").performKeyInput { pressKey(Key.Spacebar) }
        waitForIdle()
        assertEquals(2, playbackToggles, "Returning focus to the instrument restores its shortcuts")
    }

    private fun dialogTest(fontScale: Float, direction: LayoutDirection) = runDesktopComposeUiTest(
        width = 520,
        height = 640,
        effectContext = object : MotionDurationScale { override val scaleFactor = 1f },
        testTimeout = 30.seconds,
    ) {
        mainClock.autoAdvance = false
        var visible by mutableStateOf(true)
        var dismissals = 0
        setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(1f, fontScale),
                LocalLayoutDirection provides direction,
                LocalAppPresentation provides AppPresentation.Desktop,
                LocalAppViewportSize provides DpSize(520.dp, 640.dp),
            ) {
                MaterialTheme(colorScheme = AppColorScheme.MELROSE.darkColor) {
                    if (visible) AppBottomSheet("Desktop tool", { dismissals++; visible = false }) { dismissAnimated ->
                        repeat(24) { Text("Practice option $it", Modifier.fillMaxWidth().heightIn(min = 48.dp)) }
                        Button(onClick = dismissAnimated, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("desktopFinalAction")) {
                            Text("Done")
                        }
                    }
                }
            }
        }
        advance(2_000)
        val panel = onNode(SemanticsMatcher.expectValue(SemanticsProperties.PaneTitle, "Desktop tool")).fetchSemanticsNode().boundsInRoot
        assertTrue(panel.width <= 484.5f && panel.height <= 576.5f, "Dialog must stay bounded by its app window: $panel")
        onNode(hasText("Desktop tool")).assertIsDisplayed()
        val scroll = onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.ScrollBy))
        repeat(4) {
            scroll.performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, 1_000f) }
            advance(500)
        }
        val viewport = scroll.fetchSemanticsNode().boundsInRoot
        val action = onNodeWithTag("desktopFinalAction").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue(action.height >= 51.5f && action.width >= 47.5f)
        assertTrue(action.top >= viewport.top && action.bottom <= viewport.bottom + 0.5f)
        assertTrue(viewport.bottom - action.bottom >= 31.5f, "The final action needs clear scrollable end padding")
        onNodeWithTag("desktopFinalAction").performClick()
        onNodeWithTag("desktopFinalAction").performClick()
        onNode(hasContentDescription("Close Desktop tool")).assertIsNotEnabled()
        assertEquals(0, dismissals, "Dismissal must wait for the shared exit spring")
        advance(2_000)
        assertEquals(1, dismissals)
    }

    private suspend fun ComposeUiTest.advance(milliseconds: Long) {
        mainClock.advanceTimeBy(milliseconds)
        waitForIdle()
    }
}
