package com.merkost.metronome.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronUp
import com.composables.icons.lucide.Copy
import com.composables.icons.lucide.EllipsisVertical
import com.composables.icons.lucide.GripVertical
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.Trash2
import com.merkost.metronome.presets.PracticePreset
import com.merkost.metronome.ui.pressableSurface
import com.merkost.metronome.ui.cornerRadiusLarge
import com.merkost.metronome.ui.cornerRadiusXLarge
import com.merkost.metronome.ui.minimumTouchTargetSize
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.active
import metronome.shared.generated.resources.cancel
import metronome.shared.generated.resources.count_in_off
import metronome.shared.generated.resources.count_in_on
import metronome.shared.generated.resources.delete
import metronome.shared.generated.resources.duplicate
import metronome.shared.generated.resources.edited
import metronome.shared.generated.resources.favourite_preset
import metronome.shared.generated.resources.move_down
import metronome.shared.generated.resources.move_up
import metronome.shared.generated.resources.preset_actions
import metronome.shared.generated.resources.preset_name
import metronome.shared.generated.resources.preset_name_required
import metronome.shared.generated.resources.preset_name_too_long
import metronome.shared.generated.resources.preset_empty_body
import metronome.shared.generated.resources.preset_empty_title
import metronome.shared.generated.resources.rename
import metronome.shared.generated.resources.save
import metronome.shared.generated.resources.save_as_new
import metronome.shared.generated.resources.save_current_setup
import metronome.shared.generated.resources.update_preset
import metronome.shared.generated.resources.unfavourite_preset
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun PracticePresetRow(
    preset: PracticePreset,
    isActive: Boolean,
    isEdited: Boolean,
    isReordering: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onApply: () -> Unit,
    onToggleFavourite: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var actionsExpanded by remember { mutableStateOf(false) }
    val surfaceColor = if (isActive) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadiusLarge),
        color = surfaceColor,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pressableSurface(onClick = onApply, enabled = !isReordering)
                .padding(horizontal = spacingSmall, vertical = spacingSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isReordering) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = preset.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (isActive) {
                        Spacer(Modifier.width(spacingSmall))
                        Text(
                            text = if (isEdited) {
                                "${stringResource(Res.string.active)} · ${stringResource(Res.string.edited)}"
                            } else {
                                stringResource(Res.string.active)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Text(
                    text = "${preset.rhythmSummary} · ${stringResource(if (preset.countInEnabled) Res.string.count_in_on else Res.string.count_in_off)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (isReordering) {
                PresetActionIcon(
                    icon = Lucide.ChevronUp,
                    description = stringResource(Res.string.move_up, preset.name),
                    enabled = canMoveUp,
                    onClick = onMoveUp,
                )
                PresetActionIcon(
                    icon = Lucide.ChevronDown,
                    description = stringResource(Res.string.move_down, preset.name),
                    enabled = canMoveDown,
                    onClick = onMoveDown,
                )
            } else {
                PresetActionIcon(
                    icon = Lucide.Star,
                    description = stringResource(
                        if (preset.isFavourite) Res.string.unfavourite_preset else Res.string.favourite_preset,
                        preset.name,
                    ),
                    tint = if (preset.isFavourite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onToggleFavourite,
                )
                Box {
                    PresetActionIcon(
                        icon = Lucide.EllipsisVertical,
                        description = stringResource(Res.string.preset_actions, preset.name),
                        onClick = { actionsExpanded = true },
                    )
                    DropdownMenu(
                        expanded = actionsExpanded,
                        onDismissRequest = { actionsExpanded = false },
                    ) {
                        PresetMenuItem(Lucide.Pencil, Res.string.rename) {
                            actionsExpanded = false
                            onRename()
                        }
                        PresetMenuItem(Lucide.Copy, Res.string.duplicate) {
                            actionsExpanded = false
                            onDuplicate()
                        }
                        HorizontalDivider()
                        PresetMenuItem(Lucide.Trash2, Res.string.delete) {
                            actionsExpanded = false
                            onDelete()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PresetNameDialog(
    title: String,
    initialName: String,
    summary: String,
    error: String?,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember(initialName) {
        mutableStateOf(
            TextFieldValue(
                text = initialName,
                selection = TextRange(0, initialName.length),
            ),
        )
    }
    val focusRequester = remember { FocusRequester() }
    val normalizedName = name.text.trim()
    val canSave = normalizedName.isNotEmpty() && normalizedName.length <= PracticePreset.MAX_NAME_LENGTH
    val validationMessage = when {
        normalizedName.isEmpty() -> stringResource(Res.string.preset_name_required)
        normalizedName.length > PracticePreset.MAX_NAME_LENGTH -> stringResource(Res.string.preset_name_too_long)
        else -> error
    }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(cornerRadiusXLarge),
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        if (it.text.length <= PracticePreset.MAX_NAME_LENGTH + 1) {
                            name = it
                        }
                    },
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    label = { Text(stringResource(Res.string.preset_name)) },
                    supportingText = {
                        Text(validationMessage ?: "${name.text.length}/${PracticePreset.MAX_NAME_LENGTH}")
                    },
                    isError = validationMessage != null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { if (canSave) onConfirm(name.text) }),
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(enabled = canSave, onClick = { onConfirm(name.text) }) {
                Text(stringResource(Res.string.save), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        },
    )
}

@Composable
fun PresetSaveChoiceDialog(
    presetName: String,
    message: String,
    onUpdate: () -> Unit,
    onSaveAsNew: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(cornerRadiusXLarge),
        title = { Text(presetName, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onUpdate) {
                Text(stringResource(Res.string.update_preset), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.cancel))
                }
                TextButton(onClick = onSaveAsNew) {
                    Text(stringResource(Res.string.save_as_new))
                }
            }
        },
    )
}

@Composable
fun PracticePresetsEmptyState(onCreate: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 56.dp, horizontal = spacingMedium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacingSmall),
    ) {
        Icon(
            imageVector = Lucide.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp),
        )
        Text(
            text = stringResource(Res.string.preset_empty_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(Res.string.preset_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(spacingSmall))
        FilledTonalButton(onClick = onCreate) {
            Icon(Lucide.Plus, contentDescription = null)
            Spacer(Modifier.width(spacingSmall))
            Text(stringResource(Res.string.save_current_setup))
        }
    }
}

@Composable
private fun PresetActionIcon(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(minimumTouchTargetSize).semantics { contentDescription = description },
    ) {
        Icon(icon, contentDescription = null, tint = tint)
    }
}

@Composable
private fun PresetMenuItem(icon: ImageVector, label: StringResource, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(stringResource(label)) },
        onClick = onClick,
        leadingIcon = { Icon(icon, contentDescription = null) },
    )
}

@Preview
@Composable
private fun PracticePresetRowPreview() {
    MaterialTheme {
        PracticePresetRow(
            preset = PracticePreset(
                id = "warm-up",
                name = "Warm-up",
                createdAtEpochMillis = 0,
                lastUsedAtEpochMillis = null,
                isFavourite = true,
                sortPosition = 0,
                bpm = 96,
                timeSignature = com.merkost.metronome.model.TimeSignature.FOUR_FOUR,
                subdivision = com.merkost.metronome.model.Subdivision.EIGHTH,
                beats = com.merkost.metronome.model.TimeSignature.FOUR_FOUR.defaultBeats,
                countInEnabled = true,
            ),
            isActive = true,
            isEdited = false,
            isReordering = false,
            canMoveUp = false,
            canMoveDown = true,
            onApply = {},
            onToggleFavourite = {},
            onRename = {},
            onDuplicate = {},
            onDelete = {},
            onMoveUp = {},
            onMoveDown = {},
        )
    }
}

@Preview
@Composable
private fun PresetNameDialogPreview() {
    MaterialTheme {
        PresetNameDialog(
            title = "Create preset",
            initialName = "Warm-up",
            summary = "96 BPM · 4/4 · Eighth",
            error = null,
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun PresetSaveChoiceDialogPreview() {
    MaterialTheme {
        PresetSaveChoiceDialog(
            presetName = "Warm-up",
            message = "This preset has changed.",
            onUpdate = {},
            onSaveAsNew = {},
            onDismiss = {},
        )
    }
}
