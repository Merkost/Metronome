package com.merkost.metronome.model

import kotlin.math.roundToInt
import kotlin.time.Duration

fun subClickOffsets(interval: Duration, clicksPerBeat: Int): List<Duration> =
    (1 until clicksPerBeat).map { interval * it / clicksPerBeat }

fun bpmFromTapIntervals(intervalsMillis: List<Long>): Int? {
    if (intervalsMillis.isEmpty()) return null
    val sorted = intervalsMillis.sorted()
    val mid = sorted.size / 2
    val median = if (sorted.size % 2 == 1) {
        sorted[mid].toDouble()
    } else {
        (sorted[mid - 1] + sorted[mid]) / 2.0
    }
    if (median <= 0.0) return null
    return (60_000.0 / median).roundToInt().coerceIn(MIN_BPM, MAX_BPM)
}
