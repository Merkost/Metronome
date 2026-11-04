package com.merkost.metronome.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TempoStepModeTest {
    @Test
    fun gapCycleStepDueTable() {
        assertTrue(gapCycleStepDue(completedBars = 4, gapStartBar = 0, cycleBars = 3))
        assertFalse(gapCycleStepDue(completedBars = 3, gapStartBar = 0, cycleBars = 3))
        assertFalse(gapCycleStepDue(completedBars = 1, gapStartBar = 0, cycleBars = 3))
        assertTrue(gapCycleStepDue(completedBars = 7, gapStartBar = 3, cycleBars = 3))
        assertTrue(gapCycleStepDue(completedBars = 7, gapStartBar = 0, cycleBars = 3))
        assertFalse(gapCycleStepDue(completedBars = 5, gapStartBar = 0, cycleBars = 3))
        assertFalse(gapCycleStepDue(completedBars = 3, gapStartBar = 5, cycleBars = 3))
    }

    @Test
    fun unknownStorageKeyReadsAsBars() {
        assertEquals(TempoStepMode.BARS, TempoStepMode.fromStorageKey(null))
        assertEquals(TempoStepMode.BARS, TempoStepMode.fromStorageKey("nonsense"))
        assertEquals(TempoStepMode.GAP_CYCLE, TempoStepMode.fromStorageKey("GAP_CYCLE"))
        assertEquals(TempoStepMode.MANUAL, TempoStepMode.fromStorageKey("MANUAL"))
    }
}
