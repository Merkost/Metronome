package com.merkost.metronome.screens

import android.app.NotificationManager
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.metronome.R
import com.merkost.metronome.components.MySecondaryButton
import com.merkost.metronome.components.TimestampMillisecondsFormatter
import com.merkost.metronome.components.checkNotificationPolicyAccess
import com.merkost.metronome.ui.theme.AppColorScheme
import com.merkost.metronome.utils.appVersion
import com.merkost.metronome.viewModels.SettingsViewModel
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(upPress: () -> Unit) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = koinViewModel()

    val appColorScheme by viewModel.colorScheme.collectAsState()
    val colorFlash by viewModel.colorFlash.collectAsState()
    val backgroundPlay by viewModel.backgroundPlay.collectAsState()
    val totalTime by viewModel.totalTime.collectAsState()
    val currentStereo by viewModel.currentStereo.collectAsState()

    if (backgroundPlay) {
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        checkNotificationPolicyAccess(
            notificationManager = notificationManager,
            context = context
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    text = stringResource(R.string.settings),
                    style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold)
                )
            }, navigationIcon = {
                IconButton(onClick = upPress) {
                    Icon(Icons.Rounded.ArrowBack, Icons.Rounded.ArrowBack.name)
                }
            },
                actions = {
//                    IconButton(onClick = { /*TODO*/ }) {
//                        Icon(Icons.Rounded.MoreVert, Icons.Rounded.MoreVert.name)
//                    }
                }
            )
        }
    ) {

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(it)
                .padding(horizontalPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Declare an audio manager
            val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager

            val volumeLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolumeLevel = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

            var currentVolume by remember {
                mutableIntStateOf(volumeLevel)
            }

            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            modifier = Modifier
                                .fillMaxSize(),
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Icon_launcher"
                        )
                    }
                    Column {
                        Text(text = stringResource(id = R.string.app_full_name))
                        Text(
                            text = context.appVersion?.versionName.orEmpty(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            SettingsRow(title = "Volume") {
                Slider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp),
                    value = currentVolume.toFloat(),
                    onValueChange = {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, it.toInt(), 0)
                        val volumeLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                        currentVolume = volumeLevel
                    },
                    valueRange = 0f..maxVolumeLevel.toFloat(),
                    steps = maxVolumeLevel - 1,
                    colors = SliderDefaults.colors(
                        inactiveTickColor = Color.Transparent
                    )
                )
            }

            SettingsRow(
                title = "Stereo Panning",
            ) {
                Slider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp),
                    value = currentStereo.toFloat(),
                    onValueChange = viewModel::onStereoChanged,
                    valueRange = -5f..5f,
                    steps = 11,
                    colors = SliderDefaults.colors(
                        inactiveTickColor = Color.Transparent
                    )
                )
            }

            SettingsRow(title = "Color Scheme") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        item {
                            ColorSecondaryButton(
                                modifier = Modifier,
                                content = {
                                    Box(
                                        modifier = Modifier
                                            .size(55.dp)
                                            .padding(6.dp)
                                            .clip(CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Rounded.Smartphone,
                                            Icons.Rounded.Smartphone.name
                                        )
                                    }
                                },
                                isSelected = appColorScheme == AppColorScheme.MATERIAL3,
                                onClick = { viewModel.onColorSchemeChanged(AppColorScheme.MATERIAL3) })
                        }
                    }

                    items(AppColorScheme.defaultValues()) { colorScheme ->
                        ColorSecondaryButton(
                            modifier = Modifier,
                            content = {
                                Box(
                                    modifier = Modifier
                                        .size(55.dp)
                                        .padding(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    colorScheme.lightColor.primary,
                                                    colorScheme.darkColor.primary
                                                ),
                                                tileMode = TileMode.Repeated
                                            )
                                        ),
                                )
                            },
                            isSelected = colorScheme == appColorScheme,
                            onClick = { viewModel.onColorSchemeChanged(colorScheme) })
                    }
                }
            }

            SettingsRow(
                title = "Total Practice Time",
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.Alarm, Icons.Rounded.Alarm.name)
                        Text(
                            text = TimestampMillisecondsFormatter.format(totalTime),
                            maxLines = 1,
                            lineHeight = 18.sp,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                    TextButton(onClick = viewModel::resetTotalTime) {
                        Text(text = "Reset")
                    }
                }
            }

            SettingsSwitch("Color Flash", colorFlash, viewModel::onColorFlashChanged)
            SettingsSwitch("Background Play", backgroundPlay, viewModel::onBackgroundPlayChanged)

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SettingsBigButton("Contact support") {
                    context.contactSupport()
                }
                SettingsBigButton("Rate the App") {
                    context.openGooglePlay()
                }
            }
        }

    }
}

private fun Context.contactSupport() {
    runCatching {
        val osVersion = Build.VERSION.RELEASE
        val phoneModel = Build.MODEL
        val manufacturer = Build.MANUFACTURER
        val appVersionName = appVersion?.versionName
        val appVersionNumber = appVersion?.versionNumber

        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("merkostdev+metronome@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Support Request from Metronome App")
            putExtra(
                Intent.EXTRA_TEXT,
                "Please describe the issue you're experiencing or your question below:\n" +
                        "\n\n\n\n\n" +
                        "Device Information:\n" +
                        "OS Version: $osVersion\n" +
                        "Phone Model: $phoneModel\n" +
                        "Manufacturer: $manufacturer\n" +
                        "App version: $appVersionName ($appVersionNumber)\n"
            )
        }

        startActivity(Intent.createChooser(emailIntent, "Send email..."))

    }.onFailure {
        Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show()
    }
}

private fun Context.openGooglePlay() {
    runCatching {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + this.packageName)
            )
        )
    }.onFailure {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=" + this.packageName)
            )
        )
    }
}

@Composable
fun SettingsBigButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = CircleShape
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                modifier = Modifier.padding(8.dp),
                text = text,
            )
        }

    }
}


@Composable
fun ColorSecondaryButton(
    modifier: Modifier,
    content: @Composable () -> Unit,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderDp by animateDpAsState(targetValue = if (isSelected) 6.dp else 1.dp)

    MySecondaryButton(
        onClick = onClick,
        border = BorderStroke(borderDp, MaterialTheme.colorScheme.primary)
    ) {
        content()
    }
}

@Composable
fun SettingsRow(
    title: String,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(16.dp),
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = verticalArrangement
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
        content()
    }
}

@Composable
fun SettingsSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
