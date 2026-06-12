package com.merkost.metronome.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import com.merkost.metronome.ui.AppAnimations

@Composable
fun PlayPauseMorphIcon(
    isPlaying: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val progress by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = AppAnimations.Interactive,
        label = "playPauseMorph"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val leftPlay = listOf(
            Offset(w * 0.08f, 0f),
            Offset(w * 0.52f, h * 0.25f),
            Offset(w * 0.52f, h * 0.75f),
            Offset(w * 0.08f, h),
        )
        val leftPause = listOf(
            Offset(w * 0.13f, 0f),
            Offset(w * 0.40f, 0f),
            Offset(w * 0.40f, h),
            Offset(w * 0.13f, h),
        )
        val rightPlay = listOf(
            Offset(w * 0.52f, h * 0.25f),
            Offset(w * 0.96f, h * 0.5f),
            Offset(w * 0.96f, h * 0.5f),
            Offset(w * 0.52f, h * 0.75f),
        )
        val rightPause = listOf(
            Offset(w * 0.60f, 0f),
            Offset(w * 0.87f, 0f),
            Offset(w * 0.87f, h),
            Offset(w * 0.60f, h),
        )

        fun morphed(from: List<Offset>, to: List<Offset>): Path {
            val path = Path()
            from.forEachIndexed { index, start ->
                val end = to[index]
                val x = start.x + (end.x - start.x) * progress
                val y = start.y + (end.y - start.y) * progress
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            return path
        }

        rotate(degrees = 90f * progress) {
            drawPath(morphed(leftPlay, leftPause), color)
            drawPath(morphed(rightPlay, rightPause), color)
        }
    }
}
