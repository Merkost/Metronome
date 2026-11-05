package com.merkost.metronome.model

import kotlin.test.Test
import kotlin.test.assertEquals

class TempoNameTest {

    private fun name(bpm: Int) = MetronomeState(rhythm = bpm).tempoName

    @Test
    fun everyBandStartsAndEndsWhereItAlwaysDid() {
        assertEquals("Largo", name(40))
        assertEquals("Largo", name(65))
        assertEquals("Adagio", name(66))
        assertEquals("Adagio", name(75))
        assertEquals("Andante", name(76))
        assertEquals("Andante", name(107))
        assertEquals("Moderato", name(108))
        assertEquals("Moderato", name(119))
        assertEquals("Allegro", name(120))
        assertEquals("Allegro", name(167))
        assertEquals("Presto", name(168))
        assertEquals("Presto", name(200))
    }

    @Test
    fun outsideTheClassicalRangeItIsPrestissimo() {
        assertEquals("Prestissimo", name(39))
        assertEquals("Prestissimo", name(201))
    }

    @Test
    fun theDefaultTempoIsAndante() {
        assertEquals("Andante", MetronomeState().tempoName)
    }
}
