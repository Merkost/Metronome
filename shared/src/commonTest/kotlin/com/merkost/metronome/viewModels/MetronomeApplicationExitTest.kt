package com.merkost.metronome.viewModels

import androidx.lifecycle.viewModelScope
import com.merkost.metronome.model.AppDatastore
import com.merkost.metronome.model.AppDatastoreImpl
import com.merkost.metronome.model.Beat
import com.merkost.metronome.platform.AppVersionInfo
import com.merkost.metronome.platform.AppVersionProvider
import com.merkost.metronome.platform.HapticProvider
import com.merkost.metronome.practiceSets.ActivePracticeSession
import com.merkost.metronome.practiceSets.PracticePlaybackIntent
import com.merkost.metronome.practiceSets.PracticeSessionController
import com.merkost.metronome.practiceSets.PracticeSessionRepository
import com.merkost.metronome.practiceSets.PracticeSet
import com.merkost.metronome.practiceSets.PracticeSetStep
import com.merkost.metronome.practiceSets.PracticeStepTarget
import com.merkost.metronome.presets.DataStorePracticePresetRepository
import com.merkost.metronome.presets.InMemoryPreferencesDataStore
import com.merkost.metronome.presets.PracticePresetDraft
import com.merkost.metronome.review.InAppReviewRequester
import com.merkost.metronome.review.ReviewPromptCoordinator
import com.merkost.metronome.review.ReviewPromptRecord
import com.merkost.metronome.review.ReviewPromptStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MetronomeApplicationExitTest {
    @Test
    fun exitWaitsForPracticeTimeToReachPersistentStorage() = runTest {
        withFixture { fixture ->
            playFor(3_000L, fixture)
            fixture.store.saveGate = CompletableDeferred()

            val exit = async { fixture.viewModel.prepareForApplicationExit() }
            runCurrent()

            assertFalse(fixture.viewModel.metronomeState.value.playing)
            assertFalse(exit.isCompleted)
            assertEquals(0L, fixture.store.totalTime.first())
            fixture.viewModel.onPlayPauseClicked(false)
            runCurrent()
            assertFalse(fixture.viewModel.metronomeState.value.playing)

            fixture.store.saveGate?.complete(Unit)
            runCurrent()
            exit.await()

            assertEquals(3_000L, fixture.store.totalTime.first())
            fixture.viewModel.onPlayPauseClicked(false)
            runCurrent()
            assertTrue(fixture.viewModel.metronomeState.value.playing)
        }
    }

    @Test
    fun repeatedExitDuringAnExistingPauseSavesTheSessionOnlyOnce() = runTest {
        withFixture { fixture ->
            playFor(2_000L, fixture)
            fixture.store.saveGate = CompletableDeferred()
            fixture.viewModel.onStopClicked()
            runCurrent()

            val firstExit = async { fixture.viewModel.prepareForApplicationExit() }
            val secondExit = async { fixture.viewModel.prepareForApplicationExit() }
            runCurrent()
            assertFalse(firstExit.isCompleted)
            assertFalse(secondExit.isCompleted)

            fixture.store.saveGate?.complete(Unit)
            runCurrent()
            firstExit.await()
            secondExit.await()

            assertEquals(2_000L, fixture.store.totalTime.first())
            assertEquals(1, fixture.store.positiveWrites)
        }
    }

    @Test
    fun resumeCannotCancelAccountingAfterThePracticeTimeHasCommitted() = runTest {
        withFixture { fixture ->
            playFor(1_000L, fixture)
            fixture.store.afterCommitGate = CompletableDeferred()
            fixture.viewModel.onStopClicked()
            runCurrent()
            assertEquals(1_000L, fixture.store.totalTime.first())

            fixture.viewModel.onPlayPauseClicked(false)
            runCurrent()
            val exit = async { fixture.viewModel.prepareForApplicationExit() }
            runCurrent()
            assertFalse(exit.isCompleted)

            fixture.store.afterCommitGate?.complete(Unit)
            runCurrent()
            exit.await()

            assertFalse(fixture.viewModel.metronomeState.value.playing)
            assertEquals(1_000L, fixture.store.totalTime.first())
            assertEquals(1, fixture.store.positiveWrites)
        }
    }

    @Test
    fun exitWaitsForTheStructuredPracticePauseCheckpoint() = runTest {
        withFixture { fixture ->
            val state = fixture.viewModel.metronomeState.value
            fixture.presets.create(
                PracticePresetDraft("Warm up", state.rhythm, state.timeSignature, state.subdivision, state.beats, false),
            )
            val practiceSet = PracticeSet(
                "set", "Daily practice", 0L, 0L, null, null, 0,
                listOf(PracticeSetStep("step", "exit-preset", PracticeStepTarget.None)),
            )
            fixture.viewModel.startPracticeSet(practiceSet)
            runCurrent()
            advanceTimeBy(2_000L)
            runCurrent()
            fixture.sessions.pauseGate = CompletableDeferred()

            val exit = async { fixture.viewModel.prepareForApplicationExit() }
            runCurrent()
            assertFalse(exit.isCompleted)

            fixture.sessions.pauseGate?.complete(Unit)
            runCurrent()
            exit.await()

            assertFalse(fixture.viewModel.metronomeState.value.playing)
            assertEquals(PracticePlaybackIntent.Paused, fixture.sessions.session.value?.playbackIntent)
            assertEquals(2_000L, fixture.store.totalTime.first())
        }
    }

    @Test
    fun exitCancelsAReviewThatWasWaitingForAQuietPause() = runTest {
        withFixture { fixture ->
            playFor(300_000L, fixture)
            fixture.viewModel.onStopClicked()
            runCurrent()
            assertEquals(300_000L, fixture.store.totalTime.first())

            fixture.viewModel.prepareForApplicationExit()
            advanceTimeBy(2_000L)
            runCurrent()

            assertEquals(0, fixture.reviewRequests)
        }
    }

    @Test
    fun failedSessionCheckpointRejectsExitAndCanBeRetried() = runTest {
        withFixture { fixture ->
            val state = fixture.viewModel.metronomeState.value
            fixture.presets.create(
                PracticePresetDraft("Warm up", state.rhythm, state.timeSignature, state.subdivision, state.beats, false),
            )
            fixture.viewModel.startPracticeSet(
                PracticeSet(
                    "set", "Daily practice", 0L, 0L, null, null, 0,
                    listOf(PracticeSetStep("step", "exit-preset", PracticeStepTarget.None)),
                ),
            )
            runCurrent()
            advanceTimeBy(2_000L)
            runCurrent()
            fixture.sessions.failPausedWrites = true

            val exit = async { runCatching { fixture.viewModel.prepareForApplicationExit() } }
            runCurrent()

            assertTrue(exit.await().isFailure)
            assertFalse(fixture.viewModel.metronomeState.value.playing)
            assertEquals(PracticePlaybackIntent.Running, fixture.sessions.session.value?.playbackIntent)

            fixture.sessions.failPausedWrites = false
            fixture.viewModel.prepareForApplicationExit()

            assertEquals(PracticePlaybackIntent.Paused, fixture.sessions.session.value?.playbackIntent)
            assertEquals(2_000L, fixture.store.totalTime.first())
            assertEquals(1, fixture.store.positiveWrites)
            fixture.viewModel.resumePracticeSession()
            runCurrent()
            assertTrue(fixture.viewModel.metronomeState.value.playing)
        }
    }

    @Test
    fun failedPracticeTimeWriteRejectsExitAndCanBeRetried() = runTest {
        withFixture { fixture ->
            playFor(2_000L, fixture)
            fixture.store.failWrites = true

            val exit = async { runCatching { fixture.viewModel.prepareForApplicationExit() } }
            runCurrent()

            assertTrue(exit.await().isFailure)
            assertEquals(0L, fixture.store.totalTime.first())
            assertFalse(fixture.viewModel.metronomeState.value.playing)

            fixture.store.failWrites = false
            fixture.viewModel.prepareForApplicationExit()

            assertEquals(2_000L, fixture.store.totalTime.first())
            assertEquals(1, fixture.store.positiveWrites)
        }
    }

    @Test
    fun resumingAfterAFailedSavePreservesUnsavedPracticeTime() = runTest {
        withFixture { fixture ->
            playFor(2_000L, fixture)
            fixture.store.failWrites = true
            val exit = async { runCatching { fixture.viewModel.prepareForApplicationExit() } }
            runCurrent()
            assertTrue(exit.await().isFailure)
            assertEquals(0L, fixture.store.totalTime.first())

            fixture.store.failWrites = false
            fixture.viewModel.onPlayPauseClicked(false)
            runCurrent()
            advanceTimeBy(1_000L)
            runCurrent()
            assertEquals(3_000L, fixture.viewModel.metronomeState.value.stopWatchState.elapsedTime)

            val retry = async { fixture.viewModel.prepareForApplicationExit() }
            runCurrent()
            retry.await()
            fixture.viewModel.prepareForApplicationExit()

            assertEquals(3_000L, fixture.store.totalTime.first())
            assertEquals(1, fixture.store.positiveWrites)
        }
    }

    private suspend fun TestScope.withFixture(block: suspend TestScope.(Fixture) -> Unit) {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var viewModel: MetronomeViewModel? = null
        var fixture: Fixture? = null
        try {
            val preferences = InMemoryPreferencesDataStore()
            val store = ControlledPracticeStore(AppDatastoreImpl(preferences))
            store.saveOnboardingComplete(true)
            val presets = DataStorePracticePresetRepository(preferences, { "exit-preset" }, { 0L })
            val sessions = ControlledSessionRepository()
            val controller = PracticeSessionController(sessions, { "exit-session" }, { testScheduler.currentTime })
            val currentFixture = Fixture(store, presets, sessions)
            fixture = currentFixture
            val review = ReviewPromptCoordinator(
                store = object : ReviewPromptStore {
                    override suspend fun read() = ReviewPromptRecord()
                    override suspend fun markRequested(version: String, atMillis: Long) = Unit
                },
                requester = object : InAppReviewRequester {
                    override fun requestReview(): Boolean {
                        currentFixture.reviewRequests++
                        return true
                    }
                },
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
            currentFixture.viewModel = viewModel
            runCurrent()
            block(currentFixture)
        } finally {
            fixture?.store?.saveGate?.complete(Unit)
            fixture?.store?.afterCommitGate?.complete(Unit)
            fixture?.sessions?.pauseGate?.complete(Unit)
            viewModel?.viewModelScope?.cancel()
            runCurrent()
            Dispatchers.resetMain()
        }
    }

    private suspend fun TestScope.playFor(millis: Long, fixture: Fixture) {
        fixture.viewModel.onPlayPauseClicked(false)
        runCurrent()
        advanceTimeBy(millis)
        runCurrent()
    }

    private class Fixture(
        val store: ControlledPracticeStore,
        val presets: DataStorePracticePresetRepository,
        val sessions: ControlledSessionRepository,
    ) {
        lateinit var viewModel: MetronomeViewModel
        var reviewRequests = 0
    }

    private class ControlledPracticeStore(private val delegate: AppDatastore) : AppDatastore by delegate {
        var saveGate: CompletableDeferred<Unit>? = null
        var afterCommitGate: CompletableDeferred<Unit>? = null
        var positiveWrites = 0
        var failWrites = false

        override suspend fun addTotalTime(elapsedTime: Long) {
            if (elapsedTime > 0L) {
                saveGate?.await()
                if (failWrites) error("practice storage unavailable")
                delegate.addTotalTime(elapsedTime)
                positiveWrites++
                afterCommitGate?.await()
            }
        }
    }

    private class ControlledSessionRepository : PracticeSessionRepository {
        override val session = MutableStateFlow<ActivePracticeSession?>(null)
        var pauseGate: CompletableDeferred<Unit>? = null
        var failPausedWrites = false

        override suspend fun save(session: ActivePracticeSession): Boolean {
            if (session.playbackIntent == PracticePlaybackIntent.Paused) {
                pauseGate?.await()
                if (failPausedWrites) return false
            }
            this.session.value = session
            return true
        }

        override suspend fun clear(): Boolean {
            session.value = null
            return true
        }
    }
}
