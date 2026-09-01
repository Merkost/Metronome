package com.merkost.metronome.engine

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SerializedCommandQueueTest {
    @Test
    fun commandsAreHandledInSubmissionOrder() = runTest {
        val handled = mutableListOf<Int>()
        val queue = SerializedCommandQueue<Int>(this, handler = { handled += it })

        assertTrue(queue.offer(1))
        assertTrue(queue.offer(2))
        assertTrue(queue.offer(3))
        runCurrent()

        assertEquals(listOf(1, 2, 3), handled)
        queue.close()
    }

    @Test
    fun oneFailedCommandDoesNotDiscardLaterCommands() = runTest {
        val handled = mutableListOf<Int>()
        val failures = mutableListOf<String>()
        val queue = SerializedCommandQueue<Int>(
            scope = this,
            handler = {
                if (it == 1) error("graph failed")
                handled += it
            },
            onFailure = { failures += it.message.orEmpty() },
        )

        queue.offer(1)
        queue.offer(2)
        runCurrent()

        assertEquals(listOf("graph failed"), failures)
        assertEquals(listOf(2), handled)
        queue.close()
    }

    @Test
    fun closedQueueRejectsNewCommands() = runTest {
        val queue = SerializedCommandQueue<Int>(this, handler = {})

        queue.close()

        assertFalse(queue.offer(1))
    }
}
