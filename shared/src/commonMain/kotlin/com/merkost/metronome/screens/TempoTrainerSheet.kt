package com.merkost.metronome.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.merkost.metronome.components.AppBottomSheet
import com.merkost.metronome.components.MySecondaryButton
import com.merkost.metronome.components.ValueStepper
import com.merkost.metronome.model.GapTrainerConfig
import com.merkost.metronome.model.GradualTempoConfig
import com.merkost.metronome.model.MAX_BPM
import com.merkost.metronome.model.MIN_BPM
import com.merkost.metronome.model.Subdivision
import com.merkost.metronome.ui.AnimatedNumberText
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.sheetButtonHeight
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall

private val TEMPO_PRESETS = listOf(
    "Largo" to 50,
    "Andante" to 80,
    "Moderato" to 110,
    "Allegro" to 140,
    "Presto" to 180,
)

private val INCREMENT_OPTIONS = listOf(1, 2, 3, 5)
private val BARS_OPTIONS = listOf(1, 2, 4, 8)

@Composable
fun TempoTrainerSheet(
    currentBpm: Int,
    beatsPerBar: Int,
    isPlaying: Boolean,
    subdivision: Subdivision,
    activeConfig: GradualTempoConfig?,
    currentBar: Int,
    lastConfig: GradualTempoConfig?,
    activeGapConfig: GapTrainerConfig?,
    lastGapConfig: GapTrainerConfig?,
    onPresetSelected: (Int) -> Unit,
    onSubdivisionChanged: (Subdivision) -> Unit,
    onStartTrainer: (GradualTempoConfig) -> Unit,
    onStopTrainer: (resetToStart: Boolean) -> Unit,
    onStartGapTrainer: (GapTrainerConfig) -> Unit,
    onUpdateGapTrainer: (GapTrainerConfig) -> Unit,
    onStopGapTrainer: () -> Unit,
    onDismiss: () -> Unit,
) {
    var editing by remember { mutableStateOf(false) }

    AppBottomSheet(title = "Tempo", onDismiss = onDismiss) { dismissAnimated ->
        var lastActiveConfig by remember { mutableStateOf(activeConfig) }
        LaunchedEffect(activeConfig) {
            activeConfig?.let { lastActiveConfig = it }
        }

        Column(verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
            AnimatedContent(
                targetState = activeConfig != null && !editing,
                transitionSpec = { AppAnimations.fadeScaleTransform },
                label = "tempoSheetMode"
            ) { active ->
                val shownConfig = activeConfig ?: lastActiveConfig
                if (active && shownConfig != null) {
                    ActiveTrainerContent(
                        config = shownConfig,
                        currentBpm = currentBpm,
                        currentBar = currentBar,
                        isPlaying = isPlaying,
                        onStopKeep = {
                            onStopTrainer(false)
                            dismissAnimated()
                        },
                        onStopReset = {
                            onStopTrainer(true)
                            dismissAnimated()
                        },
                        onEdit = { editing = true },
                    )
                } else {
                    TempoConfigContent(
                        currentBpm = currentBpm,
                        beatsPerBar = beatsPerBar,
                        isPlaying = isPlaying,
                        seedConfig = activeConfig ?: lastConfig,
                        showPresets = activeConfig == null,
                        onPresetSelected = {
                            onPresetSelected(it)
                            dismissAnimated()
                        },
                        onStartTrainer = {
                            onStartTrainer(it)
                            dismissAnimated()
                        },
                    )
                }
            }

            HorizontalDivider()
            SubdivisionSection(
                subdivision = subdivision,
                onSubdivisionChanged = onSubdivisionChanged,
            )

            HorizontalDivider()
            GapTrainerSection(
                isPlaying = isPlaying,
                activeConfig = activeGapConfig,
                seedConfig = lastGapConfig,
                onUpdate = onUpdateGapTrainer,
                onStartAndDismiss = {
                    onStartGapTrainer(it)
                    dismissAnimated()
                },
                onStop = onStopGapTrainer,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SubdivisionSection(
    subdivision: Subdivision,
    onSubdivisionChanged: (Subdivision) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(spacingSmall))
            Text(
                text = "Subdivision",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )
        }
        Text(
            text = "Extra softer clicks between beats",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(spacingSmall)) {
            Subdivision.entries.forEach { option ->
                FilterChip(
                    selected = subdivision == option,
                    onClick = { onSubdivisionChanged(option) },
                    label = { Text(option.label) }
                )
            }
        }
    }
}

@Composable
private fun GapTrainerSection(
    isPlaying: Boolean,
    activeConfig: GapTrainerConfig?,
    seedConfig: GapTrainerConfig?,
    onUpdate: (GapTrainerConfig) -> Unit,
    onStartAndDismiss: (GapTrainerConfig) -> Unit,
    onStop: () -> Unit,
) {
    val seed = activeConfig ?: seedConfig ?: GapTrainerConfig()
    var playBars by remember { mutableStateOf(seed.playBars) }
    var muteBars by remember { mutableStateOf(seed.muteBars) }
    val active = activeConfig != null

    Column(verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.VolumeOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(spacingSmall))
            Text(
                text = "Gap Trainer",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )
        }
        Text(
            text = "Mutes the click for some bars — keep time yourself",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OptionChipsRow(
            label = "Play",
            options = listOf(2, 4, 8),
            selected = playBars,
            display = { if (it == 1) "$it bar" else "$it bars" },
            onSelect = {
                playBars = it
                if (active) onUpdate(GapTrainerConfig(it, muteBars))
            },
        )
        OptionChipsRow(
            label = "Mute",
            options = listOf(1, 2, 4),
            selected = muteBars,
            display = { if (it == 1) "$it bar" else "$it bars" },
            onSelect = {
                muteBars = it
                if (active) onUpdate(GapTrainerConfig(playBars, it))
            },
        )
        AnimatedContent(
            targetState = active,
            transitionSpec = { AppAnimations.fadeScaleTransform },
            label = "gapTrainerButton"
        ) { running ->
            if (running) {
                MySecondaryButton(
                    onClick = onStop,
                    shape = CircleShape,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SheetSecondaryLabel("Stop gap trainer")
                }
            } else {
                Button(
                    onClick = { onStartAndDismiss(GapTrainerConfig(playBars, muteBars)) },
                    shape = CircleShape,
                    modifier = Modifier.fillMaxWidth().height(sheetButtonHeight),
                ) {
                    Text(
                        text = if (isPlaying) "Start gap trainer" else "Start gap trainer & play",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun TempoConfigContent(
    currentBpm: Int,
    beatsPerBar: Int,
    isPlaying: Boolean,
    seedConfig: GradualTempoConfig?,
    showPresets: Boolean,
    onPresetSelected: (Int) -> Unit,
    onStartTrainer: (GradualTempoConfig) -> Unit,
) {
    var startBpm by remember { mutableStateOf(seedConfig?.startBpm ?: currentBpm) }
    var endBpm by remember {
        mutableStateOf(seedConfig?.endBpm ?: (currentBpm + 40).coerceAtMost(MAX_BPM))
    }
    var increment by remember { mutableStateOf(seedConfig?.increment ?: 2) }
    var barsPerStep by remember { mutableStateOf(seedConfig?.barsPerStep ?: 4) }

    Column(verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
        if (showPresets) {
            FlowRowPresets(currentBpm = currentBpm, onPresetSelected = onPresetSelected)
            HorizontalDivider()
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (endBpm >= startBpm) {
                    Icons.AutoMirrored.Rounded.TrendingUp
                } else {
                    Icons.AutoMirrored.Rounded.TrendingDown
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(spacingSmall))
            Text(
                text = "Tempo Trainer",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )
        }
        Text(
            text = "Changes tempo automatically while you play",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            ValueStepper(
                value = startBpm,
                onValueChange = { startBpm = it },
                range = MIN_BPM..MAX_BPM,
                label = "Start",
            )
            ValueStepper(
                value = endBpm,
                onValueChange = { endBpm = it },
                range = MIN_BPM..MAX_BPM,
                label = "Target",
            )
        }

        OptionChipsRow(
            label = "Step",
            options = INCREMENT_OPTIONS,
            selected = increment,
            display = { if (endBpm >= startBpm) "+$it" else "−$it" },
            onSelect = { increment = it },
        )
        OptionChipsRow(
            label = "Every",
            options = BARS_OPTIONS,
            selected = barsPerStep,
            display = { if (it == 1) "$it bar" else "$it bars" },
            onSelect = { barsPerStep = it },
        )

        val config = GradualTempoConfig(startBpm, endBpm, increment, barsPerStep)
        Text(
            text = trainerSummary(config, beatsPerBar),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Button(
            onClick = { onStartTrainer(config) },
            enabled = startBpm != endBpm,
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth().height(sheetButtonHeight),
        ) {
            Text(
                text = if (isPlaying) "Start trainer" else "Start trainer & play",
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowPresets(currentBpm: Int, onPresetSelected: (Int) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(spacingSmall)) {
        TEMPO_PRESETS.forEach { (name, bpm) ->
            FilterChip(
                selected = currentBpm == bpm,
                onClick = { onPresetSelected(bpm) },
                label = { Text("$name $bpm") }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OptionChipsRow(
    label: String,
    options: List<Int>,
    selected: Int,
    display: (Int) -> String,
    onSelect: (Int) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacingSmall),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(44.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacingSmall),
            modifier = Modifier.weight(1f)
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = selected == option,
                    onClick = { onSelect(option) },
                    label = { Text(display(option)) }
                )
            }
        }
    }
}

@Composable
private fun ActiveTrainerContent(
    config: GradualTempoConfig,
    currentBpm: Int,
    currentBar: Int,
    isPlaying: Boolean,
    onStopKeep: () -> Unit,
    onStopReset: () -> Unit,
    onEdit: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(spacingMedium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedNumberText(
                    value = currentBpm,
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                )
                Spacer(Modifier.width(spacingSmall))
                Text(
                    text = "BPM",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val direction = if (config.ascending) "+" else "−"
            Text(
                text = "${config.startBpm} → ${config.endBpm} · $direction${config.increment} every ${config.barsPerStep} bars",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = when {
                    config.isComplete(currentBpm) -> "Target reached"
                    isPlaying -> "Bar $currentBar of ${config.totalBars}"
                    else -> "Paused — advances while playing"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(spacingSmall),
            modifier = Modifier.fillMaxWidth()
        ) {
            MySecondaryButton(
                onClick = onStopKeep,
                shape = CircleShape,
                modifier = Modifier.weight(1f),
            ) {
                SheetSecondaryLabel("Stop, keep $currentBpm")
            }
            MySecondaryButton(
                onClick = onStopReset,
                shape = CircleShape,
                modifier = Modifier.weight(1f),
            ) {
                SheetSecondaryLabel("Reset to ${config.startBpm}")
            }
        }

        TextButton(onClick = onEdit) {
            Text("Edit settings", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SheetSecondaryLabel(text: String) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().height(sheetButtonHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
        )
    }
}

private fun trainerSummary(config: GradualTempoConfig, beatsPerBar: Int): String {
    val averageBpm = (config.startBpm + config.endBpm) / 2f
    val minutes = (config.totalBars * beatsPerBar / averageBpm).toInt().coerceAtLeast(1)
    return "${config.totalBars} bars · ≈ $minutes min"
}
