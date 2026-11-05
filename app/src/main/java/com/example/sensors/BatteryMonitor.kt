package com.example.sensors

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BatteryStatus(
    val level: Int = 100,
    val isCharging: Boolean = false,
    val isFull: Boolean = false
)

class BatteryMonitor(private val context: Context) {

    private val _status = MutableStateFlow(BatteryStatus())
    val status: StateFlow<BatteryStatus> = _status.asStateFlow()

    var onChargingStarted: (() -> Unit)? = null
    var onBatteryLow: (() -> Unit)? = null
    var onBatteryFull: (() -> Unit)? = null

    private var wasCharging = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 100
                val statusExtra = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = statusExtra == BatteryManager.BATTERY_STATUS_CHARGING ||
                        statusExtra == BatteryManager.BATTERY_STATUS_FULL
                val isFull = statusExtra == BatteryManager.BATTERY_STATUS_FULL || batteryPct >= 99

                _status.value = BatteryStatus(level = batteryPct, isCharging = isCharging, isFull = isFull)

                if (!wasCharging && isCharging) {
                    onChargingStarted?.invoke()
                }
                wasCharging = isCharging

                if (batteryPct <= 15 && !isCharging) {
                    onBatteryLow?.invoke()
                } else if (isFull && isCharging) {
                    onBatteryFull?.invoke()
                }
            }
        }
    }

    fun start() {
        try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            context.registerReceiver(receiver, filter)
        } catch (_: Exception) {}
    }

    fun stop() {
        try {
            context.unregisterReceiver(receiver)
        } catch (_: Exception) {}
    }
}
