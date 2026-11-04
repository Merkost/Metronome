package com.merkost.metronome.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
actual fun BackgroundPlayPermissionCheck(backgroundPlayEnabled: Boolean) = Unit

@Composable
actual fun PlatformSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(checked = checked, onCheckedChange = onCheckedChange)
}

@Composable
actual fun LiveActivitySettingsRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit) = Unit

@Preview
@Composable
private fun DesktopPlatformSwitchPreview() {
    MaterialTheme { PlatformSwitch(true, {}) }
}
