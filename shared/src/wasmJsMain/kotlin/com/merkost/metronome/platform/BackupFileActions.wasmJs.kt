package com.merkost.metronome.platform

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.merkost.metronome.components.MySecondaryButton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
@Composable
actual fun BackupFileActions(enabled: Boolean, export: suspend () -> String, onImport: (String) -> Unit, onMessage: (String) -> Unit) {
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var working by remember { mutableStateOf(false) }
    MySecondaryButton(onClick = {
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
    }, enabled = enabled && !working, modifier = Modifier.fillMaxWidth()) { Text("Copy backup") }
    MySecondaryButton(onClick = {
        val text = clipboard.getText()?.text
        if (text == null) onMessage("Copy a Metronome backup first, then paste it here.") else onImport(text)
    }, enabled = enabled && !working, modifier = Modifier.fillMaxWidth()) { Text("Paste backup") }
}
