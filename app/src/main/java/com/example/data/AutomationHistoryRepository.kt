package com.example.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class AutomationRecord(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val commandText: String,
    val actionType: String,
    val resultMessage: String,
    val isSuccess: Boolean
) {
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}

class AutomationHistoryRepository {
    private val _history = MutableStateFlow<List<AutomationRecord>>(emptyList())
    val history: StateFlow<List<AutomationRecord>> = _history.asStateFlow()

    fun recordAction(
        commandText: String,
        actionType: String,
        resultMessage: String,
        isSuccess: Boolean
    ) {
        val record = AutomationRecord(
            commandText = commandText,
            actionType = actionType,
            resultMessage = resultMessage,
            isSuccess = isSuccess
        )
        _history.value = listOf(record) + _history.value.take(49)
    }

    fun deleteRecord(id: String) {
        _history.value = _history.value.filterNot { it.id == id }
    }

    fun clearHistory() {
        _history.value = emptyList()
    }
}
