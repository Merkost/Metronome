package com.merkost.metronome.engine

import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.ClickSound
import com.merkost.metronome.platform.AudioFocusController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SoundPreviewControllerTest {
    @Test
    fun playsOneBarWithIndependentAccentAndReleasesResources() = runTest {
        val player = FakePlayer()
        val focus = FakeFocus()
        val controller = DefaultSoundPreviewController(player, focus, { false }, this)

        controller.preview(ClickSound.SOFT)
        runCurrent()
        assertEquals(ClickSound.SOFT, controller.activeSound.value)
        assertEquals(listOf(Beat.HIGH), player.beats.map { it.beat })
        advanceTimeBy(1_799)
        runCurrent()
        assertEquals(3, player.beats.size)
        advanceUntilIdle()

        assertEquals(listOf(Beat.HIGH, Beat.LOW, Beat.LOW, Beat.LOW), player.beats.map { it.beat })
        assertEquals(2_400L, testScheduler.currentTime)
        assertNull(controller.activeSound.value)
        assertEquals(1, player.releases)
        assertEquals(1, focus.abandoned)
    }

    @Test
    fun replacementCancelsOldBarWithoutCleaningUpNewAudition() = runTest {
        val player = FakePlayer()
        val controller = DefaultSoundPreviewController(player, FakeFocus(), { false }, this)
        controller.preview(ClickSound.WOOD)
        runCurrent()
        advanceTimeBy(300)
        controller.preview(ClickSound.STUDIO)
        runCurrent()

        assertEquals(ClickSound.STUDIO, controller.activeSound.value)
        assertEquals(1, player.releases)
        advanceUntilIdle()

        assertEquals(listOf(ClickSound.WOOD) + List(4) { ClickSound.STUDIO }, player.beats.map { it.sound })
        assertEquals(2, player.releases)
        assertNull(controller.activeSound.value)
    }

    @Test
    fun stopClearsStateAndCancelsEveryRemainingBeat() = runTest {
        val player = FakePlayer()
        val focus = FakeFocus()
        val controller = DefaultSoundPreviewController(player, focus, { false }, this)
        controller.preview(ClickSound.RIM)
        runCurrent()

        controller.stop()
        advanceUntilIdle()
        controller.stop()

        assertEquals(1, player.beats.size)
        assertEquals(1, player.releases)
        assertEquals(1, focus.abandoned)
        assertNull(controller.activeSound.value)
    }

    @Test
    fun refusesToPreviewDuringPlaybackOrWhenFocusIsDenied() = runTest {
        val player = FakePlayer()
        val focus = FakeFocus()
        var playing = true
        val controller = DefaultSoundPreviewController(player, focus, { playing }, this)
        controller.preview(ClickSound.CLAVE)
        runCurrent()
        assertEquals(0, focus.requests)
        assertEquals("Pause playback to preview a sound.", controller.errorMessage.value)

        playing = false
        focus.granted = false
        controller.preview(ClickSound.CLAVE)
        advanceUntilIdle()

        assertTrue(player.sounds.isEmpty())
        assertNull(controller.activeSound.value)
        assertEquals(0, focus.abandoned)
        assertNotNull(controller.errorMessage.value)
    }

    @Test
    fun mainPlaybackCancelsAuditionBeforeAnotherClick() = runTest {
        val player = FakePlayer()
        var playing = false
        val controller = DefaultSoundPreviewController(player, FakeFocus(), { playing }, this)
        controller.preview(ClickSound.SOFT)
        runCurrent()
        playing = true
        advanceUntilIdle()

        assertEquals(1, player.beats.size)
        assertEquals(1, player.releases)
        assertTrue(playing)
        assertNull(controller.activeSound.value)
    }

    @Test
    fun focusLossStopsAuditionImmediately() = runTest {
        val player = FakePlayer()
        val focus = FakeFocus()
        val controller = DefaultSoundPreviewController(player, focus, { false }, this)
        controller.preview(ClickSound.CLASSIC)
        runCurrent()
        focus.onLost?.invoke()
        advanceUntilIdle()

        assertEquals(1, player.beats.size)
        assertEquals(1, player.releases)
        assertNull(controller.activeSound.value)
    }

    @Test
    fun previewClampsLevelsWithoutMutatingPlaybackState() = runTest {
        val player = FakePlayer()
        var playing = false
        val controller = DefaultSoundPreviewController(player, FakeFocus(), { playing }, this)
        controller.preview(ClickSound.CLICK, bpm = 100, volume = 2f, pan = 10)
        advanceUntilIdle()

        assertTrue(player.beats.all { it.left == 0f && it.right == 1f })
        assertFalse(playing)
        controller.preview(ClickSound.CLICK, volume = Float.NaN, pan = -10)
        advanceUntilIdle()
        assertTrue(player.beats.takeLast(4).all { it.left == 0f && it.right == 0f })
    }

    @Test
    fun failedInitializationReleasesFocusAndDoesNotLeakActiveState() = runTest {
        val player = FakePlayer(failInitialization = true)
        val focus = FakeFocus()
        val failures = mutableListOf<Throwable>()
        val controller = DefaultSoundPreviewController(player, focus, { false }, this, failures::add)
        controller.preview(ClickSound.WOOD)
        advanceUntilIdle()

        assertEquals(1, failures.size)
        assertNotNull(controller.errorMessage.value)
        assertEquals(1, player.releases)
        assertEquals(1, focus.abandoned)
        assertNull(controller.activeSound.value)
    }

    @Test
    fun outputUpdatesChangeFollowingClicksWithoutRestartingBarOrAudioFocus() = runTest {
        val player = FakePlayer()
        val focus = FakeFocus()
        val controller = DefaultSoundPreviewController(player, focus, { false }, this)
        controller.preview(ClickSound.SOFT, volume = 0.8f)
        runCurrent()
        advanceTimeBy(300)

        controller.updateOutput(volume = 0.4f, pan = -5)
        assertEquals(ClickSound.SOFT, controller.activeSound.value)
        advanceUntilIdle()

        assertEquals(4, player.beats.size)
        assertEquals(0.8f, player.beats.first().left)
        assertEquals(0.8f, player.beats.first().right)
        assertTrue(player.beats.drop(1).all { it.left == 0.4f && it.right == 0f })
        assertEquals(listOf(Beat.HIGH, Beat.LOW, Beat.LOW, Beat.LOW), player.beats.map { it.beat })
        assertEquals(1, focus.requests)
        assertEquals(1, focus.abandoned)
        assertEquals(1, player.releases)
        assertEquals(2_400L, testScheduler.currentTime)
    }

    @Test
    fun asyncResourceFailureIsVisibleAndStopsEveryRemainingClick() = runTest {
        val player = FakePlayer()
        val focus = FakeFocus()
        val failures = mutableListOf<Throwable>()
        val controller = DefaultSoundPreviewController(player, focus, { false }, this, failures::add)
        controller.preview(ClickSound.SOFT)
        runCurrent()
        player.failLoad()
        runCurrent()
        advanceUntilIdle()

        assertEquals(1, player.beats.size)
        assertEquals(1, player.releases)
        assertEquals(1, focus.abandoned)
        assertEquals(1, failures.size)
        assertNotNull(controller.errorMessage.value)
        assertNull(controller.activeSound.value)
    }

    @Test
    fun aNewPreviewClearsThePreviousFailureAndCanPlayNormally() = runTest {
        val player = FakePlayer(failInitialization = true)
        val controller = DefaultSoundPreviewController(player, FakeFocus(), { false }, this)
        controller.preview(ClickSound.WOOD)
        advanceUntilIdle()
        assertNotNull(controller.errorMessage.value)

        player.failInitialization = false
        controller.preview(ClickSound.STUDIO)
        assertNull(controller.errorMessage.value)
        runCurrent()
        assertEquals(ClickSound.STUDIO, controller.activeSound.value)
        advanceUntilIdle()

        assertEquals(4, player.beats.size)
        assertNull(controller.errorMessage.value)
    }

    @Test
    fun loadFailureAfterStopCannotLeakIntoANewPreview() = runTest {
        val player = FakePlayer()
        val controller = DefaultSoundPreviewController(player, FakeFocus(), { false }, this)
        controller.preview(ClickSound.RIM)
        runCurrent()
        controller.stop()
        runCurrent()
        player.failLoad()
        controller.preview(ClickSound.CLAVE)
        advanceUntilIdle()

        assertEquals(4, player.beats.count { it.sound == ClickSound.CLAVE })
        assertNull(controller.errorMessage.value)
    }

    private data class Playback(val sound: ClickSound, val beat: Beat, val left: Float, val right: Float)

    private class FakePlayer(var failInitialization: Boolean = false) : MetronomePlayer {
        private val mutableFailures = MutableSharedFlow<Throwable>(extraBufferCapacity = 4)
        override val failures: Flow<Throwable> = mutableFailures.asSharedFlow()
        fun failLoad() { mutableFailures.tryEmit(IllegalStateException("Failed to decode resource")) }
        val sounds = mutableListOf<ClickSound>()
        val beats = mutableListOf<Playback>()
        var releases = 0
        private var sound = ClickSound.WOOD

        override fun initialize(initialSound: ClickSound) {
            if (failInitialization) error("Unavailable audio")
            sound = initialSound
            sounds += initialSound
        }

        override fun play(beat: Beat, stereoLeft: Float, stereoRight: Float) {
            beats += Playback(sound, beat, stereoLeft, stereoRight)
        }

        override fun stop() = Unit
        override fun release() { releases++ }
        override fun switchSound(sound: ClickSound) { this.sound = sound }
    }

    private class FakeFocus : AudioFocusController {
        var onLost: (() -> Unit)? = null
        var granted = true
        var requests = 0
        var abandoned = 0

        override fun setOnFocusLost(onLost: () -> Unit) { this.onLost = onLost }
        override fun requestFocus(): Boolean { requests++; return granted }
        override fun abandonFocus() { abandoned++ }
    }
}
