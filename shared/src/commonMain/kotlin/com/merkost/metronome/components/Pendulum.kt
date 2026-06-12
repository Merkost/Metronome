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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.merkost.metronome.model.Beat
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.pendulumHeight
import kotlin.math.cos
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
    val bobScale = remember { Animatable(1f) }

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

    LaunchedEffect(selectedIndex) {
        if (selectedIndex >= 0 && beats.getOrNull(selectedIndex) == Beat.HIGH) {
            bobScale.animateTo(1.25f, AppAnimations.Snappy)
            bobScale.animateTo(1f, AppAnimations.Settle)
        }
    }

    val armColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    val bobColor = MaterialTheme.colorScheme.primary
    val pivotColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(pendulumHeight)
    ) {
        val strokeWidth = 3.dp.toPx()
        val bobRadius = 11.dp.toPx() * bobScale.value
        val pivotRadius = 4.dp.toPx()
        val pivot = Offset(size.width / 2f, pivotRadius + strokeWidth)
        val armLength = size.height - pivot.y - 14.dp.toPx()

        val radians = angle.value * (kotlin.math.PI.toFloat() / 180f)
        val bobCenter = Offset(
            pivot.x + sin(radians) * armLength,
            pivot.y + cos(radians) * armLength,
        )

        drawLine(
            color = armColor,
            start = pivot,
            end = bobCenter,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawCircle(color = pivotColor, radius = pivotRadius, center = pivot)
        drawCircle(color = bobColor, radius = bobRadius, center = bobCenter)
    }
}
