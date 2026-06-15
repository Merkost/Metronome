package com.merkost.metronome.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class SubdivisionTimingTest {

    @Test
    fun quarter_hasNoSubClicks() {
        assertEquals(emptyList(), subClickOffsets(500.milliseconds, Subdivision.QUARTER.clicksPerBeat))
    }

    @Test
    fun eighths_splitTheBeatInHalf() {
        assertEquals(listOf(250.milliseconds), subClickOffsets(500.milliseconds, Subdivision.EIGHTH.clicksPerBeat))
    }

    @Test
    fun sixteenths_quarterEvenSpacing() {
        assertEquals(
            listOf(125.milliseconds, 250.milliseconds, 375.milliseconds),
            subClickOffsets(500.milliseconds, Subdivision.SIXTEENTH.clicksPerBeat),
        )
    }

    @Test
    fun triplets_evenlySpacedWithinTheBeat() {
        assertEquals(
            listOf(200.milliseconds, 400.milliseconds),
            subClickOffsets(600.milliseconds, Subdivision.TRIPLET.clicksPerBeat),
        )
    }

    @Test
    fun offsets_areStrictlyIncreasing_andBoundedByTheBeat() {
        for (subdivision in Subdivision.entries) {
            for (bpm in intArrayOf(MIN_BPM, 73, 121, 173, MAX_BPM)) {
                val interval = MetronomeState(rhythm = bpm).beatDuration
                val offsets = subClickOffsets(interval, subdivision.clicksPerBeat)
                var previous = Duration.ZERO
                for (offset in offsets) {
                    assertTrue(offset > previous, "offsets must strictly increase ($subdivision @ $bpm)")
                    assertTrue(offset < interval, "every sub-click must fall inside the beat ($subdivision @ $bpm)")
                    previous = offset
                }
                assertEquals(subdivision.clicksPerBeat - 1, offsets.size)
            }
        }
    }
}
