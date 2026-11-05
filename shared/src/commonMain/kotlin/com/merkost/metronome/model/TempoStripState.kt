package com.merkost.metronome.model

import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.trainer_bar_of
import metronome.shared.generated.resources.trainer_bar_of_capital
import metronome.shared.generated.resources.trainer_bar_slash
import metronome.shared.generated.resources.trainer_gap_off_bar
import metronome.shared.generated.resources.trainer_paused_advances
import metronome.shared.generated.resources.trainer_rule_caption
import metronome.shared.generated.resources.trainer_rule_every_bars
import metronome.shared.generated.resources.trainer_rule_gap_cycle
import metronome.shared.generated.resources.trainer_rule_manual
import metronome.shared.generated.resources.trainer_steps_progress
import metronome.shared.generated.resources.trainer_strip_description
import metronome.shared.generated.resources.trainer_strip_description_reached
import metronome.shared.generated.resources.trainer_strip_paused
import metronome.shared.generated.resources.trainer_strip_reached
import metronome.shared.generated.resources.trainer_summary_bars
import metronome.shared.generated.resources.trainer_summary_gap
import metronome.shared.generated.resources.trainer_summary_manual
import metronome.shared.generated.resources.trainer_step_count
import metronome.shared.generated.resources.trainer_target_reached

data class TempoStripState(
    val title: UiText,
    val caption: UiText,
    val progress: Float,
    val description: UiText,
)

private fun stepsText(config: GradualTempoConfig, bpm: Int): UiText =
    UiText.quantity(Res.plurals.trainer_steps_progress, config.totalSteps, config.stepsTaken(bpm), config.totalSteps)

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
    val bar = trainerBar.coerceAtMost(config.totalBars)
    val title = if (complete) {
        UiText.of(Res.string.trainer_strip_reached, config.endBpm)
    } else {
        UiText.plain("$bpm → ${config.endBpm}")
    }
    val caption = when {
        complete -> UiText.plain("")
        !isPlaying -> UiText.of(Res.string.trainer_strip_paused)
        countsSteps(config, gapRunning) -> stepsText(config, bpm)
        else -> UiText.of(Res.string.trainer_bar_slash, bar, config.totalBars)
    }
    val detail = if (countsSteps(config, gapRunning)) {
        stepsText(config, bpm)
    } else {
        UiText.of(Res.string.trainer_bar_of, bar, config.totalBars)
    }
    val description = if (complete) {
        UiText.of(Res.string.trainer_strip_description_reached, config.endBpm)
    } else {
        UiText.of(Res.string.trainer_strip_description, bpm, config.endBpm, detail)
    }
    return TempoStripState(title, caption, config.progressFor(bpm), description)
}

fun trainerRuleCaption(config: GradualTempoConfig): UiText {
    val direction = if (config.ascending) "+" else "−"
    val step = "$direction${config.increment}"
    val rule = when (config.stepMode) {
        TempoStepMode.BARS -> UiText.quantity(Res.plurals.trainer_rule_every_bars, config.barsPerStep, step, config.barsPerStep)
        TempoStepMode.GAP_CYCLE -> UiText.of(Res.string.trainer_rule_gap_cycle, step)
        TempoStepMode.MANUAL -> UiText.of(Res.string.trainer_rule_manual, step)
    }
    return UiText.of(Res.string.trainer_rule_caption, config.startBpm, config.endBpm, rule)
}

fun trainerProgressCaption(
    config: GradualTempoConfig,
    bpm: Int,
    trainerBar: Int,
    isPlaying: Boolean,
    gapRunning: Boolean,
): UiText {
    val bar = trainerBar.coerceAtMost(config.totalBars)
    return when {
        config.isComplete(bpm) -> UiText.of(Res.string.trainer_target_reached)
        config.stepMode == TempoStepMode.MANUAL -> stepsText(config, bpm)
        !isPlaying -> UiText.of(Res.string.trainer_paused_advances)
        config.stepMode == TempoStepMode.GAP_CYCLE && gapRunning -> stepsText(config, bpm)
        config.stepMode == TempoStepMode.GAP_CYCLE -> UiText.of(Res.string.trainer_gap_off_bar, bar, config.totalBars)
        else -> UiText.of(Res.string.trainer_bar_of_capital, bar, config.totalBars)
    }
}

fun trainerSummaryFor(config: GradualTempoConfig, beatsPerBar: Int): UiText {
    val steps = UiText.quantity(Res.plurals.trainer_step_count, config.totalSteps)
    return when (config.stepMode) {
        TempoStepMode.BARS -> {
            val averageBpm = (config.startBpm + config.endBpm) / 2f
            val minutes = (config.totalBars * beatsPerBar / averageBpm).toInt().coerceAtLeast(1)
            UiText.quantity(Res.plurals.trainer_summary_bars, config.totalBars, config.totalBars, minutes)
        }
        TempoStepMode.GAP_CYCLE -> UiText.of(Res.string.trainer_summary_gap, steps)
        TempoStepMode.MANUAL -> UiText.of(Res.string.trainer_summary_manual, steps)
    }
}
