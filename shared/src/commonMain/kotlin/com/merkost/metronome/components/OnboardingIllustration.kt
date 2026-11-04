package com.merkost.metronome.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.clearAndSetSemantics
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun OnboardingIllustration(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Canvas(modifier.clearAndSetSemantics {}) {
        val factor = minOf(size.width / 320f, size.height / 280f)
        translate((size.width - 320f * factor) / 2f, (size.height - 280f * factor) / 2f) {
            scale(factor, factor, Offset.Zero) {
                drawCircle(colors.secondaryContainer, 110f, Offset(163f, 130f))
                drawOval(colors.onSurface.copy(alpha = 0.07f), Offset(88f, 249f), Size(145f, 13f))
                val body = Path().apply {
                    moveTo(143f, 35f)
                    quadraticTo(160f, 27f, 177f, 35f)
                    lineTo(218f, 240f)
                    quadraticTo(160f, 252f, 102f, 240f)
                    close()
                }
                drawPath(body, colors.surface)
                drawPath(body, colors.onSurface, style = Stroke(3.5f))
                drawLine(colors.onSurface, Offset(124f, 218f), Offset(196f, 218f), 3f, StrokeCap.Round)
                drawLine(colors.outlineVariant, Offset(160f, 62f), Offset(160f, 198f), 2f)
                repeat(9) { index ->
                    val y = 72f + index * 13f
                    val half = if (index % 2 == 0) 8f else 5f
                    drawLine(colors.outline, Offset(160f - half, y), Offset(160f + half, y), 1.5f, StrokeCap.Round)
                }
                drawLine(colors.onSurface, Offset(160f, 226f), Offset(114f, 77f), 4.5f, StrokeCap.Round)
                val weight = Path().apply {
                    moveTo(119f, 118f)
                    lineTo(140f, 112f)
                    lineTo(148f, 136f)
                    lineTo(127f, 143f)
                    close()
                }
                drawPath(weight, colors.primary)
                drawPath(weight, colors.onSurface, style = Stroke(2f))
                drawCircle(colors.onSurface, 7f, Offset(160f, 226f))
                drawCircle(colors.surface, 2.5f, Offset(160f, 226f))
            }
        }
    }
}

@Preview
@Composable
private fun OnboardingIllustrationPreview() {
    MaterialTheme { OnboardingIllustration() }
}
