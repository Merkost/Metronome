package com.merkost.metronome.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon

const val HoverTintLight = 0.06f
const val HoverTintDark = 0.08f
const val HoverTintPressed = 0.10f

fun hoverTintAlpha(hovered: Boolean, pressed: Boolean, dark: Boolean): Float = when {
    pressed -> HoverTintPressed
    hovered -> if (dark) HoverTintDark else HoverTintLight
    else -> 0f
}

@Composable
fun Modifier.hoverTint(
    interactionSource: InteractionSource,
    shape: Shape = RoundedCornerShape(cornerRadiusLarge),
    enabled: Boolean = true,
): Modifier {
    val hovered by interactionSource.collectIsHoveredAsState()
    val pressed by interactionSource.collectIsPressedAsState()
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val alpha by animateFloatAsState(
        targetValue = if (enabled) hoverTintAlpha(hovered, pressed, dark) else 0f,
        animationSpec = AppAnimations.quick(),
        label = "hoverTint",
    )
    val ink = MaterialTheme.colorScheme.onSurface
    return this
        .then(if (enabled) Modifier.pointerHoverIcon(PointerIcon.Hand) else Modifier)
        .drawWithContent {
            drawContent()
            if (alpha > 0f) {
                drawOutline(shape.createOutline(size, layoutDirection, this), ink.copy(alpha = alpha))
            }
        }
}
