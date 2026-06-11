package com.merkost.metronome.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.cornerRadiusLarge
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall

private data class CoachStep(val title: String, val description: String, val tooltipBelow: Boolean)

private val coachSteps = listOf(
    CoachStep(
        "Shape your beat",
        "Tap any circle to cycle accented, normal, and muted beats.",
        true
    ),
    CoachStep(
        "Set your tempo",
        "Drag the slider or use the buttons. The tempo name opens presets, subdivisions, and trainers.",
        true
    ),
    CoachStep(
        "Ready to play",
        "Hit play to start. Tap Tempo matches any song, and the timer tracks your practice.",
        false
    ),
)

private val SpotlightPadding = 12.dp
private val TooltipMargin = 16.dp
private val TooltipHorizontalMargin = 24.dp

@Composable
fun CoachMarksOverlay(
    step: Int,
    targetBounds: List<Rect>,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (step !in coachSteps.indices || targetBounds.size < 3) return

    val currentStep = coachSteps[step]
    val target = targetBounds[step]
    val density = LocalDensity.current
    val spotlightPaddingPx = with(density) { SpotlightPadding.toPx() }
    val cornerRadiusPx = with(density) { cornerRadiusLarge.toPx() }

    val spotLeft by animateFloatAsState(
        target.left - spotlightPaddingPx, AppAnimations.Gentle, label = "spotL"
    )
    val spotTop by animateFloatAsState(
        target.top - spotlightPaddingPx, AppAnimations.Gentle, label = "spotT"
    )
    val spotRight by animateFloatAsState(
        target.right + spotlightPaddingPx, AppAnimations.Gentle, label = "spotR"
    )
    val spotBottom by animateFloatAsState(
        target.bottom + spotlightPaddingPx, AppAnimations.Gentle, label = "spotB"
    )
    val spotlightRect = Rect(spotLeft, spotTop, spotRight, spotBottom)

    var containerHeight by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(100f)
            .onGloballyPositioned { coordinates ->
                containerHeight = coordinates.size.height.toFloat()
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { }
    ) {
        val scrimColor = MaterialTheme.colorScheme.scrim
        Canvas(modifier = Modifier.fillMaxSize()) {
            val overlayPath = Path().apply {
                fillType = PathFillType.EvenOdd
                addRect(Rect(0f, 0f, size.width, size.height))
                addRoundRect(
                    RoundRect(
                        rect = spotlightRect,
                        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                    )
                )
            }
            drawPath(
                path = overlayPath,
                color = scrimColor.copy(alpha = 0.82f)
            )
        }

        Text(
            text = "Skip",
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 52.dp, end = 20.dp)
                .clickable { onDismiss() }
        )

        val tooltipMarginPx = with(density) { TooltipMargin.toPx() }
        var cardHeightPx by remember { mutableStateOf(0) }

        val targetY = if (currentStep.tooltipBelow) {
            containerHeight - cardHeightPx - tooltipMarginPx * 2
        } else {
            target.top - spotlightPaddingPx - tooltipMarginPx - cardHeightPx
        }
        val positioned = containerHeight > 0f && cardHeightPx > 0
        val tooltipY = remember { Animatable(0f) }
        var snapped by remember { mutableStateOf(false) }
        LaunchedEffect(targetY, positioned) {
            if (!positioned) return@LaunchedEffect
            if (snapped) {
                tooltipY.animateTo(targetY, AppAnimations.Gentle)
            } else {
                tooltipY.snapTo(targetY)
                snapped = true
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TooltipHorizontalMargin)
                .onGloballyPositioned { coordinates ->
                    cardHeightPx = coordinates.size.height
                }
                .offset { IntOffset(0, tooltipY.value.toInt()) }
                .graphicsLayer { alpha = if (snapped) 1f else 0f },
            shape = RoundedCornerShape(cornerRadiusLarge),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tooltipContent"
            ) { animatedStep ->
                if (animatedStep in coachSteps.indices) {
                    TooltipContent(
                        step = animatedStep,
                        totalSteps = coachSteps.size,
                        currentStep = coachSteps[animatedStep],
                        onNext = onNext,
                        onBack = onBack,
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun TooltipContent(
    step: Int,
    totalSteps: Int,
    currentStep: CoachStep,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(spacingMedium)
    ) {
        Text(
            text = currentStep.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = currentStep.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepDots(count = totalSteps, current = step)
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(visible = step > 0, enter = fadeIn(), exit = fadeOut()) {
                    TextButton(onClick = onBack) {
                        Text(
                            text = "Back",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                val isLast = step == totalSteps - 1
                Button(
                    onClick = if (isLast) onDismiss else onNext,
                    shape = CircleShape,
                ) {
                    Text(
                        text = if (isLast) "Got it" else "Next",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun StepDots(count: Int, current: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            val active = index == current
            val dotWidth by animateDpAsState(
                targetValue = if (active) 18.dp else 7.dp,
                animationSpec = spring(stiffness = 600f, dampingRatio = 0.8f),
                label = "dotWidth"
            )
            Box(
                modifier = Modifier
                    .height(7.dp)
                    .width(dotWidth)
                    .clip(CircleShape)
                    .background(
                        if (active) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
            )
        }
    }
}
