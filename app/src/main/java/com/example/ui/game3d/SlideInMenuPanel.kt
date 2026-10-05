package com.example.ui.game3d

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SunGold

@Composable
fun SlideInMenuPanel(
    visible: Boolean,
    isSprinting: Boolean,
    isNight: Boolean,
    onEat: () -> Unit,
    onDrink: () -> Unit,
    onToggleWalkSprint: () -> Unit,
    onSleep: () -> Unit,
    onTimeSkip: () -> Unit,
    onOpenSanctuary: () -> Unit = {},
    onOpenResearch: () -> Unit = {},
    onOpenJournal: () -> Unit = {},
    onOpenEnergyGrid: () -> Unit = {},
    onOpenMap: () -> Unit = {},
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
        exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it }),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .widthIn(min = 260.dp, max = 320.dp)
                .fillMaxHeight()
                .padding(end = 8.dp, top = 40.dp, bottom = 60.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
                .testTag("slide_in_menu_panel"),
            color = Color(0xF50B1B1E),
            shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, SolarEmerald.copy(alpha = 0.7f)),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header: Title + Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "FARM ACTIONS",
                            color = SunGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Survival & Simulation Controls",
                            color = Color(0xFF90A4AE),
                            fontSize = 9.5.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                            .testTag("btn_close_menu_panel")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Menu",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Menu Items List
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Eat Food
                    MenuItemRow(
                        icon = Icons.Default.Restaurant,
                        iconTint = Color(0xFFFF9800),
                        title = "Eat Food",
                        subtitle = "Consume meal from pantry (+Hunger)",
                        testTag = "btn_menu_eat",
                        onClick = onEat
                    )

                    // 2. Drink Water
                    MenuItemRow(
                        icon = Icons.Default.WaterDrop,
                        iconTint = Color(0xFF00E5FF),
                        title = "Drink Water",
                        subtitle = "Quench thirst with fresh water",
                        testTag = "btn_menu_drink",
                        onClick = onDrink
                    )

                    // 3. Walk / Sprint Toggle
                    MenuItemRow(
                        icon = Icons.Default.DirectionsRun,
                        iconTint = if (isSprinting) SunGold else Color.White,
                        title = if (isSprinting) "Fast Sprint (Active)" else "Walk Mode",
                        subtitle = if (isSprinting) "Consumes stamina faster" else "Normal pace",
                        testTag = "btn_menu_sprint",
                        isActive = isSprinting,
                        onClick = onToggleWalkSprint
                    )

                    // 4. Sleep / Rest
                    MenuItemRow(
                        icon = Icons.Default.Bed,
                        iconTint = CleanCyan,
                        title = if (isNight) "Sleep until Morning" else "Rest in Farmhouse",
                        subtitle = "Restore health & max stamina",
                        testTag = "btn_menu_rest",
                        onClick = onSleep
                    )

                    // 5. +2h Time Skip
                    MenuItemRow(
                        icon = Icons.Default.FastForward,
                        iconTint = SolarEmerald,
                        title = "+2 Hours",
                        subtitle = "Advance daylight & solar time",
                        testTag = "btn_menu_fast_forward",
                        onClick = onTimeSkip
                    )

                    // 6. Sanctuary Settlement
                    MenuItemRow(
                        icon = Icons.Default.People,
                        iconTint = SunGold,
                        title = "Sanctuary",
                        subtitle = "NPC Survivors, Jobs, Beds & Morale",
                        testTag = "btn_menu_sanctuary",
                        onClick = onOpenSanctuary
                    )

                    // 6B. Research Tree
                    MenuItemRow(
                        icon = Icons.Default.Science,
                        iconTint = CleanCyan,
                        title = "Research Tree",
                        subtitle = "Unlock clean tech & eco infrastructure",
                        testTag = "btn_menu_research",
                        onClick = onOpenResearch
                    )

                    // 6C. Journal & Story Missions
                    MenuItemRow(
                        icon = Icons.Default.Book,
                        iconTint = SunGold,
                        title = "Journal & Missions",
                        subtitle = "12 Story Missions, 15 POIs, SDZ Lore",
                        testTag = "btn_menu_journal",
                        onClick = onOpenJournal
                    )

                    // 7. Energy Grid
                    MenuItemRow(
                        icon = Icons.Default.Bolt,
                        iconTint = CleanCyan,
                        title = "Energy Grid",
                        subtitle = "Batteries, generators & priorities",
                        testTag = "btn_menu_energy_grid",
                        onClick = onOpenEnergyGrid
                    )

                    // 7. World Map
                    MenuItemRow(
                        icon = Icons.Default.Explore,
                        iconTint = CleanCyan,
                        title = "World Map",
                        subtitle = "3 Biomes, Fog of War, POIs",
                        testTag = "btn_menu_world_map",
                        onClick = onOpenMap
                    )

                    // 7. Settings
                    MenuItemRow(
                        icon = Icons.Default.Settings,
                        iconTint = SunGold,
                        title = "Audio & Settings",
                        subtitle = "Volume, sound effects, vibration",
                        testTag = "btn_settings_gear",
                        onClick = onOpenSettings
                    )
                }

                // Close Button Footer
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onDismiss() }
                        .testTag("btn_close_menu_footer"),
                    color = Color(0x3300E676),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SolarEmerald)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CLOSE MENU",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuItemRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    testTag: String,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag(testTag),
        color = if (isActive) Color(0x3300E676) else Color(0x660F2528),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) SolarEmerald else Color(0x22FFFFFF)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0x33000000)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color(0xFFB0BEC5),
                    fontSize = 9.sp,
                    maxLines = 1
                )
            }
        }
    }
}
