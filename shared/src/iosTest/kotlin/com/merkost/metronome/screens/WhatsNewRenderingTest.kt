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
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
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
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalTestApi::class)
class WhatsNewRenderingTest {
    @Test
    fun releaseSheetShowsOnlyThreeCurrentBenefitsAndDismissesOnce(): TestResult = renderingTest { model ->
        assertReleaseContent()
        releaseText.forEach { text ->
            reveal(onNode(hasText(text), useUnmergedTree = true)).assertIsDisplayed()
            assertTextFits(text)
        }
        assertDoneIsUsable()
        capture("whats-new-1.5.0-light")
        dismissAndAssertRemoved(model)
    }

    @Test
    fun narrowDarkRtlWithDoubleTextKeepsEveryBenefitAndDismissalFullyReachable(): TestResult = renderingTest(
        dark = true,
        direction = LayoutDirection.Rtl,
        fontScale = 2f,
        width = 320,
        height = 640,
    ) { model ->
        assertReleaseContent()
        releaseText.forEach { text ->
            reveal(onNode(hasText(text), useUnmergedTree = true)).assertIsDisplayed()
            assertTextFits(text)
        }
        assertDoneIsUsable()
        capture("whats-new-1.5.0-dark-rtl-2x-end")
        dismissAndAssertRemoved(model)
    }

    private fun renderingTest(
        dark: Boolean = false,
        direction: LayoutDirection = LayoutDirection.Ltr,
        fontScale: Float = 1f,
        width: Int = 360,
        height: Int = 780,
        block: suspend ComposeUiTest.(WhatsNewFixtureModel) -> Unit,
    ): TestResult = runSkikoComposeUiTest(
        size = Size(width.toFloat(), height.toFloat()),
        effectContext = object : MotionDurationScale {
            override val scaleFactor = 1f
        },
        testTimeout = 30.seconds,
    ) {
        mainClock.autoAdvance = false
        val model = WhatsNewFixtureModel()
        val colors = if (dark) AppColorScheme.MELROSE.darkColor else AppColorScheme.MELROSE.lightColor
        setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(1f, fontScale),
                LocalLayoutDirection provides direction,
                LocalContentColor provides colors.onSurface,
            ) {
                MaterialTheme(colorScheme = colors, typography = Typography) {
                    Box(Modifier.width(width.dp).height(height.dp).background(colors.surface).testTag("whatsNewFrame")) {
                        if (model.visible.value) {
                            WhatsNewSheet(version = "1.5.0", onDismiss = model::dismiss)
                        }
                    }
                }
            }
        }
        settle()
        val frame = onNodeWithTag("whatsNewFrame").fetchSemanticsNode().boundsInRoot
        assertEquals(width.toFloat(), frame.width, 0.5f, "The native canvas must preserve the requested viewport width")
        assertEquals(height.toFloat(), frame.height, 0.5f, "The native canvas must preserve the requested viewport height")
        val modal = modalRoot().fetchSemanticsNode().boundsInRoot
        assertEquals(width.toFloat(), modal.width, 0.5f, "The real modal must use the requested native canvas width")
        assertEquals(height.toFloat(), modal.height, 0.5f, "The real modal must use the requested native canvas height")
        assertEquals(0, model.dismissals, "Presenting the release sheet must not dismiss it")
        block(model)
    }

    private fun ComposeUiTest.assertReleaseContent() {
        val actual = onAllNodes(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.Text),
            useUnmergedTree = true,
        ).fetchSemanticsNodes().flatMap { node -> node.config[SemanticsProperties.Text].map { it.text } }
        assertEquals(releaseText.sorted(), actual.sorted(), "The 1.5.0 modal must contain exactly its three current benefits, version and dismissal action")
    }

    private fun ComposeUiTest.assertDoneIsUsable() {
        val action = reveal(onNode(hasText("Got it"))).assertIsEnabled().assertHasClickAction()
        val node = action.fetchSemanticsNode()
        val bounds = Rect(node.positionInRoot, node.size.toSize())
        val parent = generateSequence(node.parent) { it.parent }.first {
            it.config.contains(SemanticsActions.ScrollBy) && it.config.contains(SemanticsProperties.VerticalScrollAxisRange)
        }
        val viewport = parent.boundsInRoot
        val canvas = modalRoot().fetchSemanticsNode().boundsInRoot
        assertTrue(bounds.width >= 48f && bounds.height >= 52f, "Got it must retain a usable complete touch target. action=$bounds")
        assertTrue(bounds.left >= viewport.left - 0.5f && bounds.right <= viewport.right + 0.5f && bounds.top >= viewport.top - 0.5f && bounds.bottom <= viewport.bottom + 0.5f, "Got it must be wholly inside the real sheet scroll viewport. action=$bounds viewport=$viewport")
        assertTrue(bounds.left >= canvas.left - 0.5f && bounds.right <= canvas.right + 0.5f && bounds.top >= canvas.top - 0.5f && bounds.bottom <= canvas.bottom + 0.5f, "Got it must be wholly inside the native canvas. action=$bounds canvas=$canvas")
        assertTextFits("Got it")
    }

    private fun ComposeUiTest.dismissAndAssertRemoved(model: WhatsNewFixtureModel) {
        onNode(hasText("Got it")).performClick()
        settle()
        assertEquals(1, model.dismissals, "The settled dismissal must invoke the release callback exactly once")
        assertTrue(!model.visible.value, "The real release sheet must be removed after dismissal")
        releaseText.forEach { text -> onNode(hasText(text), useUnmergedTree = true).assertDoesNotExist() }
        onNodeWithTag("whatsNewFrame").assertIsDisplayed()
        advance(2_000)
        assertEquals(1, model.dismissals, "A dismissed sheet must not leave another pending callback")
    }

    private fun ComposeUiTest.reveal(target: SemanticsNodeInteraction): SemanticsNodeInteraction {
        repeat(12) { attempt ->
            val node = target.fetchSemanticsNode()
            val parent = generateSequence(node.parent) { it.parent }.firstOrNull {
                it.config.contains(SemanticsActions.ScrollBy) && it.config.contains(SemanticsProperties.VerticalScrollAxisRange)
            }
            if (parent == null) {
                target.assertIsDisplayed()
                val bounds = Rect(node.positionInRoot, node.size.toSize())
                val canvas = modalRoot().fetchSemanticsNode().boundsInRoot
                assertTrue(bounds.left >= canvas.left - 0.5f && bounds.right <= canvas.right + 0.5f && bounds.top >= canvas.top - 0.5f && bounds.bottom <= canvas.bottom + 0.5f, "The fixed sheet heading must remain wholly inside its native canvas")
                return target
            }
            val viewport = parent.boundsInRoot
            val bounds = Rect(node.positionInRoot, node.size.toSize())
            assertTrue(bounds.width <= viewport.width + 0.5f && bounds.height <= viewport.height + 0.5f, "The complete release text must fit its scroll viewport. target=$bounds viewport=$viewport")
            assertTrue(bounds.left >= viewport.left - 0.5f && bounds.right <= viewport.right + 0.5f, "Vertical scrolling must preserve the release text's full width. target=$bounds viewport=$viewport")
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
            val scroll = requireNotNull(parent.config[SemanticsActions.ScrollBy].action)
            runOnUiThread { assertTrue(scroll(0f, delta), "The release text's ancestor must accept a real scroll request") }
            advance(1_000)
            val updated = target.fetchSemanticsNode()
            val updatedParent = generateSequence(updated.parent) { it.parent }.first {
                it.config.contains(SemanticsActions.ScrollBy) && it.config.contains(SemanticsProperties.VerticalScrollAxisRange)
            }
            val after = updatedParent.config[SemanticsProperties.VerticalScrollAxisRange].value()
            assertTrue(abs(after - before) > 0.01f, "The release text remains outside a non-moving scroll boundary. attempt=$attempt before=$before after=$after target=$bounds viewport=$viewport")
        }
        throw AssertionError("The complete release text did not become visible after 12 bounded scroll requests")
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

    private fun ComposeUiTest.modalRoot(): SemanticsNodeInteraction {
        val title = onNode(hasText("What's new"), useUnmergedTree = true).fetchSemanticsNode()
        val root = generateSequence(title) { it.parent }.last()
        return onNode(SemanticsMatcher("The production release sheet's modal root") { it.id == root.id }, useUnmergedTree = true)
    }

    private fun ComposeUiTest.advance(milliseconds: Long) {
        mainClock.advanceTimeBy(milliseconds)
        waitForIdle()
    }

    private fun ComposeUiTest.settle() {
        advance(32)
        advance(2_000)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun ComposeUiTest.capture(name: String) {
        val directory = NSProcessInfo.processInfo.environment["METRONOME_NATIVE_CAPTURE_DIR"] as? String ?: return
        val image = modalRoot().captureToImage()
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

    private val releaseText = listOf(
        "What's new",
        "Version 1.5.0",
        "A simpler welcome.",
        "One graphic screen introduces the app, then opens the instrument without starting playback.",
        "A cleaner instrument.",
        "BPM and the beat take centre stage, with clearer Settings and tempo shortcuts in Set tempo.",
        "Smoother practice.",
        "Stable controls, better sheet scrolling and cleaner sound and practice tools, including large-text and right-to-left layouts.",
        "Got it",
    )
}

private class WhatsNewFixtureModel {
    val visible = mutableStateOf(true)
    var dismissals = 0

    fun dismiss() {
        dismissals++
        visible.value = false
    }
}
