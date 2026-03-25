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
)

internal val BlackNWhiteLightColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    background = Color.White,
    surface = Color.White,
    primaryContainer = Color.LightGray.copy(0.5f),
    surfaceVariant = Color.LightGray.copy(0.5f),
    secondaryContainer = Color.LightGray.copy(0.5f),
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
    primary = Periwinkle,
    onPrimary = Color.White,
    background = Color.White,
    surface = Color.White,
    primaryContainer = Periwinkle.copy(alpha = 0.7f),
    surfaceVariant = Periwinkle.copy(alpha = 0.7f)
)

internal val PeriwinkleDarkColorScheme = darkColorScheme(
    primary = Periwinkle,
    onPrimary = Color.Black,
    background = Color.Black,
    surface = Color.Black,
    primaryContainer = Periwinkle.copy(alpha = 0.7f),
    surfaceVariant = Periwinkle.copy(alpha = 0.7f)
)

internal val MintGreenLightColorScheme = lightColorScheme(
    primary = MintGreen,
    onPrimary = Color.White,
    background = Color.White,
    surface = Color.White,
    primaryContainer = MintGreen.copy(alpha = 0.7f),
    surfaceVariant = MintGreen.copy(alpha = 0.7f)
)

internal val MintGreenDarkColorScheme = darkColorScheme(
    primary = MintGreen,
    onPrimary = Color.Black,
    background = Color.Black,
    surface = Color.Black,
    primaryContainer = MintGreen.copy(alpha = 0.7f),
    surfaceVariant = MintGreen.copy(alpha = 0.7f)
)

internal val PinkLaceLightColorScheme = lightColorScheme(
    primary = PinkLace,
    onPrimary = Color.White,
    background = Color.White,
    surface = Color.White,
    primaryContainer = PinkLace.copy(alpha = 0.7f),
    surfaceVariant = PinkLace.copy(alpha = 0.7f)
)

internal val PinkLaceDarkColorScheme = darkColorScheme(
    primary = PinkLace,
    onPrimary = Color.Black,
    background = Color.Black,
    surface = Color.Black,
    primaryContainer = PinkLace.copy(alpha = 0.7f),
    surfaceVariant = PinkLace.copy(alpha = 0.7f)
)
