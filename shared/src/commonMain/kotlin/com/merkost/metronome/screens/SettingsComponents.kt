package com.merkost.metronome.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.CalendarDays
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.ExternalLink
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Monitor
import com.composables.icons.lucide.Palette
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Smartphone
import com.composables.icons.lucide.Timer
import com.composables.icons.lucide.Users
import com.composables.icons.lucide.Volume2
import com.composables.icons.lucide.Wallet
import com.merkost.metronome.components.AppChip
import com.merkost.metronome.components.AppSlider
import com.merkost.metronome.components.AppSegmentedControl
import com.merkost.metronome.model.ThemeMode
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.cornerRadiusLarge
import com.merkost.metronome.ui.cornerRadiusMedium
import com.merkost.metronome.ui.cornerRadiusXLarge
import com.merkost.metronome.ui.horizontalPadding
import com.merkost.metronome.ui.minimumTouchTargetSize
import com.merkost.metronome.ui.pressableSurface
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall
import com.merkost.metronome.ui.theme.AppColorScheme
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.suby_icon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.roundToInt

@Composable
internal fun SettingsSoundPanel(state: SettingsUiState, actions: SettingsActions) {
    var advanced by rememberSaveable { mutableStateOf(false) }
    val volumeLabel = "${(state.volume * 100).roundToInt()}%"
    val stereoLabel = when {
        state.stereo < 0 -> "Left ${-state.stereo * 20}%"
        state.stereo > 0 -> "Right ${state.stereo * 20}%"
        else -> "Centre"
    }
    Surface(shape = RoundedCornerShape(cornerRadiusXLarge), color = MaterialTheme.colorScheme.secondaryContainer) {
        Column(Modifier.padding(horizontalPadding), verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacingSmall)) {
                SettingsGlyph(Lucide.Volume2)
                Text("Find your click.", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacingSmall)) {
                Row(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(cornerRadiusLarge))
                        .pressableSurface(actions.onSoundPicker)
                        .heightIn(min = minimumTouchTargetSize)
                        .padding(vertical = spacingSmall),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacingSmall / 2)) {
                        AnimatedContent(state.selectedSound, transitionSpec = { AppAnimations.fadeThrough }, label = "settingsSound") { sound ->
                            Text(sound.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        AnimatedContent(state.selectedSound, transitionSpec = { AppAnimations.fadeThrough }, label = "settingsSoundDescription") { sound ->
                            Text(sound.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(Lucide.ChevronRight, null, modifier = Modifier.size(20.dp))
                }
                val previewing = state.previewSound == state.selectedSound
                Row(
                    modifier = Modifier.clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .pressableSurface(actions.onPreview, enabled = !state.playing)
                        .heightIn(min = minimumTouchTargetSize)
                        .padding(horizontal = 12.dp, vertical = spacingSmall),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacingSmall / 2),
                ) {
                    AnimatedContent(previewing, transitionSpec = { AppAnimations.fadeScaleTransform }, label = "settingsPreview") { active ->
                        Icon(if (active) Lucide.Pause else Lucide.Play, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                    }
                    Text(if (previewing) "Stop" else "Preview", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            AnimatedVisibility(state.playing, enter = AppAnimations.expandEnter, exit = AppAnimations.shrinkExit) {
                Text("Pause playback to preview a click.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            SettingsSlider("Click volume", volumeLabel) {
                AppSlider(
                    value = state.volume,
                    onValueChange = actions.onVolume,
                    valueRange = 0f..1f,
                    accessibilityLabel = "Click volume, $volumeLabel",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            state.previewError?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
            SettingsSwitch("Beat haptics", state.hapticEnabled, actions.onHaptic, "Feel the beat, too")
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(cornerRadiusMedium))
                    .pressableSurface({ advanced = !advanced })
                    .semantics { stateDescription = if (advanced) "Expanded" else "Collapsed" }
                    .heightIn(min = minimumTouchTargetSize),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("More audio controls", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                SettingsChevron(advanced)
            }
            AnimatedVisibility(advanced, enter = AppAnimations.expandEnter, exit = AppAnimations.shrinkExit) {
                SettingsSlider("Stereo pan", stereoLabel) {
                    AppSlider(
                        value = state.stereo.toFloat(),
                        onValueChange = actions.onStereo,
                        valueRange = -5f..5f,
                        steps = 9,
                        showActiveTicks = true,
                        accessibilityLabel = "Stereo pan, $stereoLabel",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
internal fun SettingsAppearancePanel(state: SettingsUiState, actions: SettingsActions) {
    val measurer = rememberTextMeasurer()
    val nameStyle = MaterialTheme.typography.labelLarge
    val nameWidth = with(LocalDensity.current) {
        AppColorScheme.entries.maxOf { measurer.measure(AnnotatedString(it.settingsName), nameStyle, softWrap = false, maxLines = 1).size.width }.toDp()
    }
    Column(Modifier.fillMaxWidth().padding(vertical = spacingSmall), verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacingSmall)) {
            Text("Set the mood.", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            SettingsGlyph(Lucide.Palette)
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Colour scheme", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            AnimatedContent(
                state.colorScheme,
                modifier = Modifier.width(nameWidth),
                transitionSpec = { AppAnimations.fadeThroughFixedSize },
                contentAlignment = Alignment.CenterEnd,
                label = "paletteName",
            ) { scheme ->
                Text(scheme.settingsName, style = nameStyle, color = MaterialTheme.colorScheme.onSurfaceVariant, softWrap = false, maxLines = 1)
            }
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).selectableGroup(), horizontalArrangement = Arrangement.spacedBy(spacingSmall)) {
            val palettes = if (state.supportsDynamicColor) listOf(AppColorScheme.MATERIAL3) + AppColorScheme.defaultValues() else AppColorScheme.defaultValues()
            palettes.forEach { palette ->
                SettingsPaletteChoice(palette, state.colorScheme == palette, { actions.onColorScheme(palette) }, Modifier.widthIn(min = minimumTouchTargetSize))
            }
        }
        SettingsRow("Appearance") {
            AppSegmentedControl(
                options = ThemeMode.entries.map { it.label },
                selectedIndex = ThemeMode.entries.indexOf(state.themeMode),
                onSelect = { actions.onTheme(ThemeMode.entries[it]) },
            )
        }
    }
}

@Composable
private fun SettingsPaletteChoice(
    palette: AppColorScheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ring by animateDpAsState(if (isSelected) 2.dp else 0.dp, AppAnimations.emphasized(), label = "paletteRing")
    val ringColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0f), AppAnimations.standard(), label = "paletteRingColor")
    val labelColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, AppAnimations.standard(), label = "paletteLabel")
    Column(
        modifier = modifier.clip(RoundedCornerShape(cornerRadiusMedium))
            .pressableSurface(onClick, pressedScale = 1f, role = Role.RadioButton)
            .semantics { contentDescription = "${palette.settingsName} colour scheme"
                selected = isSelected }
            .padding(vertical = spacingSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacingSmall),
    ) {
        Box(Modifier.size(minimumTouchTargetSize).border(ring, ringColor, CircleShape).padding(4.dp), contentAlignment = Alignment.Center) {
            Surface(shape = CircleShape, color = palette.lightColor.primary, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    AnimatedContent(isSelected, transitionSpec = { AppAnimations.fadeScaleTransform }, label = "paletteCheck") { chosen ->
                        if (chosen) Icon(Lucide.Check, null, tint = palette.lightColor.onPrimary, modifier = Modifier.size(20.dp))
                        else if (palette == AppColorScheme.MATERIAL3) Icon(Lucide.Smartphone, null, tint = palette.lightColor.onPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
        Text(palette.settingsName, style = MaterialTheme.typography.labelSmall, color = labelColor, fontWeight = FontWeight.SemiBold, softWrap = false, maxLines = 1)
    }
}

@Composable
internal fun SettingsDisclosure(
    title: String,
    description: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Surface(shape = RoundedCornerShape(cornerRadiusXLarge), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().pressableSurface({ expanded = !expanded })
                    .semantics { stateDescription = if (expanded) "Expanded" else "Collapsed" }
                    .heightIn(min = minimumTouchTargetSize)
                    .padding(horizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(spacingSmall),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SettingsGlyph(icon)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacingSmall / 2)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                SettingsChevron(expanded)
            }
            AnimatedVisibility(expanded, enter = AppAnimations.expandEnter, exit = AppAnimations.shrinkExit) {
                Column(Modifier.padding(start = horizontalPadding, end = horizontalPadding, bottom = horizontalPadding), verticalArrangement = Arrangement.spacedBy(spacingMedium), content = content)
            }
        }
    }
}

@Composable
private fun SettingsChevron(expanded: Boolean) {
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, AppAnimations.emphasized(), label = "settingsChevron")
    Icon(Lucide.ChevronDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp).rotate(rotation))
}

@Composable
private fun SettingsGlyph(icon: ImageVector) {
    Surface(shape = RoundedCornerShape(cornerRadiusLarge), color = MaterialTheme.colorScheme.primaryContainer) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(10.dp).size(22.dp))
    }
}

@Composable
internal fun SettingsLink(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    external: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(cornerRadiusLarge))
            .pressableSurface(onClick).heightIn(min = minimumTouchTargetSize).padding(vertical = 12.dp, horizontal = spacingSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacingMedium),
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacingSmall / 2)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(if (external) Lucide.ExternalLink else Lucide.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}

@Composable
internal fun SettingsWebCard(onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(cornerRadiusXLarge), color = MaterialTheme.colorScheme.primaryContainer) {
        Column(Modifier.padding(horizontalPadding), verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                    Text("Metronome on the web", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Your beat.\nAny screen.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Icon(Lucide.Monitor, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(40.dp))
            }
            Text("Open Metronome in your browser.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Surface(
                shape = RoundedCornerShape(cornerRadiusLarge),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(cornerRadiusLarge)).pressableSurface(onClick),
            ) {
                Row(
                    Modifier.heightIn(min = minimumTouchTargetSize).padding(horizontal = spacingMedium, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacingSmall),
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacingSmall / 2)) {
                        Text("Open web app", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Text("metronome.merkost.dev", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Lucide.ExternalLink, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
internal fun SettingsSubyCard(onClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
        Text("More by Merkost", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = spacingSmall))
        Surface(shape = RoundedCornerShape(cornerRadiusXLarge), color = MaterialTheme.colorScheme.tertiaryContainer) {
            Column(Modifier.padding(horizontalPadding), verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
                Row(horizontalArrangement = Arrangement.spacedBy(spacingSmall), verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(Res.drawable.suby_icon), null, modifier = Modifier.size(54.dp).clip(RoundedCornerShape(cornerRadiusLarge)))
                    Column(verticalArrangement = Arrangement.spacedBy(spacingSmall / 2)) {
                        Text("Suby", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        Text("Subscription manager", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                    Text("Subscriptions, a little more sorted.", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Keep renewals, costs and shared plans in one place.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SettingsFeature(Lucide.CalendarDays, "Renewals")
                    SettingsFeature(Lucide.Wallet, "Costs")
                    SettingsFeature(Lucide.Users, "Sharing")
                }
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth().clip(CircleShape).pressableSurface(onClick)) {
                    Row(Modifier.heightIn(min = minimumTouchTargetSize).padding(horizontal = spacingMedium, vertical = spacingSmall), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Explore Suby", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Icon(Lucide.ExternalLink, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsFeature(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacingSmall / 2)) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun SettingsSlider(title: String, value: String, slider: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(spacingSmall / 2)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            AnimatedContent(value, transitionSpec = { AppAnimations.fadeThrough }, label = "settingsSliderValue") { label ->
                Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        slider()
    }
}

@Composable
fun SettingsRow(
    title: String,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(spacingSmall),
    content: @Composable () -> Unit,
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = verticalArrangement) {
        Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        content()
    }
}

@Composable
internal fun SettingsChoiceRow(content: @Composable RowScope.() -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(spacingSmall), content = content)
}

@Composable
fun SettingsSwitch(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, subtitle: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = minimumTouchTargetSize)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .semantics(mergeDescendants = true) {
                contentDescription = subtitle?.let { "$title. $it" } ?: title
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacingSmall),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacingSmall / 2)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Box(Modifier.clearAndSetSemantics {}.focusProperties { canFocus = false }) { PlatformSwitch(checked, onCheckedChange) }
    }
}

private val AppColorScheme.settingsName: String
    get() = when (this) {
        AppColorScheme.MATERIAL3 -> "System"
        AppColorScheme.BLACKNWHITE -> "Mono"
        AppColorScheme.MELROSE -> "Violet"
        AppColorScheme.PERIWINKLE -> "Blue"
        AppColorScheme.MINT_GREEN -> "Mint"
        AppColorScheme.PINK_LACE -> "Pink"
    }

@Preview
@Composable
private fun SettingsPanelsPreview() {
    MaterialTheme {
        Column(verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
            SettingsSoundPanel(SettingsUiState(), SettingsActions())
            SettingsAppearancePanel(SettingsUiState(), SettingsActions())
        }
    }
}

@Preview
@Composable
private fun SettingsDisclosurePreview() {
    MaterialTheme { SettingsDisclosure("Keep your flow.", "Practice & playback", Lucide.Timer) { SettingsSwitch("Count-in", true, {}, "One bar to ease you in") } }
}

@Preview
@Composable
private fun SettingsLinkPreview() {
    MaterialTheme { SettingsLink("Your practice", "Time, backups & data", Lucide.Timer, {}) }
}

@Preview
@Composable
private fun SettingsCardsPreview() {
    MaterialTheme {
        Column(verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
            SettingsWebCard({})
            SettingsSubyCard({})
        }
    }
}

@Preview
@Composable
private fun SettingsSmallComponentsPreview() {
    MaterialTheme {
        Column(verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
            SettingsPaletteChoice(AppColorScheme.MELROSE, true, {})
            SettingsChevron(true)
            SettingsGlyph(Lucide.Volume2)
            SettingsFeature(Lucide.CalendarDays, "Renewals")
            SettingsSlider("Click volume", "80%") { AppSlider(0.8f, {}, valueRange = 0f..1f, accessibilityLabel = "Click volume") }
            SettingsRow("Appearance") { SettingsChoiceRow { AppChip(true, {}, "System") } }
            SettingsSwitch("Beat haptics", false, {}, "Feel the beat, too")
        }
    }
}
