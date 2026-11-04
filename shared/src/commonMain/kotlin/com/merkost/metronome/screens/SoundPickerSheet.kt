package com.merkost.metronome.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Square
import com.merkost.metronome.components.AppBottomSheet
import com.merkost.metronome.components.AppIconButton
import com.merkost.metronome.components.SoundToneHistogram
import com.merkost.metronome.model.ClickSound
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.cornerRadiusLarge
import com.merkost.metronome.ui.pressableSurface
import com.merkost.metronome.ui.minimumTouchTargetSize
import com.merkost.metronome.ui.spacingSmall
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SoundPickerSheet(
    selectedSound: ClickSound,
    previewSound: ClickSound?,
    onSelect: (ClickSound) -> Unit,
    onPreview: (ClickSound) -> Unit,
    onDismiss: () -> Unit,
    errorMessage: String? = null,
    previewEnabled: Boolean = true,
) {
    AppBottomSheet(title = "Click sound", onDismiss = onDismiss) {
        SoundPickerContent(selectedSound, previewSound, onSelect, onPreview, errorMessage, previewEnabled)
    }
}

@Composable
internal fun SoundPickerContent(
    selectedSound: ClickSound,
    previewSound: ClickSound?,
    onSelect: (ClickSound) -> Unit,
    onPreview: (ClickSound) -> Unit,
    errorMessage: String? = null,
    previewEnabled: Boolean = true,
) {
    Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
        AnimatedContent(
            targetState = previewEnabled,
            transitionSpec = { AppAnimations.fadeThrough },
            label = "soundPreviewAvailability",
        ) { canPreview ->
            Text(
                if (canPreview) "Choose a sound. Preview one bar at 100 BPM." else "Choose a sound. Pause playback to preview.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text("Tone profile · low → high", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ClickSound.entries.forEach { sound ->
            SoundPickerRow(sound, sound == selectedSound, previewSound == sound, { onSelect(sound) }, { onPreview(sound) }, previewEnabled)
        }
        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun SoundPickerRow(
    sound: ClickSound,
    chosen: Boolean,
    previewing: Boolean,
    onSelect: () -> Unit,
    onPreview: () -> Unit,
    previewEnabled: Boolean = true,
) {
    val color by animateColorAsState(
        if (chosen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        AppAnimations.standard(), label = "soundSelection",
    )
    val titleStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
    val measurer = rememberTextMeasurer()
    val minimumTitleWidth = with(LocalDensity.current) {
        ClickSound.entries.maxOf { measurer.measure(AnnotatedString(it.displayName), titleStyle, softWrap = false, maxLines = 1).size.width }.toDp()
    }
    val profileWidth = 72.dp
    val selectionTint by animateColorAsState(
        if (chosen) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        AppAnimations.standard(), label = "soundSelectionContent",
    )
    val previewAlpha by animateFloatAsState(
        if (previewEnabled) 1f else 0.38f,
        AppAnimations.standard(), label = "soundPreviewEnabledAlpha",
    )
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(cornerRadiusLarge)).background(color),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BoxWithConstraints(
            modifier = Modifier.weight(1f).semantics { selected = chosen }
                .pressableSurface(onClick = onSelect, role = Role.RadioButton)
                .heightIn(min = minimumTouchTargetSize).padding(12.dp),
        ) {
            val inlineProfile = maxWidth >= minimumTitleWidth + profileWidth + 20.dp + spacingSmall * 2
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(spacingSmall / 2)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacingSmall)) {
                    Text(sound.displayName, modifier = Modifier.weight(1f), style = titleStyle, color = selectionTint)
                    if (inlineProfile) SoundToneHistogram(sound, chosen, Modifier.width(profileWidth))
                    Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                        AnimatedContent(chosen, transitionSpec = { AppAnimations.fadeScaleTransform }, label = "chosenSound") { selected ->
                            if (selected) Icon(Lucide.Check, null, tint = selectionTint, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                Text(sound.description, style = MaterialTheme.typography.bodySmall, color = selectionTint)
                if (!inlineProfile) SoundToneHistogram(sound, chosen, Modifier.width(profileWidth))
            }
        }
        AppIconButton(
            onClick = onPreview,
            enabled = previewEnabled,
            modifier = Modifier.size(minimumTouchTargetSize).semantics {
                contentDescription = if (previewing) "Stop ${sound.displayName} preview" else "Preview ${sound.displayName}"
            },
        ) {
            AnimatedContent(previewing, modifier = Modifier.clearAndSetSemantics {}, transitionSpec = { AppAnimations.fadeScaleTransform }, label = "soundPreview") { playing ->
                Icon(if (playing) Lucide.Square else Lucide.Play, null, tint = selectionTint.copy(alpha = selectionTint.alpha * previewAlpha), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Preview
@Composable
private fun SoundPickerContentPreview() {
    MaterialTheme { SoundPickerContent(ClickSound.WOOD, null, {}, {}) }
}

@Preview
@Composable
private fun SoundPickerSheetPreview() {
    MaterialTheme { SoundPickerSheet(ClickSound.WOOD, null, {}, {}, {}) }
}

@Preview
@Composable
private fun SoundPickerRowPreview() {
    MaterialTheme { SoundPickerRow(ClickSound.WOOD, true, false, {}, {}) }
}
