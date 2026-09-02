package com.merkost.metronome.navigation

import androidx.navigation3.runtime.NavKey

class AppNavigator(
    private val backStack: MutableList<NavKey>,
) {
    fun navigate(destination: MainDestination) {
        if (destination == MainDestination.Main) {
            returnToMain()
        } else if (backStack.lastOrNull() != destination) {
            backStack.add(destination)
        }
    }

    fun goBack(): Boolean {
        if (backStack.size <= 1) return false
        backStack.removeLast()
        return true
    }

    fun navigateBackToOrPush(destination: MainDestination) {
        val existingIndex = backStack.indexOfLast { it == destination }
        if (existingIndex == -1) {
            navigate(destination)
            return
        }
        while (backStack.lastIndex > existingIndex) {
            backStack.removeLast()
        }
    }

    fun returnToMain() {
        while (backStack.size > 1) {
            backStack.removeLast()
        }
    }
}
