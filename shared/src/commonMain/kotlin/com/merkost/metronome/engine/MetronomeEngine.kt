package com.merkost.metronome.engine

import com.merkost.metronome.model.Beat
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

private const val SUB_CLICK_VOLUME = 0.35f

class MetronomeEngine(
    private val player: MetronomePlayer,
    private val viewModel: MetronomeViewModel,
    private val hapticProvider: HapticProvider,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var job: Job? = null

    private var beatCount = 0
    private var barNumber = 0

    fun start() {
        val initialSound = viewModel.selectedSound.value
        player.initialize(initialSound)
        job = coroutineScope.launch {
            launch {
                viewModel.selectedSound.collectLatest { sound ->
                    player.switchSound(sound)
                }
            }
            viewModel.isPlaying.collectLatest { playing ->
                if (playing) {
                    var resumeWithDelay = false
                    viewModel.metronomeState
                        .map { it.beats.size }
                        .distinctUntilChanged()
                        .collectLatest { beatsCount ->
                            viewModel.index.update { -1 }
                            beatCount = 0
                            barNumber = 0
                            viewModel.onBarReset()
                            viewModel.onCountInTick(0)
                            if (resumeWithDelay) {
                                delay(viewModel.metronomeState.value.interval.toLong())
                            } else if (viewModel.countInEnabled.value) {
                                for (remaining in beatsCount + 1 downTo 1) {
                                    viewModel.onCountInTick(remaining)
                                    val stereo = viewModel.currentStereo.value
                                    player.play(Beat.HIGH, stereo.first, stereo.second)
                                    if (viewModel.hapticEnabled.value) {
                                        hapticProvider.playBeatHaptic(Beat.HIGH)
                                    }
                                    delay(viewModel.metronomeState.value.interval.toLong())
                                }
                                viewModel.onCountInTick(0)
                            }
                            resumeWithDelay = true
                            createBeatsSequence(beatsCount).collect { index ->
                                val state = viewModel.metronomeState.value
                                val beat = state.beats[index]
                                val stereo = viewModel.currentStereo.value
                                val gapBar = (barNumber - viewModel.gapTrainerStartBar.value).coerceAtLeast(0)
                                val muted = viewModel.gapTrainerConfig.value?.isMuted(gapBar) == true
                                viewModel.index.update { index }
                                if (!muted && beat != Beat.MUTE) {
                                    player.play(beat, stereo.first, stereo.second)
                                    if (viewModel.hapticEnabled.value) {
                                        hapticProvider.playBeatHaptic(beat)
                                    }
                                }
                                val interval = state.interval
                                val clicks = state.subdivision.clicksPerBeat
                                for (click in 1 until clicks) {
                                    delay((click * interval / clicks - (click - 1) * interval / clicks).toLong())
                                    if (!muted) {
                                        player.play(
                                            Beat.LOW,
                                            stereo.first * SUB_CLICK_VOLUME,
                                            stereo.second * SUB_CLICK_VOLUME,
                                        )
                                    }
                                }
                                delay((interval - (clicks - 1) * interval / clicks).toLong())
                                beatCount++
                                if (beatCount >= beatsCount) {
                                    beatCount = 0
                                    barNumber++
                                    viewModel.onBarCompleted(barNumber)
                                }
                            }
                        }
                } else {
                    player.stop()
                    viewModel.index.update { -1 }
                    viewModel.onCountInTick(0)
                }
            }
        }
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
