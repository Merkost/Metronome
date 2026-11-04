package com.merkost.metronome.engine

import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.ClickSound
import java.io.BufferedInputStream
import java.io.InputStream
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

internal val DesktopAudioFormat = AudioFormat(48_000f, 16, 2, true, false)

internal data class DesktopPcmClip(val samples: FloatArray) {
    val frames: Int get() = samples.size / 2
}

internal data class DesktopSoundPlayback(val clip: DesktopPcmClip, val rate: Float)

internal data class DesktopSoundSamples(val normal: DesktopPcmClip, val accent: DesktopPcmClip?) {
    fun forBeat(beat: Beat): DesktopSoundPlayback = if (beat == Beat.HIGH && accent != null) {
        DesktopSoundPlayback(accent, 1f)
    } else {
        DesktopSoundPlayback(normal, beat.rate)
    }
}

internal object DesktopPcmDecoder {
    fun decode(input: InputStream): DesktopPcmClip = BufferedInputStream(input).use { buffered ->
        AudioSystem.getAudioInputStream(buffered).use { source ->
            AudioSystem.getAudioInputStream(DesktopAudioFormat, source).use { decoded ->
                val bytes = decoded.readAllBytes()
                require(bytes.isNotEmpty() && bytes.size % DesktopAudioFormat.frameSize == 0) {
                    "This click sound could not be decoded. Try another sound."
                }
                DesktopPcmClip(FloatArray(bytes.size / 2) { index ->
                    val value = (bytes[index * 2].toInt() and 255) or (bytes[index * 2 + 1].toInt() shl 8)
                    value.toShort().toFloat() / 32768f
                })
            }
        }
    }
}

internal data class DesktopPcmBlock(val bytes: ByteArray, val frames: Int, val submissions: List<() -> Unit>)

internal class DesktopPcmMixer {
    private data class Voice(
        val clip: DesktopPcmClip,
        val rate: Double,
        val left: Float,
        val right: Float,
        val generation: Long,
        var position: Double = 0.0,
        var onSubmitted: (() -> Unit)?,
    )

    private val voices = mutableListOf<Voice>()
    val hasVoices: Boolean get() = voices.isNotEmpty()

    fun enqueue(clip: DesktopPcmClip, rate: Float, left: Float, right: Float, generation: Long, onSubmitted: (() -> Unit)?) {
        if (clip.frames == 0) return
        if (voices.size >= 64) voices.removeAt(0)
        voices += Voice(
            clip = clip,
            rate = rate.takeIf { it.isFinite() && it > 0f }?.toDouble() ?: 1.0,
            left = left.takeIf(Float::isFinite)?.coerceIn(0f, 1f) ?: 0f,
            right = right.takeIf(Float::isFinite)?.coerceIn(0f, 1f) ?: 0f,
            generation = generation,
            onSubmitted = onSubmitted,
        )
    }

    fun render(frames: Int, generation: Long): DesktopPcmBlock {
        voices.removeAll { it.generation != generation || it.position >= it.clip.frames }
        val count = min(frames.coerceAtLeast(0), voices.maxOfOrNull { ceil((it.clip.frames - it.position) / it.rate).toInt() } ?: 0)
        if (count == 0) return DesktopPcmBlock(byteArrayOf(), 0, emptyList())
        val mixed = FloatArray(count * 2)
        val submissions = mutableListOf<() -> Unit>()
        voices.forEach { voice ->
            voice.onSubmitted?.let(submissions::add)
            voice.onSubmitted = null
            for (frame in 0 until count) {
                if (voice.position >= voice.clip.frames) break
                val sourceFrame = voice.position.toInt()
                val nextFrame = min(sourceFrame + 1, voice.clip.frames - 1)
                val fraction = (voice.position - sourceFrame).toFloat()
                for (channel in 0..1) {
                    val sample = voice.clip.samples[sourceFrame * 2 + channel]
                    val next = voice.clip.samples[nextFrame * 2 + channel]
                    mixed[frame * 2 + channel] += (sample + (next - sample) * fraction) * if (channel == 0) voice.left else voice.right
                }
                voice.position += voice.rate
            }
        }
        voices.removeAll { it.position >= it.clip.frames }
        val bytes = ByteArray(count * 4)
        mixed.forEachIndexed { index, value ->
            val sample = (value.coerceIn(-1f, 1f) * 32768f).roundToInt().coerceIn(-32768, 32767)
            bytes[index * 2] = sample.toByte()
            bytes[index * 2 + 1] = (sample shr 8).toByte()
        }
        return DesktopPcmBlock(bytes, count, submissions)
    }

    fun clear() = voices.clear()
}

internal class BundledDesktopSounds : DesktopSoundBank {
    private val sounds = mutableMapOf<ClickSound, DesktopSoundSamples>()

    @Synchronized
    override fun load(sound: ClickSound): DesktopSoundSamples = sounds.getOrPut(sound) {
        val name = when (sound) {
            ClickSound.WOOD -> "wood"
            ClickSound.CLICK -> "click"
            ClickSound.CLASSIC -> "metronome"
            ClickSound.SOFT -> "soft"
            ClickSound.RIM -> "rim"
            ClickSound.CLAVE -> "clave"
            ClickSound.STUDIO -> "studio"
        }
        DesktopSoundSamples(
            normal = loadResource(name),
            accent = when (sound) {
                ClickSound.WOOD, ClickSound.CLICK, ClickSound.CLASSIC -> null
                else -> loadResource("${name}_accent")
            },
        )
    }

    private fun loadResource(name: String): DesktopPcmClip {
        val stream = checkNotNull(javaClass.getResourceAsStream("/audio/$name.wav")) {
            "This click sound is unavailable. Try another sound or reinstall Metronome."
        }
        return DesktopPcmDecoder.decode(stream)
    }
}
