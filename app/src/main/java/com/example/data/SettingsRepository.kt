package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.GuiColorPreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "gui_settings")

enum class GuiPersonality {
    CALM, NORMAL, ENERGETIC
}

enum class VoiceBackendSelection {
    AUTOMATIC, KOKORO, SYSTEM_TTS
}

data class GuiSettings(
    val colorPreset: GuiColorPreset = GuiColorPreset.GUI_CYAN,
    val customHex: String = "#00D9FF",
    val faceBrightness: Float = 1.0f,
    val glowIntensity: Float = 1.0f,
    val circuitIntensity: Float = 1.0f,
    val jawIllumination: Float = 1.0f,
    val animationIntensity: Float = 1.0f,
    val personality: GuiPersonality = GuiPersonality.NORMAL,
    val voiceOutputEnabled: Boolean = true,
    val voiceBackend: VoiceBackendSelection = VoiceBackendSelection.AUTOMATIC,
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val voiceVolume: Float = 1.0f,
    val briefResponses: Boolean = false,
    val speakConfirmations: Boolean = true,
    val allowInterruption: Boolean = true,
    val interruptByWakeWord: Boolean = true,
    val interruptByTap: Boolean = true,
    val followUpWindowSeconds: Int = 5, // 0 = off, 5, 10, 20
    val masterWakeWords: Boolean = true,
    val wakeGui: Boolean = true,
    val wakeDude: Boolean = true,
    val wakeBroham: Boolean = true,
    val requireWakeWord: Boolean = false,
    val soundReactions: Boolean = false,
    val soundSensitivity: Float = 0.5f,
    val soundReactionStrength: Float = 1.0f,
    val visionMode: Boolean = false,
    val visionMotionSensitivity: Float = 0.5f,
    val visionEyeFollowStrength: Float = 0.6f,
    val visionAutoWake: Boolean = false,
    val gesturesEnabled: Boolean = true,
    val keepScreenAwake: Boolean = true,
    val immersiveMode: Boolean = true,
    val privacyMode: Boolean = false,
    val isFirstRunDone: Boolean = false,
    val hapticFeedback: Boolean = true
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val COLOR_PRESET = stringPreferencesKey("color_preset")
        val CUSTOM_HEX = stringPreferencesKey("custom_hex")
        val FACE_BRIGHTNESS = floatPreferencesKey("face_brightness")
        val GLOW_INTENSITY = floatPreferencesKey("glow_intensity")
        val CIRCUIT_INTENSITY = floatPreferencesKey("circuit_intensity")
        val JAW_ILLUMINATION = floatPreferencesKey("jaw_illumination")
        val ANIMATION_INTENSITY = floatPreferencesKey("animation_intensity")
        val PERSONALITY = stringPreferencesKey("personality")
        val VOICE_OUTPUT_ENABLED = booleanPreferencesKey("voice_output_enabled")
        val VOICE_BACKEND = stringPreferencesKey("voice_backend")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val SPEECH_PITCH = floatPreferencesKey("speech_pitch")
        val VOICE_VOLUME = floatPreferencesKey("voice_volume")
        val BRIEF_RESPONSES = booleanPreferencesKey("brief_responses")
        val SPEAK_CONFIRMATIONS = booleanPreferencesKey("speak_confirmations")
        val ALLOW_INTERRUPTION = booleanPreferencesKey("allow_interruption")
        val INTERRUPT_BY_WAKE_WORD = booleanPreferencesKey("interrupt_by_wake_word")
        val INTERRUPT_BY_TAP = booleanPreferencesKey("interrupt_by_tap")
        val FOLLOW_UP_WINDOW = intPreferencesKey("follow_up_window")
        val MASTER_WAKE_WORDS = booleanPreferencesKey("master_wake_words")
        val WAKE_GUI = booleanPreferencesKey("wake_gui")
        val WAKE_DUDE = booleanPreferencesKey("wake_dude")
        val WAKE_BROHAM = booleanPreferencesKey("wake_broham")
        val REQUIRE_WAKE_WORD = booleanPreferencesKey("require_wake_word")
        val SOUND_REACTIONS = booleanPreferencesKey("sound_reactions")
        val SOUND_SENSITIVITY = floatPreferencesKey("sound_sensitivity")
        val SOUND_REACTION_STRENGTH = floatPreferencesKey("sound_reaction_strength")
        val VISION_MODE = booleanPreferencesKey("vision_mode")
        val VISION_SENSITIVITY = floatPreferencesKey("vision_sensitivity")
        val VISION_EYE_FOLLOW = floatPreferencesKey("vision_eye_follow")
        val VISION_AUTO_WAKE = booleanPreferencesKey("vision_auto_wake")
        val GESTURES_ENABLED = booleanPreferencesKey("gestures_enabled")
        val KEEP_SCREEN_AWAKE = booleanPreferencesKey("keep_screen_awake")
        val IMMERSIVE_MODE = booleanPreferencesKey("immersive_mode")
        val PRIVACY_MODE = booleanPreferencesKey("privacy_mode")
        val FIRST_RUN_DONE = booleanPreferencesKey("first_run_done")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
    }

    val settingsFlow: Flow<GuiSettings> = context.dataStore.data.map { prefs ->
        GuiSettings(
            colorPreset = GuiColorPreset.fromName(prefs[Keys.COLOR_PRESET] ?: GuiColorPreset.GUI_CYAN.name),
            customHex = prefs[Keys.CUSTOM_HEX] ?: "#00D9FF",
            faceBrightness = prefs[Keys.FACE_BRIGHTNESS] ?: 1.0f,
            glowIntensity = prefs[Keys.GLOW_INTENSITY] ?: 1.0f,
            circuitIntensity = prefs[Keys.CIRCUIT_INTENSITY] ?: 1.0f,
            jawIllumination = prefs[Keys.JAW_ILLUMINATION] ?: 1.0f,
            animationIntensity = prefs[Keys.ANIMATION_INTENSITY] ?: 1.0f,
            personality = try {
                GuiPersonality.valueOf(prefs[Keys.PERSONALITY] ?: GuiPersonality.NORMAL.name)
            } catch (e: Exception) {
                GuiPersonality.NORMAL
            },
            voiceOutputEnabled = prefs[Keys.VOICE_OUTPUT_ENABLED] ?: true,
            voiceBackend = try {
                VoiceBackendSelection.valueOf(prefs[Keys.VOICE_BACKEND] ?: VoiceBackendSelection.AUTOMATIC.name)
            } catch (e: Exception) {
                VoiceBackendSelection.AUTOMATIC
            },
            speechRate = prefs[Keys.SPEECH_RATE] ?: 1.0f,
            speechPitch = prefs[Keys.SPEECH_PITCH] ?: 1.0f,
            voiceVolume = prefs[Keys.VOICE_VOLUME] ?: 1.0f,
            briefResponses = prefs[Keys.BRIEF_RESPONSES] ?: false,
            speakConfirmations = prefs[Keys.SPEAK_CONFIRMATIONS] ?: true,
            allowInterruption = prefs[Keys.ALLOW_INTERRUPTION] ?: true,
            interruptByWakeWord = prefs[Keys.INTERRUPT_BY_WAKE_WORD] ?: true,
            interruptByTap = prefs[Keys.INTERRUPT_BY_TAP] ?: true,
            followUpWindowSeconds = prefs[Keys.FOLLOW_UP_WINDOW] ?: 5,
            masterWakeWords = prefs[Keys.MASTER_WAKE_WORDS] ?: true,
            wakeGui = prefs[Keys.WAKE_GUI] ?: true,
            wakeDude = prefs[Keys.WAKE_DUDE] ?: true,
            wakeBroham = prefs[Keys.WAKE_BROHAM] ?: true,
            requireWakeWord = prefs[Keys.REQUIRE_WAKE_WORD] ?: false,
            soundReactions = prefs[Keys.SOUND_REACTIONS] ?: false,
            soundSensitivity = prefs[Keys.SOUND_SENSITIVITY] ?: 0.5f,
            soundReactionStrength = prefs[Keys.SOUND_REACTION_STRENGTH] ?: 1.0f,
            visionMode = prefs[Keys.VISION_MODE] ?: false,
            visionMotionSensitivity = prefs[Keys.VISION_SENSITIVITY] ?: 0.5f,
            visionEyeFollowStrength = prefs[Keys.VISION_EYE_FOLLOW] ?: 0.6f,
            visionAutoWake = prefs[Keys.VISION_AUTO_WAKE] ?: false,
            gesturesEnabled = prefs[Keys.GESTURES_ENABLED] ?: true,
            keepScreenAwake = prefs[Keys.KEEP_SCREEN_AWAKE] ?: true,
            immersiveMode = prefs[Keys.IMMERSIVE_MODE] ?: true,
            privacyMode = prefs[Keys.PRIVACY_MODE] ?: false,
            isFirstRunDone = prefs[Keys.FIRST_RUN_DONE] ?: false,
            hapticFeedback = prefs[Keys.HAPTIC_FEEDBACK] ?: true
        )
    }

    suspend fun updateColorPreset(preset: GuiColorPreset, customHex: String = "#00D9FF") {
        context.dataStore.edit {
            it[Keys.COLOR_PRESET] = preset.name
            it[Keys.CUSTOM_HEX] = customHex
        }
    }

    suspend fun updatePersonality(personality: GuiPersonality) {
        context.dataStore.edit { it[Keys.PERSONALITY] = personality.name }
    }

    suspend fun updateBrightness(brightness: Float) {
        context.dataStore.edit { it[Keys.FACE_BRIGHTNESS] = brightness }
    }

    suspend fun updateGlowIntensity(glow: Float) {
        context.dataStore.edit { it[Keys.GLOW_INTENSITY] = glow }
    }

    suspend fun updateCircuitIntensity(circuit: Float) {
        context.dataStore.edit { it[Keys.CIRCUIT_INTENSITY] = circuit }
    }

    suspend fun updateJawIllumination(jaw: Float) {
        context.dataStore.edit { it[Keys.JAW_ILLUMINATION] = jaw }
    }

    suspend fun updateVoiceOutput(enabled: Boolean) {
        context.dataStore.edit { it[Keys.VOICE_OUTPUT_ENABLED] = enabled }
    }

    suspend fun updateSpeechRate(rate: Float) {
        context.dataStore.edit { it[Keys.SPEECH_RATE] = rate }
    }

    suspend fun updateSpeechPitch(pitch: Float) {
        context.dataStore.edit { it[Keys.SPEECH_PITCH] = pitch }
    }

    suspend fun updateVoiceVolume(volume: Float) {
        context.dataStore.edit { it[Keys.VOICE_VOLUME] = volume }
    }

    suspend fun updateBriefResponses(brief: Boolean) {
        context.dataStore.edit { it[Keys.BRIEF_RESPONSES] = brief }
    }

    suspend fun updateAllowInterruption(allow: Boolean) {
        context.dataStore.edit { it[Keys.ALLOW_INTERRUPTION] = allow }
    }

    suspend fun updateFollowUpWindow(seconds: Int) {
        context.dataStore.edit { it[Keys.FOLLOW_UP_WINDOW] = seconds }
    }

    suspend fun updateWakeWords(
        master: Boolean? = null,
        gui: Boolean? = null,
        dude: Boolean? = null,
        broham: Boolean? = null
    ) {
        context.dataStore.edit { prefs ->
            master?.let { prefs[Keys.MASTER_WAKE_WORDS] = it }
            gui?.let { prefs[Keys.WAKE_GUI] = it }
            dude?.let { prefs[Keys.WAKE_DUDE] = it }
            broham?.let { prefs[Keys.WAKE_BROHAM] = it }
        }
    }

    suspend fun updateSoundReactions(enabled: Boolean, sensitivity: Float? = null) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SOUND_REACTIONS] = enabled
            sensitivity?.let { prefs[Keys.SOUND_SENSITIVITY] = it }
        }
    }

    suspend fun updateVisionMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.VISION_MODE] = enabled }
    }

    suspend fun updatePrivacyMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PRIVACY_MODE] = enabled }
    }

    suspend fun setFirstRunDone() {
        context.dataStore.edit { it[Keys.FIRST_RUN_DONE] = true }
    }

    suspend fun resetGestureDefaults() {
        context.dataStore.edit {
            it[Keys.GESTURES_ENABLED] = true
            it[Keys.HAPTIC_FEEDBACK] = true
        }
    }
}
