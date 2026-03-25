package com.merkost.metronome

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.merkost.metronome.model.AppDatastore
import com.merkost.metronome.navigation.AppNavigation
import com.merkost.metronome.ui.theme.MetronomeTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.koin.android.ext.android.get
import timber.log.Timber

class MainActivity : ComponentActivity() {

    private val appDatastore: AppDatastore = get()

    private val shouldWorkInBackground = appDatastore.backgroundPlay
        .stateIn(lifecycleScope, SharingStarted.Eagerly, false)

    private var isServiceBound: Boolean = false
    var metronomeService: MetronomeService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        bindMetronomeService()

        enableEdgeToEdge()

        setContent {
            MetronomeTheme {
                AppNavigation()
            }
        }
    }

    private fun bindMetronomeService() {
        this.bindService(
            Intent(this, MetronomeService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
        isServiceBound = true
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            metronomeService = (service as MetronomeService.MetronomeBinder).getService()
            Timber.tag("BOUND_SERVICE").d(metronomeService.toString())
        }

        override fun onServiceDisconnected(className: ComponentName) {
            metronomeService = null
            isServiceBound = false
        }
    }

    override fun onStop() {
        if (!shouldWorkInBackground.value) {
            metronomeService?.stopPlaying()
        }
        super.onStop()
    }
}
