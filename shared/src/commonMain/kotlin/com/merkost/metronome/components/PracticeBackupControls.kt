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
import com.merkost.metronome.platform.BackupFileActions
import com.merkost.metronome.ui.spacingSmall
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Composable
fun PracticeBackupControls() {
    val repository: PracticeBackupRepository = koinInject()
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
        Text("Your setups", style = MaterialTheme.typography.titleMedium)
        Text("Back up your presets and practice sets. Import adds setups and keeps your current ones.", style = MaterialTheme.typography.bodySmall)
        BackupFileActions(
            enabled = !busy,
            export = repository::export,
            onImport = { raw ->
                scope.launch {
                    busy = true
                    try {
                        val result = repository.import(raw)
                        status = "Added ${result.presetsAdded} presets and ${result.setsAdded} sets."
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (failure: Exception) {
                        status = failure.message ?: "Couldn't import the backup. Your current setups are kept."
                    } finally {
                        busy = false
                    }
                }
            },
            onMessage = { status = it },
        )
        status?.let { BackupStatus(it) }
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
