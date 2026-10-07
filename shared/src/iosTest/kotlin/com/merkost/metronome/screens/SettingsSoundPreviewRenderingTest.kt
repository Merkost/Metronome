package com.merkost.metronome.screens

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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
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
import com.merkost.metronome.model.ClickSound
import com.merkost.metronome.ui.theme.AppColorScheme
import com.merkost.metronome.ui.theme.Typography
import kotlinx.coroutines.test.TestResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalTestApi::class)
class SettingsSoundPreviewRenderingTest {
    @Test
    fun previewStartStopAndAutomaticFinishKeepTheSoundPanelInPlace(): TestResult = renderingTest { model ->
        assertPreviewGeometryAcrossSounds(model)
    }

    @Test
    fun previewGeometryStaysStableInNarrowDarkRtlWithDoubleText(): TestResult = renderingTest(
        dark = true,
        direction = LayoutDirection.Rtl,
        fontScale = 2f,
    ) { model ->
        assertPreviewGeometryAcrossSounds(model, verifyWordFit = true)
    }

    @Test
    fun rapidPreviewReversalsFinishAtIdleAndPlaybackDisablesTheAction(): TestResult = renderingTest { model ->
        selectSound(model, ClickSound.STUDIO)
        val initial = geometry(model.state.value.selectedSound)
        repeat(6) { index ->
            onNode(previewButton).assertHasClickAction().performClick()
            advance(48)
            assertEquals(index + 1, model.previewCallbacks, "Each preview or stop tap must emit one callback")
            assertEquals(if (index % 2 == 0) ClickSound.STUDIO else null, model.state.value.previewSound)
            assertGeometryEquals(initial, geometry(ClickSound.STUDIO), "During rapid preview reversal $index")
            assertPreviewDescription(previewing = index % 2 == 0)
        }
        onNode(previewButton).performClick()
        advance(64)
        assertEquals(ClickSound.STUDIO, model.state.value.previewSound)
        assertGeometryEquals(initial, geometry(ClickSound.STUDIO), "Before automatic preview finish")
        assertPreviewDescription(previewing = true)
        val callbacksBeforeFinish = model.previewCallbacks
        runOnUiThread { model.state.value = model.state.value.copy(previewSound = null) }
        advance(64)
        assertGeometryEquals(initial, geometry(ClickSound.STUDIO), "During automatic preview finish")
        assertPreviewDescription(previewing = false)
        settle()
        onNode(previewButton).assertIsEnabled()
        assertEquals(null, model.state.value.previewSound)
        assertEquals(callbacksBeforeFinish, model.previewCallbacks, "Automatic completion must not emit a new preview callback")
        assertGeometryEquals(initial, geometry(ClickSound.STUDIO), "After rapid reversals finish")
        assertPreviewDescription(previewing = false)
        runOnUiThread { model.state.value = model.state.value.copy(playing = true) }
        settle()
        onNode(hasText("Pause playback to preview a click.")).assertExists()
        val disabledGeometry = geometry(ClickSound.STUDIO)
        onNode(previewButton).assertIsNotEnabled().performTouchInput { click() }
        advance(64)
        assertEquals(callbacksBeforeFinish, model.previewCallbacks, "Playback must block the preview callback")
        assertEquals(null, model.state.value.previewSound)
        assertGeometryEquals(disabledGeometry, geometry(ClickSound.STUDIO), "After tapping disabled preview")
        assertPreviewDescription(previewing = false)
    }

    private fun ComposeUiTest.assertPreviewGeometryAcrossSounds(model: SoundPreviewFixtureModel, verifyWordFit: Boolean = false) {
        listOf(ClickSound.WOOD, ClickSound.CLASSIC, ClickSound.STUDIO).forEach { sound ->
            selectSound(model, sound)
            val initial = geometry(sound)
            if (verifyWordFit) assertSoundWordsFit(sound)
            val beforeCallbacks = model.previewCallbacks
            val preview = onNode(previewButton)
            preview.assertIsEnabled().assertHasClickAction()
            assertPreviewDescription(previewing = false)
            val previewBounds = initial.getValue("preview")
            assertTrue(previewBounds.width >= 47f && previewBounds.height >= 47f, "Preview must keep a usable touch target")
            if (sound == ClickSound.STUDIO) assertDescriptionWraps(sound)
            preview.performClick()
            advance(64)
            assertEquals(sound, model.state.value.previewSound)
            assertEquals(beforeCallbacks + 1, model.previewCallbacks)
            assertGeometryEquals(initial, geometry(sound), "During preview start for ${sound.displayName}")
            assertPreviewDescription(previewing = true)
            settle()
            assertGeometryEquals(initial, geometry(sound), "After preview start for ${sound.displayName}")
            assertPreviewDescription(previewing = true)
            onNode(previewButton).performClick()
            advance(64)
            assertEquals(null, model.state.value.previewSound)
            assertEquals(beforeCallbacks + 2, model.previewCallbacks)
            assertGeometryEquals(initial, geometry(sound), "During preview stop for ${sound.displayName}")
            assertPreviewDescription(previewing = false)
            settle()
            assertGeometryEquals(initial, geometry(sound), "After preview stop for ${sound.displayName}")
            onNode(previewButton).performClick()
            settle()
            val callbacksBeforeFinish = model.previewCallbacks
            runOnUiThread { model.state.value = model.state.value.copy(previewSound = null) }
            advance(64)
            assertGeometryEquals(initial, geometry(sound), "During automatic finish for ${sound.displayName}")
            assertPreviewDescription(previewing = false)
            settle()
            assertEquals(callbacksBeforeFinish, model.previewCallbacks)
            assertGeometryEquals(initial, geometry(sound), "After automatic finish for ${sound.displayName}")
            assertPreviewDescription(previewing = false)
        }
    }

    private fun ComposeUiTest.selectSound(model: SoundPreviewFixtureModel, sound: ClickSound) {
        runOnUiThread { model.state.value = model.state.value.copy(selectedSound = sound, previewSound = null) }
        settle()
    }

    private fun ComposeUiTest.assertDescriptionWraps(sound: ClickSound) {
        val layouts = mutableListOf<TextLayoutResult>()
        onNode(hasText(sound.description), useUnmergedTree = true).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
            assertTrue(action(layouts))
        }
        assertTrue(layouts.single().lineCount > 1, "The regression must exercise a wrapped sound description")
    }

    private fun ComposeUiTest.assertPreviewDescription(previewing: Boolean) {
        val current = if (previewing) "Stop sound preview" else "Preview click sound"
        val inactive = if (previewing) "Preview click sound" else "Stop sound preview"
        onAllNodes(buttonRole and hasContentDescription(current)).assertCountEquals(1)
        onAllNodes(hasContentDescription(inactive)).assertCountEquals(0)
        onAllNodes(hasText("Preview") or hasText("Stop")).assertCountEquals(0)
    }

    private fun ComposeUiTest.assertSoundWordsFit(sound: ClickSound) {
        val titleLayouts = mutableListOf<TextLayoutResult>()
        val title = onNode(hasText(sound.displayName), useUnmergedTree = true)
        title.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action -> assertTrue(action(titleLayouts)) }
        val titleLayout = titleLayouts.single()
        val titleBounds = title.fetchSemanticsNode().boundsInRoot
        assertEquals(1, titleLayout.lineCount, "The ${sound.displayName} sound name must remain on one line")
        assertTrue(!titleLayout.isLineEllipsized(0), "The ${sound.displayName} sound name must not use ellipsis")
        assertTrue(titleLayout.multiParagraph.intrinsics.maxIntrinsicWidth <= titleBounds.width + 0.5f, "The ${sound.displayName} sound name must fit its rendered width. intrinsicWidth=${titleLayout.multiParagraph.intrinsics.maxIntrinsicWidth} bounds=$titleBounds")
        val descriptionLayouts = mutableListOf<TextLayoutResult>()
        val description = onNode(hasText(sound.description), useUnmergedTree = true)
        description.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action -> assertTrue(action(descriptionLayouts)) }
        val descriptionLayout = descriptionLayouts.single()
        val descriptionBounds = description.fetchSemanticsNode().boundsInRoot
        val wordWidth = descriptionLayout.multiParagraph.intrinsics.minIntrinsicWidth
        assertTrue(wordWidth <= descriptionBounds.width + 0.5f, "The ${sound.displayName} description must fit whole words. wordWidth=$wordWidth bounds=$descriptionBounds")
        assertTrue(!descriptionLayout.multiParagraph.didExceedMaxLines, "The ${sound.displayName} description must not be truncated")
        repeat(descriptionLayout.lineCount) { line ->
            assertTrue(!descriptionLayout.isLineEllipsized(line), "The ${sound.displayName} description must not use ellipsis")
        }
        assertEquals(sound.description.length, descriptionLayout.getLineEnd(descriptionLayout.lineCount - 1, visibleEnd = true), "The ${sound.displayName} description must display every character")
    }

    private fun renderingTest(
        dark: Boolean = false,
        direction: LayoutDirection = LayoutDirection.Ltr,
        fontScale: Float = 1f,
        block: suspend ComposeUiTest.(SoundPreviewFixtureModel) -> Unit,
    ): TestResult = runComposeUiTest(
        effectContext = object : MotionDurationScale {
            override val scaleFactor = 1f
        },
        testTimeout = 30.seconds,
    ) {
        mainClock.autoAdvance = false
        val model = SoundPreviewFixtureModel()
        setContent { SoundPreviewFixture(model, dark, direction, fontScale) }
        settle()
        block(model)
    }

    private fun ComposeUiTest.geometry(sound: ClickSound): Map<String, Rect> = mapOf(
        "preview" to onNode(previewButton).fetchSemanticsNode().boundsInRoot,
        "soundSelector" to onNode(buttonRole and hasText(sound.displayName)).fetchSemanticsNode().boundsInRoot,
        "soundTitle" to onNode(hasText(sound.displayName), useUnmergedTree = true).fetchSemanticsNode().boundsInRoot,
        "soundDescription" to onNode(hasText(sound.description), useUnmergedTree = true).fetchSemanticsNode().boundsInRoot,
        "panel" to onNodeWithTag("soundPanel").fetchSemanticsNode().boundsInRoot,
        "following" to onNodeWithTag("following").fetchSemanticsNode().boundsInRoot,
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

    private fun ComposeUiTest.advance(milliseconds: Long) {
        mainClock.advanceTimeBy(milliseconds)
        waitForIdle()
    }

    private fun ComposeUiTest.settle() = advance(2_000)

    private val buttonRole = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)

    private val previewButton = buttonRole and (
        hasText("Preview") or hasText("Stop") or
            hasContentDescription("Preview click sound") or hasContentDescription("Stop sound preview")
    )
}

private class SoundPreviewFixtureModel {
    val state = mutableStateOf(SettingsUiState())
    var previewCallbacks = 0
    val actions = SettingsActions(
        onPreview = {
            previewCallbacks += 1
            val current = state.value
            state.value = current.copy(previewSound = if (current.previewSound == current.selectedSound) null else current.selectedSound)
        },
    )
}

@Composable
private fun SoundPreviewFixture(
    model: SoundPreviewFixtureModel,
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
                    Box(Modifier.fillMaxWidth().testTag("soundPanel")) {
                        SettingsSoundPanel(model.state.value, model.actions)
                    }
                    Text("Following content", modifier = Modifier.fillMaxWidth().testTag("following"), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
