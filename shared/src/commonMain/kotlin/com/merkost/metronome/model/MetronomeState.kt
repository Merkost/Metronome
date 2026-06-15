package com.merkost.metronome.model

import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val Int.interval: Int
    get() = (60_000.0 / this).roundToInt()

data class MetronomeState(
    val beats: List<Beat> = listOf(
        Beat.HIGH,
        Beat.LOW,
        Beat.LOW,
        Beat.LOW
    ),
    val playing: Boolean = false,
    var rhythm: Int = 80,
    var stopWatchState: StopWatchState = StopWatchState(),
    val timeSignature: TimeSignature = TimeSignature.FOUR_FOUR,
    val subdivision: Subdivision = Subdivision.QUARTER,
) {

    val interval: Int
        get() = rhythm.interval

    val beatDuration: Duration
        get() = (60_000.0 / rhythm).milliseconds

    fun updateRhythm(newRhythm: Int): MetronomeState {
        return this.copy(
            rhythm = newRhythm,
        )
    }

    val tempoName: String
        get() {
            return when (rhythm) {
                in 168..200 -> "Presto"
                in 120..168 -> "Allegro"
                in 108..120 -> "Moderato"
                in 76..108 -> "Andante"
                in 66..76 -> "Adagio"
                in 40..66 -> "Largo"
                else -> "Prestissimo"
            }
        }
}