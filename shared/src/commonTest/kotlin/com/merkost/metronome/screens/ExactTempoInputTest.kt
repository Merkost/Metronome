package com.merkost.metronome.screens

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExactTempoInputTest {

    @Test
    fun onlyDigitsSurviveAndLengthIsCappedAtThree() {
        assertEquals("120", sanitizeTempoInput("120"))
        assertEquals("12", sanitizeTempoInput("1-2.x"))
        assertEquals("999", sanitizeTempoInput("99999"))
        assertEquals("", sanitizeTempoInput("abc"))
    }

    @Test
    fun anEmptyFieldIsNotAnErrorWhileTyping() {
        assertFalse(tempoInputShowsError(""))
    }

    @Test
    fun outOfRangeNumbersShowAnError() {
        assertTrue(tempoInputShowsError("1"))
        assertTrue(tempoInputShowsError("999"))
        assertFalse(tempoInputShowsError("120"))
    }
}
