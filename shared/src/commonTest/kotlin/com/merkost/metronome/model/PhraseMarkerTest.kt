package com.merkost.metronome.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PhraseMarkerTest {
    @Test
    fun offLeavesEveryBeatUntouched() {
        Beat.entries.forEach { beat ->
            (0 until 8).forEach { bar -> assertEquals(beat, phraseBeat(beat, 0, bar, 0)) }
        }
    }

    @Test
    fun theFirstBarOfAPhraseKeepsItsAccents() {
        assertEquals(Beat.HIGH, phraseBeat(Beat.HIGH, 0, 0, 4))
        assertEquals(Beat.HIGH, phraseBeat(Beat.HIGH, 3, 4, 4))
        assertEquals(Beat.LOW, phraseBeat(Beat.LOW, 1, 8, 4))
    }

    @Test
    fun aLowDownbeatIsPromotedInTheFirstBar() {
        assertEquals(Beat.HIGH, phraseBeat(Beat.LOW, 0, 0, 4))
        assertEquals(Beat.HIGH, phraseBeat(Beat.LOW, 0, 12, 12))
    }

    @Test
    fun laterBarsDemoteEveryAccent() {
        assertEquals(Beat.LOW, phraseBeat(Beat.HIGH, 0, 1, 4))
        assertEquals(Beat.LOW, phraseBeat(Beat.HIGH, 3, 1, 4))
        assertEquals(Beat.LOW, phraseBeat(Beat.HIGH, 0, 7, 8))
    }

    @Test
    fun aMutedBeatStaysMuted() {
        assertEquals(Beat.MUTE, phraseBeat(Beat.MUTE, 0, 0, 4))
        assertEquals(Beat.MUTE, phraseBeat(Beat.MUTE, 0, 1, 4))
    }

    @Test
    fun labelReadsBar7Of12() {
        assertEquals("Bar 7 of 12", phraseBarLabel(6, 12, isPlaying = true, countInRemaining = 0))
        assertEquals("Bar 1 of 12", phraseBarLabel(12, 12, isPlaying = true, countInRemaining = 0))
    }

    @Test
    fun labelHiddenWhenOffPausedOrCountingIn() {
        assertNull(phraseBarLabel(6, 0, isPlaying = true, countInRemaining = 0))
        assertNull(phraseBarLabel(6, 12, isPlaying = false, countInRemaining = 0))
        assertNull(phraseBarLabel(0, 12, isPlaying = true, countInRemaining = 3))
    }

    @Test
    fun theAllowedLengthsAreOffAndFourToSixteen() {
        assertEquals(listOf(0, 4, 8, 12, 16), PHRASE_LENGTHS)
    }
}
