package com.merkost.metronome.model

import kotlin.test.Test
import kotlin.test.assertEquals

class TempoStripStateTest {
    private val bars = GradualTempoConfig(100, 120, 2, 4)
    private val gapCycle = GradualTempoConfig(100, 120, 2, 3, TempoStepMode.GAP_CYCLE)
    private val manual = GradualTempoConfig(100, 120, 2, 4, TempoStepMode.MANUAL)

    @Test
    fun barsCaptionUnchanged() {
        val state = tempoTrainerStripState(bars, bpm = 104, trainerBar = 5, isPlaying = true, gapRunning = false)
        assertEquals("104 → 120", state.title)
        assertEquals("bar 5 / 40", state.caption)
        assertEquals(0.2f, state.progress)
    }

    @Test
    fun gapCycleCaptionCountsSteps() {
        val state = tempoTrainerStripState(gapCycle, bpm = 106, trainerBar = 9, isPlaying = true, gapRunning = true)
        assertEquals("3 of 10 steps", state.caption)
    }

    @Test
    fun gapCycleWithoutGapNamesTheFallback() {
        val strip = tempoTrainerStripState(gapCycle, bpm = 106, trainerBar = 5, isPlaying = true, gapRunning = false)
        assertEquals("bar 5 / 30", strip.caption)
        assertEquals(
            "Gap trainer off · bar 5 of 30",
            trainerProgressCaption(gapCycle, bpm = 106, trainerBar = 5, isPlaying = true, gapRunning = false),
        )
    }

    @Test
    fun manualCaptionCountsStepsEvenWhilePaused() {
        assertEquals(
            "2 of 10 steps",
            tempoTrainerStripState(manual, bpm = 104, trainerBar = 0, isPlaying = true, gapRunning = false).caption,
        )
        assertEquals(
            "2 of 10 steps",
            trainerProgressCaption(manual, bpm = 104, trainerBar = 0, isPlaying = false, gapRunning = false),
        )
    }

    @Test
    fun pausedAndCompleteStates() {
        assertEquals("paused", tempoTrainerStripState(bars, 104, 5, isPlaying = false, gapRunning = false).caption)
        val done = tempoTrainerStripState(bars, 120, 40, isPlaying = true, gapRunning = false)
        assertEquals("120 BPM reached", done.title)
        assertEquals("", done.caption)
        assertEquals("Target reached", trainerProgressCaption(bars, 120, 40, isPlaying = true, gapRunning = false))
    }

    @Test
    fun descriptionIsOneSentence() {
        assertEquals(
            "Tempo trainer, 104 BPM, target 120, 2 of 10 steps",
            tempoTrainerStripState(gapCycle, 104, 5, isPlaying = true, gapRunning = true).description,
        )
        assertEquals(
            "Tempo trainer, 104 BPM, target 120, bar 5 of 40",
            tempoTrainerStripState(bars, 104, 5, isPlaying = true, gapRunning = false).description,
        )
        assertEquals(
            "Tempo trainer, target 120 BPM reached",
            tempoTrainerStripState(bars, 120, 40, isPlaying = true, gapRunning = false).description,
        )
    }

    @Test
    fun ruleCaptionNamesWhenTheTempoSteps() {
        assertEquals("100 → 120 · +2 every 4 bars", trainerRuleCaption(bars))
        assertEquals("100 → 120 · +2 after each gap cycle", trainerRuleCaption(gapCycle))
        assertEquals("100 → 120 · +2 when you tap", trainerRuleCaption(manual))
        assertEquals("120 → 100 · −2 when you tap", trainerRuleCaption(GradualTempoConfig(120, 100, 2, 4, TempoStepMode.MANUAL)))
    }

    @Test
    fun summaryFollowsTheStepMode() {
        assertEquals("40 bars · ≈ 1 min", trainerSummaryFor(bars, beatsPerBar = 4))
        assertEquals("10 steps · one after each gap cycle", trainerSummaryFor(gapCycle, beatsPerBar = 4))
        assertEquals("10 steps · you choose when", trainerSummaryFor(manual, beatsPerBar = 4))
        assertEquals("1 step · you choose when", trainerSummaryFor(GradualTempoConfig(100, 102, 2, 4, TempoStepMode.MANUAL), 4))
    }
}
