package com.merkost.metronome.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.pressScale

@Composable
fun AppChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    onTrailingClose: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = spring(stiffness = 600f, dampingRatio = 0.8f),
        label = "chipContainer"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = spring(stiffness = 600f, dampingRatio = 0.8f),
        label = "chipContent"
    )

    Surface(
        shape = RoundedCornerShape(50),
        color = containerColor,
        modifier = modifier
            .pressScale(interactionSource, pressedScale = 0.94f)
            .clip(RoundedCornerShape(50))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                color = contentColor,
            )
            if (onTrailingClose != null) {
                Icon(
                    imageVector = Lucide.X,
                    contentDescription = "Remove",
                    tint = contentColor,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { onTrailingClose() }
                )
            }
        }
    }
}
