package com.merkost.metronome.model

enum class TimeSignature(val label: String, val defaultBeats: List<Beat>) {
    TWO_FOUR("2/4", listOf(Beat.HIGH, Beat.LOW)),
    THREE_FOUR("3/4", listOf(Beat.HIGH, Beat.LOW, Beat.LOW)),
    FOUR_FOUR("4/4", listOf(Beat.HIGH, Beat.LOW, Beat.LOW, Beat.LOW)),
    FIVE_FOUR("5/4", listOf(Beat.HIGH, Beat.LOW, Beat.LOW, Beat.HIGH, Beat.LOW)),
    SIX_EIGHT("6/8", listOf(Beat.HIGH, Beat.LOW, Beat.LOW, Beat.HIGH, Beat.LOW, Beat.LOW)),
    SEVEN_EIGHT("7/8", listOf(Beat.HIGH, Beat.LOW, Beat.LOW, Beat.HIGH, Beat.LOW, Beat.LOW, Beat.HIGH));
}
