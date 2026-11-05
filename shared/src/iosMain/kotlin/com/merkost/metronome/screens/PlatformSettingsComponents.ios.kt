package com.merkost.metronome.screens

import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.settings_live_activity
import metronome.shared.generated.resources.settings_live_activity_subtitle
import org.jetbrains.compose.resources.stringResource
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
        stringResource(Res.string.settings_live_activity),
        checked,
        onCheckedChange,
        subtitle = stringResource(Res.string.settings_live_activity_subtitle),
    )
}
