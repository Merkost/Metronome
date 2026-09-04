package com.merkost.metronome.engine

import com.merkost.metronome.model.ClickSound

internal data class QueuedSoundPlay(
    val left: Float,
    val right: Float,
    val rate: Float,
)

internal data class ReadySoundPlay(
    val sampleId: Int,
    val play: QueuedSoundPlay,
)

internal sealed interface SoundLoadCompletion {
    data class Activated(
        val sampleId: Int,
        val sampleIdToUnload: Int?,
        val queuedPlay: ReadySoundPlay?,
    ) : SoundLoadCompletion

    data class Failed(val sampleId: Int) : SoundLoadCompletion
    data class Stale(val sampleId: Int) : SoundLoadCompletion
}

internal class SoundLoadState {
    var activeSound: ClickSound? = null
        private set
    var activeSampleId: Int? = null
        private set
    var requestedSound: ClickSound? = null
        private set

    private var pendingSound: ClickSound? = null
    private var pendingSampleId: Int? = null
    private var pendingPlay: QueuedSoundPlay? = null

    fun shouldLoad(sound: ClickSound): Boolean = requestedSound != sound

    fun beginLoading(sound: ClickSound, sampleId: Int): Int? {
        val previousPending = pendingSampleId
        requestedSound = sound
        pendingSound = sound
        pendingSampleId = sampleId
        return previousPending?.takeUnless { it == sampleId }
    }

    fun cancelPendingIfActive(sound: ClickSound): Int? {
        if (activeSound != sound) return null
        val cancelled = pendingSampleId
        pendingSound = null
        pendingSampleId = null
        requestedSound = activeSound
        return cancelled
    }

    fun complete(sampleId: Int, succeeded: Boolean): SoundLoadCompletion {
        if (sampleId != pendingSampleId) return SoundLoadCompletion.Stale(sampleId)
        val completedSound = pendingSound
        pendingSound = null
        pendingSampleId = null
        if (!succeeded || completedSound == null) {
            requestedSound = activeSound
            return SoundLoadCompletion.Failed(sampleId)
        }

        val previousActive = activeSampleId
        activeSound = completedSound
        activeSampleId = sampleId
        requestedSound = completedSound
        val queued = pendingPlay?.let { ReadySoundPlay(sampleId, it) }
        pendingPlay = null
        return SoundLoadCompletion.Activated(
            sampleId = sampleId,
            sampleIdToUnload = previousActive?.takeUnless { it == sampleId },
            queuedPlay = queued,
        )
    }

    fun playOrQueue(play: QueuedSoundPlay): ReadySoundPlay? {
        val sampleId = activeSampleId
        if (sampleId != null) return ReadySoundPlay(sampleId, play)
        pendingPlay = play
        return null
    }

    fun reset() {
        activeSound = null
        activeSampleId = null
        requestedSound = null
        pendingSound = null
        pendingSampleId = null
        pendingPlay = null
    }
}
