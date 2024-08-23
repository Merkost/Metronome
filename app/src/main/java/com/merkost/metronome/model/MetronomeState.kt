package com.merkost.metronome.model

private val Int.interval: Int
    get() = 60000 / this

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
) {

    val interval: Int
        get() = rhythm.interval

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