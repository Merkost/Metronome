package com.merkost.metronome.model

import kotlin.math.PI
import kotlin.math.sin
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration

data class BeatPulse(
    val startedAt: ComparableTimeMark,
    val interval: Duration,
    val ordinal: Long,
) {
    init {
        require(interval > Duration.ZERO && interval.isFinite())
        require(ordinal >= 0)
    }

    fun pendulumDisplacement(elapsed: Duration = startedAt.elapsedNow()): Float {
        if (elapsed <= Duration.ZERO) return 0f
        val phase = (elapsed / interval) % 2.0
        val direction = if (ordinal % 2L == 0L) 1.0 else -1.0
        return (direction * sin(PI * phase)).toFloat()
    }
}

data class BeatClockState(val generation: Long = 0L, val pulse: BeatPulse? = null)
