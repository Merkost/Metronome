package com.merkost.metronome.model

import kotlin.test.Test
import kotlin.test.assertEquals
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.trainer_bar_of
import metronome.shared.generated.resources.trainer_bar_slash
import metronome.shared.generated.resources.trainer_gap_off_bar
import metronome.shared.generated.resources.trainer_rule_caption
import metronome.shared.generated.resources.trainer_rule_every_bars
import metronome.shared.generated.resources.trainer_rule_gap_cycle
import metronome.shared.generated.resources.trainer_rule_manual
import metronome.shared.generated.resources.trainer_step_count
import metronome.shared.generated.resources.trainer_steps_progress
import metronome.shared.generated.resources.trainer_strip_description
import metronome.shared.generated.resources.trainer_strip_description_reached
import metronome.shared.generated.resources.trainer_strip_paused
import metronome.shared.generated.resources.trainer_strip_reached
import metronome.shared.generated.resources.trainer_summary_bars
import metronome.shared.generated.resources.trainer_summary_gap
import metronome.shared.generated.resources.trainer_summary_manual
import metronome.shared.generated.resources.trainer_target_reached

class TempoStripStateTest {
    private val bars = GradualTempoConfig(100, 120, 2, 4)
    private val gapCycle = GradualTempoConfig(100, 120, 2, 3, TempoStepMode.GAP_CYCLE)
    private val manual = GradualTempoConfig(100, 120, 2, 4, TempoStepMode.MANUAL)

    @Test
    fun barsCaptionUnchanged() {
        val state = tempoTrainerStripState(bars, bpm = 104, trainerBar = 5, isPlaying = true, gapRunning = false)
        assertEquals(UiText.plain("104 → 120"), state.title)
        assertEquals(UiText.of(Res.string.trainer_bar_slash, 5, 40), state.caption)
        assertEquals(0.2f, state.progress)
    }

    @Test
    fun gapCycleCaptionCountsSteps() {
        val state = tempoTrainerStripState(gapCycle, bpm = 106, trainerBar = 9, isPlaying = true, gapRunning = true)
        assertEquals(UiText.quantity(Res.plurals.trainer_steps_progress, 10, 3, 10), state.caption)
    }

    @Test
    fun gapCycleWithoutGapNamesTheFallback() {
        val strip = tempoTrainerStripState(gapCycle, bpm = 106, trainerBar = 5, isPlaying = true, gapRunning = false)
        assertEquals(UiText.of(Res.string.trainer_bar_slash, 5, 30), strip.caption)
        assertEquals(
            UiText.of(Res.string.trainer_gap_off_bar, 5, 30),
            trainerProgressCaption(gapCycle, bpm = 106, trainerBar = 5, isPlaying = true, gapRunning = false),
        )
    }

    @Test
    fun manualCaptionCountsStepsEvenWhilePaused() {
        assertEquals(
            UiText.quantity(Res.plurals.trainer_steps_progress, 10, 2, 10),
            tempoTrainerStripState(manual, bpm = 104, trainerBar = 0, isPlaying = true, gapRunning = false).caption,
        )
        assertEquals(
            UiText.quantity(Res.plurals.trainer_steps_progress, 10, 2, 10),
            trainerProgressCaption(manual, bpm = 104, trainerBar = 0, isPlaying = false, gapRunning = false),
        )
    }

    @Test
    fun pausedAndCompleteStates() {
        assertEquals(UiText.of(Res.string.trainer_strip_paused), tempoTrainerStripState(bars, 104, 5, isPlaying = false, gapRunning = false).caption)
        val done = tempoTrainerStripState(bars, 120, 40, isPlaying = true, gapRunning = false)
        assertEquals(UiText.of(Res.string.trainer_strip_reached, 120), done.title)
        assertEquals(UiText.plain(""), done.caption)
        assertEquals(UiText.of(Res.string.trainer_target_reached), trainerProgressCaption(bars, 120, 40, isPlaying = true, gapRunning = false))
    }

    @Test
    fun descriptionIsOneSentence() {
        assertEquals(
            UiText.of(Res.string.trainer_strip_description, 104, 120, UiText.quantity(Res.plurals.trainer_steps_progress, 10, 2, 10)),
            tempoTrainerStripState(gapCycle, 104, 5, isPlaying = true, gapRunning = true).description,
        )
        assertEquals(
            UiText.of(Res.string.trainer_strip_description, 104, 120, UiText.of(Res.string.trainer_bar_of, 5, 40)),
            tempoTrainerStripState(bars, 104, 5, isPlaying = true, gapRunning = false).description,
        )
        assertEquals(
            UiText.of(Res.string.trainer_strip_description_reached, 120),
            tempoTrainerStripState(bars, 120, 40, isPlaying = true, gapRunning = false).description,
        )
    }

    @Test
    fun ruleCaptionNamesWhenTheTempoSteps() {
        assertEquals(UiText.of(Res.string.trainer_rule_caption, 100, 120, UiText.quantity(Res.plurals.trainer_rule_every_bars, 4, "+2", 4)), trainerRuleCaption(bars))
        assertEquals(UiText.of(Res.string.trainer_rule_caption, 100, 120, UiText.of(Res.string.trainer_rule_gap_cycle, "+2")), trainerRuleCaption(gapCycle))
        assertEquals(UiText.of(Res.string.trainer_rule_caption, 100, 120, UiText.of(Res.string.trainer_rule_manual, "+2")), trainerRuleCaption(manual))
        assertEquals(UiText.of(Res.string.trainer_rule_caption, 120, 100, UiText.of(Res.string.trainer_rule_manual, "−2")), trainerRuleCaption(GradualTempoConfig(120, 100, 2, 4, TempoStepMode.MANUAL)))
    }

    @Test
    fun summaryFollowsTheStepMode() {
        assertEquals(UiText.quantity(Res.plurals.trainer_summary_bars, 40, 40, 1), trainerSummaryFor(bars, beatsPerBar = 4))
        assertEquals(UiText.of(Res.string.trainer_summary_gap, UiText.quantity(Res.plurals.trainer_step_count, 10)), trainerSummaryFor(gapCycle, beatsPerBar = 4))
        assertEquals(UiText.of(Res.string.trainer_summary_manual, UiText.quantity(Res.plurals.trainer_step_count, 10)), trainerSummaryFor(manual, beatsPerBar = 4))
        assertEquals(UiText.of(Res.string.trainer_summary_manual, UiText.quantity(Res.plurals.trainer_step_count, 1)), trainerSummaryFor(GradualTempoConfig(100, 102, 2, 4, TempoStepMode.MANUAL), 4))
    }
}
