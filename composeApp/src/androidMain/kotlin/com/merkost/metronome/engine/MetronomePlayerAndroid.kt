package com.merkost.metronome.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import com.merkost.metronome.R
import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.ClickSound

class MetronomePlayerAndroid(private val context: Context) : MetronomePlayer {
    @Volatile
    private var soundPool: SoundPool? = null
    @Volatile
    private var soundId: Int = 0
    @Volatile
    private var soundReady: Boolean = false

    override fun initialize(initialSound: ClickSound) {
        val pool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            ).build()
        pool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) soundReady = true
        }
        soundPool = pool
        soundId = pool.load(context, soundResource(initialSound), 1)
    }

    override fun play(beat: Beat, stereoLeft: Float, stereoRight: Float) {
        if (soundReady) {
            soundPool?.play(soundId, stereoLeft, stereoRight, 1, 0, beat.rate)
        }
    }

    override fun stop() { /* SoundPool handles this */ }

    override fun release() {
        soundPool?.release()
        soundPool = null
        soundReady = false
    }

    private fun soundResource(sound: ClickSound): Int = when (sound) {
        ClickSound.WOOD -> R.raw.wood
        ClickSound.CLICK -> R.raw.click
        ClickSound.CLASSIC -> R.raw.metronome
    }

    override fun switchSound(sound: ClickSound) {
        soundReady = false
        soundPool?.unload(soundId)
        soundId = soundPool?.load(context, soundResource(sound), 1) ?: 0
    }
}
