package com.merkost.metronome.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.merkost.metronome.components.AppBottomSheet
import com.merkost.metronome.components.AppChip
import com.merkost.metronome.components.AppSlider
import com.merkost.metronome.components.TimestampMillisecondsFormatter
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.pulseOnAppear
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
    stopWhenTimerEnds: Boolean,
    onStopWhenTimerEndsChange: (Boolean) -> Unit,
    onStart: (minutes: Int) -> Unit,
    onExtend: (minutes: Int) -> Unit,
    onRestart: () -> Unit,
    onStop: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppBottomSheet(title = "Practice timer", onDismiss = onDismiss) { dismissAnimated ->
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
                    stopWhenTimerEnds = stopWhenTimerEnds,
                    onStopWhenTimerEndsChange = onStopWhenTimerEndsChange,
                    onStart = { minutes ->
                        onStart(minutes)
                        dismissAnimated()
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ConfigTimerContent(
    isPlaying: Boolean,
    lastTimerMinutes: Int,
    todayPracticeTime: Long,
    practiceStreak: Int,
    totalPracticeTime: Long,
    onStart: (minutes: Int) -> Unit,
    stopWhenTimerEnds: Boolean = false,
    onStopWhenTimerEndsChange: (Boolean) -> Unit = {},
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

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacingSmall, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(spacingSmall),
            modifier = Modifier.fillMaxWidth(),
        ) {
            DURATION_PRESETS.forEach { preset ->
                AppChip(
                    selected = minutes == preset,
                    onClick = { minutes = preset },
                    label = "$preset min",
                )
            }
        }

        AppSlider(
            value = minutes.toFloat(),
            onValueChange = { minutes = it.toInt().coerceIn(1, 60) },
            valueRange = 1f..60f,
            steps = 58,
            modifier = Modifier.fillMaxWidth(),
            accessibilityLabel = "Practice timer duration, $minutes minutes",
        )

        Text(
            text = "Counts down while the metronome plays",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        SettingsSwitch(
            title = "Stop when time is up",
            checked = stopWhenTimerEnds,
            onCheckedChange = onStopWhenTimerEndsChange,
            subtitle = "Finishes the bar in progress, then stops.",
        )

        Button(
            onClick = { onStart(minutes) },
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth().heightIn(min = sheetButtonHeight),
        ) {
            Text(
                text = if (isPlaying) "Start timer" else "Start timer & play",
                fontWeight = FontWeight.Bold,
            )
        }

        HorizontalDivider()

        if (LocalDensity.current.fontScale >= 1.3f) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                StatText("Today", TimestampMillisecondsFormatter.formatHuman(todayPracticeTime))
                StatText("Streak", if (practiceStreak > 0) { if (practiceStreak == 1) "1 day" else "$practiceStreak days" } else "—")
                StatText("Total", TimestampMillisecondsFormatter.formatHuman(totalPracticeTime))
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                StatText(
                    label = "Today",
                    value = TimestampMillisecondsFormatter.formatHuman(todayPracticeTime),
                    modifier = Modifier.weight(1f),
                )
                StatText(
                    label = "Streak",
                    value = if (practiceStreak > 0) {
                        if (practiceStreak == 1) "1 day" else "$practiceStreak days"
                    } else {
                        "—"
                    },
                    modifier = Modifier.weight(1f),
                    alignCenter = true,
                )
                StatText(
                    label = "Total",
                    value = TimestampMillisecondsFormatter.formatHuman(totalPracticeTime),
                    modifier = Modifier.weight(1f),
                    alignEnd = true,
                )
            }
        }
    }
}

@Composable
internal fun ActiveTimerContent(
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
            AnimatedContent(
                targetState = done,
                transitionSpec = { AppAnimations.fadeScaleTransform },
                label = "timerHeadline",
            ) { finished ->
                Text(
                    text = if (finished) "Time's up" else TimestampMillisecondsFormatter.format(timerRemaining),
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                    modifier = if (finished) Modifier.pulseOnAppear(peakScale = 1.06f) else Modifier,
                )
            }
            AnimatedContent(
                targetState = done to isPlaying,
                transitionSpec = { AppAnimations.fadeThrough },
                label = "timerCaption",
            ) { (finished, playing) ->
                Text(
                    text = when {
                        finished -> "${TimestampMillisecondsFormatter.format(timerGoal)} practiced"
                        playing -> "of ${TimestampMillisecondsFormatter.format(timerGoal)}"
                        else -> "paused — counts down while playing"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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
            modifier = Modifier.fillMaxWidth().heightIn(min = sheetButtonHeight),
        ) {
            AnimatedContent(
                targetState = done,
                transitionSpec = { AppAnimations.fadeThrough },
                label = "timerStopLabel",
            ) { finished ->
                Text(text = if (finished) "Done" else "Stop timer", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StatText(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false,
    alignCenter: Boolean = false,
) {
    val alignment = when {
        alignCenter -> Alignment.CenterHorizontally
        alignEnd -> Alignment.End
        else -> Alignment.Start
    }
    val textAlign = when {
        alignCenter -> TextAlign.Center
        alignEnd -> TextAlign.End
        else -> TextAlign.Start
    }
    Column(modifier = modifier, horizontalAlignment = alignment) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = textAlign,

        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.fillMaxWidth(),
            textAlign = textAlign,

        )
    }
}
