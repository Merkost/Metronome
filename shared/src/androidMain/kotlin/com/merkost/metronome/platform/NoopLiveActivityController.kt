package com.merkost.metronome.platform

class NoopLiveActivityController : LiveActivityController {
    override fun start(snapshot: LiveActivitySnapshot) {}
    override fun update(snapshot: LiveActivitySnapshot) {}
    override fun end() {}
}
