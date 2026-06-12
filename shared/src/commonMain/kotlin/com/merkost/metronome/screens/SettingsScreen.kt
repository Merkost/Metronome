package com.merkost.metronome.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mail
import com.composables.icons.lucide.Smartphone
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.Timer
import com.merkost.metronome.components.AppChip
import com.merkost.metronome.components.AppDialog
import com.merkost.metronome.components.MySecondaryButton
import com.merkost.metronome.components.TimestampMillisecondsFormatter
import com.merkost.metronome.model.BeatDisplayStyle
import com.merkost.metronome.model.ClickSound
import com.merkost.metronome.platform.PlatformActions
import com.merkost.metronome.ui.cornerRadiusMedium
import com.merkost.metronome.ui.emojiSize
import com.merkost.metronome.ui.horizontalPadding
import com.merkost.metronome.ui.maxContentWidth
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall
import com.merkost.metronome.ui.theme.AppColorScheme
import com.merkost.metronome.viewModels.SettingsViewModel
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.settings
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
    val hapticEnabled by viewModel.hapticEnabled.collectAsState()
    val keepScreenAwake by viewModel.keepScreenAwake.collectAsState()
    val countInEnabled by viewModel.countInEnabled.collectAsState()
    val practiceStreak by viewModel.practiceStreak.collectAsState()
    val beatDisplayStyle by viewModel.beatDisplayStyle.collectAsState()
    val totalTime by viewModel.totalTime.collectAsState()
    val currentStereo by viewModel.currentStereo.collectAsState()
    val selectedSound by viewModel.selectedSound.collectAsState()

    var showBackgroundPlayPermission by remember { mutableStateOf(false) }
    if (showBackgroundPlayPermission) {
        BackgroundPlayPermissionCheck(true)
    }

    var showResetConfirmation by remember { mutableStateOf(false) }
    if (showResetConfirmation) {
        AppDialog(
            title = "Reset practice time?",
            text = "This clears your total practice time of " +
                "${TimestampMillisecondsFormatter.formatHuman(totalTime)} " +
                "and your streak. This can't be undone.",
            confirmLabel = "Reset",
            onConfirm = {
                viewModel.resetTotalTime()
                showResetConfirmation = false
            },
            onDismiss = { showResetConfirmation = false },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    text = stringResource(Res.string.settings),
                    style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold)
                )
            }, navigationIcon = {
                IconButton(onClick = upPress) {
                    Icon(Lucide.ArrowLeft, "Back")
                }
            })
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(top = it.calculateTopPadding()),
            contentAlignment = Alignment.TopCenter
        ) {
        Column(
            Modifier
                .widthIn(max = maxContentWidth)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontalPadding),
            verticalArrangement = Arrangement.spacedBy(spacingMedium)
        ) {

            AppInfoCard()

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            SettingsRow(title = "Click Sound") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ClickSound.entries.forEach { sound ->
                        val isSelected = sound == selectedSound
                        val borderWidth by animateDpAsState(
                            targetValue = if (isSelected) 2.5.dp else 0.dp
                        )
                        val borderColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                        val containerColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            else Color.Transparent
                        )
                        MySecondaryButton(
                            onClick = { viewModel.onSoundChanged(sound) },
                            border = BorderStroke(borderWidth, borderColor),
                            shape = RoundedCornerShape(cornerRadiusMedium),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(containerColor)
                                    .padding(vertical = 12.dp, horizontal = 8.dp)
                            ) {
                                Text(
                                    text = sound.emoji,
                                    fontSize = emojiSize
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = sound.displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            VolumeSlider()

            SettingsRow(title = "Stereo Panning") {
                val stereoLabel = when {
                    currentStereo < 0 -> "L${-currentStereo}"
                    currentStereo > 0 -> "R$currentStereo"
                    else -> "Center"
                }
                Text(
                    text = stereoLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp),
                    value = currentStereo.toFloat(),
                    onValueChange = viewModel::onStereoChanged,
                    valueRange = -5f..5f,
                    steps = 9,
                    colors = SliderDefaults.colors(
                        inactiveTickColor = Color.Transparent
                    )
                )
            }

            SettingsSwitch("Haptic Feedback", hapticEnabled, viewModel::onHapticChanged, subtitle = "Vibrate on each beat")

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            SettingsRow(title = "Color Scheme") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(spacingSmall)
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
                                            Lucide.Smartphone,
                                            Lucide.Smartphone.name
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
                        horizontalArrangement = Arrangement.spacedBy(spacingSmall),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Lucide.Timer, Lucide.Timer.name)
                        Text(
                            text = buildString {
                                append(TimestampMillisecondsFormatter.formatHuman(totalTime))
                                if (practiceStreak > 0) {
                                    append(" · $practiceStreak-day streak")
                                }
                            },
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                    TextButton(onClick = { showResetConfirmation = true }) {
                        Text(text = "Reset")
                    }
                }
            }

            SettingsSwitch("Color Flash", colorFlash, viewModel::onColorFlashChanged)

            SettingsSwitch(
                "Keep Screen Awake",
                keepScreenAwake,
                viewModel::onKeepScreenAwakeChanged,
                subtitle = "While the metronome plays",
            )

            SettingsSwitch(
                "Count-in",
                countInEnabled,
                viewModel::onCountInChanged,
                subtitle = "One bar before playback starts",
            )

            SettingsRow(title = "Beat Display") {
                Row(horizontalArrangement = Arrangement.spacedBy(spacingSmall)) {
                    BeatDisplayStyle.entries.forEach { style ->
                        AppChip(
                            selected = beatDisplayStyle == style,
                            onClick = { viewModel.onBeatDisplayStyleChanged(style) },
                            label = style.label,
                        )
                    }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            SettingsSwitch(
                "Background Play",
                backgroundPlay,
                onCheckedChange = { enabled ->
                    viewModel.onBackgroundPlayChanged(enabled)
                    if (enabled) showBackgroundPlayPermission = true
                }
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(spacingSmall)
            ) {
                SettingsBigButton("Contact support", icon = Lucide.Mail) {
                    platformActions.contactSupport()
                }
                SettingsBigButton("Rate the App", icon = Lucide.Star) {
                    platformActions.rateApp()
                }
            }
        }
        }
    }
}

@Composable
fun SettingsBigButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(spacingSmall, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(text = text, fontWeight = FontWeight.Bold)
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
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(spacingMedium),
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
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
