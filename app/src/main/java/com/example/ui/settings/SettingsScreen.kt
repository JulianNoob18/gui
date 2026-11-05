package com.example.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GuiPersonality
import com.example.data.GuiSettings
import com.example.data.SettingsRepository
import com.example.data.VoiceBackendSelection
import com.example.model.GuiColorPreset
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settings: GuiSettings,
    settingsRepo: SettingsRepository,
    ttsBackendStatus: String,
    onBack: () -> Unit,
    onOpenPermissionCenter: () -> Unit,
    onOpenOwnerConsole: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenExpressionLab: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "GUI SETTINGS",
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ================= SECTION 1: FACE =================
            SectionHeader(title = "FACE & ILLUMINATION")

            SettingCard {
                Text(
                    text = "Color Preset",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GuiColorPreset.entries.filter { it != GuiColorPreset.CUSTOM }.forEach { preset ->
                        val isSelected = settings.colorPreset == preset
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF142D38) else Color(0xFF071015)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable {
                                scope.launch { settingsRepo.updateColorPreset(preset) }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(preset.primaryColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = preset.label,
                                    color = if (isSelected) Color(0xFF8CF3FF) else Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Personality mode selector
                Text(
                    text = "Personality: ${settings.personality.name}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GuiPersonality.entries.forEach { p ->
                        FilterChip(
                            selected = settings.personality == p,
                            onClick = { scope.launch { settingsRepo.updatePersonality(p) } },
                            label = { Text(p.name, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00D9FF),
                                selectedLabelColor = Color(0xFF020406),
                                containerColor = Color(0xFF071015),
                                labelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                SettingSlider(
                    label = "Face Brightness",
                    value = settings.faceBrightness,
                    onValueChange = { scope.launch { settingsRepo.updateBrightness(it) } },
                    range = 0.2f..1.0f
                )

                SettingSlider(
                    label = "Glow Intensity",
                    value = settings.glowIntensity,
                    onValueChange = { scope.launch { settingsRepo.updateGlowIntensity(it) } },
                    range = 0.5f..1.8f
                )

                SettingSlider(
                    label = "Heroic Jaw Illumination",
                    value = settings.jawIllumination,
                    onValueChange = { scope.launch { settingsRepo.updateJawIllumination(it) } },
                    range = 0.5f..1.5f
                )

                SettingSlider(
                    label = "Circuit Detail Intensity",
                    value = settings.circuitIntensity,
                    onValueChange = { scope.launch { settingsRepo.updateCircuitIntensity(it) } },
                    range = 0.4f..1.6f
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ================= SECTION 2: VOICE =================
            SectionHeader(title = "VOICE & WAKE WORDS")

            SettingCard {
                SettingSwitch(
                    title = "Master Wake Words",
                    description = "Listen for wake words while active",
                    checked = settings.masterWakeWords,
                    onCheckedChange = { scope.launch { settingsRepo.updateWakeWords(master = it) } }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Individual wake word toggles
                Text(
                    text = "INDIVIDUAL WAKE WORDS",
                    color = Color(0xFF8BA5B0),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))

                SettingSwitch(
                    title = "GUI (Pronounced \"guy\")",
                    description = "Default wake word. Eyes focus forward and GUI listens.",
                    checked = settings.wakeGui,
                    onCheckedChange = { scope.launch { settingsRepo.updateWakeWords(gui = it) } }
                )
                SettingSwitch(
                    title = "DUDE",
                    description = "Casual wake word. Brow raises briefly and GUI listens.",
                    checked = settings.wakeDude,
                    onCheckedChange = { scope.launch { settingsRepo.updateWakeWords(dude = it) } }
                )
                SettingSwitch(
                    title = "BROHAM",
                    description = "Heroic wake word. Eyes move with confidence and jaw flares bright.",
                    checked = settings.wakeBroham,
                    onCheckedChange = { scope.launch { settingsRepo.updateWakeWords(broham = it) } }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SettingSwitch(
                    title = "Voice Output (TTS)",
                    description = "Spoken feedback and status updates",
                    checked = settings.voiceOutputEnabled,
                    onCheckedChange = { scope.launch { settingsRepo.updateVoiceOutput(it) } }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Voice Backend
                Text(
                    text = "Voice Backend: ${settings.voiceBackend.name}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = ttsBackendStatus,
                    color = Color(0xFF8BA5B0),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingSlider(
                    label = "Speech Rate",
                    value = settings.speechRate,
                    onValueChange = { scope.launch { settingsRepo.updateSpeechRate(it) } },
                    range = 0.6f..1.8f
                )

                SettingSlider(
                    label = "Speech Pitch",
                    value = settings.speechPitch,
                    onValueChange = { scope.launch { settingsRepo.updateSpeechPitch(it) } },
                    range = 0.6f..1.4f
                )

                SettingSwitch(
                    title = "Allow Interruption (Barge-in)",
                    description = "Interrupt speech instantly when you speak or tap",
                    checked = settings.allowInterruption,
                    onCheckedChange = { scope.launch { settingsRepo.updateAllowInterruption(it) } }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Follow up conversation window
                Text(
                    text = "Follow-up Window: ${if (settings.followUpWindowSeconds == 0) "Off" else "${settings.followUpWindowSeconds}s"}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0, 5, 10, 20).forEach { secs ->
                        FilterChip(
                            selected = settings.followUpWindowSeconds == secs,
                            onClick = { scope.launch { settingsRepo.updateFollowUpWindow(secs) } },
                            label = { Text(if (secs == 0) "Off" else "${secs}s", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00D9FF),
                                selectedLabelColor = Color(0xFF020406),
                                containerColor = Color(0xFF071015),
                                labelColor = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ================= SECTION 3: SENSORS & VISION =================
            SectionHeader(title = "SENSORS, SOUND & VISION")

            SettingCard {
                SettingSwitch(
                    title = "Vision Mode (Camera)",
                    description = "Front-camera brightness adaptation and motion tracking (Off by default)",
                    checked = settings.visionMode,
                    onCheckedChange = { scope.launch { settingsRepo.updateVisionMode(it) } }
                )

                SettingSwitch(
                    title = "Sound Reactions",
                    description = "Classify room audio intensity into visual reactions",
                    checked = settings.soundReactions,
                    onCheckedChange = { scope.launch { settingsRepo.updateSoundReactions(it) } }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ================= SECTION 4: GESTURES & AUTOMATION =================
            SectionHeader(title = "SHORTCUTS & SUBSYSTEMS")

            SettingCard {
                SettingSwitch(
                    title = "Enable Gestures",
                    description = "Navigation gestures on face canvas",
                    checked = settings.gesturesEnabled,
                    onCheckedChange = {}
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenPermissionCenter,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C1A20)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Permissions", color = Color(0xFF8CF3FF), fontSize = 12.sp)
                    }

                    Button(
                        onClick = onOpenHistory,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C1A20)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("History", color = Color(0xFF8CF3FF), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenExpressionLab,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C1A20)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Expression Lab", color = Color(0xFF8CF3FF), fontSize = 12.sp)
                    }

                    Button(
                        onClick = onOpenOwnerConsole,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF261014)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Owner Console", color = Color(0xFFFF8A80), fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = Color(0xFF00D9FF),
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun SettingCard(content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1A20)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = description, color = Color(0xFF8BA5B0), fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF00D9FF),
                checkedTrackColor = Color(0xFF004455)
            )
        )
    }
}

@Composable
private fun SettingSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = Color.White, fontSize = 12.sp)
            Text(
                text = "${(value * 100).toInt()}%",
                color = Color(0xFF8CF3FF),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF00D9FF),
                activeTrackColor = Color(0xFF00D9FF),
                inactiveTrackColor = Color(0xFF071015)
            )
        )
    }
}
