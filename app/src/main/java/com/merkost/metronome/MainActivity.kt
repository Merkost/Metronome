package com.merkost.metronome

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.merkost.metronome.components.checkNotificationPolicyAccess
import com.merkost.metronome.model.AppDatastore
import com.merkost.metronome.screens.MainScreen
import com.merkost.metronome.screens.SettingsScreen
import com.merkost.metronome.ui.theme.MetronomeTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.koin.android.ext.android.get

class MainActivity : ComponentActivity() {

    private val appDatastore: AppDatastore = get()

    private val shouldWorkInBackground = appDatastore.backgroundPlay
        .stateIn(lifecycleScope, SharingStarted.Eagerly, false)

    private var isServiceBound: Boolean = false
    var metronomeService: MetronomeService? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        bindService()

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        setContent {
            MetronomeTheme {
                checkNotificationPolicyAccess(
                    notificationManager = notificationManager,
                    context = this
                )
                Navigator()
            }
        }

    }


    private fun bindService() {
        this.bindService(
            Intent(
                this,
                MetronomeService::class.java
            ), serviceConnection, Context.BIND_AUTO_CREATE
        )
        isServiceBound = true
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            metronomeService = (service as MetronomeService.MetronomeBinder).getService()
            Log.e("BINDED_SERVICE", metronomeService.toString())
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

object MainDestinations {
    const val MAIN = "main"
    const val SETTINGS = "settings"
}

@Composable
fun Navigator() {
    val navController = rememberNavController()

    NavHost(
        modifier = Modifier,
        navController = navController,
        startDestination = MainDestinations.MAIN,
    ) {

        composable(MainDestinations.MAIN) {
            MainScreen {
                navController.navigate(MainDestinations.SETTINGS)
            }
        }

        composable(MainDestinations.SETTINGS) {
            SettingsScreen(upPress = navController::popBackStack)
        }

    }
}

