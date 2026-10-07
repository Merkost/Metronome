package com.merkost.metronome.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.minimumTouchTargetSize
import com.merkost.metronome.ui.rememberAppHaptics
import kotlin.math.abs
import kotlin.math.roundToInt
import org.jetbrains.compose.ui.tooling.preview.Preview

private const val ContinuousDetents = 100

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    accessibilityLabel: String,
    modifier: Modifier = Modifier,
    steps: Int = 0,
    showActiveTicks: Boolean = false,
) {
    val rightToLeft = LocalLayoutDirection.current == LayoutDirection.Rtl
    val haptics = rememberAppHaptics()
    val interactions = remember { MutableInteractionSource() }
    val dragged by interactions.collectIsDraggedAsState()
    val pressed by interactions.collectIsPressedAsState()
    val engaged = dragged || pressed
    val span = valueRange.endInclusive - valueRange.start
    val detentCount = if (steps > 0) steps + 1 else ContinuousDetents
    fun detentOf(raw: Float) = if (span <= 0f) 0 else ((raw - valueRange.start) / span * detentCount).roundToInt()
    var lastDetent by remember(valueRange, steps) { mutableIntStateOf(detentOf(value)) }
    val fraction by animateFloatAsState(
        targetValue = if (span > 0f) ((value - valueRange.start) / span).coerceIn(0f, 1f) else 0f,
        animationSpec = if (engaged) snap() else AppAnimations.standard(),
        label = "sliderFill",
    )
    val scale by animateFloatAsState(if (engaged) 1.12f else 1f, AppAnimations.expressive(), label = "sliderGrip")
    val trackHeight by animateFloatAsState(if (engaged) 10f else 8f, AppAnimations.standard(), label = "sliderTrack")
    val ink = MaterialTheme.colorScheme.onSurface
    val grip = MaterialTheme.colorScheme.inverseOnSurface
    val rail = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val tick = MaterialTheme.colorScheme.surface
    Slider(
        value = value.coerceIn(valueRange),
        onValueChange = { newValue ->
            val detent = detentOf(newValue)
            if (detent != lastDetent) {
                if (abs(detent - lastDetent) == 1) haptics.tick()
                lastDetent = detent
            }
            onValueChange(newValue)
        },
        valueRange = valueRange,
        steps = steps,
        interactionSource = interactions,
        thumb = {
            Box(Modifier.size(28.dp).graphicsLayer { scaleX = scale; scaleY = scale }.background(ink, CircleShape)) {
                Canvas(Modifier.size(28.dp)) {
                    for (x in -1..1) for (y in -1..1) {
                        drawCircle(grip.copy(alpha = 0.75f), 0.85.dp.toPx(), Offset(center.x + x * 3.dp.toPx(), center.y + y * 3.dp.toPx()))
                    }
                }
            }
        },
        track = {
            Canvas(Modifier.fillMaxWidth().height(20.dp)) {
                scale(scaleX = if (rightToLeft) -1f else 1f, scaleY = 1f, pivot = center) {
                val height = trackHeight.dp.toPx()
                val y = (size.height - height) / 2f
                drawRoundRect(rail, Offset(0f, y), Size(size.width, height), CornerRadius(height / 2f))
                val origin = if (valueRange.start < 0f && valueRange.endInclusive > 0f) -valueRange.start / span else 0f
                val left = minOf(origin, fraction) * size.width
                val right = maxOf(origin, fraction) * size.width
                if (right > left) drawRoundRect(ink, Offset(left, y), Size(right - left, height), CornerRadius(height / 2f))
                if (origin > 0f) drawLine(rail, Offset(origin * size.width, y - 2.dp.toPx()), Offset(origin * size.width, y + height + 2.dp.toPx()), 2.dp.toPx())
                if (showActiveTicks && steps > 0) {
                    for (index in 0..steps + 1) {
                        val position = index.toFloat() / (steps + 1)
                        drawCircle(if (position in minOf(origin, fraction)..maxOf(origin, fraction)) tick else ink.copy(alpha = 0.22f), 1.dp.toPx(), Offset(position * size.width, center.y))
                    }
                }
                }
            }
        },
        modifier = modifier.heightIn(min = minimumTouchTargetSize).semantics { contentDescription = accessibilityLabel },
    )
}

@Preview
@Composable
private fun AppSliderPreview() {
    MaterialTheme { AppSlider(0.75f, {}, 0f..1f, "Click volume") }
}
