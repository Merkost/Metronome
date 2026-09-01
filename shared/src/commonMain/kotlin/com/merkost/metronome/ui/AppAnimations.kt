package com.merkost.metronome.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview

object AppAnimations {
    fun <T> bouncy(): SpringSpec<T> = SpringSpec(
        stiffness = 600f,
        dampingRatio = 0.8f
    )

    val Interactive = SpringSpec<Float>(
        stiffness = Spring.StiffnessMedium,
        dampingRatio = Spring.DampingRatioMediumBouncy
    )

    val Bouncy = bouncy<Float>()

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

    val expandEnter: EnterTransition =
        expandVertically(spring(stiffness = 600f, dampingRatio = 0.8f)) + fadeIn()

    val shrinkExit: ExitTransition =
        shrinkVertically(spring(stiffness = 600f, dampingRatio = 0.8f)) + fadeOut()

    val fadeScaleTransform: ContentTransform =
        (fadeIn(spring(stiffness = 600f)) + scaleIn(initialScale = 0.85f, animationSpec = spring(stiffness = 600f)))
            .togetherWith(fadeOut(spring(stiffness = 1000f)) + scaleOut(targetScale = 0.85f, animationSpec = spring(stiffness = 1000f)))

    fun slideDigitTransform(towardsUp: Boolean): ContentTransform {
        val direction = if (towardsUp) -1 else 1
        return (slideInVertically(spring(stiffness = 600f, dampingRatio = 0.8f)) { it * -direction } + fadeIn())
            .togetherWith(slideOutVertically(spring(stiffness = 600f, dampingRatio = 0.8f)) { it * direction } + fadeOut())
    }
}

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

fun Modifier.pulseOnChange(
    trigger: Int,
    peakScale: Float = 1.1f,
): Modifier = composed {
    val scale = remember { Animatable(1f) }
    val last = remember { intArrayOf(trigger) }
    LaunchedEffect(trigger) {
        if (trigger != last[0]) {
            last[0] = trigger
            scale.animateTo(peakScale, AppAnimations.Snappy)
            scale.animateTo(1f, AppAnimations.Settle)
        }
    }
    this.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}

fun Modifier.pulseOnAppear(
    peakScale: Float = 1.25f,
): Modifier = composed {
    val scale = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        scale.animateTo(peakScale, AppAnimations.Snappy)
        scale.animateTo(1f, AppAnimations.Settle)
    }
    this.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}

@Composable
fun AnimatedNumberText(
    value: Int,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    autoSize: TextAutoSize? = null,
) {
    AnimatedContent(
        targetState = value,
        transitionSpec = { AppAnimations.slideDigitTransform(targetState >= initialState) },
        contentKey = { it },
        label = "number",
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) { target ->
        Text(
            text = target.toString(),
            style = style,
            color = color,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            maxLines = 1,
            autoSize = autoSize,
        )
    }
}

@Preview
@Composable
private fun AnimatedNumberTextPreview() {
    MaterialTheme {
        Box(Modifier.width(220.dp)) {
            AnimatedNumberText(
                value = 120,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 62.sp,
                ),
                modifier = Modifier.fillMaxWidth(),
                autoSize = TextAutoSize.StepBased(30.sp, 62.sp),
            )
        }
    }
}
