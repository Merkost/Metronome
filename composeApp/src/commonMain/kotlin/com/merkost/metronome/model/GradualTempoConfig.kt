package com.merkost.metronome.model

data class GradualTempoConfig(
    val startBpm: Int,
    val endBpm: Int,
    val increment: Int = 2,
    val barsPerStep: Int = 4,
) {
    val totalSteps: Int get() = ((endBpm - startBpm) / increment).coerceAtLeast(1)
    val totalBars: Int get() = totalSteps * barsPerStep
}
