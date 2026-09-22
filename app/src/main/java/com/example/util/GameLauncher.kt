package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri

enum class FreeFireEdition(val packageName: String, val title: String) {
    STANDARD("com.dts.freefireth", "Free Fire"),
    MAX("com.dts.freefiremax", "Free Fire MAX")
}

data class InstalledGameStatus(
    val standardInstalled: Boolean,
    val maxInstalled: Boolean
) {
    val anyInstalled: Boolean get() = standardInstalled || maxInstalled
}

object GameLauncher {

    fun getInstalledStatus(context: Context): InstalledGameStatus {
        val pm = context.packageManager
        val standardInstalled = try {
            pm.getLaunchIntentForPackage(FreeFireEdition.STANDARD.packageName) != null
        } catch (_: Exception) {
            false
        }

        val maxInstalled = try {
            pm.getLaunchIntentForPackage(FreeFireEdition.MAX.packageName) != null
        } catch (_: Exception) {
            false
        }

        return InstalledGameStatus(standardInstalled, maxInstalled)
    }

    fun launchGame(context: Context, edition: FreeFireEdition): Boolean {
        val pm = context.packageManager
        val intent = pm.getLaunchIntentForPackage(edition.packageName) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        return true
    }

    fun openPlayStore(context: Context, packageName: String) {
        try {
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(marketIntent)
        } catch (_: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(webIntent)
        }
    }
}
