package com.merkost.metronome.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.merkost.metronome.model.Beat
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.pendulumHeight
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private const val SWING_DEGREES = 26f

@Composable
fun Pendulum(
    selectedIndex: Int,
    beats: List<Beat>,
    isPlaying: Boolean,
    intervalMs: Int,
    modifier: Modifier = Modifier,
) {
    val angle = remember { Animatable(0f) }

    LaunchedEffect(selectedIndex, isPlaying) {
        if (isPlaying && selectedIndex >= 0) {
            val target = if (selectedIndex % 2 == 0) SWING_DEGREES else -SWING_DEGREES
            angle.animateTo(
                targetValue = target,
                animationSpec = tween(durationMillis = intervalMs, easing = FastOutSlowInEasing)
            )
        } else {
            angle.animateTo(0f, AppAnimations.Gentle)
        }
    }

    val activeBeat = if (isPlaying) beats.getOrNull(selectedIndex) else null
    val guideColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.16f)
    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.28f)
    val armShadowColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.14f)
    val armColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f)
    val armHighlightColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.62f)
    val bobColor = when (activeBeat) {
        Beat.HIGH -> MaterialTheme.colorScheme.tertiary
        Beat.MUTE -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.66f)
        else -> MaterialTheme.colorScheme.primary
    }
    val bobRingColor = when (activeBeat) {
        Beat.HIGH -> MaterialTheme.colorScheme.tertiaryContainer
        Beat.MUTE -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val bobHighlightColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
    val pivotOuterColor = MaterialTheme.colorScheme.surfaceVariant
    val pivotInnerColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(pendulumHeight)
    ) {
        val strokeWidth = 3.5.dp.toPx()
        val highlightStrokeWidth = 1.35.dp.toPx()
        val bobRadius = 11.dp.toPx()
        val bobRingRadius = bobRadius + 4.dp.toPx()
        val pivotOuterRadius = 8.dp.toPx()
        val pivotInnerRadius = 4.dp.toPx()
        val pivot = Offset(size.width / 2f, pivotOuterRadius + 4.dp.toPx())
        val armLength = size.height - pivot.y - bobRingRadius - 4.dp.toPx()
        val arcRadius = min(armLength, size.width * 0.33f)

        fun pointAt(degrees: Float, radius: Float): Offset {
            val radians = degrees * (PI.toFloat() / 180f)
            return Offset(
                pivot.x + sin(radians) * radius,
                pivot.y + cos(radians) * radius,
            )
        }

        val arcBounds = Rect(
            left = pivot.x - arcRadius,
            top = pivot.y - arcRadius,
            right = pivot.x + arcRadius,
            bottom = pivot.y + arcRadius,
        )

        drawArc(
            color = guideColor,
            startAngle = 90f - SWING_DEGREES,
            sweepAngle = SWING_DEGREES * 2f,
            useCenter = false,
            topLeft = arcBounds.topLeft,
            size = arcBounds.size,
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round),
        )

        listOf(-SWING_DEGREES, -SWING_DEGREES / 2f, 0f, SWING_DEGREES / 2f, SWING_DEGREES)
            .forEach { degrees ->
                val isCenter = degrees == 0f
                drawLine(
                    color = if (isCenter) armHighlightColor.copy(alpha = 0.45f) else tickColor,
                    start = pointAt(degrees, arcRadius - if (isCenter) 7.dp.toPx() else 5.dp.toPx()),
                    end = pointAt(degrees, arcRadius + if (isCenter) 3.dp.toPx() else 1.dp.toPx()),
                    strokeWidth = if (isCenter) 1.6.dp.toPx() else 1.1.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }

        val bobCenter = pointAt(angle.value, armLength)
        val armShadowEnd = bobCenter + Offset(1.5.dp.toPx(), 2.dp.toPx())

        drawLine(
            color = armShadowColor,
            start = pivot + Offset(1.dp.toPx(), 1.5.dp.toPx()),
            end = armShadowEnd,
            strokeWidth = strokeWidth + 2.5.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = armColor,
            start = pivot,
            end = bobCenter,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = armHighlightColor,
            start = pivot,
            end = bobCenter,
            strokeWidth = highlightStrokeWidth,
            cap = StrokeCap.Round,
        )
        drawCircle(
            color = armShadowColor,
            radius = bobRingRadius,
            center = bobCenter + Offset(1.8.dp.toPx(), 2.5.dp.toPx()),
        )
        drawCircle(color = bobRingColor, radius = bobRingRadius, center = bobCenter)
        drawCircle(color = bobColor, radius = bobRadius, center = bobCenter)
        drawCircle(
            color = bobHighlightColor,
            radius = bobRadius * 0.28f,
            center = bobCenter - Offset(bobRadius * 0.32f, bobRadius * 0.36f),
        )
        drawCircle(color = pivotOuterColor, radius = pivotOuterRadius, center = pivot)
        drawCircle(color = pivotInnerColor, radius = pivotInnerRadius, center = pivot)
        drawCircle(
            color = armHighlightColor.copy(alpha = 0.7f),
            radius = 1.6.dp.toPx(),
            center = pivot - Offset(1.5.dp.toPx(), 1.5.dp.toPx()),
        )
    }
}
