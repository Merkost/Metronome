package com.merkost.metronome.engine

import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.ClickSound
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.kimplify.cedar.logging.Cedar
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

@OptIn(ExperimentalForeignApi::class, ExperimentalCoroutinesApi::class)
class MetronomePlayerIos : MetronomePlayer {
    private val audioDispatcher = Dispatchers.Default.limitedParallelism(1)
    private val scope = CoroutineScope(audioDispatcher + SupervisorJob())

    private var audioEngine: AVAudioEngine? = null
    private var playerNode: AVAudioPlayerNode? = null
    private var varispeedNode: AVAudioUnitVarispeed? = null
    private var audioBuffer: AVAudioPCMBuffer? = null
    private var requestedSound: ClickSound? = null

    override fun initialize(initialSound: ClickSound) {
        scope.launch { initializeInternal(initialSound) }
    }

    override fun play(beat: Beat, stereoLeft: Float, stereoRight: Float) {
        scope.launch { playInternal(beat, stereoLeft, stereoRight) }
    }

    override fun stop() {
        scope.launch { playerNode?.stop() }
    }

    override fun switchSound(sound: ClickSound) {
        scope.launch { switchSoundInternal(sound) }
    }

    override fun release() {
        scope.launch {
            playerNode?.stop()
            audioEngine?.stop()
            audioEngine = null
            playerNode = null
            varispeedNode = null
            audioBuffer = null
            requestedSound = null
        }
    }

    private fun initializeInternal(initialSound: ClickSound) {
        requestedSound = initialSound
        AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, error = null)
        AVAudioSession.sharedInstance().setActive(true, error = null)

        val (name, ext) = soundFileInfo(initialSound)
        val url = NSBundle.mainBundle.URLForResource(name, withExtension = ext)
        if (url == null) {
            Cedar.tag("MetronomePlayerIos").e("Audio resource not found: $name.$ext")
            return
        }
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

        if (!engine.startAndReturnError(null)) {
            Cedar.tag("MetronomePlayerIos").e("Failed to start audio engine")
            return
        }
        player.play()

        audioEngine = engine
        playerNode = player
        varispeedNode = varispeed
    }

    private fun playInternal(beat: Beat, stereoLeft: Float, stereoRight: Float) {
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

    private fun switchSoundInternal(sound: ClickSound) {
        if (sound == requestedSound) return
        requestedSound = sound

        val (name, ext) = soundFileInfo(sound)
        val url = NSBundle.mainBundle.URLForResource(name, withExtension = ext)
        if (url == null) {
            Cedar.tag("MetronomePlayerIos").e("Audio resource not found: $name.$ext")
            return
        }
        val audioFile = AVAudioFile(forReading = url, error = null)
        val frameCount = audioFile.length.toUInt()
        val newFormat = audioFile.processingFormat
        val buffer = AVAudioPCMBuffer(pCMFormat = newFormat, frameCapacity = frameCount)
        audioFile.readIntoBuffer(buffer, error = null)

        val engine = audioEngine ?: return
        val player = playerNode ?: return
        val varispeed = varispeedNode ?: return

        val wasRunning = engine.running
        player.stop()
        engine.stop()

        engine.disconnectNodeOutput(player)
        engine.disconnectNodeOutput(varispeed)
        engine.connect(player, varispeed, newFormat)
        engine.connect(varispeed, engine.mainMixerNode, newFormat)

        audioBuffer = buffer

        if (wasRunning) {
            if (!engine.startAndReturnError(null)) {
                Cedar.tag("MetronomePlayerIos").e("Failed to restart audio engine after sound switch")
                return
            }
            player.play()
        }
    }

    private fun soundFileInfo(sound: ClickSound): Pair<String, String> = when (sound) {
        ClickSound.WOOD -> "wood" to "mp3"
        ClickSound.CLICK -> "click" to "mp3"
        ClickSound.CLASSIC -> "metronome" to "wav"
    }
}
