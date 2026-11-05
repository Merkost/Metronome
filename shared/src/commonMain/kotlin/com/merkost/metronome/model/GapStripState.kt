package com.merkost.metronome.model

import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.gap_bars_caption
import metronome.shared.generated.resources.gap_clicking_bar
import metronome.shared.generated.resources.gap_clicking_new_cycle
import metronome.shared.generated.resources.gap_count_in
import metronome.shared.generated.resources.gap_description_count_in
import metronome.shared.generated.resources.gap_description_new_cycle
import metronome.shared.generated.resources.gap_description_paused
import metronome.shared.generated.resources.gap_description_playing_bar
import metronome.shared.generated.resources.gap_description_silent_bar
import metronome.shared.generated.resources.gap_muted_bar
import metronome.shared.generated.resources.gap_muted_new_cycle
import metronome.shared.generated.resources.gap_paused
import metronome.shared.generated.resources.gap_title

data class GapStripState(
    val title: UiText,
    val caption: UiText,
    val progress: Float,
    val muted: Boolean,
    val description: UiText,
)

fun gapStripState(
    config: GapTrainerConfig,
    currentBar: Int,
    startBar: Int,
    muted: Boolean,
    isPlaying: Boolean,
    countInRemaining: Int,
): GapStripState {
    val barsCaption = UiText.quantity(Res.plurals.gap_bars_caption, config.muteBars, config.playBars, config.muteBars)
    if (!isPlaying) {
        return GapStripState(
            title = UiText.of(Res.string.gap_title),
            caption = UiText.of(Res.string.gap_paused),
            progress = 0f,
            muted = false,
            description = UiText.of(Res.string.gap_description_paused),
        )
    }
    if (countInRemaining > 0) {
        return GapStripState(
            title = UiText.of(Res.string.gap_count_in, countInRemaining),
            caption = barsCaption,
            progress = 0f,
            muted = false,
            description = UiText.of(Res.string.gap_description_count_in, countInRemaining),
        )
    }
    if (currentBar < startBar) {
        return GapStripState(
            title = if (muted) UiText.of(Res.string.gap_muted_new_cycle) else UiText.of(Res.string.gap_clicking_new_cycle),
            caption = barsCaption,
            progress = 0f,
            muted = muted,
            description = UiText.of(Res.string.gap_description_new_cycle),
        )
    }
    val gapBar = currentBar - startBar
    val inPhase = config.barInPhase(gapBar)
    val phaseLength = config.phaseLength(gapBar)
    return GapStripState(
        title = if (muted) {
            UiText.of(Res.string.gap_muted_bar, inPhase, phaseLength)
        } else {
            UiText.of(Res.string.gap_clicking_bar, inPhase, phaseLength)
        },
        caption = barsCaption,
        progress = config.cycleProgress(gapBar),
        muted = muted,
        description = if (muted) {
            UiText.of(Res.string.gap_description_silent_bar, inPhase, phaseLength)
        } else {
            UiText.of(Res.string.gap_description_playing_bar, inPhase, phaseLength)
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
