package com.merkost.metronome.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * A generic reusable dropdown selector that wraps an anchor element with a [DropdownMenu].
 *
 * @param T the type of items in the dropdown
 * @param expanded controls menu visibility
 * @param onDismiss called when the user taps outside the menu
 * @param items the menu items to display
 * @param selectedItem the currently selected item (used for highlight)
 * @param onSelect called when the user selects an item
 * @param itemContent how to render each item; receives the item and whether it is selected
 * @param modifier modifier applied to the outer Box
 * @param anchor the tappable element that triggers the dropdown
 */
@Composable
fun <T> DropdownSelector(
    expanded: Boolean,
    onDismiss: () -> Unit,
    items: List<T>,
    selectedItem: T?,
    onSelect: (T) -> Unit,
    itemContent: @Composable (T, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    anchor: @Composable () -> Unit,
) {
    val dropdownShape = RoundedCornerShape(16.dp)

    Box(modifier = modifier) {
        anchor()

        MaterialTheme(
            shapes = MaterialTheme.shapes.copy(extraSmall = dropdownShape)
        ) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismiss,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                items.forEach { item ->
                    val isSelected = item == selectedItem
                    val itemBackground = if (isSelected) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.surface
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(itemBackground)
                            .clickable { onSelect(item) }
                            .padding(
                                PaddingValues(
                                    horizontal = 16.dp,
                                    vertical = 12.dp,
                                )
                            )
                    ) {
                        itemContent(item, isSelected)
                    }
                }
            }
        }
    }
}
