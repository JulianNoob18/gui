package com.example.assistant.voice

interface VoiceBackend {
    val name: String
    val isAvailable: Boolean
    val statusDescription: String

    fun initialize(onReady: (Boolean) -> Unit)
    fun speak(
        text: String,
        utteranceId: String,
        rate: Float,
        pitch: Float,
        volume: Float,
        onStart: () -> Unit,
        onDone: () -> Unit,
        onError: (String) -> Unit
    )
    fun stop()
    fun release()
}
