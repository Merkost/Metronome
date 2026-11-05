package com.merkost.metronome.components

import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.cancel
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.merkost.metronome.ui.cornerRadiusXLarge

@Composable
fun AppDialog(
    title: String,
    text: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissLabel: String = stringResource(Res.string.cancel),
    destructive: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(cornerRadiusXLarge),
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            Text(title, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(text)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmLabel,
                    fontWeight = FontWeight.Bold,
                    color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = dismissLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    )
}
