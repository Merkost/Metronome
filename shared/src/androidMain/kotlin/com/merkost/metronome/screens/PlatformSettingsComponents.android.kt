package com.merkost.metronome.screens

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.merkost.metronome.components.checkNotificationPolicyAccess

@Composable
actual fun VolumeSlider() {
    val context = LocalContext.current
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val maxVolumeLevel = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    var currentVolume by remember {
        mutableIntStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
    }

    SettingsRow(title = "Volume") {
        Text(
            text = "$currentVolume / $maxVolumeLevel",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp),
            value = currentVolume.toFloat(),
            onValueChange = {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, it.toInt(), 0)
                currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            },
            valueRange = 0f..maxVolumeLevel.toFloat(),
            steps = maxVolumeLevel - 1,
            colors = SliderDefaults.colors(
                inactiveTickColor = Color.Transparent
            )
        )
    }
}

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
