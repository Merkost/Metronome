package com.merkost.metronome.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import com.merkost.metronome.R
import com.merkost.metronome.model.Beat

class MetronomePlayerAndroid(private val context: Context) : MetronomePlayer {
    private var soundPool: SoundPool? = null
    private var soundId: Int = 0

    override fun initialize() {
        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                    .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
                    .setUsage(AudioAttributes.USAGE_ASSISTANT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            ).build()
        soundId = soundPool!!.load(context, R.raw.wood, 3)
    }

    override fun play(beat: Beat, stereoLeft: Float, stereoRight: Float) {
        soundPool?.play(soundId, stereoLeft, stereoRight, 1, 0, beat.rate)
    }

    override fun stop() { /* SoundPool handles this */ }

    override fun release() {
        soundPool?.release()
        soundPool = null
    }
}
