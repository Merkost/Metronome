package com.merkost.metronome.model

data class GapTrainerConfig(
    val playBars: Int = 4,
    val muteBars: Int = 2,
) {
    val cycleBars: Int get() = playBars + muteBars

    fun isMuted(barIndex: Int): Boolean = barIndex % cycleBars >= playBars

    fun barInPhase(barIndex: Int): Int {
        val inCycle = barIndex % cycleBars
        return if (isMuted(barIndex)) inCycle - playBars + 1 else inCycle + 1
    }

    fun phaseLength(barIndex: Int): Int = if (isMuted(barIndex)) muteBars else playBars

    fun cycleProgress(barIndex: Int): Float = (barIndex % cycleBars + 1f) / cycleBars
}
