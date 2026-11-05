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
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.trainer_gap_off_fallback
import metronome.shared.generated.resources.trainer_mode_bars
import metronome.shared.generated.resources.trainer_mode_gap_cycle
import metronome.shared.generated.resources.trainer_mode_manual
import metronome.shared.generated.resources.trainer_step_back
import metronome.shared.generated.resources.trainer_step_next
import metronome.shared.generated.resources.trainer_steps_label
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

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
                text = stringResource(Res.string.trainer_steps_label),
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
                    label = stringResource(Res.string.trainer_mode_bars),
                )
                AppChip(
                    selected = selected == TempoStepMode.GAP_CYCLE,
                    onClick = { onSelect(TempoStepMode.GAP_CYCLE) },
                    label = stringResource(Res.string.trainer_mode_gap_cycle),
                )
                AppChip(
                    selected = selected == TempoStepMode.MANUAL,
                    onClick = { onSelect(TempoStepMode.MANUAL) },
                    label = stringResource(Res.string.trainer_mode_manual),
                )
            }
        }
        if (selected == TempoStepMode.GAP_CYCLE && !gapRunning) {
            Text(
                text = pluralStringResource(Res.plurals.trainer_gap_off_fallback, barsPerStep, barsPerStep),
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
            SheetSecondaryLabel(stringResource(Res.string.trainer_step_back))
        }
    }
    val forward: @Composable (Modifier) -> Unit = { buttonModifier ->
        Button(
            onClick = onStepForward,
            enabled = !complete,
            shape = CircleShape,
            modifier = buttonModifier.heightIn(min = sheetButtonHeight),
        ) {
            Text(text = stringResource(Res.string.trainer_step_next), fontWeight = FontWeight.Bold, maxLines = 1)
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
