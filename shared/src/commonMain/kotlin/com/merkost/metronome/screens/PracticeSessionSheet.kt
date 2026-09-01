package com.merkost.metronome.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronUp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.RotateCcw
import com.merkost.metronome.components.AppBottomSheet
import com.merkost.metronome.components.AppDialog
import com.merkost.metronome.components.sessionProgressLabel
import com.merkost.metronome.practiceSets.ActivePracticeSession
import com.merkost.metronome.practiceSets.PracticePlaybackIntent
import com.merkost.metronome.practiceSets.PracticeStepTarget
import com.merkost.metronome.practiceSets.ResolvedPracticeStep
import com.merkost.metronome.presets.PracticePreset
import com.merkost.metronome.ui.cornerRadiusLarge
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall
import metronome.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun PracticeSessionSheet(
    session: ActivePracticeSession,
    isRecovered: Boolean,
    onPrevious: () -> Unit,
    onTogglePlayback: () -> Unit,
    onNext: () -> Unit,
    onRestart: () -> Unit,
    onFinish: () -> Unit,
    onDismiss: () -> Unit,
) {
    var confirmFinish by remember { mutableStateOf(false) }
    AppBottomSheet(
        title = stringResource(Res.string.practice_session_details),
        onDismiss = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
            if (isRecovered) {
                Text(
                    stringResource(Res.string.practice_session_recovered),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(session.setName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Surface(
                shape = RoundedCornerShape(cornerRadiusLarge),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(spacingMedium),
                    verticalArrangement = Arrangement.spacedBy(spacingSmall),
                ) {
                    Text(
                        stringResource(
                            Res.string.practice_session_step,
                            session.currentStepIndex + 1,
                            session.steps.size,
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        session.currentStep.preset.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        session.currentStep.preset.rhythmSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        sessionProgressLabel(session),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (session.currentStepEdited) {
                        FilledTonalButton(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
                            Icon(Lucide.RotateCcw, contentDescription = null)
                            Spacer(Modifier.width(spacingSmall))
                            Text(stringResource(Res.string.practice_session_restart))
                        }
                    }
                }
            }
            val navigationButtons: @Composable (Modifier) -> Unit = { buttonModifier ->
                OutlinedButton(
                    onClick = onPrevious,
                    enabled = session.currentStepIndex > 0 && session.pendingStepIndex == null,
                    modifier = buttonModifier,
                ) {
                    Icon(Lucide.ArrowLeft, contentDescription = null)
                    Spacer(Modifier.width(spacingSmall))
                    Text(stringResource(Res.string.practice_session_previous))
                }
                Button(onClick = onTogglePlayback, modifier = buttonModifier) {
                    Icon(
                        if (session.playbackIntent == PracticePlaybackIntent.Running) Lucide.Pause else Lucide.Play,
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(spacingSmall))
                    Text(
                        stringResource(
                            if (session.playbackIntent == PracticePlaybackIntent.Running) Res.string.pause else Res.string.resume,
                        ),
                    )
                }
            }
            if (LocalDensity.current.fontScale >= 1.3f) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacingSmall),
                ) {
                    navigationButtons(Modifier.fillMaxWidth())
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacingSmall),
                ) {
                    navigationButtons(Modifier.weight(1f))
                }
            }
            Button(
                onClick = onNext,
                enabled = !session.isLastStep && session.pendingStepIndex == null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (session.isLastStep) {
                        stringResource(Res.string.practice_session_complete)
                    } else {
                        stringResource(Res.string.practice_session_next)
                    },
                )
                Spacer(Modifier.width(spacingSmall))
                Icon(Lucide.ArrowRight, contentDescription = null)
            }
            if (!session.isLastStep) {
                HorizontalDivider()
                Text(
                    stringResource(Res.string.practice_session_up_next),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                session.steps.drop(session.currentStepIndex + 1).forEachIndexed { offset, step ->
                    UpcomingStep(index = session.currentStepIndex + offset + 2, step = step)
                }
            }
            HorizontalDivider()
            OutlinedButton(onClick = { confirmFinish = true }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.practice_session_finish))
            }
        }
    }
    if (confirmFinish) {
        AppDialog(
            title = stringResource(Res.string.practice_session_finish_title),
            text = stringResource(Res.string.practice_session_finish_body),
            confirmLabel = stringResource(Res.string.practice_session_finish),
            dismissLabel = stringResource(Res.string.cancel),
            onConfirm = {
                confirmFinish = false
                onFinish()
                onDismiss()
            },
            onDismiss = { confirmFinish = false },
        )
    }
}

@Composable
private fun UpcomingStep(index: Int, step: ResolvedPracticeStep) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(Res.string.practice_step_number, index),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(step.preset.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            when (val target = step.target) {
                PracticeStepTarget.None -> stringResource(Res.string.practice_target_none_summary)
                is PracticeStepTarget.Duration -> stringResource(
                    Res.string.practice_target_minutes_summary,
                    target.minutes,
                )
                is PracticeStepTarget.Bars -> stringResource(Res.string.practice_target_bars_summary, target.count)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun PracticeSessionSheetPreview() {
    MaterialTheme {
        PracticeSessionSheet(
            session = ActivePracticeSession(
                id = "session",
                sourceSetId = "set",
                setName = "Daily foundations",
                steps = listOf(
                    ResolvedPracticeStep("step", previewSessionPreset(), PracticeStepTarget.Bars(8)),
                ),
                currentStepIndex = 0,
                pendingStepIndex = null,
                elapsedMillis = 0L,
                completedBars = 4,
                playbackIntent = PracticePlaybackIntent.Paused,
                targetReached = false,
                currentStepEdited = true,
                startedAtEpochMillis = 0L,
                lastCheckpointAtEpochMillis = 0L,
            ),
            isRecovered = true,
            onPrevious = {},
            onTogglePlayback = {},
            onNext = {},
            onRestart = {},
            onFinish = {},
            onDismiss = {},
        )
    }
}

private fun previewSessionPreset() = PracticePreset(
    id = "preset",
    name = "Slow accents",
    createdAtEpochMillis = 0L,
    lastUsedAtEpochMillis = null,
    isFavourite = false,
    sortPosition = 0,
    bpm = 72,
    timeSignature = com.merkost.metronome.model.TimeSignature.FOUR_FOUR,
    subdivision = com.merkost.metronome.model.Subdivision.QUARTER,
    beats = com.merkost.metronome.model.TimeSignature.FOUR_FOUR.defaultBeats,
    countInEnabled = false,
)
