package com.merkost.metronome

import com.merkost.metronome.viewModels.MetronomeViewModel
import org.koin.mp.KoinPlatform

fun togglePlayback() {
    val viewModel = KoinPlatform.getKoin().get<MetronomeViewModel>()
    viewModel.onPlayPauseClicked(viewModel.isPlaying.value)
}
