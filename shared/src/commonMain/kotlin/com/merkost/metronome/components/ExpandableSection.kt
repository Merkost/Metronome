package com.merkost.metronome.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.Lucide
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall

@Composable
fun ExpandableSection(
    icon: @Composable () -> Unit,
    title: String,
    summary: String,
    summaryActive: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = AppAnimations.Bouncy,
        label = "sectionChevron"
    )
    val interactionSource = remember { MutableInteractionSource() }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onToggle
                )
                .padding(vertical = spacingMedium)
        ) {
            icon()
            Spacer(Modifier.width(spacingSmall))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (summaryActive) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (summaryActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Spacer(Modifier.width(spacingSmall))
            Icon(
                imageVector = Lucide.ChevronDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer { rotationZ = chevronRotation }
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = AppAnimations.expandEnter,
            exit = AppAnimations.shrinkExit,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(spacingMedium),
                modifier = Modifier.padding(bottom = spacingMedium)
            ) {
                content()
            }
        }
    }
}
