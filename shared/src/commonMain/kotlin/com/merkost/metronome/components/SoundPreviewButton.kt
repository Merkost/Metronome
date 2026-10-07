package com.merkost.metronome.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Square
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.minimumTouchTargetSize
import com.merkost.metronome.ui.spacingSmall
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.ceil

private val PreviewIconSize = 18.dp

internal data class SoundPreviewMetrics(val labelStyle: TextStyle, val labelWidth: Dp, val labelHeight: Dp) {
    val width: Dp get() = maxOf(minimumTouchTargetSize, labelWidth + PreviewIconSize + spacingSmall / 2 + spacingSmall * 3)
}

@Composable
internal fun rememberSoundPreviewMetrics(): SoundPreviewMetrics {
    val style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    return remember(measurer, style, density) {
        val labels = listOf("Preview", "Stop").map { measurer.measure(AnnotatedString(it), style, softWrap = false, maxLines = 1) }
        SoundPreviewMetrics(
            labelStyle = style,
            labelWidth = with(density) { ceil(labels.maxOf { it.multiParagraph.intrinsics.maxIntrinsicWidth }).toInt().toDp() },
            labelHeight = with(density) { ceil(labels.maxOf { it.multiParagraph.height }).toInt().toDp() },
        )
    }
}

@Composable
fun SoundPreviewButton(
    previewing: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val metrics = rememberSoundPreviewMetrics()
    val labelStyle = metrics.labelStyle
    val interactions = remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    val focused by interactions.collectIsFocusedAsState()
    val feedback by animateColorAsState(
        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = when { !enabled -> 0f; pressed -> 0.08f; focused -> 0.12f; else -> 0f }),
        AppAnimations.press(),
        label = "previewInteraction",
    )
    val foreground by animateColorAsState(
        if (enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        AppAnimations.standard(),
        label = "previewForeground",
    )
    Surface(
        modifier = modifier.clip(CircleShape)
            .clickable(interactionSource = interactions, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = if (previewing) "Stop sound preview" else "Preview click sound" },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(
            Modifier.heightIn(min = minimumTouchTargetSize).background(feedback)
                .padding(horizontal = spacingSmall * 1.5f, vertical = spacingSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacingSmall / 2),
        ) {
            Box(Modifier.size(PreviewIconSize).clearAndSetSemantics {}, contentAlignment = Alignment.Center) {
                AnimatedContent(previewing, modifier = Modifier.fillMaxSize(), transitionSpec = { AppAnimations.fadeScaleTransform }, contentAlignment = Alignment.Center, label = "previewIcon") { active ->
                    Icon(if (active) Lucide.Square else Lucide.Play, null, tint = foreground, modifier = Modifier.size(PreviewIconSize))
                }
            }
            Box(Modifier.width(metrics.labelWidth).height(metrics.labelHeight).clearAndSetSemantics {}, contentAlignment = Alignment.Center) {
                AnimatedContent(previewing, modifier = Modifier.fillMaxSize(), transitionSpec = { AppAnimations.fadeThroughFixedSize }, contentAlignment = Alignment.Center, label = "previewLabel") { active ->
                    Text(if (active) "Stop" else "Preview", style = labelStyle, color = foreground, softWrap = false, maxLines = 1)
                }
            }
        }
    }
}

@Preview
@Composable
private fun SoundPreviewButtonPreview() {
    MaterialTheme { SoundPreviewButton(false, {}) }
}

@Preview
@Composable
private fun SoundPreviewButtonActivePreview() {
    MaterialTheme { SoundPreviewButton(true, {}) }
}

@Preview
@Composable
private fun SoundPreviewButtonDisabledPreview() {
    MaterialTheme { SoundPreviewButton(false, {}, enabled = false) }
}
