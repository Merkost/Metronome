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

    fun start() {
        player.initialize()
        job = coroutineScope.launch {
            launch {
                viewModel.selectedSound.collectLatest { sound ->
                    player.switchSound(sound)
                }
            }
            viewModel.isPlaying.collectLatest { playing ->
                if (playing) {
                    viewModel.index.update { -1 }
                    var interval = viewModel.metronomeState.value.interval.toLong()
                    createBeatsSequence(viewModel.metronomeState.value.beats)
                        .onEach { delay(interval) }
                        .collectLatest { index ->
                            val beat = viewModel.metronomeState.value.beats[index]
                            viewModel.index.update { index }
                            interval = viewModel.metronomeState.value.interval.toLong()
                            val stereo = viewModel.currentStereo.value
                            player.play(beat, stereo.first.toFloat(), stereo.second.toFloat())
                            if (viewModel.hapticEnabled.value) {
                                hapticProvider.playBeatHaptic(beat)
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

    private fun createBeatsSequence(beats: List<Beat>): Flow<Int> =
        beats.indices.asSequence().repeat().asFlow()
}
