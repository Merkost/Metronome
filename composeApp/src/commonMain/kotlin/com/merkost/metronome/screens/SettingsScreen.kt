package com.merkost.metronome.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.metronome.components.MySecondaryButton
import com.merkost.metronome.components.TimestampMillisecondsFormatter
import com.merkost.metronome.platform.PlatformActions
import com.merkost.metronome.ui.horizontalPadding
import com.merkost.metronome.ui.theme.AppColorScheme
import com.merkost.metronome.viewModels.SettingsViewModel
import metronome.composeapp.generated.resources.Res
import metronome.composeapp.generated.resources.settings
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(upPress: () -> Unit) {
    val viewModel: SettingsViewModel = koinViewModel()
    val platformActions: PlatformActions = koinInject()

    val appColorScheme by viewModel.colorScheme.collectAsState()
    val colorFlash by viewModel.colorFlash.collectAsState()
    val backgroundPlay by viewModel.backgroundPlay.collectAsState()
    val totalTime by viewModel.totalTime.collectAsState()
    val currentStereo by viewModel.currentStereo.collectAsState()

    BackgroundPlayPermissionCheck(backgroundPlay)

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    text = stringResource(Res.string.settings),
                    style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold)
                )
            }, navigationIcon = {
                IconButton(onClick = upPress) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                }
            })
        }
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(it)
                .padding(horizontalPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            AppInfoCard()

            VolumeSlider()

            SettingsRow(title = "Stereo Panning") {
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
                    if (platformActions.isDynamicColorSupported()) {
                        item {
                            ColorSecondaryButton(
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
                    modifier = Modifier.fillMaxWidth()
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
                    platformActions.contactSupport()
                }
                SettingsBigButton("Rate the App") {
                    platformActions.rateApp()
                }
            }
        }
    }
}

@Composable
fun SettingsBigButton(text: String, onClick: () -> Unit) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = CircleShape
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(modifier = Modifier.padding(8.dp), text = text)
        }
    }
}

@Composable
fun ColorSecondaryButton(
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
fun SettingsSwitch(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
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
