package com.merkost.metronome.model

data class GapStripState(
    val title: String,
    val caption: String,
    val progress: Float,
    val muted: Boolean,
    val description: String,
)

fun gapStripState(
    config: GapTrainerConfig,
    currentBar: Int,
    startBar: Int,
    muted: Boolean,
    isPlaying: Boolean,
    countInRemaining: Int,
): GapStripState {
    val barsCaption = "${config.playBars} + ${config.muteBars} bars"
    if (!isPlaying) {
        return GapStripState("Gap trainer", "paused", 0f, false, "Gap trainer, paused")
    }
    if (countInRemaining > 0) {
        return GapStripState(
            title = "Count-in · $countInRemaining",
            caption = barsCaption,
            progress = 0f,
            muted = false,
            description = "Gap trainer, count-in $countInRemaining",
        )
    }
    if (currentBar < startBar) {
        return GapStripState(
            title = if (muted) "Muted · new cycle next bar" else "Click · new cycle next bar",
            caption = barsCaption,
            progress = 0f,
            muted = muted,
            description = "Gap trainer, new cycle next bar",
        )
    }
    val gapBar = currentBar - startBar
    val inPhase = config.barInPhase(gapBar)
    val phaseLength = config.phaseLength(gapBar)
    return GapStripState(
        title = if (muted) "Muted · bar $inPhase/$phaseLength" else "Click · bar $inPhase/$phaseLength",
        caption = barsCaption,
        progress = config.cycleProgress(gapBar),
        muted = muted,
        description = if (muted) {
            "Gap trainer, silent bar $inPhase of $phaseLength"
        } else {
            "Gap trainer, playing bar $inPhase of $phaseLength"
        },
    )
}

fun gapClickResting(
    gapActive: Boolean,
    currentBar: Int,
    startBar: Int,
    muted: Boolean,
    isPlaying: Boolean,
    countInRemaining: Int,
): Boolean = gapActive && isPlaying && countInRemaining == 0 && currentBar >= startBar && muted
