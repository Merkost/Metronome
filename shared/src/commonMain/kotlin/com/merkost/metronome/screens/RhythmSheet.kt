package com.merkost.metronome.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
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
import com.merkost.metronome.ui.cornerRadiusXLarge
import com.merkost.metronome.ui.spacingLarge
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RhythmSheet(state: MetronomeState, selectedIndex: Int, countInEnabled: Boolean, onCountInChanged: (Boolean) -> Unit, onTimeSignatureChanged: (TimeSignature) -> Unit, onSubdivisionChanged: (Subdivision) -> Unit, onBeatChanged: (Int, Beat) -> Unit, onDismiss: () -> Unit) {
    AppBottomSheet(title = "Rhythm", onDismiss = onDismiss) {
        RhythmContent(state, selectedIndex, countInEnabled, onCountInChanged, onTimeSignatureChanged, onSubdivisionChanged, onBeatChanged)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RhythmContent(state: MetronomeState, selectedIndex: Int, countInEnabled: Boolean, onCountInChanged: (Boolean) -> Unit, onTimeSignatureChanged: (TimeSignature) -> Unit, onSubdivisionChanged: (Subdivision) -> Unit, onBeatChanged: (Int, Beat) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(spacingLarge)) {
        RhythmPanel {
            Column(verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                Text("Time signature", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.semantics { heading() })
                FlowRow(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(spacingSmall), verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                    TimeSignature.entries.forEach { signature ->
                        AppChip(state.timeSignature == signature, { onTimeSignatureChanged(signature) }, signature.label, Modifier.semantics { selected = state.timeSignature == signature; role = Role.RadioButton })
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                Text("Beat pattern", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.semantics { heading() })
                Text("Tap a beat to change its voice.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                MetronomeBalls(selectedIndex, state.beats, state.playing, AppAnimations.Emphasized, arrangementSpacing = spacingSmall, ballSize = if (state.beats.size > 5) BallSizeCompact else BallSize, modifier = Modifier.fillMaxWidth().animateContentSize(AppAnimations.emphasized()), onBallClicked = onBeatChanged)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(spacingMedium), verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                    BeatLegend(Beat.HIGH, "Accent")
                    BeatLegend(Beat.LOW, "Normal")
                    BeatLegend(Beat.MUTE, "Muted")
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
            Text("Subdivision", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.semantics { heading() })
            Text("Softer clicks between the main beats.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(spacingSmall), verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                Subdivision.entries.forEach { subdivision ->
                    AppChip(state.subdivision == subdivision, { onSubdivisionChanged(subdivision) }, subdivision.label, Modifier.semantics { selected = state.subdivision == subdivision; role = Role.RadioButton })
                }
            }
        }
        RhythmPanel {
            SettingsSwitch("Count-in", countInEnabled, onCountInChanged, "One bar before playback.")
        }
        Text("Presets keep your rhythm and count-in.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RhythmPanel(content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().animateContentSize(AppAnimations.emphasized()), shape = RoundedCornerShape(cornerRadiusXLarge), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(Modifier.padding(spacingMedium), verticalArrangement = Arrangement.spacedBy(spacingMedium), content = content)
    }
}

@Composable
private fun BeatLegend(beat: Beat, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacingSmall / 2)) {
        val glyph = Modifier.size(12.dp).clip(CircleShape).clearAndSetSemantics {}
        Box(if (beat == Beat.MUTE) glyph.border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape) else glyph.background(if (beat == Beat.HIGH) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
            if (beat == Beat.MUTE) Icon(Lucide.X, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(8.dp))
        }
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview
@Composable
private fun RhythmSheetPreview() {
    MaterialTheme { RhythmSheet(MetronomeState(), -1, false, {}, {}, {}, { _, _ -> }, {}) }
}

@Preview
@Composable
private fun RhythmContentPreview() {
    MaterialTheme { RhythmContent(MetronomeState(), -1, false, {}, {}, {}, { _, _ -> }) }
}

@Preview
@Composable
private fun RhythmPanelPreview() {
    MaterialTheme { RhythmPanel { Text("Beat pattern") } }
}

@Preview
@Composable
private fun BeatLegendPreview() {
    MaterialTheme { BeatLegend(Beat.HIGH, "Accent") }
}
