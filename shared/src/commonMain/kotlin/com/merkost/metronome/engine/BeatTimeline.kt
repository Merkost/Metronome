package com.merkost.metronome.engine

import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.TimeSource

internal class BeatTimeline(
    private val timeSource: TimeSource.WithComparableMarks,
) {
    var deadline: ComparableTimeMark = timeSource.markNow()
        private set

    fun advance(interval: Duration) {
        require(interval > Duration.ZERO)
        val scheduled = deadline + interval
        deadline = if (scheduled.elapsedNow() >= interval) timeSource.markNow() else scheduled
    }

    fun isStale(eventDeadline: ComparableTimeMark, interval: Duration): Boolean =
        eventDeadline.elapsedNow() >= interval

    fun reanchor() {
        deadline = timeSource.markNow()
    }
}
