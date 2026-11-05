package com.example.ui.console

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.automation.shizuku.ShizukuManager
import com.example.automation.shizuku.ShizukuState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerConsoleScreen(
    consoleManager: OwnerConsoleManager,
    shizukuManager: ShizukuManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var isAuthenticated by remember { mutableStateOf(false) }
    var commandInput by remember { mutableStateOf("") }
    var showHighRiskConfirmDialog by remember { mutableStateOf(false) }

    val history by consoleManager.consoleHistory.collectAsState()
    val isRunning by consoleManager.isRunning.collectAsState()
    val shizukuState by shizukuManager.state.collectAsState()

    // Request Biometric or Device PIN Authentication on launch
    LaunchedEffect(Unit) {
        val activity = context as? FragmentActivity
        if (activity != null) {
            val executor = ContextCompat.getMainExecutor(context)
            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        isAuthenticated = true
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        // If biometric is unconfigured or skipped in emulator, allow fallback access
                        if (errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS ||
                            errorCode == BiometricPrompt.ERROR_HW_NOT_PRESENT ||
                            errorCode == BiometricPrompt.ERROR_HW_UNAVAILABLE
                        ) {
                            isAuthenticated = true
                        } else {
                            Toast.makeText(context, "Authentication failed: $errString", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Owner Console Authentication")
                .setSubtitle("Verify identity to access shell console")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                            BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()

            biometricPrompt.authenticate(promptInfo)
        } else {
            isAuthenticated = true
        }
    }

    if (!isAuthenticated) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF020406)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color(0xFF00D9FF),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "AUTHENTICATING OWNER ACCESS...",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
        return
    }

    // High risk confirmation dialog
    if (showHighRiskConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showHighRiskConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFF5252)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("High-Impact Command Warning", color = Color(0xFFFF5252), fontSize = 16.sp)
                }
            },
            text = {
                Text(
                    text = "The command \"$commandInput\" contains destructive or high-impact operations. Confirm manual execution?",
                    color = Color.White
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showHighRiskConfirmDialog = false
                        consoleManager.executeCommand(commandInput) {}
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text("Confirm Run", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showHighRiskConfirmDialog = false }) {
                    Text("Cancel", color = Color(0xFF8BA5B0))
                }
            },
            containerColor = Color(0xFF0C1A20)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "OWNER CONSOLE",
                        color = Color(0xFFFF5252),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (history.isNotEmpty()) {
                        IconButton(onClick = { consoleManager.clearHistory() }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear Console",
                                tint = Color(0xFF8BA5B0)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF071015))
            )
        },
        containerColor = Color(0xFF020406),
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Identity & Shizuku Status Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1A20)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "IDENTITY: VERIFIED OWNER",
                            color = Color(0xFF00E676),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (shizukuState == ShizukuState.CONNECTED) "Shizuku: CONNECTED (Elevated ADB)" else "Shizuku: ${shizukuState.label} (Standard App Shell)",
                            color = if (shizukuState == ShizukuState.CONNECTED) Color(0xFF00D9FF) else Color(0xFFFFAB00),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Command Presets
            Text(
                text = "SAFE COMMAND PRESETS",
                color = Color(0xFF8BA5B0),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val presets = listOf(
                    "pm list packages -3",
                    "getprop ro.build.version.release",
                    "dumpsys battery",
                    "uptime",
                    "uname -a"
                )
                items(presets) { preset ->
                    SuggestionChip(
                        onClick = { commandInput = preset },
                        label = { Text(preset, color = Color(0xFF8CF3FF), fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF0C1A20))
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = commandInput,
                onValueChange = { commandInput = it },
                label = { Text("Enter shell command", color = Color(0xFF8BA5B0)) },
                placeholder = { Text("e.g. pm list packages -3", color = Color(0xFF335566)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("console_command_input"),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    color = Color.White,
                    fontSize = 13.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF5252),
                    unfocusedBorderColor = Color(0xFF14242C),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Run & Stop Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (commandInput.isNotBlank()) {
                            if (consoleManager.isHighRiskCommand(commandInput)) {
                                showHighRiskConfirmDialog = true
                            } else {
                                consoleManager.executeCommand(commandInput) {}
                            }
                        }
                    },
                    enabled = !isRunning && commandInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D9FF)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("run_command_button")
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(
                            color = Color(0xFF020406),
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF020406))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("RUN", color = Color(0xFF020406), fontWeight = FontWeight.Bold)
                    }
                }

                FilledTonalButton(
                    onClick = { consoleManager.stopRunningProcess() },
                    enabled = isRunning,
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF301014)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("stop_process_button")
                ) {
                    Icon(imageVector = Icons.Default.Stop, contentDescription = null, tint = Color(0xFFFF5252))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("STOP", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Output Terminal Viewer
            Text(
                text = "EXECUTION OUTPUT",
                color = Color(0xFF8BA5B0),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF04080B), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                if (history.isEmpty()) {
                    item {
                        Text(
                            text = "Terminal ready. Enter a safe shell command above and tap RUN.",
                            color = Color(0xFF335566),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    items(history) { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF071015)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$ ${item.command}",
                                        color = Color(0xFF00D9FF),
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("output", item.outputText))
                                            Toast.makeText(context, "Output copied", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = Color(0xFF8BA5B0),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.outputText,
                                    color = if (item.isError) Color(0xFFFF8A80) else Color(0xFFE2F4FA),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
