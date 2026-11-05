package com.example.assistant.voice

import android.content.Context
import com.example.data.VoiceBackendSelection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class VoiceOutputManager(context: Context) {

    private val androidTts = AndroidTtsBackend(context)
    private val mediaPipeTts = MediaPipeVoiceBackend()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentBackendStatus = MutableStateFlow("Ready")
    val currentBackendStatus: StateFlow<String> = _currentBackendStatus.asStateFlow()

    private var currentUtteranceId: String? = null
    private var activeBackend: VoiceBackend = androidTts

    init {
        androidTts.initialize { success ->
            _currentBackendStatus.value = if (success) "System TTS Ready" else "TTS Unavailable"
        }
    }

    fun setBackendSelection(selection: VoiceBackendSelection) {
        activeBackend = when (selection) {
            VoiceBackendSelection.AUTOMATIC, VoiceBackendSelection.SYSTEM_TTS -> androidTts
            VoiceBackendSelection.KOKORO -> {
                if (mediaPipeTts.isAvailable) mediaPipeTts else androidTts
            }
        }
        _currentBackendStatus.value = activeBackend.statusDescription
    }

    fun speak(
        text: String,
        rate: Float = 1.0f,
        pitch: Float = 1.0f,
        volume: Float = 1.0f,
        onStart: (() -> Unit)? = null,
        onComplete: (() -> Unit)? = null
    ) {
        stop() // Flush any prior speech before beginning
        val utteranceId = UUID.randomUUID().toString()
        currentUtteranceId = utteranceId

        activeBackend.speak(
            text = text,
            utteranceId = utteranceId,
            rate = rate,
            pitch = pitch,
            volume = volume,
            onStart = {
                if (currentUtteranceId == utteranceId) {
                    _isSpeaking.value = true
                    onStart?.invoke()
                }
            },
            onDone = {
                if (currentUtteranceId == utteranceId) {
                    _isSpeaking.value = false
                    currentUtteranceId = null
                    onComplete?.invoke()
                }
            },
            onError = { _ ->
                if (currentUtteranceId == utteranceId) {
                    _isSpeaking.value = false
                    currentUtteranceId = null
                }
            }
        )
    }

    /**
     * Stop immediately for barge-in interruption.
     */
    fun stop() {
        currentUtteranceId = null
        _isSpeaking.value = false
        activeBackend.stop()
    }

    fun release() {
        stop()
        androidTts.release()
        mediaPipeTts.release()
    }
}
