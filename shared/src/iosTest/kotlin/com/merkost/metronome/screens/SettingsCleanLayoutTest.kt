package com.merkost.metronome.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.merkost.metronome.ui.theme.AppColorScheme
import com.merkost.metronome.ui.theme.Typography
import kotlinx.coroutines.test.TestResult
import kotlin.test.Test
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalTestApi::class)
class SettingsCleanLayoutTest {
    @Test
    fun settingsUsesLiteralGroupsAndKeepsPromotionsSecondary(): TestResult = settingsTest { model ->
        listOf("Audio", "Appearance", "Playback", "Beat display", "Practice data", "Help").forEach { name ->
            reveal(onNode(hasText(name))).assertIsDisplayed()
        }
        listOf("Make it yours.", "A little tweak. A better groove.", "Find your click.", "A little more.").forEach { text ->
            onAllNodes(hasText(text)).assertCountEquals(0)
        }
        val web = onNode(buttonRole and hasText("Open web app"))
        reveal(web).assertHasClickAction().performClick()
        assertEquals(1, model.webClicks)
        assertTouchTarget(web.fetchSemanticsNode())
        reveal(onNode(hasText("More by Merkost"))).assertIsDisplayed()
        val suby = onNode(buttonRole and hasText("Suby"))
        reveal(suby).assertHasClickAction().performClick()
        assertEquals(1, model.subyClicks)
        assertTouchTarget(suby.fetchSemanticsNode())
        onAllNodes(hasText("Dismiss")).assertCountEquals(0)
        reveal(onNode(hasText("Metronome · 1.4.0"))).assertIsDisplayed()
    }

    @Test
    fun playbackAndBeatDisplayKeepTheirSelectionThroughCollapseAndReopen(): TestResult = settingsTest { model ->
        val playback = onNode(buttonRole and hasText("Playback"))
        reveal(playback).performClick()
        settle()
        val countIn = onNode(switchRole and hasText("Count-in"))
        reveal(countIn).assertIsOff().performClick()
        settle()
        countIn.assertIsOn()
        assertEquals(1, model.countInClicks)
        reveal(playback).performClick()
        advance(64)
        settle()
        onAllNodes(switchRole and hasText("Count-in")).assertCountEquals(0)
        playback.performClick()
        settle()
        onNode(switchRole and hasText("Count-in")).assertIsOn()
        val display = onNode(buttonRole and hasText("Beat display"))
        reveal(display).performClick()
        settle()
        val flash = onNode(switchRole and hasText("Beat flash"))
        reveal(flash).assertIsOff().performClick()
        settle()
        flash.assertIsOn()
        assertEquals(1, model.flashClicks)
        reveal(display).performClick()
        settle()
        onAllNodes(switchRole and hasText("Beat flash")).assertCountEquals(0)
        display.performClick()
        settle()
        onNode(switchRole and hasText("Beat flash")).assertIsOn()
    }

    @Test
    fun narrowRtlDoubleTextKeepsGroupsAndExternalLinksReadable(): TestResult = settingsTest(
        dark = true,
        direction = LayoutDirection.Rtl,
        fontScale = 2f,
        width = 320,
    ) {
        listOf("Audio", "Appearance", "Practice data", "Help", "Open web app", "More by Merkost", "Suby").forEach { label ->
            reveal(onNode(hasText(label))).assertIsDisplayed()
            assertTextFits(label)
        }
        val suby = onNode(buttonRole and hasText("Suby"))
        assertTouchTarget(suby.fetchSemanticsNode())
        suby.performClick()
    }

    @Test
    fun unselectedMonoSwatchRemainsVisibleAgainstDarkSurface(): TestResult = settingsTest(dark = true) { model ->
        runOnUiThread { model.state.value = model.state.value.copy(colorScheme = AppColorScheme.MELROSE) }
        settle()
        val mono = onNode(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton) and hasContentDescription("Mono colour scheme"))
        reveal(mono).assertIsDisplayed()
        val before = mono.fetchSemanticsNode().unclippedBounds()
        val pixels = mono.captureToImage().toPixelMap()
        val backdrop = pixels[0, 0]
        var visibleOutline = 0
        for (y in 8 until minOf(56, pixels.height)) {
            for (x in 0 until pixels.width) {
                val ink = pixels[x, y]
                val contrast = maxOf(abs(ink.red - backdrop.red), abs(ink.green - backdrop.green), abs(ink.blue - backdrop.blue))
                if (contrast >= 0.12f) visibleOutline++
            }
        }
        assertTrue(visibleOutline >= 30, "The unselected black swatch needs a visible outline on the dark surface; visible pixels=$visibleOutline")
        mono.performClick()
        settle()
        assertEquals(AppColorScheme.BLACKNWHITE, model.state.value.colorScheme)
        assertEquals(true, mono.fetchSemanticsNode().config[SemanticsProperties.Selected])
        assertEquals(before, mono.fetchSemanticsNode().unclippedBounds(), "Selection must preserve the swatch footprint")
    }

    @Test
    fun reducedMotionDisclosuresReachTheirFinalStateAndRemainActionable(): TestResult = settingsTest(motionScale = 0f) {
        val playback = onNode(buttonRole and hasText("Playback"))
        reveal(playback).performClick()
        advance(32)
        onNode(switchRole and hasText("Count-in")).assertHasClickAction().assertIsOff()
        playback.performClick()
        advance(32)
        onAllNodes(switchRole and hasText("Count-in")).assertCountEquals(0)
    }

    @Test
    fun practiceDataRetainsResetPlaybackGuardAndDoneAction(): TestResult = contentTest(content = { events ->
        SettingsPracticeContent(720_000L, 18_600_000L, 3, true, { events.reset++ }, { events.done++ }) {
            Text("Export & import")
        }
    }) { events ->
        onNode(hasText("Statistics")).assertIsDisplayed()
        reveal(onNode(hasText("Export & import"))).assertIsDisplayed()
        reveal(onNode(buttonRole and hasText("Reset practice statistics"))).assertIsNotEnabled().performTouchInput { click() }
        assertEquals(0, events.reset)
        reveal(onNode(buttonRole and hasText("Done"))).performClick()
        assertEquals(1, events.done)
    }

    @Test
    fun aboutKeepsEveryHelpLinkAndOneCallbackPerAction(): TestResult = contentTest(content = { events ->
        SettingsAboutContent("1.4.0", { events.guide++ }, { events.updates++ }, { events.contact++ }, { events.rate++ }, { events.website++ }, { events.privacy++ }, { events.done++ })
    }) { events ->
        listOf("Practice guide", "What's new", "Contact support", "Rate the app", "Metronome website", "Privacy policy", "Done").forEach { title ->
            val action = onNode(buttonRole and hasText(title))
            reveal(action).assertHasClickAction().performClick()
            assertTouchTarget(action.fetchSemanticsNode())
        }
        assertEquals(listOf(1, 1, 1, 1, 1, 1, 1), listOf(events.guide, events.updates, events.contact, events.rate, events.website, events.privacy, events.done))
    }

    private fun ComposeUiTest.reveal(target: SemanticsNodeInteraction): SemanticsNodeInteraction {
        repeat(12) { attempt ->
            val node = target.fetchSemanticsNode()
            val scrollParent = generateSequence(node.parent) { it.parent }.firstOrNull {
                it.config.contains(SemanticsActions.ScrollBy) && it.config.contains(SemanticsProperties.VerticalScrollAxisRange)
            }
            if (scrollParent == null) {
                target.assertIsDisplayed()
                return target
            }
            val viewport = scrollParent.boundsInRoot
            val bounds = Rect(node.positionInRoot, node.size.toSize())
            assertTrue(bounds.width <= viewport.width + 0.5f && bounds.height <= viewport.height + 0.5f, "The complete control must fit the scroll viewport. target=$bounds viewport=$viewport")
            assertTrue(bounds.left >= viewport.left - 0.5f && bounds.right <= viewport.right + 0.5f, "Vertical scrolling must not conceal the control horizontally. target=$bounds viewport=$viewport")
            val delta = when {
                bounds.top < viewport.top - 0.5f -> bounds.top - viewport.top
                bounds.bottom > viewport.bottom + 0.5f -> bounds.bottom - viewport.bottom
                else -> 0f
            }
            if (delta == 0f) {
                target.assertIsDisplayed()
                return target
            }
            val before = scrollParent.config[SemanticsProperties.VerticalScrollAxisRange].value()
            val action = requireNotNull(scrollParent.config[SemanticsActions.ScrollBy].action)
            runOnUiThread { assertTrue(action(0f, delta), "The control's ancestor must accept a real scroll request") }
            mainClock.advanceTimeBy(1_000)
            waitForIdle()
            val updated = target.fetchSemanticsNode()
            val updatedParent = generateSequence(updated.parent) { it.parent }.first {
                it.config.contains(SemanticsActions.ScrollBy) && it.config.contains(SemanticsProperties.VerticalScrollAxisRange)
            }
            val after = updatedParent.config[SemanticsProperties.VerticalScrollAxisRange].value()
            assertTrue(kotlin.math.abs(after - before) > 0.01f, "The control remains outside a non-moving scroll boundary after frame advancement. attempt=$attempt before=$before after=$after target=$bounds viewport=$viewport")
        }
        throw AssertionError("The control did not become completely visible after12 bounded scroll requests: $target")
    }

    private fun settingsTest(
        dark: Boolean = false,
        direction: LayoutDirection = LayoutDirection.Ltr,
        fontScale: Float = 1f,
        width: Int = 360,
        motionScale: Float = 1f,
        block: suspend ComposeUiTest.(CleanSettingsModel) -> Unit,
    ): TestResult = runComposeUiTest(effectContext = object : MotionDurationScale {
        override val scaleFactor = motionScale
    }, testTimeout = 30.seconds) {
        mainClock.autoAdvance = false
        val model = CleanSettingsModel()
        setContent {
            CleanSettingsFrame(dark, direction, fontScale, width) { SettingsScreenContent(model.state.value, model.actions, {}) }
        }
        settle()
        block(model)
    }

    private fun contentTest(
        content: @Composable (InfoSheetEvents) -> Unit,
        block: suspend ComposeUiTest.(InfoSheetEvents) -> Unit,
    ): TestResult = runComposeUiTest(testTimeout = 30.seconds) {
        val events = InfoSheetEvents()
        setContent {
            CleanSettingsFrame(false, LayoutDirection.Ltr, 1f, 360) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)) { content(events) }
            }
        }
        waitForIdle()
        block(events)
    }

    private fun ComposeUiTest.assertTextFits(label: String) {
        val node = onNode(hasText(label), useUnmergedTree = true)
        val bounds = node.fetchSemanticsNode().unclippedBounds()
        val layouts = mutableListOf<TextLayoutResult>()
        node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action -> assertTrue(action(layouts)) }
        val layout = layouts.single()
        assertTrue(layout.multiParagraph.intrinsics.minIntrinsicWidth <= bounds.width + 0.5f, "$label must keep whole words")
        assertTrue(layout.multiParagraph.height <= bounds.height + 0.5f, "$label must fit vertically")
        assertTrue(!layout.multiParagraph.didExceedMaxLines, "$label must not be truncated")
    }

    private fun assertTouchTarget(node: SemanticsNode) {
        val bounds = node.unclippedBounds()
        assertTrue(bounds.width >= 47.5f && bounds.height >= 47.5f, "Actions must retain 48dp touch targets. bounds=$bounds")
    }

    private fun ComposeUiTest.advance(milliseconds: Long) {
        mainClock.advanceTimeBy(milliseconds)
        waitForIdle()
    }

    private fun ComposeUiTest.settle() = advance(2_000)

    private val buttonRole = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
    private val switchRole = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch)
}

private class CleanSettingsModel {
    val state = mutableStateOf(SettingsUiState(version = "1.4.0"))
    var webClicks = 0
    var subyClicks = 0
    var countInClicks = 0
    var flashClicks = 0
    val actions = SettingsActions(
        onColorScheme = { state.value = state.value.copy(colorScheme = it) },
        onTheme = { state.value = state.value.copy(themeMode = it) },
        onWeb = { webClicks++ },
        onSuby = { subyClicks++ },
        onCountIn = {
            countInClicks++
            state.value = state.value.copy(countInEnabled = it)
        },
        onColorFlash = {
            flashClicks++
            state.value = state.value.copy(colorFlash = it)
        },
    )
}

private class InfoSheetEvents {
    var reset = 0
    var done = 0
    var guide = 0
    var updates = 0
    var contact = 0
    var rate = 0
    var website = 0
    var privacy = 0
}

@Composable
private fun CleanSettingsFrame(dark: Boolean, direction: LayoutDirection, fontScale: Float, width: Int, content: @Composable () -> Unit) {
    val colors = if (dark) AppColorScheme.MELROSE.darkColor else AppColorScheme.MELROSE.lightColor
    CompositionLocalProvider(LocalDensity provides Density(1f, fontScale), LocalLayoutDirection provides direction, LocalContentColor provides colors.onSurface) {
        MaterialTheme(colorScheme = colors, typography = Typography) {
            Box(Modifier.width(width.dp).height(640.dp).clipToBounds().background(colors.surface).testTag("settingsViewport")) { content() }
        }
    }
}

private fun SemanticsNode.unclippedBounds(): Rect = Rect(positionInRoot, size.toSize())
