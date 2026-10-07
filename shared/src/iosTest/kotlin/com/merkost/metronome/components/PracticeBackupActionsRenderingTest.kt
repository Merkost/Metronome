package com.merkost.metronome.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
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
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
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
class PracticeBackupActionsRenderingTest {
    @Test
    fun exportAndImportHaveLabelledButtonRolesAndEachTapEmitsOneAction(): TestResult = renderingTest { model ->
        onAllNodes(buttonRole).assertCountEquals(2)
        val export = onNode(buttonNamed("Export backup"))
        val importAction = onNode(buttonNamed("Import backup"))
        export.assertHasClickAction().assertIsEnabled().assert(hasText("Save your presets & practice sets"))
        importAction.assertHasClickAction().assertIsEnabled().assert(hasText("Add setups from a backup"))
        assertTrue(export.fetchSemanticsNode().boundsInRoot.bottom <= importAction.fetchSemanticsNode().boundsInRoot.top, "Export and import must be separate actions in reading order")
        export.performClick()
        advance(32)
        assertEquals(1, model.exports)
        assertEquals(0, model.imports)
        importAction.performTouchInput { click() }
        advance(32)
        assertEquals(1, model.exports)
        assertEquals(1, model.imports)
    }

    @Test
    fun disabledAndBusyStatesBlockBothActionsAndCanRecover(): TestResult = renderingTest { model ->
        val initial = geometry()
        val enabledPixels = frame()
        runOnUiThread { model.enabled.value = false }
        advance(64)
        assertDimmedLabels(enabledPixels, frame(), initial, settled = false)
        settle()
        assertDisabledActionsDoNothing(model)
        assertDimmedLabels(enabledPixels, frame(), initial, settled = true)
        assertGeometryEquals(initial, geometry(), "Disabled actions")
        runOnUiThread {
            model.enabled.value = true
            model.busy.value = true
        }
        settle()
        assertDisabledActionsDoNothing(model)
        assertDimmedLabels(enabledPixels, frame(), initial, settled = true)
        assertGeometryEquals(initial, geometry(), "Busy actions")
        runOnUiThread { model.busy.value = false }
        settle()
        listOf("Export backup", "Import backup").forEach { label ->
            assertTrue(pixelDifference(enabledPixels, frame(), initial.getValue("text.$label")) < 0.05f, "$label must recover its enabled text pixels")
        }
        onNode(buttonNamed("Export backup")).assertIsEnabled().performClick()
        onNode(buttonNamed("Import backup")).assertIsEnabled().performClick()
        advance(32)
        assertEquals(1, model.exports)
        assertEquals(1, model.imports)
        assertGeometryEquals(initial, geometry(), "Recovered actions")
    }

    @Test
    fun narrowActionsKeepEveryCharacterVisibleAtDoubleTextSize(): TestResult = renderingTest(fontScale = 2f) { model ->
        assertLargeTextFitsAndStaysStable(model)
    }

    @Test
    fun narrowDarkRtlActionsKeepEveryCharacterVisibleAtDoubleTextSize(): TestResult = renderingTest(
        dark = true,
        direction = LayoutDirection.Rtl,
        fontScale = 2f,
    ) { model ->
        assertLargeTextFitsAndStaysStable(model)
    }

    private fun ComposeUiTest.assertDisabledActionsDoNothing(model: BackupActionsFixtureModel) {
        listOf("Export backup", "Import backup").forEach { label ->
            onNode(buttonNamed(label)).assertIsNotEnabled().performTouchInput { click() }
        }
        advance(32)
        assertEquals(0, model.exports, "Disabled export must not run")
        assertEquals(0, model.imports, "Disabled import must not run")
    }

    private fun ComposeUiTest.assertLargeTextFitsAndStaysStable(model: BackupActionsFixtureModel) {
        val initial = geometry()
        assertContentFits(initial)
        runOnUiThread { model.busy.value = true }
        advance(64)
        assertGeometryEquals(initial, geometry(), "During busy transition")
        assertContentFits(geometry())
        settle()
        assertGeometryEquals(initial, geometry(), "After busy transition")
        runOnUiThread { model.busy.value = false }
        advance(64)
        assertGeometryEquals(initial, geometry(), "During recovery")
        settle()
        assertGeometryEquals(initial, geometry(), "After recovery")
        assertContentFits(geometry())
    }

    private fun ComposeUiTest.assertContentFits(geometry: Map<String, Rect>) {
        val panel = geometry.getValue("actions")
        listOf(
            "Export backup" to "Save your presets & practice sets",
            "Import backup" to "Add setups from a backup",
        ).forEach { (label, description) ->
            val button = geometry.getValue(label)
            assertTrue(button.height >= 47f, "$label must remain a usable touch target")
            assertTrue(button.left >= panel.left - 0.5f && button.right <= panel.right + 0.5f, "$label must fit the narrow panel")
            listOf(label, description).forEach { text ->
                val bounds = geometry.getValue("text.$text")
                assertTrue(bounds.left >= button.left - 0.5f && bounds.right <= button.right + 0.5f, "$text must stay horizontally inside its action")
                assertTrue(bounds.top >= button.top - 0.5f && bounds.bottom <= button.bottom + 0.5f, "$text must stay vertically inside its action")
                assertFullTextVisible(text, bounds)
            }
        }
    }

    private fun ComposeUiTest.assertFullTextVisible(text: String, bounds: Rect) {
        val layouts = mutableListOf<TextLayoutResult>()
        onNode(hasText(text), useUnmergedTree = true).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
            assertTrue(action(layouts), "$text must expose its real text layout")
        }
        val layout = layouts.single()
        assertTrue(!layout.multiParagraph.didExceedMaxLines, "$text must not be truncated by a line limit")
        assertTrue(layout.multiParagraph.height <= bounds.height + 0.5f, "$text must fit its rendered height. paragraphHeight=${layout.multiParagraph.height} bounds=$bounds")
        repeat(layout.lineCount) { line ->
            assertTrue(!layout.isLineEllipsized(line), "$text must not use ellipsis")
            val glyphWidth = layout.getLineRight(line) - layout.getLineLeft(line)
            assertTrue(glyphWidth <= bounds.width + 0.5f, "$text must fit its rendered line width. line=$line glyphWidth=$glyphWidth bounds=$bounds")
        }
        assertEquals(text.length, layout.getLineEnd(layout.lineCount - 1, visibleEnd = true), "$text must display every character")
    }

    private fun renderingTest(
        dark: Boolean = false,
        direction: LayoutDirection = LayoutDirection.Ltr,
        fontScale: Float = 1f,
        block: suspend ComposeUiTest.(BackupActionsFixtureModel) -> Unit,
    ): TestResult = runComposeUiTest(
        effectContext = object : MotionDurationScale {
            override val scaleFactor = 1f
        },
        testTimeout = 30.seconds,
    ) {
        mainClock.autoAdvance = false
        val model = BackupActionsFixtureModel()
        setContent { BackupActionsFixture(model, dark, direction, fontScale) }
        settle()
        block(model)
    }

    private fun ComposeUiTest.geometry(): Map<String, Rect> = buildMap {
        put("actions", onNodeWithTag("actions").fetchSemanticsNode().boundsInRoot)
        put("following", onNodeWithTag("following").fetchSemanticsNode().boundsInRoot)
        listOf("Export backup", "Import backup").forEach { label ->
            put(label, onNode(buttonNamed(label)).fetchSemanticsNode().boundsInRoot)
        }
        listOf("Export backup", "Save your presets & practice sets", "Import backup", "Add setups from a backup").forEach { text ->
            put("text.$text", onNode(hasText(text), useUnmergedTree = true).fetchSemanticsNode().boundsInRoot)
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

    private fun ComposeUiTest.advance(milliseconds: Long) {
        mainClock.advanceTimeBy(milliseconds)
        waitForIdle()
    }

    private fun ComposeUiTest.settle() = advance(2_000)

    private fun ComposeUiTest.frame(): ImageBitmap = onNodeWithTag("frame").captureToImage()

    private fun assertDimmedLabels(enabled: ImageBitmap, disabled: ImageBitmap, geometry: Map<String, Rect>, settled: Boolean) {
        listOf("Export backup", "Import backup").forEach { label ->
            val bounds = geometry.getValue("text.$label")
            val row = geometry.getValue(label)
            val enabledEnergy = textInkEnergy(enabled, bounds, row)
            val disabledEnergy = textInkEnergy(disabled, bounds, row)
            assertTrue(enabledEnergy > 1f, "$label must have visible enabled glyphs")
            val maximumRatio = if (settled) 0.75f else 0.98f
            assertTrue(disabledEnergy < enabledEnergy * maximumRatio, "$label must visibly dim when disabled. settled=$settled enabledEnergy=$enabledEnergy disabledEnergy=$disabledEnergy bounds=$bounds")
        }
    }

    private fun textInkEnergy(image: ImageBitmap, bounds: Rect, row: Rect): Float {
        val pixels = image.toPixelMap()
        val background = pixels[(row.right - 2f).toInt(), row.center.y.toInt()]
        var energy = 0f
        for (y in ceil(bounds.top).toInt().coerceAtLeast(0) until floor(bounds.bottom).toInt().coerceAtMost(image.height)) {
            for (x in ceil(bounds.left).toInt().coerceAtLeast(0) until floor(bounds.right).toInt().coerceAtMost(image.width)) {
                val pixel = pixels[x, y]
                energy += maxOf(abs(pixel.red - background.red), abs(pixel.green - background.green), abs(pixel.blue - background.blue))
            }
        }
        return energy
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

    private fun buttonNamed(label: String): SemanticsMatcher = buttonRole and hasText(label)

    private val buttonRole = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
}

private class BackupActionsFixtureModel {
    val enabled = mutableStateOf(true)
    val busy = mutableStateOf(false)
    var exports = 0
    var imports = 0
}

@Composable
private fun BackupActionsFixture(
    model: BackupActionsFixtureModel,
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
                Column(Modifier.width(320.dp).padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(Modifier.fillMaxWidth().testTag("actions")) {
                        PracticeBackupActions(
                            enabled = model.enabled.value && !model.busy.value,
                            onExport = { model.exports += 1 },
                            onImport = { model.imports += 1 },
                        )
                    }
                    Text("Following content", modifier = Modifier.fillMaxWidth().testTag("following"), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
