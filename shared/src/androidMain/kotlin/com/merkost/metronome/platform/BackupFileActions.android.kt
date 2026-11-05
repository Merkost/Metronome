package com.merkost.metronome.platform

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.merkost.metronome.backup.MaximumBackupCharacters
import com.merkost.metronome.backup.backupText
import com.merkost.metronome.backup.requireBackup
import com.merkost.metronome.model.UiText
import com.merkost.metronome.components.PracticeBackupActions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.backup_export_failed_location
import metronome.shared.generated.resources.backup_exported
import metronome.shared.generated.resources.backup_open_failed
import metronome.shared.generated.resources.backup_prepare_failed
import metronome.shared.generated.resources.backup_too_large

@Composable
actual fun BackupFileActions(enabled: Boolean, export: suspend () -> String, onImport: (String) -> Unit, onMessage: (UiText) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val exportAction by rememberUpdatedState(export)
    val importAction by rememberUpdatedState(onImport)
    val messageAction by rememberUpdatedState(onMessage)
    var contents by remember { mutableStateOf<String?>(null) }
    var working by remember { mutableStateOf(false) }
    val writer = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        val prepared = contents
        contents = null
        if (uri == null) working = false else scope.launch {
            working = true
            try {
                val raw = prepared ?: exportAction()
                withContext(Dispatchers.IO) {
                    requireNotNull(context.contentResolver.openOutputStream(uri)).bufferedWriter().use { it.write(raw) }
                }
                messageAction(UiText.of(Res.string.backup_exported))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                messageAction(UiText.of(Res.string.backup_export_failed_location))
            } finally { working = false }
        }
    }
    val reader = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) working = false else scope.launch {
            try {
                val raw = withContext(Dispatchers.IO) {
                    requireNotNull(context.contentResolver.openInputStream(uri)).bufferedReader().use { input ->
                        val text = StringBuilder()
                        val buffer = CharArray(4096)
                        while (true) {
                            val count = input.read(buffer)
                            if (count < 0) break
                            requireBackup(text.length + count <= MaximumBackupCharacters, Res.string.backup_too_large)
                            text.append(buffer, 0, count)
                        }
                        text.toString()
                    }
                }
                importAction(raw)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                messageAction(failure.backupText(Res.string.backup_open_failed))
            } finally { working = false }
        }
    }
    PracticeBackupActions(
        enabled = enabled && !working,
        onExport = {
            working = true
            scope.launch {
                try {
                    contents = export()
                    writer.launch("Metronome-backup.txt")
                } catch (cancelled: CancellationException) {
                    working = false
                    throw cancelled
                } catch (_: Exception) {
                    working = false
                    messageAction(UiText.of(Res.string.backup_prepare_failed))
                }
            }
        },
        onImport = { working = true; reader.launch(arrayOf("text/plain", "application/octet-stream")) },
    )
}
