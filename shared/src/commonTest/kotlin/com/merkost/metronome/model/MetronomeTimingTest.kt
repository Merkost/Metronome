package com.merkost.metronome.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.DurationUnit

class MetronomeTimingTest {

    private fun stateAt(bpm: Int) = MetronomeState(rhythm = bpm)

    @Test
    fun beatDuration_isFractional_andYieldsExactTempo() {
        for (bpm in MIN_BPM..MAX_BPM) {
            val ms = stateAt(bpm).beatDuration.toDouble(DurationUnit.MILLISECONDS)
            assertEquals(
                bpm.toDouble(),
                60_000.0 / ms,
                1e-4,
                "beatDuration must encode the exact tempo for $bpm BPM",
            )
        }
    }

    @Test
    fun beatDuration_exactValues() {
        assertEquals(500.0, stateAt(120).beatDuration.toDouble(DurationUnit.MILLISECONDS), 1e-9)
        assertEquals(600.0, stateAt(100).beatDuration.toDouble(DurationUnit.MILLISECONDS), 1e-9)
        assertEquals(1000.0, stateAt(60).beatDuration.toDouble(DurationUnit.MILLISECONDS), 1e-9)
        assertEquals(
            60_000.0 / 173,
            stateAt(173).beatDuration.toDouble(DurationUnit.MILLISECONDS),
            1e-6,
        )
    }

    @Test
    fun interval_rounds_insteadOfTruncating() {
        assertEquals(500, stateAt(120).interval)
        assertEquals(600, stateAt(100).interval)
        assertEquals(347, stateAt(173).interval)
        assertEquals(429, stateAt(140).interval)
        assertEquals(273, stateAt(220).interval)
    }

    @Test
    fun interval_roundingError_neverExceedsHalfMillisecond() {
        for (bpm in MIN_BPM..MAX_BPM) {
            val ideal = 60_000.0 / bpm
            assertTrue(
                kotlin.math.abs(stateAt(bpm).interval - ideal) <= 0.5,
                "rounded interval for $bpm BPM should be within 0.5 ms of $ideal",
            )
        }
    }

    @Test
    fun fractionalInterval_doesNotAccumulateDrift_over240Beats() {
        val bpm = 173
        val beats = 240
        val ideal = beats * (60_000.0 / bpm)
        val cumulative = beats * stateAt(bpm).beatDuration.toDouble(DurationUnit.MILLISECONDS)
        assertTrue(
            kotlin.math.abs(cumulative - ideal) < 0.5,
            "fractional interval should not accumulate drift; got ${cumulative - ideal} ms over $beats beats",
        )
    }
}
