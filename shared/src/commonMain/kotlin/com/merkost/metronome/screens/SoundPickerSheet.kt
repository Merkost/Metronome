package com.merkost.metronome.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Headphones
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Square
import com.composables.icons.lucide.Volume2
import com.merkost.metronome.components.AppBottomSheet
import com.merkost.metronome.components.AppIconButton
import com.merkost.metronome.model.ClickSound
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.cornerRadiusLarge
import com.merkost.metronome.ui.pressableSurface
import com.merkost.metronome.ui.spacingMedium
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
) {
    AppBottomSheet(title = "Pick your click.", onDismiss = onDismiss) {
        SoundPickerContent(selectedSound, previewSound, onSelect, onPreview, errorMessage)
    }
}

@Composable
private fun SoundPickerContent(
    selectedSound: ClickSound,
    previewSound: ClickSound?,
    onSelect: (ClickSound) -> Unit,
    onPreview: (ClickSound) -> Unit,
    errorMessage: String? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
        Row(horizontalArrangement = Arrangement.spacedBy(spacingSmall), verticalAlignment = Alignment.CenterVertically) {
            Icon(Lucide.Headphones, null)
            Text("Preview · one bar at 100 BPM", style = MaterialTheme.typography.bodyMedium)
        }
        ClickSound.entries.forEach { sound ->
            val chosen = sound == selectedSound
            val color by animateColorAsState(
                if (chosen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                AppAnimations.standard(), label = "soundSelection",
            )
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(cornerRadiusLarge)).background(color),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f).semantics { selected = chosen }
                        .pressableSurface(onClick = { onSelect(sound) }).padding(spacingMedium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacingSmall),
                ) {
                    AnimatedContent(chosen, transitionSpec = { AppAnimations.fadeScaleTransform }, label = "chosenSound") { selected ->
                        Icon(if (selected) Lucide.Check else Lucide.Volume2, null)
                    }
                    Column {
                        Text(sound.displayName, fontWeight = FontWeight.Bold)
                        Text(sound.description, style = MaterialTheme.typography.bodySmall)
                    }
                }
                AppIconButton(onClick = { onPreview(sound) }) {
                    AnimatedContent(previewSound == sound, transitionSpec = { AppAnimations.fadeScaleTransform }, label = "soundPreview") { playing ->
                        Icon(if (playing) Lucide.Square else Lucide.Play, if (playing) "Stop preview" else "Preview ${sound.displayName}")
                    }
                }
            }
        }
        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        Text("Previewing does not add to your practice time.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
