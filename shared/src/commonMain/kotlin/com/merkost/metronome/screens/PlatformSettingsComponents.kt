package com.merkost.metronome.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.merkost.metronome.platform.AppVersionProvider
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.app_full_name
import metronome.shared.generated.resources.ic_launcher
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun AppInfoCard() {
    val appVersionProvider: AppVersionProvider = koinInject()
    val appVersion = appVersionProvider.getAppVersion()

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp)),
                painter = painterResource(Res.drawable.ic_launcher),
                contentDescription = "App icon",
                contentScale = ContentScale.Crop
            )
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
expect fun BackgroundPlayPermissionCheck(backgroundPlayEnabled: Boolean)

@Composable
expect fun PlatformSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit)

@Composable
expect fun LiveActivitySettingsRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit)
