package com.example.ui.commandcenter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AutomationRecord

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CommandCenterSheet(
    isOpen: Boolean,
    recentCommands: List<AutomationRecord>,
    onDismiss: () -> Unit,
    onListenClick: () -> Unit,
    onOpenAppClick: (String) -> Unit,
    onWebSearchClick: (String) -> Unit,
    onMediaPlayPauseClick: () -> Unit,
    onCastingClick: () -> Unit,
    onAutomationsClick: () -> Unit,
    onExpressionLabClick: () -> Unit,
    onOwnerConsoleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var customQuery by remember { mutableStateOf("") }

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
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("command_center_sheet")
        ) {
            Text(
                text = "COMMAND CENTER",
                color = Color(0xFF00D9FF),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Primary Command Tiles (Grid)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                maxItemsInEachRow = 2
            ) {
                CommandTile(
                    title = "Push to Talk",
                    subtitle = "Listen for command",
                    icon = Icons.Default.Mic,
                    accentColor = Color(0xFF00D9FF),
                    modifier = Modifier.weight(1f),
                    testTag = "cmd_listen"
                ) {
                    onDismiss()
                    onListenClick()
                }

                CommandTile(
                    title = "Media Control",
                    subtitle = "Play / Pause / Next",
                    icon = Icons.Default.PlayArrow,
                    accentColor = Color(0xFF00E676),
                    modifier = Modifier.weight(1f),
                    testTag = "cmd_media"
                ) {
                    onMediaPlayPauseClick()
                }

                CommandTile(
                    title = "Screen Cast",
                    subtitle = "Display / Cast Panel",
                    icon = Icons.Default.Cast,
                    accentColor = Color(0xFF2979FF),
                    modifier = Modifier.weight(1f),
                    testTag = "cmd_cast"
                ) {
                    onCastingClick()
                }

                CommandTile(
                    title = "Automations",
                    subtitle = "System settings panels",
                    icon = Icons.Default.SettingsApplications,
                    accentColor = Color(0xFFFFAB00),
                    modifier = Modifier.weight(1f),
                    testTag = "cmd_automations"
                ) {
                    onAutomationsClick()
                }

                CommandTile(
                    title = "Expression Lab",
                    subtitle = "Preview 23 face states",
                    icon = Icons.Default.Face,
                    accentColor = Color(0xFFD500F9),
                    modifier = Modifier.weight(1f),
                    testTag = "cmd_expression_lab"
                ) {
                    onDismiss()
                    onExpressionLabClick()
                }

                CommandTile(
                    title = "Owner Console",
                    subtitle = "Protected shell access",
                    icon = Icons.Default.Terminal,
                    accentColor = Color(0xFFFF5252),
                    modifier = Modifier.weight(1f),
                    testTag = "cmd_owner_console"
                ) {
                    onDismiss()
                    onOwnerConsoleClick()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Manual Text Input / Quick Search
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customQuery,
                    onValueChange = { customQuery = it },
                    placeholder = { Text("Search web or launch app...", color = Color(0xFF5A7280), fontSize = 13.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00D9FF),
                        unfocusedBorderColor = Color(0xFF14242C),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("command_input_field")
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (customQuery.isNotBlank()) {
                            onDismiss()
                            onWebSearchClick(customQuery)
                            customQuery = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D9FF)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("command_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF020406)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recent Safe Commands Header
            Text(
                text = "RECENT SAFE COMMANDS",
                color = Color(0xFF8BA5B0),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (recentCommands.isEmpty()) {
                Text(
                    text = "No recent commands yet. Try saying: \"GUI, open Spotify\" or \"Dude, search for 3D printed phone stands\".",
                    color = Color(0xFF557788),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    items(recentCommands.take(5)) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "• ${item.commandText}",
                                color = Color.White,
                                fontSize = 13.sp,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (item.isSuccess) "DONE" else "FAILED",
                                color = if (item.isSuccess) Color(0xFF00E676) else Color(0xFFFF5252),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CommandTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    testTag: String = "",
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1A20)),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF8BA5B0),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
