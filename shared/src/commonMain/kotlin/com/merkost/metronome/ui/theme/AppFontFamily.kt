package com.merkost.metronome.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

/*
 * Android and iOS resolve FontFamily.Default to a real system family with true
 * weights. The web build cannot: Compose renders through Skia on a canvas, and
 * the only face compiled into skiko.wasm is a single Roboto Regular. Bold and
 * SemiBold come out synthetically emboldened there, Medium is indistinguishable
 * from Normal, and glyphs outside its 896-codepoint table are missing entirely.
 * Web therefore supplies a real font; the native targets return null and keep
 * the system family they already had.
 */
@Composable
expect fun appFontFamily(): FontFamily?

fun Typography.withFontFamily(family: FontFamily): Typography = copy(
    displayLarge = displayLarge.copy(fontFamily = family),
    displayMedium = displayMedium.copy(fontFamily = family),
    displaySmall = displaySmall.copy(fontFamily = family),
    headlineLarge = headlineLarge.copy(fontFamily = family),
    headlineMedium = headlineMedium.copy(fontFamily = family),
    headlineSmall = headlineSmall.copy(fontFamily = family),
    titleLarge = titleLarge.copy(fontFamily = family),
    titleMedium = titleMedium.copy(fontFamily = family),
    titleSmall = titleSmall.copy(fontFamily = family),
    bodyLarge = bodyLarge.copy(fontFamily = family),
    bodyMedium = bodyMedium.copy(fontFamily = family),
    bodySmall = bodySmall.copy(fontFamily = family),
    labelLarge = labelLarge.copy(fontFamily = family),
    labelMedium = labelMedium.copy(fontFamily = family),
    labelSmall = labelSmall.copy(fontFamily = family),
)
