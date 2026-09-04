package com.merkost.metronome.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.roboto_variable
import org.jetbrains.compose.resources.Font

/*
 * One variable WOFF2 covering wght 400-800, ~37 KB. Each Font() entry drives
 * the wght axis from its weight, so the real Medium, SemiBold, Bold and
 * ExtraBold cuts are used instead of Skia synthesising them from Regular.
 * Roboto is the same family Android resolves by default, so the web build now
 * matches the Android app rather than approximating it.
 *
 * Licensed under the SIL Open Font License 1.1; the licence ships alongside
 * the build as roboto-OFL.txt.
 */
@Composable
actual fun appFontFamily(): FontFamily? = FontFamily(
    Font(Res.font.roboto_variable, weight = FontWeight.Normal),
    Font(Res.font.roboto_variable, weight = FontWeight.Medium),
    Font(Res.font.roboto_variable, weight = FontWeight.SemiBold),
    Font(Res.font.roboto_variable, weight = FontWeight.Bold),
    Font(Res.font.roboto_variable, weight = FontWeight.ExtraBold),
)
