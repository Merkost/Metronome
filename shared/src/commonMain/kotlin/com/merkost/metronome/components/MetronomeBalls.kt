package com.merkost.metronome.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.merkost.metronome.model.Beat
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.BallSize
import com.merkost.metronome.ui.CircleSize
import com.merkost.metronome.ui.CircleWeight
import com.merkost.metronome.ui.pressScale
import kotlin.math.ceil

/**
 * Beat balls arranged in centered rows with a single indicator circle
 * that slides smoothly between balls — including across rows.
 */
@Composable
fun MetronomeBalls(
    selectedIndex: Int,
    beats: List<Beat>,
    isPlaying: Boolean,
    animSpec: AnimationSpec<Float>,
    arrangementSpacing: Dp = 32.dp,
    indicatorSize: Dp = CircleSize,
    ballSize: Dp = BallSize,
    modifier: Modifier = Modifier,
    onBallClicked: (index: Int, Beat) -> Unit,
) {
    // Animate the indicator position (as a float index) between balls
    val indicatorIndex = remember { Animatable(selectedIndex.coerceAtLeast(0).toFloat()) }
    LaunchedEffect(selectedIndex) {
        if (selectedIndex >= 0) {
            indicatorIndex.animateTo(selectedIndex.toFloat(), animSpec)
        }
    }

    val indicatorAlpha by animateFloatAsState(
        targetValue = if (selectedIndex >= 0) 1f else 0f,
        animationSpec = AppAnimations.Bouncy,
        label = "indicatorAlpha"
    )

    val primaryColor = MaterialTheme.colorScheme.primary

    Layout(
        modifier = modifier,
        content = {
            beats.forEachIndexed { index, beat ->
                Box(Modifier.layoutId("ball")) {
                    Ball(
                        beat = beat,
                        isActive = isPlaying && index == selectedIndex,
                        ballSize = ballSize,
                        onClick = { onBallClicked(index, beat) }
                    )
                }
            }
            Spacer(
                modifier = Modifier
                    .layoutId("indicator")
                    .size(indicatorSize)
                    .graphicsLayer { alpha = indicatorAlpha }
                    .border(CircleWeight, primaryColor, CircleShape)
            )
        }
    ) { measurables, constraints ->
        val indicatorMeasurable = measurables.first { it.layoutId == "indicator" }
        val ballMeasurables = measurables.filter { it.layoutId == "ball" }

        // Measure children with loose constraints so they use intrinsic size
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val ballPlaceables = ballMeasurables.map { it.measure(looseConstraints) }
        val indicatorPlaceable = indicatorMeasurable.measure(looseConstraints)

        if (ballPlaceables.isEmpty()) return@Layout layout(0, 0) {}

        val ballW = ballPlaceables.first().width
        val ballH = ballPlaceables.first().height
        val indW = indicatorPlaceable.width
        val indH = indicatorPlaceable.height
        val spacingPx = arrangementSpacing.roundToPx()

        // How many balls fit in one row?
        val availableWidth = constraints.maxWidth
        val maxPerRow = ((availableWidth + spacingPx) / (ballW + spacingPx)).coerceAtLeast(1)
        val rowCount = ceil(ballPlaceables.size.toFloat() / maxPerRow).toInt()

        // Offset to center balls within the indicator cell
        val cellOffsetX = (indW - ballW) / 2
        val cellOffsetY = (indH - ballH) / 2

        val rowSpacingPx = spacingPx
        val rowHeight = indH
        val totalHeight = rowHeight * rowCount + rowSpacingPx * (rowCount - 1).coerceAtLeast(0)

        // Pre-compute each ball's top-left position (ball coords, not indicator coords)
        data class BallPos(val x: Int, val y: Int)
        val positions = mutableListOf<BallPos>()

        var ballIdx = 0
        for (row in 0 until rowCount) {
            val countInRow = if (ballIdx + maxPerRow <= ballPlaceables.size) maxPerRow
            else ballPlaceables.size - ballIdx
            val rowContentWidth = ballW * countInRow + spacingPx * (countInRow - 1)
            val rowStartX = (availableWidth - rowContentWidth) / 2 // center the row
            val rowY = row * (rowHeight + rowSpacingPx)

            for (col in 0 until countInRow) {
                val x = rowStartX + col * (ballW + spacingPx)
                positions.add(BallPos(x, rowY + cellOffsetY))
                ballIdx++
            }
        }

        layout(availableWidth, totalHeight) {
            // Place the animated indicator
            val animIdx = indicatorIndex.value.coerceIn(0f, (ballPlaceables.size - 1).toFloat())
            val fromIdx = animIdx.toInt().coerceIn(positions.indices)
            val toIdx = (fromIdx + 1).coerceIn(positions.indices)
            val fraction = animIdx - fromIdx

            val fromPos = positions[fromIdx]
            val toPos = positions[toIdx]
            val indX = (fromPos.x + (toPos.x - fromPos.x) * fraction - cellOffsetX).toInt()
            val indY = (fromPos.y + (toPos.y - fromPos.y) * fraction - cellOffsetY).toInt()
            indicatorPlaceable.placeRelative(x = indX, y = indY)

            // Place balls
            ballPlaceables.forEachIndexed { index, placeable ->
                val pos = positions[index]
                placeable.placeRelative(x = pos.x, y = pos.y)
            }
        }
    }
}

@Composable
private fun Ball(
    beat: Beat,
    isActive: Boolean = false,
    ballSize: Dp = BallSize,
    onClick: () -> Unit,
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    val color by animateColorAsState(
        targetValue = when (beat) {
            Beat.HIGH -> MaterialTheme.colorScheme.primary
            Beat.LOW -> MaterialTheme.colorScheme.primaryContainer
            Beat.MUTE -> Color.Transparent
        },
        label = "ballColor"
    )

    val outlineAlpha by animateFloatAsState(
        targetValue = if (beat == Beat.MUTE) 1f else 0f,
        animationSpec = AppAnimations.Interactive,
        label = "ballOutlineAlpha"
    )
    val outlineColor = MaterialTheme.colorScheme.onSurfaceVariant

    val glowAlpha by animateFloatAsState(
        targetValue = if (isActive) 0.3f else 0f,
        animationSpec = AppAnimations.Bouncy,
        label = "ballGlowAlpha"
    )

    val beatScale by animateFloatAsState(
        targetValue = if (isActive) 1.15f else 1.0f,
        animationSpec = AppAnimations.Interactive,
        label = "ballBeatScale"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(ballSize)
            .graphicsLayer {
                scaleX = beatScale
                scaleY = beatScale
            }
            .pressScale(interactionSource)
            .drawBehind {
                if (glowAlpha > 0f) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.maxDimension / 2f * 1.15f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = glowAlpha),
                                primaryColor.copy(alpha = 0f)
                            ),
                            center = center,
                            radius = radius
                        ),
                        radius = radius,
                        center = center
                    )
                }
            }
            .padding(2.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.5.dp, outlineColor.copy(alpha = outlineAlpha * 0.5f), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true)
            ) { onClick() }
    )
}

@Composable
fun OutlinedCircle(color: Color, size: Dp = CircleSize) {
    Spacer(
        modifier = Modifier
            .size(size)
            .border(CircleWeight, color, CircleShape)
    )
}
