package com.merkost.metronome.screens

import com.merkost.metronome.components.WeekStrip
import com.merkost.metronome.model.PracticeWeek
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
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
import com.merkost.metronome.model.UiText
import com.merkost.metronome.model.asString
import com.merkost.metronome.model.streakDayLine
import com.merkost.metronome.model.timerFinishedHeadline
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.duration_minutes
import metronome.shared.generated.resources.hub_streak
import metronome.shared.generated.resources.hub_streak_days
import metronome.shared.generated.resources.hub_today
import metronome.shared.generated.resources.hub_total
import metronome.shared.generated.resources.timer_counts_down
import metronome.shared.generated.resources.timer_done
import metronome.shared.generated.resources.timer_duration_description
import metronome.shared.generated.resources.timer_extend
import metronome.shared.generated.resources.timer_of_goal
import metronome.shared.generated.resources.timer_paused_caption
import metronome.shared.generated.resources.timer_practiced_stopped
import metronome.shared.generated.resources.timer_restart
import metronome.shared.generated.resources.timer_start
import metronome.shared.generated.resources.timer_start_and_play
import metronome.shared.generated.resources.timer_stop
import metronome.shared.generated.resources.timer_stop_when_up
import metronome.shared.generated.resources.timer_stop_when_up_hint
import metronome.shared.generated.resources.timer_title
import metronome.shared.generated.resources.voice_practiced
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

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
    stoppedPlayback: Boolean,
    onStart: (minutes: Int) -> Unit,
    onExtend: (minutes: Int) -> Unit,
    onRestart: () -> Unit,
    onStop: () -> Unit,
    onDismiss: () -> Unit,
    practiceWeek: PracticeWeek? = null,
) {
    AppBottomSheet(title = stringResource(Res.string.timer_title), onDismiss = onDismiss) { dismissAnimated ->
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
                    stopWhenTimerEnds = stopWhenTimerEnds,
                    onStopWhenTimerEndsChange = onStopWhenTimerEndsChange,
                    stoppedPlayback = stoppedPlayback,
                    practiceStreak = practiceStreak,
                    week = practiceWeek,
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
                    label = stringResource(Res.string.duration_minutes, preset),
                )
            }
        }

        AppSlider(
            value = minutes.toFloat(),
            onValueChange = { minutes = it.toInt().coerceIn(1, 60) },
            valueRange = 1f..60f,
            steps = 58,
            modifier = Modifier.fillMaxWidth(),
            accessibilityLabel = pluralStringResource(Res.plurals.timer_duration_description, minutes, minutes),
        )

        Text(
            text = stringResource(Res.string.timer_counts_down),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        SettingsSwitch(
            title = stringResource(Res.string.timer_stop_when_up),
            checked = stopWhenTimerEnds,
            onCheckedChange = onStopWhenTimerEndsChange,
            subtitle = stringResource(Res.string.timer_stop_when_up_hint),
        )

        Button(
            onClick = { onStart(minutes) },
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth().heightIn(min = sheetButtonHeight),
        ) {
            Text(
                text = stringResource(if (isPlaying) Res.string.timer_start else Res.string.timer_start_and_play),
                fontWeight = FontWeight.Bold,
            )
        }

        HorizontalDivider()

        if (LocalDensity.current.fontScale >= 1.3f) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                StatText(stringResource(Res.string.hub_today), TimestampMillisecondsFormatter.formatHuman(todayPracticeTime))
                StatText(stringResource(Res.string.hub_streak), if (practiceStreak > 0) pluralStringResource(Res.plurals.hub_streak_days, practiceStreak, practiceStreak) else "—")
                StatText(stringResource(Res.string.hub_total), TimestampMillisecondsFormatter.formatHuman(totalPracticeTime))
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                StatText(
                    label = stringResource(Res.string.hub_today),
                    value = TimestampMillisecondsFormatter.formatHuman(todayPracticeTime),
                    modifier = Modifier.weight(1f),
                )
                StatText(
                    label = stringResource(Res.string.hub_streak),
                    value = if (practiceStreak > 0) {
                        pluralStringResource(Res.plurals.hub_streak_days, practiceStreak, practiceStreak)
                    } else {
                        "—"
                    },
                    modifier = Modifier.weight(1f),
                    alignCenter = true,
                )
                StatText(
                    label = stringResource(Res.string.hub_total),
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
    stopWhenTimerEnds: Boolean = false,
    onStopWhenTimerEndsChange: (Boolean) -> Unit = {},
    stoppedPlayback: Boolean = false,
    practiceStreak: Int = 0,
    week: PracticeWeek? = null,
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
                    text = if (finished) timerFinishedHeadline((timerGoal / 60_000L).toInt()).asString() else TimestampMillisecondsFormatter.format(timerRemaining),
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
                        finished && stoppedPlayback -> stringResource(Res.string.timer_practiced_stopped, TimestampMillisecondsFormatter.format(timerGoal))
                        finished -> UiText.Joined(
                            listOfNotNull(
                                UiText.of(Res.string.voice_practiced, TimestampMillisecondsFormatter.format(timerGoal)),
                                streakDayLine(practiceStreak),
                            ),
                            " · ",
                        ).asString()
                        playing -> stringResource(Res.string.timer_of_goal, TimestampMillisecondsFormatter.format(timerGoal))
                        else -> stringResource(Res.string.timer_paused_caption)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = if (finished) Modifier.semantics { liveRegion = LiveRegionMode.Polite } else Modifier,
                )
            }
        }

        if (done && week != null) WeekStrip(week)

        Row(
            horizontalArrangement = Arrangement.spacedBy(spacingSmall, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = { onExtend(5) }) {
                Text(stringResource(Res.string.timer_extend), fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onRestart) {
                Text(stringResource(Res.string.timer_restart), fontWeight = FontWeight.Bold)
            }
        }

        if (!done) {
            SettingsSwitch(
                title = stringResource(Res.string.timer_stop_when_up),
                checked = stopWhenTimerEnds,
                onCheckedChange = onStopWhenTimerEndsChange,
                subtitle = stringResource(Res.string.timer_stop_when_up_hint),
            )
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
                Text(text = stringResource(if (finished) Res.string.timer_done else Res.string.timer_stop), fontWeight = FontWeight.Bold)
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
