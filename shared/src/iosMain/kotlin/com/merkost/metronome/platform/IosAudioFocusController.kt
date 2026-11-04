package com.merkost.metronome.platform

import com.merkost.metronome.engine.IosAudioSessionLease
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionInterruptionNotification
import platform.AVFAudio.AVAudioSessionInterruptionOptionKey
import platform.AVFAudio.AVAudioSessionInterruptionOptionShouldResume
import platform.AVFAudio.AVAudioSessionInterruptionTypeBegan
import platform.AVFAudio.AVAudioSessionInterruptionTypeKey
import platform.AVFAudio.AVAudioSessionRouteChangeNotification
import platform.AVFAudio.AVAudioSessionRouteChangeReasonKey
import platform.AVFAudio.AVAudioSessionRouteChangeReasonOldDeviceUnavailable
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNumber

@OptIn(ExperimentalForeignApi::class)
class IosAudioFocusController : AudioFocusController {
    private val sessionOwner = Any()
    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val notificationCenter = NSNotificationCenter.defaultCenter
    private var onLost: (() -> Unit)? = null
    private var onEvent: ((AudioFocusEvent) -> Unit)? = null

    private val interruptionObserver = notificationCenter.addObserverForName(
        name = AVAudioSessionInterruptionNotification,
        `object` = AVAudioSession.sharedInstance(),
        queue = null,
    ) { notification ->
        val type = notification?.userInfo
            ?.get(AVAudioSessionInterruptionTypeKey) as? NSNumber
        if (type?.unsignedIntegerValue == AVAudioSessionInterruptionTypeBegan) {
            dispatch(AudioFocusEvent.LOSS_TRANSIENT)
        } else {
            val options = (notification?.userInfo?.get(AVAudioSessionInterruptionOptionKey) as? NSNumber)
                ?.unsignedIntegerValue ?: 0uL
            if (options and AVAudioSessionInterruptionOptionShouldResume != 0uL) {
                dispatch(AudioFocusEvent.GAIN)
            } else {
                dispatch(AudioFocusEvent.LOSS)
            }
        }
    }

    private val routeObserver = notificationCenter.addObserverForName(
        name = AVAudioSessionRouteChangeNotification,
        `object` = AVAudioSession.sharedInstance(),
        queue = null,
    ) { notification ->
        val reason = notification?.userInfo
            ?.get(AVAudioSessionRouteChangeReasonKey) as? NSNumber
        if (reason?.unsignedIntegerValue == AVAudioSessionRouteChangeReasonOldDeviceUnavailable) {
            dispatch(AudioFocusEvent.LOSS)
        }
    }

    override fun setOnFocusLost(onLost: () -> Unit) {
        this.onLost = onLost
    }

    override fun setOnFocusEvent(listener: (AudioFocusEvent) -> Unit) {
        this.onEvent = listener
    }

    override fun requestFocus(): Boolean = IosAudioSessionLease.acquire(sessionOwner)

    override fun abandonFocus() {
        IosAudioSessionLease.release(sessionOwner)
    }

    private fun dispatch(event: AudioFocusEvent) {
        mainScope.launch {
            onEvent?.invoke(event)
            if (event == AudioFocusEvent.LOSS || event == AudioFocusEvent.LOSS_TRANSIENT) onLost?.invoke()
        }
    }
}
