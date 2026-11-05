package com.example.assistant.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechInputManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListeningInternal = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isAvailable = MutableStateFlow(SpeechRecognizer.isRecognitionAvailable(context))
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    var onPartialResult: ((String) -> Unit)? = null
    var onFinalResult: ((String) -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onSpeechStarted: (() -> Unit)? = null
    var onSpeechEnded: (() -> Unit)? = null

    fun startListening() {
        if (isListeningInternal) return
        stopListening()

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError?.invoke("Speech recognition not available on device")
            return
        }

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createListener())
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            speechRecognizer?.startListening(intent)
            isListeningInternal = true
            _isListening.value = true
        } catch (e: Exception) {
            isListeningInternal = false
            _isListening.value = false
            onError?.invoke("Failed to start speech input: ${e.message}")
        }
    }

    fun stopListening() {
        isListeningInternal = false
        _isListening.value = false
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {
        } finally {
            speechRecognizer = null
        }
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                onSpeechStarted?.invoke()
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                isListeningInternal = false
                _isListening.value = false
                onSpeechEnded?.invoke()
            }

            override fun onError(error: Int) {
                isListeningInternal = false
                _isListening.value = false
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client speech error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                    SpeechRecognizer.ERROR_NETWORK -> "Network connection error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timed out"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech match found"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech input timed out"
                    else -> "Speech recognition error ($error)"
                }
                onError?.invoke(errorMsg)
            }

            override fun onResults(results: Bundle?) {
                isListeningInternal = false
                _isListening.value = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognizedText = matches?.firstOrNull().orEmpty()
                if (recognizedText.isNotBlank()) {
                    onFinalResult?.invoke(recognizedText)
                } else {
                    onError?.invoke("No recognized text")
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partialText = matches?.firstOrNull().orEmpty()
                if (partialText.isNotBlank()) {
                    onPartialResult?.invoke(partialText)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }
}
