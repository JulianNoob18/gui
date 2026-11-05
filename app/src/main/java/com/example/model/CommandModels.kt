package com.example.model

enum class CommandType {
    OPEN_APP,
    WEB_SEARCH,
    OPEN_URL,
    MEDIA_CONTROL,
    CAST,
    SYSTEM_PANEL,
    GUI_SETTING,
    GUI_EXPRESSION,
    SLEEP,
    WAKE,
    AUTOMATION,
    OWNER_CONSOLE_REQUEST,
    QUERY,
    UNKNOWN
}

data class ParsedCommand(
    val type: CommandType,
    val rawText: String,
    val target: String = "",
    val action: String = "",
    val query: String = "",
    val wakeWordUsed: String? = null
)

data class CommandResult(
    val isSuccess: Boolean,
    val spokenResponse: String,
    val detailMessage: String = "",
    val executedAt: Long = System.currentTimeMillis()
)
