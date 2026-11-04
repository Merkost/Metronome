package com.merkost.metronome.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WasmMotionPreferences : MotionPreferences {
    private val state = MutableStateFlow(prefersReducedMotion())

    override val reduceMotion: StateFlow<Boolean> = state

    init {
        watchReducedMotion { state.value = it }
    }
}

private fun prefersReducedMotion(): Boolean =
    js("window.matchMedia('(prefers-reduced-motion: reduce)').matches")

private fun watchReducedMotion(listener: (Boolean) -> Unit) {
    watchReducedMotionJs { listener(it) }
}

private fun watchReducedMotionJs(callback: (Boolean) -> Unit): Unit =
    js("window.matchMedia('(prefers-reduced-motion: reduce)').addEventListener('change', function (e) { callback(e.matches); })")
