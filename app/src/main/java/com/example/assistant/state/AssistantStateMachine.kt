package com.example.assistant.state

import com.example.model.AssistantState
import com.example.model.GuiExpression
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Authoritative AssistantStateMachine governing all assistant states,
 * facial expressions, transcripts, and barge-in interruption.
 */
class AssistantStateMachine(
    private val scope: CoroutineScope
) {
    private val _currentState = MutableStateFlow(AssistantState.IDLE)
    val currentState: StateFlow<AssistantState> = _currentState.asStateFlow()

    private val _currentExpression = MutableStateFlow(GuiExpression.NEUTRAL)
    val currentExpression: StateFlow<GuiExpression> = _currentExpression.asStateFlow()

    private val _transcript = MutableStateFlow("")
    val transcript: StateFlow<String> = _transcript.asStateFlow()

    private val _statusText = MutableStateFlow("")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    private val _isMicrophoneActive = MutableStateFlow(false)
    val isMicrophoneActive: StateFlow<Boolean> = _isMicrophoneActive.asStateFlow()

    private val _isFollowUpWindowActive = MutableStateFlow(false)
    val isFollowUpWindowActive: StateFlow<Boolean> = _isFollowUpWindowActive.asStateFlow()

    private var expressionOverrideJob: Job? = null
    private var transcriptFadeJob: Job? = null
    private var followUpJob: Job? = null
    private var isManualExpressionLabActive = false

    var onInterruptRequest: (() -> Unit)? = null
    var onStartListeningRequest: ((wakeWord: String?) -> Unit)? = null

    fun transitionTo(newState: AssistantState, customStatus: String? = null) {
        if (_currentState.value == AssistantState.PRIVACY && newState != AssistantState.IDLE) {
            // Stay in privacy mode unless explicitly toggled back to idle
            return
        }
        _currentState.value = newState
        _isMicrophoneActive.value = newState.isMicActive || _isFollowUpWindowActive.value

        customStatus?.let { _statusText.value = it }

        if (!isManualExpressionLabActive) {
            _currentExpression.value = newState.defaultExpression
        }
    }

    fun updateTranscript(text: String, isFinal: Boolean = false) {
        _transcript.value = text
        transcriptFadeJob?.cancel()
        if (isFinal && text.isNotBlank()) {
            transcriptFadeJob = scope.launch {
                delay(4000)
                _transcript.value = ""
            }
        }
    }

    fun clearTranscript() {
        transcriptFadeJob?.cancel()
        _transcript.value = ""
    }

    fun setTemporaryExpression(expression: GuiExpression, durationMs: Long = 1800) {
        if (isManualExpressionLabActive) return
        expressionOverrideJob?.cancel()
        _currentExpression.value = expression
        expressionOverrideJob = scope.launch {
            delay(durationMs)
            _currentExpression.value = _currentState.value.defaultExpression
        }
    }

    fun setExpressionLabPreview(expression: GuiExpression) {
        isManualExpressionLabActive = true
        expressionOverrideJob?.cancel()
        _currentExpression.value = expression
    }

    fun returnToAutoExpression() {
        isManualExpressionLabActive = false
        expressionOverrideJob?.cancel()
        _currentExpression.value = _currentState.value.defaultExpression
    }

    /**
     * Mandatory barge-in interruption.
     */
    fun interrupt(reason: String = "User Interrupted") {
        followUpJob?.cancel()
        _isFollowUpWindowActive.value = false
        onInterruptRequest?.invoke()
        transitionTo(AssistantState.ATTENTIVE, customStatus = "Interrupted")
    }

    /**
     * Start post-response follow-up conversation window.
     */
    fun startFollowUpWindow(seconds: Int) {
        if (seconds <= 0 || _currentState.value == AssistantState.PRIVACY) return
        followUpJob?.cancel()
        _isFollowUpWindowActive.value = true
        _isMicrophoneActive.value = true
        onStartListeningRequest?.invoke(null)

        followUpJob = scope.launch {
            delay(seconds * 1000L)
            _isFollowUpWindowActive.value = false
            if (_currentState.value == AssistantState.IDLE || _currentState.value == AssistantState.LISTENING) {
                _isMicrophoneActive.value = false
                transitionTo(AssistantState.IDLE)
            }
        }
    }

    fun closeFollowUpWindow() {
        followUpJob?.cancel()
        _isFollowUpWindowActive.value = false
        if (_currentState.value == AssistantState.IDLE) {
            _isMicrophoneActive.value = false
        }
    }

    fun togglePrivacyMode(): Boolean {
        return if (_currentState.value == AssistantState.PRIVACY) {
            transitionTo(AssistantState.IDLE, customStatus = "Privacy disabled")
            false
        } else {
            interrupt("Privacy enabled")
            clearTranscript()
            closeFollowUpWindow()
            _currentState.value = AssistantState.PRIVACY
            _currentExpression.value = GuiExpression.PRIVACY
            _isMicrophoneActive.value = false
            true
        }
    }
}
