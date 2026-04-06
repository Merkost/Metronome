package com.merkost.metronome.screens

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.merkost.metronome.R
import com.merkost.metronome.components.checkNotificationPolicyAccess
import com.merkost.metronome.platform.AndroidAppVersionProvider
import metronome.composeapp.generated.resources.Res
import metronome.composeapp.generated.resources.app_full_name
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun AppInfoCard() {
    val context = LocalContext.current
    val appVersion = remember { AndroidAppVersionProvider(context).getAppVersion() }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Icon_launcher"
                )
            }
            Column {
                Text(text = stringResource(Res.string.app_full_name))
                Text(
                    text = appVersion?.versionName.orEmpty(),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

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
