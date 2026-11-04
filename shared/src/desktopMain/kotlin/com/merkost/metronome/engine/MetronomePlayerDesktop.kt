package com.merkost.metronome.engine

import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.ClickSound
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal interface DesktopAudioOutput {
    fun open()
    fun start()
    fun write(bytes: ByteArray, offset: Int, length: Int): Int
    fun stopAndFlush()
    fun close()
}

internal fun interface DesktopSoundBank {
    fun load(sound: ClickSound): DesktopSoundSamples
}

class MetronomePlayerDesktop internal constructor(
    private val outputFactory: () -> DesktopAudioOutput,
    private val soundBank: DesktopSoundBank,
    private val nanoTime: () -> Long = System::nanoTime,
) : MetronomePlayer {
    constructor() : this(::JavaSoundDesktopOutput, BundledDesktopSounds())

    private sealed interface AudioCommand {
        val generation: Long
        data class Initialize(val sound: ClickSound, override val generation: Long) : AudioCommand
        data class Play(val sound: ClickSound, val beat: Beat, val left: Float, val right: Float, override val generation: Long, val queuedAtNanos: Long, val onSubmitted: (() -> Unit)?) : AudioCommand
        data class Stop(override val generation: Long) : AudioCommand
        data class Release(override val generation: Long) : AudioCommand
    }

    private val stateLock = Any()
    private val commands = LinkedBlockingQueue<AudioCommand>(64)
    private val generation = AtomicLong()
    private val mutableFailures = MutableSharedFlow<Throwable>(extraBufferCapacity = 4)
    override val failures: Flow<Throwable> = mutableFailures.asSharedFlow()
    private var worker: Thread? = null
    private var selectedSound: ClickSound? = null
    @Volatile private var initialized = false
    @Volatile private var activeOutput: DesktopAudioOutput? = null

    override fun initialize(initialSound: ClickSound) {
        synchronized(stateLock) {
            initialized = true
            selectedSound = initialSound
            val ticket = generation.incrementAndGet()
            commands.clear()
            commands.offer(AudioCommand.Initialize(initialSound, ticket))
            ensureWorker()
        }
    }

    override fun play(beat: Beat, stereoLeft: Float, stereoRight: Float) {
        enqueuePlay(beat, stereoLeft, stereoRight, null)
    }

    override fun play(beat: Beat, stereoLeft: Float, stereoRight: Float, onSubmitted: () -> Unit) {
        enqueuePlay(beat, stereoLeft, stereoRight, onSubmitted)
    }

    private fun enqueuePlay(beat: Beat, left: Float, right: Float, onSubmitted: (() -> Unit)?) {
        if (beat == Beat.MUTE) return
        synchronized(stateLock) {
            if (!initialized) return
            val now = nanoTime()
            commands.removeIf { it is AudioCommand.Play && now - it.queuedAtNanos >= MaximumPendingAgeNanos }
            val command = AudioCommand.Play(selectedSound ?: ClickSound.WOOD, beat, left, right, generation.get(), now, onSubmitted)
            if (!commands.offer(command)) {
                commands.firstOrNull { it is AudioCommand.Play }?.let(commands::remove)
                commands.offer(command)
            }
            ensureWorker()
        }
    }

    override fun stop() {
        synchronized(stateLock) {
            val ticket = generation.incrementAndGet()
            commands.clear()
            activeOutput?.stopAndFlush()
            if (worker != null) commands.offer(AudioCommand.Stop(ticket))
        }
    }

    override fun release() {
        synchronized(stateLock) {
            initialized = false
            val ticket = generation.incrementAndGet()
            commands.clear()
            activeOutput?.stopAndFlush()
            if (worker != null) commands.offer(AudioCommand.Release(ticket))
        }
    }

    override fun switchSound(sound: ClickSound) {
        synchronized(stateLock) {
            if (!initialized || selectedSound == sound) return
            selectedSound = sound
            val ticket = generation.incrementAndGet()
            commands.clear()
            activeOutput?.stopAndFlush()
            commands.offer(AudioCommand.Initialize(sound, ticket))
            ensureWorker()
        }
    }

    private fun ensureWorker() {
        if (worker?.isAlive == true) return
        worker = Thread(::renderAudio, "Metronome audio ${workerIds.incrementAndGet()}").apply {
            isDaemon = true
            priority = Thread.NORM_PRIORITY + 1
            start()
        }
    }

    private fun renderAudio() {
        val mixer = DesktopPcmMixer()
        var samples: DesktopSoundSamples? = null
        var loadedSound: ClickSound? = null
        var output: DesktopAudioOutput? = null

        fun closeOutput() {
            val previous = output
            output = null
            synchronized(stateLock) {
                if (activeOutput === previous) activeOutput = null
            }
            previous?.close()
        }

        fun ensureOutput(ticket: Long): DesktopAudioOutput? {
            output?.let { return it }
            val replacement = outputFactory()
            try {
                replacement.open()
            } catch (failure: Exception) {
                runCatching { replacement.close() }
                throw failure
            }
            return synchronized(stateLock) {
                if (ticket != generation.get() || !initialized) {
                    replacement.close()
                    null
                } else {
                    output = replacement
                    activeOutput = replacement
                    replacement
                }
            }
        }

        fun fail(failure: Exception, ticket: Long) {
            mixer.clear()
            runCatching { closeOutput() }
            if (ticket == generation.get()) {
                mutableFailures.tryEmit(IllegalStateException("Couldn't play this click. Check your Mac's output device and try another sound.", failure))
            }
        }

        fun retainCurrentClickAfterPreparation(ticket: Long, current: AudioCommand.Play? = null): AudioCommand.Play? = synchronized(stateLock) {
            if (ticket != generation.get() || !initialized) return@synchronized null
            val now = nanoTime()
            val pending = commands.filterIsInstance<AudioCommand.Play>().filter { it.generation == ticket }
            val candidates = listOfNotNull(current) + pending
            val fresh = candidates.filter { now - it.queuedAtNanos < MaximumPendingAgeNanos }
            commands.removeIf { it is AudioCommand.Play && it.generation == ticket && now - it.queuedAtNanos >= MaximumPendingAgeNanos }
            if (fresh.isNotEmpty()) current?.takeIf { now - it.queuedAtNanos < MaximumPendingAgeNanos }
            else candidates.lastOrNull()?.copy(queuedAtNanos = now)
        }

        fun handle(command: AudioCommand): Boolean {
            if (command.generation != generation.get()) return false
            when (command) {
                is AudioCommand.Initialize -> {
                    mixer.clear()
                    output?.stopAndFlush()
                    samples = null
                    loadedSound = null
                    val replacement = soundBank.load(command.sound)
                    if (command.generation == generation.get() && initialized) {
                        samples = replacement
                        loadedSound = command.sound
                        if (ensureOutput(command.generation) != null) {
                            synchronized(stateLock) {
                                retainCurrentClickAfterPreparation(command.generation)?.let(commands::offer)
                            }
                        }
                    }
                }
                is AudioCommand.Play -> {
                    val needsPreparation = loadedSound != command.sound || samples == null || output == null
                    if (!needsPreparation && nanoTime() - command.queuedAtNanos >= MaximumPendingAgeNanos) return false
                    val current = if (loadedSound == command.sound && samples != null) {
                        samples!!
                    } else {
                        val replacement = soundBank.load(command.sound)
                        if (command.generation != generation.get() || !initialized) return false
                        samples = replacement
                        loadedSound = command.sound
                        replacement
                    }
                    if (ensureOutput(command.generation) == null) return false
                    val ready = if (needsPreparation) retainCurrentClickAfterPreparation(command.generation, command) ?: return false else command
                    if (nanoTime() - ready.queuedAtNanos >= MaximumPendingAgeNanos) return false
                    val playback = current.forBeat(ready.beat)
                    mixer.enqueue(playback.clip, playback.rate, ready.left, ready.right, ready.generation, ready.onSubmitted)
                }
                is AudioCommand.Stop -> {
                    mixer.clear()
                    output?.stopAndFlush()
                }
                is AudioCommand.Release -> return synchronized(stateLock) {
                    if (command.generation != generation.get() || initialized) {
                        false
                    } else {
                        mixer.clear()
                        closeOutput()
                        if (worker === Thread.currentThread()) worker = null
                        true
                    }
                }
            }
            return false
        }

        try {
            while (true) {
                val command = if (mixer.hasVoices) commands.poll() else commands.take()
                if (command != null) {
                    try {
                        if (handle(command)) return
                    } catch (failure: Exception) {
                        fail(failure, command.generation)
                    }
                    continue
                }
                val ticket = generation.get()
                try {
                    val block = mixer.render(256, ticket)
                    if (block.frames == 0 || ticket != generation.get()) continue
                    val current = output ?: error("Audio output is unavailable.")
                    current.start()
                    var offset = 0
                    var submitted = false
                    while (offset < block.bytes.size && ticket == generation.get()) {
                        val written = current.write(block.bytes, offset, block.bytes.size - offset)
                        if (written <= 0) {
                            check(ticket != generation.get()) { "Audio output stopped accepting samples." }
                            break
                        }
                        check(written <= block.bytes.size - offset && written % 4 == 0) { "Audio output returned an invalid sample count." }
                        offset += written
                        if (!submitted) {
                            block.submissions.forEach { callback ->
                                synchronized(stateLock) {
                                    if (ticket == generation.get() && initialized) callback()
                                }
                            }
                            submitted = true
                        }
                    }
                } catch (failure: Exception) {
                    fail(failure, ticket)
                }
            }
        } finally {
            runCatching { closeOutput() }
            synchronized(stateLock) {
                if (worker === Thread.currentThread()) worker = null
            }
        }
    }

    private companion object {
        const val MaximumPendingAgeNanos = 100_000_000L
        val workerIds = AtomicInteger()
    }
}

private class JavaSoundDesktopOutput : DesktopAudioOutput {
    @Volatile private var line: SourceDataLine? = null

    override fun open() {
        val replacement = AudioSystem.getSourceDataLine(DesktopAudioFormat)
        try {
            replacement.open(DesktopAudioFormat, DesktopAudioFormat.frameSize * 512)
            line = replacement
        } catch (failure: Exception) {
            replacement.close()
            throw failure
        }
    }

    override fun start() {
        line?.let { if (!it.isRunning) it.start() }
    }

    override fun write(bytes: ByteArray, offset: Int, length: Int): Int = line?.write(bytes, offset, length) ?: 0

    override fun stopAndFlush() {
        line?.let { current ->
            runCatching { current.stop(); current.flush() }
        }
    }

    override fun close() {
        val previous = line
        line = null
        previous?.let { current ->
            runCatching { current.stop(); current.flush() }
            current.close()
        }
    }
}
