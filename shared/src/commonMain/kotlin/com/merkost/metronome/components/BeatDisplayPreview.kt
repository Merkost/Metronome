package com.merkost.metronome.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.merkost.metronome.model.BeatDisplayStyle
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.cornerRadiusLarge
import com.merkost.metronome.ui.isAppMotionReduced
import kotlinx.coroutines.delay

private const val PreviewBeats = 4
private const val PreviewBeatMillis = 625L

@Composable
fun BeatDisplayPreview(style: BeatDisplayStyle, modifier: Modifier = Modifier) {
    val reducedMotion = isAppMotionReduced()
    var beat by remember { mutableIntStateOf(0) }
    LaunchedEffect(reducedMotion) {
        beat = 0
        if (!reducedMotion) {
            while (true) {
                delay(PreviewBeatMillis)
                beat = (beat + 1) % PreviewBeats
            }
        }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp)
            .clip(RoundedCornerShape(cornerRadiusLarge))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        when (style) {
            BeatDisplayStyle.DOTS -> Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                repeat(PreviewBeats) { index ->
                    val color by animateColorAsState(
                        targetValue = if (index == beat) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceContainerHighest,
                        animationSpec = AppAnimations.quick(),
                        label = "previewDot",
                    )
                    Box(Modifier.size(22.dp).clip(CircleShape).background(color))
                }
            }
            BeatDisplayStyle.PENDULUM -> {
                val angle by animateFloatAsState(
                    targetValue = if (beat % 2 == 0) -AppAnimations.PendulumSwingDegrees else AppAnimations.PendulumSwingDegrees,
                    animationSpec = AppAnimations.emphasized(),
                    label = "previewPendulum",
                )
                val ink = MaterialTheme.colorScheme.onSurface
                val track = MaterialTheme.colorScheme.outlineVariant
                Canvas(Modifier.size(width = 120.dp, height = 72.dp)) {
                    val pivot = Offset(size.width / 2f, size.height - 6.dp.toPx())
                    drawLine(track, Offset(pivot.x - 28.dp.toPx(), pivot.y), Offset(pivot.x + 28.dp.toPx(), pivot.y), 2.dp.toPx(), StrokeCap.Round)
                    rotate(angle, pivot) {
                        drawLine(ink, pivot, Offset(pivot.x, pivot.y - 56.dp.toPx()), 3.dp.toPx(), StrokeCap.Round)
                        drawCircle(ink, 7.dp.toPx(), Offset(pivot.x, pivot.y - 40.dp.toPx()))
                    }
                }
            }
        }
    }
}
