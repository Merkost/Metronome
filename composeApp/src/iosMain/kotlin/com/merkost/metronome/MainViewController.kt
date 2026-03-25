package com.merkost.metronome

import androidx.compose.ui.window.ComposeUIViewController
import com.merkost.metronome.navigation.AppNavigation
import com.merkost.metronome.ui.theme.MetronomeTheme

fun MainViewController() = ComposeUIViewController {
    MetronomeTheme {
        AppNavigation()
    }
}
