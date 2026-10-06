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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.asSharedFlow
import org.kimplify.cedar.logging.Cedar
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioFile
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioPlayerNode
import platform.AVFAudio.AVAudioPlayerNodeBufferInterrupts
import platform.AVFAudio.AVAudioUnitVarispeed
import platform.Foundation.NSBundle
import platform.Foundation.NSError
import kotlin.math.max

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class MetronomePlayerIos : MetronomePlayer {
    private sealed interface AudioCommand {
        data class Initialize(val sound: ClickSound, val generation: Long) : AudioCommand
        data class Play(val beat: Beat, val left: Float, val right: Float, val generation: Long) : AudioCommand
        data object Stop : AudioCommand
        data class SwitchSound(val sound: ClickSound, val generation: Long) : AudioCommand
        data object Release : AudioCommand
    }

    private data class AudioGraph(
        val engine: AVAudioEngine,
        val player: AVAudioPlayerNode,
        val varispeed: AVAudioUnitVarispeed,
        val buffer: AVAudioPCMBuffer,
        val accentBuffer: AVAudioPCMBuffer?,
    )

    private val mutableFailures = MutableSharedFlow<Throwable>(extraBufferCapacity = 4)
    override val failures: Flow<Throwable> = mutableFailures.asSharedFlow()
    private val latestGeneration = MutableStateFlow(0L)
    private var processingGeneration = 0L
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val commands = SerializedCommandQueue(
        scope = scope,
        handler = ::handleCommand,
        onFailure = { error ->
            Cedar.tag(TAG).e("Audio command failed: ${error.message ?: error::class.simpleName}")
            if (processingGeneration == latestGeneration.value) mutableFailures.tryEmit(error)
        },
    )

    private val sessionOwner = Any()
    private var graph: AudioGraph? = null
    private var requestedSound: ClickSound? = null

    override fun initialize(initialSound: ClickSound) {
        latestGeneration.update { it + 1 }
        commands.offer(AudioCommand.Initialize(initialSound, latestGeneration.value))
    }

    override fun play(beat: Beat, stereoLeft: Float, stereoRight: Float) {
        commands.offer(AudioCommand.Play(beat, stereoLeft, stereoRight, latestGeneration.value))
    }

    override fun stop() {
        commands.offer(AudioCommand.Stop)
    }

    override fun switchSound(sound: ClickSound) {
        latestGeneration.update { it + 1 }
        commands.offer(AudioCommand.SwitchSound(sound, latestGeneration.value))
    }

    override fun release() {
        latestGeneration.update { it + 1 }
        commands.offer(AudioCommand.Release)
    }

    private fun handleCommand(command: AudioCommand) {
        processingGeneration = when (command) {
            is AudioCommand.Initialize -> command.generation
            is AudioCommand.Play -> command.generation
            is AudioCommand.SwitchSound -> command.generation
            AudioCommand.Stop, AudioCommand.Release -> latestGeneration.value
        }
        when (command) {
            is AudioCommand.Initialize -> initializeInternal(command.sound)
            is AudioCommand.Play -> playInternal(command.beat, command.left, command.right)
            AudioCommand.Stop -> stopInternal()
            is AudioCommand.SwitchSound -> switchSoundInternal(command.sound)
            AudioCommand.Release -> releaseInternal()
        }
    }

    private fun initializeInternal(initialSound: ClickSound) {
        if (!activateSession()) {
            reportFailure(IllegalStateException("Audio session is unavailable"))
            return
        }
        val replacement = createGraph(initialSound)
        if (replacement == null) {
            if (graph == null) deactivateSession()
            reportFailure(IllegalStateException("Sound resource could not be loaded"))
            return
        }
        installGraph(replacement, initialSound)
    }

    private fun playInternal(beat: Beat, stereoLeft: Float, stereoRight: Float) {
        val current = graph ?: return
        if (!ensureRunning(current)) {
            reportFailure(IllegalStateException("Audio engine is unavailable"))
            return
        }
        val accent = current.accentBuffer.takeIf { beat == Beat.HIGH }
        current.varispeed.rate = if (accent == null) beat.rate else 1f
        current.player.volume = max(stereoLeft, stereoRight)
        current.player.pan = if (stereoLeft + stereoRight > 0f) {
            (stereoRight - stereoLeft) / max(stereoLeft, stereoRight)
        } else {
            0f
        }
        if (!current.player.playing) current.player.play()
        current.player.scheduleBuffer(
            accent ?: current.buffer,
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
        if (!activateSession()) {
            reportFailure(IllegalStateException("Audio session is unavailable"))
            return
        }
        val replacement = createGraph(sound)
        if (replacement == null) {
            if (graph == null) deactivateSession()
            reportFailure(IllegalStateException("Sound resource could not be loaded"))
            return
        }
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
        if (!audioFile.readIntoBuffer(buffer, error = null)) {
            Cedar.tag(TAG).e("Audio resource could not be decoded: $name.$ext")
            return null
        }
        val accentBuffer = accentFileName(sound)?.let { accentName ->
            val accentUrl = NSBundle.mainBundle.URLForResource(accentName, withExtension = "wav")
            if (accentUrl == null) {
                Cedar.tag(TAG).e("Audio resource not found: $accentName.wav")
                return null
            }
            val accentFile = AVAudioFile(forReading = accentUrl, error = null)
            val loaded = AVAudioPCMBuffer(
                pCMFormat = accentFile.processingFormat,
                frameCapacity = accentFile.length.toUInt(),
            )
            if (!accentFile.readIntoBuffer(loaded, error = null)) {
                Cedar.tag(TAG).e("Audio resource could not be decoded: $accentName.wav")
                return null
            }
            loaded
        }

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
        return AudioGraph(engine, player, varispeed, buffer, accentBuffer)
    }

    private fun ensureRunning(current: AudioGraph): Boolean {
        if (!current.engine.running) {
            if (!activateSession() || !startEngine(current.engine)) return false
        }
        return true
    }

    private fun reportFailure(error: Throwable) {
        if (processingGeneration == latestGeneration.value) mutableFailures.tryEmit(error)
    }

    private fun activateSession(): Boolean = IosAudioSessionLease.acquire(sessionOwner)

    private fun deactivateSession() {
        IosAudioSessionLease.release(sessionOwner)
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
        ClickSound.SOFT -> "soft" to "wav"
        ClickSound.RIM -> "rim" to "wav"
        ClickSound.CLAVE -> "clave" to "wav"
        ClickSound.STUDIO -> "studio" to "wav"
    }

    private fun accentFileName(sound: ClickSound): String? = when (sound) {
        ClickSound.WOOD, ClickSound.CLICK, ClickSound.CLASSIC -> null
        ClickSound.SOFT -> "soft_accent"
        ClickSound.RIM -> "rim_accent"
        ClickSound.CLAVE -> "clave_accent"
        ClickSound.STUDIO -> "studio_accent"
    }

    private companion object {
        const val TAG = "MetronomePlayerIos"
    }
}
