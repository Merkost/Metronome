package com.merkost.metronome.engine

import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.ClickSound
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MetronomePlayerDesktopTest {
    @Test
    fun pulseIsSubmittedOnlyAfterTheOutputAcceptsAudioBytes() {
        val output = GatedOutput()
        val pulse = CountDownLatch(1)
        val calls = AtomicInteger()
        val player = MetronomePlayerDesktop({ output }, soundBank())
        try {
            player.initialize(ClickSound.WOOD)
            player.play(Beat.LOW, 1f, 0f) { calls.incrementAndGet(); pulse.countDown() }
            assertTrue(output.entered.await(2, TimeUnit.SECONDS), "The PCM renderer never reached the output")
            assertEquals(0, calls.get())
            output.proceed.countDown()
            assertTrue(pulse.await(2, TimeUnit.SECONDS), "The accepted sound never submitted its pulse")
            assertEquals(1, calls.get())
            assertTrue(output.written.isNotEmpty())
            assertTrue(output.written.first().asList().chunked(4).all { it[2] == 0.toByte() && it[3] == 0.toByte() })
        } finally {
            output.proceed.countDown()
            player.release()
        }
    }

    @Test
    fun stopDropsBlockedAudioAndAllQueuedPulseCallbacks() {
        val output = GatedOutput()
        val calls = AtomicInteger()
        val player = MetronomePlayerDesktop({ output }, soundBank())
        try {
            player.initialize(ClickSound.WOOD)
            player.play(Beat.LOW, 1f, 1f) { calls.incrementAndGet() }
            assertTrue(output.entered.await(2, TimeUnit.SECONDS), "The PCM renderer never reached the output")
            player.play(Beat.HIGH, 1f, 1f) { calls.incrementAndGet() }
            player.stop()
            player.release()
            assertTrue(output.closed.await(2, TimeUnit.SECONDS), "Release did not finish closing the output")
            assertEquals(0, calls.get())
            assertTrue(output.written.isEmpty())
        } finally {
            output.proceed.countDown()
            player.release()
        }
    }

    @Test
    fun aStalledOutputDoesNotReplayExpiredQueuedClicksWhenItRecovers() {
        val output = GatedOutput()
        val stalePulses = AtomicInteger()
        val freshPulse = CountDownLatch(1)
        val player = MetronomePlayerDesktop({ output }, soundBank())
        try {
            player.initialize(ClickSound.WOOD)
            player.play(Beat.LOW, 1f, 1f)
            assertTrue(output.entered.await(2, TimeUnit.SECONDS))
            repeat(1_000) { player.play(Beat.LOW, 1f, 1f) { stalePulses.incrementAndGet() } }
            TimeUnit.MILLISECONDS.sleep(250)
            player.play(Beat.HIGH, 1f, 1f) { freshPulse.countDown() }
            output.proceed.countDown()
            assertTrue(freshPulse.await(2, TimeUnit.SECONDS), "The recovered output never played the current click")
            player.release()
            assertTrue(output.closed.await(2, TimeUnit.SECONDS))
            assertEquals(0, stalePulses.get(), "Old clicks were replayed as a burst after the output recovered")
        } finally {
            output.proceed.countDown()
            player.release()
        }
    }

    @Test
    fun releasingAPreviewAllowsTheSamePlayerToInitializeAgainWithoutDecodingPerBeat() {
        val outputs = CopyOnWriteArrayList<GatedOutput>()
        val loads = AtomicInteger()
        val bank = DesktopSoundBank { loads.incrementAndGet(); DesktopSoundSamples(DesktopPcmClip(FloatArray(16) { 0.25f }), null) }
        val player = MetronomePlayerDesktop({ GatedOutput().also { it.proceed.countDown(); outputs += it } }, bank)
        try {
            player.initialize(ClickSound.WOOD)
            val first = CountDownLatch(1)
            player.play(Beat.LOW, 1f, 1f) { first.countDown() }
            assertTrue(first.await(2, TimeUnit.SECONDS))
            player.release()
            assertTrue(outputs.first().closed.await(2, TimeUnit.SECONDS))
            player.initialize(ClickSound.RIM)
            val submitted = CountDownLatch(3)
            repeat(3) { player.play(Beat.LOW, 1f, 1f) { submitted.countDown() } }
            assertTrue(submitted.await(2, TimeUnit.SECONDS))
            assertEquals(2, outputs.size)
            assertEquals(2, loads.get())
        } finally {
            player.release()
        }
    }

    @Test
    fun pausingDuringInitialLoadingDoesNotLeaveTheNextPlayWithoutASound() {
        val now = AtomicLong()
        val loading = CountDownLatch(1)
        val resume = CountDownLatch(1)
        val output = GatedOutput().also { it.proceed.countDown() }
        val player = MetronomePlayerDesktop({ output }, DesktopSoundBank {
            loading.countDown()
            check(resume.await(2, TimeUnit.SECONDS))
            DesktopSoundSamples(DesktopPcmClip(FloatArray(16) { 0.5f }), null)
        }, now::get)
        try {
            player.initialize(ClickSound.WOOD)
            assertTrue(loading.await(2, TimeUnit.SECONDS))
            player.stop()
            val pulse = CountDownLatch(1)
            player.play(Beat.LOW, 1f, 1f) { pulse.countDown() }
            now.set(TimeUnit.MILLISECONDS.toNanos(250))
            resume.countDown()
            assertTrue(pulse.await(2, TimeUnit.SECONDS), "Resuming after a cancelled initial load lost the selected sound")
        } finally {
            resume.countDown()
            player.release()
        }
    }

    @Test
    fun coldSoundPreparationDoesNotDiscardTheFirstRequestedClick() {
        val now = AtomicLong()
        val loading = CountDownLatch(1)
        val prepared = CountDownLatch(1)
        val output = GatedOutput().also { it.proceed.countDown() }
        val player = MetronomePlayerDesktop({ output }, DesktopSoundBank {
            loading.countDown()
            check(prepared.await(2, TimeUnit.SECONDS))
            DesktopSoundSamples(DesktopPcmClip(FloatArray(32) { 0.5f }), null)
        }, now::get)
        try {
            player.initialize(ClickSound.WOOD)
            assertTrue(loading.await(2, TimeUnit.SECONDS))
            val submitted = CountDownLatch(1)
            player.play(Beat.LOW, 1f, 1f) { submitted.countDown() }
            now.set(TimeUnit.MILLISECONDS.toNanos(250))
            prepared.countDown()
            assertTrue(submitted.await(2, TimeUnit.SECONDS), "Cold sound preparation discarded the first click")
        } finally {
            prepared.countDown()
            player.release()
        }
    }

    @Test
    fun slowOutputPreparationKeepsOnlyTheNewestExpiredClick() {
        val now = AtomicLong()
        val opening = CountDownLatch(1)
        val prepared = CountDownLatch(1)
        val output = GatedOutput(onOpen = {
            opening.countDown()
            check(prepared.await(2, TimeUnit.SECONDS))
        }).also { it.proceed.countDown() }
        val player = MetronomePlayerDesktop({ output }, soundBank(), now::get)
        val older = AtomicInteger()
        val newest = CountDownLatch(1)
        try {
            player.initialize(ClickSound.WOOD)
            assertTrue(opening.await(2, TimeUnit.SECONDS))
            repeat(10) { player.play(Beat.LOW, 1f, 1f) { older.incrementAndGet() } }
            player.play(Beat.HIGH, 1f, 1f) { newest.countDown() }
            now.set(TimeUnit.MILLISECONDS.toNanos(250))
            prepared.countDown()
            assertTrue(newest.await(2, TimeUnit.SECONDS), "Opening the device discarded the newest requested click")
            player.release()
            assertTrue(output.closed.await(2, TimeUnit.SECONDS))
            assertEquals(0, older.get(), "Preparation replayed the expired backlog")
        } finally {
            prepared.countDown()
            player.release()
        }
    }

    @Test
    fun freshClicksDuringPreparationReplaceExpiredClicks() {
        val now = AtomicLong()
        val opening = CountDownLatch(1)
        val prepared = CountDownLatch(1)
        val output = GatedOutput(onOpen = {
            opening.countDown()
            check(prepared.await(2, TimeUnit.SECONDS))
        }).also { it.proceed.countDown() }
        val player = MetronomePlayerDesktop({ output }, soundBank(), now::get)
        val older = AtomicInteger()
        val fresh = CountDownLatch(1)
        try {
            player.initialize(ClickSound.WOOD)
            assertTrue(opening.await(2, TimeUnit.SECONDS))
            player.play(Beat.LOW, 1f, 1f) { older.incrementAndGet() }
            now.set(TimeUnit.MILLISECONDS.toNanos(250))
            player.play(Beat.HIGH, 1f, 1f) { fresh.countDown() }
            prepared.countDown()
            assertTrue(fresh.await(2, TimeUnit.SECONDS))
            player.release()
            assertTrue(output.closed.await(2, TimeUnit.SECONDS))
            assertEquals(0, older.get())
        } finally {
            prepared.countDown()
            player.release()
        }
    }

    @Test
    fun unavailableHardwareReportsAnActionableFailureInsteadOfPretendingToPlay() = runBlocking {
        val player = MetronomePlayerDesktop({
            object : DesktopAudioOutput {
                override fun open(): Unit = error("No output device")
                override fun start() = Unit
                override fun write(bytes: ByteArray, offset: Int, length: Int): Int = length
                override fun stopAndFlush() = Unit
                override fun close() = Unit
            }
        }, soundBank())
        var message: String? = null
        val listener = launch(start = CoroutineStart.UNDISPATCHED) {
            message = player.failures.first().message
        }
        try {
            player.initialize(ClickSound.WOOD)
            withTimeout(2_000) { listener.join() }
            assertTrue(message.orEmpty().contains("output device"))
        } finally {
            listener.cancel()
            player.release()
        }
    }

    private fun soundBank() = DesktopSoundBank {
        DesktopSoundSamples(DesktopPcmClip(FloatArray(32) { 0.5f }), null)
    }

    private class GatedOutput(private val onOpen: () -> Unit = {}) : DesktopAudioOutput {
        val entered = CountDownLatch(1)
        val proceed = CountDownLatch(1)
        val closed = CountDownLatch(1)
        val written = CopyOnWriteArrayList<ByteArray>()
        @Volatile private var stopped = false

        override fun open() = onOpen()
        override fun start() { stopped = false }
        override fun write(bytes: ByteArray, offset: Int, length: Int): Int {
            entered.countDown()
            check(proceed.await(2, TimeUnit.SECONDS))
            if (stopped) return 0
            written += bytes.copyOfRange(offset, offset + length)
            return length
        }
        override fun stopAndFlush() { stopped = true; proceed.countDown() }
        override fun close() { stopped = true; proceed.countDown(); closed.countDown() }
    }
}
