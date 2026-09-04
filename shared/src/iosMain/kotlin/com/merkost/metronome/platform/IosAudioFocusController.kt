package com.merkost.metronome.platform

import com.merkost.metronome.engine.AudioSessionEvent
import com.merkost.metronome.engine.shouldStopPlaybackFor
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionInterruptionNotification
import platform.AVFAudio.AVAudioSessionInterruptionTypeBegan
import platform.AVFAudio.AVAudioSessionInterruptionTypeKey
import platform.AVFAudio.AVAudioSessionRouteChangeNotification
import platform.AVFAudio.AVAudioSessionRouteChangeReasonKey
import platform.AVFAudio.AVAudioSessionRouteChangeReasonOldDeviceUnavailable
import platform.AVFAudio.setActive
import platform.Foundation.NSError
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNumber

@OptIn(ExperimentalForeignApi::class)
class IosAudioFocusController : AudioFocusController {
    private val notificationCenter = NSNotificationCenter.defaultCenter
    private var onLost: (() -> Unit)? = null

    private val interruptionObserver = notificationCenter.addObserverForName(
        name = AVAudioSessionInterruptionNotification,
        `object` = AVAudioSession.sharedInstance(),
        queue = null,
    ) { notification ->
        val type = notification?.userInfo
            ?.get(AVAudioSessionInterruptionTypeKey) as? NSNumber
        val event = if (type?.unsignedIntegerValue == AVAudioSessionInterruptionTypeBegan) {
            AudioSessionEvent.INTERRUPTION_BEGAN
        } else {
            AudioSessionEvent.INTERRUPTION_ENDED
        }
        notifyIfPlaybackShouldStop(event)
    }

    private val routeObserver = notificationCenter.addObserverForName(
        name = AVAudioSessionRouteChangeNotification,
        `object` = AVAudioSession.sharedInstance(),
        queue = null,
    ) { notification ->
        val reason = notification?.userInfo
            ?.get(AVAudioSessionRouteChangeReasonKey) as? NSNumber
        val event = if (reason?.unsignedIntegerValue == AVAudioSessionRouteChangeReasonOldDeviceUnavailable) {
            AudioSessionEvent.OLD_ROUTE_UNAVAILABLE
        } else {
            AudioSessionEvent.OTHER
        }
        notifyIfPlaybackShouldStop(event)
    }

    override fun setOnFocusLost(onLost: () -> Unit) {
        this.onLost = onLost
    }

    override fun requestFocus(): Boolean = memScoped {
        val error = alloc<ObjCObjectVar<NSError?>>()
        AVAudioSession.sharedInstance().setActive(true, error = error.ptr)
    }

    override fun abandonFocus() {
        memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            AVAudioSession.sharedInstance().setActive(false, error = error.ptr)
        }
    }

    private fun notifyIfPlaybackShouldStop(event: AudioSessionEvent) {
        if (shouldStopPlaybackFor(event)) onLost?.invoke()
    }
}
