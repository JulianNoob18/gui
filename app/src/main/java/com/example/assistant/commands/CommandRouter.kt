package com.example.assistant.commands

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.automation.ActionRegistry
import com.example.automation.AppLauncher
import com.example.automation.CastingManager
import com.example.automation.MediaCommandManager
import com.example.model.CommandResult
import com.example.model.CommandType
import com.example.model.ParsedCommand
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class CommandRouter(
    private val context: Context,
    private val appLauncher: AppLauncher,
    private val mediaCommandManager: MediaCommandManager,
    private val castingManager: CastingManager,
    private val actionRegistry: ActionRegistry
) {

    fun execute(command: ParsedCommand): CommandResult {
        return when (command.type) {
            CommandType.OPEN_APP -> {
                appLauncher.launchAppByName(command.target)
            }

            CommandType.WEB_SEARCH -> {
                executeWebSearch(command.query)
            }

            CommandType.MEDIA_CONTROL -> {
                mediaCommandManager.handleMediaCommand(command.action)
            }

            CommandType.CAST -> {
                castingManager.openCastingControls()
            }

            CommandType.SYSTEM_PANEL -> {
                val action = actionRegistry.findById(command.target)
                if (action != null) {
                    action.execute(context)
                } else {
                    CommandResult(
                        isSuccess = false,
                        spokenResponse = "System panel not found."
                    )
                }
            }

            CommandType.SLEEP -> {
                CommandResult(
                    isSuccess = true,
                    spokenResponse = "Entering sleep mode.",
                    detailMessage = "GUI entered sleep"
                )
            }

            CommandType.WAKE -> {
                CommandResult(
                    isSuccess = true,
                    spokenResponse = "GUI online and ready.",
                    detailMessage = "GUI awakened"
                )
            }

            CommandType.GUI_SETTING -> {
                CommandResult(
                    isSuccess = true,
                    spokenResponse = "Toggling privacy mode.",
                    detailMessage = "Setting changed: ${command.target}"
                )
            }

            CommandType.GUI_EXPRESSION -> {
                CommandResult(
                    isSuccess = true,
                    spokenResponse = "Setting expression to ${command.target.lowercase()}.",
                    detailMessage = "Expression set: ${command.target}"
                )
            }

            CommandType.OWNER_CONSOLE_REQUEST -> {
                CommandResult(
                    isSuccess = true,
                    spokenResponse = "Owner console requested. Authenticate to proceed.",
                    detailMessage = "Prompting owner console authentication"
                )
            }

            CommandType.OPEN_URL -> {
                CommandResult(
                    isSuccess = false,
                    spokenResponse = "URL opening not configured."
                )
            }

            CommandType.AUTOMATION -> {
                CommandResult(
                    isSuccess = true,
                    spokenResponse = "Running automation."
                )
            }

            CommandType.QUERY -> {
                CommandResult(
                    isSuccess = true,
                    spokenResponse = "Searching query: ${command.query}."
                )
            }

            CommandType.UNKNOWN -> {
                CommandResult(
                    isSuccess = false,
                    spokenResponse = "I didn't catch that command. You can ask me to open apps, search the web, control media, or cast.",
                    detailMessage = "Unrecognized command: ${command.rawText}"
                )
            }
        }
    }

    private fun executeWebSearch(query: String): CommandResult {
        if (query.isBlank()) {
            return CommandResult(
                isSuccess = false,
                spokenResponse = "What would you like me to search for?",
                detailMessage = "Empty search query"
            )
        }

        return try {
            val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
            val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Fallback web url intent
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$encodedQuery")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            try {
                context.startActivity(searchIntent)
            } catch (_: Exception) {
                context.startActivity(browserIntent)
            }

            CommandResult(
                isSuccess = true,
                spokenResponse = "Searching for $query.",
                detailMessage = "Opened search for: $query"
            )
        } catch (e: Exception) {
            CommandResult(
                isSuccess = false,
                spokenResponse = "I couldn't open web search.",
                detailMessage = e.message ?: "Failed to open search"
            )
        }
    }
}
