package com.merkost.metronome.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.merkost.metronome.ui.appearIn
import com.merkost.metronome.ui.spacingLarge
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class EmptyStateArt { Presets, Steps }

@Composable
fun EmptyState(
    art: EmptyStateArt,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actions: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spacingLarge)
            .appearIn(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmptyStateIllustration(art, Modifier.size(width = 168.dp, height = 112.dp))
        Spacer(Modifier.height(spacingLarge))
        Text(
            text = title,
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(spacingSmall))
        Text(
            text = body,
            modifier = Modifier.widthIn(max = 360.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(spacingMedium))
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
            actions()
        }
    }
}

@Composable
internal fun EmptyStateIllustration(art: EmptyStateArt, modifier: Modifier = Modifier) {
    val ink = MaterialTheme.colorScheme.onSurface
    val faint = MaterialTheme.colorScheme.outlineVariant
    val fill = MaterialTheme.colorScheme.surfaceContainerLow
    Canvas(modifier.clearAndSetSemantics {}) {
        val u = size.width / 168f
        val line = Stroke(width = 1.5f * u, cap = StrokeCap.Round)
        when (art) {
            EmptyStateArt.Presets -> {
                val corner = CornerRadius(12f * u)
                listOf(14f to 0.45f, 7f to 0.7f, 0f to 1f).forEachIndexed { index, (shift, alpha) ->
                    val topLeft = Offset((18f + shift) * u, (14f + (14f - shift)) * u)
                    val cardSize = Size(120f * u, 68f * u)
                    drawRoundRect(fill, topLeft, cardSize, corner)
                    drawRoundRect(
                        color = if (index == 2) ink else faint,
                        topLeft = topLeft,
                        size = cardSize,
                        cornerRadius = corner,
                        style = line,
                        alpha = if (index == 2) 1f else alpha,
                    )
                }
                val front = Offset(18f * u, 28f * u)
                repeat(4) { dot ->
                    val centre = Offset(front.x + (24f + dot * 17f) * u, front.y + 46f * u)
                    if (dot == 0) drawCircle(ink, 5.5f * u, centre) else drawCircle(faint, 5.5f * u, centre, style = line)
                }
                drawLine(ink, Offset(front.x + 24f * u, front.y + 22f * u), Offset(front.x + 62f * u, front.y + 22f * u), 3f * u, StrokeCap.Round)
                drawLine(faint, Offset(front.x + 24f * u, front.y + 32f * u), Offset(front.x + 44f * u, front.y + 32f * u), 2f * u, StrokeCap.Round)
                val ribbon = androidx.compose.ui.graphics.Path().apply {
                    moveTo(front.x + 98f * u, front.y)
                    lineTo(front.x + 98f * u, front.y + 22f * u)
                    lineTo(front.x + 105f * u, front.y + 16f * u)
                    lineTo(front.x + 112f * u, front.y + 22f * u)
                    lineTo(front.x + 112f * u, front.y)
                }
                drawPath(ribbon, ink, style = line)
            }
            EmptyStateArt.Steps -> {
                val corner = CornerRadius(10f * u)
                val boxSize = Size(40f * u, 52f * u)
                val tops = listOf(40f, 22f, 4f)
                val lefts = listOf(6f, 64f, 122f)
                val dash = PathEffect.dashPathEffect(floatArrayOf(4f * u, 5f * u))
                repeat(2) { i ->
                    drawLine(
                        faint,
                        Offset((lefts[i] + 40f) * u, (tops[i] + 26f) * u),
                        Offset(lefts[i + 1] * u, (tops[i + 1] + 26f) * u),
                        1.5f * u,
                        StrokeCap.Round,
                        dash,
                    )
                }
                repeat(3) { i ->
                    val topLeft = Offset(lefts[i] * u, (tops[i] + 28f) * u)
                    drawRoundRect(fill, topLeft, boxSize, corner)
                    drawRoundRect(if (i == 0) ink else faint, topLeft, boxSize, corner, style = line)
                    drawCircle(if (i == 0) ink else faint, 5f * u, Offset(topLeft.x + 20f * u, topLeft.y + 20f * u), style = if (i == 0) androidx.compose.ui.graphics.drawscope.Fill else line)
                    drawLine(if (i == 0) ink else faint, Offset(topLeft.x + 10f * u, topLeft.y + 38f * u), Offset(topLeft.x + 30f * u, topLeft.y + 38f * u), 2f * u, StrokeCap.Round)
                }
            }
        }
    }
}

@Preview
@Composable
private fun EmptyStatePreview() {
    MaterialTheme {
        EmptyState(EmptyStateArt.Presets, "Save a setup", "Keep the setups you return to.")
    }
}
