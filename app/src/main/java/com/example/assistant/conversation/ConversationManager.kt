package com.example.assistant.conversation

import com.example.assistant.commands.CommandParser
import com.example.assistant.commands.CommandRouter
import com.example.assistant.hotword.DefaultHotwordEngine
import com.example.assistant.hotword.HotwordType
import com.example.assistant.speech.SpeechInputManager
import com.example.assistant.state.AssistantStateMachine
import com.example.assistant.voice.VoiceOutputManager
import com.example.data.AutomationHistoryRepository
import com.example.data.GuiSettings
import com.example.data.SettingsRepository
import com.example.model.AssistantState
import com.example.model.CommandType
import com.example.model.GuiExpression
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ConversationManager(
    private val scope: CoroutineScope,
    val stateMachine: AssistantStateMachine,
    val speechInput: SpeechInputManager,
    val voiceOutput: VoiceOutputManager,
    val commandParser: CommandParser,
    val commandRouter: CommandRouter,
    val historyRepo: AutomationHistoryRepository,
    val settingsRepo: SettingsRepository
) {
    val hotwordEngine = DefaultHotwordEngine()
    private var currentSettings = GuiSettings()

    var onOpenOwnerConsoleRequest: (() -> Unit)? = null
    var onWakeWordBrohamDetected: (() -> Unit)? = null

    init {
        scope.launch {
            settingsRepo.settingsFlow.collectLatest { settings ->
                currentSettings = settings
                hotwordEngine.setWakeWordToggles(
                    master = settings.masterWakeWords,
                    gui = settings.wakeGui,
                    dude = settings.wakeDude,
                    broham = settings.wakeBroham
                )
                voiceOutput.setBackendSelection(settings.voiceBackend)
            }
        }

        // Setup speech recognition callbacks
        speechInput.onPartialResult = { partial ->
            stateMachine.transitionTo(AssistantState.TRANSCRIBING)
            stateMachine.updateTranscript(partial, isFinal = false)
        }

        speechInput.onFinalResult = { recognizedText ->
            processRecognizedSpeech(recognizedText)
        }

        speechInput.onError = { errorMsg ->
            if (stateMachine.currentState.value == AssistantState.LISTENING ||
                stateMachine.currentState.value == AssistantState.TRANSCRIBING
            ) {
                stateMachine.transitionTo(AssistantState.ERROR, customStatus = errorMsg)
                scope.launch {
                    delay(1500)
                    if (stateMachine.currentState.value == AssistantState.ERROR) {
                        stateMachine.transitionTo(AssistantState.IDLE)
                    }
                }
            }
        }

        stateMachine.onInterruptRequest = {
            voiceOutput.stop()
            speechInput.stopListening()
        }

        stateMachine.onStartListeningRequest = { wakeWord ->
            startListeningInternal(wakeWord)
        }
    }

    fun startListeningWithWakeWord(wakeWord: String?) {
        if (stateMachine.currentState.value == AssistantState.PRIVACY) return

        // Instant acknowledgment reaction
        when (wakeWord?.lowercase()) {
            "dude" -> {
                stateMachine.setTemporaryExpression(GuiExpression.COOL, durationMs = 800)
            }
            "broham" -> {
                stateMachine.setTemporaryExpression(GuiExpression.HEROIC, durationMs = 1200)
                onWakeWordBrohamDetected?.invoke()
            }
            else -> {
                stateMachine.setTemporaryExpression(GuiExpression.ATTENTIVE, durationMs = 600)
            }
        }

        startListeningInternal(wakeWord)
    }

    private fun startListeningInternal(wakeWord: String?) {
        voiceOutput.stop()
        stateMachine.transitionTo(AssistantState.LISTENING, customStatus = "Listening...")
        speechInput.startListening()
    }

    fun triggerManualListening() {
        if (stateMachine.currentState.value == AssistantState.PRIVACY) return
        voiceOutput.stop()
        stateMachine.transitionTo(AssistantState.LISTENING, customStatus = "Listening...")
        speechInput.startListening()
    }

    fun emergencyStop() {
        voiceOutput.stop()
        speechInput.stopListening()
        stateMachine.closeFollowUpWindow()
        stateMachine.clearTranscript()
        stateMachine.transitionTo(AssistantState.IDLE, customStatus = "Stopped")
        stateMachine.returnToAutoExpression()
    }

    private fun processRecognizedSpeech(spokenText: String) {
        stateMachine.updateTranscript(spokenText, isFinal = true)
        val (wakeWord, commandText) = hotwordEngine.processSpokenText(spokenText)

        // Check if wake word required
        val finalCommandText = if (currentSettings.requireWakeWord && wakeWord == null && !stateMachine.isFollowUpWindowActive.value) {
            stateMachine.transitionTo(AssistantState.CONFUSED, customStatus = "Wake word required")
            return
        } else {
            commandText.ifBlank { spokenText }
        }

        // Acknowledge wake word if present
        if (wakeWord == HotwordType.BROHAM) {
            onWakeWordBrohamDetected?.invoke()
            stateMachine.setTemporaryExpression(GuiExpression.HEROIC, durationMs = 900)
        } else if (wakeWord == HotwordType.DUDE) {
            stateMachine.setTemporaryExpression(GuiExpression.COOL, durationMs = 800)
        }

        stateMachine.transitionTo(AssistantState.UNDERSTANDING, customStatus = "Understanding...")

        val parsed = commandParser.parse(finalCommandText, wakeWord?.name)

        if (parsed.type == CommandType.OWNER_CONSOLE_REQUEST) {
            onOpenOwnerConsoleRequest?.invoke()
            stateMachine.transitionTo(AssistantState.SUCCESS, customStatus = "Opening console")
            speakFeedback("Owner console requested.")
            return
        }

        if (parsed.type == CommandType.SLEEP) {
            stateMachine.transitionTo(AssistantState.SLEEPING, customStatus = "Sleeping")
            speakFeedback("Entering sleep mode.")
            return
        }

        if (parsed.type == CommandType.WAKE) {
            stateMachine.transitionTo(AssistantState.IDLE, customStatus = "Online")
            speakFeedback("GUI online.")
            return
        }

        if (parsed.type == CommandType.GUI_SETTING && parsed.target == "privacy") {
            val enabled = stateMachine.togglePrivacyMode()
            scope.launch { settingsRepo.updatePrivacyMode(enabled) }
            speakFeedback(if (enabled) "Privacy mode active." else "Privacy mode disabled.")
            return
        }

        if (parsed.type == CommandType.GUI_EXPRESSION) {
            try {
                val expr = GuiExpression.valueOf(parsed.target)
                stateMachine.setTemporaryExpression(expr, durationMs = 4000)
                speakFeedback("Setting face expression to ${expr.displayName.lowercase()}.")
            } catch (_: Exception) {}
            return
        }

        // Execute action
        stateMachine.transitionTo(AssistantState.WORKING, customStatus = "Working on it...")
        val result = commandRouter.execute(parsed)

        historyRepo.recordAction(
            commandText = spokenText,
            actionType = parsed.type.name,
            resultMessage = result.detailMessage.ifBlank { result.spokenResponse },
            isSuccess = result.isSuccess
        )

        if (result.isSuccess) {
            stateMachine.transitionTo(AssistantState.SUCCESS, customStatus = result.spokenResponse)
        } else {
            stateMachine.transitionTo(AssistantState.CONFUSED, customStatus = result.spokenResponse)
        }

        speakFeedback(result.spokenResponse) {
            if (result.isSuccess && currentSettings.followUpWindowSeconds > 0) {
                stateMachine.startFollowUpWindow(currentSettings.followUpWindowSeconds)
            } else {
                scope.launch {
                    delay(2000)
                    if (stateMachine.currentState.value == AssistantState.SUCCESS ||
                        stateMachine.currentState.value == AssistantState.CONFUSED
                    ) {
                        stateMachine.transitionTo(AssistantState.IDLE)
                    }
                }
            }
        }
    }

    private fun speakFeedback(text: String, onDone: (() -> Unit)? = null) {
        if (!currentSettings.voiceOutputEnabled || stateMachine.currentState.value == AssistantState.PRIVACY) {
            onDone?.invoke()
            return
        }

        stateMachine.transitionTo(AssistantState.SPEAKING)
        voiceOutput.speak(
            text = text,
            rate = currentSettings.speechRate,
            pitch = currentSettings.speechPitch,
            volume = currentSettings.voiceVolume,
            onStart = {
                stateMachine.transitionTo(AssistantState.SPEAKING)
            },
            onComplete = {
                onDone?.invoke()
            }
        )
    }

    fun release() {
        speechInput.stopListening()
        voiceOutput.release()
    }
}
