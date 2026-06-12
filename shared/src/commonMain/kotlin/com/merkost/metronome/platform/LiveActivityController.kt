package com.merkost.metronome.platform

enum class TimerKind { NONE, STOPWATCH, COUNTDOWN }

data class LiveActivitySnapshot(
    val isPlaying: Boolean,
    val bpm: Int,
    val tempoName: String,
    val timeSignatureLabel: String,
    val timerKind: TimerKind,
    val timerStartEpochMillis: Long?,
    val timerEndEpochMillis: Long?,
    val timerFrozenMillis: Long?,
)

interface LiveActivityController {
    fun start(snapshot: LiveActivitySnapshot)
    fun update(snapshot: LiveActivitySnapshot)
    fun end()
}
