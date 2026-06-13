package com.merkost.metronome.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.progressRingSize
import com.merkost.metronome.ui.progressRingStroke

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    size: Dp = progressRingSize,
    strokeWidth: Dp = progressRingStroke,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = AppAnimations.Gentle,
        label = "ringProgress"
    )

    Canvas(modifier = modifier.size(size)) {
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        val inset = strokeWidth.toPx() / 2
        val arcSize = Size(this.size.width - inset * 2, this.size.height - inset * 2)
        drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = stroke,
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * animatedProgress,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = stroke,
        )
    }
}
