package com.merkost.metronome.engine

import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.ClickSound
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioFile
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioPlayerNode
import platform.AVFAudio.AVAudioPlayerNodeBufferInterrupts
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVAudioUnitVarispeed
import platform.AVFAudio.setActive
import platform.Foundation.NSBundle
import kotlin.math.max

@OptIn(ExperimentalForeignApi::class)
class MetronomePlayerIos : MetronomePlayer {
    private var audioEngine: AVAudioEngine? = null
    private var playerNode: AVAudioPlayerNode? = null
    private var varispeedNode: AVAudioUnitVarispeed? = null
    private var audioBuffer: AVAudioPCMBuffer? = null

    override fun initialize() {
        AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, error = null)
        AVAudioSession.sharedInstance().setActive(true, error = null)

        val url = NSBundle.mainBundle.URLForResource("wood", withExtension = "mp3") ?: return
        val audioFile = AVAudioFile(forReading = url, error = null)

        val frameCount = audioFile.length.toUInt()
        val buffer = AVAudioPCMBuffer(
            pCMFormat = audioFile.processingFormat,
            frameCapacity = frameCount
        )
        audioFile.readIntoBuffer(buffer, error = null)
        audioBuffer = buffer

        val engine = AVAudioEngine()
        val player = AVAudioPlayerNode()
        val varispeed = AVAudioUnitVarispeed()

        engine.attachNode(player)
        engine.attachNode(varispeed)

        val format = audioFile.processingFormat
        engine.connect(player, varispeed, format)
        engine.connect(varispeed, engine.mainMixerNode, format)

        engine.startAndReturnError(null)
        player.play()

        audioEngine = engine
        playerNode = player
        varispeedNode = varispeed
    }

    override fun play(beat: Beat, stereoLeft: Float, stereoRight: Float) {
        val buffer = audioBuffer ?: return
        val player = playerNode ?: return

        varispeedNode?.rate = beat.rate

        val volume = max(stereoLeft, stereoRight)
        val pan = if (stereoLeft + stereoRight > 0f) {
            (stereoRight - stereoLeft) / max(stereoLeft, stereoRight)
        } else {
            0f
        }

        player.volume = volume
        player.pan = pan

        if (!player.playing) {
            player.play()
        }

        player.scheduleBuffer(
            buffer,
            atTime = null,
            options = AVAudioPlayerNodeBufferInterrupts,
            completionHandler = null
        )
    }

    override fun stop() {
        playerNode?.stop()
    }

    override fun release() {
        playerNode?.stop()
        audioEngine?.stop()
        audioEngine = null
        playerNode = null
        varispeedNode = null
        audioBuffer = null
    }

    private fun soundFileInfo(sound: ClickSound): Pair<String, String> = when (sound) {
        ClickSound.WOOD -> "wood" to "mp3"
        ClickSound.CLICK -> "click" to "mp3"
        ClickSound.CLASSIC -> "metronome" to "wav"
    }

    override fun switchSound(sound: ClickSound) {
        val (name, ext) = soundFileInfo(sound)
        val url = NSBundle.mainBundle.URLForResource(name, withExtension = ext) ?: return
        val audioFile = AVAudioFile(forReading = url, error = null)
        val frameCount = audioFile.length.toUInt()
        val buffer = AVAudioPCMBuffer(pCMFormat = audioFile.processingFormat, frameCapacity = frameCount)
        audioFile.readIntoBuffer(buffer, error = null)
        playerNode?.stop()
        audioBuffer = buffer
        playerNode?.play()
    }
}
