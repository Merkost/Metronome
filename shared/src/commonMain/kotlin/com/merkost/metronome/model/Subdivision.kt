package com.merkost.metronome.model

enum class Subdivision(val clicksPerBeat: Int, val label: String) {
    QUARTER(1, "Quarter"),
    EIGHTH(2, "Eighths"),
    TRIPLET(3, "Triplets"),
    SIXTEENTH(4, "16ths");
}
