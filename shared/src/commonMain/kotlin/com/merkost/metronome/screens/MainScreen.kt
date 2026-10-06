package com.merkost.metronome.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.SnackbarHost
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
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
import com.merkost.metronome.components.AppDialog
import com.merkost.metronome.components.AppSlider
import com.merkost.metronome.components.MainButtonsRow
import com.merkost.metronome.components.MetronomeBalls
import com.merkost.metronome.components.MyIconButton
import com.merkost.metronome.components.MySecondaryTextButton
import com.merkost.metronome.components.Pendulum
import com.merkost.metronome.components.PillChip
import com.merkost.metronome.components.StatusStrip
import com.merkost.metronome.components.PresetNameDialog
import com.merkost.metronome.components.PresetSaveChoiceDialog
import com.merkost.metronome.components.PracticeSessionStrip
import androidx.compose.ui.keepScreenOn
import com.merkost.metronome.model.BeatDisplayStyle
import com.merkost.metronome.model.MetronomeState
import com.merkost.metronome.model.Subdivision
import com.merkost.metronome.presets.PracticePresetDraft
import com.merkost.metronome.practiceSets.PracticeSessionStartResult
import com.merkost.metronome.ui.AnimatedNumberText
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.BallSize
import com.merkost.metronome.ui.BallSizeCompact
import com.merkost.metronome.ui.CircleSize
import com.merkost.metronome.ui.horizontalPadding
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onSettingsClicked: () -> Unit,
    onPresetsClicked: () -> Unit,
    onPracticeSetsClicked: () -> Unit,
) {
    val viewModel: MetronomeViewModel = koinInject()
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
        soundSelectionError?.let { snackbarHostState.showSnackbar(it) }
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

    val beatIndicatorSpec = AppAnimations.Emphasized

    val boxColorAnimation = remember { Animatable(0f) }

    if (colorFlash) {
        LaunchedEffect(selectedIndex) {
            if (metronomeState.playing) {
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
            .then(if (isPlaying && keepScreenAwake) Modifier.keepScreenOn() else Modifier)
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(
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
                                Icon(Lucide.ChevronDown, contentDescription = "Open rhythm", modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(Modifier.size(spacingSmall))
                        AppIconButton(onClick = onSettingsClicked) {
                            Icon(Lucide.Settings, Lucide.Settings.name)
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(top = spacingLarge),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(spacingLarge)
                    ) {

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
                                        arrangementSpacing = ballSpacing,
                                        indicatorSize = indicatorSize,
                                        ballSize = ballSize,
                                        onBallClicked = viewModel::onBallClicked,
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
                            val tempoLabel = if (metronomeState.subdivision == Subdivision.QUARTER) {
                                metronomeState.tempoName
                            } else {
                                "${metronomeState.tempoName} · ${metronomeState.subdivision.label}"
                            }
                            PillChip(
                                onClick = {
                                    tempoSheetSection = null
                                    showTempoSheet = true
                                }
                            ) {
                                AnimatedContent(
                                    targetState = tempoLabel,
                                    transitionSpec = { AppAnimations.slideLabelTransform(towardsUp = true) },
                                    label = "tempoLabel"
                                ) { label ->
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        autoSize = TextAutoSize.StepBased(
                                            minFontSize = 10.sp,
                                            maxFontSize = 18.sp,
                                        ),
                                    )
                                }
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
                                    onClick = viewModel::onSliderValueDecreased
                                )
                                val countingIn = countInRemaining > 0
                                Column(
                                    modifier = Modifier.weight(1f)
                                        .semantics { contentDescription = "Tempo ${metronomeState.rhythm} BPM. Enter exact tempo" }
                                        .pressableSurface(
                                            onClick = { showExactTempo = true },
                                            pressedScale = 1f,
                                        ),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    AnimatedNumberText(
                                        value = if (countingIn) countInRemaining else metronomeState.rhythm,
                                        style = MaterialTheme.typography.displayLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = tempoDisplaySize,
                                        ),
                                        color = if (countingIn) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.fillMaxWidth(),
                                        autoSize = TextAutoSize.StepBased(30.sp, tempoDisplaySize),
                                    )
                                    Text(
                                        text = if (countingIn) "Count in" else "BPM",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                MyIconButton(
                                    Lucide.Plus,
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
                            accessibilityLabel = "Tempo, ${metronomeState.rhythm} BPM",
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            MySecondaryTextButton(text = "- 5", onClick = viewModel::onMinusFive)
                            MySecondaryTextButton(text = "÷ 2", onClick = viewModel::divideByTwo)
                            MySecondaryTextButton(text = "× 2", onClick = viewModel::multiplyByTwo)
                            MySecondaryTextButton(text = "+ 5", onClick = viewModel::onPlusFive)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding),
                            horizontalArrangement = Arrangement.spacedBy(spacingSmall),
                        ) {
                            MainShortcut(
                                icon = Lucide.Sparkles,
                                title = "Practice",
                                caption = "Timer & trainers",
                                onClick = { showPracticeHub = true },
                                modifier = Modifier.weight(1f),
                            )
                            MainShortcut(
                                icon = Lucide.Headphones,
                                title = "Sound",
                                caption = selectedSound.displayName,
                                onClick = { showSoundPicker = true },
                                modifier = Modifier.weight(1f),
                            )
                        }
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
                            visible = gradualTempoConfig != null,
                            enter = AppAnimations.expandEnter,
                            exit = AppAnimations.shrinkExit,
                            modifier = Modifier.padding(horizontal = horizontalPadding)
                        ) {
                            val config = gradualTempoConfig ?: lastShownTrainerConfig
                            if (config != null) {
                                val complete = config.isComplete(metronomeState.rhythm)
                                StatusStrip(
                                    icon = if (config.ascending) {
                                        Lucide.TrendingUp
                                    } else {
                                        Lucide.TrendingDown
                                    },
                                    title = if (complete) {
                                        "${config.endBpm} BPM reached"
                                    } else {
                                        "${metronomeState.rhythm} → ${config.endBpm}"
                                    },
                                    caption = when {
                                        complete -> ""
                                        isPlaying -> "bar ${gradualTempoCurrentBar.coerceAtMost(config.totalBars)} / ${config.totalBars}"
                                        else -> "paused"
                                    },
                                    progress = config.progressFor(metronomeState.rhythm),
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
                                val gapBar = (currentBar - gapTrainerStartBar).coerceAtLeast(0)
                                val muted = config.isMuted(gapBar)
                                StatusStrip(
                                    icon = if (muted) {
                                        Lucide.VolumeX
                                    } else {
                                        Lucide.Volume2
                                    },
                                    title = when {
                                        !isPlaying -> "Gap trainer"
                                        muted -> "Muted · bar ${config.barInPhase(gapBar)}/${config.muteBars}"
                                        else -> "Click · bar ${config.barInPhase(gapBar)}/${config.playBars}"
                                    },
                                    caption = if (isPlaying) {
                                        "${config.playBars} + ${config.muteBars} bars"
                                    } else {
                                        "paused"
                                    },
                                    progress = config.cycleProgress(gapBar),
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
            title = "Reset practice statistics?",
            text = "Clear today’s practice time, your streak and total time. Your presets and practice sets are kept.",
            confirmLabel = "Reset statistics",
            dismissLabel = "Cancel",
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
            totalPracticeTime = totalPracticeTime,
            onStart = { minutes ->
                if (practiceSessionState.session != null) {
                    pendingStructuredAction = { viewModel.replacePracticeSessionWithTimer(minutes) }
                } else {
                    viewModel.startPracticeTimer(minutes)
                }
            },
            onExtend = viewModel::extendPracticeTimer,
            onRestart = viewModel::restartPracticeTimer,
            onStop = viewModel::dismissPracticeTimer,
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
                summary = draft.run { "$bpm BPM · ${timeSignature.label} · ${subdivision.label}" },
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
private fun MainShortcut(
    icon: ImageVector,
    title: String,
    caption: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.heightIn(min = minimumTouchTargetSize)
            .clip(RoundedCornerShape(cornerRadiusLarge))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .pressableSurface(onClick)
            .padding(spacingMedium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacingSmall),
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
            AnimatedContent(
                targetState = caption,
                transitionSpec = { AppAnimations.fadeThrough },
                label = "mainShortcutCaption",
            ) { text ->
                Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(Lucide.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
    }
}

@Preview
@Composable
private fun MainShortcutPreview() {
    MaterialTheme {
        MainShortcut(Lucide.Headphones, "Sound", "Wood", {})
    }
}
