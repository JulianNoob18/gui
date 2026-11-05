package com.example.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

class MotionSensorManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _tiltX = MutableStateFlow(0f)
    val tiltX: StateFlow<Float> = _tiltX.asStateFlow()

    private val _tiltY = MutableStateFlow(0f)
    val tiltY: StateFlow<Float> = _tiltY.asStateFlow()

    var onSuddenMovement: (() -> Unit)? = null
    var onPhonePickedUp: (() -> Unit)? = null

    private var lastAcceleration = SensorManager.GRAVITY_EARTH
    private var lastEventTime = 0L

    fun start() {
        if (accelerometer != null) {
            sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Normalized tilt between -1 and 1
        _tiltX.value = (-x / 9.8f).coerceIn(-1f, 1f)
        _tiltY.value = (y / 9.8f).coerceIn(-1f, 1f)

        val currentAcc = sqrt(x * x + y * y + z * z)
        val delta = currentAcc - lastAcceleration
        lastAcceleration = currentAcc

        val now = System.currentTimeMillis()
        if (delta > 6.0f && now - lastEventTime > 2500) {
            lastEventTime = now
            onSuddenMovement?.invoke()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
