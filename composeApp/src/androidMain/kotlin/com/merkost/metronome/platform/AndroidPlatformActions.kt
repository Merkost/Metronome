package com.merkost.metronome.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast

class AndroidPlatformActions(private val context: Context) : PlatformActions {

    override fun contactSupport() {
        runCatching {
            val osVersion = Build.VERSION.RELEASE
            val phoneModel = Build.MODEL
            val manufacturer = Build.MANUFACTURER
            val appVersionName = AndroidAppVersionProvider(context).getAppVersion()?.versionName
            val appVersionNumber = AndroidAppVersionProvider(context).getAppVersion()?.versionNumber

            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("merkostdev+metronome@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Support Request from Metronome App")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Please describe the issue you're experiencing or your question below:\n" +
                            "\n\n\n\n\n" +
                            "Device Information:\n" +
                            "OS Version: $osVersion\n" +
                            "Phone Model: $phoneModel\n" +
                            "Manufacturer: $manufacturer\n" +
                            "App version: $appVersionName ($appVersionNumber)\n"
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(emailIntent, "Send email...").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }.onFailure {
            Toast.makeText(context, "There are no email clients installed.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun rateApp() {
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
                    .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            )
        }.onFailure {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=${context.packageName}"))
                    .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            )
        }
    }

    override fun isDynamicColorSupported(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}
