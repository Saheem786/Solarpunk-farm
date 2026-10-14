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
import com.example.ui.theme.MedievalCrimson
import com.example.ui.theme.Parchment
import com.example.ui.theme.AgedGold
import com.example.ui.theme.MedievalSharpFontFamily
import com.example.ui.theme.AlmendraFontFamily
import com.example.ui.theme.MedievalTextPrimary
import com.example.ui.theme.SunGold
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald

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
                        Color(0xFF1A0F0A), // Deep Wood
                        Color(0xFF2D1B0D),
                        Color(0xFF422818),
                        Color(0xFF1A0F0A)
                    )
                )
            )
            .testTag("main_menu_overlay"),
        contentAlignment = Alignment.Center
    ) {
        // Medieval Sigil Glow
        Box(
            modifier = Modifier
                .size(400.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AgedGold.copy(alpha = 0.2f),
                            MedievalCrimson.copy(alpha = 0.1f),
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
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xF2F5E6D3),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AgedGold)
                ) {
                    Text(
                        text = "🏰 THE KING'S DECREE: RECLAIM THE ISLE",
                        color = MedievalTextPrimary,
                        fontFamily = MedievalSharpFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "MEDIEVAL\nISLE",
                    color = Parchment,
                    fontFamily = MedievalSharpFontFamily,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 44.sp,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Sow. Reap. Conquer.",
                    color = AgedGold,
                    fontFamily = MedievalSharpFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Restore a forgotten island kingdom. Tend the royal fields, harness the elements, recruit loyal subjects, and defend the crown across three distinct realms.",
                    color = Parchment.copy(alpha = 0.8f),
                    fontFamily = AlmendraFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                )
            }

            // Right Side: Action Buttons Card
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, AgedGold, RoundedCornerShape(12.dp)),
                color = Color(0xF22D1B0D), // Dark Wood
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "SELECT MODE",
                        color = AgedGold,
                        fontFamily = MedievalSharpFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    if (hasSaveGame) {
                        MainMenuButton(
                            text = "CONTINUE QUEST",
                            icon = Icons.Default.PlayArrow,
                            color = MedievalCrimson,
                            testTag = "btn_main_continue",
                            onClick = onContinueGame
                        )
                    }

                    MainMenuButton(
                        text = "NEW JOURNEY",
                        icon = Icons.Default.AddCircle,
                        color = Color(0xFF2E4D23), // Forest Moss
                        testTag = "btn_main_new_game",
                        onClick = onNewGame
                    )

                    MainMenuButton(
                        text = "CHRONICLES (3)",
                        icon = Icons.Default.Save,
                        color = Color(0xFF5D4037),
                        testTag = "btn_main_save_slots",
                        onClick = onOpenSaveSlots
                    )

                    MainMenuButton(
                        text = "TROPHIES",
                        icon = Icons.Default.EmojiEvents,
                        color = AgedGold,
                        testTag = "btn_main_achievements",
                        onClick = onOpenAchievements
                    )

                    MainMenuButton(
                        text = "OPTIONS",
                        icon = Icons.Default.Settings,
                        color = Color(0xFF424242), // Iron Grey
                        testTag = "btn_main_settings",
                        onClick = onOpenSettings
                    )

                    TextButton(
                        onClick = { showCreditsDialog = true },
                        modifier = Modifier.testTag("btn_main_credits")
                    ) {
                        Text("Credits & Lore", color = Parchment.copy(alpha = 0.6f), fontFamily = AlmendraFontFamily, fontSize = 13.sp)
                    }
                }
            }
        }

        // Bottom Right Version Badge
        Text(
            text = "v1.1.0 - Medieval Edition",
            color = AgedGold.copy(alpha = 0.5f),
            fontFamily = MedievalSharpFontFamily,
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
                tint = Parchment,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = Parchment,
                fontFamily = MedievalSharpFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}
