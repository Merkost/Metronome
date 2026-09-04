package com.merkost.metronome.engine

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

internal class SerializedCommandQueue<T>(
    scope: CoroutineScope,
    private val handler: suspend (T) -> Unit,
    private val onFailure: (Throwable) -> Unit = {},
) {
    private val channel = Channel<T>(Channel.UNLIMITED)

    init {
        scope.launch {
            for (command in channel) {
                try {
                    handler(command)
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    onFailure(error)
                }
            }
        }
    }

    fun offer(command: T): Boolean = channel.trySend(command).isSuccess

    fun close() {
        channel.close()
    }
}
