package com.merkost.metronome.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.unit.DpSize
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

enum class AppPresentation {
    Compact,
    Desktop,
}

sealed interface AppCommand {
    data class Navigate(val destination: MainDestination) : AppCommand
    data class RequestExit(val proceed: () -> Unit) : AppCommand
    data object Back : AppCommand
}

val LocalAppPresentation = staticCompositionLocalOf { AppPresentation.Compact }
val LocalAppCommands = staticCompositionLocalOf<Flow<AppCommand>> { emptyFlow() }
internal val LocalAppViewportSize = staticCompositionLocalOf { DpSize.Unspecified }
internal val LocalDesktopNavigationGuard = staticCompositionLocalOf<DesktopNavigationGuard?> { null }

internal class DesktopNavigationGuard {
    private class Registration(val destination: MainDestination, val request: (() -> Unit) -> Unit)

    private var registration: Registration? = null

    fun register(destination: MainDestination, request: (() -> Unit) -> Unit): () -> Unit {
        val entry = Registration(destination, request)
        registration = entry
        return { if (registration === entry) registration = null }
    }

    fun navigate(current: MainDestination, destination: MainDestination, proceed: () -> Unit) {
        if (current == destination) return
        leave(current, proceed)
    }

    fun leave(current: MainDestination, proceed: () -> Unit) {
        val guard = registration?.takeIf { it.destination == current }
        if (guard == null) proceed() else guard.request(proceed)
    }
}

internal fun usesDesktopSidebar(presentation: AppPresentation, widthDp: Float): Boolean =
    presentation == AppPresentation.Desktop && widthDp >= 840f

internal fun navigateDesktop(backStack: MutableList<NavKey>, destination: MainDestination) {
    val entries = if (destination == MainDestination.Main) listOf(MainDestination.Main)
    else listOf(MainDestination.Main, destination)
    if (backStack.toList() != entries) {
        backStack.clear()
        backStack.addAll(entries)
    }
}

internal enum class InstrumentKeyAction {
    TogglePlayback,
    IncreaseTempo,
    DecreaseTempo,
}

internal fun instrumentKeyAction(
    presentation: AppPresentation,
    key: Key,
    type: KeyEventType,
    modified: Boolean,
): InstrumentKeyAction? = when {
    presentation != AppPresentation.Desktop || modified -> null
    key == Key.Spacebar && type == KeyEventType.KeyUp -> InstrumentKeyAction.TogglePlayback
    key == Key.DirectionUp && type == KeyEventType.KeyDown -> InstrumentKeyAction.IncreaseTempo
    key == Key.DirectionDown && type == KeyEventType.KeyDown -> InstrumentKeyAction.DecreaseTempo
    else -> null
}
