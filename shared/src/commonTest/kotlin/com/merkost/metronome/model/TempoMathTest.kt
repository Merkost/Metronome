package com.merkost.metronome.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TempoMathTest {

    @Test
    fun emptyTaps_returnNull() {
        assertNull(bpmFromTapIntervals(emptyList()))
    }

    @Test
    fun nonPositiveMedian_returnsNull() {
        assertNull(bpmFromTapIntervals(listOf(0L)))
    }

    @Test
    fun singleInterval_mapsToBpm() {
        assertEquals(120, bpmFromTapIntervals(listOf(500L)))
        assertEquals(60, bpmFromTapIntervals(listOf(1000L)))
    }

    @Test
    fun roundsInsteadOfTruncating() {
        assertEquals(175, bpmFromTapIntervals(listOf(343L)))
    }

    @Test
    fun oddCount_usesMiddleValue() {
        assertEquals(120, bpmFromTapIntervals(listOf(600L, 400L, 500L)))
    }

    @Test
    fun evenCount_averagesTwoMiddleValues_withoutTruncation() {
        assertEquals(130, bpmFromTapIntervals(listOf(461L, 462L)))
    }

    @Test
    fun clampsToSupportedRange() {
        assertEquals(MAX_BPM, bpmFromTapIntervals(listOf(100L)))
        assertEquals(MIN_BPM, bpmFromTapIntervals(listOf(2000L)))
    }

    @Test
    fun median_rejectsASingleOutlier() {
        assertEquals(120, bpmFromTapIntervals(listOf(500L, 5000L, 500L)))
    }

    @Test
    fun isOrderIndependent() {
        assertEquals(
            bpmFromTapIntervals(listOf(480L, 500L, 520L)),
            bpmFromTapIntervals(listOf(520L, 480L, 500L)),
        )
    }
}
