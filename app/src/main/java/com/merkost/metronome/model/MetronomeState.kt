package com.merkost.metronome.model

data class MetronomeState(
    val playing: Boolean = false,
    var rhythm: Int = 180,
    var interval: Int = 60000 / rhythm,
    var stopWatchState: StopWatchState = StopWatchState(),
) {
    fun updateRhythm(newRhythm: Int): MetronomeState {
        this.rhythm = newRhythm
        this.interval = 60000 / rhythm
        return this
    }
}