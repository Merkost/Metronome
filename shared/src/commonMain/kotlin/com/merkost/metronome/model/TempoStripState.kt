package com.merkost.metronome.model

data class TempoStripState(
    val title: String,
    val caption: String,
    val progress: Float,
    val description: String,
)

private fun stepsText(config: GradualTempoConfig, bpm: Int): String =
    "${config.stepsTaken(bpm)} of ${config.totalSteps} steps"

private fun countsSteps(config: GradualTempoConfig, gapRunning: Boolean): Boolean =
    config.stepMode == TempoStepMode.MANUAL || (config.stepMode == TempoStepMode.GAP_CYCLE && gapRunning)

fun tempoTrainerStripState(
    config: GradualTempoConfig,
    bpm: Int,
    trainerBar: Int,
    isPlaying: Boolean,
    gapRunning: Boolean,
): TempoStripState {
    val complete = config.isComplete(bpm)
    val barText = "bar ${trainerBar.coerceAtMost(config.totalBars)}"
    val title = if (complete) "${config.endBpm} BPM reached" else "$bpm → ${config.endBpm}"
    val caption = when {
        complete -> ""
        !isPlaying -> "paused"
        countsSteps(config, gapRunning) -> stepsText(config, bpm)
        else -> "$barText / ${config.totalBars}"
    }
    val detail = if (countsSteps(config, gapRunning)) stepsText(config, bpm) else "$barText of ${config.totalBars}"
    val description = if (complete) {
        "Tempo trainer, target ${config.endBpm} BPM reached"
    } else {
        "Tempo trainer, $bpm BPM, target ${config.endBpm}, $detail"
    }
    return TempoStripState(title, caption, config.progressFor(bpm), description)
}

fun trainerRuleCaption(config: GradualTempoConfig): String {
    val direction = if (config.ascending) "+" else "−"
    val step = "$direction${config.increment}"
    val rule = when (config.stepMode) {
        TempoStepMode.BARS -> "$step every ${config.barsPerStep} bars"
        TempoStepMode.GAP_CYCLE -> "$step after each gap cycle"
        TempoStepMode.MANUAL -> "$step when you tap"
    }
    return "${config.startBpm} → ${config.endBpm} · $rule"
}

fun trainerProgressCaption(
    config: GradualTempoConfig,
    bpm: Int,
    trainerBar: Int,
    isPlaying: Boolean,
    gapRunning: Boolean,
): String {
    val bars = "Bar ${trainerBar.coerceAtMost(config.totalBars)} of ${config.totalBars}"
    return when {
        config.isComplete(bpm) -> "Target reached"
        config.stepMode == TempoStepMode.MANUAL -> stepsText(config, bpm)
        !isPlaying -> "Paused — advances while playing"
        config.stepMode == TempoStepMode.GAP_CYCLE && gapRunning -> stepsText(config, bpm)
        config.stepMode == TempoStepMode.GAP_CYCLE -> "Gap trainer off · ${bars.replaceFirstChar { it.lowercase() }}"
        else -> bars
    }
}

fun trainerSummaryFor(config: GradualTempoConfig, beatsPerBar: Int): String {
    val steps = if (config.totalSteps == 1) "1 step" else "${config.totalSteps} steps"
    return when (config.stepMode) {
        TempoStepMode.BARS -> {
            val averageBpm = (config.startBpm + config.endBpm) / 2f
            val minutes = (config.totalBars * beatsPerBar / averageBpm).toInt().coerceAtLeast(1)
            "${config.totalBars} bars · ≈ $minutes min"
        }
        TempoStepMode.GAP_CYCLE -> "$steps · one after each gap cycle"
        TempoStepMode.MANUAL -> "$steps · you choose when"
    }
}
