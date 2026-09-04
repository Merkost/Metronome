package com.merkost.metronome.platform

// Browsers have no Live Activity surface; the observer still resolves this.
class WasmLiveActivityController : LiveActivityController {
    override fun start(snapshot: LiveActivitySnapshot) = Unit
    override fun update(snapshot: LiveActivitySnapshot) = Unit
    override fun end() = Unit
}
