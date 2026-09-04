package com.merkost.metronome.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Play
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.PressedScaleControl
import com.merkost.metronome.ui.PressedScaleSurface
import com.merkost.metronome.ui.pressScale
import com.merkost.metronome.ui.rememberAppHaptics
import com.merkost.metronome.ui.defaultIconButtonSize
import com.merkost.metronome.ui.defaultPlayButtonSize
import com.merkost.metronome.ui.defaultSecondaryIconButtonSize
import com.merkost.metronome.ui.playButtonIconSize


@Composable
fun MySecondaryTextButton(text: String, onClick: () -> Unit) {
    val haptics = rememberAppHaptics()
    MySecondaryButton(
        onClick = {
            haptics.tick()
            onClick()
        },
        modifier = Modifier.size(defaultSecondaryIconButtonSize),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(12.sp, 24.sp),
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

    OutlinedCard(
        border = border,
        colors = CardDefaults.outlinedCardColors(),
        modifier = modifier
            .clip(shape)
            .pressScale(interactionSource, PressedScaleSurface),
        onClick = onClick,
        shape = shape,
        interactionSource = interactionSource
    ) {
        content()
    }
}

@Composable
fun AppIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    IconButton(
        onClick = onClick,
        modifier = modifier.pressScale(interactionSource, PressedScaleControl),
        enabled = enabled,
        interactionSource = interactionSource,
        content = content,
    )
}

@Composable
fun MyIconButton(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = defaultIconButtonSize,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val haptics = rememberAppHaptics()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val containerColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.primary.copy(
            alpha = when {
                isPressed -> 0.16f
                isHovered -> 0.12f
                else -> 0.07f
            }
        ),
        animationSpec = AppAnimations.standard(),
        label = "iconButtonContainer"
    )

    // Built from a Box with indication = null rather than a clickable Card:
    // Material's ripple paints its hover layer across the card's bounds, which
    // reads as a square behind a round button on any pointer device. Driving
    // the container colour from the interaction state instead keeps every
    // state inside the circle.
    Box(
        modifier = modifier
            .size(size)
            .pressScale(interactionSource, PressedScaleControl)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = {
                    haptics.tick()
                    onClick()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, icon.name, tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun PlayButton(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    size: Dp = defaultPlayButtonSize,
    onClick: () -> Unit
) {
    val haptics = rememberAppHaptics()
    val interactionSource = remember { MutableInteractionSource() }

    val cornerRadius by animateDpAsState(
        targetValue = if (isPlaying) size / 4 else size,
        label = "playButtonCorners",
        animationSpec = AppAnimations.emphasized()
    )

    val glowColor = MaterialTheme.colorScheme.primary
    val glowExtensionPx = with(LocalDensity.current) { 8.dp.toPx() }
    val cornerRadiusPx = with(LocalDensity.current) { cornerRadius.toPx() }

    val glowModifier = if (isPlaying) {
        val infiniteTransition = rememberInfiniteTransition(label = "playButtonGlow")
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.05f,
            targetValue = 0.14f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
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
        modifier = modifier
            .size(size)
            .then(glowModifier)
            .clip(RoundedCornerShape(cornerRadius))
            .pressScale(interactionSource, PressedScaleControl),
        onClick = {
            haptics.confirm()
            onClick()
        },
        shape = RoundedCornerShape(cornerRadius),
        interactionSource = interactionSource
    ) {
        AnimatedContent(
            targetState = isPlaying,
            label = "playButtonIcon",
            modifier = Modifier.fillMaxSize(),
            transitionSpec = { AppAnimations.fadeScaleTransform }
        ) { playing ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (playing) Lucide.Pause else Lucide.Play,
                    contentDescription = if (playing) "Pause metronome" else "Start metronome",
                    modifier = Modifier.size(playButtonIconSize)
                )
            }
        }
    }
}
