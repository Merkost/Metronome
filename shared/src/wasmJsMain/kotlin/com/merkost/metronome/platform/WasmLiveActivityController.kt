package com.merkost.metronome.platform

class WasmLiveActivityController : LiveActivityController {
    override fun start(snapshot: LiveActivitySnapshot) = Unit
    override fun update(snapshot: LiveActivitySnapshot) = Unit
    override fun end() = Unit
}
