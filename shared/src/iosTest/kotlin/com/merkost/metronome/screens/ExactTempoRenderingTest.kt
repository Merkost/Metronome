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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.merkost.metronome.ui.theme.AppColorScheme
import com.merkost.metronome.ui.theme.Typography
import kotlinx.coroutines.test.TestResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalTestApi::class)
class ExactTempoRenderingTest {
    @Test
    fun quickAdjustmentsStageOnlyTheDraftUntilSetTempo(): TestResult = renderingTest { events ->
        listOf(
            TempoAdjustment.MINUS_FIVE to "95",
            TempoAdjustment.HALF to "47",
            TempoAdjustment.DOUBLE to "94",
            TempoAdjustment.PLUS_FIVE to "99",
        ).forEach { (adjustment, expected) ->
            reveal(onNode(hasContentDescription(adjustment.description))).assertIsEnabled().performClick()
            advance(64)
            assertEquals(expected, draft())
            assertEquals(emptyList(), events.applied, "Quick adjustment must preserve live playback tempo")
            assertEquals(0, events.dismissals)
        }
        reveal(onNode(hasText("Set tempo"))).performClick()
        advance(64)
        assertEquals(listOf(99), events.applied)
        assertEquals(1, events.dismissals)
    }

    @Test
    fun invalidDraftsDisableConfirmationAndQuickAdjustments(): TestResult = renderingTest { events ->
        listOf("", "39", "221", "abc").forEach { invalid ->
            onNode(editable).performTextReplacement(invalid)
            advance(1_000)
            val apply = onNode(hasText("Set tempo"))
            reveal(apply).assertIsNotEnabled().performTouchInput { click() }
            TempoAdjustment.entries.forEach { adjustment ->
                onNode(hasContentDescription(adjustment.description)).assertIsNotEnabled()
            }
            assertEquals(emptyList(), events.applied)
            assertEquals(0, events.dismissals)
        }
        onNode(editable).performTextReplacement("40")
        advance(1_000)
        onNode(hasText("Set tempo")).assertIsEnabled()
        onNode(hasContentDescription(TempoAdjustment.MINUS_FIVE.description)).assertIsNotEnabled()
        onNode(hasContentDescription(TempoAdjustment.HALF.description)).assertIsNotEnabled()
        onNode(hasContentDescription(TempoAdjustment.DOUBLE.description)).assertIsEnabled()
        onNode(hasContentDescription(TempoAdjustment.PLUS_FIVE.description)).assertIsEnabled()
    }

    @Test
    fun keyboardDoneConfirmsTheStagedTempoExactlyOnce(): TestResult = renderingTest { events ->
        onNode(editable).performTextReplacement("123")
        advance(64)
        assertEquals(emptyList(), events.applied)
        onNode(editable).performImeAction()
        advance(64)
        assertEquals(listOf(123), events.applied)
        assertEquals(1, events.dismissals)
    }

    @Test
    fun narrowDarkRtlDoubleTextKeepsAdjustmentsReachableAndBounded(): TestResult = renderingTest(
        dark = true,
        direction = LayoutDirection.Rtl,
        fontScale = 2f,
        width = 320,
    ) { events ->
        onNode(editable).performTextReplacement("220")
        advance(1_000)
        TempoAdjustment.entries.forEach { adjustment ->
            val action = onNode(hasContentDescription(adjustment.description))
            reveal(action)
            val bounds = action.fetchSemanticsNode().boundsInRoot
            assertTrue(bounds.width >= 47.5f && bounds.height >= 47.5f, "Quick adjustments need 48dp targets. bounds=$bounds")
        }
        onNode(hasContentDescription(TempoAdjustment.PLUS_FIVE.description)).assertIsNotEnabled()
        onNode(hasContentDescription(TempoAdjustment.DOUBLE.description)).assertIsNotEnabled()
        onNode(hasContentDescription(TempoAdjustment.MINUS_FIVE.description)).performClick()
        advance(64)
        assertEquals("215", draft())
        assertEquals(emptyList(), events.applied)
        reveal(onNode(hasText("Set tempo"))).performClick()
        assertEquals(listOf(215), events.applied)
        assertEquals(1, events.dismissals)
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

    private fun renderingTest(
        dark: Boolean = false,
        direction: LayoutDirection = LayoutDirection.Ltr,
        fontScale: Float = 1f,
        width: Int = 360,
        block: suspend ComposeUiTest.(TempoEditorEvents) -> Unit,
    ): TestResult = runComposeUiTest(effectContext = object : MotionDurationScale {
        override val scaleFactor = 1f
    }, testTimeout = 30.seconds) {
        mainClock.autoAdvance = false
        val events = TempoEditorEvents()
        setContent {
            val colors = if (dark) AppColorScheme.MELROSE.darkColor else AppColorScheme.MELROSE.lightColor
            CompositionLocalProvider(LocalDensity provides Density(1f, fontScale), LocalLayoutDirection provides direction, LocalContentColor provides colors.onSurface) {
                MaterialTheme(colorScheme = colors, typography = Typography) {
                    Box(Modifier.width(width.dp).height(640.dp).clipToBounds().background(colors.surface)) {
                        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)) {
                            ExactTempoContent(100, { events.applied += it }, { events.dismissals++ })
                        }
                    }
                }
            }
        }
        advance(2_000)
        block(events)
    }

    private fun ComposeUiTest.draft(): String = onNode(editable).fetchSemanticsNode().config[SemanticsProperties.EditableText].text

    private fun ComposeUiTest.advance(milliseconds: Long) {
        mainClock.advanceTimeBy(milliseconds)
        waitForIdle()
    }

    private val editable = SemanticsMatcher.keyIsDefined(SemanticsProperties.EditableText)
}

private class TempoEditorEvents {
    val applied = mutableListOf<Int>()
    var dismissals = 0
}
