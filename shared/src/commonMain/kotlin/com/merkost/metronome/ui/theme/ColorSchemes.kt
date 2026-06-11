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
)

internal val PurpleDarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

internal val PurpleLightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
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
)
