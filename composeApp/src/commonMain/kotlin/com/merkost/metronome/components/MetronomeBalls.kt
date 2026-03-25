package com.merkost.metronome.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
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
import com.merkost.metronome.ui.BallColor
import com.merkost.metronome.ui.BallSize
import com.merkost.metronome.ui.CircleSize
import com.merkost.metronome.ui.CircleWeight

@Composable
fun MetronomeBalls(
    selectedIndex: Int,
    itemCount: Int,
    animSpec: AnimationSpec<Float>,
    arrangementSpacing: Dp = 32.dp,
    indicator: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {

    // Track how "selected" each item is [0, 1]
    val selectionFractions = remember(itemCount) {
        List(itemCount) { i ->
            Animatable(if (i == selectedIndex) 1f else 0f)
        }
    }

    selectionFractions.forEachIndexed { index, selectionFraction ->
        val target = if (index == selectedIndex) 1f else 0f
        LaunchedEffect(target, animSpec) {
            selectionFraction.animateTo(target, animSpec)
        }
    }

    // Animate the position of the indicator
    val indicatorIndex = remember { Animatable(0f) }
    val targetIndicatorIndex = selectedIndex.toFloat()
    LaunchedEffect(targetIndicatorIndex) {
        indicatorIndex.animateTo(targetIndicatorIndex, animSpec)
    }

    Layout(
        modifier = modifier,
        content = {
            content()
            Box(Modifier.layoutId("indicator"), content = indicator)
        }
    ) { measurables, constraints ->
        check(itemCount == (measurables.size - 1)) // account for indicator

        val arrangementSpacingPx = with(this) {
            arrangementSpacing.roundToPx()
        }

        // Divide the width into n+1 slots and give the selected item 2 slots
        val indicatorMeasurable = measurables.first { it.layoutId == "indicator" }

        val itemPlaceables = measurables
            .filterNot { it == indicatorMeasurable }
            .mapIndexed { index, measurable ->
                measurable.measure(constraints)
            }

        val width = itemPlaceables.first().width
        val indicatorPlaceable = indicatorMeasurable.measure(constraints)

        val heightRaz = (indicatorPlaceable.height - itemPlaceables.first().height) / 2
        val widthRaz = (indicatorPlaceable.width - itemPlaceables.first().width) / 2

        layout(
            width = width * itemCount + arrangementSpacingPx * (itemCount),
            height = itemPlaceables.maxByOrNull { it.height }?.height ?: 0
        ) {
            val indicatorLeft =
                width * indicatorIndex.value + indicatorIndex.value * (arrangementSpacingPx)
            indicatorPlaceable.placeRelative(x = indicatorLeft.toInt(), y = 0)
            var x = 0
            itemPlaceables.forEachIndexed { index, placeable ->
                placeable.placeRelative(x = x + widthRaz, y = 0 + heightRaz)
                if (index != itemPlaceables.size - 1) {
                    x += arrangementSpacingPx
                }
                x += placeable.width
            }
        }
    }
}

@Composable
fun Ball(
    color: Color = BallColor,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    // Glow alpha: animate to 0.3f when active, 0f when not
    val glowAlpha by animateFloatAsState(
        targetValue = if (isActive) 0.3f else 0f,
        animationSpec = SpringSpec(stiffness = 600f, dampingRatio = 0.8f),
        label = "ballGlowAlpha"
    )

    // Scale bump: animate to 1.15f when active, 1.0f otherwise
    val beatScale by animateFloatAsState(
        targetValue = if (isActive) 1.15f else 1.0f,
        animationSpec = SpringSpec(
            stiffness = Spring.StiffnessMedium,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "ballBeatScale"
    )

    // Press feedback
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1.0f,
        animationSpec = SpringSpec(
            stiffness = Spring.StiffnessMedium,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "ballPressScale"
    )

    val combinedScale = beatScale * pressScale

    Box(
        modifier = Modifier
            .size(BallSize)
            .graphicsLayer {
                scaleX = combinedScale
                scaleY = combinedScale
            }
            .drawBehind {
                if (glowAlpha > 0f) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.maxDimension / 2f * 1.6f
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
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    )
}

@Composable
fun OutlinedCircle(color: Color) {
//    val color by animateColorAsState(targetValue = if (colorWhite) Color.White else Color.Black)
    Spacer(
        modifier = Modifier
            .size(CircleSize)
            .border(CircleWeight, color, CircleShape)
    )
}
