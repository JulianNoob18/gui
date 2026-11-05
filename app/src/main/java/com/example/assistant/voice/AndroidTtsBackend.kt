package com.example.assistant.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class AndroidTtsBackend(
    private val context: Context
) : VoiceBackend {

    override val name: String = "Android System TTS"
    override var isAvailable: Boolean = false
        private set
    override var statusDescription: String = "Initializing..."
        private set

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    override fun initialize(onReady: (Boolean) -> Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    isAvailable = false
                    statusDescription = "US English language not supported or missing data."
                    onReady(false)
                } else {
                    isAvailable = true
                    isInitialized = true
                    statusDescription = "Ready (System TTS Engine)"
                    onReady(true)
                }
            } else {
                isAvailable = false
                statusDescription = "Failed to initialize System TTS engine."
                onReady(false)
            }
        }
    }

    override fun speak(
        text: String,
        utteranceId: String,
        rate: Float,
        pitch: Float,
        volume: Float,
        onStart: () -> Unit,
        onDone: () -> Unit,
        onError: (String) -> Unit
    ) {
        val engine = tts
        if (!isInitialized || engine == null) {
            onError("TTS engine not initialized")
            return
        }

        engine.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
        engine.setPitch(pitch.coerceIn(0.5f, 2.0f))

        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {
                if (id == utteranceId) onStart()
            }

            override fun onDone(id: String?) {
                if (id == utteranceId) onDone()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(id: String?) {
                if (id == utteranceId) onError("TTS playback error")
            }

            override fun onError(id: String?, errorCode: Int) {
                if (id == utteranceId) onError("TTS playback error ($errorCode)")
            }
        })

        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume.coerceIn(0f, 1f))
        }

        // QUEUE_FLUSH ensures old speech is instantly cleared before playing
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    override fun stop() {
        tts?.stop()
    }

    override fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
