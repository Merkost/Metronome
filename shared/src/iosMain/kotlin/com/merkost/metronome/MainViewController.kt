package com.merkost.metronome

import androidx.compose.ui.window.ComposeUIViewController
import com.merkost.metronome.navigation.AppNavigation
import com.merkost.metronome.platform.isDebug
import com.merkost.metronome.ui.theme.MetronomeTheme
import org.koin.compose.KoinContext
import platform.Foundation.NSProcessInfo

fun MainViewController() = ComposeUIViewController {
    KoinContext {
        MetronomeTheme {
            if (isDebug() && NSProcessInfo.processInfo.arguments.contains("--switch-motion-regression")) {
                SwitchMotionRegressionScreen()
            } else {
                AppNavigation()
            }
        }
    }
}
