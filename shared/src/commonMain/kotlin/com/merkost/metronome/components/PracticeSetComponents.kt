package com.merkost.metronome.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronUp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.EllipsisVertical
import com.composables.icons.lucide.GripVertical
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.RotateCcw
import com.composables.icons.lucide.Trash2
import com.merkost.metronome.practiceSets.ActivePracticeSession
import com.merkost.metronome.practiceSets.PracticePlaybackIntent
import com.merkost.metronome.practiceSets.PracticeSet
import com.merkost.metronome.practiceSets.PracticeSetStep
import com.merkost.metronome.practiceSets.PracticeStepTarget
import com.merkost.metronome.presets.PracticePreset
import com.merkost.metronome.ui.cornerRadiusLarge
import com.merkost.metronome.ui.minimumTouchTargetSize
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.pressableSurface
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall
import metronome.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun PracticeAgainRow(
    practiceSet: PracticeSet,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(
        Res.string.practice_again_description,
        practiceSet.name,
        practiceSet.steps.size,
    )
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadiusLarge),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minimumTouchTargetSize)
                .pressableSurface(onClick = onClick)
                .semantics(mergeDescendants = true) { contentDescription = description }
                .padding(spacingMedium),
            horizontalArrangement = Arrangement.spacedBy(spacingMedium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Lucide.RotateCcw,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(Res.string.practice_again),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = practiceSet.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(Res.string.practice_set_step_count, practiceSet.steps.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Lucide.Play,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun PracticeSetRow(
    practiceSet: PracticeSet,
    missingPresetCount: Int,
    isActive: Boolean,
    isReordering: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onStart: () -> Unit,
    onResume: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var actionsExpanded by remember { mutableStateOf(false) }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadiusLarge),
        color = animateColorAsState(
            targetValue = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
            animationSpec = AppAnimations.standard(),
            label = "practiceSetRowSurface",
        ).value,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pressableSurface(
                    onClick = if (isActive) onResume else onStart,
                    enabled = !isReordering && missingPresetCount == 0,
                )
                .padding(spacingSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedVisibility(
                visible = isReordering,
                enter = AppAnimations.revealEnter,
                exit = AppAnimations.concealExit,
            ) {
                Icon(
                    imageVector = Lucide.GripVertical,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = spacingSmall),
                )
            }
            Column(
                modifier = Modifier.weight(1f).padding(start = spacingSmall),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = practiceSet.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val detail = when {
                    missingPresetCount > 0 -> stringResource(Res.string.practice_step_missing)
                    isActive -> stringResource(Res.string.practice_set_active)
                    else -> stringResource(Res.string.practice_set_step_count, practiceSet.steps.size)
                }
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (missingPresetCount > 0) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            AnimatedContent(
                targetState = isReordering,
                transitionSpec = { AppAnimations.fadeThrough },
                label = "practiceSetRowActions",
            ) { reordering ->
            Row(verticalAlignment = Alignment.CenterVertically) {
            if (reordering) {
                SetIconButton(
                    icon = Lucide.ChevronUp,
                    description = stringResource(Res.string.move_up, practiceSet.name),
                    enabled = canMoveUp,
                    onClick = onMoveUp,
                )
                SetIconButton(
                    icon = Lucide.ChevronDown,
                    description = stringResource(Res.string.move_down, practiceSet.name),
                    enabled = canMoveDown,
                    onClick = onMoveDown,
                )
            } else {
                if (missingPresetCount == 0) {
                    SetIconButton(
                        icon = Lucide.Play,
                        description = stringResource(
                            if (isActive) Res.string.practice_set_resume else Res.string.practice_set_start,
                        ),
                        onClick = if (isActive) onResume else onStart,
                    )
                }
                Box {
                    SetIconButton(
                        icon = Lucide.EllipsisVertical,
                        description = stringResource(Res.string.practice_set_actions, practiceSet.name),
                        onClick = { actionsExpanded = true },
                    )
                    DropdownMenu(expanded = actionsExpanded, onDismissRequest = { actionsExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.practice_set_edit)) },
                            onClick = {
                                actionsExpanded = false
                                onEdit()
                            },
                            leadingIcon = { Icon(Lucide.Pencil, contentDescription = null) },
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.delete)) },
                            onClick = {
                                actionsExpanded = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Lucide.Trash2, contentDescription = null) },
                        )
                    }
                }
            }
            }
            }
        }
    }
}

@Composable
fun PracticeSetStepRow(
    index: Int,
    step: PracticeSetStep,
    preset: PracticePreset?,
    isReordering: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onTargetChange: (PracticeStepTarget) -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadiusLarge),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(spacingMedium),
            verticalArrangement = Arrangement.spacedBy(spacingSmall),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedVisibility(
                    visible = isReordering,
                    enter = AppAnimations.revealEnter,
                    exit = AppAnimations.concealExit,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Lucide.GripVertical, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(spacingSmall))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.practice_step_number, index + 1),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = preset?.name ?: stringResource(Res.string.practice_step_missing),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = preset?.rhythmSummary ?: stringResource(Res.string.practice_step_missing_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (preset == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                AnimatedContent(
                    targetState = isReordering,
                    transitionSpec = { AppAnimations.fadeThrough },
                    label = "practiceStepRowActions",
                ) { reordering ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (reordering) {
                            SetIconButton(
                                icon = Lucide.ChevronUp,
                                description = stringResource(Res.string.move_up, preset?.name.orEmpty()),
                                enabled = canMoveUp,
                                onClick = onMoveUp,
                            )
                            SetIconButton(
                                icon = Lucide.ChevronDown,
                                description = stringResource(Res.string.move_down, preset?.name.orEmpty()),
                                enabled = canMoveDown,
                                onClick = onMoveDown,
                            )
                        } else {
                            SetIconButton(
                                icon = Lucide.Trash2,
                                description = stringResource(
                                    Res.string.practice_step_remove,
                                    preset?.name ?: stringResource(Res.string.practice_step_number, index + 1),
                                ),
                                onClick = onRemove,
                            )
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = !isReordering,
                enter = AppAnimations.expandEnter,
                exit = AppAnimations.shrinkExit,
            ) {
                PracticeTargetEditor(target = step.target, onTargetChange = onTargetChange)
            }
        }
    }
}

@Composable
fun PracticeTargetEditor(
    target: PracticeStepTarget,
    onTargetChange: (PracticeStepTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
        Text(
            text = stringResource(Res.string.practice_target),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val targetChoices: @Composable (Modifier) -> Unit = { choiceModifier ->
            TargetChip(
                label = stringResource(Res.string.practice_target_none),
                selected = target is PracticeStepTarget.None,
                onClick = { onTargetChange(PracticeStepTarget.None) },
                modifier = choiceModifier,
            )
            TargetChip(
                label = stringResource(Res.string.practice_target_time),
                selected = target is PracticeStepTarget.Duration,
                onClick = {
                    onTargetChange(
                        target as? PracticeStepTarget.Duration ?: PracticeStepTarget.Duration(5),
                    )
                },
                modifier = choiceModifier,
            )
            TargetChip(
                label = stringResource(Res.string.practice_target_bars),
                selected = target is PracticeStepTarget.Bars,
                onClick = {
                    onTargetChange(target as? PracticeStepTarget.Bars ?: PracticeStepTarget.Bars(8))
                },
                modifier = choiceModifier,
            )
        }
        if (LocalDensity.current.fontScale >= 1.3f) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacingSmall),
            ) {
                targetChoices(Modifier.fillMaxWidth())
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacingSmall),
            ) {
                targetChoices(Modifier.weight(1f))
            }
        }
        when (target) {
            PracticeStepTarget.None -> Unit
            is PracticeStepTarget.Duration -> ValueStepper(
                value = target.minutes,
                onValueChange = { onTargetChange(PracticeStepTarget.Duration(it)) },
                range = 1..PracticeSet.MAX_DURATION_MINUTES,
                label = stringResource(Res.string.minutes),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            is PracticeStepTarget.Bars -> ValueStepper(
                value = target.count,
                onValueChange = { onTargetChange(PracticeStepTarget.Bars(it)) },
                range = 1..PracticeSet.MAX_BARS,
                label = stringResource(Res.string.bars),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

@Composable
fun PracticeSessionStrip(
    session: ActivePracticeSession,
    isRecovered: Boolean,
    onPrevious: () -> Unit,
    onTogglePlayback: () -> Unit,
    onNext: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadiusLarge),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Column(
            modifier = Modifier
                .pressableSurface(onClick = onOpen)
                .padding(spacingMedium),
            verticalArrangement = Arrangement.spacedBy(spacingSmall),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.setName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    AnimatedContent(
                        targetState = session.currentStepIndex,
                        transitionSpec = {
                            AppAnimations.slideLabelTransform(towardsUp = targetState >= initialState)
                        },
                        label = "practiceStripStep",
                    ) { stepIndex ->
                        Text(
                            text = stringResource(
                                Res.string.practice_session_step,
                                stepIndex + 1,
                                session.steps.size,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                Text(
                    text = sessionProgressLabel(session),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (session.targetReached) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    },
                )
            }
            if (isRecovered || session.currentStepEdited || session.pendingStepIndex != null) {
                Text(
                    text = when {
                        isRecovered -> stringResource(Res.string.practice_session_recovered)
                        session.pendingStepIndex != null -> stringResource(Res.string.practice_session_pending)
                        else -> stringResource(Res.string.practice_session_edited)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SetIconButton(
                    icon = Lucide.ArrowLeft,
                    description = stringResource(Res.string.practice_session_previous),
                    enabled = session.currentStepIndex > 0 && session.pendingStepIndex == null,
                    onClick = onPrevious,
                )
                FilledTonalButton(onClick = onTogglePlayback) {
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
                SetIconButton(
                    icon = Lucide.ArrowRight,
                    description = stringResource(Res.string.practice_session_next),
                    enabled = !session.isLastStep && session.pendingStepIndex == null,
                    onClick = onNext,
                )
            }
        }
    }
}

@Composable
fun sessionProgressLabel(session: ActivePracticeSession): String {
    if (session.targetReached) return stringResource(Res.string.practice_session_target_reached)
    return when (val target = session.currentStep.target) {
        PracticeStepTarget.None -> stringResource(Res.string.practice_session_open_ended)
        is PracticeStepTarget.Duration -> stringResource(
            Res.string.practice_session_time_progress,
            session.elapsedMillis / 60_000L,
            target.minutes,
        )
        is PracticeStepTarget.Bars -> stringResource(
            Res.string.practice_session_bar_progress,
            session.completedBars,
            target.count,
        )
    }
}

@Composable
private fun TargetChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, maxLines = 2) },
        modifier = modifier,
    )
}

@Composable
private fun SetIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    AppIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(minimumTouchTargetSize)
            .semantics { contentDescription = description },
    ) {
        Icon(icon, contentDescription = null)
    }
}

@Preview
@Composable
private fun PracticeAgainRowPreview() {
    MaterialTheme {
        PracticeAgainRow(
            practiceSet = previewSet().copy(lastCompletedAtEpochMillis = 100L),
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun PracticeAgainRowLargeTextPreview() {
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
        MaterialTheme {
            PracticeAgainRow(
                practiceSet = previewSet().copy(
                    name = "Daily foundations and repertoire transitions",
                    lastCompletedAtEpochMillis = 100L,
                ),
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun PracticeSetRowPreview() {
    MaterialTheme {
        PracticeSetRow(
            practiceSet = previewSet(),
            missingPresetCount = 0,
            isActive = false,
            isReordering = false,
            canMoveUp = false,
            canMoveDown = true,
            onStart = {},
            onResume = {},
            onEdit = {},
            onDelete = {},
            onMoveUp = {},
            onMoveDown = {},
        )
    }
}

@Preview
@Composable
private fun PracticeSetStepRowPreview() {
    MaterialTheme {
        PracticeSetStepRow(
            index = 0,
            step = previewSet().steps.first(),
            preset = previewPreset(),
            isReordering = false,
            canMoveUp = false,
            canMoveDown = false,
            onTargetChange = {},
            onRemove = {},
            onMoveUp = {},
            onMoveDown = {},
        )
    }
}

@Preview
@Composable
private fun PracticeSessionStripPreview() {
    MaterialTheme {
        PracticeSessionStrip(
            session = ActivePracticeSession(
                id = "session-1",
                sourceSetId = "set-1",
                setName = "Daily foundations",
                steps = listOf(
                    com.merkost.metronome.practiceSets.ResolvedPracticeStep(
                        "step-1",
                        previewPreset(),
                        PracticeStepTarget.Bars(8),
                    ),
                ),
                currentStepIndex = 0,
                pendingStepIndex = null,
                elapsedMillis = 60_000L,
                completedBars = 4,
                playbackIntent = PracticePlaybackIntent.Running,
                targetReached = false,
                currentStepEdited = false,
                startedAtEpochMillis = 0L,
                lastCheckpointAtEpochMillis = 0L,
            ),
            isRecovered = false,
            onPrevious = {},
            onTogglePlayback = {},
            onNext = {},
            onOpen = {},
        )
    }
}

private fun previewSet() = PracticeSet(
    id = "set-1",
    name = "Daily foundations",
    createdAtEpochMillis = 0L,
    updatedAtEpochMillis = 0L,
    lastStartedAtEpochMillis = null,
    lastCompletedAtEpochMillis = null,
    sortPosition = 0,
    steps = listOf(PracticeSetStep("step-1", "preset-1", PracticeStepTarget.Bars(8))),
)

private fun previewPreset() = PracticePreset(
    id = "preset-1",
    name = "Slow accents",
    createdAtEpochMillis = 0L,
    lastUsedAtEpochMillis = null,
    isFavourite = false,
    sortPosition = 0,
    bpm = 72,
    timeSignature = com.merkost.metronome.model.TimeSignature.FOUR_FOUR,
    subdivision = com.merkost.metronome.model.Subdivision.QUARTER,
    beats = com.merkost.metronome.model.TimeSignature.FOUR_FOUR.defaultBeats,
    countInEnabled = true,
)
