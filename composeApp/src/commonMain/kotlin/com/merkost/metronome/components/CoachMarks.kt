package com.merkost.metronome.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

private data class CoachStep(val title: String, val description: String, val tooltipBelow: Boolean)

private val coachSteps = listOf(
    CoachStep(
        "Customize your beat",
        "Tap any circle to toggle accent beats. The first beat is accented by default \u2014 creating a strong downbeat.",
        true
    ),
    CoachStep(
        "Set your tempo",
        "Drag the slider, use \u00B1 buttons, or tap the tempo name above for quick presets like Allegro or Presto.",
        true
    ),
    CoachStep(
        "Ready to play!",
        "Hit play to start. Use Tap Tempo to match any song\u2019s beat. The timer tracks your practice.",
        false
    ),
)

private val SpotlightPadding = 12.dp
private val TooltipMargin = 16.dp
private val TooltipHorizontalMargin = 24.dp
private val GotItGreen = Color(0xFF22C55E)

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
    val cornerRadiusPx = with(density) { 16.dp.toPx() }

    val spotlightRect = Rect(
        left = target.left - spotlightPaddingPx,
        top = target.top - spotlightPaddingPx,
        right = target.right + spotlightPaddingPx,
        bottom = target.bottom + spotlightPaddingPx
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(100f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* consume taps on overlay */ }
    ) {
        // Semi-transparent overlay with spotlight cutout
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
                color = Color.Black.copy(alpha = 0.82f)
            )
        }

        // Skip button at top-right
        Text(
            text = "Skip",
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 52.dp, end = 20.dp)
                .clickable { onDismiss() }
        )

        // Tooltip card
        val tooltipMarginPx = with(density) { TooltipMargin.toPx() }
        var cardHeightPx by remember { mutableStateOf(0) }

        val tooltipYOffset = if (currentStep.tooltipBelow) {
            (spotlightRect.bottom + tooltipMarginPx).toInt()
        } else {
            (spotlightRect.top - tooltipMarginPx - cardHeightPx).toInt()
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TooltipHorizontalMargin)
                .onGloballyPositioned { coordinates ->
                    cardHeightPx = coordinates.size.height
                }
                .offset { IntOffset(0, tooltipYOffset) },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            TooltipContent(
                step = step,
                totalSteps = coachSteps.size,
                currentStep = currentStep,
                onNext = onNext,
                onBack = onBack,
                onDismiss = onDismiss
            )
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = currentStep.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.Black
        )
        Text(
            text = currentStep.description,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${step + 1} / $totalSteps",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = Color.Gray
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 0) {
                    TextButton(onClick = onBack) {
                        Text(
                            text = "\u2190 Back",
                            color = Color.DarkGray
                        )
                    }
                }
                if (step < totalSteps - 1) {
                    Button(
                        onClick = onNext,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(text = "Next \u2192")
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GotItGreen
                        )
                    ) {
                        Text(text = "Got it \u2713", color = Color.White)
                    }
                }
            }
        }
    }
}
