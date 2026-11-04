package com.merkost.metronome.platform

import android.view.Window
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.core.view.WindowCompat

@Composable
fun SystemBarAppearance(window: Window) {
    val darkIcons = MaterialTheme.colorScheme.onSurface.luminance() < MaterialTheme.colorScheme.surface.luminance()
    SideEffect {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = darkIcons
            isAppearanceLightNavigationBars = darkIcons
        }
    }
}
