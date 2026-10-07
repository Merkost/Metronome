package com.merkost.metronome.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.merkost.metronome.ui.cornerRadiusXLarge
import com.merkost.metronome.ui.minimumTouchTargetSize
import com.merkost.metronome.ui.theme.AppColorScheme
import com.merkost.metronome.ui.theme.Typography
import kotlinx.coroutines.test.TestResult
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalTestApi::class)
class AppBottomSheetInsetsRenderingTest {
    @Test
    fun safeAreaConstrainsSurfacePixelsAndNestedContentConsumesInsetsOnce(): TestResult = renderingTest { model ->
        assertSafeFrame(left = 47, top = 59, right = 23, bottom = 34)
        assertNestedContentHasNoExtraInsets()
        onNode(hasText("Safe sheet")).assertIsDisplayed()
        revealFinalAction()
        onNodeWithTag("finalAction").performClick()
        advance(32)
        assertEquals(1, model.dismissals, "The scrolled final action must invoke the body callback once")
        assertFinalActionWithinViewport()
    }

    @Test
    fun negativeAnchoredPlacementCannotLiftTheSurfaceAboveItsSafeTop(): TestResult = renderingTest { model ->
        val initial = geometry()
        listOf(-24.3f, -72.8f, 0f).forEach { offset ->
            runOnUiThread { model.rawOffset.value = offset }
            advance(32)
            assertGeometryEquals(initial, geometry(), "Corrected anchored offset $offset")
            assertSafeFrame(left = 47, top = 59, right = 23, bottom = 34)
        }
        runOnUiThread { model.rawOffset.value = 96f }
        advance(32)
        val moved = geometry()
        assertEquals(initial.getValue("surface").top + 96f, moved.getValue("surface").top, 0.5f, "A downward drag must retain its natural placement")
    }

    @Test
    fun asymmetricLandscapeInsetsKeepLargeRtlContentScrollable(): TestResult = renderingTest(
        dark = true,
        direction = LayoutDirection.Rtl,
        fontScale = 2f,
        width = 600,
        height = 420,
        initialInsets = WindowInsets(left = 67, top = 21, right = 23, bottom = 21),
    ) { model ->
        assertSafeFrame(left = 67, top = 21, right = 23, bottom = 21)
        assertNestedContentHasNoExtraInsets()
        onNode(hasText("Safe sheet")).assertIsDisplayed()
        revealFinalAction()
        onNodeWithTag("finalAction").performClick()
        advance(32)
        assertEquals(1, model.dismissals)
        assertFinalActionWithinViewport()
    }

    @Test
    fun changingBottomObstructionsShrinksTheViewportOnceAndRestoresTheScrollEnd(): TestResult = renderingTest { model ->
        val initial = geometry()
        revealFinalAction()
        assertFinalActionWithinViewport()
        runOnUiThread { model.insets.value = WindowInsets(left = 47, top = 59, right = 23, bottom = 300) }
        advance(64)
        val obscured = geometry()
        assertEquals(initial.getValue("surface"), obscured.getValue("surface"), "A bottom obstruction must not move the safe surface")
        assertEquals(initial.getValue("body").bottom - 266f, obscured.getValue("body").bottom, 0.5f, "The viewport must reserve300px once instead of adding it to34px")
        revealFinalAction()
        assertFinalActionWithinViewport()
        runOnUiThread { model.insets.value = WindowInsets(left = 47, top = 59, right = 23, bottom = 34) }
        advance(64)
        assertEquals(initial.getValue("body"), geometry().getValue("body"), "Removing the obstruction must restore the original viewport")
        revealFinalAction()
        onNodeWithTag("finalAction").performClick()
        advance(32)
        assertEquals(1, model.dismissals)
        assertFinalActionWithinViewport()
    }

    private fun renderingTest(
        dark: Boolean = false,
        direction: LayoutDirection = LayoutDirection.Ltr,
        fontScale: Float = 1f,
        width: Int = 360,
        height: Int = 640,
        initialInsets: WindowInsets = WindowInsets(left = 47, top = 59, right = 23, bottom = 34),
        block: suspend ComposeUiTest.(SheetInsetsFixtureModel) -> Unit,
    ): TestResult = runComposeUiTest(
        effectContext = object : MotionDurationScale {
            override val scaleFactor = 1f
        },
        testTimeout = 20.seconds,
    ) {
        mainClock.autoAdvance = false
        val model = SheetInsetsFixtureModel(initialInsets)
        setContent { SheetInsetsFixture(model, dark, direction, fontScale, width, height) }
        advance(32)
        block(model)
    }

    private fun ComposeUiTest.assertSafeFrame(left: Int, top: Int, right: Int, bottom: Int) {
        val current = geometry()
        val viewport = current.getValue("viewport")
        val surface = current.getValue("surface")
        val body = current.getValue("body")
        assertEquals(viewport.left + left, surface.left, 0.5f, "The painted surface must clear the left cutout")
        assertEquals(viewport.top + top, surface.top, 0.5f, "The painted surface must clear the top cutout")
        assertEquals(viewport.right - right, surface.right, 0.5f, "The painted surface must clear the right cutout")
        assertEquals(viewport.bottom - bottom, body.bottom, 0.5f, "The content viewport must clear the bottom obstruction once")
        assertNoSurfacePixelsOutsideSafeEdges(onNodeWithTag("viewport").captureToImage(), left, top, right)
    }

    private fun ComposeUiTest.assertNestedContentHasNoExtraInsets() {
        val direct = onNodeWithTag("directChild").fetchSemanticsNode().boundsInRoot
        val nested = onNodeWithTag("nestedChild").fetchSemanticsNode().boundsInRoot
        assertEquals(direct.left, nested.left, 0.5f, "Nested content must not add the consumed left inset")
        assertEquals(direct.right, nested.right, 0.5f, "Nested content must not add the consumed right inset")
        assertEquals(direct.height, nested.height, 0.5f, "Nested content must not add consumed vertical insets")
        assertEquals(direct.bottom, nested.top, 0.5f, "Nested content must begin after the direct child without another top gap")
    }

    private fun ComposeUiTest.assertFinalActionWithinViewport() {
        val body = onNodeWithTag("body").fetchSemanticsNode().boundsInRoot
        val action = onNodeWithTag("finalAction").fetchSemanticsNode().boundsInRoot
        assertTrue(action.left >= body.left - 0.5f && action.right <= body.right + 0.5f)
        assertTrue(action.top >= body.top - 0.5f && action.bottom <= body.bottom + 0.5f, "The final action must remain fully within the usable content viewport")
    }

    private fun ComposeUiTest.revealFinalAction() {
        val scrollContainer = onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.ScrollBy))
        repeat(4) {
            scrollContainer.performSemanticsAction(SemanticsActions.ScrollBy) { scroll -> scroll(0f, 1_000f) }
            advance(1_000)
        }
        onNodeWithTag("finalAction").assertIsDisplayed()
    }

    private fun ComposeUiTest.geometry(): Map<String, Rect> = mapOf(
        "viewport" to onNodeWithTag("viewport").fetchSemanticsNode().boundsInRoot,
        "surface" to onNodeWithTag("surface").fetchSemanticsNode().boundsInRoot,
        "body" to onNodeWithTag("body").fetchSemanticsNode().boundsInRoot,
    )

    private fun assertGeometryEquals(expected: Map<String, Rect>, actual: Map<String, Rect>, stage: String) {
        expected.forEach { (name, bounds) ->
            val current = actual.getValue(name)
            assertEquals(bounds.left, current.left, 0.5f, "$stage moved $name left")
            assertEquals(bounds.top, current.top, 0.5f, "$stage moved $name top")
            assertEquals(bounds.right, current.right, 0.5f, "$stage moved $name right")
            assertEquals(bounds.bottom, current.bottom, 0.5f, "$stage moved $name bottom")
        }
    }

    private fun assertNoSurfacePixelsOutsideSafeEdges(image: ImageBitmap, left: Int, top: Int, right: Int) {
        val pixels = image.toPixelMap()
        val backdrop = pixels[0, 0]
        var maximumDifference = 0f
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                if (y < top || x < left || x >= image.width - right) {
                    val pixel = pixels[x, y]
                    maximumDifference = maxOf(maximumDifference, abs(pixel.red - backdrop.red), abs(pixel.green - backdrop.green), abs(pixel.blue - backdrop.blue), abs(pixel.alpha - backdrop.alpha))
                }
            }
        }
        assertTrue(maximumDifference < 0.015f, "Surface pixels must not enter the excluded top/side bands. difference=$maximumDifference")
        val middle = pixels[image.width / 2, (top + 8).coerceAtMost(image.height - 1)]
        val surfaceDifference = maxOf(abs(middle.red - backdrop.red), abs(middle.green - backdrop.green), abs(middle.blue - backdrop.blue))
        assertTrue(surfaceDifference > 0.02f, "The safe region must contain real surface pixels")
    }

    private fun ComposeUiTest.advance(milliseconds: Long) {
        mainClock.advanceTimeBy(milliseconds)
        waitForIdle()
    }
}

private class SheetInsetsFixtureModel(initialInsets: WindowInsets) {
    val insets = mutableStateOf(initialInsets)
    val rawOffset = mutableStateOf(0f)
    var dismissals = 0
}

@Composable
private fun SheetInsetsFixture(
    model: SheetInsetsFixtureModel,
    dark: Boolean,
    direction: LayoutDirection,
    fontScale: Float,
    width: Int,
    height: Int,
) {
    val colors = if (dark) AppColorScheme.MELROSE.darkColor else AppColorScheme.MELROSE.lightColor
    val insets = model.insets.value
    CompositionLocalProvider(
        LocalDensity provides Density(1f, fontScale),
        LocalLayoutDirection provides direction,
    ) {
        MaterialTheme(colorScheme = colors, typography = Typography) {
            Box(Modifier.width(width.dp).height(height.dp).clipToBounds().background(colors.primaryContainer).testTag("viewport")) {
                Surface(
                    modifier = Modifier.appBottomSheetSafeArea(insets) { model.rawOffset.value }
                        .offset { IntOffset(0, model.rawOffset.value.roundToInt()) }
                        .fillMaxSize().testTag("surface"),
                    shape = RoundedCornerShape(topStart = cornerRadiusXLarge, topEnd = cornerRadiusXLarge),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column(Modifier.fillMaxSize().windowInsetsPadding(insets.only(WindowInsetsSides.Bottom))) {
                        AppBottomSheetBody("Safe sheet", { model.dismissals += 1 }, Modifier.testTag("body")) { dismissAnimated ->
                            Box(Modifier.fillMaxWidth().height(24.dp).testTag("directChild"))
                            Box(Modifier.windowInsetsPadding(insets).fillMaxWidth().height(24.dp).testTag("nestedChild"))
                            repeat(24) { index ->
                                Text("Setup row $index", modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp), style = MaterialTheme.typography.bodyLarge)
                            }
                            Button(onClick = dismissAnimated, modifier = Modifier.fillMaxWidth().heightIn(min = minimumTouchTargetSize).testTag("finalAction")) {
                                Text("Final action")
                            }
                        }
                    }
                }
            }
        }
    }
}
