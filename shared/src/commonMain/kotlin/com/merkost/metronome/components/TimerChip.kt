package com.merkost.metronome.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.progressRingSize
import com.merkost.metronome.ui.pulseOnAppear
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall

private sealed interface TimerChipMode {
    data class Stopwatch(val elapsed: Long) : TimerChipMode
    data class Countdown(val remaining: Long, val goal: Long, val dimmed: Boolean) : TimerChipMode
    data object Done : TimerChipMode
}

@Composable
fun TimerChip(
    modifier: Modifier = Modifier,
    stopWatchElapsed: Long,
    timerGoal: Long?,
    timerRemaining: Long,
    isPlaying: Boolean,
    onClick: () -> Unit,
) {
    val mode = when {
        timerGoal == null -> TimerChipMode.Stopwatch(stopWatchElapsed)
        timerRemaining <= 0L -> TimerChipMode.Done
        else -> TimerChipMode.Countdown(timerRemaining, timerGoal, dimmed = !isPlaying)
    }

    MySecondaryButton(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        modifier = modifier,
    ) {
        AnimatedContent(
            targetState = mode,
            transitionSpec = { AppAnimations.fadeScaleTransform },
            contentKey = { it::class },
            label = "timerChipMode"
        ) { current ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacingSmall, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacingMedium)
            ) {
                when (current) {
                    is TimerChipMode.Stopwatch -> {
                        Icon(Icons.Rounded.Timer, Icons.Rounded.Timer.name)
                        Text(
                            text = TimestampMillisecondsFormatter.format(current.elapsed),
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    is TimerChipMode.Countdown -> {
                        val contentColor = if (current.dimmed) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                        ProgressRing(
                            progress = 1f - current.remaining.toFloat() / current.goal.toFloat(),
                            color = contentColor,
                        )
                        Text(
                            text = TimestampMillisecondsFormatter.format(current.remaining),
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = contentColor,
                        )
                    }

                    is TimerChipMode.Done -> {
                        Box(
                            modifier = Modifier
                                .size(progressRingSize)
                                .pulseOnAppear(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Check,
                                Icons.Rounded.Check.name,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Text(
                            text = "Done",
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}
