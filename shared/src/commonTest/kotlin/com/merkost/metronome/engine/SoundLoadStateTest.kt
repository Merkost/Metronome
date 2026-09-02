package com.merkost.metronome.engine

import com.merkost.metronome.model.ClickSound
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SoundLoadStateTest {
    private val firstBeat = QueuedSoundPlay(left = 1f, right = 0.5f, rate = 1.2f)

    @Test
    fun staleLoadCompletionCannotActivateAnObsoleteSound() {
        val state = SoundLoadState()
        state.beginLoading(ClickSound.WOOD, sampleId = 1)

        assertEquals(1, state.beginLoading(ClickSound.CLICK, sampleId = 2))
        assertIs<SoundLoadCompletion.Stale>(state.complete(sampleId = 1, succeeded = true))
        assertNull(state.activeSampleId)

        val activated = assertIs<SoundLoadCompletion.Activated>(
            state.complete(sampleId = 2, succeeded = true),
        )
        assertEquals(2, activated.sampleId)
        assertEquals(ClickSound.CLICK, state.activeSound)
    }

    @Test
    fun currentSoundRemainsPlayableUntilReplacementIsReady() {
        val state = SoundLoadState()
        state.beginLoading(ClickSound.WOOD, sampleId = 1)
        state.complete(sampleId = 1, succeeded = true)
        state.beginLoading(ClickSound.CLICK, sampleId = 2)

        assertEquals(1, state.playOrQueue(firstBeat)?.sampleId)

        val activated = assertIs<SoundLoadCompletion.Activated>(
            state.complete(sampleId = 2, succeeded = true),
        )
        assertEquals(1, activated.sampleIdToUnload)
        assertEquals(2, state.playOrQueue(firstBeat)?.sampleId)
    }

    @Test
    fun failedReplacementKeepsCurrentSoundAndAllowsRetry() {
        val state = SoundLoadState()
        state.beginLoading(ClickSound.WOOD, sampleId = 1)
        state.complete(sampleId = 1, succeeded = true)
        state.beginLoading(ClickSound.CLICK, sampleId = 2)

        assertIs<SoundLoadCompletion.Failed>(state.complete(sampleId = 2, succeeded = false))
        assertEquals(ClickSound.WOOD, state.activeSound)
        assertEquals(1, state.playOrQueue(firstBeat)?.sampleId)
        assertTrue(state.shouldLoad(ClickSound.CLICK))
    }

    @Test
    fun latestBeatQueuedBeforeInitialLoadPlaysWhenSoundActivates() {
        val state = SoundLoadState()
        state.beginLoading(ClickSound.WOOD, sampleId = 1)

        assertNull(state.playOrQueue(firstBeat))
        val latestBeat = firstBeat.copy(rate = 0.8f)
        assertNull(state.playOrQueue(latestBeat))

        val activated = assertIs<SoundLoadCompletion.Activated>(
            state.complete(sampleId = 1, succeeded = true),
        )
        assertEquals(ReadySoundPlay(sampleId = 1, play = latestBeat), activated.queuedPlay)
    }

    @Test
    fun selectingActiveSoundCancelsPendingReplacementWithoutReloading() {
        val state = SoundLoadState()
        state.beginLoading(ClickSound.WOOD, sampleId = 1)
        state.complete(sampleId = 1, succeeded = true)
        state.beginLoading(ClickSound.CLICK, sampleId = 2)

        assertEquals(2, state.cancelPendingIfActive(ClickSound.WOOD))
        assertFalse(state.shouldLoad(ClickSound.WOOD))
        assertEquals(ClickSound.WOOD, state.requestedSound)
    }
}
