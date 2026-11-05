package com.merkost.metronome.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import com.merkost.metronome.model.PracticeWeek
import com.merkost.metronome.components.AppSnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Headphones
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.Minus
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.TrendingDown
import com.composables.icons.lucide.TrendingUp
import com.composables.icons.lucide.Volume2
import com.composables.icons.lucide.VolumeX
import com.merkost.metronome.components.AppIconButton
import com.merkost.metronome.components.MetronomeMark
import com.merkost.metronome.components.PhraseBarCounter
import com.merkost.metronome.components.AppDialog
import com.merkost.metronome.components.AppSlider
import com.merkost.metronome.components.MainButtonsRow
import com.merkost.metronome.components.MetronomeBalls
import com.merkost.metronome.components.MyIconButton
import com.merkost.metronome.components.Pendulum
import com.merkost.metronome.components.PillChip
import com.merkost.metronome.components.MainShortcuts
import com.merkost.metronome.components.StatusStrip
import com.merkost.metronome.components.StatusStripAction
import com.merkost.metronome.model.NoSoundHint
import com.merkost.metronome.model.TempoStepMode
import com.merkost.metronome.model.tempoTrainerStripState
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronUp
import com.merkost.metronome.components.PresetNameDialog
import com.merkost.metronome.components.PresetSaveChoiceDialog
import com.merkost.metronome.components.PracticeSessionStrip
import androidx.compose.ui.keepScreenOn
import com.merkost.metronome.navigation.AppPresentation
import com.merkost.metronome.navigation.LocalAppPresentation
import com.merkost.metronome.model.beatFlashVisible
import com.merkost.metronome.model.indicatorCrossfades
import com.merkost.metronome.model.indicatorStiffnessFor
import androidx.compose.animation.core.SpringSpec
import com.merkost.metronome.model.asString
import com.merkost.metronome.model.resolve
import com.merkost.metronome.model.countInWord
import com.merkost.metronome.model.gapClickResting
import com.merkost.metronome.model.gapStripState
import com.merkost.metronome.model.phraseBarLabel
import com.merkost.metronome.model.BeatDisplayStyle
import com.merkost.metronome.model.ClickSound
import com.merkost.metronome.model.MetronomeState
import com.merkost.metronome.model.Subdivision
import com.merkost.metronome.presets.PracticePresetDraft
import com.merkost.metronome.practiceSets.PracticeSessionStartResult
import com.merkost.metronome.ui.AnimatedNumberText
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.desktopInstrumentKeys
import com.merkost.metronome.ui.BallSize
import com.merkost.metronome.ui.BallSizeCompact
import com.merkost.metronome.ui.CircleSize
import com.merkost.metronome.ui.horizontalPadding
import com.merkost.metronome.ui.isAppMotionReduced
import com.merkost.metronome.ui.cornerRadiusLarge
import com.merkost.metronome.ui.minimumTouchTargetSize
import com.merkost.metronome.ui.pressableSurface
import com.merkost.metronome.engine.SoundPreviewController
import com.merkost.metronome.ui.maxContentWidth
import com.merkost.metronome.ui.pulseOnChange
import com.merkost.metronome.ui.spacingLarge
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall
import com.merkost.metronome.ui.tempoDisplaySize
import com.merkost.metronome.viewModels.MetronomeViewModel
import com.merkost.metronome.viewModels.PracticeCompletionEvent
import com.merkost.metronome.viewModels.PracticePresetsViewModel
import com.merkost.metronome.viewModels.PresetUiEvent
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.applies_next_bar
import metronome.shared.generated.resources.app_name
import metronome.shared.generated.resources.choose_save_action
import metronome.shared.generated.resources.create_preset
import metronome.shared.generated.resources.edited
import metronome.shared.generated.resources.preset_deleted
import metronome.shared.generated.resources.preset_duplicated
import metronome.shared.generated.resources.preset_invalid
import metronome.shared.generated.resources.preset_in_use
import metronome.shared.generated.resources.preset_limit_reached
import metronome.shared.generated.resources.preset_saved
import metronome.shared.generated.resources.preset_storage_failed
import metronome.shared.generated.resources.preset_updated
import metronome.shared.generated.resources.cancel
import metronome.shared.generated.resources.practice_session_storage_warning
import metronome.shared.generated.resources.practice_completion_storage_failed
import metronome.shared.generated.resources.practice_set_start_invalid
import metronome.shared.generated.resources.practice_set_start_missing
import metronome.shared.generated.resources.practice_set_start_storage_failed
import metronome.shared.generated.resources.retry
import metronome.shared.generated.resources.structured_practice_replace_body
import metronome.shared.generated.resources.structured_practice_replace_confirm
import metronome.shared.generated.resources.structured_practice_replace_title
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import metronome.shared.generated.resources.main_click_resting
import metronome.shared.generated.resources.main_count_in_caption
import metronome.shared.generated.resources.main_exact_tempo_description
import metronome.shared.generated.resources.main_hint_faster
import metronome.shared.generated.resources.main_hint_slower
import metronome.shared.generated.resources.main_listen
import metronome.shared.generated.resources.main_open_rhythm
import metronome.shared.generated.resources.main_preset_summary
import metronome.shared.generated.resources.main_reset_body
import metronome.shared.generated.resources.main_reset_confirm
import metronome.shared.generated.resources.main_reset_title
import metronome.shared.generated.resources.main_tempo_label
import metronome.shared.generated.resources.main_tempo_slider_description
import metronome.shared.generated.resources.strip_audio_unavailable
import metronome.shared.generated.resources.strip_audio_unavailable_description
import metronome.shared.generated.resources.strip_click_volume_description
import metronome.shared.generated.resources.strip_click_volume_off
import metronome.shared.generated.resources.strip_dismiss
import metronome.shared.generated.resources.strip_edit_gap_trainer
import metronome.shared.generated.resources.strip_edit_tempo_trainer
import metronome.shared.generated.resources.strip_media_volume_buttons_description
import metronome.shared.generated.resources.strip_media_volume_off
import metronome.shared.generated.resources.strip_media_volume_tappable_description
import metronome.shared.generated.resources.strip_open_settings
import metronome.shared.generated.resources.strip_open_settings_action
import metronome.shared.generated.resources.strip_preset_applied
import metronome.shared.generated.resources.strip_show_volume_controls
import metronome.shared.generated.resources.strip_stop_tempo_trainer
import metronome.shared.generated.resources.strip_tap_to_adjust
import metronome.shared.generated.resources.strip_tap_to_retry
import metronome.shared.generated.resources.strip_trainer_stopped
import metronome.shared.generated.resources.strip_try_again
import metronome.shared.generated.resources.strip_use_volume_buttons
import metronome.shared.generated.resources.trainer_gap_stop
import metronome.shared.generated.resources.trainer_step_next

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onSettingsClicked: () -> Unit,
    onPresetsClicked: () -> Unit,
    onPracticeSetsClicked: () -> Unit,
) {
    val viewModel: MetronomeViewModel = koinInject()
    val presentation = LocalAppPresentation.current
    val desktop = presentation == AppPresentation.Desktop
    val instrumentFocusRequester = remember { FocusRequester() }
    LaunchedEffect(desktop) {
        if (desktop) instrumentFocusRequester.requestFocus()
    }
    val desktopKeyboard = if (desktop) {
        Modifier.desktopInstrumentKeys(
            presentation = presentation,
            onTogglePlayback = { viewModel.onPlayPauseClicked(viewModel.isPlaying.value) },
            onIncreaseTempo = viewModel::onSliderValueIncreased,
            onDecreaseTempo = viewModel::onSliderValueDecreased,
        ).focusRequester(instrumentFocusRequester).focusable()
    } else Modifier
    val presetsViewModel: PracticePresetsViewModel = koinViewModel()
    koinInject<com.merkost.metronome.engine.MetronomeEngine>()
    val colorFlash by viewModel.colorFlash.collectAsState()
    val metronomeState: MetronomeState by viewModel.metronomeState.collectAsState()
    val beats by remember(metronomeState.beats) {
        derivedStateOf { metronomeState.beats }
    }
    val isPlaying by viewModel.isPlaying.collectAsState()
    val practiceSessionState by viewModel.practiceSessionState.collectAsState()
    val recentPracticeSet by viewModel.recentPracticeSet.collectAsState()
    val selectedIndex by viewModel.index.collectAsState()

    val whatsNewVersion by viewModel.whatsNewVersion.collectAsState()
    var bottomControlsBounds by remember { mutableStateOf<Rect?>(null) }
    val soundPreviewController: SoundPreviewController = koinInject()
    val selectedSound by viewModel.selectedSound.collectAsState()
    val previewSound by soundPreviewController.activeSound.collectAsState()
    val previewError by soundPreviewController.errorMessage.collectAsState()
    val clickVolume by viewModel.clickVolume.collectAsState()
    val soundSelectionError by viewModel.soundSelectionError.collectAsState()
    val stereoPan by viewModel.stereoPan.collectAsState()
    var showSoundPicker by remember { mutableStateOf(false) }
    var pendingSoundPreview by remember { mutableStateOf<Job?>(null) }
    val soundPreviewScope = rememberCoroutineScope()
    var showPracticeHub by remember { mutableStateOf(false) }
    var showExactTempo by remember { mutableStateOf(false) }
    var showRhythmSheet by remember { mutableStateOf(false) }
    var showPracticeData by remember { mutableStateOf(false) }
    var showResetPracticeConfirmation by remember { mutableStateOf(false) }
    val practiceSetCount by viewModel.practiceSetCount.collectAsState()
    var pendingPracticeAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val practiceTimerGoal by viewModel.practiceTimerGoal.collectAsState()
    val practiceTimerRemaining by viewModel.practiceTimerRemaining.collectAsState()
    val lastTimerMinutes by viewModel.lastTimerMinutes.collectAsState()
    val totalPracticeTime by viewModel.totalPracticeTime.collectAsState()
    val todayPracticeTime by viewModel.todayPracticeTime.collectAsState()
    val practiceStreak by viewModel.practiceStreak.collectAsState()
    val practiceWeek by viewModel.practiceWeek.collectAsState()
    var showTimerSheet by remember { mutableStateOf(false) }
    var showPracticeSessionSheet by remember { mutableStateOf(false) }
    var pendingStructuredAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val presetsUiState by presetsViewModel.uiState.collectAsState()
    val activePresetState by viewModel.activePresetState.collectAsState()
    val countInEnabled by viewModel.countInEnabled.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var presetEditorDraft by remember { mutableStateOf<PracticePresetDraft?>(null) }
    var showPresetSaveChoice by remember { mutableStateOf(false) }
    LaunchedEffect(soundSelectionError) {
        soundSelectionError?.let { snackbarHostState.showSnackbar(it.resolve()) }
    }

    val gradualTempoConfig by viewModel.gradualTempoConfig.collectAsState()
    val gradualTempoCurrentBar by viewModel.gradualTempoCurrentBar.collectAsState()
    val lastTrainerConfig by viewModel.lastTrainerConfig.collectAsState()
    var showTempoSheet by remember { mutableStateOf(false) }
    var tempoSheetSection by remember { mutableStateOf<TempoSheetSection?>(null) }

    var lastShownTrainerConfig by remember { mutableStateOf(gradualTempoConfig) }
    LaunchedEffect(gradualTempoConfig) {
        gradualTempoConfig?.let { lastShownTrainerConfig = it }
    }

    val gapTrainerConfig by viewModel.gapTrainerConfig.collectAsState()
    val gapTrainerStartBar by viewModel.gapTrainerStartBar.collectAsState()
    val currentBar by viewModel.currentBar.collectAsState()
    val stopWhenTimerEnds by viewModel.stopWhenTimerEnds.collectAsState()
    val lastGapConfig by viewModel.lastGapConfig.collectAsState()
    var lastShownGapConfig by remember { mutableStateOf(gapTrainerConfig) }
    LaunchedEffect(gapTrainerConfig) {
        gapTrainerConfig?.let { lastShownGapConfig = it }
    }

    LaunchedEffect(showTimerSheet) { viewModel.setTimerSheetVisible(showTimerSheet) }
    LaunchedEffect(showTempoSheet) { viewModel.setTempoSheetVisible(showTempoSheet) }
    LaunchedEffect(Unit) {
        presetsViewModel.events.collect { event ->
            val message = when (event) {
                is PresetUiEvent.Saved -> {
                    viewModel.onPresetStored(event.preset)
                    getString(Res.string.preset_saved, event.preset.name)
                }
                is PresetUiEvent.Updated -> {
                    viewModel.onPresetStored(event.preset)
                    getString(Res.string.preset_updated, event.preset.name)
                }
                is PresetUiEvent.Duplicated -> getString(Res.string.preset_duplicated, event.preset.name)
                is PresetUiEvent.Deleted -> {
                    viewModel.onPresetDeleted(event.id)
                    getString(Res.string.preset_deleted, event.name)
                }
                is PresetUiEvent.InUse -> getString(Res.string.preset_in_use)
                PresetUiEvent.LimitReached -> getString(Res.string.preset_limit_reached)
                is PresetUiEvent.Invalid -> getString(Res.string.preset_invalid)
                PresetUiEvent.StorageFailure -> getString(Res.string.preset_storage_failed)
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(practiceSessionState.persistenceWarning) {
        if (practiceSessionState.persistenceWarning) {
            val result = snackbarHostState.showSnackbar(
                message = getString(Res.string.practice_session_storage_warning),
                actionLabel = getString(Res.string.retry),
                withDismissAction = true,
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.retryPracticeSessionPersistence()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.practiceSessionStartResults.collect { result ->
            when (result) {
                is PracticeSessionStartResult.Started -> Unit
                is PracticeSessionStartResult.MissingPreset -> snackbarHostState.showSnackbar(
                    getString(Res.string.practice_set_start_missing),
                )
                PracticeSessionStartResult.InvalidSet -> snackbarHostState.showSnackbar(
                    getString(Res.string.practice_set_start_invalid),
                )
                PracticeSessionStartResult.PersistenceFailed -> snackbarHostState.showSnackbar(
                    getString(Res.string.practice_set_start_storage_failed),
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.practiceCompletionEvents.collect { event ->
            when (event) {
                PracticeCompletionEvent.StorageFailure -> snackbarHostState.showSnackbar(
                    getString(Res.string.practice_completion_storage_failed),
                )
            }
        }
    }

    val keepScreenAwake by viewModel.keepScreenAwake.collectAsState()
    val countInRemaining by viewModel.countInRemaining.collectAsState()
    val practiceTimerStoppedPlayback by viewModel.practiceTimerStoppedPlayback.collectAsState()
    val tempoTrainerStoppedNotice by viewModel.tempoTrainerStoppedNotice.collectAsState()
    val noSoundHint by viewModel.noSoundHint.collectAsState()
    val audioUnavailable by viewModel.audioUnavailable.collectAsState()
    var lastNoSoundHint by remember { mutableStateOf<NoSoundHint?>(null) }
    LaunchedEffect(noSoundHint) { noSoundHint?.let { lastNoSoundHint = it } }
    val phraseLength by viewModel.phraseLength.collectAsState()

    val beatIndicatorSpec = remember(metronomeState.rhythm) {
        SpringSpec<Float>(dampingRatio = 0.94f, stiffness = indicatorStiffnessFor(metronomeState.rhythm))
    }

    val boxColorAnimation = remember { Animatable(0f) }

    val reducedMotion = isAppMotionReduced()
    if (colorFlash) {
        LaunchedEffect(selectedIndex) {
            if (beatFlashVisible(colorFlash, metronomeState.playing, reducedMotion, metronomeState.rhythm, selectedIndex, metronomeState.beats.size)) {
                boxColorAnimation.animateTo(0.28f, AppAnimations.quick())
                boxColorAnimation.animateTo(0f, AppAnimations.standard())
            } else {
                boxColorAnimation.animateTo(0f, AppAnimations.standard())
            }
        }
    }
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(desktopKeyboard)
            .then(if (!desktop && isPlaying && keepScreenAwake) Modifier.keepScreenOn() else Modifier)
    ) {
        Scaffold(
            snackbarHost = { AppSnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        if (!desktop) Row(
                            modifier = Modifier.widthIn(max = 260.dp),
                            horizontalArrangement = Arrangement.spacedBy(spacingSmall),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            MetronomeMark(Modifier.size(28.dp), MaterialTheme.colorScheme.primary)
                            Text(
                                text = stringResource(Res.string.app_name),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                autoSize = TextAutoSize.StepBased(minFontSize = 12.sp, maxFontSize = 22.sp),
                            )
                        }
                    },
                    actions = {
                        PillChip(onClick = { showRhythmSheet = true }, showDropdownIcon = false) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(metronomeState.timeSignature.label, style = MaterialTheme.typography.labelLarge)
                                Icon(Lucide.ChevronDown, contentDescription = stringResource(Res.string.main_open_rhythm), modifier = Modifier.size(16.dp))
                            }
                        }
                        if (!desktop) {
                            Spacer(Modifier.size(spacingSmall))
                            AppIconButton(onClick = onSettingsClicked) {
                                Icon(Lucide.Settings, Lucide.Settings.name)
                            }
                        }
                    }
                )
            }
        ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(it),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = maxContentWidth)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    MainScrollableContent(Modifier.fillMaxWidth().weight(1f)) {

                        val beatDisplayStyle by viewModel.beatDisplayStyle.collectAsState()
                        AnimatedContent(
                            targetState = beatDisplayStyle,
                            transitionSpec = { AppAnimations.fadeScaleTransform },
                            label = "beatDisplay",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = spacingLarge),
                        ) { style ->
                            when (style) {
                                BeatDisplayStyle.PENDULUM -> {
                                    val beatClock by viewModel.beatClock.collectAsState()
                                    Pendulum(
                                        selectedIndex = selectedIndex,
                                        beats = beats,
                                        isPlaying = isPlaying,
                                        intervalMs = metronomeState.interval,
                                        beatPulse = beatClock.pulse,
                                    )
                                }

                                BeatDisplayStyle.DOTS -> {
                                    val compactBalls = beats.size > 5
                                    val ballSpacing = if (compactBalls) spacingSmall else spacingLarge
                                    val ballSize = if (compactBalls) BallSizeCompact else BallSize
                                    val indicatorSize = minOf(CircleSize, ballSize + ballSpacing)
                                    MetronomeBalls(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .animateContentSize(AppAnimations.emphasized())
                                            .padding(horizontal = horizontalPadding),
                                        selectedIndex = selectedIndex,
                                        beats = beats,
                                        isPlaying = isPlaying,
                                        animSpec = beatIndicatorSpec,
                                        beatPulse = viewModel.beatClock.collectAsState().value.pulse,
                                        crossfade = indicatorCrossfades(metronomeState.rhythm),
                                        arrangementSpacing = ballSpacing,
                                        indicatorSize = indicatorSize,
                                        ballSize = ballSize,
                                        onBallClicked = viewModel::onBallClicked,
                                        onSetBeat = viewModel::setBeat,
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalPadding),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(spacingSmall)
                        ) {
                            if (phraseLength != 0) {
                                PhraseBarCounter(
                                    label = phraseBarLabel(currentBar, phraseLength, isPlaying, countInRemaining),
                                    phraseLength = phraseLength,
                                    onClick = {
                                        tempoSheetSection = TempoSheetSection.PHRASE
                                        showTempoSheet = true
                                    },
                                )
                            }
                            val tempoLabel = if (metronomeState.subdivision == Subdivision.QUARTER) {
                                metronomeState.tempoName
                            } else {
                                stringResource(Res.string.main_tempo_label, metronomeState.tempoName, stringResource(metronomeState.subdivision.labelRes))
                            }
                            TextButton(
                                onClick = {
                                    tempoSheetSection = null
                                    showTempoSheet = true
                                },
                                modifier = Modifier.heightIn(min = minimumTouchTargetSize),
                            ) {
                                AnimatedContent(
                                    targetState = tempoLabel,
                                    transitionSpec = { AppAnimations.slideLabelTransform(towardsUp = true) },
                                    label = "tempoLabel"
                                ) { label ->
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        autoSize = TextAutoSize.StepBased(
                                            minFontSize = 12.sp,
                                            maxFontSize = 16.sp,
                                        ),
                                    )
                                }
                                Icon(Lucide.ChevronDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = spacingSmall / 2).size(16.dp))
                            }

                            val presetStatus = activePresetState.pending?.let {
                                "${it.name} · ${stringResource(Res.string.applies_next_bar)}"
                            } ?: activePresetState.active?.let {
                                if (activePresetState.isEdited) {
                                    "${it.name} · ${stringResource(Res.string.edited)}"
                                } else {
                                    it.name
                                }
                            }
                            AnimatedVisibility(
                                visible = presetStatus != null,
                                enter = AppAnimations.expandEnter,
                                exit = AppAnimations.shrinkExit,
                            ) {
                                TextButton(
                                    onClick = {
                                        tempoSheetSection = null
                                        showTempoSheet = true
                                    }
                                ) {
                                    Text(
                                        text = presetStatus.orEmpty(),
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = spacingSmall),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                MyIconButton(
                                    Lucide.Minus,
                                    hint = stringResource(Res.string.main_hint_slower),
                                    onClick = viewModel::onSliderValueDecreased
                                )
                                val countingIn = countInRemaining > 0
                                val exactTempoDescription = stringResource(Res.string.main_exact_tempo_description, metronomeState.rhythm)
                                val clickResting = gapClickResting(
                                    gapActive = gapTrainerConfig != null,
                                    currentBar = currentBar,
                                    startBar = gapTrainerStartBar,
                                    muted = viewModel.isGapBarMuted(currentBar),
                                    isPlaying = isPlaying,
                                    countInRemaining = countInRemaining,
                                )
                                Column(
                                    modifier = Modifier.weight(1f)
                                        .semantics { contentDescription = exactTempoDescription }
                                        .pressableSurface(
                                            onClick = { showExactTempo = true },
                                            pressedScale = 1f,
                                        ),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    val tempoStyle = MaterialTheme.typography.displayLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = tempoDisplaySize,
                                    )
                                    if (countingIn || clickResting) {
                                        Text(
                                            text = if (countingIn) countInWord(countInRemaining).asString() else stringResource(Res.string.main_listen),
                                            style = tempoStyle,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            modifier = Modifier.fillMaxWidth(),
                                            autoSize = TextAutoSize.StepBased(30.sp, tempoDisplaySize),
                                        )
                                    } else {
                                        AnimatedNumberText(
                                            value = metronomeState.rhythm,
                                            style = tempoStyle,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.fillMaxWidth(),
                                            autoSize = TextAutoSize.StepBased(30.sp, tempoDisplaySize),
                                        )
                                    }
                                    Text(
                                        text = when {
                                            countingIn -> stringResource(Res.string.main_count_in_caption, metronomeState.rhythm, metronomeState.tempoName)
                                            clickResting -> stringResource(Res.string.main_click_resting)
                                            else -> "BPM"
                                        },
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                MyIconButton(
                                    Lucide.Plus,
                                    hint = stringResource(Res.string.main_hint_faster),
                                    onClick = viewModel::onSliderValueIncreased
                                )
                            }
                        }
                        AppSlider(
                            modifier = Modifier.padding(horizontal = horizontalPadding),
                            value = metronomeState.rhythm.toFloat(),
                            onValueChange = viewModel::onSliderValueChanged,
                            valueRange = viewModel.metronomeRange,
                            steps = viewModel.steps,
                            accessibilityLabel = stringResource(Res.string.main_tempo_slider_description, metronomeState.rhythm),
                        )
                        MainShortcuts(
                            selectedSound = selectedSound,
                            onPractice = { showPracticeHub = true },
                            onSound = { showSoundPicker = true },
                            modifier = Modifier.padding(horizontal = horizontalPadding),
                        )
                    }

                    Column(
                        modifier = Modifier
                            .padding(bottom = spacingLarge)
                            .onGloballyPositioned { coordinates ->
                                bottomControlsBounds = coordinates.boundsInRoot()
                            },
                        verticalArrangement = Arrangement.spacedBy(spacingMedium)
                    ) {
                        AnimatedVisibility(
                            visible = audioUnavailable,
                            enter = AppAnimations.expandEnter,
                            exit = AppAnimations.shrinkExit,
                            modifier = Modifier.padding(horizontal = horizontalPadding)
                        ) {
                            StatusStrip(
                                icon = Lucide.VolumeX,
                                title = stringResource(Res.string.strip_audio_unavailable),
                                caption = stringResource(Res.string.strip_tap_to_retry),
                                progress = 0f,
                                accent = MaterialTheme.colorScheme.error,
                                description = stringResource(Res.string.strip_audio_unavailable_description),
                                onClickLabel = stringResource(Res.string.strip_try_again),
                                stopLabel = stringResource(Res.string.strip_dismiss),
                                onClick = { viewModel.onPlayPauseClicked(false) },
                                onStop = viewModel::dismissAudioUnavailable,
                                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                            )
                        }

                        AnimatedVisibility(
                            visible = noSoundHint != null,
                            enter = AppAnimations.expandEnter,
                            exit = AppAnimations.shrinkExit,
                            modifier = Modifier.padding(horizontal = horizontalPadding)
                        ) {
                            val hint = noSoundHint ?: lastNoSoundHint
                            if (hint != null) {
                                val mediaOff = hint == NoSoundHint.MEDIA_VOLUME_OFF
                                val tappable = !mediaOff || viewModel.canShowVolumeControls
                                StatusStrip(
                                    icon = Lucide.VolumeX,
                                    title = stringResource(if (mediaOff) Res.string.strip_media_volume_off else Res.string.strip_click_volume_off),
                                    caption = when {
                                        !mediaOff -> stringResource(Res.string.strip_open_settings)
                                        tappable -> stringResource(Res.string.strip_tap_to_adjust)
                                        else -> stringResource(Res.string.strip_use_volume_buttons)
                                    },
                                    progress = 0f,
                                    accent = MaterialTheme.colorScheme.error,
                                    description = when {
                                        !mediaOff -> stringResource(Res.string.strip_click_volume_description)
                                        tappable -> stringResource(Res.string.strip_media_volume_tappable_description)
                                        else -> stringResource(Res.string.strip_media_volume_buttons_description)
                                    },
                                    onClickLabel = stringResource(if (mediaOff) Res.string.strip_show_volume_controls else Res.string.strip_open_settings_action),
                                    onClick = when {
                                        !mediaOff -> onSettingsClicked
                                        tappable -> viewModel::showVolumeControls
                                        else -> null
                                    },
                                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = tempoTrainerStoppedNotice && gradualTempoConfig == null,
                            enter = AppAnimations.expandEnter,
                            exit = AppAnimations.shrinkExit,
                            modifier = Modifier.padding(horizontal = horizontalPadding)
                        ) {
                            StatusStrip(
                                icon = Lucide.TrendingUp,
                                title = stringResource(Res.string.strip_trainer_stopped),
                                caption = stringResource(Res.string.strip_preset_applied),
                                progress = 0f,
                                accent = MaterialTheme.colorScheme.tertiary,
                                onClick = {
                                    tempoSheetSection = TempoSheetSection.TRAINER
                                    showTempoSheet = true
                                },
                            )
                        }

                        AnimatedVisibility(
                            visible = gradualTempoConfig != null,
                            enter = AppAnimations.expandEnter,
                            exit = AppAnimations.shrinkExit,
                            modifier = Modifier.padding(horizontal = horizontalPadding)
                        ) {
                            val config = gradualTempoConfig ?: lastShownTrainerConfig
                            if (config != null) {
                                val strip = tempoTrainerStripState(
                                    config = config,
                                    bpm = metronomeState.rhythm,
                                    trainerBar = gradualTempoCurrentBar,
                                    isPlaying = isPlaying,
                                    gapRunning = gapTrainerConfig != null,
                                )
                                val manual = config.stepMode == TempoStepMode.MANUAL &&
                                    !config.isComplete(metronomeState.rhythm)
                                StatusStrip(
                                    icon = if (config.ascending) {
                                        Lucide.TrendingUp
                                    } else {
                                        Lucide.TrendingDown
                                    },
                                    title = strip.title.asString(),
                                    caption = strip.caption.asString(),
                                    progress = strip.progress,
                                    description = strip.description.asString(),
                                    onClickLabel = stringResource(Res.string.strip_edit_tempo_trainer),
                                    stopLabel = stringResource(Res.string.strip_stop_tempo_trainer),
                                    action = if (manual) {
                                        StatusStripAction(
                                            icon = if (config.ascending) Lucide.ChevronUp else Lucide.ChevronDown,
                                            label = stringResource(Res.string.trainer_step_next),
                                            onClick = viewModel::stepTempoTrainerForward,
                                        )
                                    } else {
                                        null
                                    },
                                    accent = MaterialTheme.colorScheme.tertiary,
                                    onClick = {
                                        tempoSheetSection = TempoSheetSection.TRAINER
                                        showTempoSheet = true
                                    },
                                    onStop = { viewModel.stopGradualTempo() },
                                    titleModifier = Modifier.pulseOnChange(metronomeState.rhythm),
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = gapTrainerConfig != null,
                            enter = AppAnimations.expandEnter,
                            exit = AppAnimations.shrinkExit,
                            modifier = Modifier.padding(horizontal = horizontalPadding)
                        ) {
                            val config = gapTrainerConfig ?: lastShownGapConfig
                            if (config != null) {
                                val strip = gapStripState(
                                    config = config,
                                    currentBar = currentBar,
                                    startBar = gapTrainerStartBar,
                                    muted = viewModel.isGapBarMuted(currentBar),
                                    isPlaying = isPlaying,
                                    countInRemaining = countInRemaining,
                                )
                                StatusStrip(
                                    icon = if (strip.muted) Lucide.VolumeX else Lucide.Volume2,
                                    title = strip.title.asString(),
                                    caption = strip.caption.asString(),
                                    progress = strip.progress,
                                    description = strip.description.asString(),
                                    onClickLabel = stringResource(Res.string.strip_edit_gap_trainer),
                                    stopLabel = stringResource(Res.string.trainer_gap_stop),
                                    accent = MaterialTheme.colorScheme.primary,
                                    onClick = {
                                        tempoSheetSection = TempoSheetSection.GAP
                                        showTempoSheet = true
                                    },
                                    onStop = { viewModel.stopGapTrainer() },
                                    titleModifier = Modifier.pulseOnChange(currentBar),
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.fillMaxWidth())

                        AnimatedVisibility(
                            visible = practiceSessionState.session != null,
                            enter = AppAnimations.expandEnter,
                            exit = AppAnimations.shrinkExit,
                            modifier = Modifier.padding(horizontal = horizontalPadding),
                        ) {
                            practiceSessionState.session?.let { session ->
                                PracticeSessionStrip(
                                    session = session,
                                    isRecovered = practiceSessionState.isRecovered,
                                    onPrevious = viewModel::previousPracticeStep,
                                    onTogglePlayback = viewModel::togglePracticeSessionPlayback,
                                    onNext = viewModel::nextPracticeStep,
                                    onOpen = { showPracticeSessionSheet = true },
                                )
                            }
                        }

                        MainButtonsRow(
                            Modifier.padding(horizontal = horizontalPadding),
                            isPlaying = isPlaying,
                            stopWatchState = metronomeState.stopWatchState,
                            timerGoal = practiceTimerGoal,
                            timerRemaining = practiceTimerRemaining,
                            barStart = if (selectedIndex == 0) viewModel.beatClock.collectAsState().value.pulse else null,
                            onPlayPause = viewModel::onPlayPauseClicked,
                            onTempoTap = viewModel::onTempoTap,
                            onTimerClick = { showTimerSheet = true }
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val flashBottom = bottomControlsBounds?.top ?: size.height
                    drawRect(
                        color = primaryColor,
                        size = size.copy(height = flashBottom.coerceIn(0f, size.height)),
                        alpha = boxColorAnimation.value.coerceIn(0f, 0.5f)
                    )
                }
        )

    }

    whatsNewVersion?.let { version ->
        WhatsNewSheet(
            version = version,
            onDismiss = viewModel::onWhatsNewDismissed,
        )
    }

    LaunchedEffect(showSoundPicker, showPracticeHub, showExactTempo, showRhythmSheet, showPracticeData) {
        viewModel.setCompanionSheetVisible(showSoundPicker || showPracticeHub || showExactTempo || showRhythmSheet || showPracticeData)
    }
    DisposableEffect(Unit) {
        onDispose {
            pendingSoundPreview?.cancel()
            soundPreviewController.stop()
            viewModel.setCompanionSheetVisible(false)
        }
    }

    if (showSoundPicker) {
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner, soundPreviewController) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                    pendingSoundPreview?.cancel()
                    soundPreviewController.stop()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                pendingSoundPreview?.cancel()
                soundPreviewController.stop()
            }
        }
        SoundPickerSheet(
            selectedSound = selectedSound,
            previewSound = previewSound,
            onSelect = {
                pendingSoundPreview?.cancel()
                soundPreviewController.stop()
                viewModel.onSoundSelected(it)
            },
            onPreview = { sound ->
                pendingSoundPreview?.cancel()
                if (previewSound == sound) soundPreviewController.stop()
                else {
                    pendingSoundPreview = soundPreviewScope.launch {
                        val paused = viewModel.pauseForSoundPreview()
                        if (paused && showSoundPicker && lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                            soundPreviewController.preview(sound, volume = clickVolume, pan = stereoPan)
                        }
                    }
                }
            },
            onDismiss = {
                pendingSoundPreview?.cancel()
                soundPreviewController.stop()
                showSoundPicker = false
            },
            errorMessage = previewError,
        )
    }

    if (showPracticeHub) {
        PracticeHubSheet(
            presetCount = presetsUiState.presets.size,
            practiceSetCount = practiceSetCount,
            timerMinutes = practiceTimerGoal?.let { (it / 60_000L).toInt() } ?: lastTimerMinutes,
            todayPracticeTime = todayPracticeTime,
            practiceStreak = practiceStreak,
            totalPracticeTime = totalPracticeTime,
            onOpenTimer = { pendingPracticeAction = { showTimerSheet = true } },
            onOpenTempoTrainer = {
                pendingPracticeAction = {
                    tempoSheetSection = TempoSheetSection.TRAINER
                    showTempoSheet = true
                }
            },
            onOpenGapTrainer = {
                pendingPracticeAction = {
                    tempoSheetSection = TempoSheetSection.GAP
                    showTempoSheet = true
                }
            },
            onOpenPresets = { pendingPracticeAction = onPresetsClicked },
            onOpenPracticeSets = { pendingPracticeAction = onPracticeSetsClicked },
            onOpenPracticeData = { pendingPracticeAction = { showPracticeData = true } },
            onDismiss = {
                showPracticeHub = false
                val action = pendingPracticeAction
                pendingPracticeAction = null
                action?.invoke()
            },
        )
    }

    if (showExactTempo) {
        ExactTempoSheet(
            currentBpm = metronomeState.rhythm,
            onApply = { viewModel.onSliderValueChanged(it.toFloat()) },
            onDismiss = { showExactTempo = false },
        )
    }

    if (showRhythmSheet) {
        RhythmSheet(
            state = metronomeState,
            selectedIndex = selectedIndex,
            countInEnabled = countInEnabled,
            onCountInChanged = viewModel::onCountInChanged,
            onTimeSignatureChanged = viewModel::onTimeSignatureChanged,
            onSubdivisionChanged = viewModel::onSubdivisionChanged,
            onBeatChanged = viewModel::onBallClicked,
            onDismiss = { showRhythmSheet = false },
        )
    }

    if (showPracticeData) {
        SettingsPracticeSheet(
            todayTime = todayPracticeTime,
            totalTime = totalPracticeTime,
            practiceStreak = practiceStreak,
            playing = isPlaying,
            onReset = { showResetPracticeConfirmation = true },
            onDismiss = { showPracticeData = false },
        )
    }

    if (showResetPracticeConfirmation) {
        AppDialog(
            title = stringResource(Res.string.main_reset_title),
            text = stringResource(Res.string.main_reset_body),
            confirmLabel = stringResource(Res.string.main_reset_confirm),
            destructive = true,
            dismissLabel = stringResource(Res.string.cancel),
            onConfirm = {
                viewModel.resetPracticeStatistics()
                showResetPracticeConfirmation = false
            },
            onDismiss = { showResetPracticeConfirmation = false },
        )
    }

    if (showTimerSheet) {
        PracticeTimerSheet(
            isPlaying = isPlaying,
            timerGoal = practiceTimerGoal,
            timerRemaining = practiceTimerRemaining,
            lastTimerMinutes = lastTimerMinutes,
            todayPracticeTime = todayPracticeTime,
            practiceStreak = practiceStreak,
            practiceWeek = practiceWeek,
            totalPracticeTime = totalPracticeTime,
            stopWhenTimerEnds = stopWhenTimerEnds,
            onStopWhenTimerEndsChange = viewModel::onStopWhenTimerEndsChanged,
            stoppedPlayback = practiceTimerStoppedPlayback,
            onStart = { minutes ->
                if (practiceSessionState.session != null) {
                    pendingStructuredAction = { viewModel.replacePracticeSessionWithTimer(minutes) }
                } else {
                    viewModel.startPracticeTimer(minutes)
                }
            },
            onExtend = viewModel::extendPracticeTimer,
            onRestart = viewModel::restartPracticeTimer,
            onStop = { viewModel.dismissPracticeTimer() },
            onDismiss = { showTimerSheet = false },
        )
    }

    if (showTempoSheet) {
        TempoTrainerSheet(
            currentBpm = metronomeState.rhythm,
            beatsPerBar = beats.size,
            isPlaying = isPlaying,
            subdivision = metronomeState.subdivision,
            favouritePresets = presetsUiState.favourites,
            recentPresets = presetsUiState.recents,
            recentPracticeSet = recentPracticeSet,
            activePresetState = activePresetState,
            activeConfig = gradualTempoConfig,
            currentBar = gradualTempoCurrentBar,
            lastConfig = lastTrainerConfig,
            activeGapConfig = gapTrainerConfig,
            lastGapConfig = lastGapConfig,
            initialSection = tempoSheetSection,
            onPresetSelected = { viewModel.onSliderValueChanged(it.toFloat()) },
            onApplyPracticePreset = viewModel::applyPracticePreset,
            onSaveCurrentSetup = {
                val state = viewModel.metronomeState.value
                val defaultName = "${state.rhythm} BPM · ${state.timeSignature.label}"
                val draft = PracticePresetDraft(
                    name = defaultName,
                    bpm = state.rhythm,
                    timeSignature = state.timeSignature,
                    subdivision = state.subdivision,
                    beats = state.beats,
                    countInEnabled = countInEnabled,
                )
                if (activePresetState.active != null && activePresetState.isEdited) {
                    presetEditorDraft = draft
                    showPresetSaveChoice = true
                } else {
                    presetEditorDraft = draft
                }
            },
            onManagePresets = onPresetsClicked,
            onManagePracticeSets = onPracticeSetsClicked,
            onPracticeAgain = { practiceSet ->
                if (viewModel.hasStructuredPracticeConflict()) {
                    pendingStructuredAction = { viewModel.startPracticeSet(practiceSet) }
                } else {
                    viewModel.startPracticeSet(practiceSet)
                }
            },
            onSubdivisionChanged = viewModel::onSubdivisionChanged,
            phraseLength = phraseLength,
            onPhraseLengthChanged = viewModel::onPhraseLengthChanged,
            onStepTrainerForward = viewModel::stepTempoTrainerForward,
            onStepTrainerBack = viewModel::stepTempoTrainerBack,
            onStartTrainer = { config ->
                if (practiceSessionState.session != null) {
                    pendingStructuredAction = { viewModel.replacePracticeSessionWithTempoTrainer(config) }
                } else {
                    viewModel.startGradualTempo(config)
                }
            },
            onStopTrainer = viewModel::stopGradualTempo,
            onStartGapTrainer = { config ->
                if (practiceSessionState.session != null) {
                    pendingStructuredAction = { viewModel.replacePracticeSessionWithGapTrainer(config) }
                } else {
                    viewModel.startGapTrainer(config)
                }
            },
            onUpdateGapTrainer = viewModel::updateGapTrainer,
            onStopGapTrainer = viewModel::stopGapTrainer,
            onDismiss = { showTempoSheet = false },
        )
    }

    if (showPracticeSessionSheet) {
        practiceSessionState.session?.let { session ->
            PracticeSessionSheet(
                session = session,
                isRecovered = practiceSessionState.isRecovered,
                onPrevious = viewModel::previousPracticeStep,
                onTogglePlayback = viewModel::togglePracticeSessionPlayback,
                onNext = viewModel::nextPracticeStep,
                onRestart = viewModel::restartPracticeStep,
                onFinish = viewModel::finishPracticeSession,
                onDismiss = { showPracticeSessionSheet = false },
            )
        }
    }

    pendingStructuredAction?.let { action ->
        AppDialog(
            title = stringResource(Res.string.structured_practice_replace_title),
            text = stringResource(Res.string.structured_practice_replace_body),
            confirmLabel = stringResource(Res.string.structured_practice_replace_confirm),
            dismissLabel = stringResource(Res.string.cancel),
            onConfirm = {
                pendingStructuredAction = null
                action()
            },
            onDismiss = { pendingStructuredAction = null },
        )
    }

    if (showPresetSaveChoice) {
        val active = activePresetState.active
        val draft = presetEditorDraft
        if (active != null && draft != null) {
            PresetSaveChoiceDialog(
                presetName = active.name,
                message = stringResource(Res.string.choose_save_action),
                onUpdate = {
                    presetsViewModel.update(active.id, draft.copy(name = active.name))
                    showPresetSaveChoice = false
                    presetEditorDraft = null
                },
                onSaveAsNew = {
                    showPresetSaveChoice = false
                    presetEditorDraft = draft.copy(name = "${draft.bpm} BPM · ${draft.timeSignature.label}")
                },
                onDismiss = {
                    showPresetSaveChoice = false
                    presetEditorDraft = null
                },
            )
        }
    } else {
        presetEditorDraft?.let { draft ->
            PresetNameDialog(
                title = stringResource(Res.string.create_preset),
                initialName = draft.name,
                summary = draft.run { stringResource(Res.string.main_preset_summary, bpm, timeSignature.label, stringResource(subdivision.labelRes)) },
                error = null,
                onConfirm = { name ->
                    presetsViewModel.create(draft.copy(name = name))
                    presetEditorDraft = null
                },
                onDismiss = { presetEditorDraft = null },
            )
        }
    }
}

@Composable
internal fun MainScrollableContent(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    BoxWithConstraints(modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(vertical = spacingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacingLarge, Alignment.CenterVertically),
            content = content,
        )
    }
}

@Preview
@Composable
private fun MainScrollableContentPreview() {
    MaterialTheme { MainScrollableContent(Modifier.height(320.dp)) { MainShortcuts(ClickSound.WOOD, {}, {}) } }
}
