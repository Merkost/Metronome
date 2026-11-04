package com.merkost.metronome.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runSkikoComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.merkost.metronome.ui.theme.AppColorScheme
import com.merkost.metronome.ui.theme.Typography
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.test.TestResult
import platform.Foundation.NSProcessInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalTestApi::class)
class WelcomeRenderingTest {
    @Test
    fun oneGraphicScreenExplainsThreeBenefitsWithOnePrimaryActionAndNoSetupForm(): TestResult = renderingTest { model ->
        onNodeWithTag("welcomeWordmark").assertIsDisplayed().assert(hasText("Metronome"))
        assertTextFits("Metronome")
        onNode(hasText("Find your rhythm.")).assertIsDisplayed()
        onNode(hasText("A steady beat, at your pace.")).assertIsDisplayed()
        onNodeWithTag("welcomeGraphic").assertIsDisplayed()
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
        features.forEach { (key, title, description) ->
            onNodeWithTag("welcomeFeature-$key").assertIsDisplayed()
                .assert(hasText(title)).assert(hasText(description))
                .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
        }
        onNodeWithTag("welcomeAction").assertIsEnabled().assertHasClickAction().assert(hasText("Get started"))
        assertActionLabelCentered("Get started")
        onNode(hasText("Next")).assertDoesNotExist()
        onNode(hasContentDescription("Previous page")).assertDoesNotExist()
        onNode(hasContentDescription("Page 1 of 3")).assertDoesNotExist()
        onNode(hasText("Pick a starting pace.")).assertDoesNotExist()
        onNode(hasText("Pick your click")).assertDoesNotExist()
        assertEquals(0, model.completionCalls)
        assertEquals(0, model.skipCalls)
        capture("welcome-single-light")
    }

    @Test
    fun getStartedCompletesOnceAndPreservesTheButtonAndExplanationGeometry(): TestResult = renderingTest { model ->
        val original = geometry()
        onNodeWithTag("welcomeAction").performClick()
        settle()
        onNodeWithTag("welcomeAction").assertIsNotEnabled().assert(hasText("Getting ready…"))
        onNode(hasText("Skip")).assertIsNotEnabled()
        assertGeometryEquals(original, geometry(), "Saving onboarding")
        assertEquals(1, model.completionCalls)
        assertEquals(0, model.skipCalls)
    }

    @Test
    fun skipRemainsSeparateAndBlocksFurtherActionsWhileSaving(): TestResult = renderingTest { model ->
        onNode(hasText("Skip")).performClick()
        settle()
        onNode(hasText("Skip")).assertIsNotEnabled().performTouchInput { click() }
        onNodeWithTag("welcomeAction").assertIsNotEnabled().performTouchInput { click() }
        settle()
        assertEquals(1, model.skipCalls)
        assertEquals(0, model.completionCalls)
    }

    @Test
    fun capturedCompletionAndSkipActionsAcceptOnlyTheFirstActionBeforeRecomposition(): TestResult = renderingTest { model ->
        val primary = requireNotNull(onNodeWithTag("welcomeAction").fetchSemanticsNode().config[SemanticsActions.OnClick].action)
        val skip = requireNotNull(onNode(hasText("Skip")).fetchSemanticsNode().config[SemanticsActions.OnClick].action)
        runOnUiThread {
            primary()
            primary()
            skip()
        }
        assertEquals(1, model.completionCalls)
        assertEquals(0, model.skipCalls)
        settle()
        onNodeWithTag("welcomeAction").assertIsNotEnabled()
        onNode(hasText("Skip")).assertIsNotEnabled()
    }

    @Test
    fun capturedSkipAndCompletionActionsAcceptOnlyTheFirstSkipBeforeRecomposition(): TestResult = renderingTest { model ->
        val primary = requireNotNull(onNodeWithTag("welcomeAction").fetchSemanticsNode().config[SemanticsActions.OnClick].action)
        val skip = requireNotNull(onNode(hasText("Skip")).fetchSemanticsNode().config[SemanticsActions.OnClick].action)
        runOnUiThread {
            skip()
            skip()
            primary()
        }
        assertEquals(1, model.skipCalls)
        assertEquals(0, model.completionCalls)
        settle()
        onNodeWithTag("welcomeAction").assertIsNotEnabled()
        onNode(hasText("Skip")).assertIsNotEnabled()
    }

    @Test
    fun persistenceFailureShowsAnAccessibleRetryOnTheSameWelcomeScreen(): TestResult = renderingTest { model ->
        onNodeWithTag("welcomeAction").performClick()
        settle()
        runOnUiThread {
            model.saving.value = false
            model.error.value = "Could not save. Please try again."
        }
        settle()
        onNode(hasText("Could not save. Please try again."))
            .assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
        onNodeWithTag("welcomeAction").assertIsEnabled().assert(hasText("Get started")).performClick()
        settle()
        assertEquals(2, model.completionCalls)
        assertEquals(0, model.skipCalls)
        onNodeWithTag("welcomeAction").assertIsNotEnabled()
        onNode(hasText("Could not save. Please try again.")).assertDoesNotExist()
    }

    @Test
    fun narrowDarkRtlWithDoubleTextKeepsEveryBenefitScrollableAndSavingFeedbackStable(): TestResult = renderingTest(
        dark = true,
        direction = LayoutDirection.Rtl,
        fontScale = 2f,
        width = 320,
        height = 640,
    ) { model ->
        onNodeWithTag("welcomeWordmark").assertDoesNotExist()
        onNodeWithTag("welcomeBrandMark").assertIsDisplayed().assert(hasContentDescription("Metronome"))
        val headerAction = onNode(hasText("Skip")).assertIsDisplayed().assertIsEnabled().fetchSemanticsNode().boundsInRoot
        val headerViewport = onNodeWithTag("welcomeFrame").fetchSemanticsNode().boundsInRoot
        assertTrue(headerAction.width >= 48f && headerAction.height >= 48f, "Skip must keep a usable touch target at large text")
        assertTrue(headerAction.left >= headerViewport.left && headerAction.right <= headerViewport.right && headerAction.top >= headerViewport.top && headerAction.bottom <= headerViewport.bottom, "The header action must stay inside the compact RTL viewport")
        assertTextFits("Skip")
        reveal(onNode(hasText("Find your rhythm."), useUnmergedTree = true)).assertIsDisplayed()
        assertTextFits("Find your rhythm.")
        reveal(onNode(hasText("A steady beat, at your pace."), useUnmergedTree = true)).assertIsDisplayed()
        assertTextFits("A steady beat, at your pace.")
        features.forEach { (key, title, description) ->
            reveal(onNodeWithTag("welcomeFeature-$key")).assertIsDisplayed()
            assertTextFits(title)
            assertTextFits(description)
        }
        val viewport = onNodeWithTag("welcomeFrame").fetchSemanticsNode().boundsInRoot
        val original = actionBounds()
        assertTrue(original.width >= 48f && original.height >= 48f, "The primary action must keep a usable touch target")
        assertTrue(original.left >= viewport.left && original.right <= viewport.right && original.top >= viewport.top && original.bottom <= viewport.bottom, "The primary action must remain within the compact viewport")
        onNodeWithTag("welcomeAction").assertIsDisplayed().assertIsEnabled()
        assertActionLabelCentered("Get started")
        capture("welcome-single-dark-rtl-2x-end")
        onNodeWithTag("welcomeAction").performClick()
        settle()
        assertRectEquals(original, actionBounds(), "Large-text saving feedback")
        assertTextFits("Getting ready…")
        assertActionLabelCentered("Getting ready…")
        assertEquals(1, model.completionCalls)
        assertEquals(0, model.skipCalls)
    }

    @Test
    fun widerDarkWelcomeCentersItsContentAndActionWithinTheReadableWidth(): TestResult = renderingTest(
        dark = true,
        width = 600,
        height = 900,
    ) { model ->
        val viewport = onNodeWithTag("welcomeFrame").fetchSemanticsNode().boundsInRoot
        val content = onNodeWithTag("welcomeContent").fetchSemanticsNode().boundsInRoot
        val action = actionBounds()
        assertTrue(content.width <= 480.5f, "Wide welcome content must retain the app's readable maximum width")
        assertTrue(action.width <= 480.5f, "The primary action must retain the readable maximum width")
        assertEquals(viewport.center.x, content.center.x, 0.5f, "Wide welcome content must be centered")
        assertEquals(viewport.center.x, action.center.x, 0.5f, "Wide welcome action must be centered")
        assertTrue(action.left >= viewport.left && action.right <= viewport.right && action.top >= viewport.top && action.bottom <= viewport.bottom, "The primary action must remain fully inside the actual wide viewport")
        onNodeWithTag("welcomeAction").assertIsDisplayed()
        features.forEach { (key, title, description) ->
            onNodeWithTag("welcomeFeature-$key").assertIsDisplayed()
            assertTextFits(title)
            assertTextFits(description)
        }
        capture("welcome-single-tablet-dark")
        assertEquals(0, model.completionCalls)
        assertEquals(0, model.skipCalls)
    }

    @Test
    fun reducedMotionShowsSavingImmediatelyWithoutMovingOrAnimatingTheExplanation(): TestResult = renderingTest(motionScale = 0f) { model ->
        val original = geometry()
        val before = onNodeWithTag("welcomeContent").captureToImage()
        onNodeWithTag("welcomeAction").performClick()
        advance(32)
        onNodeWithTag("welcomeAction").assertIsNotEnabled().assert(hasText("Getting ready…"))
        val immediate = onNodeWithTag("welcomeContent").captureToImage()
        assertGeometryEquals(original, geometry(), "Reduced-motion completion")
        assertTrue(pixelDifference(before, immediate) < 0.05f, "Completion must leave the benefit explanation in place")
        settle()
        assertTrue(pixelDifference(immediate, onNodeWithTag("welcomeContent").captureToImage()) < 0.05f, "Reduced motion must leave no lingering decorative animation")
        capture("welcome-single-reduced-saving")
        assertEquals(1, model.completionCalls)
        assertEquals(0, model.skipCalls)
    }

    private fun renderingTest(
        dark: Boolean = false,
        direction: LayoutDirection = LayoutDirection.Ltr,
        fontScale: Float = 1f,
        motionScale: Float = 1f,
        width: Int = 360,
        height: Int = 780,
        block: suspend ComposeUiTest.(WelcomeFixtureModel) -> Unit,
    ): TestResult = runSkikoComposeUiTest(
        size = Size(width.toFloat(), height.toFloat()),
        effectContext = object : MotionDurationScale {
            override val scaleFactor = motionScale
        },
        testTimeout = 30.seconds,
    ) {
        mainClock.autoAdvance = false
        val model = WelcomeFixtureModel()
        val colors = if (dark) AppColorScheme.MELROSE.darkColor else AppColorScheme.MELROSE.lightColor
        setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(1f, fontScale),
                LocalLayoutDirection provides direction,
                LocalContentColor provides colors.onSurface,
            ) {
                MaterialTheme(colorScheme = colors, typography = Typography) {
                    Box(Modifier.width(width.dp).height(height.dp).background(colors.surface).testTag("welcomeFrame")) {
                        WelcomeScreen(model.saving.value, model.error.value, model::complete, model::skip)
                    }
                }
            }
        }
        settle()
        val frame = onNodeWithTag("welcomeFrame").fetchSemanticsNode().boundsInRoot
        assertEquals(width.toFloat(), frame.width, 0.5f, "The native canvas must preserve the requested viewport width")
        assertEquals(height.toFloat(), frame.height, 0.5f, "The native canvas must preserve the requested viewport height")
        block(model)
    }

    private fun ComposeUiTest.geometry(): Map<String, Rect> = buildMap {
        put("action", actionBounds())
        put("content", onNodeWithTag("welcomeContent").fetchSemanticsNode().boundsInRoot)
        put("graphic", onNodeWithTag("welcomeGraphic").fetchSemanticsNode().boundsInRoot)
        features.forEach { (key, _, _) -> put(key, onNodeWithTag("welcomeFeature-$key").fetchSemanticsNode().boundsInRoot) }
    }

    private fun ComposeUiTest.reveal(target: SemanticsNodeInteraction): SemanticsNodeInteraction {
        repeat(12) { attempt ->
            val node = target.fetchSemanticsNode()
            val parent = generateSequence(node.parent) { it.parent }.firstOrNull {
                it.config.contains(SemanticsActions.ScrollBy) && it.config.contains(SemanticsProperties.VerticalScrollAxisRange)
            }
            if (parent == null) {
                target.assertIsDisplayed()
                return target
            }
            val viewport = parent.boundsInRoot
            val bounds = Rect(node.positionInRoot, node.size.toSize())
            assertTrue(bounds.width <= viewport.width + 0.5f && bounds.height <= viewport.height + 0.5f, "The complete explanation must fit its scroll viewport. target=$bounds viewport=$viewport")
            assertTrue(bounds.left >= viewport.left - 0.5f && bounds.right <= viewport.right + 0.5f, "Vertical scrolling must preserve the explanation's full width. target=$bounds viewport=$viewport")
            val delta = when {
                bounds.top < viewport.top - 0.5f -> bounds.top - viewport.top
                bounds.bottom > viewport.bottom + 0.5f -> bounds.bottom - viewport.bottom
                else -> 0f
            }
            if (delta == 0f) {
                target.assertIsDisplayed()
                return target
            }
            val before = parent.config[SemanticsProperties.VerticalScrollAxisRange].value()
            val action = requireNotNull(parent.config[SemanticsActions.ScrollBy].action)
            runOnUiThread { assertTrue(action(0f, delta), "The explanation's ancestor must accept a real scroll request") }
            advance(1_000)
            val updated = target.fetchSemanticsNode()
            val updatedParent = generateSequence(updated.parent) { it.parent }.first {
                it.config.contains(SemanticsActions.ScrollBy) && it.config.contains(SemanticsProperties.VerticalScrollAxisRange)
            }
            val after = updatedParent.config[SemanticsProperties.VerticalScrollAxisRange].value()
            assertTrue(kotlin.math.abs(after - before) > 0.01f, "The explanation remains outside a non-moving scroll boundary. attempt=$attempt before=$before after=$after target=$bounds viewport=$viewport")
        }
        throw AssertionError("The complete explanation did not become visible after 12 bounded scroll requests")
    }

    private fun ComposeUiTest.assertTextFits(text: String) {
        val node = onNode(hasText(text), useUnmergedTree = true)
        val layouts = mutableListOf<TextLayoutResult>()
        node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { assertTrue(it(layouts)) }
        val layout = layouts.single()
        val bounds = node.fetchSemanticsNode().boundsInRoot
        assertTrue(layout.multiParagraph.intrinsics.minIntrinsicWidth <= bounds.width + 0.5f, "$text must fit whole words without forced breaks")
        assertTrue(!layout.multiParagraph.didExceedMaxLines, "$text must not lose lines")
        assertTrue(layout.multiParagraph.height <= bounds.height + 0.5f, "$text must fit its rendered text height")
        repeat(layout.lineCount) { line -> assertTrue(!layout.isLineEllipsized(line), "$text must remain fully readable") }
    }

    private fun ComposeUiTest.actionBounds(): Rect = onNodeWithTag("welcomeAction").fetchSemanticsNode().boundsInRoot
    private fun ComposeUiTest.frame(): ImageBitmap = onNodeWithTag("welcomeFrame").captureToImage()

    private fun ComposeUiTest.assertActionLabelCentered(text: String) {
        val label = onNode(hasText(text), useUnmergedTree = true)
        val layouts = mutableListOf<TextLayoutResult>()
        label.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { assertTrue(it(layouts)) }
        val layout = layouts.single()
        val bounds = label.fetchSemanticsNode().boundsInRoot
        val glyphCenter = bounds.top + (layout.getLineTop(0) + layout.getLineBottom(layout.lineCount - 1)) / 2f
        val indicator = onNodeWithTag("welcomeActionIndicator", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        assertEquals(indicator.center.y, bounds.center.y, 0.5f, "$text must retain a centered intrinsic text box beside its indicator")
        assertTrue(kotlin.math.abs(indicator.center.y - glyphCenter) <= 1f, "$text must remain visually aligned with its indicator within one native pixel of font leading")
    }

    private fun ComposeUiTest.advance(milliseconds: Long) {
        mainClock.advanceTimeBy(milliseconds)
        waitForIdle()
    }

    private fun ComposeUiTest.settle() = advance(2_000)

    private fun assertGeometryEquals(expected: Map<String, Rect>, actual: Map<String, Rect>, label: String) {
        expected.forEach { (key, bounds) -> assertRectEquals(bounds, actual.getValue(key), "$label $key") }
    }

    private fun assertRectEquals(expected: Rect, actual: Rect, label: String) {
        assertEquals(expected.left, actual.left, 0.5f, "$label moved left")
        assertEquals(expected.top, actual.top, 0.5f, "$label moved top")
        assertEquals(expected.right, actual.right, 0.5f, "$label moved right")
        assertEquals(expected.bottom, actual.bottom, 0.5f, "$label moved bottom")
    }

    private fun pixelDifference(first: ImageBitmap, second: ImageBitmap): Float {
        assertEquals(first.width, second.width)
        assertEquals(first.height, second.height)
        val before = first.toPixelMap()
        val after = second.toPixelMap()
        var difference = 0f
        for (y in 0 until first.height) for (x in 0 until first.width) {
            val left = before[x, y]
            val right = after[x, y]
            difference += maxOf(kotlin.math.abs(left.red - right.red), kotlin.math.abs(left.green - right.green), kotlin.math.abs(left.blue - right.blue))
        }
        return difference
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun ComposeUiTest.capture(name: String) {
        val directory = NSProcessInfo.processInfo.environment["METRONOME_NATIVE_CAPTURE_DIR"] as? String ?: return
        val image = frame()
        val path = "$directory/$name.ppm"
        val stream = platform.posix.fopen(path, "wb") ?: error("Cannot create native capture $path")
        try {
            platform.posix.fputs("P6\n${image.width} ${image.height}\n255\n", stream)
            val pixels = image.toPixelMap()
            for (y in 0 until image.height) for (x in 0 until image.width) {
                val pixel = pixels[x, y]
                platform.posix.fputc((pixel.red * 255).toInt().coerceIn(0, 255), stream)
                platform.posix.fputc((pixel.green * 255).toInt().coerceIn(0, 255), stream)
                platform.posix.fputc((pixel.blue * 255).toInt().coerceIn(0, 255), stream)
            }
        } finally {
            platform.posix.fclose(stream)
        }
    }

    private val features = listOf(
        Triple("pace", "Your pace", "Tap a tempo. Find your flow."),
        Triple("sound", "Your sound", "Clicks, accents and subdivisions."),
        Triple("practice", "Your practice", "Timers, trainers and saved routines."),
    )
}

private class WelcomeFixtureModel {
    val saving = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    var completionCalls = 0
    var skipCalls = 0

    fun complete() {
        completionCalls++
        error.value = null
        saving.value = true
    }

    fun skip() {
        skipCalls++
        error.value = null
        saving.value = true
    }
}
