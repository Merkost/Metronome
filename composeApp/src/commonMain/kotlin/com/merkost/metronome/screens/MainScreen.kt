package com.merkost.metronome.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.metronome.components.CoachMarksOverlay
import com.merkost.metronome.components.Ball
import com.merkost.metronome.components.DropdownSelector
import com.merkost.metronome.components.MainButtonsRow
import com.merkost.metronome.components.MetronomeBalls
import com.merkost.metronome.components.MyIconButton
import com.merkost.metronome.components.MySecondaryTextButton
import com.merkost.metronome.components.OutlinedCircle
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

@OptIn(ExperimentalMaterial3Api::class)
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
                        Text(
                            modifier = Modifier.weight(1f),
                            text = metronomeState.rhythm.toString(),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                fontSize = 62.sp
                            )
                        )
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
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
                MainButtonsRow(
                    Modifier.padding(horizontal = horizontalPadding),
                    isPlaying = isPlaying,
                    metronomeState.stopWatchState,
                    onPlayPause = viewModel::onPlayPauseClicked,
                    onTempoTap = viewModel::onTempoTap
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
}
