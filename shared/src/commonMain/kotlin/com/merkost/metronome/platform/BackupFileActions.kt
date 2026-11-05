package com.merkost.metronome.platform

import androidx.compose.runtime.Composable
import com.merkost.metronome.model.UiText

@Composable
expect fun BackupFileActions(
    enabled: Boolean,
    export: suspend () -> String,
    onImport: (String) -> Unit,
    onMessage: (UiText) -> Unit,
)
