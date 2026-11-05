package com.merkost.metronome.platform

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.merkost.metronome.backup.BackupException
import com.merkost.metronome.backup.MaximumBackupCharacters
import com.merkost.metronome.backup.backupText
import com.merkost.metronome.backup.requireBackup
import com.merkost.metronome.model.UiText
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
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.backup_choose_file
import metronome.shared.generated.resources.backup_export_dialog_title
import metronome.shared.generated.resources.backup_export_failed
import metronome.shared.generated.resources.backup_exported
import metronome.shared.generated.resources.backup_import_dialog_title
import metronome.shared.generated.resources.backup_not_utf8
import metronome.shared.generated.resources.backup_open_failed
import metronome.shared.generated.resources.backup_too_large
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun BackupFileActions(enabled: Boolean, export: suspend () -> String, onImport: (String) -> Unit, onMessage: (UiText) -> Unit) {
    val scope = rememberCoroutineScope()
    val exportAction by rememberUpdatedState(export)
    val importAction by rememberUpdatedState(onImport)
    val messageAction by rememberUpdatedState(onMessage)
    var working by remember { mutableStateOf(false) }
    val exportTitle = stringResource(Res.string.backup_export_dialog_title)
    val importTitle = stringResource(Res.string.backup_import_dialog_title)
    PracticeBackupActions(
        enabled = enabled && !working,
        onExport = {
            if (!working) {
                working = true
                scope.launch {
                    try {
                        val raw = exportAction()
                        val file = chooseBackupFile(FileDialog.SAVE, exportTitle)
                        if (file != null) {
                            withContext(Dispatchers.IO) { DesktopBackupFiles.write(file, raw) }
                            messageAction(UiText.of(Res.string.backup_exported))
                        }
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (failure: Exception) {
                        messageAction(failure.backupText(Res.string.backup_export_failed))
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
                        val file = chooseBackupFile(FileDialog.LOAD, importTitle)
                        if (file != null) importAction(withContext(Dispatchers.IO) { DesktopBackupFiles.read(file) })
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (failure: Exception) {
                        messageAction(failure.backupText(Res.string.backup_open_failed))
                    } finally {
                        working = false
                    }
                }
            }
        },
    )
}

private suspend fun chooseBackupFile(mode: Int, title: String): File? = withContext(Dispatchers.Swing) {
    val owner = generateSequence(KeyboardFocusManager.getCurrentKeyboardFocusManager().activeWindow) { it.owner }
        .firstOrNull { it is Frame || it is Dialog }
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
        requireBackup(file.isFile, Res.string.backup_choose_file)
        val maximumBytes = MaximumBackupCharacters * 4
        requireBackup(Files.size(file.toPath()) <= maximumBytes.toLong(), Res.string.backup_too_large)
        val bytes = Files.newInputStream(file.toPath()).use { it.readNBytes(maximumBytes + 1) }
        requireBackup(bytes.size <= maximumBytes, Res.string.backup_too_large)
        val text = try {
            StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes)).toString()
        } catch (failure: CharacterCodingException) {
            throw BackupException(UiText.of(Res.string.backup_not_utf8)).apply { initCause(failure) }
        }
        requireBackup(text.length <= MaximumBackupCharacters, Res.string.backup_too_large)
        return text
    }

    fun write(file: File, raw: String) {
        requireBackup(raw.length <= MaximumBackupCharacters, Res.string.backup_too_large)
        Files.writeString(file.toPath(), raw, StandardCharsets.UTF_8)
    }
}

@Preview
@Composable
private fun DesktopBackupFileActionsPreview() {
    MaterialTheme { BackupFileActions(false, { "" }, {}, {}) }
}
