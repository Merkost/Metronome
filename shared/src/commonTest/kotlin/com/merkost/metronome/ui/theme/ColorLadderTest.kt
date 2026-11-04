package com.merkost.metronome.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ColorLadderTest {

    private val dark = mapOf(
        "bw" to BlackNWhiteDarkColorScheme,
        "melrose" to PurpleDarkColorScheme,
        "periwinkle" to PeriwinkleDarkColorScheme,
        "mint" to MintGreenDarkColorScheme,
        "pink" to PinkLaceDarkColorScheme,
    )

    private val light = mapOf(
        "bw" to BlackNWhiteLightColorScheme,
        "melrose" to PurpleLightColorScheme,
        "periwinkle" to PeriwinkleLightColorScheme,
        "mint" to MintGreenLightColorScheme,
        "pink" to PinkLaceLightColorScheme,
    )

    private val every = dark.mapKeys { "dark-${it.key}" } + light.mapKeys { "light-${it.key}" }

    private fun contrast(foreground: Color, background: Color): Float {
        val fg = foreground.compositeOver(background).luminance()
        val bg = background.luminance()
        val lighter = maxOf(fg, bg)
        val darker = minOf(fg, bg)
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    private fun surfaces(scheme: ColorScheme): List<Color> = listOf(
        scheme.surface,
        scheme.surfaceContainerLow,
        scheme.surfaceContainer,
        scheme.surfaceContainerHigh,
        scheme.surfaceContainerHighest,
        scheme.surfaceVariant.compositeOver(scheme.surface),
        scheme.primaryContainer.compositeOver(scheme.surface),
    )

    private fun isNeutral(color: Color): Boolean =
        abs(color.red - color.green) < 0.01f && abs(color.green - color.blue) < 0.01f

    @Test
    fun secondaryTextIsQuieterThanPrimaryTextButStillReadable() {
        every.forEach { (name, scheme) ->
            assertNotEquals(scheme.onSurface, scheme.onSurfaceVariant, name)
            surfaces(scheme).forEach { surface ->
                assertTrue(contrast(scheme.onSurfaceVariant, surface) >= 4.5f, "$name on $surface")
            }
        }
    }

    @Test
    fun secondaryTextSitsNearSixtyFivePercentInk() {
        assertEquals(Color(0xFFADADAD), BlackNWhiteDarkColorScheme.onSurfaceVariant)
        assertEquals(Color(0xFF595959), BlackNWhiteLightColorScheme.onSurfaceVariant)
        dark.values.forEach { assertEquals(Color(0xFFADADAD), it.onSurfaceVariant) }
        light.values.forEach { assertEquals(Color(0xFF595959), it.onSurfaceVariant) }
    }

    @Test
    fun tertiaryIsDistinctFromPrimaryAndReadable() {
        every.forEach { (name, scheme) ->
            assertNotEquals(scheme.primary, scheme.tertiary, name)
            assertTrue(contrast(scheme.tertiary, scheme.surface) >= 4.5f, "$name tertiary on surface")
            assertTrue(contrast(scheme.onTertiary, scheme.tertiary) >= 4.5f, "$name onTertiary")
        }
    }

    @Test
    fun darkLadderUsesTheDesignedGreys() {
        val scheme = BlackNWhiteDarkColorScheme
        assertEquals(Color.Black, scheme.surface)
        assertEquals(Color(0xFF333333), scheme.primaryContainer)
        assertEquals(Color(0xFF2B2B2B), scheme.surfaceVariant)
        assertEquals(Color(0xFF222222), scheme.secondaryContainer)
        assertEquals(Color(0xFF333333), scheme.tertiaryContainer)
    }

    @Test
    fun lightNeutralsAreOpaque() {
        val scheme = BlackNWhiteLightColorScheme
        listOf(scheme.primaryContainer, scheme.surfaceVariant, scheme.secondaryContainer, scheme.tertiaryContainer)
            .forEach { assertEquals(1f, it.alpha) }
    }

    @Test
    fun outlineRolesAreNeutralOnEveryScheme() {
        every.forEach { (name, scheme) ->
            assertTrue(isNeutral(scheme.outline), "$name outline")
            assertTrue(isNeutral(scheme.outlineVariant), "$name outlineVariant")
            assertTrue(isNeutral(scheme.scrim), "$name scrim")
        }
    }

    @Test
    fun hairlinesAreTwelvePercentInkAndControlEdgesAreVisible() {
        dark.values.forEach {
            assertEquals(Color.White.copy(alpha = 0.12f), it.outlineVariant)
            assertTrue(contrast(it.outline, it.surface) >= 3f)
        }
        light.values.forEach {
            assertEquals(Color.Black.copy(alpha = 0.12f), it.outlineVariant)
            assertTrue(contrast(it.outline, it.surface) >= 3f)
        }
    }

    @Test
    fun errorRolesAreSetExplicitlyAndReadable() {
        every.forEach { (name, scheme) ->
            assertTrue(contrast(scheme.error, scheme.surface) >= 4.5f, "$name error")
            assertTrue(contrast(scheme.onError, scheme.error) >= 4.5f, "$name onError")
            assertTrue(contrast(scheme.onErrorContainer, scheme.errorContainer) >= 4.5f, "$name onErrorContainer")
        }
        assertEquals(Color(0xFFB3261E), BlackNWhiteLightColorScheme.error)
        assertEquals(Color(0xFFF2B8B5), BlackNWhiteDarkColorScheme.error)
    }
}
