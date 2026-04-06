package com.merkost.metronome.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.merkost.metronome.model.AppDatastore
import com.merkost.metronome.platform.resolveDynamicColorScheme
import org.koin.compose.koinInject

@Composable
fun MetronomeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val appDatastore: AppDatastore = koinInject()
    val selectedColorScheme by appDatastore.colorScheme.collectAsState(AppColorScheme.BLACKNWHITE)

    val colorScheme = when {
        selectedColorScheme == AppColorScheme.MATERIAL3 -> {
            resolveDynamicColorScheme(darkTheme)
                ?: if (darkTheme) selectedColorScheme.darkColor else selectedColorScheme.lightColor
        }
        else -> if (darkTheme) selectedColorScheme.darkColor else selectedColorScheme.lightColor
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
