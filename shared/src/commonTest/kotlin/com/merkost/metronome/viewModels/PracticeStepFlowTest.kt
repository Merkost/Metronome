package com.merkost.metronome.viewModels

import androidx.lifecycle.viewModelScope
import com.merkost.metronome.model.AppDatastore
import com.merkost.metronome.model.AppDatastoreImpl
import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.Subdivision
import com.merkost.metronome.model.TimeSignature
import com.merkost.metronome.platform.AppVersionInfo
import com.merkost.metronome.platform.AppVersionProvider
import com.merkost.metronome.platform.HapticProvider
import com.merkost.metronome.practiceSets.ActivePracticeSession
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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
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

    private fun twoStepSet() = PracticeSet(
        "set", "Daily practice", 0L, 0L, null, null, 0,
        listOf(
            PracticeSetStep("step-a", "preset-1", PracticeStepTarget.None),
            PracticeSetStep("step-b", "preset-2", PracticeStepTarget.None),
        ),
    )

    private suspend fun TestScope.withFixture(countInFirstStep: Boolean = false, block: suspend TestScope.(Fixture) -> Unit) {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var viewModel: MetronomeViewModel? = null
        var fixture: Fixture? = null
        try {
            val preferences = InMemoryPreferencesDataStore()
            val store = GatedStore(AppDatastoreImpl(preferences))
            store.saveOnboardingComplete(true)
            var counter = 0
            val presets = DataStorePracticePresetRepository(preferences, { "preset-${++counter}" }, { 0L })
            presets.create(PracticePresetDraft("Slow four", 100, TimeSignature.FOUR_FOUR, Subdivision.QUARTER, listOf(Beat.HIGH, Beat.LOW, Beat.LOW, Beat.LOW), countInFirstStep))
            presets.create(PracticePresetDraft("Fast three", 140, TimeSignature.THREE_FOUR, Subdivision.QUARTER, listOf(Beat.HIGH, Beat.LOW, Beat.LOW), false))
            val sessions = MemorySessionRepository()
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

    private class GatedStore(private val delegate: AppDatastore) : AppDatastore by delegate {
        var countInGate: CompletableDeferred<Unit>? = null

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
