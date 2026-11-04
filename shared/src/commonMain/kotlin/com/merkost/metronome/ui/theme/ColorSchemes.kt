package com.merkost.metronome.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

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

private val InkMutedLight = Color(0xFF595959)
private val InkMutedDark = Color(0xFFADADAD)
private val HairlineLight = Color.Black.copy(alpha = 0.12f)
private val HairlineDark = Color.White.copy(alpha = 0.12f)
private val OutlineLight = Color(0xFF757575)
private val OutlineDark = Color(0xFF8C8C8C)

private val DarkControlRest = Color(0xFF222222)
private val DarkInactiveTrack = Color(0xFF2B2B2B)
private val DarkLowBeat = Color(0xFF333333)

private val LightControlRest = Color(0xFFF2F2F2)
private val LightInactiveTrack = Color(0xFFE9E9E9)
private val LightLowBeat = Color(0xFFE4E4E4)

private val ErrorLight = Color(0xFFB3261E)
private val ErrorContainerLight = Color(0xFFF9DEDC)
private val OnErrorContainerLight = Color(0xFF410E0B)
private val ErrorDark = Color(0xFFF2B8B5)
private val OnErrorDark = Color(0xFF601410)
private val ErrorContainerDark = Color(0xFF8C1D18)
private val OnErrorContainerDark = Color(0xFFF9DEDC)

private fun deeperForLight(primary: Color): Color = lerp(primary, Color.Black, 0.30f)
private fun deeperForDark(primary: Color): Color = lerp(primary, Color.Black, 0.25f)

internal val BlackNWhiteDarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    onSurface = Color.White,
    inverseOnSurface = Color.Black,
    background = Color.Black,
    surface = Color.Black,
    primaryContainer = DarkLowBeat,
    surfaceVariant = DarkInactiveTrack,
    secondaryContainer = DarkControlRest,
    tertiary = InkMutedDark,
    onTertiary = Color.Black,
    tertiaryContainer = DarkLowBeat,
    onPrimaryContainer = Color.White,
    onSecondaryContainer = Color.White,
    onTertiaryContainer = Color.White,
    onSurfaceVariant = InkMutedDark,
    outline = OutlineDark,
    outlineVariant = HairlineDark,
    scrim = Color.Black,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
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
    onSurface = Color.Black,
    inverseOnSurface = Color.White,
    background = Color.White,
    surface = Color.White,
    primaryContainer = LightLowBeat,
    surfaceVariant = LightInactiveTrack,
    secondaryContainer = LightControlRest,
    tertiary = InkMutedLight,
    onTertiary = Color.White,
    tertiaryContainer = LightLowBeat,
    onPrimaryContainer = Color.Black,
    onSecondaryContainer = Color.Black,
    onTertiaryContainer = Color.Black,
    onSurfaceVariant = InkMutedLight,
    outline = OutlineLight,
    outlineVariant = HairlineLight,
    scrim = Color.Black,
    error = ErrorLight,
    onError = Color.White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
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
    onSurface = Color.White,
    inverseOnSurface = Color.Black,
    background = Color.Black,
    surface = Color.Black,
    primaryContainer = MelroseDark.copy(alpha = 0.5f),
    surfaceVariant = MelroseDark.copy(alpha = 0.4f),
    tertiary = deeperForDark(Melrose),
    onTertiary = Color.Black,
    tertiaryContainer = MelroseDark.copy(alpha = 0.5f),
    onPrimaryContainer = Color.White,
    onSecondaryContainer = Color.White,
    onTertiaryContainer = Color.White,
    onSurfaceVariant = InkMutedDark,
    outline = OutlineDark,
    outlineVariant = HairlineDark,
    scrim = Color.Black,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
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
    onSurface = Color.Black,
    inverseOnSurface = Color.White,
    background = Color.White,
    surface = Color.White,
    primaryContainer = Melrose.copy(alpha = 0.3f),
    surfaceVariant = Melrose.copy(alpha = 0.4f),
    tertiary = deeperForLight(MelroseDark),
    onTertiary = Color.White,
    tertiaryContainer = Melrose.copy(alpha = 0.3f),
    onPrimaryContainer = Color.Black,
    onSecondaryContainer = Color.Black,
    onTertiaryContainer = Color.Black,
    onSurfaceVariant = InkMutedLight,
    outline = OutlineLight,
    outlineVariant = HairlineLight,
    scrim = Color.Black,
    error = ErrorLight,
    onError = Color.White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
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
    onSurface = Color.Black,
    inverseOnSurface = Color.White,
    background = Color.White,
    surface = Color.White,
    primaryContainer = Periwinkle.copy(alpha = 0.3f),
    surfaceVariant = Periwinkle.copy(alpha = 0.4f),
    tertiary = deeperForLight(PeriwinkleDark),
    onTertiary = Color.White,
    tertiaryContainer = Periwinkle.copy(alpha = 0.3f),
    onPrimaryContainer = Color.Black,
    onSecondaryContainer = Color.Black,
    onTertiaryContainer = Color.Black,
    onSurfaceVariant = InkMutedLight,
    outline = OutlineLight,
    outlineVariant = HairlineLight,
    scrim = Color.Black,
    error = ErrorLight,
    onError = Color.White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
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
    onSurface = Color.White,
    inverseOnSurface = Color.Black,
    background = Color.Black,
    surface = Color.Black,
    primaryContainer = PeriwinkleDark.copy(alpha = 0.5f),
    surfaceVariant = PeriwinkleDark.copy(alpha = 0.4f),
    tertiary = deeperForDark(Periwinkle),
    onTertiary = Color.Black,
    tertiaryContainer = PeriwinkleDark.copy(alpha = 0.5f),
    onPrimaryContainer = Color.White,
    onSecondaryContainer = Color.White,
    onTertiaryContainer = Color.White,
    onSurfaceVariant = InkMutedDark,
    outline = OutlineDark,
    outlineVariant = HairlineDark,
    scrim = Color.Black,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
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
    onSurface = Color.Black,
    inverseOnSurface = Color.White,
    background = Color.White,
    surface = Color.White,
    primaryContainer = MintGreen.copy(alpha = 0.3f),
    surfaceVariant = MintGreen.copy(alpha = 0.4f),
    tertiary = deeperForLight(MintGreenDark),
    onTertiary = Color.White,
    tertiaryContainer = MintGreen.copy(alpha = 0.3f),
    onPrimaryContainer = Color.Black,
    onSecondaryContainer = Color.Black,
    onTertiaryContainer = Color.Black,
    onSurfaceVariant = InkMutedLight,
    outline = OutlineLight,
    outlineVariant = HairlineLight,
    scrim = Color.Black,
    error = ErrorLight,
    onError = Color.White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
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
    onSurface = Color.White,
    inverseOnSurface = Color.Black,
    background = Color.Black,
    surface = Color.Black,
    primaryContainer = MintGreenDark.copy(alpha = 0.5f),
    surfaceVariant = MintGreenDark.copy(alpha = 0.4f),
    tertiary = deeperForDark(MintGreen),
    onTertiary = Color.Black,
    tertiaryContainer = MintGreenDark.copy(alpha = 0.5f),
    onPrimaryContainer = Color.White,
    onSecondaryContainer = Color.White,
    onTertiaryContainer = Color.White,
    onSurfaceVariant = InkMutedDark,
    outline = OutlineDark,
    outlineVariant = HairlineDark,
    scrim = Color.Black,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
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
    onSurface = Color.Black,
    inverseOnSurface = Color.White,
    background = Color.White,
    surface = Color.White,
    primaryContainer = PinkLace.copy(alpha = 0.3f),
    surfaceVariant = PinkLace.copy(alpha = 0.4f),
    tertiary = deeperForLight(PinkLaceDark),
    onTertiary = Color.White,
    tertiaryContainer = PinkLace.copy(alpha = 0.3f),
    onPrimaryContainer = Color.Black,
    onSecondaryContainer = Color.Black,
    onTertiaryContainer = Color.Black,
    onSurfaceVariant = InkMutedLight,
    outline = OutlineLight,
    outlineVariant = HairlineLight,
    scrim = Color.Black,
    error = ErrorLight,
    onError = Color.White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
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
    onSurface = Color.White,
    inverseOnSurface = Color.Black,
    background = Color.Black,
    surface = Color.Black,
    primaryContainer = PinkLaceDark.copy(alpha = 0.5f),
    surfaceVariant = PinkLaceDark.copy(alpha = 0.4f),
    tertiary = deeperForDark(PinkLace),
    onTertiary = Color.Black,
    tertiaryContainer = PinkLaceDark.copy(alpha = 0.5f),
    onPrimaryContainer = Color.White,
    onSecondaryContainer = Color.White,
    onTertiaryContainer = Color.White,
    onSurfaceVariant = InkMutedDark,
    outline = OutlineDark,
    outlineVariant = HairlineDark,
    scrim = Color.Black,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    surfaceTint = PinkLace,
    surfaceContainerLowest = DarkContainerLowest,
    surfaceContainerLow = DarkContainerLow,
    surfaceContainer = DarkContainer,
    surfaceContainerHigh = DarkContainerHigh,
    surfaceContainerHighest = DarkContainerHighest,
)
