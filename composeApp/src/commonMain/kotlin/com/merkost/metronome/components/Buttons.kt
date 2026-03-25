package com.merkost.metronome.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.merkost.metronome.ui.defaultIconButtonSize
import com.merkost.metronome.ui.defaultPlayButtonSize
import com.merkost.metronome.ui.defaultSecondaryIconButtonSize

@Composable
fun MySecondaryTextButton(text: String, onClick: () -> Unit) {
    MySecondaryButton(onClick = onClick, modifier = Modifier.size(defaultSecondaryIconButtonSize)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun MySecondaryButton(
    modifier: Modifier = Modifier,
    border: BorderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
    shape: Shape = CircleShape,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    OutlinedCard(
        border = border,
        colors = CardDefaults.outlinedCardColors(),
        modifier = modifier,
        onClick = onClick,
        shape = shape
    ) {
        content()
    }
}


@Composable
fun MyIconButton(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = defaultIconButtonSize,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(size)
            .then(modifier),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        onClick = onClick,
        shape = CircleShape
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Icon(icon, icon.name, modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun PlayButton(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    size: Dp = defaultPlayButtonSize,
    onClick: () -> Unit
) {

    val animationSpec = tween<Dp>(250)
    val cornerRadius by animateDpAsState(
        targetValue = if (isPlaying)
            size / 4 else size, label = "playButtonCorners",
        animationSpec = animationSpec
    )

    // Breathing glow when playing
    val glowColor = MaterialTheme.colorScheme.primary
    val glowExtensionPx = with(LocalDensity.current) { 8.dp.toPx() }

    val glowModifier = if (isPlaying) {
        val infiniteTransition = rememberInfiniteTransition(label = "playButtonGlow")
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.06f,
            targetValue = 0.18f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 2000,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "playButtonGlowAlpha"
        )
        Modifier.drawBehind {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.maxDimension / 2f + glowExtensionPx
            drawCircle(
                color = glowColor.copy(alpha = glowAlpha),
                radius = radius,
                center = center
            )
        }
    } else {
        Modifier
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = glowModifier
            .size(size)
            .then(modifier),
        onClick = onClick,
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Crossfade(
            isPlaying, label = "playButtonIcon",
            modifier = Modifier.fillMaxSize(),
            animationSpec = tween(250)
        ) { isPlaying ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (isPlaying) {
                    Icon(
                        imageVector = Icons.Rounded.Pause,
                        contentDescription = Icons.Rounded.Pause.name,
                        modifier = Modifier
                            .size(50.dp)
                            .align(Alignment.Center)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = Icons.Rounded.PlayArrow.name,
                        modifier = Modifier
                            .size(50.dp)
                            .align(Alignment.Center)
                    )
                }
            }
        }
    }
}
