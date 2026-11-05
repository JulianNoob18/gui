package com.example.automation

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.model.CommandResult

data class RegisteredAction(
    val id: String,
    val displayName: String,
    val description: String,
    val riskLevel: String, // "LOW", "MEDIUM", "HIGH"
    val execute: (Context) -> CommandResult
)

class ActionRegistry {

    val actions: List<RegisteredAction> = listOf(
        RegisteredAction(
            id = "sys_wifi",
            displayName = "Open Wi-Fi Settings",
            description = "Opens Android Wi-Fi network configuration panel.",
            riskLevel = "LOW",
            execute = { context ->
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                CommandResult(isSuccess = true, spokenResponse = "Opening Wi-Fi settings.")
            }
        ),
        RegisteredAction(
            id = "sys_bluetooth",
            displayName = "Open Bluetooth Settings",
            description = "Opens Android Bluetooth device management.",
            riskLevel = "LOW",
            execute = { context ->
                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                CommandResult(isSuccess = true, spokenResponse = "Opening Bluetooth settings.")
            }
        ),
        RegisteredAction(
            id = "sys_display",
            displayName = "Open Display Settings",
            description = "Opens Android screen and brightness settings.",
            riskLevel = "LOW",
            execute = { context ->
                val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                CommandResult(isSuccess = true, spokenResponse = "Opening Display settings.")
            }
        ),
        RegisteredAction(
            id = "sys_sound",
            displayName = "Open Sound Settings",
            description = "Opens Android volume and notification sound settings.",
            riskLevel = "LOW",
            execute = { context ->
                val intent = Intent(Settings.ACTION_SOUND_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                CommandResult(isSuccess = true, spokenResponse = "Opening Sound settings.")
            }
        ),
        RegisteredAction(
            id = "sys_battery",
            displayName = "Open Battery Settings",
            description = "Opens Android battery usage and power saver panel.",
            riskLevel = "LOW",
            execute = { context ->
                val intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                    CommandResult(isSuccess = true, spokenResponse = "Opening Battery settings.")
                } catch (_: Exception) {
                    val fallback = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(fallback)
                    CommandResult(isSuccess = true, spokenResponse = "Opening Battery Saver settings.")
                }
            }
        )
    )

    fun findById(id: String): RegisteredAction? = actions.firstOrNull { it.id == id }
}
