package com.merkost.metronome.engine

import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.subClickOffsets
import com.merkost.metronome.platform.AudioFocusController
import com.merkost.metronome.platform.HapticProvider
import com.merkost.metronome.viewModels.MetronomeViewModel
import com.merkost.metronome.viewModels.repeat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.TimeSource

private const val SUB_CLICK_VOLUME = 0.35f

class MetronomeEngine(
    private val player: MetronomePlayer,
    private val viewModel: MetronomeViewModel,
    private val hapticProvider: HapticProvider,
    private val audioFocus: AudioFocusController,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var job: Job? = null
    private val timeSource = TimeSource.Monotonic

    private var beatCount = 0
    private var barNumber = 0

    fun start() {
        val initialSound = viewModel.selectedSound.value
        player.initialize(initialSound)
        audioFocus.setOnFocusLost { viewModel.onStopClicked() }
        job = coroutineScope.launch {
            launch {
                viewModel.selectedSound.collectLatest { sound ->
                    player.switchSound(sound)
                }
            }
            viewModel.isPlaying.collectLatest { playing ->
                if (playing) {
                    audioFocus.requestFocus()
                    var isRestart = false
                    viewModel.metronomeState
                        .map { it.beats.size }
                        .distinctUntilChanged()
                        .collectLatest { beatsCount ->
                            viewModel.index.update { -1 }
                            beatCount = 0
                            barNumber = 0
                            viewModel.onBarReset()
                            viewModel.onCountInTick(0)

                            var nextBeat = timeSource.markNow()

                            if (!isRestart && viewModel.countInEnabled.value) {
                                for (remaining in beatsCount downTo 1) {
                                    delayUntil(nextBeat)
                                    viewModel.onCountInTick(remaining)
                                    val stereo = viewModel.currentStereo.value
                                    val volume = viewModel.clickVolume.value
                                    player.play(Beat.HIGH, stereo.first * volume, stereo.second * volume)
                                    if (viewModel.hapticEnabled.value) {
                                        hapticProvider.playBeatHaptic(Beat.HIGH)
                                    }
                                    nextBeat += viewModel.metronomeState.value.beatDuration
                                }
                                viewModel.onCountInTick(0)
                            }
                            isRestart = true

                            createBeatsSequence(beatsCount).collect { index ->
                                delayUntil(nextBeat)
                                val beatStart = nextBeat
                                val state = viewModel.metronomeState.value
                                val beat = state.beats[index]
                                val stereo = viewModel.currentStereo.value
                                val volume = viewModel.clickVolume.value
                                val gapBar = (barNumber - viewModel.gapTrainerStartBar.value).coerceAtLeast(0)
                                val muted = viewModel.gapTrainerConfig.value?.isMuted(gapBar) == true
                                viewModel.index.update { index }
                                if (!muted && beat != Beat.MUTE) {
                                    player.play(beat, stereo.first * volume, stereo.second * volume)
                                    if (viewModel.hapticEnabled.value) {
                                        hapticProvider.playBeatHaptic(beat)
                                    }
                                }

                                val interval = state.beatDuration
                                val clicks = state.subdivision.clicksPerBeat
                                for (offset in subClickOffsets(interval, clicks)) {
                                    delayUntil(beatStart + offset)
                                    if (!muted) {
                                        player.play(
                                            Beat.LOW,
                                            stereo.first * SUB_CLICK_VOLUME * volume,
                                            stereo.second * SUB_CLICK_VOLUME * volume,
                                        )
                                    }
                                }

                                nextBeat = beatStart + interval
                                if (nextBeat.elapsedNow() > interval) {
                                    nextBeat = timeSource.markNow()
                                }

                                beatCount++
                                if (beatCount >= beatsCount) {
                                    beatCount = 0
                                    barNumber++
                                    viewModel.onBarCompleted(barNumber)
                                }
                            }
                        }
                } else {
                    audioFocus.abandonFocus()
                    player.stop()
                    viewModel.index.update { -1 }
                    viewModel.onCountInTick(0)
                }
            }
        }
    }

    private suspend fun delayUntil(target: TimeSource.Monotonic.ValueTimeMark) {
        val remaining = -target.elapsedNow()
        if (remaining > Duration.ZERO) delay(remaining)
    }

    fun stop() {
        job?.cancel()
        player.stop()
    }

    fun release() {
        stop()
        player.release()
    }

    private fun createBeatsSequence(beatsCount: Int): Flow<Int> =
        (0 until beatsCount).asSequence().repeat().asFlow()
}
