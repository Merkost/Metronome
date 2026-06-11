package com.merkost.metronome.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.merkost.metronome.components.CoachMarksOverlay
import com.merkost.metronome.components.DropdownSelector
import com.merkost.metronome.components.MainButtonsRow
import com.merkost.metronome.components.MetronomeBalls
import com.merkost.metronome.components.MyIconButton
import com.merkost.metronome.components.MySecondaryTextButton
import com.merkost.metronome.components.StatusStrip
import androidx.compose.ui.keepScreenOn
import com.merkost.metronome.model.MetronomeState
import com.merkost.metronome.model.Subdivision
import com.merkost.metronome.model.TimeSignature
import com.merkost.metronome.ui.AnimatedNumberText
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.BallSize
import com.merkost.metronome.ui.BallSizeCompact
import com.merkost.metronome.ui.CircleSize
import com.merkost.metronome.ui.horizontalPadding
import com.merkost.metronome.ui.maxContentWidth
import com.merkost.metronome.ui.pulseOnChange
import com.merkost.metronome.ui.spacingLarge
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall
import com.merkost.metronome.ui.tempoDisplaySize
import com.merkost.metronome.viewModels.MetronomeViewModel
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onSettingsClicked: () -> Unit) {
    val viewModel: MetronomeViewModel = koinInject()
    koinInject<com.merkost.metronome.engine.MetronomeEngine>()
    val colorFlash by viewModel.colorFlash.collectAsState()
    val metronomeState: MetronomeState by viewModel.metronomeState.collectAsState()
    val beats by remember(metronomeState.beats) {
        derivedStateOf { metronomeState.beats }
    }
    val isPlaying by viewModel.isPlaying.collectAsState()
    val selectedIndex by viewModel.index.collectAsState()

    val onboardingStep by viewModel.onboardingStep.collectAsState()
    var beatBallsBounds by remember { mutableStateOf<Rect?>(null) }
    var tempoSectionBounds by remember { mutableStateOf<Rect?>(null) }
    var bottomControlsBounds by remember { mutableStateOf<Rect?>(null) }
    val spotlightTargets = remember(beatBallsBounds, tempoSectionBounds, bottomControlsBounds) {
        listOfNotNull(beatBallsBounds, tempoSectionBounds, bottomControlsBounds)
    }

    val practiceTimerGoal by viewModel.practiceTimerGoal.collectAsState()
    val practiceTimerRemaining by viewModel.practiceTimerRemaining.collectAsState()
    val lastTimerMinutes by viewModel.lastTimerMinutes.collectAsState()
    val totalPracticeTime by viewModel.totalPracticeTime.collectAsState()
    var showTimerSheet by remember { mutableStateOf(false) }

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

    var tsExpanded by remember { mutableStateOf(false) }

    val keepScreenAwake by viewModel.keepScreenAwake.collectAsState()

    val springSpec = AppAnimations.Bouncy

    val boxColorAnimation = remember { Animatable(0f) }

    if (colorFlash) {
        LaunchedEffect(selectedIndex) {
            if (metronomeState.playing) {
                boxColorAnimation.animateTo(0.35f, springSpec)
                boxColorAnimation.animateTo(0f, springSpec)
            } else {
                boxColorAnimation.animateTo(0f, springSpec)
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
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(Res.string.app_name),
                            style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    actions = {
                        DropdownSelector(
                            expanded = tsExpanded,
                            onDismiss = { tsExpanded = false },
                            items = TimeSignature.entries.toList(),
                            selectedItem = metronomeState.timeSignature,
                            onSelect = {
                                viewModel.onTimeSignatureChanged(it)
                                tsExpanded = false
                            },
                            itemContent = { ts, _ ->
                                Column {
                                    Text(
                                        text = ts.label,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "${ts.defaultBeats.size} beats",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            anchor = {
                                Text(
                                    text = "${metronomeState.timeSignature.label} ▾",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(spacingSmall))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { tsExpanded = true }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        )
                        IconButton(onClick = onSettingsClicked) {
                            Icon(Icons.Default.Settings, Icons.Default.Settings.name)
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

                        val compactBalls = beats.size > 5
                        val ballSpacing = if (compactBalls) spacingSmall else spacingLarge
                        val ballSize = if (compactBalls) BallSizeCompact else BallSize
                        val indicatorSize = minOf(CircleSize, ballSize + ballSpacing)

                        MetronomeBalls(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalPadding)
                                .padding(bottom = spacingLarge)
                                .onGloballyPositioned { coordinates ->
                                    beatBallsBounds = coordinates.boundsInRoot()
                                },
                            selectedIndex = selectedIndex.coerceIn(beats.indices),
                            beats = beats,
                            isPlaying = isPlaying,
                            animSpec = springSpec,
                            arrangementSpacing = ballSpacing,
                            indicatorSize = indicatorSize,
                            ballSize = ballSize,
                            onBallClicked = viewModel::onBallClicked,
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalPadding)
                                .onGloballyPositioned { coordinates ->
                                    tempoSectionBounds = coordinates.boundsInRoot()
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(spacingSmall)
                        ) {
                            val tempoLabel = if (metronomeState.subdivision == Subdivision.QUARTER) {
                                metronomeState.tempoName
                            } else {
                                "${metronomeState.tempoName} · ${metronomeState.subdivision.label}"
                            }
                            AnimatedContent(targetState = tempoLabel) { label ->
                                Text(
                                    text = "$label ▾",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.clickable {
                                        tempoSheetSection = null
                                        showTempoSheet = true
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = spacingSmall),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                MyIconButton(
                                    Icons.Default.Remove,
                                    onClick = viewModel::onSliderValueDecreased
                                )
                                AnimatedNumberText(
                                    value = metronomeState.rhythm,
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = tempoDisplaySize
                                    ),
                                    modifier = Modifier.weight(1f).pulseOnChange(
                                        metronomeState.rhythm,
                                        peakScale = 1.02f
                                    ),
                                )
                                MyIconButton(
                                    Icons.Default.Add,
                                    onClick = viewModel::onSliderValueIncreased
                                )
                            }
                        }
                        Slider(
                            modifier = Modifier.padding(horizontal = horizontalPadding).height(20.dp),
                            value = metronomeState.rhythm.toFloat(),
                            onValueChange = viewModel::onSliderValueChanged,
                            valueRange = viewModel.metronomeRange,
                            steps = viewModel.steps,
                            colors = SliderDefaults.colors(
                                activeTickColor = Color.Transparent,
                                inactiveTickColor = Color.Transparent,
                            )
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

                        Spacer(modifier = Modifier.size(spacingLarge))
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
                                        Icons.AutoMirrored.Rounded.TrendingUp
                                    } else {
                                        Icons.AutoMirrored.Rounded.TrendingDown
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
                                        Icons.AutoMirrored.Rounded.VolumeOff
                                    } else {
                                        Icons.AutoMirrored.Rounded.VolumeUp
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
                .padding(bottom = 140.dp)
                .drawBehind {
                    drawRect(
                        primaryColor,
                        alpha = boxColorAnimation.value.coerceIn(0f, 0.5f)
                    )
                }
        )

        AnimatedVisibility(
            visible = onboardingStep >= 0 && spotlightTargets.size == 3,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CoachMarksOverlay(
                step = onboardingStep,
                targetBounds = spotlightTargets,
                onNext = viewModel::onOnboardingNext,
                onBack = viewModel::onOnboardingBack,
                onDismiss = viewModel::onOnboardingDismiss,
            )
        }
    }

    if (showTimerSheet) {
        PracticeTimerSheet(
            isPlaying = isPlaying,
            timerGoal = practiceTimerGoal,
            timerRemaining = practiceTimerRemaining,
            lastTimerMinutes = lastTimerMinutes,
            sessionElapsed = metronomeState.stopWatchState.elapsedTime,
            totalPracticeTime = totalPracticeTime,
            onStart = viewModel::startPracticeTimer,
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
            activeConfig = gradualTempoConfig,
            currentBar = gradualTempoCurrentBar,
            lastConfig = lastTrainerConfig,
            activeGapConfig = gapTrainerConfig,
            lastGapConfig = lastGapConfig,
            initialSection = tempoSheetSection,
            onPresetSelected = { viewModel.onSliderValueChanged(it.toFloat()) },
            onSubdivisionChanged = viewModel::onSubdivisionChanged,
            onStartTrainer = viewModel::startGradualTempo,
            onStopTrainer = viewModel::stopGradualTempo,
            onStartGapTrainer = viewModel::startGapTrainer,
            onUpdateGapTrainer = viewModel::updateGapTrainer,
            onStopGapTrainer = viewModel::stopGapTrainer,
            onDismiss = { showTempoSheet = false },
        )
    }
}
