package com.merkost.metronome.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.FileInput
import com.composables.icons.lucide.FileOutput
import com.composables.icons.lucide.Lucide
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.cornerRadiusLarge
import com.merkost.metronome.ui.cornerRadiusMedium
import com.merkost.metronome.ui.minimumTouchTargetSize
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun PracticeBackupActions(
    enabled: Boolean,
    onExport: () -> Unit,
    onImport: () -> Unit,
    modifier: Modifier = Modifier,
    exportLabel: String = "Export backup",
    exportDescription: String = "Save your presets & practice sets",
    importLabel: String = "Import backup",
    importDescription: String = "Add setups from a backup",
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadiusLarge),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column {
            PracticeBackupActionRow(exportLabel, exportDescription, Lucide.FileOutput, enabled, onExport)
            HorizontalDivider(
                modifier = Modifier.padding(start = spacingMedium * 2 + 40.dp, end = spacingMedium),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            PracticeBackupActionRow(importLabel, importDescription, Lucide.FileInput, enabled, onImport)
        }
    }
}

@Composable
private fun PracticeBackupActionRow(
    label: String,
    description: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val disabledForeground = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    val chevron = if (LocalLayoutDirection.current == LayoutDirection.Rtl) Lucide.ChevronLeft else Lucide.ChevronRight
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val color by animateColorAsState(
        if (enabled && (pressed || focused)) MaterialTheme.colorScheme.surfaceContainerHigh
        else MaterialTheme.colorScheme.surfaceContainerLow,
        AppAnimations.press(),
        label = "backupActionSurface",
    )
    val iconColor by animateColorAsState(
        if (enabled) MaterialTheme.colorScheme.onPrimaryContainer else disabledForeground,
        AppAnimations.standard(),
        label = "backupActionIcon",
    )
    val iconContainer by animateColorAsState(
        if (enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        AppAnimations.standard(),
        label = "backupActionIconContainer",
    )
    val titleColor by animateColorAsState(
        if (enabled) MaterialTheme.colorScheme.onSurface else disabledForeground,
        AppAnimations.standard(),
        label = "backupActionTitle",
    )
    val supportingColor by animateColorAsState(
        if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else disabledForeground,
        AppAnimations.standard(),
        label = "backupActionSupportingContent",
    )
    Row(
        modifier = Modifier.fillMaxWidth().background(color)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .heightIn(min = minimumTouchTargetSize)
            .padding(horizontal = spacingMedium, vertical = spacingSmall * 1.5f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacingMedium),
    ) {
        Surface(shape = RoundedCornerShape(cornerRadiusMedium), color = iconContainer) {
            Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacingSmall / 2)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = titleColor)
            Text(description, style = MaterialTheme.typography.bodySmall, color = supportingColor)
        }
        Icon(chevron, null, tint = supportingColor, modifier = Modifier.size(18.dp))
    }
}

@Preview
@Composable
private fun PracticeBackupActionsPreview() {
    MaterialTheme { PracticeBackupActions(true, {}, {}, Modifier.width(360.dp)) }
}

@Preview
@Composable
private fun PracticeBackupActionsDisabledPreview() {
    MaterialTheme { PracticeBackupActions(false, {}, {}, Modifier.width(360.dp)) }
}

@Preview
@Composable
private fun PracticeBackupActionsLargeTextPreview() {
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
        MaterialTheme { PracticeBackupActions(true, {}, {}, Modifier.width(320.dp)) }
    }
}

@Preview
@Composable
private fun PracticeBackupActionRowPreview() {
    MaterialTheme {
        PracticeBackupActionRow("Export backup", "Save your presets & practice sets", Lucide.FileOutput, true, {})
    }
}
