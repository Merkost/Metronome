package com.merkost.metronome.engine

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import org.kimplify.cedar.logging.Cedar
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionMixWithOthers
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSError
import platform.Foundation.NSLock

@OptIn(ExperimentalForeignApi::class)
internal object IosAudioSessionLease {
    private val lock = NSLock()
    private val owners = mutableSetOf<Any>()

    fun acquire(owner: Any): Boolean {
        lock.lock()
        try {
            val acquired = memScoped {
                val error = alloc<ObjCObjectVar<NSError?>>()
                val session = AVAudioSession.sharedInstance()
                if (owners.isEmpty() && !session.setCategory(
                        AVAudioSessionCategoryPlayback,
                        withOptions = AVAudioSessionCategoryOptionMixWithOthers,
                        error = error.ptr,
                    )
                ) {
                    Cedar.tag("IosAudioSession").e("setCategory failed: ${error.value?.localizedDescription}")
                    return@memScoped false
                }
                val active = session.setActive(true, error = error.ptr)
                if (!active) Cedar.tag("IosAudioSession").e("setActive failed: ${error.value?.localizedDescription}")
                active
            }
            if (acquired) owners += owner
            return acquired
        } finally {
            lock.unlock()
        }
    }

    fun release(owner: Any) {
        lock.lock()
        try {
            if (!owners.remove(owner) || owners.isNotEmpty()) return
            memScoped {
                val error = alloc<ObjCObjectVar<NSError?>>()
                val deactivated = AVAudioSession.sharedInstance().setActive(false, error = error.ptr)
                if (!deactivated) Cedar.tag("IosAudioSession").e("setActive(false) failed: ${error.value?.localizedDescription}")
            }
        } finally {
            lock.unlock()
        }
    }
}
