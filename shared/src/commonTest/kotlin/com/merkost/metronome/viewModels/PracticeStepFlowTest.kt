package com.merkost.metronome.viewModels

import androidx.lifecycle.viewModelScope
import com.merkost.metronome.model.AppDatastore
import com.merkost.metronome.model.AppDatastoreImpl
import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.GapTrainerConfig
import com.merkost.metronome.model.GradualTempoConfig
import com.merkost.metronome.model.Subdivision
import com.merkost.metronome.model.TimeSignature
import com.merkost.metronome.platform.AppVersionInfo
import com.merkost.metronome.platform.AppVersionProvider
import com.merkost.metronome.platform.HapticProvider
import com.merkost.metronome.practiceSets.ActivePracticeSession
import com.merkost.metronome.practiceSets.PracticePlaybackIntent
import com.merkost.metronome.practiceSets.PracticeSessionController
import com.merkost.metronome.practiceSets.ResolvedPracticeStep
import com.merkost.metronome.practiceSets.PracticeSessionRepository
import com.merkost.metronome.practiceSets.PracticeSet
import com.merkost.metronome.practiceSets.PracticeSetStep
import com.merkost.metronome.practiceSets.PracticeStepTarget
import com.merkost.metronome.presets.DataStorePracticePresetRepository
import com.merkost.metronome.presets.PracticePreset
import com.merkost.metronome.presets.InMemoryPreferencesDataStore
import com.merkost.metronome.presets.PracticePresetDraft
import com.merkost.metronome.review.InAppReviewRequester
import com.merkost.metronome.review.ReviewPromptCoordinator
import com.merkost.metronome.review.ReviewPromptRecord
import com.merkost.metronome.review.ReviewPromptStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PracticeStepFlowTest {
    @Test
    fun aStepRequestedWhilePlayingIsAppliedWhenPlaybackPausesBeforeTheBarEnds() = runTest {
        withFixture { fixture ->
            fixture.viewModel.startPracticeSet(twoStepSet())
            runCurrent()
            assertTrue(fixture.viewModel.metronomeState.value.playing)
            assertEquals(100, fixture.viewModel.metronomeState.value.rhythm)

            fixture.viewModel.nextPracticeStep()
            runCurrent()
            assertEquals(1, fixture.sessions.session.value?.pendingStepIndex)

            fixture.viewModel.onStopClicked()
            runCurrent()

            val session = fixture.sessions.session.value
            assertNull(session?.pendingStepIndex)
            assertEquals(1, session?.currentStepIndex)
            assertEquals(140, fixture.viewModel.metronomeState.value.rhythm)
            assertEquals(TimeSignature.THREE_FOUR, fixture.viewModel.metronomeState.value.timeSignature)
        }
    }

    @Test
    fun theFirstStepsCountInSettingIsVisibleBeforeItsSlowSaveCompletes() = runTest {
        withFixture(countInFirstStep = true) { fixture ->
            fixture.store.countInGate = CompletableDeferred()
            fixture.viewModel.startPracticeSet(twoStepSet())
            runCurrent()

            assertTrue(fixture.viewModel.metronomeState.value.playing)
            assertTrue(fixture.viewModel.countInEnabled.value)
            fixture.store.countInGate?.complete(Unit)
            runCurrent()
            assertTrue(fixture.viewModel.countInEnabled.value)
        }
    }

    @Test
    fun recoveredSessionKeepsItsStepAccentPatternWhenOnboardingLoadFinishesLast() = runTest {
        val accents = listOf(Beat.HIGH, Beat.MUTE, Beat.LOW, Beat.LOW)
        val preset = PracticePreset(
            id = "recovered", name = "Recovered", createdAtEpochMillis = 0L, lastUsedAtEpochMillis = null,
            isFavourite = false, sortPosition = 0, bpm = 90, timeSignature = TimeSignature.FOUR_FOUR,
            subdivision = Subdivision.QUARTER, beats = accents, countInEnabled = false,
        )
        val session = ActivePracticeSession(
            id = "recovered-session", sourceSetId = "set", setName = "Daily",
            steps = listOf(ResolvedPracticeStep("step", preset, PracticeStepTarget.None)),
            currentStepIndex = 0, pendingStepIndex = null, elapsedMillis = 0L, completedBars = 0,
            playbackIntent = PracticePlaybackIntent.Paused, targetReached = false, currentStepEdited = false,
            startedAtEpochMillis = 1L, lastCheckpointAtEpochMillis = 1L,
        )
        withFixture(initialSession = session, slowTimeSignatureMillis = 100L) { fixture ->
            advanceTimeBy(500L)
            runCurrent()
            assertEquals(accents, fixture.viewModel.metronomeState.value.beats)
            assertEquals(90, fixture.viewModel.metronomeState.value.rhythm)
        }
    }

    @Test
    fun applyingAPresetKeepsTheGapTrainerAndStopsTheTempoTrainer() = runTest {
        withFixture { fixture ->
            val preset = PracticePreset(
                id = "manual", name = "Manual", createdAtEpochMillis = 0L, lastUsedAtEpochMillis = null,
                isFavourite = false, sortPosition = 0, bpm = 80, timeSignature = TimeSignature.FOUR_FOUR,
                subdivision = Subdivision.QUARTER, beats = listOf(Beat.HIGH, Beat.LOW, Beat.LOW, Beat.LOW),
                countInEnabled = false,
            )
            fixture.viewModel.startGapTrainer(GapTrainerConfig(3, 1))
            fixture.viewModel.startGradualTempo(GradualTempoConfig(startBpm = 90, endBpm = 120))
            runCurrent()
            fixture.viewModel.onStopClicked()
            runCurrent()

            fixture.viewModel.applyPracticePreset(preset)
            runCurrent()

            assertEquals(GapTrainerConfig(3, 1), fixture.viewModel.gapTrainerConfig.value)
            assertNull(fixture.viewModel.gradualTempoConfig.value)
            assertEquals(80, fixture.viewModel.metronomeState.value.rhythm)
        }
    }

    @Test
    fun theTimerEndingStopsPlaybackAfterTheCurrentBarWhenTheOptionIsOn() = runTest {
        withFixture { fixture ->
            fixture.viewModel.onStopWhenTimerEndsChanged(true)
            runCurrent()
            fixture.viewModel.startPracticeTimer(1)
            runCurrent()
            advanceTimeBy(61_000L)
            runCurrent()
            assertEquals(0L, fixture.viewModel.practiceTimerRemaining.value)
            assertTrue(fixture.viewModel.metronomeState.value.playing)
            fixture.viewModel.onBarCompleted(1)
            runCurrent()
            assertFalse(fixture.viewModel.metronomeState.value.playing)
        }
    }

    @Test
    fun theTimerEndingKeepsPlayingWhenTheOptionIsOff() = runTest {
        withFixture { fixture ->
            fixture.viewModel.startPracticeTimer(1)
            runCurrent()
            advanceTimeBy(61_000L)
            runCurrent()
            fixture.viewModel.onBarCompleted(1)
            runCurrent()
            assertTrue(fixture.viewModel.metronomeState.value.playing)
        }
    }

    @Test
    fun dismissingTheTimerAfterItEndsCancelsAPendingStop() = runTest {
        withFixture { fixture ->
            fixture.viewModel.onStopWhenTimerEndsChanged(true)
            runCurrent()
            fixture.viewModel.startPracticeTimer(1)
            runCurrent()
            advanceTimeBy(61_000L)
            runCurrent()
            fixture.viewModel.dismissPracticeTimer()
            fixture.viewModel.onBarCompleted(1)
            runCurrent()
            assertTrue(fixture.viewModel.metronomeState.value.playing)
        }
    }

    @Test
    fun extendingTheTimerAfterItEndsCancelsAPendingStop() = runTest {
        withFixture { fixture ->
            fixture.viewModel.onStopWhenTimerEndsChanged(true)
            runCurrent()
            fixture.viewModel.startPracticeTimer(1)
            runCurrent()
            advanceTimeBy(61_000L)
            runCurrent()
            fixture.viewModel.extendPracticeTimer(5)
            runCurrent()
            fixture.viewModel.onBarCompleted(1)
            runCurrent()
            assertTrue(fixture.viewModel.metronomeState.value.playing)
        }
    }

    @Test
    fun theTimerStoppingPlaybackIsReportedUntilDismissed() = runTest {
        withFixture { fixture ->
            fixture.viewModel.onStopWhenTimerEndsChanged(true)
            runCurrent()
            fixture.viewModel.startPracticeTimer(1)
            runCurrent()
            advanceTimeBy(61_000L)
            runCurrent()
            assertFalse(fixture.viewModel.practiceTimerStoppedPlayback.value)
            fixture.viewModel.onBarCompleted(1)
            runCurrent()
            assertFalse(fixture.viewModel.metronomeState.value.playing)
            assertTrue(fixture.viewModel.practiceTimerStoppedPlayback.value)
            fixture.viewModel.dismissPracticeTimer()
            assertFalse(fixture.viewModel.practiceTimerStoppedPlayback.value)
        }
    }

    @Test
    fun theTimerKeepsPlayingWithoutReportingAStopWhenTheOptionIsOff() = runTest {
        withFixture { fixture ->
            fixture.viewModel.startPracticeTimer(1)
            runCurrent()
            advanceTimeBy(61_000L)
            runCurrent()
            fixture.viewModel.onBarCompleted(1)
            runCurrent()
            assertFalse(fixture.viewModel.practiceTimerStoppedPlayback.value)
        }
    }

    @Test
    fun startingAnotherTimerClearsTheStoppedReport() = runTest {
        withFixture { fixture ->
            fixture.viewModel.onStopWhenTimerEndsChanged(true)
            runCurrent()
            fixture.viewModel.startPracticeTimer(1)
            runCurrent()
            advanceTimeBy(61_000L)
            runCurrent()
            fixture.viewModel.onBarCompleted(1)
            runCurrent()
            fixture.viewModel.restartPracticeTimer()
            runCurrent()
            assertFalse(fixture.viewModel.practiceTimerStoppedPlayback.value)
        }
    }

    @Test
    fun settingABeatChangesOnlyThatBeatAndOnlyWhenItDiffers() = runTest {
        withFixture { fixture ->
            val before = fixture.viewModel.metronomeState.value.beats
            val target = if (before[1] == Beat.HIGH) Beat.MUTE else Beat.HIGH
            fixture.viewModel.setBeat(1, target)
            val after = fixture.viewModel.metronomeState.value.beats
            assertEquals(target, after[1])
            assertEquals(before.filterIndexed { i, _ -> i != 1 }, after.filterIndexed { i, _ -> i != 1 })
            fixture.viewModel.setBeat(1, target)
            assertEquals(after, fixture.viewModel.metronomeState.value.beats)
        }
    }

    private fun twoStepSet() = PracticeSet(
        "set", "Daily practice", 0L, 0L, null, null, 0,
        listOf(
            PracticeSetStep("step-a", "preset-1", PracticeStepTarget.None),
            PracticeSetStep("step-b", "preset-2", PracticeStepTarget.None),
        ),
    )

    private suspend fun TestScope.withFixture(countInFirstStep: Boolean = false, initialSession: ActivePracticeSession? = null, slowTimeSignatureMillis: Long = 0L, block: suspend TestScope.(Fixture) -> Unit) {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var viewModel: MetronomeViewModel? = null
        var fixture: Fixture? = null
        try {
            val preferences = InMemoryPreferencesDataStore()
            val store = GatedStore(AppDatastoreImpl(preferences), slowTimeSignatureMillis)
            store.saveOnboardingComplete(true)
            var counter = 0
            val presets = DataStorePracticePresetRepository(preferences, { "preset-${++counter}" }, { 0L })
            presets.create(PracticePresetDraft("Slow four", 100, TimeSignature.FOUR_FOUR, Subdivision.QUARTER, listOf(Beat.HIGH, Beat.LOW, Beat.LOW, Beat.LOW), countInFirstStep))
            presets.create(PracticePresetDraft("Fast three", 140, TimeSignature.THREE_FOUR, Subdivision.QUARTER, listOf(Beat.HIGH, Beat.LOW, Beat.LOW), false))
            val sessions = MemorySessionRepository().also { it.session.value = initialSession }
            val controller = PracticeSessionController(sessions, { "step-session" }, { testScheduler.currentTime })
            val current = Fixture(store, sessions)
            fixture = current
            val review = ReviewPromptCoordinator(
                store = object : ReviewPromptStore {
                    override suspend fun read() = ReviewPromptRecord()
                    override suspend fun markRequested(version: String, atMillis: Long) = Unit
                },
                requester = object : InAppReviewRequester { override fun requestReview() = false },
                appVersionProvider = object : AppVersionProvider {
                    override fun getAppVersion() = AppVersionInfo("1.5.0", 1L)
                },
                nowMillis = { testScheduler.currentTime },
            )
            viewModel = MetronomeViewModel(
                appDatastore = store,
                hapticProvider = object : HapticProvider {
                    override fun playBeatHaptic(beat: Beat) = Unit
                    override fun playConfirmHaptic() = Unit
                },
                reviewPromptCoordinator = review,
                practicePresetRepository = presets,
                practiceSessionController = controller,
                nowMillis = { testScheduler.currentTime },
            )
            current.viewModel = viewModel
            runCurrent()
            block(current)
        } finally {
            fixture?.store?.countInGate?.complete(Unit)
            viewModel?.viewModelScope?.cancel()
            runCurrent()
            Dispatchers.resetMain()
        }
    }

    private class Fixture(val store: GatedStore, val sessions: MemorySessionRepository) {
        lateinit var viewModel: MetronomeViewModel
    }

    private class GatedStore(private val delegate: AppDatastore, private val timeSignatureDelay: Long = 0L) : AppDatastore by delegate {
        var countInGate: CompletableDeferred<Unit>? = null

        override val timeSignature: Flow<TimeSignature> = delegate.timeSignature.onEach { if (timeSignatureDelay > 0L) delay(timeSignatureDelay) }

        override suspend fun saveCountInEnabled(enabled: Boolean) {
            countInGate?.await()
            delegate.saveCountInEnabled(enabled)
        }
    }

    private class MemorySessionRepository : PracticeSessionRepository {
        override val session = MutableStateFlow<ActivePracticeSession?>(null)
        override suspend fun save(session: ActivePracticeSession): Boolean {
            this.session.value = session
            return true
        }
        override suspend fun clear(): Boolean {
            session.value = null
            return true
        }
    }
}
