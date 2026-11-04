package com.merkost.metronome.screens

import com.composables.icons.lucide.ChartNoAxesColumn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.ArrowRight
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.composables.icons.lucide.CircleHelp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.SlidersHorizontal
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.Timer
import com.merkost.metronome.components.AppChip
import com.merkost.metronome.components.AppDialog
import com.merkost.metronome.components.AppIconButton
import com.merkost.metronome.components.TimestampMillisecondsFormatter
import com.merkost.metronome.engine.SoundPreviewController
import com.merkost.metronome.navigation.AppPresentation
import com.merkost.metronome.navigation.LocalAppPresentation
import com.merkost.metronome.model.BeatDisplayStyle
import com.merkost.metronome.model.ClickSound
import com.merkost.metronome.model.ThemeMode
import com.merkost.metronome.platform.AppVersionProvider
import com.merkost.metronome.platform.PlatformActions
import com.merkost.metronome.ui.horizontalPadding
import com.merkost.metronome.ui.maxContentWidth
import com.merkost.metronome.ui.spacingLarge
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall
import com.merkost.metronome.ui.theme.AppColorScheme
import com.merkost.metronome.model.practiceSummaryLine
import com.merkost.metronome.viewModels.MetronomeViewModel
import com.merkost.metronome.viewModels.SettingsViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

internal data class SettingsUiState(
    val colorScheme: AppColorScheme = AppColorScheme.BLACKNWHITE,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val colorFlash: Boolean = false,
    val backgroundPlay: Boolean = false,
    val hapticEnabled: Boolean = false,
    val keepScreenAwake: Boolean = true,
    val countInEnabled: Boolean = false,
    val liveActivityEnabled: Boolean = true,
    val beatDisplayStyle: BeatDisplayStyle = BeatDisplayStyle.DOTS,
    val stereo: Int = 0,
    val volume: Float = 1f,
    val selectedSound: ClickSound = ClickSound.WOOD,
    val previewSound: ClickSound? = null,
    val playing: Boolean = false,
    val supportsDynamicColor: Boolean = false,
    val supportsHaptics: Boolean = true,
    val version: String = "",
    val previewError: String? = null,
    val practiceSummary: String? = null,
)

internal data class SettingsActions(
    val onColorScheme: (AppColorScheme) -> Unit = {},
    val onTheme: (ThemeMode) -> Unit = {},
    val onColorFlash: (Boolean) -> Unit = {},
    val onBackgroundPlay: (Boolean) -> Unit = {},
    val onHaptic: (Boolean) -> Unit = {},
    val onKeepScreenAwake: (Boolean) -> Unit = {},
    val onCountIn: (Boolean) -> Unit = {},
    val onLiveActivity: (Boolean) -> Unit = {},
    val onBeatDisplay: (BeatDisplayStyle) -> Unit = {},
    val onStereo: (Float) -> Unit = {},
    val onVolume: (Float) -> Unit = {},
    val onSoundPicker: () -> Unit = {},
    val onPreview: () -> Unit = {},
    val onPractice: () -> Unit = {},
    val onWhatsNew: () -> Unit = {},
    val onAbout: () -> Unit = {},
    val onWeb: () -> Unit = {},
    val onSuby: () -> Unit = {},
)

@Composable
fun SettingsScreen(upPress: () -> Unit) {
    val viewModel: SettingsViewModel = koinViewModel()
    val metronome: MetronomeViewModel = koinInject()
    val platformActions: PlatformActions = koinInject()
    val appVersionProvider: AppVersionProvider = koinInject()
    val soundPreview: SoundPreviewController = koinInject()
    val uriHandler = LocalUriHandler.current
    val desktop = LocalAppPresentation.current == AppPresentation.Desktop
    val colorScheme by viewModel.colorScheme.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val colorFlash by viewModel.colorFlash.collectAsState()
    val backgroundPlay by viewModel.backgroundPlay.collectAsState()
    val hapticEnabled by viewModel.hapticEnabled.collectAsState()
    val keepScreenAwake by viewModel.keepScreenAwake.collectAsState()
    val countInEnabled by viewModel.countInEnabled.collectAsState()
    val liveActivityEnabled by viewModel.liveActivityEnabled.collectAsState()
    val beatDisplayStyle by viewModel.beatDisplayStyle.collectAsState()
    val totalTime by viewModel.totalTime.collectAsState()
    val todayTime by viewModel.todayTime.collectAsState()
    val practiceStreak by viewModel.practiceStreak.collectAsState()
    val currentStereo by viewModel.currentStereo.collectAsState()
    val clickVolume by viewModel.clickVolume.collectAsState()
    val selectedSound by viewModel.selectedSound.collectAsState()
    val previewSound by soundPreview.activeSound.collectAsState()
    val previewError by soundPreview.errorMessage.collectAsState()
    val playing by metronome.isPlaying.collectAsState()
    var activeSheet by rememberSaveable { mutableStateOf<String?>(null) }
    var showResetConfirmation by rememberSaveable { mutableStateOf(false) }
    var showBackgroundPlayPermission by rememberSaveable { mutableStateOf(false) }
    val version = appVersionProvider.getAppVersion()?.versionName.orEmpty()

    DisposableEffect(soundPreview) {
        onDispose { soundPreview.stop() }
    }
    LaunchedEffect(playing) {
        if (playing) soundPreview.stop()
    }
    LaunchedEffect(clickVolume, currentStereo) {
        soundPreview.updateOutput(clickVolume, currentStereo)
    }
    if (!desktop && showBackgroundPlayPermission && backgroundPlay) {
        BackgroundPlayPermissionCheck(true)
    }

    val preview: (ClickSound) -> Unit = { sound ->
        if (previewSound == sound) soundPreview.stop()
        else if (!playing) soundPreview.preview(sound, volume = clickVolume, pan = currentStereo)
    }
    when (activeSheet) {
        "sounds" -> SoundPickerSheet(
            selectedSound = selectedSound,
            previewSound = previewSound,
            errorMessage = previewError,
            previewEnabled = !playing,
            onSelect = { soundPreview.stop(); viewModel.onSoundChanged(it) },
            onPreview = preview,
            onDismiss = {
                soundPreview.stop()
                activeSheet = null
            },
        )
        "practice" -> SettingsPracticeSheet(
            todayTime = todayTime,
            totalTime = totalTime,
            practiceStreak = practiceStreak,
            playing = playing,
            onReset = { showResetConfirmation = true },
            onDismiss = { activeSheet = null },
        )
        "about" -> SettingsAboutSheet(
            version = version,
            onGuide = { activeSheet = "guide" },
            onWhatsNew = { activeSheet = "whats-new" },
            onContact = platformActions::contactSupport,
            onRate = platformActions::rateApp,
            onWebsite = { uriHandler.openUri("https://metronome.merkost.dev/") },
            onPrivacy = { uriHandler.openUri("https://metronome.merkost.dev/privacy.html") },
            onDismiss = { activeSheet = null },
        )
        "guide" -> SettingsGuideSheet(onDismiss = { activeSheet = null })
        "whats-new" -> WhatsNewSheet(version = version, onDismiss = { activeSheet = null })
    }
    if (showResetConfirmation) {
        AppDialog(
            title = "Reset practice statistics?",
            text = "Clears today's time, your streak and ${TimestampMillisecondsFormatter.formatHuman(totalTime)} of total practice. Your presets and practice sets are kept. This can't be undone.",
            confirmLabel = "Reset statistics",
            destructive = true,
            onConfirm = {
                if (!playing) viewModel.resetTotalTime()
                showResetConfirmation = false
            },
            onDismiss = { showResetConfirmation = false },
        )
    }

    SettingsScreenContent(
        state = SettingsUiState(
            colorScheme = colorScheme,
            themeMode = themeMode,
            colorFlash = colorFlash,
            backgroundPlay = backgroundPlay,
            hapticEnabled = hapticEnabled,
            keepScreenAwake = keepScreenAwake,
            countInEnabled = countInEnabled,
            liveActivityEnabled = liveActivityEnabled,
            beatDisplayStyle = beatDisplayStyle,
            stereo = currentStereo,
            volume = clickVolume,
            selectedSound = selectedSound,
            previewSound = previewSound,
            playing = playing,
            supportsDynamicColor = platformActions.isDynamicColorSupported(),
            supportsHaptics = !desktop,
            version = version,
            previewError = previewError,
            practiceSummary = practiceSummaryLine(totalTime, practiceStreak),
        ),
        actions = SettingsActions(
            onColorScheme = viewModel::onColorSchemeChanged,
            onTheme = viewModel::onThemeModeChanged,
            onColorFlash = viewModel::onColorFlashChanged,
            onBackgroundPlay = {
                viewModel.onBackgroundPlayChanged(it)
                showBackgroundPlayPermission = it
            },
            onHaptic = viewModel::onHapticChanged,
            onKeepScreenAwake = viewModel::onKeepScreenAwakeChanged,
            onCountIn = viewModel::onCountInChanged,
            onLiveActivity = viewModel::onLiveActivityChanged,
            onBeatDisplay = viewModel::onBeatDisplayStyleChanged,
            onStereo = viewModel::onStereoChanged,
            onVolume = viewModel::onClickVolumeChanged,
            onSoundPicker = {
                soundPreview.stop()
                activeSheet = "sounds"
            },
            onPreview = { preview(selectedSound) },
            onPractice = { activeSheet = "practice" },
            onWhatsNew = { activeSheet = "whats-new" },
            onAbout = { activeSheet = "about" },
            onWeb = { uriHandler.openUri("https://metronome.merkost.dev/app/") },
            onSuby = { uriHandler.openUri("https://subyapp.com/") },
        ),
        upPress = upPress,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreenContent(
    state: SettingsUiState,
    actions: SettingsActions,
    upPress: () -> Unit,
) {
    val desktop = LocalAppPresentation.current == AppPresentation.Desktop
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (!desktop) {
                        AppIconButton(onClick = upPress) {
                            Icon(if (LocalLayoutDirection.current == LayoutDirection.Rtl) Lucide.ArrowRight else Lucide.ArrowLeft, "Back")
                        }
                    }
                },
            )
        },
    ) { insets ->
        Box(Modifier.fillMaxSize().padding(top = insets.calculateTopPadding()), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier
                    .widthIn(max = maxContentWidth)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = horizontalPadding)
                    .padding(bottom = spacingLarge),
                verticalArrangement = Arrangement.spacedBy(spacingMedium),
            ) {
                state.practiceSummary?.let { summary ->
                    Text(
                        summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                SettingsSoundPanel(state, actions)
                SettingsAppearancePanel(state, actions)
                SettingsDisclosure(
                    title = "Playback",
                    description = if (desktop) "Prepare before the first beat" else "Count-in, screen & background audio",
                    icon = Lucide.Timer,
                ) {
                    SettingsSwitch("Count-in", state.countInEnabled, actions.onCountIn, "One bar before playback")
                    if (!desktop) {
                        SettingsSwitch("Keep screen awake", state.keepScreenAwake, actions.onKeepScreenAwake, "While the beat is playing")
                        SettingsSwitch("Background playback", state.backgroundPlay, actions.onBackgroundPlay, "Play while using other apps")
                        LiveActivitySettingsRow(state.liveActivityEnabled, actions.onLiveActivity)
                    } else {
                        Text("Playback continues while you use other apps. Quit Metronome to stop playback.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                SettingsDisclosure(
                    title = "Beat display",
                    description = "Visual style & flash",
                    icon = Lucide.SlidersHorizontal,
                ) {
                    SettingsRow("Style") {
                        SettingsChoiceRow {
                            BeatDisplayStyle.entries.forEach { style ->
                                AppChip(state.beatDisplayStyle == style, { actions.onBeatDisplay(style) }, style.label)
                            }
                        }
                    }
                    SettingsSwitch("Beat flash", state.colorFlash, actions.onColorFlash, "Flashes each beat, downbeat only above 180 BPM")
                    Text(if (desktop) "Choose how each beat moves." else "Animations follow your device's motion preference.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                SettingsGroup("Practice data") {
                    SettingsLink("Statistics & backups", "Practice time, export & import", Lucide.ChartNoAxesColumn, actions.onPractice)
                }
                SettingsGroup("Help") {
                    SettingsLink("What's new", "Latest updates", Lucide.Sparkles, actions.onWhatsNew)
                    SettingsLink("Help & about", "Guide, support & app information", Lucide.CircleHelp, actions.onAbout)
                    SettingsWebCard(actions.onWeb)
                }
                SettingsSubyCard(actions.onSuby)
                Text(
                    "Metronome${state.version.takeIf { it.isNotBlank() }?.let { " · $it" }.orEmpty()}",
                    modifier = Modifier.fillMaxWidth().padding(top = spacingSmall),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    MaterialTheme(colorScheme = AppColorScheme.MELROSE.lightColor) {
        SettingsScreenContent(SettingsUiState(colorScheme = AppColorScheme.MELROSE, version = "1.4.0"), SettingsActions(), {})
    }
}

@Preview
@Composable
private fun SettingsScreenDarkPreview() {
    MaterialTheme(colorScheme = AppColorScheme.MELROSE.darkColor) {
        SettingsScreenContent(SettingsUiState(colorScheme = AppColorScheme.MELROSE, version = "1.4.0"), SettingsActions(), {})
    }
}
