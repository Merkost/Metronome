package com.merkost.metronome.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.metronome.model.GradualTempoConfig
import com.merkost.metronome.model.MAX_BPM
import com.merkost.metronome.components.CoachMarksOverlay
import com.merkost.metronome.components.Ball
import com.merkost.metronome.components.DropdownSelector
import com.merkost.metronome.components.FeatureCard
import com.merkost.metronome.components.MainButtonsRow
import com.merkost.metronome.components.MetronomeBalls
import com.merkost.metronome.components.MyIconButton
import com.merkost.metronome.components.MySecondaryTextButton
import com.merkost.metronome.components.OutlinedCircle
import com.merkost.metronome.components.StatusTag
import com.merkost.metronome.components.TimestampMillisecondsFormatter
import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.MetronomeState
import com.merkost.metronome.model.TimeSignature
import com.merkost.metronome.ui.horizontalPadding
import com.merkost.metronome.viewModels.MetronomeViewModel
import metronome.composeapp.generated.resources.Res
import metronome.composeapp.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

private val TEMPO_PRESETS = listOf(
    "Largo" to 50,
    "Andante" to 80,
    "Moderato" to 110,
    "Allegro" to 140,
    "Presto" to 180,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(onSettingsClicked: () -> Unit) {
    val viewModel: MetronomeViewModel = koinInject()
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
    var showTimerPicker by remember { mutableStateOf(false) }

    val gradualTempoConfig by viewModel.gradualTempoConfig.collectAsState()
    val gradualTempoCurrentBar by viewModel.gradualTempoCurrentBar.collectAsState()
    var showGradualTempoPicker by remember { mutableStateOf(false) }

    var tsExpanded by remember { mutableStateOf(false) }
    var presetsExpanded by remember { mutableStateOf(false) }

    val springSpec = SpringSpec<Float>(stiffness = 600f, dampingRatio = 0.8f)

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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.drawWithContent {
                    drawContent()
                    drawRect(
                        primaryColor,
                        alpha = boxColorAnimation.value.coerceIn(0f, 0.5f)
                    )
                },
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
                                text = "${metronomeState.timeSignature.label} \u25BC",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
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
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            primaryColor,
                            alpha = boxColorAnimation.value.coerceIn(0f, 0.5f)
                        )
                    }
                    .padding(top = 32.dp)
                    .padding(horizontal = horizontalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {

                MetronomeBalls(
                    modifier = Modifier
                        .padding(bottom = 64.dp)
                        .onGloballyPositioned { coordinates ->
                            beatBallsBounds = coordinates.boundsInRoot()
                        },
                    selectedIndex = selectedIndex.coerceIn(beats.indices),
                    itemCount = beats.size,
                    animSpec = springSpec,
                    indicator = {
                        OutlinedCircle(MaterialTheme.colorScheme.primary)
                    },
                ) {
                    beats.forEachIndexed { index, beat ->
                        val color by animateColorAsState(
                            targetValue =
                            if (beat == Beat.HIGH) {
                                MaterialTheme.colorScheme.primary
                            } else MaterialTheme.colorScheme.primaryContainer,
                            label = "ballsColor"
                        )
                        Ball(
                            color = color,
                            isActive = isPlaying && index == selectedIndex.coerceIn(beats.indices),
                            onClick = { viewModel.onBallClicked(index, beat) }
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            tempoSectionBounds = coordinates.boundsInRoot()
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DropdownSelector(
                        expanded = presetsExpanded,
                        onDismiss = { presetsExpanded = false },
                        items = TEMPO_PRESETS,
                        selectedItem = TEMPO_PRESETS.firstOrNull { it.second == metronomeState.rhythm },
                        onSelect = {
                            viewModel.onSliderValueChanged(it.second.toFloat())
                            presetsExpanded = false
                        },
                        itemContent = { preset, _ ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = preset.first,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "${preset.second} BPM",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        anchor = {
                            AnimatedContent(targetState = metronomeState.tempoName) { name ->
                                Text(
                                    text = "$name \u25BE",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.clickable { presetsExpanded = !presetsExpanded }
                                )
                            }
                        }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MyIconButton(
                            Icons.Default.Remove,
                            onClick = viewModel::onSliderValueDecreased
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                modifier = Modifier.combinedClickable(
                                    onClick = {},
                                    onLongClick = {
                                        viewModel.onLongPressConfirm()
                                        showGradualTempoPicker = true
                                    }
                                ),
                                text = metronomeState.rhythm.toString(),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center,
                                    fontSize = 62.sp
                                )
                            )
                            if (gradualTempoConfig != null) {
                                Text(
                                    text = "\u2197 Auto-increasing",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFF9800),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        MyIconButton(
                            Icons.Default.Add,
                            onClick = viewModel::onSliderValueIncreased
                        )
                    }
                }
                Slider(
                    modifier = Modifier.height(20.dp),
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MySecondaryTextButton(text = "- 5", onClick = viewModel::onMinusFive)
                    MySecondaryTextButton(text = "÷ 2", onClick = viewModel::divideByTwo)
                    MySecondaryTextButton(text = "× 2", onClick = viewModel::multiplyByTwo)
                    MySecondaryTextButton(text = "+ 5", onClick = viewModel::onPlusFive)
                }
                Spacer(modifier = Modifier.size(32.dp))
            }

            Column(
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .onGloballyPositioned { coordinates ->
                        bottomControlsBounds = coordinates.boundsInRoot()
                    },
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HorizontalDivider(modifier = Modifier.fillMaxWidth())

                if (gradualTempoConfig != null) {
                    val config = gradualTempoConfig!!
                    val currentBar = gradualTempoCurrentBar
                    val completed = currentBar >= config.totalBars
                    Column(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                        FeatureCard(
                            title = "\uD83D\uDCC8 Gradual Tempo",
                            statusTag = {
                                StatusTag(
                                    if (completed) "COMPLETE" else "RUNNING",
                                    Color(0xFFFF9800)
                                )
                            },
                            progress = currentBar.toFloat() / config.totalBars.toFloat(),
                            progressColor = Color(0xFFFF9800),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "Start",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "${config.startBpm}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Spacer(Modifier.weight(1f))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "End",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "${config.endBpm}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                            Text(
                                "+${config.increment} BPM / ${config.barsPerStep} bars  \u2022  Bar $currentBar / ${config.totalBars}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(
                            onClick = { viewModel.dismissGradualTempo() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Dismiss")
                        }
                    }
                } else if (practiceTimerGoal != null) {
                    val goal = practiceTimerGoal!!
                    val remaining = practiceTimerRemaining
                    Column(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                        FeatureCard(
                            title = "\u23F1 Practice Timer",
                            statusTag = {
                                if (remaining > 0) {
                                    StatusTag("ACTIVE", Color(0xFF4CAF50))
                                } else {
                                    StatusTag("DONE", Color(0xFFFF9800))
                                }
                            },
                            progress = 1f - (remaining.toFloat() / goal.toFloat()),
                            progressColor = Color(0xFF4CAF50),
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    TimestampMillisecondsFormatter.format(remaining),
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    "of ${TimestampMillisecondsFormatter.format(goal)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        TextButton(
                            onClick = { viewModel.dismissPracticeTimer() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Dismiss")
                        }
                    }
                }

                MainButtonsRow(
                    Modifier.padding(horizontal = horizontalPadding),
                    isPlaying = isPlaying,
                    metronomeState.stopWatchState,
                    onPlayPause = viewModel::onPlayPauseClicked,
                    onTempoTap = viewModel::onTempoTap,
                    onStopwatchLongPress = {
                        viewModel.onLongPressConfirm()
                        showTimerPicker = true
                    }
                )
            }
        }

        if (onboardingStep >= 0 && spotlightTargets.size == 3) {
            CoachMarksOverlay(
                step = onboardingStep,
                targetBounds = spotlightTargets,
                onNext = viewModel::onOnboardingNext,
                onBack = viewModel::onOnboardingBack,
                onDismiss = viewModel::onOnboardingDismiss,
            )
        }
    }

    if (showTimerPicker) {
        AlertDialog(
            onDismissRequest = { showTimerPicker = false },
            title = { Text("Practice Timer", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Choose practice duration:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    listOf(5, 10, 15, 20, 30).forEach { minutes ->
                        TextButton(
                            onClick = {
                                viewModel.startPracticeTimer(minutes)
                                showTimerPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "$minutes minutes",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTimerPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showGradualTempoPicker) {
        GradualTempoDialog(
            currentBpm = metronomeState.rhythm,
            onDismiss = { showGradualTempoPicker = false },
            onStart = { config ->
                viewModel.startGradualTempo(config)
                showGradualTempoPicker = false
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GradualTempoDialog(
    currentBpm: Int,
    onDismiss: () -> Unit,
    onStart: (GradualTempoConfig) -> Unit,
) {
    var startBpm by remember { mutableStateOf(currentBpm) }
    var endBpm by remember { mutableStateOf((currentBpm + 40).coerceAtMost(MAX_BPM)) }
    var selectedIncrement by remember { mutableStateOf(2) }
    var selectedBarsPerStep by remember { mutableStateOf(4) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gradual Tempo", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Start BPM
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Start BPM", style = MaterialTheme.typography.bodyMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (startBpm > 40) startBpm-- }) {
                            Icon(Icons.Default.Remove, "Decrease start BPM")
                        }
                        Text(
                            "$startBpm",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { if (startBpm < endBpm - 1) startBpm++ }) {
                            Icon(Icons.Default.Add, "Increase start BPM")
                        }
                    }
                }

                // End BPM
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("End BPM", style = MaterialTheme.typography.bodyMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (endBpm > startBpm + 1) endBpm-- }) {
                            Icon(Icons.Default.Remove, "Decrease end BPM")
                        }
                        Text(
                            "$endBpm",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { if (endBpm < MAX_BPM) endBpm++ }) {
                            Icon(Icons.Default.Add, "Increase end BPM")
                        }
                    }
                }

                // Increment chips
                Column {
                    Text(
                        "Increment",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 2, 3, 5).forEach { inc ->
                            FilterChip(
                                selected = selectedIncrement == inc,
                                onClick = { selectedIncrement = inc },
                                label = { Text("+$inc") }
                            )
                        }
                    }
                }

                // Frequency chips
                Column {
                    Text(
                        "Every N bars",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(2, 4, 8).forEach { bars ->
                            FilterChip(
                                selected = selectedBarsPerStep == bars,
                                onClick = { selectedBarsPerStep = bars },
                                label = { Text("$bars") }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onStart(
                        GradualTempoConfig(
                            startBpm = startBpm,
                            endBpm = endBpm,
                            increment = selectedIncrement,
                            barsPerStep = selectedBarsPerStep,
                        )
                    )
                }
            ) {
                Text("Start", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
