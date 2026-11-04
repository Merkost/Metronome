package com.merkost.metronome.model

val PHRASE_LENGTHS = listOf(0, 4, 8, 12, 16)

fun phraseBeat(beat: Beat, beatIndex: Int, barNumber: Int, phraseLength: Int): Beat {
    if (phraseLength <= 0 || beat == Beat.MUTE) return beat
    val firstBar = barNumber % phraseLength == 0
    return when {
        firstBar && beatIndex == 0 && beat == Beat.LOW -> Beat.HIGH
        !firstBar && beat == Beat.HIGH -> Beat.LOW
        else -> beat
    }
}

fun phraseBarLabel(currentBar: Int, phraseLength: Int, isPlaying: Boolean, countInRemaining: Int): String? {
    if (phraseLength <= 0 || !isPlaying || countInRemaining > 0) return null
    return "Bar ${currentBar % phraseLength + 1} of $phraseLength"
}
