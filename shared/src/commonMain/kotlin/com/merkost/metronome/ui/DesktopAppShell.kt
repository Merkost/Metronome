package com.merkost.metronome.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Bookmark
import com.composables.icons.lucide.Layers
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Timer
import com.merkost.metronome.components.MetronomeMark
import com.merkost.metronome.navigation.AppPresentation
import com.merkost.metronome.navigation.LocalAppViewportSize
import com.merkost.metronome.navigation.MainDestination
import com.merkost.metronome.navigation.usesDesktopSidebar
import com.merkost.metronome.ui.theme.readableThemeColor
import org.jetbrains.compose.ui.tooling.preview.Preview

private data class DesktopDestination(
    val destination: MainDestination,
    val label: String,
    val compactLabel: String,
    val icon: ImageVector,
)

private val desktopDestinations = listOf(
    DesktopDestination(MainDestination.Main, "Metronome", "Tempo", Lucide.Timer),
    DesktopDestination(MainDestination.PracticePresets, "Presets", "Presets", Lucide.Bookmark),
    DesktopDestination(MainDestination.PracticeSets, "Practice sets", "Sets", Lucide.Layers),
    DesktopDestination(MainDestination.Settings, "Settings", "Settings", Lucide.Settings),
)

@Composable
fun DesktopAppShell(
    selectedDestination: MainDestination,
    onNavigate: (MainDestination) -> Unit,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        val wide = usesDesktopSidebar(AppPresentation.Desktop, maxWidth.value)
        CompositionLocalProvider(LocalAppViewportSize provides DpSize(maxWidth, maxHeight)) {
            if (wide) {
                Row(Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.width(200.dp).fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(horizontal = horizontalPadding, vertical = spacingMedium),
                        verticalArrangement = Arrangement.spacedBy(spacingSmall),
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = spacingMedium),
                            horizontalArrangement = Arrangement.spacedBy(spacingSmall),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            MetronomeMark(Modifier.size(28.dp), MaterialTheme.colorScheme.onSurface)
                            if (LocalDensity.current.fontScale <= 1.4f) {
                                Text("Metronome", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                        Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).selectableGroup(), verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                            desktopDestinations.forEach { item ->
                                DesktopNavigationItem(
                                    item = item,
                                    selected = selectedDestination == item.destination,
                                    compact = false,
                                    onClick = { onNavigate(item.destination) },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                        if (selectedDestination == MainDestination.Main) {
                            Text(
                                "Space to play or pause\n↑ ↓ to adjust tempo",
                                modifier = Modifier.padding(top = spacingLarge, bottom = spacingSmall),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    VerticalDivider(Modifier.fillMaxHeight(), color = MaterialTheme.colorScheme.outlineVariant)
                    Box(Modifier.weight(1f).fillMaxHeight()) { content() }
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().selectableGroup()
                            .padding(horizontal = spacingSmall, vertical = spacingSmall),
                        horizontalArrangement = Arrangement.spacedBy(spacingSmall),
                    ) {
                        desktopDestinations.forEach { item ->
                            DesktopNavigationItem(
                                item = item,
                                selected = selectedDestination == item.destination,
                                compact = true,
                                onClick = { onNavigate(item.destination) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Box(Modifier.fillMaxWidth().weight(1f)) { content() }
                }
            }
        }
    }
}

@Composable
private fun DesktopNavigationItem(
    item: DesktopDestination,
    selected: Boolean,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val background by animateColorAsState(
        targetValue = when {
            selected -> MaterialTheme.colorScheme.primaryContainer
            hovered -> MaterialTheme.colorScheme.surfaceContainerHighest
            else -> MaterialTheme.colorScheme.surface.copy(alpha = 0f)
        },
        animationSpec = AppAnimations.standard(),
        label = "desktopNavigationBackground",
    )
    val foreground = readableThemeColor(
        foreground = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        background = background.compositeOver(if (compact) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerLow),
        first = MaterialTheme.colorScheme.onSurface,
        second = MaterialTheme.colorScheme.inverseOnSurface,
    )
    val shape = RoundedCornerShape(cornerRadiusMedium)
    val container = modifier.clip(shape).background(background)
        .then(if (focused) Modifier.border(1.dp, MaterialTheme.colorScheme.primary, shape) else Modifier)
        .selectable(selected = selected, role = Role.Tab, interactionSource = interactionSource, indication = null, onClick = onClick)
        .semantics { contentDescription = item.label }
        .heightIn(min = minimumTouchTargetSize)
    if (compact) {
        Column(
            modifier = container.padding(horizontal = spacingSmall / 2, vertical = spacingSmall),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacingSmall / 2),
        ) {
            Icon(item.icon, null, tint = foreground, modifier = Modifier.size(20.dp))
            Text(item.compactLabel, color = foreground, style = MaterialTheme.typography.labelMedium, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
        }
    } else {
        Row(
            modifier = container.padding(horizontal = spacingSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacingSmall),
        ) {
            Icon(item.icon, null, tint = foreground, modifier = Modifier.size(20.dp))
            Text(item.label, color = foreground, style = MaterialTheme.typography.labelLarge, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
        }
    }
}

@Preview(widthDp = 1000, heightDp = 720)
@Composable
private fun DesktopAppShellWidePreview() {
    MaterialTheme { DesktopAppShell(MainDestination.Main, {}) { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("80 BPM") } } }
}

@Preview(widthDp = 520, heightDp = 720)
@Composable
private fun DesktopAppShellCompactPreview() {
    MaterialTheme { DesktopAppShell(MainDestination.Settings, {}) { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Settings") } } }
}
