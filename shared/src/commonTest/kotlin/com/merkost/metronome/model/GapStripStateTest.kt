package com.merkost.metronome.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GapStripStateTest {
    private val config = GapTrainerConfig(playBars = 4, muteBars = 2)

    private fun state(
        currentBar: Int,
        startBar: Int = 0,
        muted: Boolean = false,
        isPlaying: Boolean = true,
        countInRemaining: Int = 0,
    ) = gapStripState(config, currentBar, startBar, muted, isPlaying, countInRemaining)

    @Test
    fun countInShowsTheCountWithNoProgress() {
        val result = state(currentBar = 0, countInRemaining = 3)
        assertEquals("Count-in · 3", result.title)
        assertEquals(0f, result.progress)
        assertFalse(result.muted)
        assertEquals("4 + 2 bars", result.caption)
    }

    @Test
    fun theLeadInBarHasNoProgressAndNamesWhatComesNext() {
        val playing = state(currentBar = 2, startBar = 3)
        assertEquals("Click · new cycle next bar", playing.title)
        assertEquals(0f, playing.progress)
        val silent = state(currentBar = 2, startBar = 3, muted = true)
        assertEquals("Muted · new cycle next bar", silent.title)
        assertTrue(silent.muted)
    }

    @Test
    fun theFirstBarOfTheCycleStartsTheProgress() {
        val result = state(currentBar = 3, startBar = 3)
        assertEquals("Click · bar 1/4", result.title)
        assertEquals(1f / 6f, result.progress)
    }

    @Test
    fun mutedBarsCountWithinTheMutePhase() {
        val result = state(currentBar = 5, muted = true)
        assertEquals("Muted · bar 2/2", result.title)
        assertEquals(1f, result.progress)
        assertTrue(result.muted)
    }

    @Test
    fun pausedStripShowsTheSpeakerAndNoProgress() {
        val result = state(currentBar = 4, muted = true, isPlaying = false)
        assertEquals("Gap trainer", result.title)
        assertEquals("paused", result.caption)
        assertFalse(result.muted)
        assertEquals(0f, result.progress)
    }

    @Test
    fun descriptionsNameThePhaseInWords() {
        assertEquals("Gap trainer, playing bar 2 of 4", state(currentBar = 1).description)
        assertEquals("Gap trainer, silent bar 1 of 2", state(currentBar = 4, muted = true).description)
        assertEquals("Gap trainer, count-in 3", state(currentBar = 0, countInRemaining = 3).description)
        assertEquals("Gap trainer, new cycle next bar", state(currentBar = 2, startBar = 3).description)
        assertEquals("Gap trainer, paused", state(currentBar = 0, isPlaying = false).description)
    }

    @Test
    fun clickRestsOnlyWhilePlayingInASilentBarOfTheCycle() {
        assertTrue(gapClickResting(true, currentBar = 5, startBar = 3, muted = true, isPlaying = true, countInRemaining = 0))
        assertFalse(gapClickResting(false, currentBar = 5, startBar = 3, muted = true, isPlaying = true, countInRemaining = 0))
        assertFalse(gapClickResting(true, currentBar = 5, startBar = 3, muted = false, isPlaying = true, countInRemaining = 0))
        assertFalse(gapClickResting(true, currentBar = 5, startBar = 3, muted = true, isPlaying = false, countInRemaining = 0))
        assertFalse(gapClickResting(true, currentBar = 5, startBar = 3, muted = true, isPlaying = true, countInRemaining = 2))
        assertFalse(gapClickResting(true, currentBar = 2, startBar = 3, muted = true, isPlaying = true, countInRemaining = 0))
    }
}
