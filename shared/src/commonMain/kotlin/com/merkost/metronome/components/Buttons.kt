package com.merkost.metronome.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.defaultIconButtonSize
import com.merkost.metronome.ui.defaultPlayButtonSize
import com.merkost.metronome.ui.defaultSecondaryIconButtonSize
import com.merkost.metronome.ui.playButtonIconSize

private const val BUTTON_PRESS_SCALE = 0.96f
private const val PLAY_PRESS_SCALE = 0.92f

@Composable
fun MySecondaryTextButton(text: String, onClick: () -> Unit) {
    MySecondaryButton(onClick = onClick, modifier = Modifier.size(defaultSecondaryIconButtonSize)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun MySecondaryButton(
    modifier: Modifier = Modifier,
    border: BorderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
    shape: Shape = CircleShape,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) BUTTON_PRESS_SCALE else 1f,
        animationSpec = AppAnimations.Interactive,
        label = "secondaryButtonScale"
    )

    OutlinedCard(
        border = border,
        colors = CardDefaults.outlinedCardColors(),
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        onClick = onClick,
        shape = shape,
        interactionSource = interactionSource
    ) {
        content()
    }
}

@Composable
fun MyIconButton(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = defaultIconButtonSize,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) BUTTON_PRESS_SCALE else 1f,
        animationSpec = AppAnimations.Interactive,
        label = "iconButtonScale"
    )

    Card(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(modifier),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        onClick = onClick,
        shape = CircleShape,
        interactionSource = interactionSource
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Icon(icon, icon.name, modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun PlayButton(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    size: Dp = defaultPlayButtonSize,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) PLAY_PRESS_SCALE else 1f,
        animationSpec = AppAnimations.Interactive,
        label = "playButtonPressScale"
    )

    val cornerRadius by animateDpAsState(
        targetValue = if (isPlaying) size / 4 else size,
        label = "playButtonCorners",
        animationSpec = tween(250)
    )

    val glowColor = MaterialTheme.colorScheme.primary
    val glowExtensionPx = with(LocalDensity.current) { 8.dp.toPx() }
    val cornerRadiusPx = with(LocalDensity.current) { cornerRadius.toPx() }

    val glowModifier = if (isPlaying) {
        val infiniteTransition = rememberInfiniteTransition(label = "playButtonGlow")
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.06f,
            targetValue = 0.18f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "playButtonGlowAlpha"
        )
        Modifier.drawBehind {
            val ext = glowExtensionPx
            drawRoundRect(
                color = glowColor.copy(alpha = glowAlpha),
                topLeft = Offset(-ext, -ext),
                size = androidx.compose.ui.geometry.Size(
                    this.size.width + ext * 2,
                    this.size.height + ext * 2
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadiusPx + ext)
            )
        }
    } else {
        Modifier
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = glowModifier
            .size(size)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .then(modifier),
        onClick = onClick,
        shape = RoundedCornerShape(cornerRadius),
        interactionSource = interactionSource
    ) {
        Crossfade(
            isPlaying,
            label = "playButtonIcon",
            modifier = Modifier.fillMaxSize(),
            animationSpec = tween(250)
        ) { playing ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(playButtonIconSize)
                )
            }
        }
    }
}
