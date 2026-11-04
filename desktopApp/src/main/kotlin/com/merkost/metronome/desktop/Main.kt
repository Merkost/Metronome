package com.merkost.metronome.desktop

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.merkost.metronome.di.commonModule
import com.merkost.metronome.di.desktopModule
import com.merkost.metronome.engine.MetronomeEngine
import com.merkost.metronome.engine.MetronomePlayer
import com.merkost.metronome.engine.SoundPreviewController
import com.merkost.metronome.navigation.AppCommand
import com.merkost.metronome.navigation.AppNavigation
import com.merkost.metronome.navigation.AppPresentation
import com.merkost.metronome.navigation.LocalAppCommands
import com.merkost.metronome.navigation.LocalAppPresentation
import com.merkost.metronome.navigation.MainDestination
import com.merkost.metronome.platform.PlatformActions
import com.merkost.metronome.platform.AppVersionProvider
import com.merkost.metronome.platform.DesktopPersistence
import com.merkost.metronome.ui.theme.MetronomeTheme
import com.merkost.metronome.viewModels.MetronomeViewModel
import java.awt.Desktop
import java.awt.Dimension
import java.awt.Taskbar
import javax.imageio.ImageIO
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import org.koin.core.context.startKoin

fun main() {
    System.setProperty("apple.awt.application.name", "Metronome")
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    startKoin { modules(commonModule, desktopModule) }

    application {
        val commands = remember { MutableSharedFlow<AppCommand>(extraBufferCapacity = 16) }
        var visible by remember { mutableStateOf(true) }
        var showAbout by remember { mutableStateOf(false) }
        var audioError by remember { mutableStateOf<String?>(null) }
        var quitError by remember { mutableStateOf<String?>(null) }
        var quitting by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        val windowState = rememberWindowState(width = 1000.dp, height = 780.dp)
        val mainWindow = remember { AtomicReference<ComposeWindow?>() }
        val viewModel = koinInject<MetronomeViewModel>()
        val player = koinInject<MetronomePlayer>()
        val preview = koinInject<SoundPreviewController>()
        val engine = koinInject<MetronomeEngine>()
        val actions = koinInject<PlatformActions>()
        val persistence = koinInject<DesktopPersistence>()
        val version = koinInject<AppVersionProvider>().getAppVersion()
        val onboardingLoaded by viewModel.onboardingLoaded.collectAsState()
        val onboardingVisible by viewModel.onboardingVisible.collectAsState()
        val playing by viewModel.isPlaying.collectAsState()
        val ready = onboardingLoaded && !onboardingVisible
        val currentReady by rememberUpdatedState(ready)

        fun reveal() {
            visible = true
            windowState.isMinimized = false
            mainWindow.get()?.let {
                it.toFront()
                it.requestFocus()
            }
        }

        fun navigate(destination: MainDestination) {
            reveal()
            commands.tryEmit(AppCommand.Navigate(destination))
        }

        fun quit() {
            if (quitting) return
            quitting = true
            preview.stop()
            scope.launch {
                try {
                    withTimeout(10_000L) {
                        viewModel.prepareForApplicationExit()
                        persistence.awaitPendingWrites()
                    }
                    engine.release()
                    exitApplication()
                } catch (error: Exception) {
                    if (error is CancellationException && error !is kotlinx.coroutines.TimeoutCancellationException) throw error
                    reveal()
                    quitting = false
                    quitError = "Your latest practice progress couldn’t be saved. Please try quitting again."
                }
            }
        }

        fun requestQuit() {
            if (quitting) return
            reveal()
            if (currentReady) commands.tryEmit(AppCommand.RequestExit(::quit)) else quit()
        }

        LaunchedEffect(player) {
            player.failures.collect { error ->
                viewModel.onStopClicked()
                audioError = error.message ?: "Check the Mac's sound output, then try playback again."
            }
        }

        Window(
            onCloseRequest = { visible = false },
            title = "Metronome",
            visible = visible,
            state = windowState,
        ) {
            LaunchedEffect(window, quitting) {
                window.isEnabled = !quitting
            }
            DisposableEffect(window) {
                mainWindow.set(window)
                window.minimumSize = Dimension(520, 600)
                if (Taskbar.isTaskbarSupported()) {
                    val taskbar = Taskbar.getTaskbar()
                    if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                        Thread.currentThread().contextClassLoader.getResourceAsStream("metronome.png")?.use {
                            taskbar.iconImage = ImageIO.read(it)
                        }
                    }
                }
                val desktop = Desktop.getDesktop()
                if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
                    desktop.setAboutHandler {
                        reveal()
                        showAbout = true
                    }
                }
                if (desktop.isSupported(Desktop.Action.APP_PREFERENCES)) {
                    desktop.setPreferencesHandler { if (currentReady) navigate(MainDestination.Settings) }
                }
                if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
                    desktop.setQuitHandler { _, response ->
                        response.cancelQuit()
                        requestQuit()
                    }
                }
                val reopen = java.awt.desktop.AppReopenedListener {
                    reveal()
                }
                desktop.addAppEventListener(reopen)
                onDispose {
                    mainWindow.compareAndSet(window, null)
                    desktop.removeAppEventListener(reopen)
                }
            }

            MenuBar {
                Menu("View", enabled = !quitting) {
                    Item("Metronome", shortcut = KeyShortcut(Key.One, meta = true), enabled = ready) {
                        navigate(MainDestination.Main)
                    }
                    Item("Saved tempos", shortcut = KeyShortcut(Key.Two, meta = true), enabled = ready) {
                        navigate(MainDestination.PracticePresets)
                    }
                    Item("Practice sets", shortcut = KeyShortcut(Key.Three, meta = true), enabled = ready) {
                        navigate(MainDestination.PracticeSets)
                    }
                    Separator()
                    Item("Settings…", shortcut = KeyShortcut(Key.Comma, meta = true), enabled = ready) {
                        navigate(MainDestination.Settings)
                    }
                }
                Menu("Playback", enabled = !quitting) {
                    Item(
                        if (playing) "Stop" else "Start",
                        shortcut = KeyShortcut(Key.Enter, meta = true),
                        enabled = ready,
                        onClick = { viewModel.onPlayPauseClicked(viewModel.isPlaying.value) },
                    )
                    Item("Tap tempo", shortcut = KeyShortcut(Key.T, meta = true), enabled = ready) {
                        viewModel.onTempoTap()
                    }
                }
                Menu("Help") {
                    Item("Metronome support", onClick = actions::contactSupport)
                    Item("Rate Metronome", onClick = actions::rateApp)
                    Separator()
                    Item("About Metronome") {
                        reveal()
                        showAbout = true
                    }
                }
            }

            KoinContext {
                CompositionLocalProvider(
                    LocalAppPresentation provides AppPresentation.Desktop,
                    LocalAppCommands provides commands,
                ) {
                    MetronomeTheme {
                        Surface(color = MaterialTheme.colorScheme.surface) {
                            AppNavigation()
                        }
                        if (showAbout) {
                            AlertDialog(
                                onDismissRequest = { showAbout = false },
                                title = { Text("Metronome") },
                                text = {
                                    Text("Precise timing. Better practice.\n\nVersion ${version?.versionName.orEmpty()} (${version?.versionNumber ?: 1})\n\nSpace: start or stop\n↑ / ↓: adjust tempo\n⌘1 / ⌘2 / ⌘3: navigate\n⌘,: Settings\n\nMade by Merkost")
                                },
                                confirmButton = {
                                    TextButton(onClick = { showAbout = false }) { Text("Done") }
                                },
                            )
                        }
                        audioError?.let { message ->
                            AlertDialog(
                                onDismissRequest = { audioError = null },
                                title = { Text("Sound output unavailable") },
                                text = { Text(message) },
                                confirmButton = {
                                    TextButton(onClick = { audioError = null }) { Text("OK") }
                                },
                            )
                        }
                        quitError?.let { message ->
                            AlertDialog(
                                onDismissRequest = { quitError = null },
                                title = { Text("Couldn’t finish saving") },
                                text = { Text(message) },
                                confirmButton = {
                                    TextButton(onClick = { quitError = null; quit() }) { Text("Try again") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { quitError = null }) { Text("Keep open") }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
