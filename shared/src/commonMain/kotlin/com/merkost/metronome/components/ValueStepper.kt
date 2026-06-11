package com.merkost.metronome.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.merkost.metronome.ui.AnimatedNumberText
import com.merkost.metronome.ui.pressScale
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
                icon = Icons.Rounded.Remove,
                enabled = value > range.first,
                onStep = { step -> onValueChange((value - step).coerceIn(range)) }
            )
            AnimatedNumberText(
                value = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                modifier = Modifier.widthIn(min = 52.dp),
            )
            RepeatingStepButton(
                icon = Icons.Rounded.Add,
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
                currentOnStep(if (repeats >= 10) 5 else 1)
                repeats++
                delay(90L)
            }
        }
    }

    Card(
        modifier = Modifier
            .size(stepperButtonSize)
            .pressScale(interactionSource, pressedScale = 0.9f),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        onClick = {
            if (holdConsumed[0]) {
                holdConsumed[0] = false
            } else {
                onStep(1)
            }
        },
        enabled = enabled,
        shape = CircleShape,
        interactionSource = interactionSource
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(icon, icon.name, modifier = Modifier.size(20.dp))
        }
    }
}
