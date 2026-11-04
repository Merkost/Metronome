package com.merkost.metronome.platform

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.merkost.metronome.backup.MaximumBackupCharacters
import com.merkost.metronome.components.PracticeBackupActions
import java.awt.Dialog
import java.awt.FileDialog
import java.awt.Frame
import java.awt.KeyboardFocusManager
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
actual fun BackupFileActions(enabled: Boolean, export: suspend () -> String, onImport: (String) -> Unit, onMessage: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    val exportAction by rememberUpdatedState(export)
    val importAction by rememberUpdatedState(onImport)
    val messageAction by rememberUpdatedState(onMessage)
    var working by remember { mutableStateOf(false) }
    PracticeBackupActions(
        enabled = enabled && !working,
        onExport = {
            if (!working) {
                working = true
                scope.launch {
                    try {
                        val raw = exportAction()
                        val file = chooseBackupFile(FileDialog.SAVE)
                        if (file != null) {
                            withContext(Dispatchers.IO) { DesktopBackupFiles.write(file, raw) }
                            messageAction("Backup exported.")
                        }
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (failure: Exception) {
                        messageAction(failure.message ?: "Couldn't export this backup.")
                    } finally {
                        working = false
                    }
                }
            }
        },
        onImport = {
            if (!working) {
                working = true
                scope.launch {
                    try {
                        val file = chooseBackupFile(FileDialog.LOAD)
                        if (file != null) importAction(withContext(Dispatchers.IO) { DesktopBackupFiles.read(file) })
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (failure: Exception) {
                        messageAction(failure.message ?: "Couldn't open this backup.")
                    } finally {
                        working = false
                    }
                }
            }
        },
    )
}

private suspend fun chooseBackupFile(mode: Int): File? = withContext(Dispatchers.Swing) {
    val owner = generateSequence(KeyboardFocusManager.getCurrentKeyboardFocusManager().activeWindow) { it.owner }
        .firstOrNull { it is Frame || it is Dialog }
    val title = if (mode == FileDialog.SAVE) "Export Metronome backup" else "Import Metronome backup"
    val dialog = when (owner) {
        is Dialog -> FileDialog(owner, title, mode)
        is Frame -> FileDialog(owner, title, mode)
        else -> FileDialog(null as Frame?, title, mode)
    }
    try {
        if (mode == FileDialog.SAVE) dialog.file = "Metronome-backup.json"
        dialog.isMultipleMode = false
        dialog.isVisible = true
        dialog.file?.let { File(dialog.directory, it) }
    } finally {
        dialog.dispose()
    }
}

internal object DesktopBackupFiles {
    fun read(file: File): String {
        require(file.isFile) { "Choose a backup file." }
        val maximumBytes = MaximumBackupCharacters * 4
        require(Files.size(file.toPath()) <= maximumBytes.toLong()) { "This backup is too large." }
        val bytes = Files.newInputStream(file.toPath()).use { it.readNBytes(maximumBytes + 1) }
        require(bytes.size <= maximumBytes) { "This backup is too large." }
        val text = try {
            StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes)).toString()
        } catch (failure: CharacterCodingException) {
            throw IllegalArgumentException("Choose a UTF-8 text backup.", failure)
        }
        require(text.length <= MaximumBackupCharacters) { "This backup is too large." }
        return text
    }

    fun write(file: File, raw: String) {
        require(raw.length <= MaximumBackupCharacters) { "This backup is too large." }
        Files.writeString(file.toPath(), raw, StandardCharsets.UTF_8)
    }
}

@Preview
@Composable
private fun DesktopBackupFileActionsPreview() {
    MaterialTheme { BackupFileActions(false, { "" }, {}, {}) }
}
