package com.merkost.metronome.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.roboto_variable
import org.jetbrains.compose.resources.Font

@Composable
actual fun appFontFamily(): FontFamily? = FontFamily(
    Font(Res.font.roboto_variable, weight = FontWeight.Normal),
    Font(Res.font.roboto_variable, weight = FontWeight.Medium),
    Font(Res.font.roboto_variable, weight = FontWeight.SemiBold),
    Font(Res.font.roboto_variable, weight = FontWeight.Bold),
    Font(Res.font.roboto_variable, weight = FontWeight.ExtraBold),
)
