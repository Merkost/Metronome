package com.merkost.metronome.desktop

import com.merkost.metronome.navigation.LocalWindowTitleInset
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
import androidx.compose.ui.window.WindowPlacement
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
import metronome.shared.generated.resources.app_name
import metronome.shared.generated.resources.menu_view
import metronome.shared.generated.resources.shell_presets
import metronome.shared.generated.resources.practice_sets
import metronome.shared.generated.resources.menu_playback
import metronome.shared.generated.resources.menu_stop
import metronome.shared.generated.resources.menu_start
import metronome.shared.generated.resources.menu_tap_tempo
import metronome.shared.generated.resources.menu_faster
import metronome.shared.generated.resources.menu_slower
import metronome.shared.generated.resources.menu_window
import metronome.shared.generated.resources.menu_close
import metronome.shared.generated.resources.menu_minimize
import metronome.shared.generated.resources.menu_zoom
import metronome.shared.generated.resources.menu_help
import metronome.shared.generated.resources.menu_support
import metronome.shared.generated.resources.menu_rate
import metronome.shared.generated.resources.about_dialog_text
import metronome.shared.generated.resources.done
import metronome.shared.generated.resources.shell_audio_error_title
import metronome.shared.generated.resources.shell_audio_error_fallback
import metronome.shared.generated.resources.common_ok
import metronome.shared.generated.resources.shell_quit_error_title
import metronome.shared.generated.resources.shell_quit_error_message
import metronome.shared.generated.resources.common_try_again
import metronome.shared.generated.resources.shell_quit_keep_open
import metronome.shared.generated.resources.Res
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.context.startKoin

private class AudioFailure(val message: String?)

fun main() {
    System.setProperty("apple.awt.application.name", "Metronome")
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    startKoin { modules(commonModule, desktopModule) }

    application {
        val commands = remember { MutableSharedFlow<AppCommand>(extraBufferCapacity = 16) }
        var visible by remember { mutableStateOf(true) }
        var showAbout by remember { mutableStateOf(false) }
        var audioError by remember { mutableStateOf<AudioFailure?>(null) }
        var quitError by remember { mutableStateOf(false) }
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
                    quitError = true
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
                audioError = AudioFailure(error.message)
            }
        }

        val unifiedTitleBar = System.getProperty("os.name").orEmpty().startsWith("Mac")
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
                if (unifiedTitleBar) {
                    window.rootPane.putClientProperty("apple.awt.fullWindowContent", true)
                    window.rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
                    window.rootPane.putClientProperty("apple.awt.windowTitleVisible", false)
                }
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

            val appName = stringResource(Res.string.app_name)
            val viewMenu = stringResource(Res.string.menu_view)
            val presetsItem = stringResource(Res.string.shell_presets)
            val practiceSetsItem = stringResource(Res.string.practice_sets)
            val playbackMenu = stringResource(Res.string.menu_playback)
            val playbackToggleItem = stringResource(if (playing) Res.string.menu_stop else Res.string.menu_start)
            val tapTempoItem = stringResource(Res.string.menu_tap_tempo)
            val fasterItem = stringResource(Res.string.menu_faster)
            val slowerItem = stringResource(Res.string.menu_slower)
            val windowMenu = stringResource(Res.string.menu_window)
            val closeItem = stringResource(Res.string.menu_close)
            val minimizeItem = stringResource(Res.string.menu_minimize)
            val zoomItem = stringResource(Res.string.menu_zoom)
            val helpMenu = stringResource(Res.string.menu_help)
            val supportItem = stringResource(Res.string.menu_support)
            val rateItem = stringResource(Res.string.menu_rate)

            MenuBar {
                Menu(viewMenu, enabled = !quitting) {
                    Item(appName, shortcut = KeyShortcut(Key.One, meta = true), enabled = ready) {
                        navigate(MainDestination.Main)
                    }
                    Item(presetsItem, shortcut = KeyShortcut(Key.Two, meta = true), enabled = ready) {
                        navigate(MainDestination.PracticePresets)
                    }
                    Item(practiceSetsItem, shortcut = KeyShortcut(Key.Three, meta = true), enabled = ready) {
                        navigate(MainDestination.PracticeSets)
                    }
                }
                Menu(playbackMenu, enabled = !quitting) {
                    Item(
                        playbackToggleItem,
                        shortcut = KeyShortcut(Key.Enter, meta = true),
                        enabled = ready,
                        onClick = { viewModel.onPlayPauseClicked(viewModel.isPlaying.value) },
                    )
                    Item(tapTempoItem, shortcut = KeyShortcut(Key.T, meta = true), enabled = ready) {
                        viewModel.onTempoTap()
                    }
                    Separator()
                    Item(fasterItem, shortcut = KeyShortcut(Key.Equals, meta = true), enabled = ready) {
                        viewModel.onSliderValueIncreased()
                    }
                    Item(slowerItem, shortcut = KeyShortcut(Key.Minus, meta = true), enabled = ready) {
                        viewModel.onSliderValueDecreased()
                    }
                }
                Menu(windowMenu) {
                    Item(closeItem, shortcut = KeyShortcut(Key.W, meta = true)) {
                        visible = false
                    }
                    Item(minimizeItem, shortcut = KeyShortcut(Key.M, meta = true)) {
                        windowState.isMinimized = true
                    }
                    Item(zoomItem) {
                        windowState.placement =
                            if (windowState.placement == WindowPlacement.Maximized) WindowPlacement.Floating
                            else WindowPlacement.Maximized
                    }
                    Separator()
                    Item(appName, enabled = !quitting) { reveal() }
                }
                Menu(helpMenu) {
                    Item(supportItem, onClick = actions::contactSupport)
                    Item(rateItem, onClick = actions::rateApp)
                }
            }

            KoinContext {
                CompositionLocalProvider(
                    LocalAppPresentation provides AppPresentation.Desktop,
                    LocalAppCommands provides commands,
                    LocalWindowTitleInset provides if (unifiedTitleBar) 28.dp else 0.dp,
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
                                    Text(stringResource(Res.string.about_dialog_text, version?.versionName.orEmpty(), version?.versionNumber ?: 1))
                                },
                                confirmButton = {
                                    TextButton(onClick = { showAbout = false }) { Text(stringResource(Res.string.done)) }
                                },
                            )
                        }
                        audioError?.let { failure ->
                            AlertDialog(
                                onDismissRequest = { audioError = null },
                                title = { Text(stringResource(Res.string.shell_audio_error_title)) },
                                text = { Text(failure.message ?: stringResource(Res.string.shell_audio_error_fallback)) },
                                confirmButton = {
                                    TextButton(onClick = { audioError = null }) { Text(stringResource(Res.string.common_ok)) }
                                },
                            )
                        }
                        if (quitError) {
                            AlertDialog(
                                onDismissRequest = { quitError = false },
                                title = { Text(stringResource(Res.string.shell_quit_error_title)) },
                                text = { Text(stringResource(Res.string.shell_quit_error_message)) },
                                confirmButton = {
                                    TextButton(onClick = { quitError = false; quit() }) { Text(stringResource(Res.string.common_try_again)) }
                                },
                                dismissButton = {
                                    TextButton(onClick = { quitError = false }) { Text(stringResource(Res.string.shell_quit_keep_open)) }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
