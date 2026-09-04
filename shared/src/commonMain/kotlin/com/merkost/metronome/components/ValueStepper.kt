package com.merkost.metronome.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Minus
import com.composables.icons.lucide.Plus
import com.merkost.metronome.ui.AnimatedNumberText
import com.merkost.metronome.ui.PressedScaleControl
import com.merkost.metronome.ui.pressScale
import com.merkost.metronome.ui.rememberAppHaptics
import com.merkost.metronome.ui.spacingSmall
import com.merkost.metronome.ui.stepperButtonSize
import kotlinx.coroutines.delay

@Composable
fun ValueStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacingSmall / 2)
    ) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacingSmall)
        ) {
            RepeatingStepButton(
                icon = Lucide.Minus,
                enabled = value > range.first,
                onStep = { step -> onValueChange((value - step).coerceIn(range)) }
            )
            AnimatedNumberText(
                value = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                modifier = Modifier.width(60.dp),
                autoSize = TextAutoSize.StepBased(12.sp, 24.sp),
            )
            RepeatingStepButton(
                icon = Lucide.Plus,
                enabled = value < range.last,
                onStep = { step -> onValueChange((value + step).coerceIn(range)) }
            )
        }
    }
}

@Composable
private fun RepeatingStepButton(
    icon: ImageVector,
    enabled: Boolean,
    onStep: (Int) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptics = rememberAppHaptics()
    val currentOnStep by rememberUpdatedState(onStep)
    val currentEnabled by rememberUpdatedState(enabled)
    val holdConsumed = remember { booleanArrayOf(false) }

    LaunchedEffect(isPressed) {
        if (isPressed && currentEnabled) {
            holdConsumed[0] = false
            delay(400L)
            holdConsumed[0] = true
            var repeats = 0
            while (currentEnabled) {
                if (repeats % 3 == 0) haptics.tick()
                currentOnStep(if (repeats >= 10) 5 else 1)
                repeats++
                delay(90L)
            }
        }
    }

    val isHovered by interactionSource.collectIsHoveredAsState()
    val container = when {
        !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    // indication = null for the same reason as MyIconButton: the ripple's
    // hover layer is drawn to the component bounds, not the circle.
    Box(
        modifier = Modifier
            .size(stepperButtonSize)
            .pressScale(interactionSource, pressedScale = PressedScaleControl)
            .clip(CircleShape)
            .background(container)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = {
                    if (holdConsumed[0]) {
                        holdConsumed[0] = false
                    } else {
                        haptics.tick()
                        onStep(1)
                    }
                },
            ),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = if (icon == Lucide.Minus) "Decrease" else "Increase", modifier = Modifier.size(20.dp))
        }
    }
}
