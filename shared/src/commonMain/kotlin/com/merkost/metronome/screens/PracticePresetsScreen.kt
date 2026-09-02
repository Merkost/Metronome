package com.merkost.metronome.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.merkost.metronome.components.AppIconButton
import com.merkost.metronome.components.AppDialog
import com.merkost.metronome.components.PracticePresetRow
import com.merkost.metronome.components.PracticePresetsEmptyState
import com.merkost.metronome.components.PresetNameDialog
import com.merkost.metronome.model.MetronomeState
import com.merkost.metronome.presets.ActivePresetState
import com.merkost.metronome.presets.PracticePreset
import com.merkost.metronome.presets.PracticePresetDraft
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.horizontalPadding
import com.merkost.metronome.ui.maxContentWidth
import com.merkost.metronome.ui.spacingLarge
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall
import com.merkost.metronome.viewModels.MetronomeViewModel
import com.merkost.metronome.viewModels.PracticePresetsUiState
import com.merkost.metronome.viewModels.PracticePresetsViewModel
import com.merkost.metronome.viewModels.PresetUiEvent
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.back
import metronome.shared.generated.resources.cancel
import metronome.shared.generated.resources.create_preset
import metronome.shared.generated.resources.delete
import metronome.shared.generated.resources.delete_preset
import metronome.shared.generated.resources.delete_preset_message
import metronome.shared.generated.resources.done
import metronome.shared.generated.resources.duplicate_preset
import metronome.shared.generated.resources.open_practice_sets
import metronome.shared.generated.resources.preset_count
import metronome.shared.generated.resources.preset_copy_name
import metronome.shared.generated.resources.preset_deleted
import metronome.shared.generated.resources.preset_duplicated
import metronome.shared.generated.resources.preset_invalid
import metronome.shared.generated.resources.preset_in_use
import metronome.shared.generated.resources.preset_in_use_sets
import metronome.shared.generated.resources.preset_in_use_sets_more
import metronome.shared.generated.resources.preset_in_use_title
import metronome.shared.generated.resources.preset_limit_reached
import metronome.shared.generated.resources.preset_migration_failed
import metronome.shared.generated.resources.preset_name_required
import metronome.shared.generated.resources.preset_name_too_long
import metronome.shared.generated.resources.preset_saved
import metronome.shared.generated.resources.preset_storage_failed
import metronome.shared.generated.resources.preset_updated
import metronome.shared.generated.resources.practice_presets
import metronome.shared.generated.resources.rename_preset
import metronome.shared.generated.resources.reorder
import metronome.shared.generated.resources.retry
import metronome.shared.generated.resources.save_current_setup
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.abs

private sealed interface PresetEditor {
    val draft: PracticePresetDraft
    data class Create(override val draft: PracticePresetDraft) : PresetEditor {
    }
    data class Rename(val preset: PracticePreset) : PresetEditor {
        override val draft = preset.toDraft()
    }
    data class Duplicate(val preset: PracticePreset) : PresetEditor {
        override val draft = preset.toDraft()
    }
}

@Composable
fun PracticePresetsScreen(
    upPress: () -> Unit,
    onOpenPracticeSets: () -> Unit,
) {
    val metronomeViewModel: MetronomeViewModel = koinInject()
    val presetsViewModel: PracticePresetsViewModel = koinViewModel()
    val uiState by presetsViewModel.uiState.collectAsState()
    val metronomeState by metronomeViewModel.metronomeState.collectAsState()
    val activeState by metronomeViewModel.activePresetState.collectAsState()
    val countInEnabled by metronomeViewModel.countInEnabled.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var editor by remember { mutableStateOf<PresetEditor?>(null) }
    var deleteCandidate by remember { mutableStateOf<PracticePreset?>(null) }
    var editorError by remember { mutableStateOf<String?>(null) }
    var blockingSetNames by remember { mutableStateOf<List<String>?>(null) }

    DisposableEffect(Unit) {
        metronomeViewModel.setPresetManagementVisible(true)
        onDispose { metronomeViewModel.setPresetManagementVisible(false) }
    }

    val limitMessage = stringResource(Res.string.preset_limit_reached)
    val storageMessage = stringResource(Res.string.preset_storage_failed)
    val requiredMessage = stringResource(Res.string.preset_name_required)
    val tooLongMessage = stringResource(Res.string.preset_name_too_long)
    val invalidMessage = stringResource(Res.string.preset_invalid)

    LaunchedEffect(Unit) {
        metronomeViewModel.pauseForPresetManagement()
        presetsViewModel.events.collect { event ->
            val message = when (event) {
                is PresetUiEvent.Saved -> {
                    metronomeViewModel.onPresetStored(event.preset)
                    getString(Res.string.preset_saved, event.preset.name)
                }
                is PresetUiEvent.Updated -> {
                    metronomeViewModel.onPresetStored(event.preset)
                    getString(Res.string.preset_updated, event.preset.name)
                }
                is PresetUiEvent.Duplicated -> getString(Res.string.preset_duplicated, event.preset.name)
                is PresetUiEvent.Deleted -> {
                    metronomeViewModel.onPresetDeleted(event.id)
                    getString(Res.string.preset_deleted, event.name)
                }
                is PresetUiEvent.InUse -> {
                    blockingSetNames = event.setNames
                    null
                }
                PresetUiEvent.LimitReached -> limitMessage
                is PresetUiEvent.Invalid -> when (event.error) {
                    com.merkost.metronome.presets.PresetValidationError.EMPTY_NAME -> requiredMessage
                    com.merkost.metronome.presets.PresetValidationError.NAME_TOO_LONG -> tooLongMessage
                    else -> invalidMessage
                }
                PresetUiEvent.StorageFailure -> storageMessage
            }
            message?.let { snackbarHostState.showSnackbar(it) }
        }
    }

    PracticePresetsContent(
        uiState = uiState,
        activeState = activeState,
        snackbarHostState = snackbarHostState,
        onBack = upPress,
        onCreate = {
            editorError = null
            editor = PresetEditor.Create(metronomeState.toPresetDraft(countInEnabled))
        },
        onApply = {
            metronomeViewModel.applyPracticePreset(it)
            upPress()
        },
        onToggleFavourite = presetsViewModel::toggleFavourite,
        onRename = {
            editorError = null
            editor = PresetEditor.Rename(it)
        },
        onDuplicate = {
            editorError = null
            editor = PresetEditor.Duplicate(it)
        },
        onDelete = { deleteCandidate = it },
        onMove = presetsViewModel::move,
        onReorderingChanged = presetsViewModel::setReordering,
        onRetryMigration = presetsViewModel::retryMigration,
    )

    editor?.let { currentEditor ->
        PresetNameDialog(
            title = stringResource(
                when (currentEditor) {
                    is PresetEditor.Create -> Res.string.create_preset
                    is PresetEditor.Rename -> Res.string.rename_preset
                    is PresetEditor.Duplicate -> Res.string.duplicate_preset
                }
            ),
            initialName = when (currentEditor) {
                is PresetEditor.Create -> currentEditor.draft.name
                is PresetEditor.Rename -> currentEditor.preset.name
                is PresetEditor.Duplicate -> stringResource(
                    Res.string.preset_copy_name,
                    currentEditor.preset.name,
                )
            },
            summary = currentEditor.draft.run {
                "$bpm BPM · ${timeSignature.label}" + if (subdivision.label.isNotBlank()) " · ${subdivision.label}" else ""
            },
            error = editorError,
            onConfirm = { name ->
                val normalized = name.trim()
                editorError = when {
                    normalized.isEmpty() -> requiredMessage
                    normalized.length > PracticePreset.MAX_NAME_LENGTH -> tooLongMessage
                    else -> null
                }
                if (editorError == null) {
                    when (currentEditor) {
                        is PresetEditor.Create -> presetsViewModel.create(currentEditor.draft.copy(name = normalized))
                        is PresetEditor.Rename -> presetsViewModel.update(currentEditor.preset.id, currentEditor.draft.copy(name = normalized))
                        is PresetEditor.Duplicate -> presetsViewModel.duplicate(currentEditor.preset.id, normalized)
                    }
                    editor = null
                }
            },
            onDismiss = { editor = null },
        )
    }

    deleteCandidate?.let { preset ->
        AppDialog(
            title = stringResource(Res.string.delete_preset),
            text = stringResource(Res.string.delete_preset_message, preset.name),
            confirmLabel = stringResource(Res.string.delete),
            dismissLabel = stringResource(Res.string.cancel),
            onConfirm = {
                presetsViewModel.delete(preset.id)
                deleteCandidate = null
            },
            onDismiss = { deleteCandidate = null },
        )
    }

    blockingSetNames?.let { names ->
        val visibleNames = names.take(3).joinToString(", ")
        val remainingCount = (names.size - 3).coerceAtLeast(0)
        AppDialog(
            title = stringResource(Res.string.preset_in_use_title),
            text = if (remainingCount > 0) {
                stringResource(Res.string.preset_in_use_sets_more, visibleNames, remainingCount)
            } else {
                stringResource(Res.string.preset_in_use_sets, visibleNames)
            },
            confirmLabel = stringResource(Res.string.open_practice_sets),
            dismissLabel = stringResource(Res.string.cancel),
            onConfirm = {
                blockingSetNames = null
                onOpenPracticeSets()
            },
            onDismiss = { blockingSetNames = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PracticePresetsContent(
    uiState: PracticePresetsUiState,
    activeState: ActivePresetState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onApply: (PracticePreset) -> Unit,
    onToggleFavourite: (String) -> Unit,
    onRename: (PracticePreset) -> Unit,
    onDuplicate: (PracticePreset) -> Unit,
    onDelete: (PracticePreset) -> Unit,
    onMove: (String, Int) -> Unit,
    onReorderingChanged: (Boolean) -> Unit,
    onRetryMigration: () -> Unit,
) {
    val useStackedSummary = LocalDensity.current.fontScale >= 1.3f
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(Res.string.practice_presets),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    AppIconButton(onClick = onBack) {
                        Icon(Lucide.ArrowLeft, contentDescription = stringResource(Res.string.back))
                    }
                },
                actions = {
                    if (uiState.presets.isNotEmpty()) {
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
            AnimatedContent(
                targetState = uiState.presets.isEmpty() && !uiState.migrationFailed,
                transitionSpec = { AppAnimations.fadeThroughRoute },
                label = "presetsLibraryState",
            ) { isEmpty ->
            if (isEmpty) {
                PracticePresetsEmptyState(
                    onCreate = onCreate,
                    modifier = Modifier.widthIn(max = maxContentWidth).padding(horizontal = horizontalPadding),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.widthIn(max = maxContentWidth).fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = horizontalPadding,
                        end = horizontalPadding,
                        top = spacingSmall,
                        bottom = spacingLarge,
                    ),
                    verticalArrangement = Arrangement.spacedBy(spacingSmall),
                ) {
                    if (uiState.migrationFailed) {
                        item(key = "migration") {
                            Column(
                                modifier = Modifier.animateItem().fillMaxWidth().padding(vertical = spacingSmall),
                                verticalArrangement = Arrangement.spacedBy(spacingSmall),
                            ) {
                                Text(
                                    stringResource(Res.string.preset_migration_failed),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                )
                                TextButton(onClick = onRetryMigration) {
                                    Text(stringResource(Res.string.retry))
                                }
                            }
                        }
                    }
                    item(key = "summary") {
                        if (useStackedSummary) {
                            Column(
                                modifier = Modifier.animateItem().fillMaxWidth().padding(vertical = spacingSmall),
                                verticalArrangement = Arrangement.spacedBy(spacingSmall),
                            ) {
                                Text(
                                    stringResource(Res.string.preset_count, uiState.presets.size),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (!uiState.isReordering) {
                                    Button(
                                        onClick = onCreate,
                                        enabled = uiState.presets.size < PracticePreset.MAX_PRESETS,
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Icon(Lucide.Plus, contentDescription = null)
                                        Spacer(Modifier.width(spacingSmall))
                                        Text(stringResource(Res.string.save_current_setup))
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.animateItem().fillMaxWidth().padding(vertical = spacingSmall),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    stringResource(Res.string.preset_count, uiState.presets.size),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f),
                                )
                                if (!uiState.isReordering) {
                                    Button(onClick = onCreate, enabled = uiState.presets.size < PracticePreset.MAX_PRESETS) {
                                        Icon(Lucide.Plus, contentDescription = null)
                                        Spacer(Modifier.width(spacingSmall))
                                        Text(stringResource(Res.string.save_current_setup))
                                    }
                                }
                            }
                        }
                    }
                    if (uiState.presets.size >= PracticePreset.MAX_PRESETS) {
                        item(key = "limit") {
                            Text(
                                text = stringResource(Res.string.preset_limit_reached),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.animateItem().padding(bottom = spacingSmall),
                            )
                        }
                    }
                    itemsIndexed(uiState.presets, key = { _, preset -> preset.id }) { index, preset ->
                        val dragThreshold = with(LocalDensity.current) { 44.dp.toPx() }
                        var accumulatedDrag by remember(preset.id) { mutableFloatStateOf(0f) }
                        PracticePresetRow(
                            preset = preset,
                            isActive = activeState.active?.id == preset.id,
                            isEdited = activeState.active?.id == preset.id && activeState.isEdited,
                            isReordering = uiState.isReordering,
                            canMoveUp = index > 0 && uiState.presets[index - 1].isFavourite == preset.isFavourite,
                            canMoveDown = index < uiState.presets.lastIndex && uiState.presets[index + 1].isFavourite == preset.isFavourite,
                            onApply = { onApply(preset) },
                            onToggleFavourite = { onToggleFavourite(preset.id) },
                            onRename = { onRename(preset) },
                            onDuplicate = { onDuplicate(preset) },
                            onDelete = { onDelete(preset) },
                            onMoveUp = { onMove(preset.id, index - 1) },
                            onMoveDown = { onMove(preset.id, index + 1) },
                            modifier = Modifier.animateItem().pointerInput(uiState.isReordering, preset.id, index) {
                                if (!uiState.isReordering) return@pointerInput
                                detectDragGesturesAfterLongPress(
                                    onDragEnd = { accumulatedDrag = 0f },
                                    onDragCancel = { accumulatedDrag = 0f },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        accumulatedDrag += dragAmount.y
                                        if (abs(accumulatedDrag) >= dragThreshold) {
                                            val destination = if (accumulatedDrag < 0f) index - 1 else index + 1
                                            if (
                                                destination in uiState.presets.indices &&
                                                uiState.presets[destination].isFavourite == preset.isFavourite
                                            ) {
                                                onMove(preset.id, destination)
                                            }
                                            accumulatedDrag = 0f
                                        }
                                    },
                                )
                            },
                        )
                    }
                }
            }
            }
        }
    }
}

private fun MetronomeState.toPresetDraft(countInEnabled: Boolean): PracticePresetDraft = PracticePresetDraft(
    name = "$rhythm BPM · ${timeSignature.label}",
    bpm = rhythm,
    timeSignature = timeSignature,
    subdivision = subdivision,
    beats = beats,
    countInEnabled = countInEnabled,
)

@Preview
@Composable
private fun PracticePresetsScreenPreview() {
    MaterialTheme {
        PracticePresetsContent(
            uiState = PracticePresetsUiState(),
            activeState = ActivePresetState(),
            snackbarHostState = SnackbarHostState(),
            onBack = {},
            onCreate = {},
            onApply = {},
            onToggleFavourite = {},
            onRename = {},
            onDuplicate = {},
            onDelete = {},
            onMove = { _, _ -> },
            onReorderingChanged = {},
            onRetryMigration = {},
        )
    }
}
