package com.merkost.metronome.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.merkost.metronome.components.AppBottomSheet
import com.merkost.metronome.components.AppDialog
import com.merkost.metronome.components.PracticeSetRow
import com.merkost.metronome.components.PracticeSetStepRow
import com.merkost.metronome.practiceSets.PracticeSessionStartResult
import com.merkost.metronome.practiceSets.PracticeSet
import com.merkost.metronome.practiceSets.PracticeSetValidationError
import com.merkost.metronome.presets.PracticePreset
import com.merkost.metronome.ui.cornerRadiusLarge
import com.merkost.metronome.ui.horizontalPadding
import com.merkost.metronome.ui.maxContentWidth
import com.merkost.metronome.ui.spacingLarge
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall
import com.merkost.metronome.viewModels.PracticeSetEditorState
import com.merkost.metronome.viewModels.PracticeSetUiEvent
import com.merkost.metronome.viewModels.PracticeSetsUiState
import com.merkost.metronome.viewModels.PracticeSetsViewModel
import com.merkost.metronome.viewModels.MetronomeViewModel
import metronome.shared.generated.resources.*
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject

@Composable
fun PracticeSetsScreen(
    upPress: () -> Unit,
    onStart: () -> Unit,
    onOpenPresets: () -> Unit,
) {
    val viewModel: PracticeSetsViewModel = koinViewModel()
    val metronomeViewModel: MetronomeViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val practiceSessionState by metronomeViewModel.practiceSessionState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var deleteCandidate by remember { mutableStateOf<PracticeSet?>(null) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showPresetPicker by remember { mutableStateOf(false) }
    var pendingStart by remember { mutableStateOf<PracticeSet?>(null) }

    DisposableEffect(Unit) {
        metronomeViewModel.setPresetManagementVisible(true)
        onDispose { metronomeViewModel.setPresetManagementVisible(false) }
    }

    LaunchedEffect(Unit) {
        metronomeViewModel.pauseForPresetManagement()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            val message = when (event) {
                is PracticeSetUiEvent.Saved -> getString(Res.string.practice_set_saved, event.practiceSet.name)
                is PracticeSetUiEvent.Updated -> getString(Res.string.practice_set_updated, event.practiceSet.name)
                is PracticeSetUiEvent.Deleted -> getString(Res.string.practice_set_deleted, event.name)
                is PracticeSetUiEvent.Invalid -> when (event.error) {
                    PracticeSetValidationError.EMPTY_NAME -> getString(Res.string.practice_set_name_required)
                    PracticeSetValidationError.NAME_TOO_LONG -> getString(Res.string.practice_set_name_too_long)
                    PracticeSetValidationError.EMPTY_STEPS -> getString(Res.string.practice_set_steps_required)
                    else -> getString(Res.string.practice_set_storage_failed)
                }
                PracticeSetUiEvent.ActiveSetLocked -> getString(Res.string.practice_set_locked)
                PracticeSetUiEvent.LimitReached -> getString(Res.string.practice_set_limit_reached)
                PracticeSetUiEvent.Conflict -> getString(Res.string.practice_set_conflict)
                PracticeSetUiEvent.StorageFailure -> getString(Res.string.practice_set_storage_failed)
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(Unit) {
        metronomeViewModel.practiceSessionStartResults.collect { result ->
            when (result) {
                is PracticeSessionStartResult.Started -> onStart()
                is PracticeSessionStartResult.MissingPreset -> snackbarHostState.showSnackbar(
                    getString(Res.string.practice_set_start_missing),
                )
                PracticeSessionStartResult.InvalidSet -> snackbarHostState.showSnackbar(
                    getString(Res.string.practice_set_start_invalid),
                )
                PracticeSessionStartResult.PersistenceFailed -> snackbarHostState.showSnackbar(
                    getString(Res.string.practice_set_start_storage_failed),
                )
            }
        }
    }

    val editor = uiState.editor
    PracticeSetsContent(
        uiState = uiState,
        persistenceWarning = practiceSessionState.persistenceWarning,
        snackbarHostState = snackbarHostState,
        onBack = {
            when {
                editor?.hasUnsavedChanges == true -> showDiscardDialog = true
                editor != null -> viewModel.cancelEditing()
                else -> upPress()
            }
        },
        onCreate = viewModel::beginCreate,
        onStart = { practiceSet ->
            if (metronomeViewModel.hasStructuredPracticeConflict()) {
                pendingStart = practiceSet
            } else {
                metronomeViewModel.startPracticeSet(practiceSet)
            }
        },
        onResume = {
            metronomeViewModel.resumePracticeSession()
            onStart()
        },
        onRetryPersistence = metronomeViewModel::retryPracticeSessionPersistence,
        onEdit = viewModel::beginEdit,
        onDelete = { deleteCandidate = it },
        onMove = viewModel::move,
        onReorderingChanged = viewModel::setReordering,
        onNameChanged = viewModel::setName,
        onSave = viewModel::save,
        onAddPreset = { showPresetPicker = true },
        onRemoveStep = viewModel::removeStep,
        onMoveStep = viewModel::moveStep,
        onTargetChange = viewModel::setTarget,
        onEditorReorderingChanged = viewModel::setEditorReordering,
    )

    deleteCandidate?.let { practiceSet ->
        AppDialog(
            title = stringResource(Res.string.practice_set_delete_title, practiceSet.name),
            text = stringResource(Res.string.practice_set_delete_body),
            confirmLabel = stringResource(Res.string.delete),
            dismissLabel = stringResource(Res.string.cancel),
            onConfirm = {
                viewModel.delete(practiceSet.id)
                deleteCandidate = null
            },
            onDismiss = { deleteCandidate = null },
        )
    }

    if (showDiscardDialog) {
        AppDialog(
            title = stringResource(Res.string.practice_set_unsaved_title),
            text = stringResource(Res.string.practice_set_unsaved_body),
            confirmLabel = stringResource(Res.string.discard),
            dismissLabel = stringResource(Res.string.cancel),
            onConfirm = {
                showDiscardDialog = false
                viewModel.cancelEditing()
            },
            onDismiss = { showDiscardDialog = false },
        )
    }

    if (showPresetPicker) {
        PresetPickerSheet(
            presets = uiState.presets,
            onSelect = {
                viewModel.addPreset(it)
                showPresetPicker = false
            },
            onOpenPresets = {
                showPresetPicker = false
                onOpenPresets()
            },
            onDismiss = { showPresetPicker = false },
        )
    }

    pendingStart?.let { practiceSet ->
        AppDialog(
            title = stringResource(Res.string.structured_practice_replace_title),
            text = stringResource(Res.string.structured_practice_replace_body),
            confirmLabel = stringResource(Res.string.structured_practice_replace_confirm),
            dismissLabel = stringResource(Res.string.cancel),
            onConfirm = {
                pendingStart = null
                metronomeViewModel.startPracticeSet(practiceSet)
            },
            onDismiss = { pendingStart = null },
        )
    }
}

@Composable
private fun PracticeSetsContent(
    uiState: PracticeSetsUiState,
    persistenceWarning: Boolean,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onStart: (PracticeSet) -> Unit,
    onResume: () -> Unit,
    onRetryPersistence: () -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (PracticeSet) -> Unit,
    onMove: (String, Int) -> Unit,
    onReorderingChanged: (Boolean) -> Unit,
    onNameChanged: (String) -> Unit,
    onSave: () -> Unit,
    onAddPreset: () -> Unit,
    onRemoveStep: (String) -> Unit,
    onMoveStep: (String, Int) -> Unit,
    onTargetChange: (String, com.merkost.metronome.practiceSets.PracticeStepTarget) -> Unit,
    onEditorReorderingChanged: (Boolean) -> Unit,
) {
    val editor = uiState.editor
    if (editor == null) {
        PracticeSetsLibrary(
            uiState = uiState,
            persistenceWarning = persistenceWarning,
            snackbarHostState = snackbarHostState,
            onBack = onBack,
            onCreate = onCreate,
            onStart = onStart,
            onResume = onResume,
            onRetryPersistence = onRetryPersistence,
            onEdit = onEdit,
            onDelete = onDelete,
            onMove = onMove,
            onReorderingChanged = onReorderingChanged,
        )
    } else {
        PracticeSetEditor(
            editor = editor,
            presets = uiState.presets,
            snackbarHostState = snackbarHostState,
            onBack = onBack,
            onNameChanged = onNameChanged,
            onSave = onSave,
            onAddPreset = onAddPreset,
            onRemoveStep = onRemoveStep,
            onMoveStep = onMoveStep,
            onTargetChange = onTargetChange,
            onReorderingChanged = onEditorReorderingChanged,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PracticeSetsLibrary(
    uiState: PracticeSetsUiState,
    persistenceWarning: Boolean,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onStart: (PracticeSet) -> Unit,
    onResume: () -> Unit,
    onRetryPersistence: () -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (PracticeSet) -> Unit,
    onMove: (String, Int) -> Unit,
    onReorderingChanged: (Boolean) -> Unit,
) {
    val stacked = LocalDensity.current.fontScale >= 1.3f
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(Res.string.practice_sets),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Lucide.ArrowLeft, contentDescription = stringResource(Res.string.back))
                    }
                },
                actions = {
                    if (uiState.sets.isNotEmpty()) {
                        TextButton(onClick = { onReorderingChanged(!uiState.isReordering) }) {
                            Text(stringResource(if (uiState.isReordering) Res.string.done else Res.string.reorder))
                        }
                    }
                },
            )
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(contentPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            if (uiState.sets.isEmpty()) {
                PracticeSetsEmptyState(
                    onCreate = onCreate,
                    modifier = Modifier.widthIn(max = maxContentWidth).padding(horizontal = horizontalPadding),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.widthIn(max = maxContentWidth).fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = horizontalPadding,
                        end = horizontalPadding,
                        top = spacingSmall,
                        bottom = spacingLarge,
                    ),
                    verticalArrangement = Arrangement.spacedBy(spacingSmall),
                ) {
                    if (persistenceWarning) {
                        item(key = "persistence-warning") {
                            Surface(
                                shape = RoundedCornerShape(cornerRadiusLarge),
                                color = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(spacingMedium),
                                    verticalArrangement = Arrangement.spacedBy(spacingSmall),
                                ) {
                                    Text(
                                        text = stringResource(Res.string.practice_session_storage_warning),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    TextButton(
                                        onClick = onRetryPersistence,
                                        modifier = Modifier.align(Alignment.End),
                                    ) {
                                        Text(stringResource(Res.string.retry))
                                    }
                                }
                            }
                        }
                    }
                    item(key = "summary") {
                        if (stacked) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = spacingSmall),
                                verticalArrangement = Arrangement.spacedBy(spacingSmall),
                            ) {
                                Text(
                                    stringResource(Res.string.practice_set_count, uiState.sets.size),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (!uiState.isReordering) {
                                    CreateSetButton(uiState.sets.size < PracticeSet.MAX_SETS, onCreate)
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = spacingSmall),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    stringResource(Res.string.practice_set_count, uiState.sets.size),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f),
                                )
                                if (!uiState.isReordering) {
                                    CreateSetButton(uiState.sets.size < PracticeSet.MAX_SETS, onCreate)
                                }
                            }
                        }
                    }
                    itemsIndexed(uiState.sets, key = { _, set -> set.id }) { index, set ->
                        val presetIds = uiState.presets.mapTo(mutableSetOf()) { it.id }
                        PracticeSetRow(
                            practiceSet = set,
                            missingPresetCount = set.steps.count { it.presetId !in presetIds },
                            isActive = set.id == uiState.activeSourceSetId,
                            isReordering = uiState.isReordering,
                            canMoveUp = index > 0,
                            canMoveDown = index < uiState.sets.lastIndex,
                            onStart = { onStart(set) },
                            onResume = onResume,
                            onEdit = { onEdit(set.id) },
                            onDelete = { onDelete(set) },
                            onMoveUp = { onMove(set.id, index - 1) },
                            onMoveDown = { onMove(set.id, index + 1) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PracticeSetEditor(
    editor: PracticeSetEditorState,
    presets: List<PracticePreset>,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onNameChanged: (String) -> Unit,
    onSave: () -> Unit,
    onAddPreset: () -> Unit,
    onRemoveStep: (String) -> Unit,
    onMoveStep: (String, Int) -> Unit,
    onTargetChange: (String, com.merkost.metronome.practiceSets.PracticeStepTarget) -> Unit,
    onReorderingChanged: (Boolean) -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (editor.sourceId == null) Res.string.practice_set_create else Res.string.practice_set_edit,
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Lucide.ArrowLeft, contentDescription = stringResource(Res.string.back))
                    }
                },
                actions = {
                    TextButton(onClick = onSave) {
                        Text(stringResource(Res.string.save), fontWeight = FontWeight.Bold)
                    }
                },
            )
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(contentPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier.widthIn(max = maxContentWidth).fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    top = spacingMedium,
                    bottom = spacingLarge,
                ),
                verticalArrangement = Arrangement.spacedBy(spacingMedium),
            ) {
                item(key = "name") {
                    OutlinedTextField(
                        value = editor.name,
                        onValueChange = { if (it.length <= PracticeSet.MAX_NAME_LENGTH + 1) onNameChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(Res.string.practice_set_name)) },
                        supportingText = { Text("${editor.name.length}/${PracticeSet.MAX_NAME_LENGTH}") },
                        singleLine = true,
                    )
                }
                if (editor.steps.isNotEmpty()) {
                    item(key = "step-actions") {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(Res.string.practice_set_step_count, editor.steps.size),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(onClick = { onReorderingChanged(!editor.isReordering) }) {
                                Text(stringResource(if (editor.isReordering) Res.string.done else Res.string.reorder))
                            }
                        }
                    }
                }
                itemsIndexed(editor.steps, key = { _, step -> step.id }) { index, step ->
                    PracticeSetStepRow(
                        index = index,
                        step = step,
                        preset = presets.firstOrNull { it.id == step.presetId },
                        isReordering = editor.isReordering,
                        canMoveUp = index > 0,
                        canMoveDown = index < editor.steps.lastIndex,
                        onTargetChange = { onTargetChange(step.id, it) },
                        onRemove = { onRemoveStep(step.id) },
                        onMoveUp = { onMoveStep(step.id, index - 1) },
                        onMoveDown = { onMoveStep(step.id, index + 1) },
                    )
                }
                if (!editor.isReordering) {
                    item(key = "add") {
                        FilledTonalButton(
                            onClick = onAddPreset,
                            enabled = editor.steps.size < PracticeSet.MAX_STEPS,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Lucide.Plus, contentDescription = null)
                            Spacer(Modifier.width(spacingSmall))
                            Text(stringResource(Res.string.add_preset_step))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PracticeSetsEmptyState(onCreate: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = spacingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacingSmall),
    ) {
        Icon(Lucide.Plus, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(
            stringResource(Res.string.practice_set_empty_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            stringResource(Res.string.practice_set_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onCreate) {
            Icon(Lucide.Plus, contentDescription = null)
            Spacer(Modifier.width(spacingSmall))
            Text(stringResource(Res.string.practice_set_create))
        }
    }
}

@Composable
private fun CreateSetButton(enabled: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled) {
        Icon(Lucide.Plus, contentDescription = null)
        Spacer(Modifier.width(spacingSmall))
        Text(stringResource(Res.string.practice_set_create))
    }
}

@Composable
private fun PresetPickerSheet(
    presets: List<PracticePreset>,
    onSelect: (PracticePreset) -> Unit,
    onOpenPresets: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppBottomSheet(title = stringResource(Res.string.choose_preset), onDismiss = onDismiss) {
        if (presets.isEmpty()) {
            Text(
                stringResource(Res.string.practice_set_no_presets),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.padding(spacingSmall))
            Button(onClick = onOpenPresets, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.practice_presets))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                presets.forEach { preset ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(preset) },
                        shape = RoundedCornerShape(cornerRadiusLarge),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                    ) {
                        Column(modifier = Modifier.padding(spacingMedium)) {
                            Text(preset.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                preset.rhythmSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PracticeSetsScreenPreview() {
    MaterialTheme {
        PracticeSetsContent(
            uiState = PracticeSetsUiState(),
            persistenceWarning = false,
            snackbarHostState = SnackbarHostState(),
            onBack = {},
            onCreate = {},
            onStart = {},
            onResume = {},
            onRetryPersistence = {},
            onEdit = {},
            onDelete = {},
            onMove = { _, _ -> },
            onReorderingChanged = {},
            onNameChanged = {},
            onSave = {},
            onAddPreset = {},
            onRemoveStep = {},
            onMoveStep = { _, _ -> },
            onTargetChange = { _, _ -> },
            onEditorReorderingChanged = {},
        )
    }
}
