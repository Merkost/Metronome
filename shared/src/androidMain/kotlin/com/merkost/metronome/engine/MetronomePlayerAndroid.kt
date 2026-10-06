package com.merkost.metronome.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.merkost.metronome.R
import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.ClickSound
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class MetronomePlayerAndroid(private val context: Context) : MetronomePlayer {
    private val mutableFailures = MutableSharedFlow<Throwable>(extraBufferCapacity = 4)
    override val failures: Flow<Throwable> = mutableFailures.asSharedFlow()
    private val lock = Any()
    private val loadState = SoundLoadState()
    private val accentLoadState = SoundLoadState()
    private val loadOwners = mutableMapOf<Int, SoundLoadState>()
    private val streams = ArrayDeque<Int>()
    private var soundPool: SoundPool? = null

    override fun initialize(initialSound: ClickSound) {
        synchronized(lock) {
            soundPool?.release()
            loadState.reset()
            accentLoadState.reset()
            loadOwners.clear()
            streams.clear()
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
            loadSound(pool, loadState, initialSound, accented = false)
            loadSound(pool, accentLoadState, initialSound, accented = true)
        }
    }

    override fun play(beat: Beat, stereoLeft: Float, stereoRight: Float) {
        if (beat == Beat.MUTE) return
        synchronized(lock) {
            val pool = soundPool ?: return
            val state = if (beat == Beat.HIGH) accentLoadState else loadState
            val rate = if (beat == Beat.HIGH && state.activeSound?.hasAccentResource == true) 1f else beat.rate
            state.playOrQueue(
                QueuedSoundPlay(
                    left = stereoLeft,
                    right = stereoRight,
                    rate = rate,
                )
            )?.let { ready -> pool.playReady(ready) }
        }
    }

    override fun stop() {
        synchronized(lock) {
            val pool = soundPool
            streams.forEach { pool?.stop(it) }
            streams.clear()
            loadState.clearQueuedPlay()
            accentLoadState.clearQueuedPlay()
        }
    }

    override fun release() {
        synchronized(lock) {
            soundPool?.release()
            soundPool = null
            loadState.reset()
            accentLoadState.reset()
            loadOwners.clear()
            streams.clear()
        }
    }

    override fun switchSound(sound: ClickSound) {
        synchronized(lock) {
            val pool = soundPool ?: return
            loadSound(pool, loadState, sound, accented = false)
            loadSound(pool, accentLoadState, sound, accented = true)
        }
    }

    private fun loadSound(pool: SoundPool, state: SoundLoadState, sound: ClickSound, accented: Boolean) {
        if (!state.shouldLoad(sound)) return
        val cancelled = state.cancelPendingIfActive(sound)
        if (state.activeSound == sound) {
            cancelled?.let { loadOwners.remove(it); pool.unload(it) }
            return
        }
        val sampleId = pool.load(context, soundResource(sound, accented), 1)
        if (sampleId == 0) {
            mutableFailures.tryEmit(IllegalStateException("Unable to load ${sound.displayName}"))
            return
        }
        loadOwners[sampleId] = state
        state.beginLoading(sound, sampleId)?.let { loadOwners.remove(it); pool.unload(it) }
    }

    private fun handleLoadCompletion(pool: SoundPool, sampleId: Int, succeeded: Boolean) {
        val state = loadOwners.remove(sampleId) ?: return
        when (val completion = state.complete(sampleId, succeeded)) {
            is SoundLoadCompletion.Activated -> {
                completion.sampleIdToUnload?.let(pool::unload)
                completion.queuedPlay?.let { ready ->
                    val playback = if (state === accentLoadState && state.activeSound?.hasAccentResource == true) {
                        ready.copy(play = ready.play.copy(rate = 1f))
                    } else {
                        ready
                    }
                    pool.playReady(playback)
                }
            }
            is SoundLoadCompletion.Failed -> {
                pool.unload(completion.sampleId)
                mutableFailures.tryEmit(IllegalStateException("Sound resource could not be loaded"))
            }
            is SoundLoadCompletion.Stale -> pool.unload(completion.sampleId)
        }
    }

    private fun SoundPool.playReady(ready: ReadySoundPlay) {
        val stream = play(
            ready.sampleId,
            ready.play.left,
            ready.play.right,
            1,
            0,
            ready.play.rate,
        )
        if (stream != 0) {
            streams.addLast(stream)
            if (streams.size > 4) streams.removeFirst()
        } else {
            mutableFailures.tryEmit(IllegalStateException("Sound could not start"))
        }
    }

    private val ClickSound.hasAccentResource: Boolean
        get() = when (this) {
            ClickSound.WOOD, ClickSound.CLICK, ClickSound.CLASSIC -> false
            ClickSound.SOFT, ClickSound.RIM, ClickSound.CLAVE, ClickSound.STUDIO -> true
        }

    private fun soundResource(sound: ClickSound, accented: Boolean): Int = when (sound) {
        ClickSound.WOOD -> R.raw.wood
        ClickSound.CLICK -> R.raw.click
        ClickSound.CLASSIC -> R.raw.metronome
        ClickSound.SOFT -> if (accented) R.raw.soft_accent else R.raw.soft
        ClickSound.RIM -> if (accented) R.raw.rim_accent else R.raw.rim
        ClickSound.CLAVE -> if (accented) R.raw.clave_accent else R.raw.clave
        ClickSound.STUDIO -> if (accented) R.raw.studio_accent else R.raw.studio
    }
}
