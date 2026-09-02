package com.merkost.metronome.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.merkost.metronome.ui.minimumTouchTargetSize
import com.merkost.metronome.ui.rememberAppHaptics
import kotlin.math.roundToInt

@Composable
fun AppSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    accessibilityLabel: String,
    modifier: Modifier = Modifier,
    steps: Int = 0,
    showActiveTicks: Boolean = false,
) {
    val haptics = rememberAppHaptics()
    var lastTickedValue by remember { mutableStateOf(value.roundToInt()) }

    Slider(
        value = value,
        onValueChange = { newValue ->
            val rounded = newValue.roundToInt()
            if (rounded != lastTickedValue) {
                lastTickedValue = rounded
                haptics.tick()
            }
            onValueChange(newValue)
        },
        valueRange = valueRange,
        steps = steps,
        colors = SliderDefaults.colors(
            activeTickColor = if (showActiveTicks) {
                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f)
            } else {
                Color.Transparent
            },
            inactiveTickColor = Color.Transparent,
        ),
        modifier = modifier
            .heightIn(min = minimumTouchTargetSize)
            .semantics { contentDescription = accessibilityLabel },
    )
}
