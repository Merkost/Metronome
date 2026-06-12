package com.merkost.metronome.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.merkost.metronome.components.AppBottomSheet
import com.merkost.metronome.components.AppChip
import com.merkost.metronome.components.TimestampMillisecondsFormatter
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.pulseOnChange
import com.merkost.metronome.ui.sheetButtonHeight
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall

private val DURATION_PRESETS = listOf(5, 10, 15, 20, 30)

@Composable
fun PracticeTimerSheet(
    isPlaying: Boolean,
    timerGoal: Long?,
    timerRemaining: Long,
    lastTimerMinutes: Int,
    todayPracticeTime: Long,
    practiceStreak: Int,
    totalPracticeTime: Long,
    onStart: (minutes: Int) -> Unit,
    onExtend: (minutes: Int) -> Unit,
    onRestart: () -> Unit,
    onStop: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppBottomSheet(title = "Practice Timer", onDismiss = onDismiss) { dismissAnimated ->
        var lastActiveGoal by remember { mutableStateOf(timerGoal ?: 0L) }
        LaunchedEffect(timerGoal) {
            timerGoal?.let { lastActiveGoal = it }
        }

        AnimatedContent(
            targetState = timerGoal != null,
            transitionSpec = { AppAnimations.fadeScaleTransform },
            label = "practiceSheetMode"
        ) { active ->
            if (active) {
                ActiveTimerContent(
                    isPlaying = isPlaying,
                    timerGoal = timerGoal ?: lastActiveGoal,
                    timerRemaining = timerRemaining,
                    onExtend = onExtend,
                    onRestart = onRestart,
                    onStop = {
                        onStop()
                        dismissAnimated()
                    },
                )
            } else {
                ConfigTimerContent(
                    isPlaying = isPlaying,
                    lastTimerMinutes = lastTimerMinutes,
                    todayPracticeTime = todayPracticeTime,
                    practiceStreak = practiceStreak,
                    totalPracticeTime = totalPracticeTime,
                    onStart = { minutes ->
                        onStart(minutes)
                        dismissAnimated()
                    },
                )
            }
        }
    }
}

@Composable
private fun ConfigTimerContent(
    isPlaying: Boolean,
    lastTimerMinutes: Int,
    todayPracticeTime: Long,
    practiceStreak: Int,
    totalPracticeTime: Long,
    onStart: (minutes: Int) -> Unit,
) {
    var minutes by remember { mutableStateOf(lastTimerMinutes.coerceIn(1, 60)) }

    Column(verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = TimestampMillisecondsFormatter.format(minutes * 60_000L),
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                modifier = Modifier.pulseOnChange(minutes, peakScale = 1.03f),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(spacingSmall, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            DURATION_PRESETS.forEach { preset ->
                AppChip(
                    selected = minutes == preset,
                    onClick = { minutes = preset },
                    label = "$preset",
                )
            }
        }

        Slider(
            value = minutes.toFloat(),
            onValueChange = { minutes = it.toInt().coerceIn(1, 60) },
            valueRange = 1f..60f,
            steps = 58,
            colors = SliderDefaults.colors(
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent,
            ),
            modifier = Modifier.height(20.dp),
        )

        Text(
            text = "Counts down while the metronome plays",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Button(
            onClick = { onStart(minutes) },
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth().height(sheetButtonHeight),
        ) {
            Text(
                text = if (isPlaying) "Start timer" else "Start timer & play",
                fontWeight = FontWeight.Bold,
            )
        }

        HorizontalDivider()

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            StatText(
                label = "Today",
                value = TimestampMillisecondsFormatter.formatHuman(todayPracticeTime),
            )
            StatText(
                label = "Streak",
                value = if (practiceStreak > 0) {
                    if (practiceStreak == 1) "1 day" else "$practiceStreak days"
                } else {
                    "—"
                },
                alignCenter = true,
            )
            StatText(
                label = "Total",
                value = TimestampMillisecondsFormatter.formatHuman(totalPracticeTime),
                alignEnd = true,
            )
        }
    }
}

@Composable
private fun ActiveTimerContent(
    isPlaying: Boolean,
    timerGoal: Long,
    timerRemaining: Long,
    onExtend: (minutes: Int) -> Unit,
    onRestart: () -> Unit,
    onStop: () -> Unit,
) {
    val done = timerRemaining <= 0L

    Column(
        verticalArrangement = Arrangement.spacedBy(spacingMedium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (done) "Time's up" else TimestampMillisecondsFormatter.format(timerRemaining),
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
            )
            Text(
                text = when {
                    done -> "${TimestampMillisecondsFormatter.format(timerGoal)} practiced"
                    isPlaying -> "of ${TimestampMillisecondsFormatter.format(timerGoal)}"
                    else -> "paused — counts down while playing"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(spacingSmall, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = { onExtend(5) }) {
                Text("+5 min", fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onRestart) {
                Text("Restart", fontWeight = FontWeight.Bold)
            }
        }

        Button(
            onClick = onStop,
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth().height(sheetButtonHeight),
        ) {
            Text(text = if (done) "Done" else "Stop timer", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StatText(
    label: String,
    value: String,
    alignEnd: Boolean = false,
    alignCenter: Boolean = false,
) {
    val alignment = when {
        alignCenter -> Alignment.CenterHorizontally
        alignEnd -> Alignment.End
        else -> Alignment.Start
    }
    Column(horizontalAlignment = alignment) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        )
    }
}
