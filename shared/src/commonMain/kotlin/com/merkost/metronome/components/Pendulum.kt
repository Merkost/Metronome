package com.merkost.metronome.components

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.BeatPulse
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.pendulumHeight
import com.merkost.metronome.ui.isAppMotionReduced
import kotlinx.coroutines.isActive
import org.jetbrains.compose.ui.tooling.preview.Preview

private const val MIN_BPM = 40f
private const val MAX_BPM = 240f

@Composable
fun Pendulum(
    selectedIndex: Int,
    beats: List<Beat>,
    isPlaying: Boolean,
    intervalMs: Int,
    beatPulse: BeatPulse?,
    modifier: Modifier = Modifier,
) {
    val angle = remember { mutableFloatStateOf(0f) }
    val currentPulse by rememberUpdatedState(beatPulse)
    val reducedMotion = isAppMotionReduced()

    LaunchedEffect(isPlaying, reducedMotion) {
        if (isPlaying && !reducedMotion) {
            while (isActive) {
                withFrameNanos {
                    angle.floatValue = (currentPulse?.pendulumDisplacement() ?: 0f) * AppAnimations.PendulumSwingDegrees
                }
            }
        } else {
            animate(angle.floatValue, 0f, animationSpec = AppAnimations.calm()) { value, _ ->
                angle.floatValue = value
            }
        }
    }

    val bpm = (60_000f / intervalMs).coerceIn(MIN_BPM, MAX_BPM)
    val weightFraction by animateFloatAsState(
        targetValue = 0.72f - 0.34f * ((bpm - MIN_BPM) / (MAX_BPM - MIN_BPM)),
        animationSpec = AppAnimations.Calm,
        label = "pendulumWeight"
    )

    val activeBeat = if (isPlaying) beats.getOrNull(selectedIndex) else null
    val bodyColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
    val baseColor = MaterialTheme.colorScheme.surfaceVariant
    val armColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    val armHighlightColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
    val tipColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
    val weightColor = when (activeBeat) {
        Beat.HIGH -> MaterialTheme.colorScheme.tertiary
        Beat.MUTE -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.66f)
        else -> MaterialTheme.colorScheme.primary
    }
    val pivotColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(pendulumHeight)
    ) {
        val pivot = Offset(size.width / 2f, size.height - 10.dp.toPx())
        val tipY = 10.dp.toPx()
        val armLength = pivot.y - tipY

        val bodyTopHalfWidth = 26.dp.toPx()
        val bodyBottomHalfWidth = 44.dp.toPx()
        val body = Path().apply {
            moveTo(pivot.x - bodyTopHalfWidth, tipY - 4.dp.toPx())
            lineTo(pivot.x + bodyTopHalfWidth, tipY - 4.dp.toPx())
            lineTo(pivot.x + bodyBottomHalfWidth, pivot.y + 5.dp.toPx())
            lineTo(pivot.x - bodyBottomHalfWidth, pivot.y + 5.dp.toPx())
            close()
        }
        drawPath(body, bodyColor)

        rotate(degrees = angle.floatValue, pivot = pivot) {
            val tip = Offset(pivot.x, pivot.y - armLength)
            drawLine(
                color = armColor,
                start = pivot,
                end = tip,
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = armHighlightColor,
                start = pivot,
                end = tip,
                strokeWidth = 1.2.dp.toPx(),
                cap = StrokeCap.Round,
            )

            val weightCenter = Offset(pivot.x, pivot.y - armLength * weightFraction)
            val weightHalfWidth = 9.dp.toPx()
            val weightHalfHeight = 12.dp.toPx()
            val weight = Path().apply {
                moveTo(weightCenter.x, weightCenter.y - weightHalfHeight)
                lineTo(weightCenter.x + weightHalfWidth, weightCenter.y)
                lineTo(weightCenter.x, weightCenter.y + weightHalfHeight)
                lineTo(weightCenter.x - weightHalfWidth, weightCenter.y)
                close()
            }
            drawPath(weight, weightColor)

            drawCircle(color = tipColor, radius = 4.dp.toPx(), center = tip)
        }

        drawRoundRect(
            color = baseColor,
            topLeft = Offset(pivot.x - 40.dp.toPx(), pivot.y - 4.5.dp.toPx()),
            size = Size(80.dp.toPx(), 9.dp.toPx()),
            cornerRadius = CornerRadius(4.5.dp.toPx()),
        )
        drawCircle(color = pivotColor, radius = 5.5.dp.toPx(), center = pivot)
    }
}

@Preview
@Composable
private fun PendulumPreview() {
    MaterialTheme { Pendulum(-1, List(4) { Beat.LOW }, false, 750, null) }
}
