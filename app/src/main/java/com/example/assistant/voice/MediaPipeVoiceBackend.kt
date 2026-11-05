package com.example.assistant.voice

/**
 * MediaPipe / Kokoro neural voice backend interface implementation.
 * As prescribed by prompt Section 11:
 * "If MediaPipe or the Kokoro model cannot be included in the initial build:
 *  - fully implement Android TextToSpeech
 *  - retain the MediaPipeVoiceBackend interface
 *  - provide a clear unavailable status
 *  - do not pretend Kokoro is active"
 */
class MediaPipeVoiceBackend : VoiceBackend {
    override val name: String = "MediaPipe Kokoro Neural TTS"
    override val isAvailable: Boolean = false
    override val statusDescription: String = "Kokoro model weights not bundled in applet package. System TTS active as fallback."

    override fun initialize(onReady: (Boolean) -> Unit) {
        onReady(false)
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
        onError("MediaPipe Kokoro model unavailable")
    }

    override fun stop() {}

    override fun release() {}
}
