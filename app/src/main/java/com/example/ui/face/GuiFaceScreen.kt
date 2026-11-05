package com.example.ui.face

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GuiSettings
import com.example.model.AssistantState
import kotlin.math.abs

@Composable
fun GuiFaceScreen(
    viewModel: FaceViewModel,
    settings: GuiSettings,
    isVisionActive: Boolean,
    onOpenQuickControls: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCommandCenter: () -> Unit,
    onOpenExpressionLab: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenOwnerConsole: () -> Unit,
    onFirstRunDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visualState by viewModel.visualState.collectAsState()
    val currentState by viewModel.stateMachine.currentState.collectAsState()
    val transcript by viewModel.stateMachine.transcript.collectAsState()
    val statusText by viewModel.stateMachine.statusText.collectAsState()
    val isMicActive by viewModel.stateMachine.isMicrophoneActive.collectAsState()
    val isFollowUpActive by viewModel.stateMachine.isFollowUpWindowActive.collectAsState()

    var dragStartOffset by remember { mutableStateOf<Offset?>(null) }
    var totalDragOffset by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF020406))
            .testTag("gui_face_screen")
            .pointerInput(settings.gesturesEnabled) {
                // Multi-finger and single-finger tap gestures
                detectTapGestures(
                    onTap = { offset ->
                        val normX = offset.x / size.width.toFloat()
                        val normY = offset.y / size.height.toFloat()
                        viewModel.onFaceTapped(normX, normY)
                    },
                    onDoubleTap = {
                        viewModel.onDoubleTap()
                    },
                    onLongPress = {
                        viewModel.onLongPress()
                    }
                )
            }
            .pointerInput(settings.gesturesEnabled) {
                // Drag gestures for navigation swipes and eye tracking
                detectDragGestures(
                    onDragStart = { offset ->
                        dragStartOffset = offset
                        totalDragOffset = Offset.Zero
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDragOffset += dragAmount
                        viewModel.onDrag(dragAmount, size.width.toFloat(), size.height.toFloat())
                    },
                    onDragEnd = {
                        val start = dragStartOffset
                        val delta = totalDragOffset
                        viewModel.onDragRelease()

                        if (settings.gesturesEnabled && start != null) {
                            val w = size.width.toFloat()
                            val h = size.height.toFloat()
                            val absX = abs(delta.x)
                            val absY = abs(delta.y)

                            // Swipe Down from near top
                            if (delta.y > 100f && absY > absX && start.y < h * 0.35f) {
                                if (delta.y > 350f) {
                                    onOpenSettings()
                                } else {
                                    onOpenQuickControls()
                                }
                            }
                            // Swipe Up from near bottom
                            else if (delta.y < -100f && absY > absX && start.y > h * 0.65f) {
                                onOpenCommandCenter()
                            }
                            // Swipe Left -> Expression Lab
                            else if (delta.x < -120f && absX > absY) {
                                onOpenExpressionLab()
                            }
                            // Swipe Right -> Automation History
                            else if (delta.x > 120f && absX > absY) {
                                onOpenHistory()
                            }
                        }
                        dragStartOffset = null
                        totalDragOffset = Offset.Zero
                    },
                    onDragCancel = {
                        viewModel.onDragRelease()
                        dragStartOffset = null
                        totalDragOffset = Offset.Zero
                    }
                )
            }
    ) {
        val customColor = try {
            Color(android.graphics.Color.parseColor(settings.customHex))
        } catch (_: Exception) {
            Color(0xFF00D9FF)
        }

        // GUI robotic face canvas
        FaceCanvas(
            visualState = visualState,
            colorPreset = settings.colorPreset,
            customColor = customColor,
            faceBrightness = settings.faceBrightness,
            glowIntensity = settings.glowIntensity,
            circuitIntensity = settings.circuitIntensity
        )

        // Top Status Bar Indicators (Microphone, Camera, Privacy, Follow-up)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isMicActive) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E676))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isFollowUpActive) "FOLLOW-UP LISTENING" else "LISTENING",
                        color = Color(0xFF00E676),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                } else if (currentState == AssistantState.PRIVACY) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Privacy Active",
                        tint = Color(0xFFFFAB00),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PRIVACY ACTIVE",
                        color = Color(0xFFFFAB00),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isVisionActive) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Camera Active",
                        tint = Color(0xFF00D9FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "VISION",
                        color = Color(0xFF00D9FF),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                // MOBI Exo Suit label
                Text(
                    text = "MOBI EXO SUIT",
                    color = Color(0xFF335566),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }

        // Bottom Temporary Auto-Fading Transcript & Status Area
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 36.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = transcript.isNotBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = Color(0xCC071015),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = transcript,
                        color = Color(0xFFE2F4FA),
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = statusText.isNotBlank() && transcript.isBlank() && currentState != AssistantState.IDLE,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = Color(0x99071015),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = statusText,
                        color = Color(0xFF8CF3FF),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Startup Boot Sequence on First Launch
        BootSequenceOverlay(
            isFirstRun = !settings.isFirstRunDone,
            onBootFinished = onFirstRunDone,
            setTemporaryExpression = { expr, duration ->
                viewModel.stateMachine.setTemporaryExpression(expr, duration)
            }
        )
    }
}
