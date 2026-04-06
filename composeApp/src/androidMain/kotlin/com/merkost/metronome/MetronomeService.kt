package com.merkost.metronome

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Binder
import android.os.IBinder
import com.merkost.metronome.viewModels.MetronomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class MetronomeService : Service(), KoinComponent {

    companion object {
        private const val CHANNEL_ID = "Metronome service"
        private const val STOP_SERVICE = "STOP_METRONOME_SERVICE"
    }

    private val binder = MetronomeBinder()
    private val viewModel: MetronomeViewModel = get()

    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var job: Job? = null

    inner class MetronomeBinder : Binder() {
        fun getService(): MetronomeService {
            return this@MetronomeService
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            STOP_SERVICE -> {
                viewModel.onStopClicked()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onCreate() {
        // Audio playback is handled by MetronomeEngine (shared across platforms).
        // This service only manages the foreground notification.
        job = coroutineScope.launch {
            viewModel.isPlaying.collectLatest { playing ->
                if (playing) {
                    startForegroundNotification()
                } else {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                }
            }
        }
    }

    private fun startForegroundNotification() {
        val mChannel =
            NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_LOW)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)

        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, MetronomeService::class.java).apply { action = STOP_SERVICE }

        val pStopSelf = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )

        val stopAction = Notification.Action.Builder(
            Icon.createWithResource(
                this, android.R.drawable.ic_media_pause
            ), "Stop", pStopSelf
        ).build()

        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(getText(R.string.notification_title))
            .setContentText(getText(R.string.notification_message))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setLargeIcon(Icon.createWithResource(this, R.drawable.ic_launcher_foreground))
            .setContentIntent(pendingIntent).addAction(stopAction).setDeleteIntent(pStopSelf)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        job?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun stopPlaying() {
        viewModel.onStopClicked()
    }
}
