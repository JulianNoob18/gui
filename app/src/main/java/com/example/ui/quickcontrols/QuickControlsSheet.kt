package com.example.ui.quickcontrols

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GuiSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickControlsSheet(
    isOpen: Boolean,
    settings: GuiSettings,
    onDismiss: () -> Unit,
    onListenClick: () -> Unit,
    onStopClick: () -> Unit,
    onTogglePrivacy: (Boolean) -> Unit,
    onSleepClick: () -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleSoundReactions: (Boolean) -> Unit,
    onToggleVisionMode: (Boolean) -> Unit,
    onOpenFullSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFA071015),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .testTag("quick_controls_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QUICK CONTROLS",
                    color = Color(0xFF00D9FF),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp
                )
                Button(
                    onClick = {
                        onDismiss()
                        onOpenFullSettings()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C1A20)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("open_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color(0xFF8CF3FF)
                    )
                    Spacer(modifier = Modifier.padding(2.dp))
                    Text("Settings", color = Color(0xFF8CF3FF), fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        onDismiss()
                        onListenClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D9FF)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_listen_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Listen",
                        tint = Color(0xFF020406)
                    )
                    Spacer(modifier = Modifier.padding(2.dp))
                    Text("Listen", color = Color(0xFF020406), fontWeight = FontWeight.Bold)
                }

                FilledTonalButton(
                    onClick = {
                        onStopClick()
                        onDismiss()
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF261014)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_stop_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = Color(0xFFFF5252)
                    )
                    Spacer(modifier = Modifier.padding(2.dp))
                    Text("Stop", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Toggles Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1A20)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Privacy Mode toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = if (settings.privacyMode) Color(0xFFFFAB00) else Color(0xFF8BA5B0)
                            )
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(
                                text = "Privacy Mode",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                        Switch(
                            checked = settings.privacyMode,
                            onCheckedChange = onTogglePrivacy,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFFFAB00),
                                checkedTrackColor = Color(0xFF4A3500)
                            ),
                            modifier = Modifier.testTag("quick_privacy_toggle")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Vision Mode toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = if (settings.visionMode) Color(0xFF00D9FF) else Color(0xFF8BA5B0)
                            )
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(
                                text = "Vision Mode (Camera)",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                        Switch(
                            checked = settings.visionMode,
                            onCheckedChange = onToggleVisionMode,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00D9FF),
                                checkedTrackColor = Color(0xFF004455)
                            ),
                            modifier = Modifier.testTag("quick_vision_toggle")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sound reactions toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Sound Reactions", color = Color.White, fontSize = 14.sp)
                        Switch(
                            checked = settings.soundReactions,
                            onCheckedChange = onToggleSoundReactions,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00D9FF),
                                checkedTrackColor = Color(0xFF004455)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sliders (Brightness & Volume)
            Text(
                text = "Face Brightness: ${(settings.faceBrightness * 100).toInt()}%",
                color = Color(0xFFB0D0DC),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            Slider(
                value = settings.faceBrightness,
                onValueChange = onBrightnessChange,
                valueRange = 0.2f..1.0f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF00D9FF),
                    activeTrackColor = Color(0xFF00D9FF),
                    inactiveTrackColor = Color(0xFF14242C)
                ),
                modifier = Modifier.testTag("brightness_slider")
            )

            Text(
                text = "Voice Volume: ${(settings.voiceVolume * 100).toInt()}%",
                color = Color(0xFFB0D0DC),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            Slider(
                value = settings.voiceVolume,
                onValueChange = onVolumeChange,
                valueRange = 0.0f..1.0f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF00D9FF),
                    activeTrackColor = Color(0xFF00D9FF),
                    inactiveTrackColor = Color(0xFF14242C)
                ),
                modifier = Modifier.testTag("volume_slider")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sleep Mode button
            FilledTonalButton(
                onClick = {
                    onDismiss()
                    onSleepClick()
                },
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF0C1A20)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = Color(0xFF8CF3FF)
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text("Enter Sleep Mode", color = Color(0xFF8CF3FF))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
