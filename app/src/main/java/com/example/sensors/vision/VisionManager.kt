package com.example.sensors.vision

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executors

class VisionManager(private val context: Context) {

    private val _isVisionActive = MutableStateFlow(false)
    val isVisionActive: StateFlow<Boolean> = _isVisionActive.asStateFlow()

    private val _motionOffsetX = MutableStateFlow(0f)
    val motionOffsetX: StateFlow<Float> = _motionOffsetX.asStateFlow()

    private val _motionOffsetY = MutableStateFlow(0f)
    val motionOffsetY: StateFlow<Float> = _motionOffsetY.asStateFlow()

    private val _estimatedBrightness = MutableStateFlow(0.8f)
    val estimatedBrightness: StateFlow<Float> = _estimatedBrightness.asStateFlow()

    var onSignificantMotionDetected: (() -> Unit)? = null

    private var cameraProvider: ProcessCameraProvider? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var lastLuma = 0.0

    fun startVision(lifecycleOwner: LifecycleOwner, hasCameraPermission: Boolean) {
        if (!hasCameraPermission) return
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                val previewSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    analyzeFrame(imageProxy)
                }

                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(lifecycleOwner, previewSelector, imageAnalysis)
                _isVisionActive.value = true
            } catch (_: Exception) {
                _isVisionActive.value = false
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun analyzeFrame(image: ImageProxy) {
        val plane = image.planes.firstOrNull() ?: return image.close()
        val buffer = plane.buffer
        val data = ByteArray(buffer.remaining())
        buffer.get(data)

        var totalLuma = 0L
        for (byte in data) {
            totalLuma += (byte.toInt() and 0xFF)
        }
        val avgLuma = if (data.isNotEmpty()) totalLuma.toDouble() / data.size else 128.0
        val normalizedBrightness = (avgLuma / 255.0).toFloat().coerceIn(0.2f, 1.2f)
        _estimatedBrightness.value = normalizedBrightness

        val delta = Math.abs(avgLuma - lastLuma)
        lastLuma = avgLuma

        if (delta > 25.0) {
            onSignificantMotionDetected?.invoke()
        }
        image.close()
    }

    fun stopVision() {
        try {
            cameraProvider?.unbindAll()
        } catch (_: Exception) {}
        _isVisionActive.value = false
    }

    fun release() {
        stopVision()
        cameraExecutor.shutdown()
    }
}
