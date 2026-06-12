package com.merkost.metronome.screens

import android.app.NotificationManager
import android.content.Context
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.merkost.metronome.components.checkNotificationPolicyAccess

@Composable
actual fun BackgroundPlayPermissionCheck(backgroundPlayEnabled: Boolean) {
    if (backgroundPlayEnabled) {
        val context = LocalContext.current
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        checkNotificationPolicyAccess(
            notificationManager = notificationManager,
            context = context
        )
    }
}

@Composable
actual fun PlatformSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(checked = checked, onCheckedChange = onCheckedChange)
}

@Composable
actual fun LiveActivitySettingsRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
}
