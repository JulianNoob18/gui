package com.example.assistant.commands

import com.example.model.CommandType
import com.example.model.ParsedCommand
import java.util.Locale

class CommandParser {

    fun parse(rawText: String, wakeWord: String? = null): ParsedCommand {
        val trimmed = rawText.trim()
        val lower = trimmed.lowercase(Locale.ROOT)

        // Web search checks
        if (lower.startsWith("search for ") || lower.startsWith("search the web for ") ||
            lower.startsWith("search web for ") || lower.startsWith("look up ") ||
            lower.startsWith("search ") || lower.startsWith("google ")
        ) {
            val query = when {
                lower.startsWith("search the web for ") -> trimmed.substring(20)
                lower.startsWith("search web for ") -> trimmed.substring(15)
                lower.startsWith("search for ") -> trimmed.substring(11)
                lower.startsWith("look up ") -> trimmed.substring(8)
                lower.startsWith("search ") -> trimmed.substring(7)
                lower.startsWith("google ") -> trimmed.substring(7)
                else -> trimmed
            }.trim()

            return ParsedCommand(
                type = CommandType.WEB_SEARCH,
                rawText = trimmed,
                query = query,
                wakeWordUsed = wakeWord
            )
        }

        // App launching
        if (lower.startsWith("open ") || lower.startsWith("launch ") || lower.startsWith("start ")) {
            val target = when {
                lower.startsWith("open ") -> trimmed.substring(5)
                lower.startsWith("launch ") -> trimmed.substring(7)
                lower.startsWith("start ") -> trimmed.substring(6)
                else -> trimmed
            }.trim()

            val targetLower = target.lowercase(Locale.ROOT)
            if (targetLower == "casting" || targetLower == "screen cast" || targetLower == "cast") {
                return ParsedCommand(
                    type = CommandType.CAST,
                    rawText = trimmed,
                    action = "open_cast",
                    wakeWordUsed = wakeWord
                )
            }
            if (targetLower == "console" || targetLower == "owner console") {
                return ParsedCommand(
                    type = CommandType.OWNER_CONSOLE_REQUEST,
                    rawText = trimmed,
                    wakeWordUsed = wakeWord
                )
            }

            return ParsedCommand(
                type = CommandType.OPEN_APP,
                rawText = trimmed,
                target = target,
                wakeWordUsed = wakeWord
            )
        }

        // Casting commands
        if (lower.contains("cast to screen") || lower.contains("open casting") ||
            lower.contains("screen cast") || lower == "cast"
        ) {
            return ParsedCommand(
                type = CommandType.CAST,
                rawText = trimmed,
                action = "open_cast",
                wakeWordUsed = wakeWord
            )
        }

        // Media controls
        if (lower == "play" || lower == "pause" || lower == "resume" || lower == "stop" ||
            lower.contains("next song") || lower.contains("previous song") ||
            lower.contains("next track") || lower.contains("previous track") ||
            lower == "skip"
        ) {
            return ParsedCommand(
                type = CommandType.MEDIA_CONTROL,
                rawText = trimmed,
                action = lower,
                wakeWordUsed = wakeWord
            )
        }

        // System Settings
        if (lower.contains("wifi") || lower.contains("wi-fi")) {
            return ParsedCommand(type = CommandType.SYSTEM_PANEL, rawText = trimmed, target = "sys_wifi", wakeWordUsed = wakeWord)
        }
        if (lower.contains("bluetooth")) {
            return ParsedCommand(type = CommandType.SYSTEM_PANEL, rawText = trimmed, target = "sys_bluetooth", wakeWordUsed = wakeWord)
        }
        if (lower.contains("display") || lower.contains("brightness")) {
            return ParsedCommand(type = CommandType.SYSTEM_PANEL, rawText = trimmed, target = "sys_display", wakeWordUsed = wakeWord)
        }
        if (lower.contains("sound") || lower.contains("volume")) {
            return ParsedCommand(type = CommandType.SYSTEM_PANEL, rawText = trimmed, target = "sys_sound", wakeWordUsed = wakeWord)
        }
        if (lower.contains("battery") || lower.contains("power")) {
            return ParsedCommand(type = CommandType.SYSTEM_PANEL, rawText = trimmed, target = "sys_battery", wakeWordUsed = wakeWord)
        }

        // Sleep / Wake
        if (lower == "sleep" || lower == "go to sleep") {
            return ParsedCommand(type = CommandType.SLEEP, rawText = trimmed, wakeWordUsed = wakeWord)
        }
        if (lower == "wake up" || lower == "wake") {
            return ParsedCommand(type = CommandType.WAKE, rawText = trimmed, wakeWordUsed = wakeWord)
        }

        // Privacy mode
        if (lower.contains("privacy mode") || lower.contains("privacy")) {
            return ParsedCommand(type = CommandType.GUI_SETTING, rawText = trimmed, target = "privacy", wakeWordUsed = wakeWord)
        }

        // Facial expressions
        if (lower.contains("heroic") || lower.contains("be heroic")) {
            return ParsedCommand(type = CommandType.GUI_EXPRESSION, rawText = trimmed, target = "HEROIC", wakeWordUsed = wakeWord)
        }
        if (lower.contains("smile") || lower.contains("happy")) {
            return ParsedCommand(type = CommandType.GUI_EXPRESSION, rawText = trimmed, target = "IDLE_HAPPY", wakeWordUsed = wakeWord)
        }
        if (lower.contains("goofy")) {
            return ParsedCommand(type = CommandType.GUI_EXPRESSION, rawText = trimmed, target = "GOOFY", wakeWordUsed = wakeWord)
        }
        if (lower.contains("cool")) {
            return ParsedCommand(type = CommandType.GUI_EXPRESSION, rawText = trimmed, target = "COOL", wakeWordUsed = wakeWord)
        }
        if (lower.contains("love")) {
            return ParsedCommand(type = CommandType.GUI_EXPRESSION, rawText = trimmed, target = "LOVE", wakeWordUsed = wakeWord)
        }

        return ParsedCommand(
            type = CommandType.UNKNOWN,
            rawText = trimmed,
            wakeWordUsed = wakeWord
        )
    }
}
