package com.merkost.metronome

import androidx.compose.ui.window.ComposeUIViewController
import com.merkost.metronome.di.commonModule
import com.merkost.metronome.di.iosModule
import com.merkost.metronome.navigation.AppNavigation
import com.merkost.metronome.ui.theme.MetronomeTheme
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

fun MainViewController() = ComposeUIViewController {
    KoinApplication(configuration = koinConfiguration(declaration = {
        modules(
            commonModule,
            iosModule
        )
    }), content = {
        MetronomeTheme {
            AppNavigation()
        }
    })
}
