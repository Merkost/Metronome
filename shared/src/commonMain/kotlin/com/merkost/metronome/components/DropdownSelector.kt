package com.merkost.metronome.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.PressedScaleSurface
import com.merkost.metronome.ui.cornerRadiusLarge
import com.merkost.metronome.ui.pressableSurface
import com.merkost.metronome.ui.rememberAppHaptics
import com.merkost.metronome.ui.cornerRadiusMedium
import com.merkost.metronome.ui.minimumTouchTargetSize

@Composable
fun <T> DropdownSelector(
    expanded: Boolean,
    onDismiss: () -> Unit,
    items: List<T>,
    selectedItem: T?,
    onSelect: (T) -> Unit,
    itemContent: @Composable (T, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    footer: (@Composable () -> Unit)? = null,
    anchor: @Composable () -> Unit,
) {
    val dropdownShape = RoundedCornerShape(cornerRadiusLarge)
    val haptics = rememberAppHaptics()

    Box(modifier = modifier) {
        anchor()

        MaterialTheme(
            shapes = MaterialTheme.shapes.copy(extraSmall = dropdownShape)
        ) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismiss,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                ),
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
            ) {
                items.forEach { item ->
                    val isSelected = item == selectedItem
                    val itemBackground by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                        } else {
                            Color.Transparent
                        },
                        animationSpec = AppAnimations.standard(),
                        label = "dropdownItemBackground",
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .clip(RoundedCornerShape(cornerRadiusMedium))
                            .background(itemBackground)
                            .pressableSurface(
                                onClick = {
                                    haptics.select()
                                    onSelect(item)
                                },
                                pressedScale = PressedScaleSurface,
                            )
                            .heightIn(min = minimumTouchTargetSize)
                            .padding(PaddingValues(horizontal = 14.dp, vertical = 12.dp))
                    ) {
                        itemContent(item, isSelected)
                    }
                }
                if (footer != null) {
                    footer()
                }
            }
        }
    }
}
