package com.merkost.metronome.engine

import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.TimeSource

internal class BeatTimeline(
    private val timeSource: TimeSource.WithComparableMarks,
) {
    var deadline: ComparableTimeMark = timeSource.markNow()
        private set

    private var lastInterval: Duration = Duration.ZERO

    fun markBeat(interval: Duration) {
        lastInterval = interval
    }

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

    fun resumeAfterRestart() {
        if (deadline.elapsedNow() < Duration.ZERO) return
        val next = deadline + lastInterval
        if (lastInterval > Duration.ZERO && next.elapsedNow() < Duration.ZERO) deadline = next else reanchor()
    }
}
