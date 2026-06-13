package com.merkost.metronome.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class AppColorScheme(val lightColor: ColorScheme, val darkColor: ColorScheme) {
    MATERIAL3(BlackNWhiteLightColorScheme, BlackNWhiteDarkColorScheme),
    BLACKNWHITE(BlackNWhiteLightColorScheme, BlackNWhiteDarkColorScheme),
    MELROSE(PurpleLightColorScheme, PurpleDarkColorScheme),
    PERIWINKLE(PeriwinkleLightColorScheme, PeriwinkleDarkColorScheme),
    MINT_GREEN(MintGreenLightColorScheme, MintGreenDarkColorScheme),
    PINK_LACE(PinkLaceLightColorScheme, PinkLaceDarkColorScheme);

    companion object {
        fun defaultValues(): List<AppColorScheme> {
            return listOf(BLACKNWHITE, MELROSE, PERIWINKLE, MINT_GREEN, PINK_LACE)
        }
    }
}

private val DarkContainerLowest = Color(0xFF0A0A0A)
private val DarkContainerLow = Color(0xFF121212)
private val DarkContainer = Color(0xFF1A1A1A)
private val DarkContainerHigh = Color(0xFF222222)
private val DarkContainerHighest = Color(0xFF2B2B2B)

private val LightContainerLowest = Color.White
private val LightContainerLow = Color(0xFFF7F7F7)
private val LightContainer = Color(0xFFF2F2F2)
private val LightContainerHigh = Color(0xFFECECEC)
private val LightContainerHighest = Color(0xFFE6E6E6)

internal val BlackNWhiteDarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    background = Color.Black,
    surface = Color.Black,
    primaryContainer = Color.DarkGray,
    surfaceVariant = Color.DarkGray,
    secondaryContainer = Color.DarkGray,
    tertiary = Color.White,
    onTertiary = Color.Black,
    tertiaryContainer = Color.DarkGray,
    surfaceTint = Color.White,
    surfaceContainerLowest = DarkContainerLowest,
    surfaceContainerLow = DarkContainerLow,
    surfaceContainer = DarkContainer,
    surfaceContainerHigh = DarkContainerHigh,
    surfaceContainerHighest = DarkContainerHighest,
)

internal val BlackNWhiteLightColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    background = Color.White,
    surface = Color.White,
    primaryContainer = Color.LightGray.copy(0.5f),
    surfaceVariant = Color.LightGray.copy(0.5f),
    secondaryContainer = Color.LightGray.copy(0.5f),
    tertiary = Color.Black,
    onTertiary = Color.White,
    tertiaryContainer = Color.LightGray.copy(0.5f),
    surfaceTint = Color.Black,
    surfaceContainerLowest = LightContainerLowest,
    surfaceContainerLow = LightContainerLow,
    surfaceContainer = LightContainer,
    surfaceContainerHigh = LightContainerHigh,
    surfaceContainerHighest = LightContainerHighest,
)

internal val PurpleDarkColorScheme = darkColorScheme(
    primary = Melrose,
    onPrimary = Color.Black,
    background = Color.Black,
    surface = Color.Black,
    primaryContainer = MelroseDark.copy(alpha = 0.5f),
    surfaceVariant = MelroseDark.copy(alpha = 0.4f),
    tertiary = Melrose,
    onTertiary = Color.Black,
    tertiaryContainer = MelroseDark.copy(alpha = 0.5f),
    surfaceTint = Melrose,
    surfaceContainerLowest = DarkContainerLowest,
    surfaceContainerLow = DarkContainerLow,
    surfaceContainer = DarkContainer,
    surfaceContainerHigh = DarkContainerHigh,
    surfaceContainerHighest = DarkContainerHighest,
)

internal val PurpleLightColorScheme = lightColorScheme(
    primary = MelroseDark,
    onPrimary = Color.White,
    background = Color.White,
    surface = Color.White,
    primaryContainer = Melrose.copy(alpha = 0.3f),
    surfaceVariant = Melrose.copy(alpha = 0.4f),
    tertiary = MelroseDark,
    onTertiary = Color.White,
    tertiaryContainer = Melrose.copy(alpha = 0.3f),
    surfaceTint = MelroseDark,
    surfaceContainerLowest = LightContainerLowest,
    surfaceContainerLow = LightContainerLow,
    surfaceContainer = LightContainer,
    surfaceContainerHigh = LightContainerHigh,
    surfaceContainerHighest = LightContainerHighest,
)

internal val PeriwinkleLightColorScheme = lightColorScheme(
    primary = PeriwinkleDark,
    onPrimary = Color.White,
    background = Color.White,
    surface = Color.White,
    primaryContainer = Periwinkle.copy(alpha = 0.3f),
    surfaceVariant = Periwinkle.copy(alpha = 0.4f),
    tertiary = PeriwinkleDark,
    onTertiary = Color.White,
    tertiaryContainer = Periwinkle.copy(alpha = 0.3f),
    surfaceTint = PeriwinkleDark,
    surfaceContainerLowest = LightContainerLowest,
    surfaceContainerLow = LightContainerLow,
    surfaceContainer = LightContainer,
    surfaceContainerHigh = LightContainerHigh,
    surfaceContainerHighest = LightContainerHighest,
)

internal val PeriwinkleDarkColorScheme = darkColorScheme(
    primary = Periwinkle,
    onPrimary = Color.Black,
    background = Color.Black,
    surface = Color.Black,
    primaryContainer = PeriwinkleDark.copy(alpha = 0.5f),
    surfaceVariant = PeriwinkleDark.copy(alpha = 0.4f),
    tertiary = Periwinkle,
    onTertiary = Color.Black,
    tertiaryContainer = PeriwinkleDark.copy(alpha = 0.5f),
    surfaceTint = Periwinkle,
    surfaceContainerLowest = DarkContainerLowest,
    surfaceContainerLow = DarkContainerLow,
    surfaceContainer = DarkContainer,
    surfaceContainerHigh = DarkContainerHigh,
    surfaceContainerHighest = DarkContainerHighest,
)

internal val MintGreenLightColorScheme = lightColorScheme(
    primary = MintGreenDark,
    onPrimary = Color.White,
    background = Color.White,
    surface = Color.White,
    primaryContainer = MintGreen.copy(alpha = 0.3f),
    surfaceVariant = MintGreen.copy(alpha = 0.4f),
    tertiary = MintGreenDark,
    onTertiary = Color.White,
    tertiaryContainer = MintGreen.copy(alpha = 0.3f),
    surfaceTint = MintGreenDark,
    surfaceContainerLowest = LightContainerLowest,
    surfaceContainerLow = LightContainerLow,
    surfaceContainer = LightContainer,
    surfaceContainerHigh = LightContainerHigh,
    surfaceContainerHighest = LightContainerHighest,
)

internal val MintGreenDarkColorScheme = darkColorScheme(
    primary = MintGreen,
    onPrimary = Color.Black,
    background = Color.Black,
    surface = Color.Black,
    primaryContainer = MintGreenDark.copy(alpha = 0.5f),
    surfaceVariant = MintGreenDark.copy(alpha = 0.4f),
    tertiary = MintGreen,
    onTertiary = Color.Black,
    tertiaryContainer = MintGreenDark.copy(alpha = 0.5f),
    surfaceTint = MintGreen,
    surfaceContainerLowest = DarkContainerLowest,
    surfaceContainerLow = DarkContainerLow,
    surfaceContainer = DarkContainer,
    surfaceContainerHigh = DarkContainerHigh,
    surfaceContainerHighest = DarkContainerHighest,
)

internal val PinkLaceLightColorScheme = lightColorScheme(
    primary = PinkLaceDark,
    onPrimary = Color.White,
    background = Color.White,
    surface = Color.White,
    primaryContainer = PinkLace.copy(alpha = 0.3f),
    surfaceVariant = PinkLace.copy(alpha = 0.4f),
    tertiary = PinkLaceDark,
    onTertiary = Color.White,
    tertiaryContainer = PinkLace.copy(alpha = 0.3f),
    surfaceTint = PinkLaceDark,
    surfaceContainerLowest = LightContainerLowest,
    surfaceContainerLow = LightContainerLow,
    surfaceContainer = LightContainer,
    surfaceContainerHigh = LightContainerHigh,
    surfaceContainerHighest = LightContainerHighest,
)

internal val PinkLaceDarkColorScheme = darkColorScheme(
    primary = PinkLace,
    onPrimary = Color.Black,
    background = Color.Black,
    surface = Color.Black,
    primaryContainer = PinkLaceDark.copy(alpha = 0.5f),
    surfaceVariant = PinkLaceDark.copy(alpha = 0.4f),
    tertiary = PinkLace,
    onTertiary = Color.Black,
    tertiaryContainer = PinkLaceDark.copy(alpha = 0.5f),
    surfaceTint = PinkLace,
    surfaceContainerLowest = DarkContainerLowest,
    surfaceContainerLow = DarkContainerLow,
    surfaceContainer = DarkContainer,
    surfaceContainerHigh = DarkContainerHigh,
    surfaceContainerHighest = DarkContainerHighest,
)
