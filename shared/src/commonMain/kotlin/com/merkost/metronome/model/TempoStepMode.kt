package com.merkost.metronome.model

enum class TempoStepMode {
    BARS,
    GAP_CYCLE,
    MANUAL;

    companion object {
        fun fromStorageKey(key: String?): TempoStepMode = entries.firstOrNull { it.name == key } ?: BARS
    }
}

fun gapCycleStepDue(completedBars: Int, gapStartBar: Int, cycleBars: Int): Boolean {
    val barsIntoGap = completedBars - gapStartBar
    return cycleBars > 0 && barsIntoGap > cycleBars && (barsIntoGap - 1) % cycleBars == 0
}
