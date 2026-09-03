package com.merkost.metronome.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.merkost.metronome.ui.brandMarkFilled
import com.merkost.metronome.ui.brandMarkSubpaths

private const val DRAW_DURATION_MILLIS = 900
private const val INK_DURATION_MILLIS = 600
private val MarkSize = 132.dp

@Composable
fun AnimatedSplash(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val drawProgress = remember { Animatable(0f) }
    val inkProgress = remember { Animatable(0f) }
    val currentOnFinished by rememberUpdatedState(onFinished)

    LaunchedEffect(Unit) {
        drawProgress.animateTo(1f, tween(DRAW_DURATION_MILLIS, easing = LinearEasing))
        inkProgress.animateTo(1f, tween(INK_DURATION_MILLIS))
        currentOnFinished()
    }

    val markPx = with(LocalDensity.current) { MarkSize.toPx() }
    val subpaths = remember(markPx) { brandMarkSubpaths(markPx) }
    val filled = remember(markPx) { brandMarkFilled(markPx) }
    val measures = remember(subpaths) { subpaths.map { PathMeasure().apply { setPath(it, false) } } }
    val ink = MaterialTheme.colorScheme.onBackground

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(MarkSize)) {
            val segmentCount = measures.size
            val strokeWidth = size.minDimension * 0.055f

            measures.forEachIndexed { index, measure ->
                val start = index.toFloat() / segmentCount
                val local = ((drawProgress.value - start) * segmentCount).coerceIn(0f, 1f)
                if (local <= 0f) return@forEachIndexed
                val drawn = Path()
                measure.getSegment(0f, measure.length * local, drawn, true)
                drawPath(
                    path = drawn,
                    color = ink,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )
            }

            if (inkProgress.value > 0f) {
                val revealed = size.height * inkProgress.value
                clipRect(top = size.height - revealed) {
                    drawPath(path = filled, color = ink)
                }
            }
        }
    }
}
