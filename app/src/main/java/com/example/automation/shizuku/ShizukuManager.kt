package com.example.automation.shizuku

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rikka.shizuku.Shizuku

enum class ShizukuState(val label: String) {
    NOT_INSTALLED("Not Installed"),
    NOT_RUNNING("Not Running"),
    PERMISSION_REQUIRED("Permission Required"),
    CONNECTED("Connected"),
    DISCONNECTED("Disconnected"),
    ERROR("Error")
}

class ShizukuManager(private val context: Context) {

    private val _state = MutableStateFlow(ShizukuState.NOT_RUNNING)
    val state: StateFlow<ShizukuState> = _state.asStateFlow()

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkStatus()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        _state.value = ShizukuState.DISCONNECTED
    }

    private val requestPermissionResultListener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            _state.value = ShizukuState.CONNECTED
        } else {
            _state.value = ShizukuState.PERMISSION_REQUIRED
        }
    }

    fun initialize() {
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)
            checkStatus()
        } catch (_: Throwable) {
            _state.value = ShizukuState.NOT_INSTALLED
        }
    }

    fun checkStatus() {
        try {
            if (!isShizukuInstalled()) {
                _state.value = ShizukuState.NOT_INSTALLED
                return
            }

            if (Shizuku.pingBinder()) {
                if (Shizuku.isPreV11()) {
                    _state.value = ShizukuState.ERROR
                } else {
                    val hasPermission = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
                    _state.value = if (hasPermission) ShizukuState.CONNECTED else ShizukuState.PERMISSION_REQUIRED
                }
            } else {
                _state.value = ShizukuState.NOT_RUNNING
            }
        } catch (_: Throwable) {
            _state.value = ShizukuState.NOT_RUNNING
        }
    }

    fun requestPermission(requestCode: Int = 1001) {
        try {
            if (Shizuku.pingBinder()) {
                Shizuku.requestPermission(requestCode)
            }
        } catch (_: Throwable) {
            _state.value = ShizukuState.ERROR
        }
    }

    private fun isShizukuInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo("moe.shizuku.privileged.api", 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun release() {
        try {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(requestPermissionResultListener)
        } catch (_: Throwable) {}
    }
}
