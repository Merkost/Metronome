package com.merkost.metronome.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
expect fun resolveDynamicColorScheme(darkTheme: Boolean): ColorScheme?
