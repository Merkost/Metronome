package com.merkost.metronome.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.pressScale
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall
import com.merkost.metronome.ui.statusStripHeight

@Composable
fun StatusStrip(
    icon: ImageVector,
    title: String,
    caption: String,
    progress: Float,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onStop: (() -> Unit)? = null,
    titleModifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = AppAnimations.Gentle,
        label = "stripProgress"
    )

    Surface(
        shape = RoundedCornerShape(50),
        color = accent.copy(alpha = 0.10f),
        modifier = modifier
            .fillMaxWidth()
            .height(statusStripHeight)
            .pressScale(interactionSource, pressedScale = 0.97f)
            .clip(RoundedCornerShape(50))
            .drawBehind {
                drawRect(
                    color = accent.copy(alpha = 0.16f),
                    size = size.copy(width = size.width * animatedProgress)
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = spacingMedium, end = spacingSmall)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(spacingSmall))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = accent,
                modifier = titleModifier,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (onStop != null) {
                Spacer(Modifier.width(spacingSmall / 2))
                IconButton(onClick = onStop, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Stop",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Spacer(Modifier.width(spacingSmall))
            }
        }
    }
}
