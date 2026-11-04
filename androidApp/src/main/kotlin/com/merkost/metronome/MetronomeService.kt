package com.merkost.metronome

import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.media.AudioManager
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.media.MediaMetadata
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import com.merkost.metronome.engine.shouldStopWhenHidden
import com.merkost.metronome.viewModels.MetronomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.kimplify.cedar.logging.Cedar
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class MetronomeService : Service(), KoinComponent {

    companion object {
        private const val CHANNEL_ID = "Metronome service"
        private const val NOTIFICATION_ID = 1
        private const val ACTION_STOP = "STOP_METRONOME_SERVICE"
        private const val ACTION_PLAY = "PLAY_METRONOME_SERVICE"
        private const val ACTION_PAUSE = "PAUSE_METRONOME_SERVICE"
        private const val WAKE_LOCK_TIMEOUT_MS = 4 * 60 * 60 * 1000L
    }

    private val binder = MetronomeBinder()
    private val viewModel: MetronomeViewModel = get()

    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var job: Job? = null
    private var mediaSession: MediaSession? = null
    private var noisyReceiverRegistered = false
    private var inForeground = false
    @Volatile private var dismissed = false

    private val wakeLock: PowerManager.WakeLock by lazy {
        (getSystemService(POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Metronome:playback")
            .apply { setReferenceCounted(false) }
    }

    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                viewModel.onStopClicked()
            }
        }
    }

    private val sessionCallback = object : MediaSession.Callback() {
        override fun onPlay() = requestPlay()
        override fun onPause() = viewModel.onStopClicked()
        override fun onStop() = viewModel.onStopClicked()
    }

    inner class MetronomeBinder : Binder() {
        fun getService(): MetronomeService {
            return this@MetronomeService
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> requestPlay()
            ACTION_PAUSE -> viewModel.onStopClicked()
            ACTION_STOP -> {
                dismissed = true
                viewModel.onStopClicked()
                stopForeground(STOP_FOREGROUND_REMOVE)
                inForeground = false
                (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(NOTIFICATION_ID)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onCreate() {
        createChannel()
        mediaSession = MediaSession(this, "Metronome").apply {
            @Suppress("DEPRECATION")
            setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS)
            setCallback(sessionCallback)
            setSessionActivity(contentIntent())
            isActive = true
        }
        job = coroutineScope.launch {
            launch {
                viewModel.isPlaying.collectLatest { playing ->
                    if (playing) {
                        wakeLock.acquire(WAKE_LOCK_TIMEOUT_MS)
                        registerNoisyReceiver()
                    } else {
                        releaseWakeLock()
                        unregisterNoisyReceiver()
                    }
                }
            }
            launch {
                viewModel.metronomeState
                    .map { Triple(it.playing, it.rhythm, it.timeSignature.label) }
                    .distinctUntilChanged()
                    .collectLatest { (playing, bpm, meter) -> publish(playing, bpm, meter) }
            }
        }
    }

    private fun requestPlay() {
        if (!viewModel.metronomeState.value.playing) viewModel.onPlayPauseClicked(false)
    }

    private fun createChannel() {
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_LOW)
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
    }

    private fun contentIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP }
        return PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun serviceIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, MetronomeService::class.java).apply { this.action = action }
        return PendingIntent.getService(
            this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun publish(playing: Boolean, bpm: Int, meter: String) {
        val session = mediaSession ?: return
        val actions = PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE or
            PlaybackState.ACTION_PLAY_PAUSE or PlaybackState.ACTION_STOP
        session.setPlaybackState(
            PlaybackState.Builder()
                .setActions(actions)
                .setState(
                    if (playing) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED,
                    PlaybackState.PLAYBACK_POSITION_UNKNOWN,
                    1f,
                )
                .build()
        )
        session.setMetadata(
            MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, "$bpm BPM")
                .putString(MediaMetadata.METADATA_KEY_ARTIST, "Metronome · $meter")
                .build()
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (playing) {
            dismissed = false
        } else if (dismissed) {
            if (inForeground) stopForeground(STOP_FOREGROUND_REMOVE)
            inForeground = false
            manager.cancel(NOTIFICATION_ID)
            return
        }
        val notification = buildNotification(playing, bpm, meter, session)
        if (playing) {
            try {
                startForeground(NOTIFICATION_ID, notification)
                inForeground = true
            } catch (e: Exception) {
                if (Build.VERSION.SDK_INT >= 31 && e is ForegroundServiceStartNotAllowedException) {
                    Cedar.tag("SERVICE").w("Foreground start not allowed; playback continues without it")
                    manager.notify(NOTIFICATION_ID, notification)
                } else {
                    throw e
                }
            }
        } else if (inForeground) {
            stopForeground(STOP_FOREGROUND_DETACH)
            inForeground = false
            manager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(
        playing: Boolean,
        bpm: Int,
        meter: String,
        session: MediaSession,
    ): Notification {
        val toggle = if (playing) {
            Notification.Action.Builder(
                Icon.createWithResource(this, android.R.drawable.ic_media_pause),
                "Pause",
                serviceIntent(ACTION_PAUSE, 1),
            ).build()
        } else {
            Notification.Action.Builder(
                Icon.createWithResource(this, android.R.drawable.ic_media_play),
                "Play",
                serviceIntent(ACTION_PLAY, 2),
            ).build()
        }
        val stop = Notification.Action.Builder(
            Icon.createWithResource(this, android.R.drawable.ic_menu_close_clear_cancel),
            "Stop",
            serviceIntent(ACTION_STOP, 3),
        ).build()
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("$bpm BPM")
            .setContentText("$meter · ${if (playing) "Playing" else "Paused"}")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(contentIntent())
            .setDeleteIntent(serviceIntent(ACTION_STOP, 3))
            .setOngoing(playing)
            .setOnlyAlertOnce(true)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setCategory(Notification.CATEGORY_TRANSPORT)
            .addAction(toggle)
            .addAction(stop)
            .setStyle(
                Notification.MediaStyle()
                    .setMediaSession(session.sessionToken)
                    .setShowActionsInCompactView(0, 1)
            )
            .build()
    }

    private fun registerNoisyReceiver() {
        if (noisyReceiverRegistered) return
        registerReceiver(noisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
        noisyReceiverRegistered = true
    }

    private fun unregisterNoisyReceiver() {
        if (!noisyReceiverRegistered) return
        unregisterReceiver(noisyReceiver)
        noisyReceiverRegistered = false
    }

    private fun releaseWakeLock() {
        if (wakeLock.isHeld) wakeLock.release()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        dismissed = true
        viewModel.onStopClicked()
        stopForeground(STOP_FOREGROUND_REMOVE)
        inForeground = false
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(NOTIFICATION_ID)
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        job?.cancel()
        releaseWakeLock()
        unregisterNoisyReceiver()
        mediaSession?.release()
        mediaSession = null
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun stopPlaying() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        if (shouldStopWhenHidden(backgroundPlay = false, screenInteractive = powerManager.isInteractive)) {
            dismissed = true
            viewModel.onStopClicked()
        }
    }
}
