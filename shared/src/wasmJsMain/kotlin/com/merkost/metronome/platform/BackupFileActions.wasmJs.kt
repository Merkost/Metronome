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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
@Composable
actual fun BackupFileActions(enabled: Boolean, export: suspend () -> String, onImport: (String) -> Unit, onMessage: (String) -> Unit) {
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
                    onMessage("Backup copied. Save it in a text file to keep a copy.")
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    onMessage("Couldn't copy the backup. Check your browser clipboard permission.")
                } finally { working = false }
            }
        },
        onImport = {
            val text = clipboard.getText()?.text
            if (text == null) onMessage("Copy a Metronome backup first, then paste it here.") else onImport(text)
        },
        exportLabel = "Copy backup",
        exportDescription = "Copy your presets & practice sets",
        importLabel = "Paste backup",
        importDescription = "Add setups from your clipboard",
    )
}
