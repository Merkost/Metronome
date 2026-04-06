package com.merkost.metronome.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Standard animation specs for the app.
 * - [Interactive]: Snappy response for user-driven actions (press, tap, toggle).
 * - [Bouncy]: Playful overshoot for beat pulses and selection changes.
 * - [Gentle]: Smooth transitions for layout changes and appearance.
 */
object AppAnimations {
    val Interactive = SpringSpec<Float>(
        stiffness = Spring.StiffnessMedium,
        dampingRatio = Spring.DampingRatioMediumBouncy
    )

    val Bouncy = SpringSpec<Float>(
        stiffness = 600f,
        dampingRatio = 0.8f
    )

    val Gentle = SpringSpec<Float>(
        stiffness = Spring.StiffnessLow,
        dampingRatio = Spring.DampingRatioNoBouncy
    )

    val Snappy = SpringSpec<Float>(
        stiffness = 1000f,
        dampingRatio = 0.3f
    )

    val Settle = SpringSpec<Float>(
        stiffness = 600f,
        dampingRatio = 0.6f
    )
}

/**
 * Scales down to [pressedScale] when pressed, returns to 1.0 on release.
 * Uses [AppAnimations.Interactive] for a bouncy feel.
 *
 * Usage:
 * ```
 * Box(
 *     Modifier
 *         .pressScale(interactionSource)
 *         .clickable(interactionSource, indication) { ... }
 * )
 * ```
 */
@Composable
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.85f,
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1.0f,
        animationSpec = AppAnimations.Interactive,
        label = "pressScale"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Pulses scale up then back when [trigger] changes.
 * Useful for tap feedback, beat indicators, etc.
 *
 * Usage:
 * ```
 * var tapCount by remember { mutableStateOf(0) }
 * Box(Modifier.pulseOnChange(tapCount))
 * ```
 */
fun Modifier.pulseOnChange(
    trigger: Any,
    peakScale: Float = 1.1f,
): Modifier = composed {
    val scale = remember { Animatable(1f) }
    LaunchedEffect(trigger) {
        if (trigger != 0) {
            scale.animateTo(peakScale, AppAnimations.Snappy)
            scale.animateTo(1f, AppAnimations.Settle)
        }
    }
    this.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}
