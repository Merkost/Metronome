package com.merkost.metronome.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoverTintTest {

    @Test
    fun restingSurfaceCarriesNoTint() {
        assertEquals(0f, hoverTintAlpha(hovered = false, pressed = false, dark = false))
        assertEquals(0f, hoverTintAlpha(hovered = false, pressed = false, dark = true))
    }

    @Test
    fun hoverIsSixPercentInLightAndEightInDark() {
        assertEquals(0.06f, hoverTintAlpha(hovered = true, pressed = false, dark = false))
        assertEquals(0.08f, hoverTintAlpha(hovered = true, pressed = false, dark = true))
    }

    @Test
    fun pressedIsTenPercentAndWinsOverHover() {
        assertEquals(0.10f, hoverTintAlpha(hovered = true, pressed = true, dark = false))
        assertEquals(0.10f, hoverTintAlpha(hovered = false, pressed = true, dark = true))
    }

    @Test
    fun tintStaysQuieterThanAnySelectedState() {
        assertTrue(HoverTintLight < HoverTintDark)
        assertTrue(HoverTintDark < HoverTintPressed)
        assertTrue(HoverTintPressed <= 0.12f)
    }
}
