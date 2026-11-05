package com.merkost.metronome.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.merkost.metronome.components.PracticeBackupActions
import com.merkost.metronome.model.UiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.backup_copied
import metronome.shared.generated.resources.backup_copy
import metronome.shared.generated.resources.backup_copy_description
import metronome.shared.generated.resources.backup_copy_failed
import metronome.shared.generated.resources.backup_paste
import metronome.shared.generated.resources.backup_paste_description
import metronome.shared.generated.resources.backup_paste_first
import org.jetbrains.compose.resources.stringResource

@Suppress("DEPRECATION")
@Composable
actual fun BackupFileActions(enabled: Boolean, export: suspend () -> String, onImport: (String) -> Unit, onMessage: (UiText) -> Unit) {
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var working by remember { mutableStateOf(false) }
    PracticeBackupActions(
        enabled = enabled && !working,
        onExport = {
            working = true
            scope.launch {
                try {
                    clipboard.setText(AnnotatedString(export()))
                    onMessage(UiText.of(Res.string.backup_copied))
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    onMessage(UiText.of(Res.string.backup_copy_failed))
                } finally { working = false }
            }
        },
        onImport = {
            val text = clipboard.getText()?.text
            if (text == null) onMessage(UiText.of(Res.string.backup_paste_first)) else onImport(text)
        },
        exportLabel = stringResource(Res.string.backup_copy),
        exportDescription = stringResource(Res.string.backup_copy_description),
        importLabel = stringResource(Res.string.backup_paste),
        importDescription = stringResource(Res.string.backup_paste_description),
    )
}
