package com.example.automation

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.model.CommandResult

class AppLauncher(private val context: Context) {

    fun launchAppByName(appNameQuery: String): CommandResult {
        val cleanQuery = appNameQuery.trim().lowercase()
        if (cleanQuery.isBlank()) {
            return CommandResult(
                isSuccess = false,
                spokenResponse = "I didn't catch the app name.",
                detailMessage = "App name was empty."
            )
        }

        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)

        // Find best match by label
        val matchedApp = resolveInfos.firstOrNull { info ->
            val label = info.loadLabel(pm).toString().lowercase()
            label == cleanQuery || label.contains(cleanQuery)
        }

        return if (matchedApp != null) {
            val packageName = matchedApp.activityInfo.packageName
            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    context.startActivity(launchIntent)
                    val label = matchedApp.loadLabel(pm).toString()
                    CommandResult(
                        isSuccess = true,
                        spokenResponse = "Opening $label.",
                        detailMessage = "Launched package: $packageName"
                    )
                } catch (e: Exception) {
                    CommandResult(
                        isSuccess = false,
                        spokenResponse = "Could not launch $cleanQuery.",
                        detailMessage = e.message ?: "Launch failed"
                    )
                }
            } else {
                CommandResult(
                    isSuccess = false,
                    spokenResponse = "Could not find a launch intent for $cleanQuery.",
                    detailMessage = "No launcher intent for package: $packageName"
                )
            }
        } else {
            // Check common system shortcuts if not found in launcher list
            when (cleanQuery) {
                "settings", "android settings" -> {
                    val settingsIntent = Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
                    CommandResult(isSuccess = true, spokenResponse = "Opening Settings.")
                }
                "calculator" -> {
                    CommandResult(isSuccess = false, spokenResponse = "Calculator app is not installed.")
                }
                "spotify" -> {
                    CommandResult(isSuccess = false, spokenResponse = "Spotify is not installed on this device.")
                }
                "youtube" -> {
                    CommandResult(isSuccess = false, spokenResponse = "YouTube is not installed on this device.")
                }
                else -> {
                    CommandResult(
                        isSuccess = false,
                        spokenResponse = "I couldn't find an installed app named $appNameQuery.",
                        detailMessage = "No installed app matched '$appNameQuery'"
                    )
                }
            }
        }
    }
}
