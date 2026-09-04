package com.merkost.metronome

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.merkost.metronome.di.commonModule
import com.merkost.metronome.di.wasmJsModule
import com.merkost.metronome.navigation.AppNavigation
import com.merkost.metronome.ui.theme.MetronomeTheme
import org.koin.compose.KoinContext
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin { modules(commonModule, wasmJsModule) }
    ComposeViewport {
        KoinContext {
            MetronomeTheme {
                AppNavigation()
            }
        }
    }
}
