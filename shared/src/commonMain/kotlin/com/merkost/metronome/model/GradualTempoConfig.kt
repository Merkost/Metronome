package com.merkost.metronome.model

import kotlin.math.abs

data class GradualTempoConfig(
    val startBpm: Int,
    val endBpm: Int,
    val increment: Int = 2,
    val barsPerStep: Int = 4,
    val stepMode: TempoStepMode = TempoStepMode.BARS,
) {
    val ascending: Boolean get() = endBpm >= startBpm
    val totalSteps: Int
        get() = ((abs(endBpm - startBpm) + increment - 1) / increment).coerceAtLeast(1)
    val totalBars: Int get() = totalSteps * barsPerStep

    fun progressFor(currentBpm: Int): Float {
        val span = endBpm - startBpm
        if (span == 0) return 1f
        return ((currentBpm - startBpm).toFloat() / span).coerceIn(0f, 1f)
    }

    fun nextBpmFrom(currentBpm: Int): Int {
        return if (ascending) {
            (currentBpm + increment).coerceAtMost(endBpm)
        } else {
            (currentBpm - increment).coerceAtLeast(endBpm)
        }
    }

    private fun travelledFrom(currentBpm: Int): Int =
        if (ascending) currentBpm - startBpm else startBpm - currentBpm

    fun stepsTaken(currentBpm: Int): Int {
        val travelled = travelledFrom(currentBpm)
        if (travelled <= 0) return 0
        return ((travelled + increment - 1) / increment).coerceAtMost(totalSteps)
    }

    fun previousBpmFrom(currentBpm: Int): Int {
        val taken = stepsTaken(currentBpm)
        if (taken == 0) return currentBpm
        val offset = increment * (taken - 1)
        return if (ascending) startBpm + offset else startBpm - offset
    }

    fun isComplete(currentBpm: Int): Boolean {
        return if (ascending) currentBpm >= endBpm else currentBpm <= endBpm
    }
}
