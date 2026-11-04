package com.merkost.metronome.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.merkost.metronome.components.AppChip
import com.merkost.metronome.components.MySecondaryButton
import com.merkost.metronome.model.GradualTempoConfig
import com.merkost.metronome.model.TempoStepMode
import com.merkost.metronome.ui.sheetButtonHeight
import com.merkost.metronome.ui.spacingSmall

fun gapCycleFallbackCaption(barsPerStep: Int): String =
    "Gap trainer is off · steps every $barsPerStep bars until it starts"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrainerStepModeRow(
    selected: TempoStepMode,
    gapRunning: Boolean,
    barsPerStep: Int,
    onSelect: (TempoStepMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacingSmall),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Steps",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(44.dp),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(spacingSmall),
                verticalArrangement = Arrangement.spacedBy(spacingSmall),
            ) {
                AppChip(
                    selected = selected == TempoStepMode.BARS,
                    onClick = { onSelect(TempoStepMode.BARS) },
                    label = "Bars",
                )
                AppChip(
                    selected = selected == TempoStepMode.GAP_CYCLE,
                    onClick = { onSelect(TempoStepMode.GAP_CYCLE) },
                    label = "Gap cycle",
                )
                AppChip(
                    selected = selected == TempoStepMode.MANUAL,
                    onClick = { onSelect(TempoStepMode.MANUAL) },
                    label = "When I tap",
                )
            }
        }
        if (selected == TempoStepMode.GAP_CYCLE && !gapRunning) {
            Text(
                text = gapCycleFallbackCaption(barsPerStep),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun ManualStepControls(
    config: GradualTempoConfig,
    currentBpm: Int,
    onStepForward: () -> Unit,
    onStepBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val complete = config.isComplete(currentBpm)
    val atStart = config.stepsTaken(currentBpm) == 0
    val stacked = LocalDensity.current.fontScale > 1.3f
    val back: @Composable (Modifier) -> Unit = { buttonModifier ->
        MySecondaryButton(
            onClick = onStepBack,
            enabled = !atStart,
            shape = CircleShape,
            modifier = buttonModifier,
        ) {
            SheetSecondaryLabel("Step back")
        }
    }
    val forward: @Composable (Modifier) -> Unit = { buttonModifier ->
        Button(
            onClick = onStepForward,
            enabled = !complete,
            shape = CircleShape,
            modifier = buttonModifier.heightIn(min = sheetButtonHeight),
        ) {
            Text(text = "Next step", fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
    if (stacked) {
        Column(
            verticalArrangement = Arrangement.spacedBy(spacingSmall),
            modifier = modifier.fillMaxWidth(),
        ) {
            forward(Modifier.fillMaxWidth())
            back(Modifier.fillMaxWidth())
        }
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacingSmall),
            modifier = modifier.fillMaxWidth(),
        ) {
            back(Modifier.weight(1f))
            forward(Modifier.weight(1f))
        }
    }
}
