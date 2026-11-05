package com.merkost.metronome.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.merkost.metronome.components.AppBottomSheet
import com.merkost.metronome.components.MetronomeBalls
import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.MetronomeState
import com.merkost.metronome.model.Subdivision
import com.merkost.metronome.model.TimeSignature
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.BallSize
import com.merkost.metronome.ui.BallSizeCompact
import com.merkost.metronome.ui.minimumTouchTargetSize
import com.merkost.metronome.ui.pressableSurface
import com.merkost.metronome.ui.rememberAppHaptics
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall
import org.jetbrains.compose.ui.tooling.preview.Preview
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.rhythm_beat_pattern
import metronome.shared.generated.resources.rhythm_beat_pattern_hint
import metronome.shared.generated.resources.rhythm_count_in
import metronome.shared.generated.resources.rhythm_count_in_hint
import metronome.shared.generated.resources.rhythm_legend_accent
import metronome.shared.generated.resources.rhythm_legend_muted
import metronome.shared.generated.resources.rhythm_legend_normal
import metronome.shared.generated.resources.rhythm_presets_hint
import metronome.shared.generated.resources.rhythm_subdivision
import metronome.shared.generated.resources.rhythm_subdivision_hint
import metronome.shared.generated.resources.rhythm_time_signature
import metronome.shared.generated.resources.rhythm_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun RhythmSheet(state: MetronomeState, selectedIndex: Int, countInEnabled: Boolean, onCountInChanged: (Boolean) -> Unit, onTimeSignatureChanged: (TimeSignature) -> Unit, onSubdivisionChanged: (Subdivision) -> Unit, onBeatChanged: (Int, Beat) -> Unit, onDismiss: () -> Unit) {
    AppBottomSheet(title = stringResource(Res.string.rhythm_title), onDismiss = onDismiss) {
        RhythmContent(state, selectedIndex, countInEnabled, onCountInChanged, onTimeSignatureChanged, onSubdivisionChanged, onBeatChanged)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RhythmContent(state: MetronomeState, selectedIndex: Int, countInEnabled: Boolean, onCountInChanged: (Boolean) -> Unit, onTimeSignatureChanged: (TimeSignature) -> Unit, onSubdivisionChanged: (Subdivision) -> Unit, onBeatChanged: (Int, Beat) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(spacingMedium + spacingSmall)) {
        Column(verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
            Text(stringResource(Res.string.rhythm_time_signature), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.semantics { heading() })
            FlowRow(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(spacingSmall), verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                TimeSignature.entries.forEach { signature ->
                    RhythmChoice(signature.label, state.timeSignature == signature) { onTimeSignatureChanged(signature) }
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
            Text(stringResource(Res.string.rhythm_beat_pattern), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.semantics { heading() })
            Text(stringResource(Res.string.rhythm_beat_pattern_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            MetronomeBalls(selectedIndex, state.beats, state.playing, AppAnimations.Emphasized, arrangementSpacing = spacingSmall, ballSize = if (state.beats.size > 5) BallSizeCompact else BallSize, modifier = Modifier.fillMaxWidth().animateContentSize(AppAnimations.emphasized()), onBallClicked = onBeatChanged)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(spacingMedium), verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                BeatLegend(Beat.HIGH, stringResource(Res.string.rhythm_legend_accent))
                BeatLegend(Beat.LOW, stringResource(Res.string.rhythm_legend_normal))
                BeatLegend(Beat.MUTE, stringResource(Res.string.rhythm_legend_muted))
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
            Text(stringResource(Res.string.rhythm_subdivision), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.semantics { heading() })
            Text(stringResource(Res.string.rhythm_subdivision_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(spacingSmall), verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                Subdivision.entries.forEach { subdivision ->
                    RhythmChoice(stringResource(subdivision.labelRes), state.subdivision == subdivision) { onSubdivisionChanged(subdivision) }
                }
            }
        }
        SettingsSwitch(stringResource(Res.string.rhythm_count_in), countInEnabled, onCountInChanged, stringResource(Res.string.rhythm_count_in_hint))
        Text(stringResource(Res.string.rhythm_presets_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RhythmChoice(label: String, chosen: Boolean, onClick: () -> Unit) {
    val haptics = rememberAppHaptics()
    val background by animateColorAsState(
        if (chosen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        AppAnimations.standard(), label = "rhythmChoiceBackground",
    )
    val foreground by animateColorAsState(
        if (chosen) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        AppAnimations.standard(), label = "rhythmChoiceForeground",
    )
    Surface(
        shape = CircleShape,
        color = background,
        modifier = Modifier.clip(CircleShape).semantics { selected = chosen }
            .pressableSurface(onClick = { haptics.select(); onClick() }, role = Role.RadioButton),
    ) {
        Box(Modifier.heightIn(min = minimumTouchTargetSize).widthIn(min = minimumTouchTargetSize).padding(horizontal = spacingMedium, vertical = spacingSmall), contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = foreground)
        }
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
private fun RhythmChoicePreview() {
    MaterialTheme { RhythmChoice("4/4", true, {}) }
}

@Preview
@Composable
private fun BeatLegendPreview() {
    MaterialTheme { BeatLegend(Beat.HIGH, "Accent") }
}
