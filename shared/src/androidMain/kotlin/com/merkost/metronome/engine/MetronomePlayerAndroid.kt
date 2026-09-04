package com.merkost.metronome.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.merkost.metronome.R
import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.ClickSound

class MetronomePlayerAndroid(private val context: Context) : MetronomePlayer {
    private val lock = Any()
    private val loadState = SoundLoadState()
    private var soundPool: SoundPool? = null

    override fun initialize(initialSound: ClickSound) {
        synchronized(lock) {
            soundPool?.release()
            loadState.reset()
            val pool = SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                ).build()
            pool.setOnLoadCompleteListener { loadedPool, sampleId, status ->
                synchronized(lock) {
                    if (soundPool === loadedPool) {
                        handleLoadCompletion(loadedPool, sampleId, status == 0)
                    }
                }
            }
            soundPool = pool
            val sampleId = pool.load(context, soundResource(initialSound), 1)
            loadState.beginLoading(initialSound, sampleId)
        }
    }

    override fun play(beat: Beat, stereoLeft: Float, stereoRight: Float) {
        synchronized(lock) {
            val pool = soundPool ?: return
            loadState.playOrQueue(
                QueuedSoundPlay(
                    left = stereoLeft,
                    right = stereoRight,
                    rate = beat.rate,
                )
            )?.let { ready -> pool.playReady(ready) }
        }
    }

    override fun stop() {}

    override fun release() {
        synchronized(lock) {
            soundPool?.release()
            soundPool = null
            loadState.reset()
        }
    }

    override fun switchSound(sound: ClickSound) {
        synchronized(lock) {
            val pool = soundPool ?: return
            if (!loadState.shouldLoad(sound)) return
            val cancelled = loadState.cancelPendingIfActive(sound)
            if (loadState.activeSound == sound) {
                cancelled?.let(pool::unload)
                return
            }
            val sampleId = pool.load(context, soundResource(sound), 1)
            loadState.beginLoading(sound, sampleId)?.let(pool::unload)
        }
    }

    private fun handleLoadCompletion(pool: SoundPool, sampleId: Int, succeeded: Boolean) {
        when (val completion = loadState.complete(sampleId, succeeded)) {
            is SoundLoadCompletion.Activated -> {
                completion.sampleIdToUnload?.let(pool::unload)
                completion.queuedPlay?.let { ready -> pool.playReady(ready) }
            }
            is SoundLoadCompletion.Failed -> pool.unload(completion.sampleId)
            is SoundLoadCompletion.Stale -> pool.unload(completion.sampleId)
        }
    }

    private fun SoundPool.playReady(ready: ReadySoundPlay) {
        play(
            ready.sampleId,
            ready.play.left,
            ready.play.right,
            1,
            0,
            ready.play.rate,
        )
    }

    private fun soundResource(sound: ClickSound): Int = when (sound) {
        ClickSound.WOOD -> R.raw.wood
        ClickSound.CLICK -> R.raw.click
        ClickSound.CLASSIC -> R.raw.metronome
    }
}
