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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.merkost.metronome.model.ThemeMode
import com.merkost.metronome.ui.theme.AppColorScheme
import com.merkost.metronome.ui.theme.Typography
import kotlinx.coroutines.test.TestResult
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalTestApi::class)
class SettingsAppearanceRenderingTest {
    @Test
    fun themeSelectionKeepsControlsLabelsAndFollowingContentInPlace(): TestResult = renderingTest { model ->
        assertThemeSelectionGeometry(model)
    }

    @Test
    fun paletteSelectionKeepsControlsLabelsAndFollowingContentInPlace(): TestResult = renderingTest { model ->
        assertPaletteSelectionGeometry(model)
    }

    @Test
    fun appearanceSelectionStaysStableAtNarrowRtlWithLargerText(): TestResult = renderingTest(
        dark = true,
        direction = LayoutDirection.Rtl,
        fontScale = 1.5f,
    ) { model ->
        assertThemeSelectionGeometry(model)
        assertPaletteSelectionGeometry(model)
    }

    @Test
    fun appearanceSelectionStaysStableWithDoubleSizeText(): TestResult = renderingTest(fontScale = 2f) { model ->
        val layouts = mutableListOf<TextLayoutResult>()
        onNode(hasText("System"), useUnmergedTree = true).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
            assertTrue(action(layouts), "System must expose its real text layout")
        }
        val systemLabel = layouts.single()
        val labelBounds = onNode(hasText("System"), useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        val intrinsicWidth = systemLabel.multiParagraph.intrinsics.maxIntrinsicWidth
        assertEquals(1, systemLabel.lineCount, "System must remain readable on one line at double font size. width=${systemLabel.size.width} intrinsicWidth=${systemLabel.multiParagraph.intrinsics.maxIntrinsicWidth}")
        assertTrue(intrinsicWidth <= labelBounds.width + 0.5f, "System glyphs must fit the rendered label width. intrinsicWidth=$intrinsicWidth bounds=$labelBounds")
        assertTrue(systemLabel.multiParagraph.height <= labelBounds.height + 0.5f, "System glyphs must fit the rendered label height. paragraphHeight=${systemLabel.multiParagraph.height} bounds=$labelBounds")
        assertTrue(!systemLabel.multiParagraph.didExceedMaxLines && !systemLabel.isLineEllipsized(0), "System must remain fully visible without ellipsis at double font size")
        assertEquals(6, systemLabel.getLineEnd(0, visibleEnd = true), "System must display every character at double font size")
        assertThemeSelectionGeometry(model)
        assertPaletteSelectionGeometry(model)
    }

    @Test
    fun appearanceGroupsExposeExactlyOneSelectedRadioAndEmitOneSelection(): TestResult = renderingTest { model ->
        onAllNodes(radioRole).assertCountEquals(8)
        listOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM).forEachIndexed { index, mode ->
            onNode(hasText(mode.label)).assertHasClickAction().performClick()
            advance(32)
            assertEquals(mode, model.state.value.themeMode)
            assertEquals(index + 1, model.themeSelections.size)
            ThemeMode.entries.forEach { choice ->
                onNode(hasText(choice.label))
                    .assert(radioRole)
                    .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, choice == mode))
            }
            settle()
        }
        palettes.forEachIndexed { index, (scheme, _) ->
            onNode(paletteNamed(scheme)).assertHasClickAction().performClick()
            advance(32)
            assertEquals(scheme, model.state.value.colorScheme)
            assertEquals(index + 1, model.paletteSelections.size)
            palettes.forEach { (choice, _) ->
                onNode(paletteNamed(choice))
                    .assert(radioRole)
                    .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, choice == scheme))
            }
            settle()
        }
    }

    @Test
    fun rapidThemeAndPaletteReversalsKeepGeometryAndFinishOnTheLastChoices(): TestResult = renderingTest { model ->
        val initial = geometry()
        val original = frame()
        val choices = listOf(
            ThemeMode.LIGHT to AppColorScheme.MELROSE,
            ThemeMode.DARK to AppColorScheme.PINK_LACE,
            ThemeMode.LIGHT to AppColorScheme.PERIWINKLE,
            ThemeMode.SYSTEM to AppColorScheme.BLACKNWHITE,
            ThemeMode.DARK to AppColorScheme.MINT_GREEN,
        )
        choices.forEachIndexed { index, (mode, palette) ->
            onNode(hasText(mode.label)).performClick()
            onNode(paletteNamed(palette)).performClick()
            advance(48)
            assertGeometryEquals(initial, geometry(), "During rapid reversal $index")
            assertOneSelectedChoice(mode, palette)
            assertEquals(index + 1, model.themeSelections.size, "Each theme selection must emit one callback")
            assertEquals(index + 1, model.paletteSelections.size, "Each palette selection must emit one callback")
            assertUnchangedPixels(original, frame(), initial.getValue("following"))
        }
        settle()
        assertGeometryEquals(initial, geometry(), "After rapid reversals")
        assertOneSelectedChoice(ThemeMode.DARK, AppColorScheme.MINT_GREEN)
        assertEquals(choices.map { it.first }, model.themeSelections)
        assertEquals(choices.map { it.second }, model.paletteSelections)
        assertTrue(pixelDifference(original, frame(), themeGroupBounds(initial)) > 1f, "The final theme selection must be visible after rapid reversals")
        assertTrue(pixelDifference(original, frame(), initial.getValue("palette.Mint")) > 1f, "The final palette selection must be visible after rapid reversals")
    }

    @Test
    fun reducedMotionSelectionsKeepGeometryAndRenderTheFinalChoicesImmediately(): TestResult = renderingTest(motionScale = 0f) { model ->
        val initial = geometry()
        listOf(
            ThemeMode.LIGHT to AppColorScheme.MELROSE,
            ThemeMode.DARK to AppColorScheme.PINK_LACE,
            ThemeMode.SYSTEM to AppColorScheme.BLACKNWHITE,
        ).forEachIndexed { index, (mode, palette) ->
            val before = frame()
            onNode(hasText(mode.label)).performClick()
            onNode(paletteNamed(palette)).performClick()
            advance(32)
            val immediate = frame()
            assertGeometryEquals(initial, geometry(), "Reduced-motion selection $index")
            assertOneSelectedChoice(mode, palette)
            assertEquals(index + 1, model.themeSelections.size)
            assertEquals(index + 1, model.paletteSelections.size)
            assertTrue(pixelDifference(before, immediate, themeGroupBounds(initial)) > 1f, "Reduced motion must display the new theme without waiting for a transition")
            val name = palettes.first { it.first == palette }.second
            assertTrue(pixelDifference(before, immediate, initial.getValue("palette.$name")) > 1f, "Reduced motion must display the new palette without waiting for a transition")
            settle()
            assertUnchangedPixels(immediate, frame(), initial.getValue("panel"))
            assertGeometryEquals(initial, geometry(), "Settled reduced-motion selection $index")
            assertOneSelectedChoice(mode, palette)
        }
    }

    @Test
    fun keyboardFocusAndSpaceSelectEachThemeOnceWithoutMovingControls(): TestResult = renderingTest { model ->
        val initial = geometry()
        val original = frame()
        val system = onNode(hasText("System"))
        system.requestFocus()
        settle()
        system.assertIsFocused()
        assertTrue(pixelDifference(original, frame(), initial.getValue("theme.System")) > 1f, "Keyboard focus must have a visible state on the focused theme")
        system.performKeyInput { pressKey(Key.Tab) }
        advance(32)
        val light = onNode(hasText("Light"))
        light.assertIsFocused().performKeyInput { pressKey(Key.Spacebar) }
        advance(32)
        assertOneSelectedChoice(ThemeMode.LIGHT, AppColorScheme.BLACKNWHITE)
        assertEquals(listOf(ThemeMode.LIGHT), model.themeSelections)
        light.performKeyInput { pressKey(Key.Tab) }
        advance(32)
        val dark = onNode(hasText("Dark"))
        dark.assertIsFocused().performKeyInput { pressKey(Key.Spacebar) }
        advance(32)
        assertOneSelectedChoice(ThemeMode.DARK, AppColorScheme.BLACKNWHITE)
        assertEquals(listOf(ThemeMode.LIGHT, ThemeMode.DARK), model.themeSelections)
        settle()
        dark.assertIsFocused()
        assertGeometryEquals(initial, geometry(), "After keyboard selection")
        assertUnchangedPixels(original, frame(), initial.getValue("following"))
    }

    private fun ComposeUiTest.assertThemeSelectionGeometry(model: AppearanceFixtureModel) {
        val initial = geometry()
        assertContained(initial)
        listOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM, ThemeMode.DARK, ThemeMode.LIGHT, ThemeMode.SYSTEM).forEach { mode ->
            val before = frame()
            onNode(hasText(mode.label)).performClick()
            advance(64)
            val middle = frame()
            assertGeometryEquals(initial, geometry(), "During selection of ${mode.label}")
            assertEquals(mode, model.state.value.themeMode)
            settle()
            val after = frame()
            assertGeometryEquals(initial, geometry(), "After selection of ${mode.label}")
            val groupDifference = pixelDifference(before, middle, themeGroupBounds(initial))
            assertTrue(groupDifference > 1f, "The theme indicator must visibly move during selection of ${mode.label}")
            assertTrue(pixelDifference(middle, after, themeGroupBounds(initial)) > 1f, "The ${mode.label} transition must have a real intermediate frame before settling")
            assertTrue(pixelDifference(before, after, initial.getValue("theme.${mode.label}")) > 1f, "The selected theme must visibly change")
            assertUnchangedPixels(before, middle, initial.getValue("following"))
            assertUnchangedPixels(before, after, initial.getValue("following"))
        }
        val themeBounds = ThemeMode.entries.map { initial.getValue("theme.${it.label}") }
        themeBounds.drop(1).forEach {
            assertEquals(themeBounds.first().width, it.width, 1f, "Theme choices must have equal widths")
            assertEquals(themeBounds.first().height, it.height, 1f, "Theme choices must have equal heights")
        }
    }

    private fun ComposeUiTest.assertPaletteSelectionGeometry(model: AppearanceFixtureModel) {
        val initial = geometry()
        assertContained(initial)
        (palettes.drop(1) + palettes.take(1)).forEach { (scheme, name) ->
            val before = frame()
            onNode(paletteNamed(scheme)).performClick()
            advance(64)
            val middle = frame()
            assertGeometryEquals(initial, geometry(), "During selection of $name")
            assertEquals(scheme, model.state.value.colorScheme)
            settle()
            val after = frame()
            assertGeometryEquals(initial, geometry(), "After selection of $name")
            assertTrue(pixelDifference(before, middle, initial.getValue("palette.$name")) > 1f, "The $name palette must have visible intermediate selection pixels")
            assertTrue(pixelDifference(before, after, initial.getValue("palette.$name")) > 1f, "The $name palette must visibly show its selection")
            assertUnchangedPixels(before, middle, initial.getValue("following"))
            assertUnchangedPixels(before, after, initial.getValue("following"))
        }
    }

    private fun renderingTest(
        dark: Boolean = false,
        direction: LayoutDirection = LayoutDirection.Ltr,
        fontScale: Float = 1f,
        motionScale: Float = 1f,
        block: suspend ComposeUiTest.(AppearanceFixtureModel) -> Unit,
    ): TestResult = runComposeUiTest(
        effectContext = object : MotionDurationScale {
            override val scaleFactor = motionScale
        },
        testTimeout = 30.seconds,
    ) {
        mainClock.autoAdvance = false
        val model = AppearanceFixtureModel()
        setContent { AppearanceFixture(model, dark, direction, fontScale) }
        settle()
        block(model)
    }

    private fun ComposeUiTest.geometry(): Map<String, Rect> = buildMap {
        val panel = onNodeWithTag("appearance").fetchSemanticsNode().boundsInRoot
        put("panel", panel)
        put("following", onNodeWithTag("following").fetchSemanticsNode().boundsInRoot)
        val heading = onNode(hasText("Colour scheme"), useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        put("colourHeading", heading)
        val nameMatcher = palettes.fold(hasText("System")) { matcher, (_, name) -> matcher or hasText(name) }
        val names = onAllNodes(nameMatcher, useUnmergedTree = true).fetchSemanticsNodes()
            .filter { it.boundsInRoot.center.y >= heading.top && it.boundsInRoot.center.y <= heading.bottom }
        assertTrue(names.isNotEmpty(), "The selected palette name must remain present in its reserved header area")
        val rowTop = minOf(heading.top, names.minOf { it.boundsInRoot.top })
        val rowBottom = maxOf(heading.bottom, names.maxOf { it.boundsInRoot.bottom })
        val nameSlot = if (heading.left - panel.left > panel.right - heading.right) {
            Rect(panel.left, rowTop, heading.left, rowBottom)
        } else {
            Rect(heading.right, rowTop, panel.right, rowBottom)
        }
        put("paletteNameSlot", nameSlot)
        names.forEach {
            val label = it.boundsInRoot
            assertTrue(label.left >= nameSlot.left - 0.5f && label.right <= nameSlot.right + 0.5f, "The palette name must remain inside its reserved width. heading=$heading slot=$nameSlot label=$label")
        }
        ThemeMode.entries.forEach { mode ->
            put("theme.${mode.label}", onNode(hasText(mode.label)).fetchSemanticsNode().boundsInRoot)
            put("themeLabel.${mode.label}", onNode(hasText(mode.label), useUnmergedTree = true).fetchSemanticsNode().boundsInRoot)
        }
        palettes.forEach { (scheme, name) ->
            val control = onNode(paletteNamed(scheme)).fetchSemanticsNode().boundsInRoot
            put("palette.$name", control)
            val labels = onAllNodes(hasText(name), useUnmergedTree = true).fetchSemanticsNodes()
                .filter { control.contains(it.boundsInRoot.center) }
            assertEquals(1, labels.size, "The $name palette must have one label inside its control")
            put("paletteLabel.$name", labels.single().boundsInRoot)
        }
    }

    private fun assertGeometryEquals(expected: Map<String, Rect>, actual: Map<String, Rect>, stage: String) {
        expected.forEach { (name, bounds) ->
            val current = actual.getValue(name)
            assertEquals(bounds.left, current.left, 0.5f, "$stage moved $name left")
            assertEquals(bounds.top, current.top, 0.5f, "$stage moved $name top")
            assertEquals(bounds.right, current.right, 0.5f, "$stage moved $name right")
            assertEquals(bounds.bottom, current.bottom, 0.5f, "$stage moved $name bottom")
        }
    }

    private fun assertContained(geometry: Map<String, Rect>) {
        val panel = geometry.getValue("panel")
        geometry.filterKeys { it.startsWith("theme.") || it.startsWith("palette.") }.forEach { (name, bounds) ->
            assertTrue(bounds.width >= 47f && bounds.height >= 47f, "$name must keep a usable touch target")
            assertTrue(bounds.left >= panel.left - 0.5f && bounds.right <= panel.right + 0.5f, "$name must fit the narrow panel")
            val label = geometry.getValue(name.replace(".", "Label."))
            assertTrue(label.left >= bounds.left - 0.5f && label.right <= bounds.right + 0.5f, "$name label must fit its control")
            assertTrue(label.top >= bounds.top - 0.5f && label.bottom <= bounds.bottom + 0.5f, "$name label must remain visible inside its control")
        }
    }

    private fun ComposeUiTest.advance(milliseconds: Long) {
        mainClock.advanceTimeBy(milliseconds)
        waitForIdle()
    }

    private fun ComposeUiTest.settle() = advance(2_000)

    private fun ComposeUiTest.frame(): ImageBitmap = onNodeWithTag("frame").captureToImage()

    private fun ComposeUiTest.assertOneSelectedChoice(mode: ThemeMode, palette: AppColorScheme) {
        onAllNodes(radioRole).assertCountEquals(8)
        onAllNodes(radioRole and SemanticsMatcher.expectValue(SemanticsProperties.Selected, true)).assertCountEquals(2)
        ThemeMode.entries.forEach { choice ->
            onNode(hasText(choice.label)).assert(radioRole)
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, choice == mode))
        }
        palettes.forEach { (choice, _) ->
            onNode(paletteNamed(choice)).assert(radioRole)
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, choice == palette))
        }
    }

    private fun themeGroupBounds(geometry: Map<String, Rect>): Rect {
        val bounds = ThemeMode.entries.map { geometry.getValue("theme.${it.label}") }
        return Rect(bounds.minOf { it.left }, bounds.minOf { it.top }, bounds.maxOf { it.right }, bounds.maxOf { it.bottom })
    }

    private fun assertUnchangedPixels(before: ImageBitmap, after: ImageBitmap, bounds: Rect) {
        assertTrue(pixelDifference(before, after, bounds) < 0.05f, "Selection must leave following content pixels in place")
    }

    private fun pixelDifference(before: ImageBitmap, after: ImageBitmap, bounds: Rect): Float {
        val first = before.toPixelMap()
        val second = after.toPixelMap()
        var difference = 0f
        for (y in ceil(bounds.top).toInt().coerceAtLeast(0) until floor(bounds.bottom).toInt().coerceAtMost(before.height)) {
            for (x in ceil(bounds.left).toInt().coerceAtLeast(0) until floor(bounds.right).toInt().coerceAtMost(before.width)) {
                val left = first[x, y]
                val right = second[x, y]
                difference += maxOf(abs(left.red - right.red), abs(left.green - right.green), abs(left.blue - right.blue))
            }
        }
        return difference
    }

    private fun paletteNamed(scheme: AppColorScheme): SemanticsMatcher = hasContentDescription("${palettes.first { it.first == scheme }.second} colour scheme")

    private val radioRole = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)

    private val palettes = listOf(
        AppColorScheme.BLACKNWHITE to "Mono",
        AppColorScheme.MELROSE to "Violet",
        AppColorScheme.PERIWINKLE to "Blue",
        AppColorScheme.MINT_GREEN to "Mint",
        AppColorScheme.PINK_LACE to "Pink",
    )
}

private class AppearanceFixtureModel {
    val state = mutableStateOf(SettingsUiState())
    val themeSelections = mutableListOf<ThemeMode>()
    val paletteSelections = mutableListOf<AppColorScheme>()
    val actions = SettingsActions(
        onTheme = { mode -> themeSelections += mode; state.value = state.value.copy(themeMode = mode) },
        onColorScheme = { scheme -> paletteSelections += scheme; state.value = state.value.copy(colorScheme = scheme) },
    )
}

@Composable
private fun AppearanceFixture(
    model: AppearanceFixtureModel,
    dark: Boolean,
    direction: LayoutDirection,
    fontScale: Float,
) {
    val colors = if (dark) AppColorScheme.MELROSE.darkColor else AppColorScheme.MELROSE.lightColor
    CompositionLocalProvider(
        LocalLayoutDirection provides direction,
        LocalDensity provides Density(1f, fontScale),
        LocalContentColor provides colors.onSurface,
    ) {
        MaterialTheme(colorScheme = colors, typography = Typography) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface).testTag("frame")) {
                Column(Modifier.width(360.dp).padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(Modifier.fillMaxWidth().testTag("appearance")) {
                        SettingsAppearancePanel(model.state.value, model.actions)
                    }
                    Text("Keep your flow.", modifier = Modifier.fillMaxWidth().testTag("following"), style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}
