package com.merkost.metronome.platform

import androidx.compose.runtime.Composable

@Composable
expect fun BackupFileActions(
    enabled: Boolean,
    export: suspend () -> String,
    onImport: (String) -> Unit,
    onMessage: (String) -> Unit,
)
