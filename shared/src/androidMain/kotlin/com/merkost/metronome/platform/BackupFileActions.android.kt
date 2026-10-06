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
import com.merkost.metronome.components.PracticeBackupActions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun BackupFileActions(enabled: Boolean, export: suspend () -> String, onImport: (String) -> Unit, onMessage: (String) -> Unit) {
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
                messageAction("Backup exported.")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                messageAction("Couldn't export this backup. Try another location.")
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
                            require(text.length + count <= MaximumBackupCharacters) { "This backup is too large." }
                            text.append(buffer, 0, count)
                        }
                        text.toString()
                    }
                }
                importAction(raw)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                messageAction(failure.message ?: "Couldn't open this backup.")
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
                    messageAction("Couldn't prepare this backup.")
                }
            }
        },
        onImport = { working = true; reader.launch(arrayOf("text/plain", "application/octet-stream")) },
    )
}
