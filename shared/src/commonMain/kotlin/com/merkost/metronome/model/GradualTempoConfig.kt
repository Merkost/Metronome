package com.merkost.metronome.model

import kotlin.math.abs

data class GradualTempoConfig(
    val startBpm: Int,
    val endBpm: Int,
    val increment: Int = 2,
    val barsPerStep: Int = 4,
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

    fun isComplete(currentBpm: Int): Boolean {
        return if (ascending) currentBpm >= endBpm else currentBpm <= endBpm
    }
}
