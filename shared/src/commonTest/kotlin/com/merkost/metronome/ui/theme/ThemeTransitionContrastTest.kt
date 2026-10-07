package com.merkost.metronome.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ThemeTransitionContrastTest {
    @Test
    fun textStaysReadableWhileMonochromeAppearanceReverses() {
        for (step in 0..100) {
            val fraction = step / 100f
            val surface = lerp(Color.White, Color.Black, fraction)
            val foreground = readableThemeColor(lerp(Color.Black, Color.White, fraction), surface, Color.White, Color.Black)
            assertTrue(contrast(foreground, surface) >= 4.5f, "Unreadable at $fraction")
        }
    }

    @Test
    fun readableBrandForegroundKeepsItsColor() {
        assertEquals(MelroseDark, readableThemeColor(MelroseDark, Color.White, Color.Black, Color.White))
    }

    @Test
    fun dynamicNeutralRolesHaveAReadableFallbackDuringReversal() {
        val light = Color(0xFFE6E0E9)
        val dark = Color(0xFF322F35)
        val surface = Color(0xFF777777)
        val result = readableThemeColor(lerp(dark, light, 0.5f), surface, light, dark)
        assertTrue(contrast(result, surface) >= 4.5f)
    }

    private fun contrast(foreground: Color, background: Color): Float =
        (maxOf(foreground.luminance(), background.luminance()) + 0.05f) /
            (minOf(foreground.luminance(), background.luminance()) + 0.05f)
}
