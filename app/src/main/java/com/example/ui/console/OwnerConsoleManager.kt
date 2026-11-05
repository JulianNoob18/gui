package com.example.ui.console

import com.example.automation.shizuku.ShizukuManager
import com.example.automation.shizuku.ShizukuState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

data class ConsoleOutput(
    val command: String,
    val outputText: String,
    val isError: Boolean = false,
    val exitCode: Int = 0,
    val durationMs: Long = 0
)

class OwnerConsoleManager(
    private val scope: CoroutineScope,
    private val shizukuManager: ShizukuManager
) {
    private val _consoleHistory = MutableStateFlow<List<ConsoleOutput>>(emptyList())
    val consoleHistory: StateFlow<List<ConsoleOutput>> = _consoleHistory.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var activeJob: Job? = null
    private var activeProcess: Process? = null

    fun isHighRiskCommand(command: String): Boolean {
        val lower = command.lowercase()
        val highRiskPatterns = listOf(
            "rm ", "rmdir", "delete", "format", "reboot", "shutdown",
            "pm uninstall", "pm clear", "chmod", "chown", "mkfs", "dd ",
            "partition", "userdel", "setenforce"
        )
        return highRiskPatterns.any { lower.contains(it) }
    }

    fun executeCommand(command: String, onFinished: (ConsoleOutput) -> Unit) {
        if (_isRunning.value) return
        _isRunning.value = true

        activeJob = scope.launch(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            var exitCode = -1
            val outputBuilder = StringBuilder()
            var isErr = false

            try {
                val isShizuku = shizukuManager.state.value == ShizukuState.CONNECTED

                val process = if (isShizuku) {
                    try {
                        val method = Shizuku::class.java.getDeclaredMethod(
                            "newProcess",
                            Array<String>::class.java,
                            Array<String>::class.java,
                            String::class.java
                        )
                        method.isAccessible = true
                        method.invoke(null, arrayOf("sh", "-c", command), null, null) as Process
                    } catch (e: Exception) {
                        outputBuilder.append("Shizuku execution failed: ${e.message}\nFalling back to app shell...\n")
                        Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
                    }
                } else {
                    Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
                }
                activeProcess = process

                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val errReader = BufferedReader(InputStreamReader(process.errorStream))

                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    outputBuilder.append(line).append("\n")
                }
                while (errReader.readLine().also { line = it } != null) {
                    outputBuilder.append("[ERR] ").append(line).append("\n")
                    isErr = true
                }

                exitCode = process.waitFor()
            } catch (e: Exception) {
                isErr = true
                outputBuilder.append("Execution exception: ${e.message}\n")
            } finally {
                val duration = System.currentTimeMillis() - startTime
                val output = ConsoleOutput(
                    command = command,
                    outputText = outputBuilder.toString().ifBlank { "[Process finished with exit code $exitCode]" },
                    isError = isErr || exitCode != 0,
                    exitCode = exitCode,
                    durationMs = duration
                )
                withContext(Dispatchers.Main) {
                    _consoleHistory.value = listOf(output) + _consoleHistory.value
                    _isRunning.value = false
                    activeProcess = null
                    onFinished(output)
                }
            }
        }
    }

    fun stopRunningProcess() {
        activeJob?.cancel()
        try {
            activeProcess?.destroy()
        } catch (_: Exception) {}
        activeProcess = null
        _isRunning.value = false
    }

    fun clearHistory() {
        _consoleHistory.value = emptyList()
    }
}
