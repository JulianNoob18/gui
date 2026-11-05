package com.example.model

/**
 * Authoritative assistant states managed by AssistantStateMachine.
 */
enum class AssistantState(
    val displayName: String,
    val defaultExpression: GuiExpression,
    val isMicActive: Boolean = false,
    val isSpeaking: Boolean = false,
    val isWorkInProgress: Boolean = false
) {
    IDLE("Idle", GuiExpression.NEUTRAL),
    ATTENTIVE("Attentive", GuiExpression.ATTENTIVE, isMicActive = true),
    LISTENING("Listening", GuiExpression.LISTENING, isMicActive = true),
    TRANSCRIBING("Transcribing", GuiExpression.TRANSCRIBING, isMicActive = true),
    UNDERSTANDING("Understanding", GuiExpression.FOCUSED),
    THINKING("Thinking", GuiExpression.THINKING),
    WORKING("Working", GuiExpression.WORKING, isWorkInProgress = true),
    SPEAKING("Speaking", GuiExpression.SPEAKING, isSpeaking = true),
    INTERRUPTED("Interrupted", GuiExpression.ATTENTIVE, isMicActive = true),
    SUCCESS("Success", GuiExpression.SUCCESS),
    CONFUSED("Confused", GuiExpression.CONFUSED),
    ERROR("Error", GuiExpression.ERROR),
    SLEEPING("Sleeping", GuiExpression.SLEEPING),
    PRIVACY("Privacy Mode", GuiExpression.PRIVACY);
}
