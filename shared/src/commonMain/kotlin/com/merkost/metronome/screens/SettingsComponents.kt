package com.merkost.metronome.screens

import androidx.compose.ui.graphics.luminance
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
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ExternalLink
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Monitor
import com.composables.icons.lucide.Smartphone
import com.composables.icons.lucide.Timer
import com.composables.icons.lucide.Volume2
import com.merkost.metronome.components.AppChip
import com.merkost.metronome.components.AppSlider
import com.merkost.metronome.components.SoundPreviewButton
import com.merkost.metronome.components.rememberSoundPreviewMetrics
import com.merkost.metronome.components.AppSegmentedControl
import com.merkost.metronome.model.ThemeMode
import com.merkost.metronome.model.ClickSound
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.cornerRadiusLarge
import com.merkost.metronome.ui.cornerRadiusMedium
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
import kotlin.math.ceil

@Composable
internal fun SettingsSoundPanel(state: SettingsUiState, actions: SettingsActions) {
    var advanced by rememberSaveable { mutableStateOf(false) }
    val volumeLabel = "${(state.volume * 100).roundToInt()}%"
    val stereoLabel = when {
        state.stereo < 0 -> "Left ${-state.stereo * 20}%"
        state.stereo > 0 -> "Right ${state.stereo * 20}%"
        else -> "Centre"
    }
    Surface(shape = RoundedCornerShape(cornerRadiusLarge), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(Modifier.padding(horizontalPadding), verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacingSmall)) {
                SettingsGlyph(Lucide.Volume2)
                Text("Audio", modifier = Modifier.semantics { heading() }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            SettingsSoundSelectionRow(state, actions)
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
            if (state.supportsHaptics) {
                SettingsSwitch("Beat haptics", state.hapticEnabled, actions.onHaptic, "Haptic feedback on each beat")
            }
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
private fun SettingsSoundSelectionRow(state: SettingsUiState, actions: SettingsActions) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val nameStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    val descriptionStyle = MaterialTheme.typography.bodySmall
    val minimumTextWidth = remember(measurer, nameStyle, descriptionStyle) {
        ClickSound.entries.maxOf { sound ->
            maxOf(
                measurer.measure(AnnotatedString(sound.displayName), nameStyle).multiParagraph.intrinsics.minIntrinsicWidth,
                measurer.measure(AnnotatedString(sound.description), descriptionStyle).multiParagraph.intrinsics.minIntrinsicWidth,
            )
        }
    }
    val minimumPickerWidth = with(density) { ceil(minimumTextWidth).toInt().toDp() } + 20.dp
    val previewWidth = rememberSoundPreviewMetrics().width
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (minimumPickerWidth + previewWidth + spacingSmall > maxWidth) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                SettingsSoundPicker(state, actions, Modifier.fillMaxWidth())
                SoundPreviewButton(
                    previewing = state.previewSound == state.selectedSound,
                    onClick = actions.onPreview,
                    enabled = !state.playing,
                    modifier = Modifier.align(Alignment.End),
                )
            }
        } else {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacingSmall)) {
                SettingsSoundPicker(state, actions, Modifier.weight(1f))
                SoundPreviewButton(
                    previewing = state.previewSound == state.selectedSound,
                    onClick = actions.onPreview,
                    enabled = !state.playing,
                )
            }
        }
    }
}

@Composable
private fun SettingsSoundPicker(state: SettingsUiState, actions: SettingsActions, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clip(RoundedCornerShape(cornerRadiusLarge))
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
        Icon(if (LocalLayoutDirection.current == LayoutDirection.Rtl) Lucide.ChevronLeft else Lucide.ChevronRight, null, modifier = Modifier.size(20.dp))
    }
}

@Preview
@Composable
private fun SettingsSoundSelectionRowPreview() {
    MaterialTheme { SettingsSoundSelectionRow(SettingsUiState(), SettingsActions()) }
}

@Preview
@Composable
private fun SettingsSoundPickerPreview() {
    MaterialTheme { SettingsSoundPicker(SettingsUiState(), SettingsActions()) }
}

@Composable
internal fun SettingsAppearancePanel(state: SettingsUiState, actions: SettingsActions) {
    val measurer = rememberTextMeasurer()
    val nameStyle = MaterialTheme.typography.labelLarge
    val nameWidth = with(LocalDensity.current) {
        AppColorScheme.entries.maxOf { measurer.measure(AnnotatedString(it.settingsName), nameStyle, softWrap = false, maxLines = 1).size.width }.toDp()
    }
    Column(Modifier.fillMaxWidth().padding(vertical = spacingSmall), verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
        Text("Appearance", modifier = Modifier.semantics { heading() }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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
        SettingsRow("Theme") {
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
        val face = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) palette.darkColor else palette.lightColor
        val faceShape = RoundedCornerShape(cornerRadiusMedium)
        Column(
            Modifier.size(width = 60.dp, height = 76.dp)
                .border(ring.coerceAtLeast(1.dp), if (isSelected) ringColor else MaterialTheme.colorScheme.outlineVariant, faceShape)
                .clip(faceShape)
                .background(face.background)
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(3) { dot ->
                    Box(Modifier.size(6.dp).clip(CircleShape).background(face.primary.copy(alpha = if (dot == 0) 1f else 0.3f)))
                }
            }
            Text("96", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = face.onBackground, maxLines = 1)
            Box(Modifier.size(18.dp).clip(CircleShape).background(face.primary), contentAlignment = Alignment.Center) {
                AnimatedContent(isSelected, transitionSpec = { AppAnimations.fadeScaleTransform }, label = "paletteCheck") { chosen ->
                    if (chosen) Icon(Lucide.Check, null, tint = face.onPrimary, modifier = Modifier.size(12.dp))
                    else if (palette == AppColorScheme.MATERIAL3) Icon(Lucide.Smartphone, null, tint = face.onPrimary, modifier = Modifier.size(11.dp))
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
    Surface(shape = RoundedCornerShape(cornerRadiusLarge), color = MaterialTheme.colorScheme.surfaceContainerLow) {
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
                    Text(title, modifier = Modifier.semantics { heading() }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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
    Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
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
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacingSmall / 2)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            if (external) Lucide.ExternalLink else if (LocalLayoutDirection.current == LayoutDirection.Rtl) Lucide.ChevronLeft else Lucide.ChevronRight,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
internal fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(cornerRadiusLarge), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(Modifier.fillMaxWidth().padding(horizontalPadding), verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
            Text(title, modifier = Modifier.semantics { heading() }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
internal fun SettingsWebCard(onClick: () -> Unit) {
    SettingsLink("Open web app", "metronome.merkost.dev", Lucide.Monitor, onClick, external = true)
}

@Composable
internal fun SettingsSubyCard(onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
        Text("More by Merkost", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = spacingSmall))
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(cornerRadiusLarge))
                .pressableSurface(onClick).heightIn(min = minimumTouchTargetSize).padding(vertical = spacingSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacingMedium),
        ) {
            Image(painterResource(Res.drawable.suby_icon), null, modifier = Modifier.size(40.dp).clip(RoundedCornerShape(cornerRadiusMedium)))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacingSmall / 2)) {
                Text("Suby", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Keep subscriptions & renewals in one place", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Lucide.ExternalLink, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
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
            .clip(RoundedCornerShape(cornerRadiusMedium))
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
    MaterialTheme { SettingsDisclosure("Playback", "Count-in, screen & background audio", Lucide.Timer) { SettingsSwitch("Count-in", true, {}, "One bar before playback") } }
}

@Preview
@Composable
private fun SettingsLinkPreview() {
    MaterialTheme { SettingsLink("Statistics & backups", "Practice time, export & import", Lucide.Timer, {}) }
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
            SettingsSlider("Click volume", "80%") { AppSlider(0.8f, {}, valueRange = 0f..1f, accessibilityLabel = "Click volume") }
            SettingsRow("Appearance") { SettingsChoiceRow { AppChip(true, {}, "System") } }
            SettingsSwitch("Beat haptics", false, {}, "Haptic feedback on each beat")
        }
    }
}

@Preview
@Composable
private fun SettingsGroupPreview() {
    MaterialTheme { SettingsGroup("Help") { SettingsWebCard({}) } }
}
