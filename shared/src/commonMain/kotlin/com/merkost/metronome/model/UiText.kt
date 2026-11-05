package com.merkost.metronome.model

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

sealed interface UiText {
    data class Str(val resource: StringResource, val args: List<Any> = emptyList()) : UiText

    data class Quantity(
        val resource: PluralStringResource,
        val quantity: Int,
        val args: List<Any> = listOf(quantity),
    ) : UiText

    data class Plain(val value: String) : UiText

    data class Joined(val parts: List<UiText>, val separator: String = "") : UiText

    companion object {
        fun of(resource: StringResource, vararg args: Any): UiText = Str(resource, args.toList())

        fun quantity(resource: PluralStringResource, quantity: Int, vararg args: Any): UiText =
            Quantity(resource, quantity, if (args.isEmpty()) listOf(quantity) else args.toList())

        fun plain(value: String): UiText = Plain(value)
    }
}

@Composable
fun UiText.asString(): String = when (this) {
    is UiText.Plain -> value
    is UiText.Str -> stringResource(resource, *args.map { if (it is UiText) it.asString() else it }.toTypedArray())
    is UiText.Quantity -> pluralStringResource(
        resource,
        quantity,
        *args.map { if (it is UiText) it.asString() else it }.toTypedArray(),
    )
    is UiText.Joined -> parts.map { it.asString() }.joinToString(separator)
}

suspend fun UiText.resolve(): String = when (this) {
    is UiText.Plain -> value
    is UiText.Str -> getString(resource, *args.map { if (it is UiText) it.resolve() else it }.toTypedArray())
    is UiText.Quantity -> getPluralString(
        resource,
        quantity,
        *args.map { if (it is UiText) it.resolve() else it }.toTypedArray(),
    )
    is UiText.Joined -> parts.map { it.resolve() }.joinToString(separator)
}
