package com.merkost.metronome.components

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.merkost.metronome.R
import timber.log.Timber

@Composable
fun checkNotificationPolicyAccess(
    notificationManager: NotificationManager,
    context: Context
): Boolean {
    if (notificationManager.areNotificationsEnabled()) {
        return true
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) PermissionDialog(context)
        else OldPermissionDialog(context = context)
    }
    return false
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun PermissionDialog(context: Context) {
    var openDialog by remember { mutableStateOf(true) }
    val notificationPermissions =
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)

    LaunchedEffect(notificationPermissions.status) {
        if (notificationPermissions.status.isGranted) {
            openDialog = false
        }
    }

    if (openDialog) {
        AlertDialog(onDismissRequest = {
            openDialog = false
        },
            title = { Text(stringResource(R.string.notification_permission_title)) },
            text = { Text(stringResource(R.string.notification_permission_description)) },
            dismissButton = {
                Button(
                    onClick = {
                        openDialog = false
                    },
                ) {
                    Text(text = "No")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (notificationPermissions.status.shouldShowRationale) {
                            context.startNotificationSettings()
                        } else {
                            notificationPermissions.launchPermissionRequest()
                        }
                    },
                ) { Text(text = "Yes") }
            }
        )
    }
}


@Composable
internal fun OldPermissionDialog(context: Context) {
    var openDialog by remember { mutableStateOf(true) }

    if (openDialog) {
        AlertDialog(onDismissRequest = {
            openDialog = false
        },
            title = { Text(stringResource(R.string.notification_permission_title)) },
            text = { Text(stringResource(R.string.notification_permission_description)) },
            dismissButton = {
                Button(
                    onClick = { openDialog = false },
                ) {
                    Text(text = "No")
                }
            },
            confirmButton = {
                Button(
                    onClick = { context.startNotificationSettings() },
                ) { Text(text = "Yes") }
            })
    }
}

private fun Context.startNotificationSettings() {
    try {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    } catch (e: Exception) {
        Timber.w(e, "startNotificationSettings")
    }
}