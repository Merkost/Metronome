package com.merkost.metronome.screens

import android.annotation.SuppressLint
import android.content.Context.AUDIO_SERVICE
import android.media.AudioManager
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.metronome.R
import com.merkost.metronome.components.MySecondaryButton
import com.merkost.metronome.components.TimestampMillisecondsFormatter
import com.merkost.metronome.model.ColorScheme
import com.merkost.metronome.viewModels.SettingsViewModel
import org.koin.androidx.compose.get

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(upPress: () -> Unit) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = get()

    val colorScheme by viewModel.colorScheme.collectAsState()
    val colorFlash by viewModel.colorFlash.collectAsState()
    val backgroundPlay by viewModel.backgroundPlay.collectAsState()
    val totalTime by viewModel.totalTime.collectAsState()
    val currentStereo by viewModel.currentStereo.collectAsState()


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
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Rounded.MoreVert, Icons.Rounded.MoreVert.name)
                    }
                }
            )
        }
    ) {

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(it)
                .padding(horizontalPadding),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {


            // Declare an audio manager
            val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager

            // on below line we are creating variables for
            // volume level, max volume, volume percent.
            val volumeLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolumeLevel = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

            // on below line we are creating a variable
            // for current volume and initializing it.
            var currentVolume by remember {
                mutableStateOf(volumeLevel)
            }

            SettingsRow(title = "Volume") {
                Slider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp),
                    value = currentVolume.toFloat(),
                    onValueChange = {
                        // on below line we are decreasing volume
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, it.toInt(), 0)
                        // on below line we are getting our current volume level.
                        val volumeLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                        currentVolume = volumeLevel
                    },
                    valueRange = 0f..maxVolumeLevel.toFloat(),
                    steps = maxVolumeLevel - 1,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Black, activeTrackColor = Color.Black,
                        inactiveTrackColor = Color.LightGray
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
                        thumbColor = Color.Black, activeTrackColor = Color.Black,
                        inactiveTrackColor = Color.LightGray
                    )
                )
            }

            SettingsRow(title = "Color Scheme") {
                val colors = remember { ColorScheme.values() }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(colors) {
                        ColorSecondaryButton(
                            it,
                            colorScheme == it,
                            onClick = { viewModel.onColorSchemeChanged(it) })
                    }
                }
            }

            SettingsRow(title = "Total Practice Time") {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
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
                SettingsBigButton("Send Feedback", {})
                SettingsBigButton("Rate Us", {})
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBigButton(
    text: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Black, contentColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = CircleShape
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
            )
        }

    }
}


@Composable
fun ColorSecondaryButton(color: ColorScheme, isSelected: Boolean, onClick: () -> Unit) {

    val borderDp by animateDpAsState(targetValue = if (isSelected) 3.dp else 1.dp)
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Color.Black else Color.LightGray.copy(
            alpha = 0.5f
        )
    )
    MySecondaryButton(
        onClick = onClick,
        border = BorderStroke(borderDp, borderColor)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .padding(8.dp)
                .clip(CircleShape)
                .background(color.color),
        )
    }
}

@Composable
fun SettingsRow(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
