package com.merkost.metronome.engine

import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.ClickSound
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class DesktopPcmTest {
    @Test
    fun monoWaveDecodesToStereoWithoutChangingItsLevel() {
        val wave = ByteArrayOutputStream()
        val pcm = byteArrayOf(0, 64, 0, -64, 0, 32)
        AudioInputStream(ByteArrayInputStream(pcm), AudioFormat(48_000f, 16, 1, true, false), 3).use {
            AudioSystem.write(it, AudioFileFormat.Type.WAVE, wave)
        }
        val clip = DesktopPcmDecoder.decode(ByteArrayInputStream(wave.toByteArray()))
        assertEquals(3, clip.frames)
        assertEquals(0.5f, clip.samples[0], 0.0001f)
        assertEquals(clip.samples[0], clip.samples[1])
        assertEquals(-0.5f, clip.samples[2], 0.0001f)
        assertEquals(clip.samples[2], clip.samples[3])
    }

    @Test
    fun mixerKeepsHardPannedChannelsSeparateAndClampsOverlappingPeaks() {
        val mixer = DesktopPcmMixer()
        val clip = DesktopPcmClip(floatArrayOf(0.75f, 0.75f, -0.75f, -0.75f))
        mixer.enqueue(clip, 1f, 1f, 0f, 1L, null)
        mixer.enqueue(clip, 1f, 1f, 0f, 1L, null)
        val block = mixer.render(8, 1L)
        assertEquals(2, block.frames)
        assertEquals(32767, sample(block.bytes, 0))
        assertEquals(0, sample(block.bytes, 1))
        assertEquals(-32768, sample(block.bytes, 2))
        assertEquals(0, sample(block.bytes, 3))
    }

    @Test
    fun changingGenerationDiscardsUnsubmittedAudioAndPulseCallbacks() {
        val mixer = DesktopPcmMixer()
        mixer.enqueue(DesktopPcmClip(FloatArray(32) { 0.5f }), 1f, 1f, 1f, 4L) {}
        val block = mixer.render(8, 5L)
        assertEquals(0, block.frames)
        assertTrue(block.bytes.isEmpty())
        assertTrue(block.submissions.isEmpty())
    }

    @Test
    fun aVoiceProducesOnlyOneSubmissionAndPitchChangesItsDuration() {
        val mixer = DesktopPcmMixer()
        val clip = DesktopPcmClip(FloatArray(32) { 0.5f })
        mixer.enqueue(clip, 2f, 0f, 0.5f, 1L) {}
        val first = mixer.render(4, 1L)
        val last = mixer.render(20, 1L)
        assertEquals(4, first.frames)
        assertEquals(4, last.frames)
        assertEquals(1, first.submissions.size)
        assertTrue(last.submissions.isEmpty())
        assertEquals(0, sample(first.bytes, 0))
        assertEquals(8192, sample(first.bytes, 1))
    }

    @Test
    fun everyBundledSoundDecodesAndNewSoundsUseTheirDistinctAccent() {
        val bank = BundledDesktopSounds()
        ClickSound.entries.forEach { sound ->
            val loaded = bank.load(sound)
            assertTrue(loaded.normal.frames > 0, sound.name)
            assertTrue(loaded.normal.samples.all { it.isFinite() && it in -1f..1f }, sound.name)
            val high = loaded.forBeat(Beat.HIGH)
            if (sound in setOf(ClickSound.WOOD, ClickSound.CLICK, ClickSound.CLASSIC)) {
                assertEquals(1.4f, high.rate)
                assertSame(loaded.normal, high.clip)
            } else {
                assertEquals(1f, high.rate)
                assertTrue(high.clip.frames > 0)
                assertNotEquals(loaded.normal.samples.toList(), high.clip.samples.toList(), sound.name)
            }
            assertSame(loaded, bank.load(sound))
        }
    }

    private fun sample(bytes: ByteArray, index: Int): Int =
        ((bytes[index * 2].toInt() and 255) or (bytes[index * 2 + 1].toInt() shl 8)).toShort().toInt()
}
