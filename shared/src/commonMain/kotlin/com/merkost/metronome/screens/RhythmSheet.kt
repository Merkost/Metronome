package com.merkost.metronome.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import com.merkost.metronome.components.AppBottomSheet
import com.merkost.metronome.components.AppChip
import com.merkost.metronome.components.MetronomeBalls
import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.MetronomeState
import com.merkost.metronome.model.Subdivision
import com.merkost.metronome.model.TimeSignature
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.BallSize
import com.merkost.metronome.ui.BallSizeCompact
import com.merkost.metronome.ui.spacingLarge
import com.merkost.metronome.ui.minimumTouchTargetSize
import com.merkost.metronome.ui.pressableSurface
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RhythmSheet(
    state: MetronomeState,
    selectedIndex: Int,
    countInEnabled: Boolean,
    onCountInChanged: (Boolean) -> Unit,
    onTimeSignatureChanged: (TimeSignature) -> Unit,
    onSubdivisionChanged: (Subdivision) -> Unit,
    onBeatChanged: (Int, Beat) -> Unit,
    onDismiss: () -> Unit,
) {
    AppBottomSheet(title = "Rhythm", onDismiss = onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(spacingLarge)) {
            Column(verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                Text("Time signature", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(spacingSmall),
                    verticalArrangement = Arrangement.spacedBy(spacingSmall),
                ) {
                    TimeSignature.entries.forEach { signature ->
                        AppChip(
                            selected = state.timeSignature == signature,
                            onClick = { onTimeSignatureChanged(signature) },
                            label = signature.label,
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
                Text("Shape the bar", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                MetronomeBalls(
                    selectedIndex = selectedIndex,
                    beats = state.beats,
                    isPlaying = state.playing,
                    animSpec = AppAnimations.Emphasized,
                    arrangementSpacing = spacingSmall,
                    ballSize = if (state.beats.size > 5) BallSizeCompact else BallSize,
                    modifier = Modifier.fillMaxWidth().animateContentSize(AppAnimations.emphasized()),
                    onBallClicked = onBeatChanged,
                )
                Text(
                    "Tap a beat to cycle through normal, accent and mute. The ring follows playback.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                Text("Subdivision", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                Text("Softer clicks between your beats.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(spacingSmall),
                    verticalArrangement = Arrangement.spacedBy(spacingSmall),
                ) {
                    Subdivision.entries.forEach { subdivision ->
                        AppChip(
                            selected = state.subdivision == subdivision,
                            onClick = { onSubdivisionChanged(subdivision) },
                            label = subdivision.label,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = minimumTouchTargetSize)
                    .semantics(mergeDescendants = true) {
                        toggleableState = if (countInEnabled) ToggleableState.On else ToggleableState.Off
                    }
                    .pressableSurface(onClick = { onCountInChanged(!countInEnabled) }, role = Role.Switch)
                    .padding(vertical = spacingSmall),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacingSmall),
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                    Text("Count-in", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("Hear one bar before playback starts.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = countInEnabled, onCheckedChange = null)
            }
            Text(
                "A preset remembers this rhythm, tempo and count-in setting.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun RhythmSheetPreview() {
    MaterialTheme {
        RhythmSheet(MetronomeState(), -1, false, {}, {}, {}, { _, _ -> }, {})
    }
}
