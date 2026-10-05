package com.example.ui.game3d

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
fun MainMenuOverlay(
    hasSaveGame: Boolean,
    onContinueGame: () -> Unit,
    onNewGame: () -> Unit,
    onOpenSaveSlots: () -> Unit,
    onOpenAchievements: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCredits: () -> Unit
) {
    var showCreditsDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF07181A),
                        Color(0xFF0F322B),
                        Color(0xFF1D523A),
                        Color(0xFF0A1B1A)
                    )
                )
            )
            .testTag("main_menu_overlay"),
        contentAlignment = Alignment.Center
    ) {
        // Solarpunk Sunrise Glow Circle
        Box(
            modifier = Modifier
                .size(360.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            SunGold.copy(alpha = 0.25f),
                            SolarEmerald.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Branding & Title
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .padding(end = 24.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x3300E5FF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CleanCyan)
                ) {
                    Text(
                        text = "🌱 REGEN AGRICULTURE & POWER SIMULATOR",
                        color = CleanCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "ECO FARM\nSIMULATOR",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 36.sp,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Build. Grow. Restore.",
                    color = SunGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Rebuild a fallen eco-city, harvest crops, manage solar & hydro power grids, recruit survivors, and restore nature across 3 biomes.",
                    color = Color(0xFFB0BEC5),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            // Right Side: Action Buttons Card
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(2.dp, SolarEmerald.copy(alpha = 0.8f), RoundedCornerShape(24.dp)),
                color = Color(0xF209181A),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "SELECT MODE",
                        color = SunGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    if (hasSaveGame) {
                        MainMenuButton(
                            text = "CONTINUE FARM",
                            icon = Icons.Default.PlayArrow,
                            color = SolarEmerald,
                            testTag = "btn_main_continue",
                            onClick = onContinueGame
                        )
                    }

                    MainMenuButton(
                        text = "NEW GAME",
                        icon = Icons.Default.AddCircle,
                        color = CleanCyan,
                        testTag = "btn_main_new_game",
                        onClick = onNewGame
                    )

                    MainMenuButton(
                        text = "SAVE SLOTS (3)",
                        icon = Icons.Default.Save,
                        color = Color(0xFF00E676),
                        testTag = "btn_main_save_slots",
                        onClick = onOpenSaveSlots
                    )

                    MainMenuButton(
                        text = "ACHIEVEMENTS",
                        icon = Icons.Default.EmojiEvents,
                        color = SunGold,
                        testTag = "btn_main_achievements",
                        onClick = onOpenAchievements
                    )

                    MainMenuButton(
                        text = "SETTINGS",
                        icon = Icons.Default.Settings,
                        color = Color(0xFF90A4AE),
                        testTag = "btn_main_settings",
                        onClick = onOpenSettings
                    )

                    TextButton(
                        onClick = { showCreditsDialog = true },
                        modifier = Modifier.testTag("btn_main_credits")
                    ) {
                        Text("Credits & Lore", color = Color(0xFFB0BEC5), fontSize = 12.sp)
                    }
                }
            }
        }

        // Bottom Right Version Badge
        Text(
            text = "v1.0.0 (Build 1) - Play Store Release",
            color = Color(0x88FFFFFF),
            fontSize = 10.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }

    if (showCreditsDialog) {
        Dialog(onDismissRequest = { showCreditsDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, SunGold, RoundedCornerShape(20.dp)),
                color = Color(0xFD09181A)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = "Credits",
                        tint = SunGold,
                        modifier = Modifier.size(36.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "ECO FARM SIMULATOR",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "A Solarpunk Vision of Sustainable Development",
                        color = CleanCyan,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Created as an eco-conscious simulation celebrating closed-loop agriculture, clean power grids, and community restoration.",
                        color = Color(0xFFB0BEC5),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showCreditsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = SunGold)
                    ) {
                        Text("Close", color = Color(0xFF091215), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MainMenuButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF091215),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = Color(0xFF091215),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}
