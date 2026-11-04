package com.merkost.metronome.model

import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BeatStrikeTest {

    private fun settleMillis(stiffness: Float): Double = 5.83 / sqrt(stiffness.toDouble()) * 1000.0

    @Test
    fun accentStrikesHarderThanNormalAndMuteDoesNotStrike() {
        assertTrue(strikePeakFor(Beat.HIGH) > strikePeakFor(Beat.LOW))
        assertTrue(strikePeakFor(Beat.LOW) > 1f)
        assertEquals(1f, strikePeakFor(Beat.MUTE))
    }

    @Test
    fun strikeStaysSubtle() {
        assertTrue(strikePeakFor(Beat.HIGH) <= 1.2f)
    }

    @Test
    fun slowTempoKeepsTheCalmBaseline() {
        assertEquals(INDICATOR_BASE_STIFFNESS, indicatorStiffnessFor(bpm = 60))
        assertEquals(INDICATOR_BASE_STIFFNESS, indicatorStiffnessFor(bpm = 40))
    }

    @Test
    fun indicatorSettlesWithinAThirdOfTheBeatAcrossTheCrossfadeFreeRange() {
        for (bpm in 40..INDICATOR_CROSSFADE_ABOVE_BPM) {
            val beatMillis = 60_000.0 / bpm
            val settle = settleMillis(indicatorStiffnessFor(bpm))
            assertTrue(settle <= beatMillis * 0.35 + 1.0, "bpm=$bpm settle=$settle beat=$beatMillis")
        }
    }

    @Test
    fun stiffnessNeverDecreasesAsTempoRises() {
        var previous = 0f
        for (bpm in 30..300) {
            val value = indicatorStiffnessFor(bpm)
            assertTrue(value >= previous, "bpm=$bpm")
            previous = value
        }
    }

    @Test
    fun fastTempoCrossfadesInsteadOfSliding() {
        assertFalse(indicatorCrossfades(INDICATOR_CROSSFADE_ABOVE_BPM))
        assertTrue(indicatorCrossfades(INDICATOR_CROSSFADE_ABOVE_BPM + 1))
        assertTrue(indicatorCrossfades(300))
    }

    @Test
    fun strikeIsSuppressedWhenMotionIsReducedOrNotPlaying() {
        assertTrue(strikeVisible(playing = true, reducedMotion = false, beat = Beat.LOW))
        assertFalse(strikeVisible(playing = true, reducedMotion = true, beat = Beat.LOW))
        assertFalse(strikeVisible(playing = false, reducedMotion = false, beat = Beat.HIGH))
        assertFalse(strikeVisible(playing = true, reducedMotion = false, beat = Beat.MUTE))
    }

    @Test
    fun playHaloOnlyPulsesOnTheFirstBeatOfTheBar() {
        assertTrue(haloPulseVisible(playing = true, reducedMotion = false, beatIndex = 0))
        assertFalse(haloPulseVisible(playing = true, reducedMotion = false, beatIndex = 1))
        assertFalse(haloPulseVisible(playing = true, reducedMotion = true, beatIndex = 0))
        assertFalse(haloPulseVisible(playing = false, reducedMotion = false, beatIndex = 0))
    }

    @Test
    fun haloAlphaStaysWithinFiveToEightPercent() {
        assertTrue(HALO_PULSE_ALPHA in 0.05f..0.08f)
    }
}
