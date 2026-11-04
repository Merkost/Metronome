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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Timer
import com.merkost.metronome.components.PracticePresetRow
import com.merkost.metronome.components.PracticeSetRow
import com.merkost.metronome.model.GapTrainerConfig
import com.merkost.metronome.model.GradualTempoConfig
import com.merkost.metronome.model.Subdivision
import com.merkost.metronome.model.TimeSignature
import com.merkost.metronome.practiceSets.ActivePracticeSession
import com.merkost.metronome.practiceSets.PracticePlaybackIntent
import com.merkost.metronome.practiceSets.PracticeSet
import com.merkost.metronome.practiceSets.PracticeSetStep
import com.merkost.metronome.practiceSets.PracticeStepTarget
import com.merkost.metronome.practiceSets.ResolvedPracticeStep
import com.merkost.metronome.presets.PracticePreset
import com.merkost.metronome.ui.theme.AppColorScheme
import com.merkost.metronome.viewModels.PracticeSetsUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class PracticeCleanLayoutTest {
    @Test
    fun starterPreviewIsSecondaryAndOnlyAppearsInAnEmptyLibrary() = runComposeUiTest {
        mainClock.autoAdvance = false
        val state = mutableStateOf(PracticeSetsUiState())
        var creates = 0
        var previews = 0
        var starts = 0
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                MaterialTheme {
                    Box(Modifier.width(360.dp).height(760.dp)) {
                        PracticeSetsLibrary(
                            uiState = state.value,
                            persistenceWarning = false,
                            snackbarHostState = SnackbarHostState(),
                            onBack = {},
                            onCreate = { creates++ },
                            onPreviewStarter = { previews++ },
                            onStart = { starts++ },
                            onResume = {},
                            onRetryPersistence = {},
                            onEdit = {},
                            onDelete = {},
                            onMove = { _, _ -> },
                            onReorderingChanged = {},
                        )
                    }
                }
            }
        }
        settle()
        val create = onNode(hasText("Create set")).fetchSemanticsNode().boundsInRoot
        reveal(hasText("Preview & edit"))
        settle()
        onNode(hasText("Preview & edit")).performClick()
        assertEquals(1, previews)
        assertEquals(0, creates)
        assertEquals(0, starts)
        assertTrue(create.height >= 48f)
        reveal(hasText("Create set")).performClick()
        assertEquals(1, creates)
        runOnUiThread { state.value = PracticeSetsUiState(sets = listOf(set()), presets = listOf(preset())) }
        settle()
        onNode(hasText("Try a warm-up")).assertDoesNotExist()
        onNode(hasText("Preview & edit")).assertDoesNotExist()
        onNode(hasContentDescription("Start set")).performClick()
        assertEquals(1, starts)
    }

    @Test
    fun presetActionsStayIndependentOfApplyingAndReordering() = runComposeUiTest {
        mainClock.autoAdvance = false
        val reordering = mutableStateOf(false)
        var applies = 0
        var favourites = 0
        var renames = 0
        var duplicates = 0
        var moves = 0
        setContent {
            PracticeViewport {
                PracticePresetRow(
                    preset = preset(), isActive = false, isEdited = false,
                    isReordering = reordering.value, canMoveUp = false, canMoveDown = true,
                    onApply = { applies++ }, onToggleFavourite = { favourites++ },
                    onRename = { renames++ }, onDuplicate = { duplicates++ }, onDelete = {},
                    onMoveUp = {}, onMoveDown = { moves++ },
                )
            }
        }
        settle()
        onNode(hasText("Warm-up")).performClick()
        onNode(hasContentDescription("Favourite Warm-up")).performClick()
        assertEquals(1, applies)
        assertEquals(1, favourites)
        onNode(hasContentDescription("Actions for Warm-up")).performClick()
        settle()
        onNode(hasText("Rename")).performClick()
        settle()
        onNode(hasContentDescription("Actions for Warm-up")).performClick()
        settle()
        onNode(hasText("Duplicate")).performClick()
        assertEquals(1, renames)
        assertEquals(1, duplicates)
        assertEquals(1, applies)
        runOnUiThread { reordering.value = true }
        settle()
        onNode(hasContentDescription("Move Warm-up down")).performClick()
        assertEquals(1, moves)
        assertEquals(1, applies)
    }

    @Test
    fun setPlaybackHasARealTouchTargetAndResumeKeepsTheSameGeometry() = runComposeUiTest {
        mainClock.autoAdvance = false
        val active = mutableStateOf(false)
        var starts = 0
        var resumes = 0
        setContent {
            PracticeViewport(fontScale = 2f, rtl = true, dark = true) {
                PracticeSetRow(
                    practiceSet = set(), missingPresetCount = 0, isActive = active.value,
                    isReordering = false, canMoveUp = false, canMoveDown = false,
                    onStart = { starts++ }, onResume = { resumes++ }, onEdit = {}, onDelete = {},
                    onMoveUp = {}, onMoveDown = {},
                )
            }
        }
        settle()
        val start = onNode(hasContentDescription("Start set")).fetchSemanticsNode().boundsInRoot
        assertTrue(start.width >= 48f && start.height >= 48f)
        onNode(hasContentDescription("Start set")).performClick()
        assertEquals(1, starts)
        runOnUiThread { active.value = true }
        settle()
        val resume = onNode(hasContentDescription("Resume set")).fetchSemanticsNode().boundsInRoot
        assertEquals(start, resume)
        onNode(hasContentDescription("Resume set")).performClick()
        assertEquals(1, resumes)
        assertEquals(1, starts)
    }

    @Test
    fun largeRtlTimerChoicesWrapWithoutClippingAndStartTheChosenDuration() = runComposeUiTest {
        mainClock.autoAdvance = false
        var selectedMinutes = 0
        setContent {
            PracticeViewport(fontScale = 2f, rtl = true, dark = true) {
                ConfigTimerContent(
                    isPlaying = false, lastTimerMinutes = 10,
                    todayPracticeTime = 120_000, practiceStreak = 2, totalPracticeTime = 7_200_000,
                    onStart = { selectedMinutes = it },
                )
            }
        }
        settle()
        listOf("5 min", "10 min", "15 min", "20 min", "30 min").forEach { label -> assertTextFits(label) }
        onNode(hasText("30 min")).performClick()
        reveal(hasText("Start timer & play"))
        settle()
        onNode(hasText("Start timer & play")).assertIsDisplayed().performClick()
        assertEquals(30, selectedMinutes)
    }

    @Test
    fun largeTextTrainerParametersRemainReachableAndPreserveTheirValues() = runComposeUiTest {
        mainClock.autoAdvance = false
        var config: GradualTempoConfig? = null
        setContent {
            PracticeViewport(fontScale = 2f, rtl = true, dark = true) {
                TrainerConfigContent(
                    currentBpm = 80, beatsPerBar = 4, isPlaying = false,
                    seedConfig = GradualTempoConfig(80, 120, 2, 4),
                    onStartTrainer = { config = it },
                )
            }
        }
        settle()
        val start = onNode(hasText("Start")).fetchSemanticsNode().boundsInRoot
        val target = onNode(hasText("Target")).fetchSemanticsNode().boundsInRoot
        assertTrue(target.top > start.bottom, "Large-text tempo fields must stack")
        reveal(hasText("+5")).performClick()
        reveal(hasText("8 bars")).performClick()
        reveal(hasText("Start trainer & play"))
        settle()
        onNode(hasText("Start trainer & play")).performClick()
        assertEquals(GradualTempoConfig(80, 120, 5, 8), config)
    }

    @Test
    fun activeGapEditsUpdateTheExistingTrainerAndStopStaysIndependent() = runComposeUiTest {
        mainClock.autoAdvance = false
        var config: GapTrainerConfig? = null
        var stops = 0
        var starts = 0
        setContent {
            PracticeViewport(fontScale = 2f, rtl = true, dark = true) {
                GapTrainerContent(
                    isPlaying = true, activeConfig = GapTrainerConfig(4, 2), seedConfig = null,
                    onUpdate = { config = it }, onStartAndDismiss = { starts++ }, onStop = { stops++ },
                )
            }
        }
        settle()
        reveal(hasText("8 bars")).performClick()
        assertEquals(GapTrainerConfig(8, 2), config)
        reveal(hasText("1 bar")).performClick()
        assertEquals(GapTrainerConfig(8, 1), config)
        reveal(hasText("Stop gap trainer"))
        settle()
        onNode(hasText("Stop gap trainer")).performClick()
        assertEquals(1, stops)
        assertEquals(0, starts)
    }

    @Test
    fun sessionRecoveryNavigationAndFinishRemainAvailableAtLargeRtlText() = runComposeUiTest {
        mainClock.autoAdvance = false
        var playback = 0
        var next = 0
        var restores = 0
        var finishRequests = 0
        setContent {
            PracticeViewport(fontScale = 2f, rtl = true, dark = true) {
                PracticeSessionContent(
                    session = session(), isRecovered = true, onPrevious = {},
                    onTogglePlayback = { playback++ }, onNext = { next++ }, onRestart = { restores++ },
                    onRequestFinish = { finishRequests++ },
                )
            }
        }
        settle()
        assertTextFits("Recovered session · paused")
        reveal(hasText("Restore this step")).performClick()
        reveal(hasText("Resume")).performClick()
        reveal(hasText("Next step")).performClick()
        reveal(hasText("Finish session"))
        settle()
        onNode(hasText("Finish session")).performClick()
        assertEquals(1, playback)
        assertEquals(1, next)
        assertEquals(1, restores)
        assertEquals(1, finishRequests)
    }

    @Test
    fun neutralHubRowKeepsItsLabelAndActionAtLargeRtlText() = runComposeUiTest {
        mainClock.autoAdvance = false
        var clicks = 0
        setContent {
            PracticeViewport(fontScale = 2f, rtl = true, dark = true) {
                PracticeHubRow(Lucide.Timer, "Timer", value = "15 min", onClick = { clicks++ })
            }
        }
        settle()
        assertTextFits("Timer")
        assertTextFits("15 min")
        val target = onNode(hasText("Timer")).fetchSemanticsNode().boundsInRoot
        assertTrue(target.height >= 48f)
        onNode(hasText("Timer")).performClick()
        assertEquals(1, clicks)
    }

    private fun ComposeUiTest.reveal(matcher: SemanticsMatcher): SemanticsNodeInteraction {
        val target = onNode(matcher)
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
        throw AssertionError("The control did not become completely visible after12 bounded scroll requests: $matcher")
    }

    private fun ComposeUiTest.settle() {
        mainClock.advanceTimeBy(2000)
        waitForIdle()
    }

    private fun ComposeUiTest.assertTextFits(label: String) {
        val node = onNode(hasText(label), useUnmergedTree = true)
        val layouts = mutableListOf<TextLayoutResult>()
        node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        val layout = layouts.single()
        assertTrue(!layout.multiParagraph.didExceedMaxLines, "$label must remain readable")
        assertTrue(layout.multiParagraph.height <= layout.size.height + 0.5f, "$label must not clip vertically")
        assertTrue(layout.multiParagraph.intrinsics.minIntrinsicWidth <= layout.size.width + 0.5f, "$label must fit without breaking words")
    }
}

@Composable
private fun PracticeViewport(
    fontScale: Float = 1f,
    rtl: Boolean = false,
    dark: Boolean = false,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalDensity provides Density(1f, fontScale),
        LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
    ) {
        MaterialTheme(colorScheme = if (dark) AppColorScheme.MELROSE.darkColor else AppColorScheme.MELROSE.lightColor) {
            Box(Modifier.width(320.dp).height(640.dp).background(MaterialTheme.colorScheme.surface).testTag("viewport")) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)) { content() }
            }
        }
    }
}

private fun preset() = PracticePreset(
    id = "warm-up", name = "Warm-up", createdAtEpochMillis = 0, lastUsedAtEpochMillis = null,
    isFavourite = false, sortPosition = 0, bpm = 80, timeSignature = TimeSignature.FOUR_FOUR,
    subdivision = Subdivision.QUARTER, beats = TimeSignature.FOUR_FOUR.defaultBeats, countInEnabled = false,
)

private fun set() = PracticeSet(
    id = "daily", name = "Warm-up", createdAtEpochMillis = 0, updatedAtEpochMillis = 0,
    lastStartedAtEpochMillis = null, lastCompletedAtEpochMillis = null, sortPosition = 0,
    steps = listOf(PracticeSetStep("one", "warm-up", PracticeStepTarget.Duration(2))),
)

private fun session() = ActivePracticeSession(
    id = "session", sourceSetId = "daily", setName = "Daily practice",
    steps = listOf(
        ResolvedPracticeStep("one", preset(), PracticeStepTarget.Duration(2)),
        ResolvedPracticeStep("two", preset().copy(id = "target", name = "Build speed"), PracticeStepTarget.Bars(8)),
    ),
    currentStepIndex = 0, pendingStepIndex = null, elapsedMillis = 30_000, completedBars = 0,
    playbackIntent = PracticePlaybackIntent.Paused, targetReached = false, currentStepEdited = true,
    startedAtEpochMillis = 0, lastCheckpointAtEpochMillis = 0,
)
