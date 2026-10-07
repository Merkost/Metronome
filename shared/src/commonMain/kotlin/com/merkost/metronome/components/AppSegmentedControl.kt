package com.merkost.metronome.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.cornerRadiusLarge
import com.merkost.metronome.ui.cornerRadiusMedium
import com.merkost.metronome.ui.minimumTouchTargetSize
import com.merkost.metronome.ui.rememberAppHaptics
import com.merkost.metronome.ui.spacingSmall
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.ceil

@Composable
fun AppSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    require(options.isNotEmpty())
    require(selectedIndex in options.indices)
    val haptics = rememberAppHaptics()
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val position by animateFloatAsState(selectedIndex.toFloat(), AppAnimations.emphasized(), label = "segmentPosition")
    val indicator by animateColorAsState(MaterialTheme.colorScheme.primaryContainer, AppAnimations.standard(), label = "segmentIndicator")
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val labelStyle = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
    val inset = spacingSmall / 2
    val labelInset = spacingSmall / 4
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val cellWidth = with(density) { (maxWidth - inset * 2).toPx() } / options.size
        val widestLabel = options.maxOf { measurer.measure(AnnotatedString(it), labelStyle, softWrap = false, maxLines = 1).size.width }
        val vertical = cellWidth < with(density) { minimumTouchTargetSize.toPx() } || widestLabel + with(density) { labelInset.toPx() * 2 } > cellWidth
        val fullLabelWidth = with(density) { (maxWidth - inset * 2 - labelInset * 2).roundToPx() }.coerceAtLeast(1)
        val tallestLabel = options.maxOf { ceil(measurer.measure(AnnotatedString(it), labelStyle, constraints = Constraints(maxWidth = fullLabelWidth)).multiParagraph.height).toInt() }
        val verticalCellHeight = maxOf(minimumTouchTargetSize, with(density) { tallestLabel.toDp() } + spacingSmall * 2)
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(cornerRadiusLarge)).background(MaterialTheme.colorScheme.surfaceContainerHigh)) {
            Canvas(Modifier.matchParentSize()) {
                val insetPx = inset.toPx()
                val width = size.width - insetPx * 2
                val height = size.height - insetPx * 2
                val visualPosition = if (rtl && !vertical) options.lastIndex - position else position
                val indicatorWidth = if (vertical) width else width / options.size
                val indicatorHeight = if (vertical) height / options.size else height
                drawRoundRect(
                    color = indicator,
                    topLeft = Offset(
                        insetPx + if (vertical) 0f else visualPosition * indicatorWidth,
                        insetPx + if (vertical) visualPosition * indicatorHeight else 0f,
                    ),
                    size = Size(indicatorWidth.coerceAtLeast(0f), indicatorHeight.coerceAtLeast(0f)),
                    cornerRadius = CornerRadius(cornerRadiusMedium.toPx()),
                )
            }
            if (vertical) {
                Column(Modifier.fillMaxWidth().padding(inset).selectableGroup()) {
                    options.forEachIndexed { index, label ->
                        AppSegmentedOption(
                            selected = index == selectedIndex,
                            onClick = { haptics.select(); onSelect(index) },
                            label = label,
                            modifier = Modifier.fillMaxWidth().height(verticalCellHeight),
                        )
                    }
                }
            } else {
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(inset).selectableGroup()) {
                    options.forEachIndexed { index, label ->
                        AppSegmentedOption(
                            selected = index == selectedIndex,
                            onClick = { haptics.select(); onSelect(index) },
                            label = label,
                            modifier = Modifier.weight(1f).fillMaxHeight().heightIn(min = minimumTouchTargetSize),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppSegmentedOption(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    val interactions = remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    val focused by interactions.collectIsFocusedAsState()
    val interactionColor by animateColorAsState(
        MaterialTheme.colorScheme.onSurface.copy(alpha = when { pressed -> 0.08f; focused -> 0.12f; else -> 0f }),
        AppAnimations.press(),
        label = "segmentInteraction",
    )
    val content by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        AppAnimations.standard(),
        label = "segmentContent",
    )
    Box(
        modifier.clip(RoundedCornerShape(cornerRadiusMedium))
            .selectable(
                selected = selected,
                interactionSource = interactions,
                indication = null,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .background(interactionColor)
            .padding(horizontal = spacingSmall / 4, vertical = spacingSmall),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = content, textAlign = TextAlign.Center)
    }
}

@Preview
@Composable
private fun AppSegmentedControlPreview() {
    MaterialTheme { AppSegmentedControl(listOf("System", "Light", "Dark"), 0, {}) }
}
