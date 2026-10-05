package com.example.ui.game3d

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SunGold

@Composable
fun SettingsMenuModal(
    masterVolume: Float,
    musicVolume: Float,
    sfxVolume: Float,
    vibrationEnabled: Boolean,
    onMasterVolumeChange: (Float) -> Unit,
    onMusicVolumeChange: (Float) -> Unit,
    onSfxVolumeChange: (Float) -> Unit,
    onVibrationToggle: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    onLoadClick: () -> Unit,
    onNewGameClick: () -> Unit,
    onDismiss: () -> Unit
) {
    var showLoadConfirm by remember { mutableStateOf(false) }
    var showNewGameConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, SolarEmerald.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .testTag("settings_menu_modal"),
            color = Color(0xF80B1E1C),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "System & Audio Settings",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Audio volumes, haptics & game state",
                    color = Color(0xFFB0BEC5),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                if (!showLoadConfirm && !showNewGameConfirm) {
                    // 1. Audio Controls Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF132A2D))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Master Volume
                            VolumeRow(
                                title = "Master Volume",
                                value = masterVolume,
                                icon = Icons.Default.VolumeUp,
                                onValueChange = onMasterVolumeChange
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Music Volume
                            VolumeRow(
                                title = "Music Volume",
                                value = musicVolume,
                                icon = Icons.Default.GraphicEq,
                                onValueChange = onMusicVolumeChange
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // SFX Volume
                            VolumeRow(
                                title = "SFX Volume",
                                value = sfxVolume,
                                icon = Icons.Default.VolumeUp,
                                onValueChange = onSfxVolumeChange
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Vibration Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Vibration,
                                        contentDescription = null,
                                        tint = SunGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Haptic Vibration",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Switch(
                                    checked = vibrationEnabled,
                                    onCheckedChange = onVibrationToggle,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF091215),
                                        checkedTrackColor = SolarEmerald
                                    ),
                                    modifier = Modifier.testTag("toggle_vibration")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Game Save / Load Options
                    SettingsButton(
                        text = "Save Game",
                        color = SolarEmerald,
                        testTag = "btn_settings_save",
                        onClick = onSaveClick
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsButton(
                        text = "Load Last Save",
                        color = CleanCyan,
                        testTag = "btn_settings_load",
                        onClick = { showLoadConfirm = true }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsButton(
                        text = "New Game (Reset)",
                        color = Color(0xFFFF5252),
                        testTag = "btn_settings_new_game",
                        onClick = { showNewGameConfirm = true }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_settings_cancel")
                    ) {
                        Text("Done", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                } else if (showLoadConfirm) {
                    ConfirmationBlock(
                        title = "Confirm Load Game?",
                        desc = "This will reload your last save. All unsaved progress will be lost.",
                        confirmText = "Reload Save",
                        confirmColor = CleanCyan,
                        testTagConfirm = "btn_confirm_load",
                        testTagCancel = "btn_cancel_load",
                        onConfirm = {
                            showLoadConfirm = false
                            onLoadClick()
                            onDismiss()
                        },
                        onCancel = { showLoadConfirm = false }
                    )
                } else if (showNewGameConfirm) {
                    ConfirmationBlock(
                        title = "Confirm Reset World?",
                        desc = "Warning: Starting a fresh game will permanently erase existing farm progress.",
                        confirmText = "Reset & Restart",
                        confirmColor = Color(0xFFFF5252),
                        testTagConfirm = "btn_confirm_reset",
                        testTagCancel = "btn_cancel_reset",
                        onConfirm = {
                            showNewGameConfirm = false
                            onNewGameClick()
                            onDismiss()
                        },
                        onCancel = { showNewGameConfirm = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun VolumeRow(
    title: String,
    value: Float,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CleanCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "${(value * 100).toInt()}%",
                color = CleanCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = CleanCyan,
                activeTrackColor = CleanCyan,
                inactiveTrackColor = Color(0xFF1E3C40)
            ),
            modifier = Modifier
                .height(28.dp)
                .testTag("slider_${title.replace(" ", "_").lowercase()}")
        )
    }
}

@Composable
private fun SettingsButton(
    text: String,
    color: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(
            text = text,
            color = Color(0xFF091215),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun ConfirmationBlock(
    title: String,
    desc: String,
    confirmText: String,
    confirmColor: Color,
    testTagConfirm: String,
    testTagCancel: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = desc,
            color = Color(0xFFCFD8DC),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag(testTagConfirm),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = confirmColor)
        ) {
            Text(confirmText, color = Color(0xFF091215), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
            onClick = onCancel,
            modifier = Modifier.testTag(testTagCancel)
        ) {
            Text("Back", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
