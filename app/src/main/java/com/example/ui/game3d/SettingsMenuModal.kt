package com.example.ui.game3d

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "System Settings",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Manage your solarpunk farm archives",
                    color = Color(0xFFB0BEC5),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                if (!showLoadConfirm && !showNewGameConfirm) {
                    // Standard Options
                    SettingsButton(
                        text = "Save Game",
                        color = SolarEmerald,
                        testTag = "btn_settings_save",
                        onClick = onSaveClick
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SettingsButton(
                        text = "Load Game",
                        color = CleanCyan,
                        testTag = "btn_settings_load",
                        onClick = { showLoadConfirm = true }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SettingsButton(
                        text = "New Game (Reset)",
                        color = Color(0xFFFF5252),
                        testTag = "btn_settings_new_game",
                        onClick = { showNewGameConfirm = true }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_settings_cancel")
                    ) {
                        Text("Cancel", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else if (showLoadConfirm) {
                    // Load Confirmation Screen
                    ConfirmationBlock(
                        title = "Confirm Load Game?",
                        desc = "This will reload from your last save. All unsaved progress will be lost.",
                        confirmText = "Reload Save",
                        confirmColor = CleanCyan,
                        testTagConfirm = "btn_confirm_load",
                        testTagCancel = "btn_cancel_load",
                        onConfirm = {
                            showLoadConfirm = false
                            onLoadClick()
                        },
                        onCancel = { showLoadConfirm = false }
                    )
                } else if (showNewGameConfirm) {
                    // New Game Confirmation Screen
                    ConfirmationBlock(
                        title = "Confirm Start Fresh?",
                        desc = "This will delete your save archive permanently and start a new game from Day 1.",
                        confirmText = "Delete & Reset",
                        confirmColor = Color(0xFFFF5252),
                        testTagConfirm = "btn_confirm_new_game",
                        testTagCancel = "btn_cancel_new_game",
                        onConfirm = {
                            showNewGameConfirm = false
                            onNewGameClick()
                        },
                        onCancel = { showNewGameConfirm = false }
                    )
                }
            }
        }
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
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.8f)),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag(testTag)
    ) {
        Text(
            text = text,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
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
            color = confirmColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = desc,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF263238)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .testTag(testTagCancel)
            ) {
                Text("Cancel", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = confirmColor),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .testTag(testTagConfirm)
            ) {
                Text(confirmText, color = Color(0xFF091215), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
