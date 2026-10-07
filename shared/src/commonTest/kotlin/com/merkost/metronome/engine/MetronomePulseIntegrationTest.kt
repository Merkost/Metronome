package com.merkost.metronome.engine

import androidx.lifecycle.viewModelScope
import com.merkost.metronome.model.AppDatastoreImpl
import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.ClickSound
import com.merkost.metronome.model.GapTrainerConfig
import com.merkost.metronome.model.Subdivision
import com.merkost.metronome.model.TimeSignature
import com.merkost.metronome.platform.AppVersionInfo
import com.merkost.metronome.platform.AppVersionProvider
import com.merkost.metronome.platform.HapticProvider
import com.merkost.metronome.platform.NoopAudioFocusController
import com.merkost.metronome.presets.DataStorePracticePresetRepository
import com.merkost.metronome.presets.InMemoryPreferencesDataStore
import com.merkost.metronome.review.InAppReviewRequester
import com.merkost.metronome.review.ReviewPromptCoordinator
import com.merkost.metronome.review.ReviewPromptRecord
import com.merkost.metronome.review.ReviewPromptStore
import com.merkost.metronome.viewModels.MetronomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource
import kotlin.time.TimeSource

@OptIn(ExperimentalCoroutinesApi::class)
class MetronomePulseIntegrationTest {
    @Test
    fun deferredAudioSubmissionStartsAtCentreAndOlderCallbacksCannotRewindIt() = runTest {
        fixture(deferred = true) { state ->
            state.viewModel.onPlayPauseClicked(false)
            runCurrent()
            assertNull(state.viewModel.beatClock.value.pulse)
            assertEquals(1, state.player.pending.size)
            step(state, 500)
            assertEquals(2, state.player.pending.size)
            state.player.submit(1)
            val latest = assertNotNull(state.viewModel.beatClock.value.pulse)
            assertEquals(1L, latest.ordinal)
            assertEquals(Duration.ZERO, latest.startedAt.elapsedNow())
            assertEquals(0f, latest.pendulumDisplacement())
            step(state, 100)
            state.player.submit(0)
            assertEquals(latest, state.viewModel.beatClock.value.pulse)
        }
    }

    @Test
    fun cancellationDuringTimestampCaptureCannotRestoreAPreviousPlaybackSession() = runTest {
        fixture(deferred = true) { state ->
            state.viewModel.onPlayPauseClicked(false)
            runCurrent()
            val oldGeneration = state.viewModel.beatClock.value.generation
            state.source.beforeMark = {
                state.engine.stop()
                state.engine.start()
            }
            state.player.submit(0)
            assertNull(state.viewModel.beatClock.value.pulse, "A callback that passed its active check before cancellation must still be fenced at publication")
            runCurrent()
            assertTrue(state.viewModel.beatClock.value.generation > oldGeneration)
            state.player.submit(1)
            val current = assertNotNull(state.viewModel.beatClock.value.pulse)
            assertEquals(0L, current.ordinal)
            assertEquals(0f, current.pendulumDisplacement())
        }
    }

    @Test
    fun countInFlowsIntoOddBarsWithoutResettingTheSwingDirection() = runTest {
        fixture(countIn = true) { state ->
            state.viewModel.onTimeSignatureChanged(TimeSignature.THREE_FOUR)
            state.viewModel.onPlayPauseClicked(false)
            runCurrent()
            val ordinals = mutableListOf<Long>()
            repeat(7) { index ->
                if (index > 0) step(state, 500)
                val pulse = assertNotNull(state.viewModel.beatClock.value.pulse)
                ordinals += pulse.ordinal
                assertEquals(0f, pulse.pendulumDisplacement())
            }
            assertEquals(listOf(0L, 1L, 2L, 3L, 4L, 5L, 6L), ordinals)
            assertEquals(listOf(Beat.HIGH, Beat.HIGH, Beat.HIGH, Beat.HIGH, Beat.LOW, Beat.LOW, Beat.HIGH), state.player.beats)
            assertEquals(0, state.viewModel.index.value)
            state.viewModel.onStopClicked()
            runCurrent()
            assertNull(state.viewModel.beatClock.value.pulse)
        }
    }

    @Test
    fun silentGapBarsRetainScheduledPhaseAndSubdivisionsDoNotRetargetTheMainSwing() = runTest {
        fixture { state ->
            state.viewModel.onTimeSignatureChanged(TimeSignature.TWO_FOUR)
            state.viewModel.startGapTrainer(GapTrainerConfig(1, 1))
            runCurrent()
            step(state, 500)
            step(state, 500)
            val silence = assertNotNull(state.viewModel.beatClock.value.pulse)
            assertEquals(2L, silence.ordinal)
            assertEquals(2, state.player.beats.size)
            assertEquals(0f, silence.pendulumDisplacement())
            step(state, 250)
            assertEquals(1f, silence.pendulumDisplacement(), 0.00001f)
            step(state, 250)
            step(state, 500)
            assertEquals(4L, assertNotNull(state.viewModel.beatClock.value.pulse).ordinal)
            assertEquals(3, state.player.beats.size)
            state.viewModel.stopGapTrainer()
            state.viewModel.onSubdivisionChanged(Subdivision.EIGHTH)
            runCurrent()
            step(state, 500)
            val primary = assertNotNull(state.viewModel.beatClock.value.pulse)
            val submitted = state.player.beats.size
            step(state, 250)
            assertEquals(submitted + 1, state.player.beats.size)
            assertEquals(primary, state.viewModel.beatClock.value.pulse)
        }
    }

    @Test
    fun stalePrimariesAreSkippedAndTheNextSubmittedClickHasFreshCentreTiming() = runTest {
        fixture(haptics = true) { state ->
            state.haptic.onBeat = {
                state.haptic.onBeat = null
                state.source.time += 1000.milliseconds
            }
            state.viewModel.onPlayPauseClicked(false)
            runCurrent()
            assertEquals(listOf(Beat.LOW), state.player.beats)
            val pulse = assertNotNull(state.viewModel.beatClock.value.pulse)
            assertEquals(1L, pulse.ordinal)
            assertEquals(Duration.ZERO, pulse.startedAt.elapsedNow())
            assertEquals(0f, pulse.pendulumDisplacement())
        }
    }

    @Test
    fun pauseResumeAndMeterRestartFencePreviouslyQueuedSubmissions() = runTest {
        fixture(deferred = true) { state ->
            state.viewModel.onPlayPauseClicked(false)
            runCurrent()
            state.viewModel.onStopClicked()
            runCurrent()
            assertNull(state.viewModel.beatClock.value.pulse)
            state.viewModel.onPlayPauseClicked(false)
            runCurrent()
            state.player.submit(0)
            assertNull(state.viewModel.beatClock.value.pulse)
            state.player.submit(1)
            assertEquals(0L, assertNotNull(state.viewModel.beatClock.value.pulse).ordinal)
            val beforeMeter = state.viewModel.beatClock.value.generation
            step(state, 500)
            state.viewModel.onTimeSignatureChanged(TimeSignature.THREE_FOUR)
            runCurrent()
            assertTrue(state.viewModel.beatClock.value.generation > beforeMeter)
            assertNull(state.viewModel.beatClock.value.pulse)
            state.player.submit(2)
            assertNull(state.viewModel.beatClock.value.pulse)
            state.player.submit(3)
            assertEquals(2L, assertNotNull(state.viewModel.beatClock.value.pulse).ordinal)
        }
    }

    @Test
    fun aLiveTempoChangeUsesTheNextBeatsExactCadenceWithoutRetargetingThisBeat() = runTest {
        fixture { state ->
            state.viewModel.onPlayPauseClicked(false)
            runCurrent()
            val first = assertNotNull(state.viewModel.beatClock.value.pulse)
            step(state, 250)
            state.viewModel.onSliderValueChanged(60f)
            runCurrent()
            assertEquals(first, state.viewModel.beatClock.value.pulse)
            step(state, 250)
            val slower = assertNotNull(state.viewModel.beatClock.value.pulse)
            assertEquals(1L, slower.ordinal)
            assertEquals(1000.milliseconds, slower.interval)
            assertEquals(0f, slower.pendulumDisplacement())
            step(state, 500)
            assertEquals(slower, state.viewModel.beatClock.value.pulse)
            assertEquals(-1f, slower.pendulumDisplacement(), 0.00001f)
            step(state, 500)
            assertEquals(2L, assertNotNull(state.viewModel.beatClock.value.pulse).ordinal)
            assertEquals(3, state.player.beats.size)
        }
    }

    @Test
    fun mutedPrimaryBeatsKeepThePendulumClockWithoutSubmittingSound() = runTest {
        fixture { state ->
            state.viewModel.onBallClicked(0, Beat.HIGH)
            state.viewModel.onBallClicked(0, Beat.LOW)
            state.viewModel.onPlayPauseClicked(false)
            runCurrent()
            assertTrue(state.player.beats.isEmpty())
            val silent = assertNotNull(state.viewModel.beatClock.value.pulse)
            assertEquals(0L, silent.ordinal)
            step(state, 250)
            assertEquals(1f, silent.pendulumDisplacement(), 0.00001f)
            step(state, 250)
            assertEquals(listOf(Beat.LOW), state.player.beats)
            assertEquals(1L, assertNotNull(state.viewModel.beatClock.value.pulse).ordinal)
        }
    }

    private fun TestScope.step(state: Fixture, millis: Long) {
        state.source.time += millis.milliseconds
        advanceTimeBy(millis)
        runCurrent()
    }

    private suspend fun TestScope.fixture(deferred: Boolean = false, countIn: Boolean = false, haptics: Boolean = false, block: suspend TestScope.(Fixture) -> Unit) {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val preferences = InMemoryPreferencesDataStore()
        val store = AppDatastoreImpl(preferences)
        store.saveOnboardingComplete(true)
        store.saveCountInEnabled(countIn)
        store.saveHapticEnabled(haptics)
        val haptic = TestHaptics()
        val source = InterceptedClock()
        val player = SubmissionPlayer(deferred)
        val version = object : AppVersionProvider {
            override fun getAppVersion() = AppVersionInfo("1.4.0", 10L)
        }
        val review = ReviewPromptCoordinator(
            object : ReviewPromptStore {
                override suspend fun read() = ReviewPromptRecord()
                override suspend fun markRequested(version: String, atMillis: Long) = Unit
            },
            object : InAppReviewRequester { override fun requestReview() = false },
            version,
            nowMillis = { 0L },
        )
        val viewModel = MetronomeViewModel(store, haptic, review, DataStorePracticePresetRepository(preferences, { "pulse-test" }, { 0L }))
        val engine = MetronomeEngine(player, viewModel, haptic, NoopAudioFocusController(), coroutineScope = backgroundScope, timeSource = source)
        try {
            runCurrent()
            viewModel.onSliderValueChanged(120f)
            runCurrent()
            engine.start()
            runCurrent()
            block(Fixture(viewModel, engine, source, player, haptic))
        } finally {
            engine.release()
            viewModel.viewModelScope.cancel()
            runCurrent()
            Dispatchers.resetMain()
        }
    }

    private data class Fixture(val viewModel: MetronomeViewModel, val engine: MetronomeEngine, val source: InterceptedClock, val player: SubmissionPlayer, val haptic: TestHaptics)

    private class InterceptedClock : TimeSource.WithComparableMarks {
        val time = TestTimeSource()
        var beforeMark: (() -> Unit)? = null
        override fun markNow(): ComparableTimeMark {
            val hook = beforeMark
            beforeMark = null
            hook?.invoke()
            return time.markNow()
        }
    }

    private class SubmissionPlayer(private val deferred: Boolean) : MetronomePlayer {
        val beats = mutableListOf<Beat>()
        val pending = mutableListOf<() -> Unit>()
        override fun initialize(initialSound: ClickSound) = Unit
        override fun play(beat: Beat, stereoLeft: Float, stereoRight: Float) { beats += beat }
        override fun play(beat: Beat, stereoLeft: Float, stereoRight: Float, onSubmitted: () -> Unit) {
            if (deferred) pending += { beats += beat; onSubmitted() } else { beats += beat; onSubmitted() }
        }
        fun submit(index: Int) = pending[index]()
        override fun stop() = Unit
        override fun release() = Unit
        override fun switchSound(sound: ClickSound) = Unit
    }

    private class TestHaptics : HapticProvider {
        var onBeat: (() -> Unit)? = null
        override fun playBeatHaptic(beat: Beat) { onBeat?.invoke() }
        override fun playConfirmHaptic() = Unit
    }
}
