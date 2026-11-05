package com.example.ui.face

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assistant.conversation.ConversationManager
import com.example.assistant.state.AssistantStateMachine
import com.example.data.GuiPersonality
import com.example.data.SettingsRepository
import com.example.model.AssistantState
import com.example.model.GuiExpression
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

data class FaceVisualState(
    val eyeScaleX: Float = 1.0f,
    val eyeScaleY: Float = 1.0f,
    val leftUpperLid: Float = 0.05f,
    val rightUpperLid: Float = 0.05f,
    val lowerLid: Float = 0.05f,
    val leftBrowHeight: Float = 0.0f,
    val rightBrowHeight: Float = 0.0f,
    val leftBrowTilt: Float = 0.0f,
    val rightBrowTilt: Float = 0.0f,
    val lookX: Float = 0.0f,
    val lookY: Float = 0.0f,
    val mouthHeight: Float = 0.2f,
    val mouthWidth: Float = 0.6f,
    val jawGlow: Float = 0.25f,
    val cheekGlow: Float = 0.3f,
    val templeGlow: Float = 0.25f,
    val circuitPulse: Float = 1.0f,
    val isLoveEyes: Boolean = false,
    val isWinkingLeft: Boolean = false,
    val isWinkingRight: Boolean = false,
    val energyOffset: Float = 0.0f
)

class FaceViewModel(
    val stateMachine: AssistantStateMachine,
    val conversationManager: ConversationManager,
    val settingsRepo: SettingsRepository
) : ViewModel() {

    private val _visualState = MutableStateFlow(FaceVisualState())
    val visualState: StateFlow<FaceVisualState> = _visualState.asStateFlow()

    private var idleJob: Job? = null
    private var speakingAnimJob: Job? = null
    private var currentPersonality = GuiPersonality.NORMAL
    private var jawMultiplier = 1.0f
    private var touchDragActive = false

    private var tapCount = 0
    private var lastTapTime = 0L

    init {
        // Observe expression from state machine and interpolate targets
        viewModelScope.launch {
            stateMachine.currentExpression.collectLatest { expression ->
                applyExpression(expression)
            }
        }

        // Observe assistant state for speaking mouth modulation
        viewModelScope.launch {
            stateMachine.currentState.collectLatest { state ->
                if (state == AssistantState.SPEAKING) {
                    startSpeakingAnimation()
                } else {
                    speakingAnimJob?.cancel()
                }
            }
        }

        // Observe settings
        viewModelScope.launch {
            settingsRepo.settingsFlow.collectLatest { settings ->
                currentPersonality = settings.personality
                jawMultiplier = settings.jawIllumination
            }
        }

        // Broham wake word heroic jaw flare
        conversationManager.onWakeWordBrohamDetected = {
            triggerHeroicJawFlare()
        }

        startIdleLoop()
    }

    private fun applyExpression(expr: GuiExpression) {
        val baseJaw = (expr.jawGlowAlpha * jawMultiplier).coerceIn(0.05f, 1.0f)
        val isLeftWink = expr.isWinkingLeft
        val isRightWink = expr.isWinkingRight

        val lUpper = if (isLeftWink) 1.0f else expr.upperLidCover
        val rUpper = if (isRightWink) 1.0f else expr.upperLidCover

        val lBrowH = if (expr.isAsymmetricBrow) expr.browHeight + 0.3f else expr.browHeight
        val rBrowH = if (expr.isAsymmetricBrow) expr.browHeight - 0.2f else expr.browHeight
        val lBrowT = if (expr.isAsymmetricBrow) expr.browTilt + 0.2f else expr.browTilt
        val rBrowT = if (expr.isAsymmetricBrow) expr.browTilt - 0.2f else expr.browTilt

        _visualState.value = _visualState.value.copy(
            eyeScaleX = expr.eyeScaleX,
            eyeScaleY = expr.eyeScaleY,
            leftUpperLid = lUpper,
            rightUpperLid = rUpper,
            lowerLid = expr.lowerLidTension,
            leftBrowHeight = lBrowH,
            rightBrowHeight = rBrowH,
            leftBrowTilt = lBrowT,
            rightBrowTilt = rBrowT,
            lookX = if (!touchDragActive) expr.lookX else _visualState.value.lookX,
            lookY = if (!touchDragActive) expr.lookY else _visualState.value.lookY,
            mouthHeight = expr.mouthHeight,
            mouthWidth = expr.mouthWidth,
            jawGlow = baseJaw,
            cheekGlow = expr.cheekGlowAlpha,
            templeGlow = expr.templeGlowAlpha,
            circuitPulse = expr.circuitPulseSpeed,
            isLoveEyes = expr.isLoveEyes,
            isWinkingLeft = isLeftWink,
            isWinkingRight = isRightWink
        )
    }

    fun triggerHeroicJawFlare() {
        viewModelScope.launch {
            _visualState.value = _visualState.value.copy(jawGlow = 1.0f)
            delay(1200)
            _visualState.value = _visualState.value.copy(
                jawGlow = (stateMachine.currentExpression.value.jawGlowAlpha * jawMultiplier).coerceIn(0.1f, 1f)
            )
        }
    }

    private fun startSpeakingAnimation() {
        speakingAnimJob?.cancel()
        speakingAnimJob = viewModelScope.launch {
            var toggle = false
            while (isActive && stateMachine.currentState.value == AssistantState.SPEAKING) {
                val mouthH = if (toggle) 0.65f else 0.25f
                val mouthW = if (toggle) 0.8f else 0.55f
                val jaw = if (toggle) 0.85f else 0.55f
                _visualState.value = _visualState.value.copy(
                    mouthHeight = mouthH,
                    mouthWidth = mouthW,
                    jawGlow = jaw
                )
                toggle = !toggle
                delay(130)
            }
        }
    }

    private fun startIdleLoop() {
        idleJob?.cancel()
        idleJob = viewModelScope.launch {
            while (isActive) {
                val interval = when (currentPersonality) {
                    GuiPersonality.CALM -> Random.nextLong(3500, 6500)
                    GuiPersonality.NORMAL -> Random.nextLong(2000, 4500)
                    GuiPersonality.ENERGETIC -> Random.nextLong(1200, 2800)
                }
                delay(interval)

                val state = stateMachine.currentState.value
                if (state == AssistantState.IDLE && !touchDragActive) {
                    performRandomIdleAction()
                }
            }
        }
    }

    private suspend fun performRandomIdleAction() {
        when (Random.nextInt(5)) {
            0 -> performBlink(isDouble = false)
            1 -> performBlink(isDouble = true)
            2 -> performGlance()
            3 -> performSubtleBrowAdjustment()
            4 -> performCircuitPulse()
        }
    }

    private suspend fun performBlink(isDouble: Boolean) {
        val currentLid = stateMachine.currentExpression.value.upperLidCover
        // Close eyes
        _visualState.value = _visualState.value.copy(leftUpperLid = 1.0f, rightUpperLid = 1.0f)
        delay(120)
        // Open
        _visualState.value = _visualState.value.copy(leftUpperLid = currentLid, rightUpperLid = currentLid)

        if (isDouble) {
            delay(100)
            _visualState.value = _visualState.value.copy(leftUpperLid = 1.0f, rightUpperLid = 1.0f)
            delay(110)
            _visualState.value = _visualState.value.copy(leftUpperLid = currentLid, rightUpperLid = currentLid)
        }
    }

    private suspend fun performGlance() {
        val glanceX = if (Random.nextBoolean()) Random.nextFloat() * 0.45f else -Random.nextFloat() * 0.45f
        val glanceY = if (Random.nextBoolean()) Random.nextFloat() * 0.25f else -Random.nextFloat() * 0.25f

        _visualState.value = _visualState.value.copy(lookX = glanceX, lookY = glanceY)
        delay(Random.nextLong(700, 1600))
        _visualState.value = _visualState.value.copy(lookX = 0.0f, lookY = 0.0f)
    }

    private suspend fun performSubtleBrowAdjustment() {
        val browShift = Random.nextFloat() * 0.25f
        _visualState.value = _visualState.value.copy(
            leftBrowHeight = _visualState.value.leftBrowHeight + browShift,
            rightBrowHeight = _visualState.value.rightBrowHeight + browShift
        )
        delay(800)
        applyExpression(stateMachine.currentExpression.value)
    }

    private suspend fun performCircuitPulse() {
        _visualState.value = _visualState.value.copy(circuitPulse = 2.5f)
        delay(1000)
        _visualState.value = _visualState.value.copy(circuitPulse = 1.0f)
    }

    // Touch interaction handlers with normalized hit-testing
    fun onFaceTapped(normalizedX: Float, normalizedY: Float) {
        // If speaking, tap interrupts speech!
        if (stateMachine.currentState.value == AssistantState.SPEAKING) {
            conversationManager.emergencyStop()
            return
        }

        // Detect rapid repeated taps
        val now = System.currentTimeMillis()
        if (now - lastTapTime < 400) {
            tapCount++
            if (tapCount >= 4) {
                stateMachine.setTemporaryExpression(GuiExpression.ANNOYED, durationMs = 2500)
                tapCount = 0
                return
            }
        } else {
            tapCount = 1
        }
        lastTapTime = now

        when {
            // Forehead
            normalizedY < 0.28f -> {
                stateMachine.setTemporaryExpression(GuiExpression.SURPRISED, durationMs = 1600)
            }
            // Left eye
            normalizedX < 0.42f && normalizedY in 0.28f..0.50f -> {
                viewModelScope.launch {
                    _visualState.value = _visualState.value.copy(isWinkingLeft = true, leftUpperLid = 1.0f)
                    delay(700)
                    _visualState.value = _visualState.value.copy(isWinkingLeft = false, leftUpperLid = 0.05f)
                    stateMachine.setTemporaryExpression(GuiExpression.CURIOUS, durationMs = 1400)
                }
            }
            // Right eye
            normalizedX > 0.58f && normalizedY in 0.28f..0.50f -> {
                viewModelScope.launch {
                    _visualState.value = _visualState.value.copy(isWinkingRight = true, rightUpperLid = 1.0f)
                    delay(700)
                    _visualState.value = _visualState.value.copy(isWinkingRight = false, rightUpperLid = 0.05f)
                }
            }
            // Nose bridge
            normalizedX in 0.42f..0.58f && normalizedY in 0.32f..0.55f -> {
                stateMachine.setTemporaryExpression(GuiExpression.FOCUSED, durationMs = 1800)
            }
            // Left cheek
            normalizedX < 0.35f && normalizedY in 0.50f..0.72f -> {
                viewModelScope.launch {
                    _visualState.value = _visualState.value.copy(lookX = -0.55f)
                    delay(1200)
                    _visualState.value = _visualState.value.copy(lookX = 0.0f)
                }
            }
            // Right cheek
            normalizedX > 0.65f && normalizedY in 0.50f..0.72f -> {
                viewModelScope.launch {
                    _visualState.value = _visualState.value.copy(lookX = 0.55f)
                    delay(1200)
                    _visualState.value = _visualState.value.copy(lookX = 0.0f)
                }
            }
            // Mouth
            normalizedX in 0.35f..0.65f && normalizedY in 0.60f..0.76f -> {
                stateMachine.setTemporaryExpression(GuiExpression.GOOFY, durationMs = 2000)
            }
            // Jaw / Chin
            normalizedY > 0.74f -> {
                stateMachine.setTemporaryExpression(GuiExpression.HEROIC, durationMs = 2200)
                triggerHeroicJawFlare()
            }
            else -> {
                stateMachine.setTemporaryExpression(GuiExpression.ATTENTIVE, durationMs = 1000)
            }
        }
    }

    fun onDoubleTap() {
        stateMachine.setTemporaryExpression(GuiExpression.EXCITED, durationMs = 2200)
        triggerHeroicJawFlare()
    }

    fun onLongPress() {
        stateMachine.setTemporaryExpression(GuiExpression.LOVE, durationMs = 2600)
    }

    fun onDrag(offsetDelta: Offset, sizeWidth: Float, sizeHeight: Float) {
        touchDragActive = true
        val normalizedDeltaX = (offsetDelta.x / (sizeWidth * 0.4f)).coerceIn(-1f, 1f)
        val normalizedDeltaY = (offsetDelta.y / (sizeHeight * 0.4f)).coerceIn(-1f, 1f)

        val newLookX = (_visualState.value.lookX + normalizedDeltaX).coerceIn(-0.65f, 0.65f)
        val newLookY = (_visualState.value.lookY + normalizedDeltaY).coerceIn(-0.55f, 0.55f)

        _visualState.value = _visualState.value.copy(lookX = newLookX, lookY = newLookY)
    }

    fun onDragRelease() {
        touchDragActive = false
        viewModelScope.launch {
            _visualState.value = _visualState.value.copy(lookX = 0.0f, lookY = 0.0f)
        }
    }
}
