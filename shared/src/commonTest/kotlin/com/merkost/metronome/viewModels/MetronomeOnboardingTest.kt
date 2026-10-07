package com.merkost.metronome.viewModels

import androidx.lifecycle.viewModelScope
import com.merkost.metronome.model.AppDatastore
import com.merkost.metronome.model.AppDatastoreImpl
import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.ClickSound
import com.merkost.metronome.platform.AppVersionInfo
import com.merkost.metronome.platform.AppVersionProvider
import com.merkost.metronome.platform.HapticProvider
import com.merkost.metronome.presets.DataStorePracticePresetRepository
import com.merkost.metronome.presets.InMemoryPreferencesDataStore
import com.merkost.metronome.review.InAppReviewRequester
import com.merkost.metronome.review.ReviewPromptCoordinator
import com.merkost.metronome.review.ReviewPromptRecord
import com.merkost.metronome.review.ReviewPromptStore
import com.merkost.metronome.whatsnew.WhatsNewCoordinator
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MetronomeOnboardingTest {
    @Test
    fun waitsForStoredFirstUseStateBeforeShowingWelcome() = runTest {
        withFixture(deferInitialRead = true) { fixture ->
            runCurrent()
            assertFalse(fixture.viewModel.onboardingLoaded.value)
            assertFalse(fixture.viewModel.onboardingVisible.value)

            fixture.viewModel.completeOnboarding(120, ClickSound.RIM, true)
            runCurrent()
            assertEquals(0, fixture.store.completionWrites)
            assertFalse(fixture.viewModel.metronomeState.value.playing)

            fixture.store.initialReadGate?.complete(Unit)
            runCurrent()
            assertTrue(fixture.viewModel.onboardingLoaded.value)
            assertTrue(fixture.viewModel.onboardingVisible.value)
            assertNull(fixture.viewModel.whatsNewVersion.value)
        }
    }

    @Test
    fun keepsPreviouslyCompletedOnboardingComplete() = runTest {
        withFixture(completed = true) { fixture ->
            runCurrent()
            assertTrue(fixture.viewModel.onboardingLoaded.value)
            assertFalse(fixture.viewModel.onboardingVisible.value)
            assertTrue(fixture.store.onboardingComplete.first())
            assertEquals(0, fixture.store.completionWrites)
            assertFalse(fixture.viewModel.metronomeState.value.playing)
        }
    }

    @Test
    fun startWaitsForPersistenceAndThenUsesChosenPaceAndSound() = runTest {
        withFixture { fixture ->
            runCurrent()
            fixture.store.saveGate = CompletableDeferred()
            fixture.viewModel.completeOnboarding(120, ClickSound.RIM, true)
            runCurrent()

            assertTrue(fixture.viewModel.onboardingVisible.value)
            assertTrue(fixture.viewModel.onboardingSaving.value)
            assertFalse(fixture.viewModel.metronomeState.value.playing)
            assertFalse(fixture.store.onboardingComplete.first())

            fixture.store.saveGate?.complete(Unit)
            runCurrent()

            assertFalse(fixture.viewModel.onboardingVisible.value)
            assertFalse(fixture.viewModel.onboardingSaving.value)
            assertTrue(fixture.store.onboardingComplete.first())
            assertEquals(ClickSound.RIM, fixture.store.selectedSound.first())
            assertEquals(120, fixture.viewModel.metronomeState.value.rhythm)
            assertTrue(fixture.viewModel.metronomeState.value.playing)
            assertNull(fixture.viewModel.whatsNewVersion.value)
        }
    }

    @Test
    fun skipCompletesWithoutStartingPlaybackOrAddingPracticeTime() = runTest {
        withFixture { fixture ->
            runCurrent()
            fixture.viewModel.completeOnboarding(60, ClickSound.SOFT, false)
            runCurrent()

            assertTrue(fixture.store.onboardingComplete.first())
            assertFalse(fixture.viewModel.onboardingVisible.value)
            assertFalse(fixture.viewModel.metronomeState.value.playing)
            assertEquals(0L, fixture.store.totalTime.first())
            assertEquals(60, fixture.viewModel.metronomeState.value.rhythm)
            assertEquals(ClickSound.SOFT, fixture.store.selectedSound.first())
        }
    }

    @Test
    fun failedPersistenceKeepsWelcomeVisibleAndAllowsRetry() = runTest {
        withFixture { fixture ->
            runCurrent()
            fixture.store.failCompletion = true
            fixture.viewModel.completeOnboarding(80, ClickSound.CLAVE, true)
            runCurrent()

            assertTrue(fixture.viewModel.onboardingVisible.value)
            assertFalse(fixture.viewModel.onboardingSaving.value)
            assertFalse(fixture.viewModel.metronomeState.value.playing)
            assertFalse(fixture.store.onboardingComplete.first())
            assertNotNull(fixture.viewModel.onboardingError.value)

            fixture.store.failCompletion = false
            fixture.viewModel.completeOnboarding(80, ClickSound.CLAVE, true)
            runCurrent()

            assertFalse(fixture.viewModel.onboardingVisible.value)
            assertTrue(fixture.viewModel.metronomeState.value.playing)
            assertTrue(fixture.store.onboardingComplete.first())
            assertNull(fixture.viewModel.onboardingError.value)
        }
    }

    @Test
    fun backgroundingDuringSavePreventsUnexpectedAutoplay() = runTest {
        withFixture { fixture ->
            runCurrent()
            fixture.store.saveGate = CompletableDeferred()
            fixture.viewModel.completeOnboarding(120, ClickSound.STUDIO, true)
            runCurrent()
            fixture.viewModel.onOnboardingBackgrounded()
            fixture.store.saveGate?.complete(Unit)
            runCurrent()

            assertTrue(fixture.store.onboardingComplete.first())
            assertFalse(fixture.viewModel.onboardingVisible.value)
            assertFalse(fixture.viewModel.metronomeState.value.playing)
        }
    }

    @Test
    fun hydrationFailureCanRetryWithoutTreatingExistingUserAsNew() = runTest {
        withFixture(completed = true, failInitialRead = true) { fixture ->
            runCurrent()
            assertFalse(fixture.viewModel.onboardingLoaded.value)
            assertFalse(fixture.viewModel.onboardingVisible.value)
            assertNotNull(fixture.viewModel.onboardingLoadError.value)

            fixture.store.failInitialRead = false
            fixture.viewModel.retryOnboardingLoad()
            runCurrent()

            assertTrue(fixture.viewModel.onboardingLoaded.value)
            assertFalse(fixture.viewModel.onboardingVisible.value)
            assertNull(fixture.viewModel.onboardingLoadError.value)
            assertEquals(0, fixture.store.completionWrites)
        }
    }

    @Test
    fun repeatedStartWhileSavingWritesCompletionOnce() = runTest {
        withFixture { fixture ->
            runCurrent()
            fixture.store.saveGate = CompletableDeferred()
            fixture.viewModel.completeOnboarding(80, ClickSound.WOOD, true)
            fixture.viewModel.completeOnboarding(120, ClickSound.RIM, true)
            runCurrent()
            assertEquals(1, fixture.store.completionWrites)

            fixture.store.saveGate?.complete(Unit)
            runCurrent()
            assertEquals(80, fixture.viewModel.metronomeState.value.rhythm)
            assertEquals(ClickSound.WOOD, fixture.store.selectedSound.first())
        }
    }

    private suspend fun TestScope.withFixture(
        completed: Boolean = false,
        deferInitialRead: Boolean = false,
        failInitialRead: Boolean = false,
        block: suspend TestScope.(Fixture) -> Unit,
    ) {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var viewModel: MetronomeViewModel? = null
        try {
            val preferences = InMemoryPreferencesDataStore()
            val delegate = AppDatastoreImpl(preferences)
            if (completed) delegate.saveOnboardingComplete(true)
            val store = ControlledOnboardingStore(delegate).apply {
                if (deferInitialRead) initialReadGate = CompletableDeferred()
                this.failInitialRead = failInitialRead
            }
            val versionProvider = object : AppVersionProvider {
                override fun getAppVersion() = AppVersionInfo("1.4.0", 10L)
            }
            val reviewCoordinator = ReviewPromptCoordinator(
                store = object : ReviewPromptStore {
                    override suspend fun read() = ReviewPromptRecord()
                    override suspend fun markRequested(version: String, atMillis: Long) = Unit
                },
                requester = object : InAppReviewRequester {
                    override fun requestReview() = false
                },
                appVersionProvider = versionProvider,
                nowMillis = { 0L },
            )
            viewModel = MetronomeViewModel(
                appDatastore = store,
                hapticProvider = object : HapticProvider {
                    override fun playBeatHaptic(beat: Beat) = Unit
                    override fun playConfirmHaptic() = Unit
                },
                reviewPromptCoordinator = reviewCoordinator,
                practicePresetRepository = DataStorePracticePresetRepository(preferences, { "test-preset" }, { 0L }),
                whatsNewCoordinator = WhatsNewCoordinator(store, versionProvider),
            )
            block(Fixture(store, viewModel))
        } finally {
            viewModel?.viewModelScope?.cancel()
            runCurrent()
            Dispatchers.resetMain()
        }
    }

    private data class Fixture(
        val store: ControlledOnboardingStore,
        val viewModel: MetronomeViewModel,
    )

    private class ControlledOnboardingStore(
        private val delegate: AppDatastore,
    ) : AppDatastore by delegate {
        var initialReadGate: CompletableDeferred<Unit>? = null
        var saveGate: CompletableDeferred<Unit>? = null
        var failCompletion = false
        var failInitialRead = false
        var completionWrites = 0

        override val onboardingComplete: Flow<Boolean> = flow {
            initialReadGate?.await()
            if (failInitialRead) error("read unavailable")
            emitAll(delegate.onboardingComplete)
        }

        override suspend fun saveOnboardingComplete(complete: Boolean) {
            completionWrites++
            if (failCompletion) error("storage unavailable")
            saveGate?.await()
            delegate.saveOnboardingComplete(complete)
        }
    }
}
