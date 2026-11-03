package com.merkost.metronome.model

import kotlin.test.Test
import kotlin.test.assertEquals

class BeatTest {
    @Test
    fun clicksToCountsStepsAlongTheTapCycle() {
        assertEquals(0, Beat.HIGH.clicksTo(Beat.HIGH))
        assertEquals(1, Beat.HIGH.clicksTo(Beat.LOW))
        assertEquals(2, Beat.HIGH.clicksTo(Beat.MUTE))
        assertEquals(1, Beat.LOW.clicksTo(Beat.MUTE))
        assertEquals(2, Beat.LOW.clicksTo(Beat.HIGH))
        assertEquals(1, Beat.MUTE.clicksTo(Beat.HIGH))
        assertEquals(2, Beat.MUTE.clicksTo(Beat.LOW))
    }

    @Test
    fun applyingTheCountReachesTheTarget() {
        Beat.entries.forEach { from ->
            Beat.entries.forEach { target ->
                var current = from
                repeat(from.clicksTo(target)) { current = current.next() }
                assertEquals(target, current)
            }
        }
    }
}
