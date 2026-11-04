package com.merkost.metronome.screens

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TempoAdjustmentTest {
    @Test
    fun quickAdjustmentsKeepTheExistingTempoRangeAndIntegerHalving() {
        assertNull(TempoAdjustment.MINUS_FIVE.applyTo(44))
        assertEquals(40, TempoAdjustment.MINUS_FIVE.applyTo(45))
        assertNull(TempoAdjustment.HALF.applyTo(79))
        assertEquals(40, TempoAdjustment.HALF.applyTo(80))
        assertEquals(40, TempoAdjustment.HALF.applyTo(81))
        assertEquals(220, TempoAdjustment.DOUBLE.applyTo(110))
        assertNull(TempoAdjustment.DOUBLE.applyTo(111))
        assertEquals(220, TempoAdjustment.PLUS_FIVE.applyTo(215))
        assertNull(TempoAdjustment.PLUS_FIVE.applyTo(216))
    }

    @Test
    fun invalidDraftsNeverProduceAnAdjustment() {
        listOf(Int.MIN_VALUE, -1, 0, 39, 221, Int.MAX_VALUE).forEach { draft ->
            TempoAdjustment.entries.forEach { adjustment -> assertNull(adjustment.applyTo(draft)) }
        }
    }
}
