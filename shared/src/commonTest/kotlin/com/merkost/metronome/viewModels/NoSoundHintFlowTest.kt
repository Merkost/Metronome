package com.merkost.metronome.viewModels

import androidx.lifecycle.viewModelScope
import com.merkost.metronome.model.AppDatastoreImpl
import com.merkost.metronome.model.NoSoundHint
import com.merkost.metronome.platform.AppVersionInfo
import com.merkost.metronome.platform.AppVersionProvider
import com.merkost.metronome.platform.AudioOutputStatus
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
import kotlin.test.assertNull

class NoSoundHintFlowTest {
    @Test
    fun hintAppearsOnPlayAndClearsWithinASecondOfRaisingTheVolume() = runTest {
        val status = FakeStatus(mediaOff = true)
        withFixture(status) { viewModel ->
            assertNull(viewModel.noSoundHint.value)
            viewModel.onPlayPauseClicked(false)
            runCurrent()
            assertEquals(NoSoundHint.MEDIA_VOLUME_OFF, viewModel.noSoundHint.value)
            status.mediaOff = false
            advanceTimeBy(1_001)
            assertNull(viewModel.noSoundHint.value)
            status.mediaOff = true
            advanceTimeBy(1_001)
            assertEquals(NoSoundHint.MEDIA_VOLUME_OFF, viewModel.noSoundHint.value)
        }
    }

    @Test
    fun zeroClickVolumeIsNamedWhenMediaIsAudible() = runTest {
        withFixture(FakeStatus(mediaOff = false), clickVolume = 0f) { viewModel ->
            viewModel.onPlayPauseClicked(false)
            runCurrent()
            assertEquals(NoSoundHint.CLICK_VOLUME_OFF, viewModel.noSoundHint.value)
        }
    }

    @Test
    fun unknownStatusNeverPolls() = runTest {
        withFixture(null) { viewModel ->
            viewModel.onPlayPauseClicked(false)
            advanceTimeBy(3_000)
            assertNull(viewModel.noSoundHint.value)
        }
    }

    @Test
    fun hintClearsOnPause() = runTest {
        val status = FakeStatus(mediaOff = true)
        withFixture(status) { viewModel ->
            viewModel.onPlayPauseClicked(false)
            runCurrent()
            assertEquals(NoSoundHint.MEDIA_VOLUME_OFF, viewModel.noSoundHint.value)
            viewModel.onPlayPauseClicked(true)
            runCurrent()
            assertNull(viewModel.noSoundHint.value)
            val before = status.reads
            advanceTimeBy(3_000)
            assertEquals(before, status.reads)
        }
    }

    @Test
    fun showVolumeControlsReachesThePlatform() = runTest {
        val status = FakeStatus(mediaOff = true)
        withFixture(status) { viewModel ->
            viewModel.showVolumeControls()
            assertEquals(1, status.shown)
        }
    }

    @Test
    fun audioFailureWhilePlayingStopsPlaybackAndRaisesTheUnavailableState() = runTest {
        withFixture(null) { viewModel ->
            viewModel.onPlayPauseClicked(false)
            runCurrent()
            viewModel.onAudioUnavailable()
            runCurrent()
            assertEquals(false, viewModel.metronomeState.value.playing)
            assertEquals(true, viewModel.audioUnavailable.value)
            viewModel.onPlayPauseClicked(false)
            runCurrent()
            assertEquals(false, viewModel.audioUnavailable.value)
            assertEquals(true, viewModel.metronomeState.value.playing)
        }
    }

    @Test
    fun audioFailureWhilePausedIsIgnored() = runTest {
        withFixture(null) { viewModel ->
            viewModel.onAudioUnavailable()
            runCurrent()
            assertEquals(false, viewModel.audioUnavailable.value)
        }
    }

    @Test
    fun dismissingTheUnavailableNoticeClearsIt() = runTest {
        withFixture(null) { viewModel ->
            viewModel.onPlayPauseClicked(false)
            runCurrent()
            viewModel.onAudioUnavailable()
            viewModel.dismissAudioUnavailable()
            assertEquals(false, viewModel.audioUnavailable.value)
        }
    }

    private class FakeStatus(var mediaOff: Boolean) : AudioOutputStatus {
        var reads = 0
        var shown = 0
        override fun isMediaVolumeOff(): Boolean {
            reads++
            return mediaOff
        }
        override val canShowVolumeControls = true
        override fun showVolumeControls() {
            shown++
        }
    }

    private suspend fun TestScope.withFixture(
        status: AudioOutputStatus?,
        clickVolume: Float = 1f,
        block: suspend TestScope.(MetronomeViewModel) -> Unit,
    ) {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var viewModel: MetronomeViewModel? = null
        try {
            val preferences = InMemoryPreferencesDataStore()
            val store = AppDatastoreImpl(preferences)
            store.saveOnboardingComplete(true)
            store.saveClickVolume(clickVolume)
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
            val haptics = object : HapticProvider {
                override fun playBeatHaptic(beat: com.merkost.metronome.model.Beat) = Unit
                override fun playConfirmHaptic() = Unit
            }
            viewModel = if (status != null) {
                MetronomeViewModel(
                    appDatastore = store,
                    hapticProvider = haptics,
                    reviewPromptCoordinator = review,
                    practicePresetRepository = DataStorePracticePresetRepository(preferences, { "preset" }, { 0L }),
                    nowMillis = { testScheduler.currentTime },
                    audioOutputStatus = status,
                )
            } else {
                MetronomeViewModel(
                    appDatastore = store,
                    hapticProvider = haptics,
                    reviewPromptCoordinator = review,
                    practicePresetRepository = DataStorePracticePresetRepository(preferences, { "preset" }, { 0L }),
                    nowMillis = { testScheduler.currentTime },
                )
            }
            runCurrent()
            block(viewModel)
        } finally {
            viewModel?.viewModelScope?.cancel()
            runCurrent()
            Dispatchers.resetMain()
        }
    }
}
