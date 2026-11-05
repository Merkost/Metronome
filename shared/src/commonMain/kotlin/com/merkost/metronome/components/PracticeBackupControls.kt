package com.merkost.metronome.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.merkost.metronome.backup.PracticeBackupRepository
import com.merkost.metronome.backup.backupText
import com.merkost.metronome.model.UiText
import com.merkost.metronome.model.asString
import com.merkost.metronome.platform.BackupFileActions
import com.merkost.metronome.ui.spacingSmall
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.backup_controls_body
import metronome.shared.generated.resources.backup_controls_title
import metronome.shared.generated.resources.backup_import_added
import metronome.shared.generated.resources.backup_import_failed
import metronome.shared.generated.resources.backup_presets_count
import metronome.shared.generated.resources.backup_sets_count
import org.jetbrains.compose.resources.stringResource

@Composable
fun PracticeBackupControls() {
    val repository: PracticeBackupRepository = koinInject()
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf<UiText?>(null) }
    var busy by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
        Text(stringResource(Res.string.backup_controls_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(Res.string.backup_controls_body), style = MaterialTheme.typography.bodySmall)
        BackupFileActions(
            enabled = !busy,
            export = repository::export,
            onImport = { raw ->
                scope.launch {
                    busy = true
                    try {
                        val result = repository.import(raw)
                        status = UiText.of(
                            Res.string.backup_import_added,
                            UiText.quantity(Res.plurals.backup_presets_count, result.presetsAdded),
                            UiText.quantity(Res.plurals.backup_sets_count, result.setsAdded),
                        )
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (failure: Exception) {
                        status = failure.backupText(Res.string.backup_import_failed)
                    } finally {
                        busy = false
                    }
                }
            },
            onMessage = { status = it },
        )
        status?.let { BackupStatus(it.asString()) }
    }
}

@Composable
private fun BackupStatus(message: String) {
    Text(message, modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Preview
@Composable
private fun BackupStatusPreview() {
    MaterialTheme { BackupStatus("Added 3 presets and 1 set.") }
}
