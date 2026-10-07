package com.merkost.metronome.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
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
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasContentDescription
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
import androidx.compose.ui.unit.toSize
import com.merkost.metronome.components.SoundToneHistogram
import com.merkost.metronome.model.ClickSound
import com.merkost.metronome.ui.theme.AppColorScheme
import com.merkost.metronome.ui.theme.Typography
import kotlinx.coroutines.test.TestResult
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalTestApi::class)
class SoundPickerRenderingTest {
    @Test
    fun everySoundPaintsADistinctPassiveToneProfileWithStableSelectionGeometry(): TestResult = renderingTest(profilesOnly = true) { model ->
        val initialBounds = ClickSound.entries.associateWith { profileBounds(it) }
        val initialPaint = ClickSound.entries.associateWith { paintedColumnHeights(onNodeWithTag(profileTag(it)).captureToImage()) }
        initialBounds.forEach { (sound, bounds) ->
            assertEquals(24f, bounds.height, 0.5f, "The ${sound.displayName} tone profile must reserve a fixed height")
            assertTrue(initialPaint.getValue(sound).sum() > 0, "The ${sound.displayName} profile must paint visible ink")
            val canvas = profileNode(bounds)
            assertTrue(canvas.config.isClearingSemantics, "The decorative tone profile must clear accessibility semantics")
            assertTrue(canvas.config.none(), "The decorative tone profile must not announce an independent action or label")
        }
        ClickSound.entries.forEachIndexed { index, sound ->
            ClickSound.entries.drop(index + 1).forEach { other ->
                val difference = initialPaint.getValue(sound).zip(initialPaint.getValue(other)).sumOf { (first, second) -> abs(first - second) }
                assertTrue(difference > 20, "${sound.displayName} and ${other.displayName} must have distinguishable painted profiles. difference=$difference")
            }
        }
        runOnUiThread { model.profilesSelected.value = true }
        advance(64)
        ClickSound.entries.forEach { sound -> assertRectEquals(initialBounds.getValue(sound), profileBounds(sound), "Selecting ${sound.displayName}") }
        settle()
        ClickSound.entries.forEach { sound ->
            assertRectEquals(initialBounds.getValue(sound), profileBounds(sound), "Selected ${sound.displayName}")
            val selectedPaint = paintedColumnHeights(onNodeWithTag(profileTag(sound)).captureToImage())
            val largestChange = initialPaint.getValue(sound).zip(selectedPaint).maxOf { (first, second) -> abs(first - second) }
            assertTrue(largestChange <= 1, "Selection must preserve the ${sound.displayName} frequency profile. largestColumnChange=$largestChange")
        }
    }

    @Test
    fun selectingTheToneProfileAndPreviewingKeepSeparateCallbacksAndStableRows(): TestResult = renderingTest { model ->
        ClickSound.entries.forEach { sound ->
            reveal(sound)
            val initial = geometry()
            val selections = model.selectionCallbacks
            row(sound).assertHasClickAction().performClick()
            advance(64)
            assertEquals(selections + 1, model.selectionCallbacks)
            assertEquals(sound, model.selectedSound.value)
            assertEquals(null, model.previewSound.value)
            assertSelectedSound(sound)
            assertGeometryEquals(initial, geometry(), "During selection of ${sound.displayName}")
            settle()
            assertGeometryEquals(initial, geometry(), "After selection of ${sound.displayName}")
            assertSeparatePreviewTarget(sound)
            val chart = chartBounds(sound)
            val viewport = onNodeWithTag("viewport").fetchSemanticsNode().boundsInRoot
            onNodeWithTag("viewport").performTouchInput { click(chart.center - viewport.topLeft) }
            settle()
            assertEquals(selections + 2, model.selectionCallbacks, "A tone-profile tap must select exactly once")
            assertEquals(0, model.previewCallbacks, "A tone-profile tap must not start audio preview")
            assertGeometryEquals(initial, geometry(), "After tapping the ${sound.displayName} tone profile")
        }
        reveal(ClickSound.WOOD)
        val initial = geometry()
        val selections = model.selectionCallbacks
        preview(ClickSound.WOOD, previewing = false).assertHasClickAction().performClick()
        advance(64)
        assertEquals(ClickSound.WOOD, model.previewSound.value)
        assertEquals(1, model.previewCallbacks)
        assertEquals(selections, model.selectionCallbacks, "The preview target must not select a sound")
        assertCurrentPreviewDescriptions(ClickSound.WOOD)
        assertGeometryEquals(initial, geometry(), "During Wood preview start")
        settle()
        assertGeometryEquals(initial, geometry(), "After Wood preview start")
        preview(ClickSound.WOOD, previewing = true).performClick()
        advance(64)
        assertEquals(null, model.previewSound.value)
        assertEquals(2, model.previewCallbacks)
        assertEquals(selections, model.selectionCallbacks, "The stop target must not select a sound")
        assertCurrentPreviewDescriptions(null)
        assertGeometryEquals(initial, geometry(), "During Wood preview stop")
        settle()
        assertGeometryEquals(initial, geometry(), "After Wood preview stop")
    }

    @Test
    fun rapidSelectionAndPreviewReversalsKeepOneSelectedSoundAndCurrentActions(): TestResult = renderingTest { model ->
        reveal(ClickSound.CLICK)
        val initial = geometry()
        listOf(ClickSound.CLICK, ClickSound.WOOD, ClickSound.CLICK, ClickSound.WOOD).forEachIndexed { index, sound ->
            row(sound).performClick()
            advance(48)
            assertEquals(index + 1, model.selectionCallbacks)
            assertSelectedSound(sound)
            assertGeometryEquals(initial, geometry(), "During rapid selection $index")
        }
        val selections = model.selectionCallbacks
        repeat(6) { index ->
            preview(ClickSound.WOOD, previewing = index % 2 == 1).performClick()
            advance(48)
            val expectedPreview = if (index % 2 == 0) ClickSound.WOOD else null
            assertEquals(expectedPreview, model.previewSound.value)
            assertEquals(index + 1, model.previewCallbacks, "Each rapid preview or stop action must invoke one callback")
            assertEquals(selections, model.selectionCallbacks)
            assertCurrentPreviewDescriptions(expectedPreview)
            assertSelectedSound(ClickSound.WOOD)
            assertGeometryEquals(initial, geometry(), "During rapid preview reversal $index")
        }
        preview(ClickSound.WOOD, previewing = false).performClick()
        advance(64)
        val callbacks = model.previewCallbacks
        runOnUiThread { model.previewSound.value = null }
        advance(64)
        assertCurrentPreviewDescriptions(null)
        assertGeometryEquals(initial, geometry(), "During automatic preview completion")
        settle()
        assertEquals(callbacks, model.previewCallbacks, "Automatic completion must not invoke another preview action")
        assertSelectedSound(ClickSound.WOOD)
        assertGeometryEquals(initial, geometry(), "After rapid reversals and automatic completion")
    }

    @Test
    fun narrowDarkRtlWithDoubleTextKeepsWordsReadableAndStudioReachable(): TestResult = renderingTest(
        dark = true,
        direction = LayoutDirection.Rtl,
        fontScale = 2f,
        width = 320,
    ) { model ->
        ClickSound.entries.forEach { sound ->
            reveal(sound)
            assertSoundTextFits(sound)
            assertSeparatePreviewTarget(sound)
            assertEquals(24f, chartBounds(sound).height, 0.5f, "Large text must not stretch the decorative profile")
        }
        val studio = ClickSound.STUDIO
        row(studio).assertIsDisplayed()
        preview(studio, previewing = false).assertIsDisplayed()
        val viewport = onNodeWithTag("viewport").fetchSemanticsNode().boundsInRoot
        val card = cardBounds(studio)
        assertTrue(card.top >= viewport.top - 0.5f && card.bottom <= viewport.bottom + 0.5f, "The final Studio row must be fully reachable. row=$card viewport=$viewport")
        val initial = geometry()
        row(studio).performClick()
        advance(64)
        assertSelectedSound(studio)
        assertGeometryEquals(initial, geometry(), "During large-text Studio selection")
        settle()
        val target = preview(studio, previewing = false)
        target.performTouchInput { click() }
        settle()
        assertEquals(1, model.previewCallbacks)
        assertEquals(1, model.selectionCallbacks, "The separate preview button must preserve the selection callback count")
        assertEquals(studio, model.previewSound.value)
        assertCurrentPreviewDescriptions(studio)
        assertGeometryEquals(initial, geometry(), "After large-text Studio preview starts")
        preview(studio, previewing = true).performClick()
        advance(64)
        assertGeometryEquals(initial, geometry(), "During large-text Studio preview stop")
        assertCurrentPreviewDescriptions(null)
        settle()
        assertEquals(2, model.previewCallbacks)
        assertGeometryEquals(initial, geometry(), "After large-text Studio preview stops")
    }

    private fun renderingTest(
        profilesOnly: Boolean = false,
        dark: Boolean = false,
        direction: LayoutDirection = LayoutDirection.Ltr,
        fontScale: Float = 1f,
        width: Int = 360,
        block: suspend ComposeUiTest.(SoundPickerFixtureModel) -> Unit,
    ): TestResult = runComposeUiTest(
        effectContext = object : MotionDurationScale {
            override val scaleFactor = 1f
        },
        testTimeout = 30.seconds,
    ) {
        mainClock.autoAdvance = false
        val model = SoundPickerFixtureModel()
        setContent { SoundPickerFixture(model, profilesOnly, dark, direction, fontScale, width) }
        settle()
        block(model)
    }

    private fun ComposeUiTest.row(sound: ClickSound): SemanticsNodeInteraction = onNode(radioRole and hasText(sound.displayName))

    private fun ComposeUiTest.preview(sound: ClickSound, previewing: Boolean): SemanticsNodeInteraction = onNode(buttonRole and hasContentDescription(previewDescription(sound, previewing)))

    private fun ComposeUiTest.assertSelectedSound(sound: ClickSound) {
        onAllNodes(radioRole and SemanticsMatcher.expectValue(SemanticsProperties.Selected, true)).assertCountEquals(1)
        ClickSound.entries.forEach { current ->
            assertEquals(current == sound, row(current).fetchSemanticsNode().config[SemanticsProperties.Selected], "Only ${sound.displayName} must be selected")
        }
    }

    private fun ComposeUiTest.assertCurrentPreviewDescriptions(previewing: ClickSound?) {
        ClickSound.entries.forEach { sound ->
            val active = previewing == sound
            preview(sound, active).assertHasClickAction()
            onAllNodes(hasContentDescription(previewDescription(sound, active))).assertCountEquals(1)
            onAllNodes(hasContentDescription(previewDescription(sound, !active)), useUnmergedTree = true).assertCountEquals(0)
        }
    }

    private fun ComposeUiTest.assertSeparatePreviewTarget(sound: ClickSound) {
        val selection = row(sound).fetchSemanticsNode().unclippedBounds()
        val target = onNode(buttonRole and (hasContentDescription(previewDescription(sound, false)) or hasContentDescription(previewDescription(sound, true)))).fetchSemanticsNode().unclippedBounds()
        assertTrue(target.width >= 47.5f && target.height >= 47.5f, "The ${sound.displayName} preview must retain a 48dp touch target. bounds=$target")
        assertTrue(selection.right <= target.left + 0.5f || target.right <= selection.left + 0.5f, "Selection and preview must occupy separate targets. selection=$selection preview=$target")
    }

    private fun ComposeUiTest.reveal(sound: ClickSound) {
        val scrollContainer = onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.ScrollBy))
        repeat(6) {
            val viewport = onNodeWithTag("viewport").fetchSemanticsNode().boundsInRoot
            val card = cardBounds(sound)
            val amount = when {
                card.top < viewport.top -> card.top - viewport.top - 8f
                card.bottom > viewport.bottom -> card.bottom - viewport.bottom + 8f
                else -> 0f
            }
            if (amount == 0f) return
            scrollContainer.performSemanticsAction(SemanticsActions.ScrollBy) { action -> action(0f, amount) }
            advance(1_000)
        }
        val viewport = onNodeWithTag("viewport").fetchSemanticsNode().boundsInRoot
        val card = cardBounds(sound)
        assertTrue(card.top >= viewport.top - 0.5f && card.bottom <= viewport.bottom + 0.5f, "Bounded scrolling must reveal ${sound.displayName}. row=$card viewport=$viewport")
    }

    private fun ComposeUiTest.assertSoundTextFits(sound: ClickSound) {
        val title = onNode(hasText(sound.displayName), useUnmergedTree = true)
        val titleBounds = title.fetchSemanticsNode().unclippedBounds()
        val titleLayout = textLayout(title)
        assertEquals(1, titleLayout.lineCount, "The ${sound.displayName} sound name must stay on one line")
        assertTrue(titleLayout.multiParagraph.intrinsics.maxIntrinsicWidth <= titleBounds.width + 0.5f, "The ${sound.displayName} title must fit. intrinsicWidth=${titleLayout.multiParagraph.intrinsics.maxIntrinsicWidth} bounds=$titleBounds")
        val description = onNode(hasText(sound.description), useUnmergedTree = true)
        val descriptionBounds = description.fetchSemanticsNode().unclippedBounds()
        val descriptionLayout = textLayout(description)
        assertTrue(descriptionLayout.multiParagraph.intrinsics.minIntrinsicWidth <= descriptionBounds.width + 0.5f, "The ${sound.displayName} description must preserve whole words. wordWidth=${descriptionLayout.multiParagraph.intrinsics.minIntrinsicWidth} bounds=$descriptionBounds")
        listOf(titleLayout to titleBounds, descriptionLayout to descriptionBounds).forEach { (layout, bounds) ->
            assertTrue(!layout.multiParagraph.didExceedMaxLines, "The ${sound.displayName} text must not be truncated")
            assertTrue(layout.multiParagraph.height <= bounds.height + 0.5f, "The ${sound.displayName} text must fit vertically. height=${layout.multiParagraph.height} bounds=$bounds")
            repeat(layout.lineCount) { line -> assertTrue(!layout.isLineEllipsized(line), "The ${sound.displayName} text must not use ellipsis") }
            assertEquals(layout.layoutInput.text.length, layout.getLineEnd(layout.lineCount - 1, visibleEnd = true), "The ${sound.displayName} text must display every character")
        }
    }

    private fun textLayout(node: SemanticsNodeInteraction): TextLayoutResult {
        val layouts = mutableListOf<TextLayoutResult>()
        node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action -> assertTrue(action(layouts)) }
        return layouts.single()
    }

    private fun ComposeUiTest.geometry(): Map<String, Rect> = buildMap {
        put("viewport", onNodeWithTag("viewport").fetchSemanticsNode().unclippedBounds())
        put("content", onNodeWithTag("pickerContent").fetchSemanticsNode().unclippedBounds())
        put("following", onNodeWithTag("following").fetchSemanticsNode().unclippedBounds())
        ClickSound.entries.forEach { sound ->
            put("${sound.name}:card", cardBounds(sound))
            put("${sound.name}:profile", chartBounds(sound))
            put("${sound.name}:selection", row(sound).fetchSemanticsNode().unclippedBounds())
            put("${sound.name}:title", onNode(hasText(sound.displayName), useUnmergedTree = true).fetchSemanticsNode().unclippedBounds())
            put("${sound.name}:description", onNode(hasText(sound.description), useUnmergedTree = true).fetchSemanticsNode().unclippedBounds())
            put("${sound.name}:preview", onNode(buttonRole and (hasContentDescription(previewDescription(sound, false)) or hasContentDescription(previewDescription(sound, true)))).fetchSemanticsNode().unclippedBounds())
        }
    }

    private fun ComposeUiTest.cardBounds(sound: ClickSound): Rect {
        val selection = row(sound).fetchSemanticsNode().unclippedBounds()
        val target = onNode(buttonRole and (hasContentDescription(previewDescription(sound, false)) or hasContentDescription(previewDescription(sound, true)))).fetchSemanticsNode().unclippedBounds()
        return Rect(minOf(selection.left, target.left), minOf(selection.top, target.top), maxOf(selection.right, target.right), maxOf(selection.bottom, target.bottom))
    }

    private fun ComposeUiTest.chartBounds(sound: ClickSound): Rect = profileNode(row(sound).fetchSemanticsNode().unclippedBounds()).unclippedBounds()

    private fun ComposeUiTest.profileBounds(sound: ClickSound): Rect = onNodeWithTag(profileTag(sound)).fetchSemanticsNode().unclippedBounds()

    private fun ComposeUiTest.profileNode(container: Rect): SemanticsNode = onAllNodes(passiveProfile, useUnmergedTree = true).fetchSemanticsNodes().single { node ->
        val bounds = node.unclippedBounds()
        bounds.left >= container.left - 0.5f && bounds.right <= container.right + 0.5f && bounds.top >= container.top - 0.5f && bounds.bottom <= container.bottom + 0.5f
    }

    private fun paintedColumnHeights(image: ImageBitmap): List<Int> {
        val pixels = image.toPixelMap()
        val background = pixels[0, 0]
        return List(image.width) { x ->
            (0 until image.height).count { y ->
                val pixel = pixels[x, y]
                maxOf(abs(pixel.red - background.red), abs(pixel.green - background.green), abs(pixel.blue - background.blue), abs(pixel.alpha - background.alpha)) > 0.1f
            }
        }
    }

    private fun assertGeometryEquals(expected: Map<String, Rect>, actual: Map<String, Rect>, stage: String) {
        expected.forEach { (name, bounds) -> assertRectEquals(bounds, actual.getValue(name), "$stage changed $name") }
    }

    private fun assertRectEquals(expected: Rect, actual: Rect, stage: String) {
        assertEquals(expected.left, actual.left, 0.5f, "$stage left")
        assertEquals(expected.top, actual.top, 0.5f, "$stage top")
        assertEquals(expected.right, actual.right, 0.5f, "$stage right")
        assertEquals(expected.bottom, actual.bottom, 0.5f, "$stage bottom")
    }

    private fun ComposeUiTest.advance(milliseconds: Long) {
        mainClock.advanceTimeBy(milliseconds)
        waitForIdle()
    }

    private fun ComposeUiTest.settle() = advance(2_000)

    private val radioRole = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)
    private val buttonRole = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
    private val passiveProfile = SemanticsMatcher("passive tone profile") { node ->
        node.config.isClearingSemantics && node.config.none() && node.size.width > 48 && abs(node.size.height - 24) <= 1
    }
}

private class SoundPickerFixtureModel {
    val selectedSound = mutableStateOf(ClickSound.WOOD)
    val previewSound = mutableStateOf<ClickSound?>(null)
    val profilesSelected = mutableStateOf(false)
    var selectionCallbacks = 0
    var previewCallbacks = 0

    fun select(sound: ClickSound) {
        selectionCallbacks += 1
        selectedSound.value = sound
    }

    fun preview(sound: ClickSound) {
        previewCallbacks += 1
        previewSound.value = if (previewSound.value == sound) null else sound
    }
}

@Composable
private fun SoundPickerFixture(
    model: SoundPickerFixtureModel,
    profilesOnly: Boolean,
    dark: Boolean,
    direction: LayoutDirection,
    fontScale: Float,
    width: Int,
) {
    val colors = if (dark) AppColorScheme.MELROSE.darkColor else AppColorScheme.MELROSE.lightColor
    CompositionLocalProvider(
        LocalDensity provides Density(1f, fontScale),
        LocalLayoutDirection provides direction,
        LocalContentColor provides colors.onSurface,
    ) {
        MaterialTheme(colorScheme = colors, typography = Typography) {
            Box(Modifier.width(width.dp).height(640.dp).clipToBounds().background(colors.surface).testTag("viewport")) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (profilesOnly) {
                        ClickSound.entries.forEach { sound ->
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(sound.displayName, style = MaterialTheme.typography.titleMedium)
                                Box(Modifier.fillMaxWidth().testTag(profileTag(sound))) {
                                    SoundToneHistogram(sound, model.profilesSelected.value)
                                }
                            }
                        }
                    } else {
                        Box(Modifier.fillMaxWidth().testTag("pickerContent")) {
                            SoundPickerContent(model.selectedSound.value, model.previewSound.value, model::select, model::preview)
                        }
                        Text("Following content", modifier = Modifier.fillMaxWidth().testTag("following"), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

private fun SemanticsNode.unclippedBounds(): Rect = Rect(positionInRoot, size.toSize())

private fun previewDescription(sound: ClickSound, previewing: Boolean): String = if (previewing) "Stop ${sound.displayName} preview" else "Preview ${sound.displayName}"

private fun profileTag(sound: ClickSound): String = "profile:${sound.name}"
