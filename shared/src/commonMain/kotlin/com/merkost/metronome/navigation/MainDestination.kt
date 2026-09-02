package com.merkost.metronome.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface MainDestination : NavKey {
    @Serializable
    data object Main : MainDestination

    @Serializable
    data object Settings : MainDestination

    @Serializable
    data object PracticePresets : MainDestination

    @Serializable
    data object PracticeSets : MainDestination
}
