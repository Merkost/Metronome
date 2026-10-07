package com.merkost.metronome.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.compositeOver
import com.merkost.metronome.ui.AppAnimations
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.merkost.metronome.model.AppDatastore
import com.merkost.metronome.model.ThemeMode
import com.merkost.metronome.platform.resolveDynamicColorScheme
import org.koin.compose.koinInject

@Composable
fun MetronomeTheme(
    content: @Composable () -> Unit
) {
    val appDatastore: AppDatastore = koinInject()
    val selectedColorScheme by appDatastore.colorScheme.collectAsState(AppColorScheme.BLACKNWHITE)
    val themeMode by appDatastore.themeMode.collectAsState(ThemeMode.SYSTEM)
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        selectedColorScheme == AppColorScheme.MATERIAL3 -> {
            resolveDynamicColorScheme(darkTheme)
                ?: if (darkTheme) selectedColorScheme.darkColor else selectedColorScheme.lightColor
        }
        else -> if (darkTheme) selectedColorScheme.darkColor else selectedColorScheme.lightColor
    }

    val brandedScheme = if (selectedColorScheme == AppColorScheme.MATERIAL3) colorScheme else colorScheme.copy(
        secondary = if (darkTheme) Melrose else MelroseDark,
        onSecondary = if (darkTheme) colorScheme.surface else colorScheme.inverseOnSurface,
        secondaryContainer = if (darkTheme) MelroseDark.copy(alpha = 0.5f) else Melrose,
        onSecondaryContainer = colorScheme.onSurface,
        tertiaryContainer = if (darkTheme) PeriwinkleDark.copy(alpha = 0.5f) else Periwinkle,
        tertiaryFixed = MintGreen,
        onTertiaryFixed = if (darkTheme) colorScheme.surface else colorScheme.onSurface,
        onTertiaryContainer = colorScheme.onSurface,
    )

    val fontFamily = appFontFamily()
    val typography = remember(fontFamily) {
        fontFamily?.let { Typography.withFontFamily(it) } ?: Typography
    }

    MaterialTheme(
        colorScheme = animatedColorScheme(brandedScheme),
        typography = typography,
        content = content
    )
}

@Composable
private fun animatedColorScheme(target: ColorScheme): ColorScheme {
    val animated = target.copy(
    primary = animatedThemeColor(target.primary, "primary"),
    onPrimary = animatedThemeColor(target.onPrimary, "onPrimary"),
    primaryContainer = animatedThemeColor(target.primaryContainer, "primaryContainer"),
    onPrimaryContainer = animatedThemeColor(target.onPrimaryContainer, "onPrimaryContainer"),
    inversePrimary = animatedThemeColor(target.inversePrimary, "inversePrimary"),
    secondary = animatedThemeColor(target.secondary, "secondary"),
    onSecondary = animatedThemeColor(target.onSecondary, "onSecondary"),
    secondaryContainer = animatedThemeColor(target.secondaryContainer, "secondaryContainer"),
    onSecondaryContainer = animatedThemeColor(target.onSecondaryContainer, "onSecondaryContainer"),
    tertiary = animatedThemeColor(target.tertiary, "tertiary"),
    onTertiary = animatedThemeColor(target.onTertiary, "onTertiary"),
    tertiaryContainer = animatedThemeColor(target.tertiaryContainer, "tertiaryContainer"),
    tertiaryFixed = animatedThemeColor(target.tertiaryFixed, "tertiaryFixed"),
    onTertiaryFixed = animatedThemeColor(target.onTertiaryFixed, "onTertiaryFixed"),
    onTertiaryContainer = animatedThemeColor(target.onTertiaryContainer, "onTertiaryContainer"),
    background = animatedThemeColor(target.background, "background"),
    onBackground = animatedThemeColor(target.onBackground, "onBackground"),
    surface = animatedThemeColor(target.surface, "surface"),
    onSurface = animatedThemeColor(target.onSurface, "onSurface"),
    surfaceVariant = animatedThemeColor(target.surfaceVariant, "surfaceVariant"),
    onSurfaceVariant = animatedThemeColor(target.onSurfaceVariant, "onSurfaceVariant"),
    surfaceTint = animatedThemeColor(target.surfaceTint, "surfaceTint"),
    inverseSurface = animatedThemeColor(target.inverseSurface, "inverseSurface"),
    inverseOnSurface = animatedThemeColor(target.inverseOnSurface, "inverseOnSurface"),
    error = animatedThemeColor(target.error, "error"),
    onError = animatedThemeColor(target.onError, "onError"),
    errorContainer = animatedThemeColor(target.errorContainer, "errorContainer"),
    onErrorContainer = animatedThemeColor(target.onErrorContainer, "onErrorContainer"),
    outline = animatedThemeColor(target.outline, "outline"),
    outlineVariant = animatedThemeColor(target.outlineVariant, "outlineVariant"),
    scrim = animatedThemeColor(target.scrim, "scrim"),
    surfaceBright = animatedThemeColor(target.surfaceBright, "surfaceBright"),
    surfaceDim = animatedThemeColor(target.surfaceDim, "surfaceDim"),
    surfaceContainer = animatedThemeColor(target.surfaceContainer, "surfaceContainer"),
    surfaceContainerHigh = animatedThemeColor(target.surfaceContainerHigh, "surfaceContainerHigh"),
    surfaceContainerHighest = animatedThemeColor(target.surfaceContainerHighest, "surfaceContainerHighest"),
    surfaceContainerLow = animatedThemeColor(target.surfaceContainerLow, "surfaceContainerLow"),
    surfaceContainerLowest = animatedThemeColor(target.surfaceContainerLowest, "surfaceContainerLowest"),
    )
    fun readable(foreground: Color, background: Color): Color = readableThemeColor(
        foreground,
        background.compositeOver(animated.surface),
        target.onSurface,
        target.inverseOnSurface,
    )
    return animated.copy(
        onPrimary = readable(animated.onPrimary, animated.primary),
        onPrimaryContainer = readable(animated.onPrimaryContainer, animated.primaryContainer),
        onSecondary = readable(animated.onSecondary, animated.secondary),
        onSecondaryContainer = readable(animated.onSecondaryContainer, animated.secondaryContainer),
        onTertiary = readable(animated.onTertiary, animated.tertiary),
        onTertiaryContainer = readable(animated.onTertiaryContainer, animated.tertiaryContainer),
        onBackground = readable(animated.onBackground, animated.background),
        onSurface = readable(animated.onSurface, animated.surface),
        onSurfaceVariant = readable(animated.onSurfaceVariant, animated.surfaceVariant),
        inverseOnSurface = readable(animated.inverseOnSurface, animated.inverseSurface),
        onError = readable(animated.onError, animated.error),
        onErrorContainer = readable(animated.onErrorContainer, animated.errorContainer),
    )
}

internal fun readableThemeColor(foreground: Color, background: Color, first: Color, second: Color): Color {
    fun contrast(color: Color): Float {
        val luminance = color.luminance()
        val surface = background.luminance()
        return (maxOf(luminance, surface) + 0.05f) / (minOf(luminance, surface) + 0.05f)
    }
    if (contrast(foreground) >= 4.5f) return foreground
    val preferred = if (contrast(first) >= contrast(second)) first else second
    if (contrast(preferred) >= 4.5f) return preferred
    return if (contrast(ContrastForegroundDark) >= contrast(ContrastForegroundLight)) ContrastForegroundDark else ContrastForegroundLight
}

@Composable
private fun animatedThemeColor(target: Color, label: String): Color {
    val color by animateColorAsState(target, AppAnimations.emphasized(), label = "theme-$label")
    return color
}
