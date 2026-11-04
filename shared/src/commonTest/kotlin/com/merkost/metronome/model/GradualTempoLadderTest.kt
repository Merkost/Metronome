package com.merkost.metronome.model

import kotlin.test.Test
import kotlin.test.assertEquals

class GradualTempoLadderTest {
    private val ascending = GradualTempoConfig(startBpm = 116, endBpm = 132, increment = 5)
    private val descending = GradualTempoConfig(startBpm = 132, endBpm = 116, increment = 5)

    @Test
    fun stepsTakenCountsRungsFromTheStart() {
        assertEquals(0, ascending.stepsTaken(116))
        assertEquals(1, ascending.stepsTaken(121))
        assertEquals(3, ascending.stepsTaken(131))
        assertEquals(4, ascending.stepsTaken(132))
        assertEquals(0, ascending.stepsTaken(100))
        assertEquals(4, ascending.stepsTaken(140))
    }

    @Test
    fun previousRungFromTheTargetIsTheLastRung() {
        assertEquals(131, ascending.previousBpmFrom(132))
        assertEquals(126, ascending.previousBpmFrom(131))
        assertEquals(121, ascending.previousBpmFrom(126))
        assertEquals(116, ascending.previousBpmFrom(121))
    }

    @Test
    fun previousRungNeverGoesBelowTheStart() {
        assertEquals(116, ascending.previousBpmFrom(116))
        assertEquals(100, ascending.previousBpmFrom(100))
    }

    @Test
    fun descendingLaddersStepTheOtherWay() {
        assertEquals(2, descending.stepsTaken(122))
        assertEquals(127, descending.previousBpmFrom(122))
        assertEquals(132, descending.previousBpmFrom(132))
    }

    @Test
    fun theStepModeDefaultsToBars() {
        assertEquals(TempoStepMode.BARS, GradualTempoConfig(80, 120, 5, 8).stepMode)
        assertEquals(GradualTempoConfig(80, 120, 5, 8), GradualTempoConfig(80, 120, 5, 8, TempoStepMode.BARS))
    }
}
