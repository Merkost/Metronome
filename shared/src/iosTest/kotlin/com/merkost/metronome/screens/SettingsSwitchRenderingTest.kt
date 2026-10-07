package com.merkost.metronome.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.SlidersHorizontal
import com.composables.icons.lucide.Timer
import com.merkost.metronome.ui.theme.AppColorScheme
import kotlinx.coroutines.test.TestResult
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalTestApi::class)
class SettingsSwitchRenderingTest {
    @Test
    fun disclosureSwitchPixelsFadeAndClipDuringEnterAndExit(): TestResult = renderingTest {
        assertDisclosureRendering()
    }

    @Test
    fun disclosureSwitchPixelsFadeAndClipInDarkRtlWithLargerText(): TestResult = renderingTest(
        dark = true,
        direction = LayoutDirection.Rtl,
        fontScale = 1.5f,
    ) {
        assertDisclosureRendering(LayoutDirection.Rtl)
    }

    @Test
    fun rapidReversalsKeepSwitchPixelsInsideTheCurrentDisclosure(): TestResult = renderingTest {
        repeat(3) {
            toggleFlow()
            advance(64)
            assertTrackWithinFlow(frame(), flowBounds(), LayoutDirection.Ltr)
            toggleFlow()
            advance(48)
            assertTrackWithinFlow(frame(), flowBounds(), LayoutDirection.Ltr)
        }
        settle()
        onAllNodes(switchRole).assertCountEquals(0)
        toggleFlow()
        settle()
        onAllNodes(switchRole).assertCountEquals(4)
        onNode(switchNamed("Count-in")).assertIsOn()
    }

    @Test
    fun collapsingTheFirstDisclosureMovesTheSecondSwitchWithItsRow(): TestResult = renderingTest {
        toggleFlow()
        toggleDisplay()
        settle()
        val originalDisplay = displayBounds()
        val originalFlash = onNode(switchNamed("Beat flash")).fetchSemanticsNode().boundsInRoot
        toggleFlow()
        advance(80)
        val currentDisplay = displayBounds()
        val currentFlash = onNode(switchNamed("Beat flash")).fetchSemanticsNode().boundsInRoot
        assertTrue(currentDisplay.top < originalDisplay.top)
        assertEquals(originalFlash.top - originalDisplay.top, currentFlash.top - currentDisplay.top, 1f)
        val image = frame()
        assertNoTrack(
            image,
            Rect(flowBounds().right - 80f, flowBounds().bottom + 1f, flowBounds().right, currentFlash.top - 1f),
        )
        assertTrue(trackEnergy(image, trackBand(currentFlash, LayoutDirection.Ltr)) > 1f)
        settle()
        onAllNodes(switchRole).assertCountEquals(1)
        onNode(switchNamed("Beat flash")).assertIsOn()
    }

    @Test
    fun selectionSurvivesDismissalAndReopeningWithOneLabelledSwitchPerRow(): TestResult = renderingTest { model ->
        toggleFlow()
        settle()
        onAllNodes(switchRole).assertCountEquals(4)
        onAllNodes(switchNamed("Count-in")).assertCountEquals(1)
        onNode(switchNamed("Count-in")).assertHasClickAction().assertIsOn().performClick()
        settle()
        onNode(switchNamed("Count-in")).assertIsOff()
        assertEquals(false, model.countIn.value)
        runOnUiThread { model.visible.value = false }
        advance(32)
        onAllNodes(switchRole).assertCountEquals(0)
        runOnUiThread { model.visible.value = true }
        advance(32)
        onAllNodes(switchRole).assertCountEquals(0)
        toggleFlow()
        settle()
        onNode(switchNamed("Count-in")).assertIsOff()
        toggleFlow()
        settle()
        onAllNodes(switchRole).assertCountEquals(0)
    }

    @Test
    fun rowAndThumbTapsEachEmitOneCallbackAndExternalStateUpdatesRender(): TestResult = renderingTest { model ->
        toggleFlow()
        settle()
        val row = onNode(switchNamed("Count-in"))
        row.performClick()
        settle()
        row.assertIsOff()
        assertEquals(1, model.countInEvents)
        row.performTouchInput { click(Offset(width - 32f, centerY)) }
        settle()
        row.assertIsOn()
        assertEquals(2, model.countInEvents)
        runOnUiThread { model.countIn.value = false }
        settle()
        row.assertIsOff()
        val rowBounds = row.fetchSemanticsNode().boundsInRoot
        val offEnergy = trackEnergy(frame(), trackBand(rowBounds, LayoutDirection.Ltr))
        runOnUiThread { model.countIn.value = true }
        advance(64)
        row.assertIsOn()
        val changingEnergy = trackEnergy(frame(), trackBand(rowBounds, LayoutDirection.Ltr))
        settle()
        val onEnergy = trackEnergy(frame(), trackBand(rowBounds, LayoutDirection.Ltr))
        assertTrue(changingEnergy > offEnergy + 1f && changingEnergy < onEnergy * 0.98f)
        assertEquals(2, model.countInEvents)
    }

    @Test
    fun thumbDragsSnapAndEmitOneCallbackInBothDirections(): TestResult = renderingTest { model ->
        assertThumbDrag(model, LayoutDirection.Ltr)
    }

    @Test
    fun thumbDragsFollowRtlAndSnapWithoutAnUnchangedCallback(): TestResult = renderingTest(
        direction = LayoutDirection.Rtl,
    ) { model ->
        assertThumbDrag(model, LayoutDirection.Rtl)
    }

    @Test
    fun keyboardFocusVisitsEachLabelledRowOnceAndSpaceEmitsOneCallback(): TestResult = renderingTest { model ->
        toggleFlow()
        settle()
        val countIn = onNode(switchNamed("Count-in"))
        countIn.requestFocus()
        advance(32)
        countIn.assertIsFocused().assertIsOn()
        countIn.performKeyInput { pressKey(Key.Spacebar) }
        settle()
        countIn.assertIsFocused().assertIsOff()
            .assert(hasContentDescription("Count-in. One bar to ease you in"))
        assertEquals(false, model.countIn.value, "Space must toggle the focused labelled row")
        assertEquals(1, model.countInEvents, "Space must emit one callback despite the interactive child switch")
        onAllNodes(switchNamed("Count-in")).assertCountEquals(1)
        countIn.performKeyInput { pressKey(Key.Tab) }
        advance(32)
        val screenAwake = onNode(switchNamed("Keep screen awake"))
        screenAwake.assertIsFocused().assertIsOn()
        screenAwake.performKeyInput { pressKey(Key.Tab) }
        advance(32)
        onNode(switchNamed("Background playback")).assertIsFocused().assertIsOn()
        countIn.assertIsOff().assert(hasContentDescription("Count-in. One bar to ease you in"))
        assertEquals(1, model.countInEvents, "Tab must skip decorative child controls without toggling the previous row")
        onAllNodes(switchRole).assertCountEquals(4)
    }

    @Test
    fun cancelledDragAfterCrossingTheMidpointKeepsStateAndRestoresThumbPixels(): TestResult = renderingTest { model ->
        toggleFlow()
        settle()
        val row = onNode(switchNamed("Count-in"))
        val bounds = row.fetchSemanticsNode().boundsInRoot
        val onThumb = Offset(bounds.right - 22f, bounds.center.y)
        val offThumb = Offset(bounds.right - 42f, bounds.center.y)
        val original = frame()
        row.performTouchInput {
            down(Offset(width - 22f, centerY))
            moveTo(Offset(width - 60f, centerY), delayMillis = 32)
        }
        advance(32)
        val dragging = frame()
        val originalOffPixel = pixel(original, offThumb)
        val originalOnPixel = pixel(original, onThumb)
        val draggingOffPixel = pixel(dragging, offThumb)
        val movedDifference = colorDifference(draggingOffPixel, originalOffPixel)
        val thumbDifference = colorDifference(draggingOffPixel, originalOnPixel)
        assertTrue(
            movedDifference > 0.15f,
            "Held drag must visibly cross the midpoint. bounds=$bounds off=$offThumb originalOff=${rgb(originalOffPixel)} draggingOff=${rgb(draggingOffPixel)} difference=$movedDifference",
        )
        assertTrue(
            thumbDifference < 0.05f,
            "Held drag must place thumb pixels at the opposite endpoint. off=$offThumb on=$onThumb originalOn=${rgb(originalOnPixel)} draggingOff=${rgb(draggingOffPixel)} difference=$thumbDifference",
        )
        row.assertIsOn()
        assertEquals(true, model.countIn.value, "The model must remain on while a drag is held")
        assertEquals(0, model.countInEvents, "A held drag must not emit a callback")
        row.performTouchInput { cancel() }
        cancelScenePointerInput()
        settle()
        row.assertIsOn()
        assertEquals(true, model.countIn.value, "Native scene cancellation must preserve the model")
        assertEquals(0, model.countInEvents, "Native scene cancellation must not emit a callback")
        val restored = frame()
        val restoredOnPixel = pixel(restored, onThumb)
        val restoredOffPixel = pixel(restored, offThumb)
        val restoredOnDifference = colorDifference(restoredOnPixel, originalOnPixel)
        val restoredOffDifference = colorDifference(restoredOffPixel, originalOffPixel)
        assertTrue(
            restoredOnDifference < 0.02f,
            "Canceled thumb must return to the original on position. on=$onThumb original=${rgb(originalOnPixel)} restored=${rgb(restoredOnPixel)} difference=$restoredOnDifference",
        )
        assertTrue(
            restoredOffDifference < 0.02f,
            "Canceled thumb must uncover the original off-position track. off=$offThumb original=${rgb(originalOffPixel)} restored=${rgb(restoredOffPixel)} difference=$restoredOffDifference",
        )
    }

    @Test
    fun offThumbPixelsHaveContrastInEveryBundledLightPalette(): TestResult = offThumbPaletteTest(dark = false)

    @Test
    fun offThumbPixelsHaveContrastInEveryBundledDarkPalette(): TestResult = offThumbPaletteTest(dark = true)

    private fun offThumbPaletteTest(dark: Boolean): TestResult = runComposeUiTest(
        effectContext = object : MotionDurationScale {
            override val scaleFactor = 1f
        },
        testTimeout = 15.seconds,
    ) {
        mainClock.autoAdvance = false
        val palette = mutableStateOf(AppColorScheme.BLACKNWHITE)
        setContent {
            val colors = if (dark) palette.value.darkColor else palette.value.lightColor
            MaterialTheme(colorScheme = colors) {
                Box(Modifier.fillMaxSize().background(colors.surface).testTag("frame")) {
                    Box(Modifier.width(360.dp).padding(16.dp)) {
                        SettingsSwitch("Count-in", false, {}, "One bar to ease you in")
                    }
                }
            }
        }
        advance(32)
        AppColorScheme.defaultValues().forEach { scheme ->
            runOnUiThread { palette.value = scheme }
            settle()
            val row = onNode(switchNamed("Count-in"))
            row.assertIsOff()
            val bounds = row.fetchSemanticsNode().boundsInRoot
            val rendered = frame()
            val track = pixel(rendered, Offset(bounds.right - 15f, bounds.center.y))
            for (dx in listOf(-4f, 0f, 4f)) {
                for (dy in listOf(-4f, 0f, 4f)) {
                    val thumb = pixel(rendered, Offset(bounds.right - 42f + dx, bounds.center.y + dy))
                    val ratio = contrast(thumb, track)
                    assertTrue(ratio >= 4.45f, "Rendered off thumb is missing or lacks contrast in $scheme dark=$dark: $ratio")
                }
            }
        }
    }

    @OptIn(InternalComposeUiApi::class)
    private fun ComposeUiTest.cancelScenePointerInput() {
        runOnUiThread { (this@cancelScenePointerInput as SkikoComposeUiTest).scene.cancelPointerInput() }
    }

    private fun rgb(color: Color): String = "(${color.red}, ${color.green}, ${color.blue}, alpha=${color.alpha})"

    private fun pixel(image: ImageBitmap, position: Offset): Color = image.toPixelMap()[position.x.toInt(), position.y.toInt()]

    private fun colorDifference(first: Color, second: Color): Float = maxOf(
        abs(first.red - second.red),
        abs(first.green - second.green),
        abs(first.blue - second.blue),
    )

    private fun contrast(first: Color, second: Color): Float {
        val firstLuminance = first.luminance()
        val secondLuminance = second.luminance()
        return (maxOf(firstLuminance, secondLuminance) + 0.05f) / (minOf(firstLuminance, secondLuminance) + 0.05f)
    }

    private fun ComposeUiTest.assertThumbDrag(model: SwitchFixtureModel, direction: LayoutDirection) {
        toggleFlow()
        settle()
        val row = onNode(switchNamed("Count-in"))
        val sign = if (direction == LayoutDirection.Ltr) 1f else -1f
        row.performTouchInput {
            val trackCenter = if (direction == LayoutDirection.Ltr) width - 32f else 32f
            swipe(Offset(trackCenter + 10f * sign, centerY), Offset(trackCenter - 28f * sign, centerY))
        }
        settle()
        row.assertIsOff()
        assertEquals(1, model.countInEvents)
        row.performTouchInput {
            val trackCenter = if (direction == LayoutDirection.Ltr) width - 32f else 32f
            swipe(Offset(trackCenter - 10f * sign, centerY), Offset(trackCenter - 44f * sign, centerY))
        }
        settle()
        row.assertIsOff()
        assertEquals(1, model.countInEvents)
        row.performTouchInput {
            val trackCenter = if (direction == LayoutDirection.Ltr) width - 32f else 32f
            swipe(Offset(trackCenter - 10f * sign, centerY), Offset(trackCenter + 28f * sign, centerY))
        }
        settle()
        row.assertIsOn()
        assertEquals(2, model.countInEvents)
    }

    private fun renderingTest(
        dark: Boolean = false,
        direction: LayoutDirection = LayoutDirection.Ltr,
        fontScale: Float = 1f,
        block: suspend ComposeUiTest.(SwitchFixtureModel) -> Unit,
    ): TestResult = runComposeUiTest(
        effectContext = object : MotionDurationScale {
            override val scaleFactor = 1f
        },
        testTimeout = 15.seconds,
    ) {
        mainClock.autoAdvance = false
        val model = SwitchFixtureModel()
        setContent { SwitchFixture(model, dark, direction, fontScale) }
        advance(32)
        block(model)
    }

    private fun ComposeUiTest.assertDisclosureRendering(direction: LayoutDirection = LayoutDirection.Ltr) {
        toggleFlow()
        settle()
        val fullBounds = flowBounds()
        val firstRow = onNode(switchNamed("Count-in")).fetchSemanticsNode().boundsInRoot
        val expanded = frame()
        val referenceEnergy = trackEnergy(expanded, trackBand(firstRow, direction))
        assertTrue(referenceEnergy > 1f, "The settled switch must be present in the captured pixels")
        toggleFlow()
        advance(64)
        val exitBounds = flowBounds()
        val exiting = frame()
        assertTrue(exitBounds.height < fullBounds.height && exitBounds.height > firstRow.bottom - fullBounds.top)
        assertTrackWithinFlow(exiting, exitBounds, direction)
        val exitEnergy = trackEnergy(exiting, trackBand(firstRow, direction))
        assertTrue(exitEnergy > referenceEnergy * 0.02f && exitEnergy < referenceEnergy * 0.98f, "The switch must fade before disposal")
        settle()
        onAllNodes(switchRole).assertCountEquals(0)
        assertTrackWithinFlow(frame(), flowBounds(), direction)
        toggleFlow()
        var capturedEnter = false
        repeat(16) {
            if (!capturedEnter) {
                advance(16)
                val currentBounds = flowBounds()
                val entering = frame()
                assertTrackWithinFlow(entering, currentBounds, direction)
                if (currentBounds.bottom >= firstRow.bottom + 2f && currentBounds.bottom < fullBounds.bottom - 4f) {
                    val enterEnergy = trackEnergy(entering, trackBand(firstRow, direction))
                    assertTrue(enterEnergy > referenceEnergy * 0.02f && enterEnergy < referenceEnergy * 0.98f, "The switch must fade while its full row is inside the expanding clip")
                    capturedEnter = true
                }
            }
        }
        assertTrue(capturedEnter, "The test must observe a real intermediate expansion frame")
        settle()
        onAllNodes(switchRole).assertCountEquals(4)
        assertTrue(trackEnergy(frame(), trackBand(firstRow, direction)) > referenceEnergy * 0.98f)
    }

    private fun ComposeUiTest.advance(milliseconds: Long) {
        mainClock.advanceTimeBy(milliseconds)
        waitForIdle()
    }

    private fun ComposeUiTest.settle() = advance(2_000)

    private fun ComposeUiTest.toggleFlow() = onNode(hasText("Keep your flow.")).performClick()

    private fun ComposeUiTest.toggleDisplay() = onNode(hasText("See the beat.")).performClick()

    private fun ComposeUiTest.frame(): ImageBitmap = onNodeWithTag("frame").captureToImage()

    private fun ComposeUiTest.flowBounds(): Rect = onNodeWithTag("flow").fetchSemanticsNode().boundsInRoot

    private fun ComposeUiTest.displayBounds(): Rect = onNodeWithTag("display").fetchSemanticsNode().boundsInRoot

    private fun assertTrackWithinFlow(image: ImageBitmap, flow: Rect, direction: LayoutDirection) {
        val band = trackBand(flow, direction)
        assertNoTrack(image, Rect(band.left, flow.bottom + 1f, band.right, image.height.toFloat()))
    }

    private fun assertNoTrack(image: ImageBitmap, bounds: Rect) {
        assertTrue(trackEnergy(image, bounds) < 0.05f, "Switch pixels escaped the current disclosure clip: $bounds")
    }

    private fun trackBand(bounds: Rect, direction: LayoutDirection): Rect = when (direction) {
        LayoutDirection.Ltr -> Rect(bounds.right - 80f, bounds.top, bounds.right, bounds.bottom)
        LayoutDirection.Rtl -> Rect(bounds.left, bounds.top, bounds.left + 80f, bounds.bottom)
    }

    private fun trackEnergy(image: ImageBitmap, bounds: Rect): Float {
        if (bounds.width <= 0f || bounds.height <= 0f) return 0f
        val pixels = image.toPixelMap()
        var energy = 0f
        for (y in ceil(bounds.top).toInt().coerceAtLeast(0) until floor(bounds.bottom).toInt().coerceAtMost(image.height)) {
            for (x in ceil(bounds.left).toInt().coerceAtLeast(0) until floor(bounds.right).toInt().coerceAtMost(image.width)) {
                val pixel = pixels[x, y]
                val strength = pixel.green - maxOf(pixel.red, pixel.blue)
                if (strength > 0.02f) energy += strength
            }
        }
        return energy
    }

    private val switchRole = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch)

    private fun switchNamed(title: String): SemanticsMatcher = switchRole and (
        hasContentDescription(title, substring = true) or hasText(title, substring = true)
    )
}

private class SwitchFixtureModel {
    val visible = mutableStateOf(true)
    val countIn = mutableStateOf(true)
    var countInEvents = 0
    val screenAwake = mutableStateOf(true)
    val background = mutableStateOf(true)
    val liveActivity = mutableStateOf(true)
    val flash = mutableStateOf(true)
}

@Composable
private fun SwitchFixture(
    model: SwitchFixtureModel,
    dark: Boolean,
    direction: LayoutDirection,
    fontScale: Float,
) {
    val background = if (dark) Color(0xFF121212) else Color.White
    val primary = if (dark) Color(0xFF00CF72) else Color(0xFF007F46)
    val theme = if (dark) {
        darkColorScheme(primary = primary, surface = background, surfaceContainerLow = background)
    } else {
        lightColorScheme(primary = primary, surface = background, surfaceContainerLow = background)
    }
    CompositionLocalProvider(
        LocalLayoutDirection provides direction,
        LocalDensity provides Density(1f, fontScale),
    ) {
        MaterialTheme(colorScheme = theme) {
            Box(Modifier.fillMaxSize().background(background).testTag("frame")) {
                Column(Modifier.width(360.dp).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (model.visible.value) {
                        Box(Modifier.fillMaxWidth().testTag("flow")) {
                            SettingsDisclosure("Keep your flow.", "Practice & playback", Lucide.Timer) {
                                SettingsSwitch("Count-in", model.countIn.value, { model.countInEvents += 1; model.countIn.value = it }, "One bar to ease you in")
                                SettingsSwitch("Keep screen awake", model.screenAwake.value, { model.screenAwake.value = it }, "While the beat is playing")
                                SettingsSwitch("Background playback", model.background.value, { model.background.value = it }, "Keep going when you leave the app")
                                LiveActivitySettingsRow(model.liveActivity.value, { model.liveActivity.value = it })
                            }
                        }
                        Box(Modifier.fillMaxWidth().testTag("display")) {
                            SettingsDisclosure("See the beat.", "Beat display & motion", Lucide.SlidersHorizontal) {
                                SettingsSwitch("Beat flash", model.flash.value, { model.flash.value = it }, "A little pulse while you play")
                            }
                        }
                    } else {
                        Text("Settings dismissed")
                    }
                }
            }
        }
    }
}
