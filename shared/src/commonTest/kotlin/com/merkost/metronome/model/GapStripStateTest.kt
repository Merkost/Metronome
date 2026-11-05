package com.merkost.metronome.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
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
        assertEquals(UiText.of(Res.string.gap_count_in, 3), result.title)
        assertEquals(0f, result.progress)
        assertFalse(result.muted)
        assertEquals(UiText.quantity(Res.plurals.gap_bars_caption, 2, 4, 2), result.caption)
    }

    @Test
    fun theLeadInBarHasNoProgressAndNamesWhatComesNext() {
        val playing = state(currentBar = 2, startBar = 3)
        assertEquals(UiText.of(Res.string.gap_clicking_new_cycle), playing.title)
        assertEquals(0f, playing.progress)
        val silent = state(currentBar = 2, startBar = 3, muted = true)
        assertEquals(UiText.of(Res.string.gap_muted_new_cycle), silent.title)
        assertTrue(silent.muted)
    }

    @Test
    fun theFirstBarOfTheCycleStartsTheProgress() {
        val result = state(currentBar = 3, startBar = 3)
        assertEquals(UiText.of(Res.string.gap_clicking_bar, 1, 4), result.title)
        assertEquals(1f / 6f, result.progress)
    }

    @Test
    fun mutedBarsCountWithinTheMutePhase() {
        val result = state(currentBar = 5, muted = true)
        assertEquals(UiText.of(Res.string.gap_muted_bar, 2, 2), result.title)
        assertEquals(1f, result.progress)
        assertTrue(result.muted)
    }

    @Test
    fun pausedStripShowsTheSpeakerAndNoProgress() {
        val result = state(currentBar = 4, muted = true, isPlaying = false)
        assertEquals(UiText.of(Res.string.gap_title), result.title)
        assertEquals(UiText.of(Res.string.gap_paused), result.caption)
        assertFalse(result.muted)
        assertEquals(0f, result.progress)
    }

    @Test
    fun descriptionsNameThePhaseInWords() {
        assertEquals(UiText.of(Res.string.gap_description_playing_bar, 2, 4), state(currentBar = 1).description)
        assertEquals(UiText.of(Res.string.gap_description_silent_bar, 1, 2), state(currentBar = 4, muted = true).description)
        assertEquals(UiText.of(Res.string.gap_description_count_in, 3), state(currentBar = 0, countInRemaining = 3).description)
        assertEquals(UiText.of(Res.string.gap_description_new_cycle), state(currentBar = 2, startBar = 3).description)
        assertEquals(UiText.of(Res.string.gap_description_paused), state(currentBar = 0, isPlaying = false).description)
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
