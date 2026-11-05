package com.example.ui.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.example.automation.shizuku.ShizukuManager
import com.example.automation.shizuku.ShizukuState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionCenterScreen(
    shizukuManager: ShizukuManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val shizukuState by shizukuManager.state.collectAsState()

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasMicPermission = granted
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "PERMISSION CENTER",
                        color = Color(0xFF00D9FF),
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "GUI SYSTEM CAPABILITIES & ACCESS",
                color = Color(0xFF8BA5B0),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Microphone
            PermissionCard(
                title = "Microphone (Voice & Wake Words)",
                description = "Used to hear wake words ('GUI', 'Dude', 'Broham') and process voice commands. Audio is processed locally and never stored.",
                isGranted = hasMicPermission,
                onRequest = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                testTag = "perm_mic"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Camera (Vision Mode)
            PermissionCard(
                title = "Camera (Optional Vision Mode)",
                description = "Enables ambient brightness adaptation, motion detection, and subtle eye-following. No faces or individuals are identified, and no images are stored.",
                isGranted = hasCameraPermission,
                onRequest = { cameraLauncher.launch(Manifest.permission.CAMERA) },
                testTag = "perm_camera"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Media Access
            PermissionCard(
                title = "Media Session Control",
                description = "Allows GUI to play, pause, and skip music or podcasts across active media players.",
                isGranted = true,
                onRequest = {},
                isActionRequired = false,
                testTag = "perm_media"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Shizuku
            PermissionCard(
                title = "Shizuku Integration",
                description = "Optional elevated ADB capability. Allows device automation and package management without root. Current status: ${shizukuState.label}.",
                isGranted = shizukuState == ShizukuState.CONNECTED,
                onRequest = { shizukuManager.requestPermission() },
                buttonText = if (shizukuState == ShizukuState.PERMISSION_REQUIRED) "Authorize" else "Check Status",
                isActionRequired = shizukuState == ShizukuState.PERMISSION_REQUIRED,
                testTag = "perm_shizuku"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Biometric Auth
            PermissionCard(
                title = "Biometric & Device Credential",
                description = "Guards the protected Owner Console against unauthorized access.",
                isGranted = true,
                onRequest = {},
                isActionRequired = false,
                testTag = "perm_biometric"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit,
    buttonText: String = "Request",
    isActionRequired: Boolean = true,
    testTag: String = ""
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1A20)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().testTag(testTag)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = if (isGranted) Color(0xFF00E676) else Color(0xFFFFAB00),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = if (isGranted) "READY" else "OFFLINE",
                        color = if (isGranted) Color(0xFF00E676) else Color(0xFFFFAB00),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                color = Color(0xFF8BA5B0),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            if (!isGranted && isActionRequired) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D9FF)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = buttonText,
                        color = Color(0xFF020406),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
