package com.merkost.metronome.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.merkost.metronome.platform.IosAppVersionProvider
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.app_full_name
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun AppInfoCard() {
    val appVersion = IosAppVersionProvider().getAppVersion()
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = stringResource(Res.string.app_full_name))
            Text(
                text = appVersion?.versionName.orEmpty(),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
actual fun VolumeSlider() {
    // iOS doesn't support programmatic volume control
}

@Composable
actual fun BackgroundPlayPermissionCheck(backgroundPlayEnabled: Boolean) {
    // iOS background audio uses AVAudioSession — no runtime permission needed
}
