package com.merkost.metronome.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.merkost.metronome.R
import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.ClickSound

class MetronomePlayerAndroid(private val context: Context) : MetronomePlayer {
    @Volatile
    private var soundPool: SoundPool? = null
    @Volatile
    private var activeSoundId: Int = 0
    @Volatile
    private var pendingSoundId: Int = 0
    @Volatile
    private var requestedSound: ClickSound? = null
    @Volatile
    private var pendingPlay: PendingPlay? = null

    private data class PendingPlay(val left: Float, val right: Float, val rate: Float)

    override fun initialize(initialSound: ClickSound) {
        val pool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            ).build()
        pool.setOnLoadCompleteListener { loadedPool, sampleId, status ->
            if (status != 0) return@setOnLoadCompleteListener
            if (sampleId == pendingSoundId) {
                val replacing = activeSoundId
                activeSoundId = sampleId
                pendingSoundId = 0
                if (replacing != 0 && replacing != sampleId) loadedPool.unload(replacing)
            } else if (activeSoundId == 0) {
                activeSoundId = sampleId
            }
            pendingPlay?.let { queued ->
                pendingPlay = null
                loadedPool.play(sampleId, queued.left, queued.right, 1, 0, queued.rate)
            }
        }
        soundPool = pool
        requestedSound = initialSound
        pendingSoundId = pool.load(context, soundResource(initialSound), 1)
    }

    override fun play(beat: Beat, stereoLeft: Float, stereoRight: Float) {
        val pool = soundPool ?: return
        val id = activeSoundId
        if (id != 0) {
            pool.play(id, stereoLeft, stereoRight, 1, 0, beat.rate)
        } else {
            pendingPlay = PendingPlay(stereoLeft, stereoRight, beat.rate)
        }
    }

    override fun stop() { /* SoundPool handles this */ }

    override fun release() {
        soundPool?.release()
        soundPool = null
        activeSoundId = 0
        pendingSoundId = 0
        requestedSound = null
        pendingPlay = null
    }

    private fun soundResource(sound: ClickSound): Int = when (sound) {
        ClickSound.WOOD -> R.raw.wood
        ClickSound.CLICK -> R.raw.click
        ClickSound.CLASSIC -> R.raw.metronome
    }

    override fun switchSound(sound: ClickSound) {
        if (sound == requestedSound) return
        val pool = soundPool ?: return
        requestedSound = sound
        val previousPending = pendingSoundId
        if (previousPending != 0) pool.unload(previousPending)
        pendingSoundId = pool.load(context, soundResource(sound), 1)
    }
}
