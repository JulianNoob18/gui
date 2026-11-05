package com.example.automation

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.model.CommandResult

class CastingManager(private val context: Context) {

    fun openCastingControls(): CommandResult {
        return try {
            val intent = Intent(Settings.ACTION_CAST_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            CommandResult(
                isSuccess = true,
                spokenResponse = "Opening casting controls.",
                detailMessage = "Launched Android cast settings"
            )
        } catch (e: Exception) {
            // Fallback to wireless display or standard connected devices
            try {
                val fallbackIntent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
                CommandResult(
                    isSuccess = true,
                    spokenResponse = "Opening connected devices controls.",
                    detailMessage = "Fallback cast controls opened"
                )
            } catch (err: Exception) {
                CommandResult(
                    isSuccess = false,
                    spokenResponse = "Casting controls are not available on this device.",
                    detailMessage = err.message ?: "Failed to open cast controls"
                )
            }
        }
    }
}
