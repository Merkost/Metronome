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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MetronomeEngine(
    private val player: MetronomePlayer,
    private val viewModel: MetronomeViewModel,
    private val hapticProvider: HapticProvider,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var job: Job? = null

    private val _barCompleted = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val barCompleted: SharedFlow<Int> = _barCompleted
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
                    // React to beat-list changes (time signature) while playing
                    viewModel.metronomeState
                        .map { it.beats.size }
                        .distinctUntilChanged()
                        .collectLatest { beatsCount ->
                            viewModel.index.update { -1 }
                            beatCount = 0
                            barNumber = 0
                            var interval = viewModel.metronomeState.value.interval.toLong()
                            createBeatsSequence(beatsCount)
                                .onEach { delay(interval) }
                                .collectLatest { index ->
                                    val beat = viewModel.metronomeState.value.beats[index]
                                    viewModel.index.update { index }
                                    interval =
                                        viewModel.metronomeState.value.interval.toLong()
                                    val stereo = viewModel.currentStereo.value
                                    player.play(beat, stereo.first, stereo.second)
                                    if (viewModel.hapticEnabled.value) {
                                        hapticProvider.playBeatHaptic(beat)
                                    }
                                    // Bar counting for gradual tempo
                                    beatCount++
                                    val beatsPerBar = beatsCount
                                    if (beatCount >= beatsPerBar) {
                                        beatCount = 0
                                        barNumber++
                                        _barCompleted.tryEmit(barNumber)
                                        val config = viewModel.gradualTempoConfig.value
                                        if (config != null) {
                                            viewModel.incrementGradualTempo()
                                        }
                                    }
                                }
                        }
                } else {
                    viewModel.index.update { -1 }
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
