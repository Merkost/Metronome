package com.merkost.metronome.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Constraints
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Headphones
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Sparkles
import com.merkost.metronome.model.ClickSound
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.cornerRadiusLarge
import com.merkost.metronome.ui.minimumTouchTargetSize
import com.merkost.metronome.ui.pressableSurface
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.ceil

private val ShortcutIconSize = 20.dp
private val ShortcutChevronSize = 16.dp
private val ShortcutTextGap = spacingSmall / 4

private data class ShortcutMetrics(val titleStyle: TextStyle, val titleHeight: Dp, val captionHeight: Dp, val height: Dp, val iconsAbove: Boolean)

@Composable
internal fun MainShortcuts(selectedSound: ClickSound, onPractice: () -> Unit, onSound: () -> Unit, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val titleStyle = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
    val captionStyle = MaterialTheme.typography.bodySmall
    val captions = remember { listOf("Timer & trainers") + ClickSound.entries.map { it.displayName } }
    val minimumTextWidth = remember(measurer, titleStyle, captionStyle) {
        maxOf(
            listOf("Practice", "Sound").maxOf { measurer.measure(AnnotatedString(it), titleStyle).multiParagraph.intrinsics.maxIntrinsicWidth },
            captions.maxOf { measurer.measure(AnnotatedString(it), captionStyle).multiParagraph.intrinsics.minIntrinsicWidth },
        )
    }
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val minimum = with(density) { ceil(minimumTextWidth).toInt().toDp() }
        val halfWidth = (maxWidth - spacingSmall) / 2
        val sideBySide = halfWidth >= minimum + spacingMedium * 2
        val cardWidth = if (sideBySide) halfWidth else maxWidth
        val horizontalExtras = ShortcutIconSize + ShortcutChevronSize + spacingSmall * 2
        val iconsAbove = sideBySide && cardWidth < minimum + spacingMedium * 2 + horizontalExtras
        val textWidth = with(density) { (cardWidth - spacingMedium * 2 - if (iconsAbove) 0.dp else horizontalExtras).toPx().toInt().coerceAtLeast(1) }
        val metrics = remember(measurer, density, titleStyle, captionStyle, textWidth, iconsAbove) {
            val constraints = Constraints(maxWidth = textWidth)
            val titleHeight = with(density) { listOf("Practice", "Sound").maxOf { measurer.measure(AnnotatedString(it), titleStyle, constraints = constraints).size.height }.toDp() }
            val captionHeight = with(density) { captions.maxOf { measurer.measure(AnnotatedString(it), captionStyle, constraints = constraints).size.height }.toDp() }
            val textHeight = titleHeight + ShortcutTextGap + captionHeight
            val contentHeight = if (iconsAbove) ShortcutIconSize + spacingSmall + textHeight else maxOf(ShortcutIconSize, textHeight)
            ShortcutMetrics(titleStyle, titleHeight, captionHeight, maxOf(minimumTouchTargetSize, contentHeight + spacingMedium * 2), iconsAbove)
        }
        if (sideBySide) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacingSmall)) {
                MainShortcut(Lucide.Sparkles, "Practice", "Timer & trainers", onPractice, metrics, Modifier.weight(1f))
                MainShortcut(Lucide.Headphones, "Sound", selectedSound.displayName, onSound, metrics, Modifier.weight(1f))
            }
        } else {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                MainShortcut(Lucide.Sparkles, "Practice", "Timer & trainers", onPractice, metrics, Modifier.fillMaxWidth())
                MainShortcut(Lucide.Headphones, "Sound", selectedSound.displayName, onSound, metrics, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun MainShortcut(icon: ImageVector, title: String, caption: String, onClick: () -> Unit, metrics: ShortcutMetrics, modifier: Modifier = Modifier) {
    val chevron = if (LocalLayoutDirection.current == LayoutDirection.Rtl) Lucide.ChevronLeft else Lucide.ChevronRight
    val container = modifier.height(metrics.height).clip(RoundedCornerShape(cornerRadiusLarge))
        .background(MaterialTheme.colorScheme.surfaceContainer).pressableSurface(onClick)
        .semantics(mergeDescendants = true) { contentDescription = "$title. $caption" }.padding(spacingMedium)
    if (metrics.iconsAbove) {
        Column(container, verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(ShortcutIconSize))
                Icon(chevron, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(ShortcutChevronSize))
            }
            ShortcutText(title, caption, metrics)
        }
    } else {
        Row(container, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacingSmall)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(ShortcutIconSize))
            ShortcutText(title, caption, metrics, Modifier.weight(1f))
            Icon(chevron, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(ShortcutChevronSize))
        }
    }
}

@Composable
private fun ShortcutText(title: String, caption: String, metrics: ShortcutMetrics, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(ShortcutTextGap)) {
        Text(title, modifier = Modifier.fillMaxWidth().height(metrics.titleHeight), style = metrics.titleStyle, color = MaterialTheme.colorScheme.onSurface)
        AnimatedContent(caption, modifier = Modifier.fillMaxWidth().height(metrics.captionHeight), transitionSpec = { AppAnimations.fadeThroughFixedSize }, contentAlignment = Alignment.TopStart, label = "mainShortcutCaption") { text ->
            Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview
@Composable
private fun MainShortcutsPreview() {
    MaterialTheme { MainShortcuts(ClickSound.WOOD, {}, {}) }
}

@Preview
@Composable
private fun MainShortcutPreview() {
    MaterialTheme { MainShortcuts(ClickSound.STUDIO, {}, {}) }
}

@Preview
@Composable
private fun ShortcutTextPreview() {
    MaterialTheme { MainShortcuts(ClickSound.CLICK, {}, {}) }
}
