package com.merkost.metronome

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.drawable.Icon
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.viewModelScope
import com.merkost.metronome.model.AppDatastore
import com.merkost.metronome.model.MetronomeState
import com.merkost.metronome.model.StopWatchState
import com.merkost.metronome.viewModels.MetronomeViewModel
import com.merkost.metronome.viewModels.getNextIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class MetronomeService : Service(), KoinComponent {

    companion object {
        private const val TAG = "METRONOME_SERVICE"
        private const val CHANNEL_ID = "METRONOME SERVICE"
        private const val STOP_SERVICE = "STOP_METRONOME_SERVICE"
        const val MAX_BPM = 220
        const val MIN_BPM = 40
    }

    private val appDatastore: AppDatastore = get()

    var stopWatch = StopWatchState()
    val isPlaying = MutableStateFlow(false)
    var metronomeState = MetronomeState()
    var index = -1
    private val binder = MetronomeBinder()
    private val viewModel: MetronomeViewModel = get()

    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    var job: Job? = null

    inner class MetronomeBinder : Binder() {
        fun getService(): MetronomeService {
            return this@MetronomeService
        }
    }


    override fun onCreate() {
        val soundPool: SoundPool =
            SoundPool.Builder()
                .setMaxStreams(4) // to prevent delaying the next tick under any circumstances
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .build()

        val sound = soundPool.load(this, R.raw.wood, 1)

        job = coroutineScope.launch() {


            isPlaying.collectLatest { playing ->
                if (playing.not()) {
                    appDatastore.addTotalTime(stopWatch.elapsedTime)
                } else {
                    launch { startTimerCoroutine() }
                }

                val stereoPanningLeft = viewModel.currentStereo.value.first.toFloat()
                val stereoPanningRight = viewModel.currentStereo.value.second.toFloat()

                while (playing) {
                    var rate = 1f
                    if (index == 0)
                        rate = 1.4f

                    if (index >= 0) soundPool.play(
                        sound,
                        stereoPanningLeft,
                        stereoPanningRight,
                        1,
                        0,
                        rate
                    )
                    index = getNextIndex(index)
                    delay(metronomeState.interval.toLong())
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
            0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent =
            Intent(this, MetronomeService::class.java).apply { action = STOP_SERVICE }

        val pStopSelf =
            PendingIntent.getService(
                this,
                0,
                stopIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
            )

        val stopAction = Notification.Action.Builder(
            Icon.createWithResource(
                this,
                android.R.drawable.ic_media_pause
            ), "Stop", pStopSelf
        ).build()

        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(getText(R.string.notification_title))
            .setContentText(getText(R.string.notification_message))
//            .setSmallIcon(R.drawable.ic_metronome_icon_white)
//            .setLargeIcon(Icon.createWithResource(this, R.drawable.ic_metronome_icon_circle_bg))
            .setContentIntent(pendingIntent)
            .addAction(stopAction)
            .setDeleteIntent(pStopSelf)
            .build()

        startForeground(1, notification)
    }

    fun onPlayPauseClicked() {
        isPlaying.update { it.not() }
    }

    override fun onDestroy() {
//        job?.cancel()
    }


    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun stopPlaying() {
        viewModel.onStopClicked()
    }

    private suspend fun startTimerCoroutine() {
        val beginTimeMillis = System.currentTimeMillis()
        stopWatch = StopWatchState(0, 0)
        while (true) {
            val currentTimeMillis = System.currentTimeMillis()
            stopWatch = StopWatchState(
                beginTimeMillis,
                currentTimeMillis - beginTimeMillis
            )
            delay(20)
        }
    }

}