package com.merkost.metronome.engine

import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.ClickSound
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.kimplify.cedar.logging.Cedar
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioFile
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioPlayerNode
import platform.AVFAudio.AVAudioPlayerNodeBufferInterrupts
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionMixWithOthers
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVAudioUnitVarispeed
import platform.AVFAudio.setActive
import platform.Foundation.NSBundle
import platform.Foundation.NSError
import kotlin.math.max

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class MetronomePlayerIos : MetronomePlayer {
    private sealed interface AudioCommand {
        data class Initialize(val sound: ClickSound) : AudioCommand
        data class Play(val beat: Beat, val left: Float, val right: Float) : AudioCommand
        data object Stop : AudioCommand
        data class SwitchSound(val sound: ClickSound) : AudioCommand
        data object Release : AudioCommand
    }

    private data class AudioGraph(
        val engine: AVAudioEngine,
        val player: AVAudioPlayerNode,
        val varispeed: AVAudioUnitVarispeed,
        val buffer: AVAudioPCMBuffer,
    )

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val commands = SerializedCommandQueue(
        scope = scope,
        handler = ::handleCommand,
        onFailure = { error ->
            Cedar.tag(TAG).e("Audio command failed: ${error.message ?: error::class.simpleName}")
        },
    )

    private var graph: AudioGraph? = null
    private var requestedSound: ClickSound? = null

    override fun initialize(initialSound: ClickSound) {
        commands.offer(AudioCommand.Initialize(initialSound))
    }

    override fun play(beat: Beat, stereoLeft: Float, stereoRight: Float) {
        commands.offer(AudioCommand.Play(beat, stereoLeft, stereoRight))
    }

    override fun stop() {
        commands.offer(AudioCommand.Stop)
    }

    override fun switchSound(sound: ClickSound) {
        commands.offer(AudioCommand.SwitchSound(sound))
    }

    override fun release() {
        commands.offer(AudioCommand.Release)
    }

    private fun handleCommand(command: AudioCommand) {
        when (command) {
            is AudioCommand.Initialize -> initializeInternal(command.sound)
            is AudioCommand.Play -> playInternal(command.beat, command.left, command.right)
            AudioCommand.Stop -> stopInternal()
            is AudioCommand.SwitchSound -> switchSoundInternal(command.sound)
            AudioCommand.Release -> releaseInternal()
        }
    }

    private fun initializeInternal(initialSound: ClickSound) {
        if (!configureSession() || !activateSession()) return
        val replacement = createGraph(initialSound) ?: return
        installGraph(replacement, initialSound)
    }

    private fun playInternal(beat: Beat, stereoLeft: Float, stereoRight: Float) {
        val current = graph ?: return
        if (!ensureRunning(current)) return
        current.varispeed.rate = beat.rate
        current.player.volume = max(stereoLeft, stereoRight)
        current.player.pan = if (stereoLeft + stereoRight > 0f) {
            (stereoRight - stereoLeft) / max(stereoLeft, stereoRight)
        } else {
            0f
        }
        if (!current.player.playing) current.player.play()
        current.player.scheduleBuffer(
            current.buffer,
            atTime = null,
            options = AVAudioPlayerNodeBufferInterrupts,
            completionHandler = null,
        )
    }

    private fun stopInternal() {
        graph?.player?.stop()
        graph?.engine?.stop()
        deactivateSession()
    }

    private fun switchSoundInternal(sound: ClickSound) {
        if (sound == requestedSound) return
        if (!activateSession()) return
        val replacement = createGraph(sound) ?: return
        installGraph(replacement, sound)
    }

    private fun releaseInternal() {
        graph?.player?.stop()
        graph?.engine?.stop()
        graph = null
        requestedSound = null
        deactivateSession()
    }

    private fun installGraph(replacement: AudioGraph, sound: ClickSound) {
        val previous = graph
        graph = replacement
        requestedSound = sound
        previous?.player?.stop()
        previous?.engine?.stop()
    }

    private fun createGraph(sound: ClickSound): AudioGraph? {
        val (name, ext) = soundFileInfo(sound)
        val url = NSBundle.mainBundle.URLForResource(name, withExtension = ext)
        if (url == null) {
            Cedar.tag(TAG).e("Audio resource not found: $name.$ext")
            return null
        }
        val audioFile = AVAudioFile(forReading = url, error = null)
        val buffer = AVAudioPCMBuffer(
            pCMFormat = audioFile.processingFormat,
            frameCapacity = audioFile.length.toUInt(),
        )
        audioFile.readIntoBuffer(buffer, error = null)

        val engine = AVAudioEngine()
        val player = AVAudioPlayerNode()
        val varispeed = AVAudioUnitVarispeed()
        engine.attachNode(player)
        engine.attachNode(varispeed)
        engine.connect(player, varispeed, audioFile.processingFormat)
        engine.connect(varispeed, engine.mainMixerNode, audioFile.processingFormat)
        engine.prepare()
        if (!startEngine(engine)) return null
        player.play()
        return AudioGraph(engine, player, varispeed, buffer)
    }

    private fun ensureRunning(current: AudioGraph): Boolean {
        if (!current.engine.running) {
            if (!activateSession() || !startEngine(current.engine)) return false
        }
        return true
    }

    private fun configureSession(): Boolean = memScoped {
        val error = alloc<ObjCObjectVar<NSError?>>()
        val configured = AVAudioSession.sharedInstance().setCategory(
            AVAudioSessionCategoryPlayback,
            withOptions = AVAudioSessionCategoryOptionMixWithOthers,
            error = error.ptr,
        )
        if (!configured) Cedar.tag(TAG).e("setCategory failed: ${describe(error.value)}")
        configured
    }

    private fun activateSession(): Boolean = memScoped {
        val error = alloc<ObjCObjectVar<NSError?>>()
        val activated = AVAudioSession.sharedInstance().setActive(true, error = error.ptr)
        if (!activated) Cedar.tag(TAG).e("setActive failed: ${describe(error.value)}")
        activated
    }

    private fun deactivateSession() {
        memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            val deactivated = AVAudioSession.sharedInstance().setActive(false, error = error.ptr)
            if (!deactivated) Cedar.tag(TAG).e("setActive(false) failed: ${describe(error.value)}")
        }
    }

    private fun startEngine(engine: AVAudioEngine): Boolean = memScoped {
        val error = alloc<ObjCObjectVar<NSError?>>()
        val started = engine.startAndReturnError(error.ptr)
        if (!started) Cedar.tag(TAG).e("Audio engine start failed: ${describe(error.value)}")
        started
    }

    private fun describe(error: NSError?): String =
        if (error == null) "unknown error" else "${error.localizedDescription} [${error.domain}:${error.code}]"

    private fun soundFileInfo(sound: ClickSound): Pair<String, String> = when (sound) {
        ClickSound.WOOD -> "wood" to "mp3"
        ClickSound.CLICK -> "click" to "mp3"
        ClickSound.CLASSIC -> "metronome" to "wav"
    }

    private companion object {
        const val TAG = "MetronomePlayerIos"
    }
}
