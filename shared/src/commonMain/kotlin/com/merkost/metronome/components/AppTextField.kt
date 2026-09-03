package com.merkost.metronome.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import com.merkost.metronome.ui.cornerRadiusLarge
import com.merkost.metronome.ui.minimumTouchTargetSize

@Composable
private fun appTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = Color.Transparent,
    disabledBorderColor = Color.Transparent,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = MaterialTheme.colorScheme.primary,
)

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.heightIn(min = minimumTouchTargetSize),
        label = label?.let { { Text(it) } },
        supportingText = supportingText?.let { { Text(it) } },
        isError = isError,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(cornerRadiusLarge),
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
        colors = appTextFieldColors(),
    )
}

@Composable
fun AppTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.heightIn(min = minimumTouchTargetSize),
        label = label?.let { { Text(it) } },
        supportingText = supportingText?.let { { Text(it) } },
        isError = isError,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(cornerRadiusLarge),
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
        colors = appTextFieldColors(),
    )
}
