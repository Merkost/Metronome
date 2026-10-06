package com.merkost.metronome.screens

import androidx.compose.runtime.Composable
import com.merkost.metronome.components.IosStyleSwitch

@Composable
actual fun PlatformSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    IosStyleSwitch(checked, onCheckedChange)
}

@Composable
actual fun BackgroundPlayPermissionCheck(backgroundPlayEnabled: Boolean) {
}

@Composable
actual fun LiveActivitySettingsRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    SettingsSwitch(
        "Live Activity",
        checked,
        onCheckedChange,
        subtitle = "Tempo and timer on the Lock Screen",
    )
}
