package com.example.automation

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.view.KeyEvent
import com.example.model.CommandResult

class MediaCommandManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    fun sendMediaKey(keyCode: Int): Boolean {
        return try {
            val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
            val upEvent = KeyEvent(KeyEvent.ACTION_UP, keyCode)
            audioManager?.dispatchMediaKeyEvent(downEvent)
            audioManager?.dispatchMediaKeyEvent(upEvent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun handleMediaCommand(action: String): CommandResult {
        val clean = action.trim().lowercase()
        return when {
            clean.contains("pause") -> {
                val sent = sendMediaKey(KeyEvent.KEYCODE_MEDIA_PAUSE)
                CommandResult(
                    isSuccess = sent,
                    spokenResponse = if (sent) "Paused." else "Could not pause media."
                )
            }
            clean.contains("play") || clean.contains("resume") -> {
                val sent = sendMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY)
                CommandResult(
                    isSuccess = sent,
                    spokenResponse = if (sent) "Playing." else "Could not start playback."
                )
            }
            clean.contains("next") || clean.contains("skip") -> {
                val sent = sendMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
                CommandResult(
                    isSuccess = sent,
                    spokenResponse = if (sent) "Next track." else "Could not skip track."
                )
            }
            clean.contains("prev") || clean.contains("previous") || clean.contains("back") -> {
                val sent = sendMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                CommandResult(
                    isSuccess = sent,
                    spokenResponse = if (sent) "Previous track." else "Could not go back."
                )
            }
            clean.contains("stop") -> {
                val sent = sendMediaKey(KeyEvent.KEYCODE_MEDIA_STOP)
                CommandResult(
                    isSuccess = sent,
                    spokenResponse = if (sent) "Stopped." else "Could not stop playback."
                )
            }
            else -> {
                CommandResult(
                    isSuccess = false,
                    spokenResponse = "Media command not recognized."
                )
            }
        }
    }
}
