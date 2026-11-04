package com.merkost.metronome.viewModels

import androidx.lifecycle.viewModelScope
import com.merkost.metronome.model.AppDatastoreImpl
import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.GapTrainerConfig
import com.merkost.metronome.model.GradualTempoConfig
import com.merkost.metronome.model.TempoStepMode
import com.merkost.metronome.platform.AppVersionInfo
import com.merkost.metronome.platform.AppVersionProvider
import com.merkost.metronome.platform.HapticProvider
import com.merkost.metronome.presets.DataStorePracticePresetRepository
import com.merkost.metronome.presets.InMemoryPreferencesDataStore
import com.merkost.metronome.review.InAppReviewRequester
import com.merkost.metronome.review.ReviewPromptCoordinator
import com.merkost.metronome.review.ReviewPromptRecord
import com.merkost.metronome.review.ReviewPromptStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TempoTrainerStepModeTest {
    private fun trainer(mode: TempoStepMode, bars: Int = 4) =
        GradualTempoConfig(startBpm = 100, endBpm = 110, increment = 2, barsPerStep = bars, stepMode = mode)

    private fun Fixture.bpm() = viewModel.metronomeState.value.rhythm

    @Test
    fun gapCycleModeStepsAfterTheFirstReturningBar() = runTest {
        withFixture { fixture ->
            fixture.viewModel.startGapTrainer(GapTrainerConfig(2, 1))
            fixture.viewModel.startGradualTempo(trainer(TempoStepMode.GAP_CYCLE, 3))
            runCurrent()
            (1..3).forEach { fixture.viewModel.onBarCompleted(it) }
            assertEquals(100, fixture.bpm())
            fixture.viewModel.onBarCompleted(4)
            assertEquals(102, fixture.bpm())
            fixture.viewModel.onBarCompleted(5)
            fixture.viewModel.onBarCompleted(6)
            assertEquals(102, fixture.bpm())
            fixture.viewModel.onBarCompleted(7)
            assertEquals(104, fixture.bpm())
        }
    }

    @Test
    fun gapCycleModeIgnoresTheLeadInBar() = runTest {
        withFixture { fixture ->
            fixture.viewModel.startGradualTempo(trainer(TempoStepMode.GAP_CYCLE, 3))
            runCurrent()
            fixture.viewModel.onBarStarted(2)
            fixture.viewModel.startGapTrainer(GapTrainerConfig(2, 1))
            runCurrent()
            (3..6).forEach { fixture.viewModel.onBarCompleted(it) }
            assertEquals(100, fixture.bpm())
            fixture.viewModel.onBarCompleted(7)
            assertEquals(102, fixture.bpm())
        }
    }

    @Test
    fun liveGapEditRebasesTheStep() = runTest {
        withFixture { fixture ->
            fixture.viewModel.startGapTrainer(GapTrainerConfig(2, 1))
            fixture.viewModel.startGradualTempo(trainer(TempoStepMode.GAP_CYCLE, 3))
            runCurrent()
            fixture.viewModel.onBarStarted(3)
            fixture.viewModel.updateGapTrainer(GapTrainerConfig(1, 1))
            runCurrent()
            (4..6).forEach { fixture.viewModel.onBarCompleted(it) }
            assertEquals(100, fixture.bpm())
            fixture.viewModel.onBarCompleted(7)
            assertEquals(102, fixture.bpm())
        }
    }

    @Test
    fun gapCycleModeFallsBackToTheBarCountWhenTheGapTrainerStops() = runTest {
        withFixture { fixture ->
            fixture.viewModel.startGapTrainer(GapTrainerConfig(2, 1))
            fixture.viewModel.startGradualTempo(trainer(TempoStepMode.GAP_CYCLE, 2))
            runCurrent()
            fixture.viewModel.stopGapTrainer()
            fixture.viewModel.onBarCompleted(1)
            assertEquals(100, fixture.bpm())
            fixture.viewModel.onBarCompleted(2)
            assertEquals(102, fixture.bpm())
        }
    }

    @Test
    fun manualModeNeverStepsOnBars() = runTest {
        withFixture { fixture ->
            fixture.viewModel.startGradualTempo(trainer(TempoStepMode.MANUAL, 1))
            runCurrent()
            (1..20).forEach { fixture.viewModel.onBarCompleted(it) }
            assertEquals(100, fixture.bpm())
        }
    }

    @Test
    fun stepForwardAndBackMoveOneRungAndClamp() = runTest {
        withFixture { fixture ->
            fixture.viewModel.startGradualTempo(trainer(TempoStepMode.MANUAL))
            runCurrent()
            fixture.viewModel.stepTempoTrainerForward()
            assertEquals(102, fixture.bpm())
            fixture.viewModel.stepTempoTrainerBack()
            assertEquals(100, fixture.bpm())
            fixture.viewModel.stepTempoTrainerBack()
            assertEquals(100, fixture.bpm())
        }
    }

    @Test
    fun stepBackFromTheTargetLandsOnTheLastRung() = runTest {
        withFixture { fixture ->
            fixture.viewModel.startGradualTempo(
                GradualTempoConfig(116, 132, 5, 4, TempoStepMode.MANUAL),
            )
            runCurrent()
            repeat(4) { fixture.viewModel.stepTempoTrainerForward() }
            assertEquals(132, fixture.bpm())
            fixture.viewModel.stepTempoTrainerBack()
            assertEquals(131, fixture.bpm())
        }
    }

    @Test
    fun stepBackBelowTheStartDoesNothing() = runTest {
        withFixture { fixture ->
            fixture.viewModel.startGradualTempo(trainer(TempoStepMode.MANUAL))
            runCurrent()
            fixture.viewModel.onSliderValueChanged(90f)
            runCurrent()
            fixture.viewModel.stepTempoTrainerBack()
            assertEquals(90, fixture.bpm())
        }
    }

    @Test
    fun reachingTheTargetManuallyKeepsTheTrainer() = runTest {
        withFixture { fixture ->
            fixture.viewModel.startGradualTempo(trainer(TempoStepMode.MANUAL))
            runCurrent()
            repeat(5) { fixture.viewModel.stepTempoTrainerForward() }
            assertEquals(110, fixture.bpm())
            advanceTimeBy(6_000L)
            runCurrent()
            assertNotNull(fixture.viewModel.gradualTempoConfig.value)
        }
    }

    @Test
    fun reachingTheTargetPlaysTheConfirmHaptic() = runTest {
        withFixture { fixture ->
            fixture.viewModel.startGradualTempo(trainer(TempoStepMode.MANUAL))
            runCurrent()
            repeat(4) { fixture.viewModel.stepTempoTrainerForward() }
            assertEquals(0, fixture.haptics.confirms)
            fixture.viewModel.stepTempoTrainerForward()
            assertEquals(1, fixture.haptics.confirms)
            fixture.viewModel.stepTempoTrainerForward()
            assertEquals(1, fixture.haptics.confirms)
        }
    }

    @Test
    fun forwardAndBackAreNoOpsInBarsMode() = runTest {
        withFixture { fixture ->
            fixture.viewModel.startGradualTempo(trainer(TempoStepMode.BARS))
            runCurrent()
            fixture.viewModel.stepTempoTrainerForward()
            fixture.viewModel.stepTempoTrainerBack()
            assertEquals(100, fixture.bpm())
        }
    }

    @Test
    fun theStepModeSurvivesStartingTheTrainer() = runTest {
        withFixture { fixture ->
            fixture.viewModel.startGradualTempo(trainer(TempoStepMode.GAP_CYCLE, 3))
            runCurrent()
            assertEquals(TempoStepMode.GAP_CYCLE, fixture.viewModel.gradualTempoConfig.value?.stepMode)
            runCurrent()
            assertEquals(TempoStepMode.GAP_CYCLE, fixture.viewModel.lastTrainerConfig.value?.stepMode)
        }
    }

    private suspend fun TestScope.withFixture(block: suspend TestScope.(Fixture) -> Unit) {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var viewModel: MetronomeViewModel? = null
        try {
            val preferences = InMemoryPreferencesDataStore()
            val store = AppDatastoreImpl(preferences)
            store.saveOnboardingComplete(true)
            store.saveHapticEnabled(true)
            val haptics = CountingHaptics()
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
                hapticProvider = haptics,
                reviewPromptCoordinator = review,
                practicePresetRepository = DataStorePracticePresetRepository(preferences, { "preset" }, { 0L }),
                nowMillis = { testScheduler.currentTime },
            )
            runCurrent()
            block(Fixture(viewModel, haptics))
        } finally {
            viewModel?.viewModelScope?.cancel()
            runCurrent()
            Dispatchers.resetMain()
        }
    }

    private class Fixture(val viewModel: MetronomeViewModel, val haptics: CountingHaptics)

    private class CountingHaptics : HapticProvider {
        var confirms = 0
        override fun playBeatHaptic(beat: Beat) = Unit
        override fun playConfirmHaptic() { confirms++ }
    }
}
