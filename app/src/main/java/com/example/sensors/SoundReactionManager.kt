package com.example.sensors

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class SoundIntensity {
    QUIET, NORMAL, NOTICEABLE, LOUD, SUDDEN
}

class SoundReactionManager(
    private val scope: CoroutineScope
) {
    private var audioRecord: AudioRecord? = null
    private var pollingJob: Job? = null
    private var isRunning = false

    var onSoundIntensityDetected: ((SoundIntensity) -> Unit)? = null

    @SuppressLint("MissingPermission")
    fun start(hasPermission: Boolean) {
        if (!hasPermission || isRunning) return
        try {
            val sampleRate = 8000
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
                audioRecord?.startRecording()
                isRunning = true
                startPolling(bufferSize)
            }
        } catch (_: Exception) {
            stop()
        }
    }

    private fun startPolling(bufferSize: Int) {
        pollingJob?.cancel()
        pollingJob = scope.launch(Dispatchers.Default) {
            val buffer = ShortArray(bufferSize)
            var lastPeak = 0
            while (isActive && isRunning) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    var maxAmp = 0
                    for (i in 0 until read) {
                        val absVal = abs(buffer[i].toInt())
                        if (absVal > maxAmp) maxAmp = absVal
                    }

                    val intensity = when {
                        maxAmp > 24000 && (maxAmp - lastPeak > 12000) -> SoundIntensity.SUDDEN
                        maxAmp > 20000 -> SoundIntensity.LOUD
                        maxAmp > 10000 -> SoundIntensity.NOTICEABLE
                        maxAmp > 3000 -> SoundIntensity.NORMAL
                        else -> SoundIntensity.QUIET
                    }

                    lastPeak = maxAmp
                    if (intensity != SoundIntensity.QUIET && intensity != SoundIntensity.NORMAL) {
                        onSoundIntensityDetected?.invoke(intensity)
                        delay(1200) // Debounce sound reactions
                    }
                }
                delay(120)
            }
        }
    }

    fun stop() {
        isRunning = false
        pollingJob?.cancel()
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
    }
}
