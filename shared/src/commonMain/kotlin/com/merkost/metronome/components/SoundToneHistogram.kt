package com.merkost.metronome.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.merkost.metronome.model.ClickSound
import com.merkost.metronome.model.spectrum
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.spacingSmall
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun SoundToneHistogram(
    sound: ClickSound,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val tint by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        AppAnimations.standard(),
        label = "soundToneTint",
    )
    val bars = sound.spectrum.bars
    Canvas(modifier.fillMaxWidth().height(spacingSmall * 3).clearAndSetSemantics {}) {
        val baseline = 1.dp.toPx()
        drawLine(tint.copy(alpha = 0.16f), Offset(0f, size.height - baseline / 2f), Offset(size.width, size.height - baseline / 2f), strokeWidth = baseline)
        val step = size.width / bars.size
        val width = step * 0.65f
        bars.forEachIndexed { index, amplitude ->
            val height = amplitude * size.height
            if (height > 0f) {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(index * step + (step - width) / 2f, size.height - height),
                    size = Size(width, height),
                    cornerRadius = CornerRadius(width / 2f, width / 2f),
                )
            }
        }
    }
}

@Preview
@Composable
private fun SoundToneHistogramPreview() {
    MaterialTheme { SoundToneHistogram(ClickSound.WOOD, false) }
}

@Preview
@Composable
private fun SoundToneHistogramSelectedPreview() {
    MaterialTheme { SoundToneHistogram(ClickSound.STUDIO, true) }
}
