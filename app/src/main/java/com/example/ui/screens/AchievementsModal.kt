package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SunGold

data class AchievementItem(
    val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val progressCurrent: Int = 0,
    val progressMax: Int = 1
)

@Composable
fun AchievementsModal(
    unlockedAchievementIds: Set<String>,
    onDismiss: () -> Unit
) {
    val achievementsList = listOf(
        AchievementItem("ach_first_steps", "First Steps", "Build your first campfire and light it.", unlockedAchievementIds.contains("ach_first_steps"), Icons.Default.LocalFireDepartment),
        AchievementItem("ach_green_thumb", "Green Thumb", "Harvest 100 crops from your fields.", unlockedAchievementIds.contains("ach_green_thumb"), Icons.Default.Agriculture, 45, 100),
        AchievementItem("ach_master_farmer", "Master Farmer", "Harvest 1000 crops across all biomes.", unlockedAchievementIds.contains("ach_master_farmer"), Icons.Default.Grass, 120, 1000),
        AchievementItem("ach_power_up", "Power Up", "Construct your first solar panel or wind turbine.", unlockedAchievementIds.contains("ach_power_up"), Icons.Default.SolarPower),
        AchievementItem("ach_grid_master", "Grid Master", "Connect 20 buildings to the clean energy grid.", unlockedAchievementIds.contains("ach_grid_master"), Icons.Default.ElectricalServices, 8, 20),
        AchievementItem("ach_ecologist", "Ecologist", "Reach 90% overall ecosystem health.", unlockedAchievementIds.contains("ach_ecologist"), Icons.Default.Eco, 75, 90),
        AchievementItem("ach_zoologist", "Zoologist", "Discover and observe all 15 wildlife species.", unlockedAchievementIds.contains("ach_zoologist"), Icons.Default.Pets, 9, 15),
        AchievementItem("ach_explorer", "Explorer", "Discover all 15 Points of Interest across 3 biomes.", unlockedAchievementIds.contains("ach_explorer"), Icons.Default.Explore, 6, 15),
        AchievementItem("ach_storyteller", "Storyteller", "Complete all 12 story missions.", unlockedAchievementIds.contains("ach_storyteller"), Icons.Default.Book, 4, 12),
        AchievementItem("ach_mayor", "Mayor", "Host 8 NPCs in your settlement with 80%+ morale.", unlockedAchievementIds.contains("ach_mayor"), Icons.Default.Groups, 3, 8),
        AchievementItem("ach_fisherman", "Fisherman", "Catch 100 fish from rivers and ponds.", unlockedAchievementIds.contains("ach_fisherman"), Icons.Default.Sailing, 18, 100),
        AchievementItem("ach_master_chef", "Master Chef", "Cook 50 organic meals at campfire or kitchen.", unlockedAchievementIds.contains("ach_master_chef"), Icons.Default.Restaurant, 12, 50),
        AchievementItem("ach_architect", "Architect", "Place 100 solarpunk structures and pipes.", unlockedAchievementIds.contains("ach_architect"), Icons.Default.Foundation, 24, 100),
        AchievementItem("ach_researcher", "Researcher", "Unlock all technology tree nodes.", unlockedAchievementIds.contains("ach_researcher"), Icons.Default.Science, 5, 20),
        AchievementItem("ach_survivor", "Survivor", "Survive 50 in-game days.", unlockedAchievementIds.contains("ach_survivor"), Icons.Default.WbSunny, 12, 50),
        AchievementItem("ach_veteran", "Veteran", "Survive 100 in-game days.", unlockedAchievementIds.contains("ach_veteran"), Icons.Default.MilitaryTech, 12, 100),
        AchievementItem("ach_millionaire", "Millionaire", "Earn $10,000 SolCoins through trade.", unlockedAchievementIds.contains("ach_millionaire"), Icons.Default.MonetizationOn, 1450, 10000),
        AchievementItem("ach_eco_warrior", "Eco Warrior", "Plant 500 trees in damaged biomes.", unlockedAchievementIds.contains("ach_eco_warrior"), Icons.Default.Forest, 82, 500),
        AchievementItem("ach_new_dawn", "New Dawn", "Rebuild civilization and trigger the New Dawn ending.", unlockedAchievementIds.contains("ach_new_dawn"), Icons.Default.AutoAwesome),
        AchievementItem("ach_perfectionist", "Perfectionist", "Achieve 100% total game completion.", unlockedAchievementIds.contains("ach_perfectionist"), Icons.Default.EmojiEvents)
    )

    val unlockedCount = achievementsList.count { it.isUnlocked }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, SunGold.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                .testTag("modal_achievements"),
            color = Color(0xFD09181A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(SunGold, SolarEmerald))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Achievements",
                                tint = Color(0xFF091215),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "PLAY STORE ACHIEVEMENTS",
                                color = SunGold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Earned $unlockedCount / 20 badges",
                                color = Color(0xFF90A4AE),
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                            .testTag("btn_close_achievements_modal")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar Overall
                LinearProgressIndicator(
                    progress = { (unlockedCount.toFloat() / 20f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = SunGold,
                    trackColor = Color(0x33FFFFFF)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Achievements List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(achievementsList, key = { it.id }) { item ->
                        AchievementCard(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementCard(item: AchievementItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isUnlocked) Color(0x3300E676) else Color(0x22132A2D)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (item.isUnlocked) SolarEmerald else Color(0x33FFFFFF)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (item.isUnlocked) SolarEmerald else Color(0x33FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = if (item.isUnlocked) Color(0xFF091215) else Color(0xFF90A4AE),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        color = if (item.isUnlocked) SunGold else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (item.isUnlocked) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SolarEmerald
                        ) {
                            Text(
                                text = "UNLOCKED",
                                color = Color(0xFF091215),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (item.progressMax > 1) {
                        Text(
                            text = "${item.progressCurrent}/${item.progressMax}",
                            color = CleanCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.description,
                    color = Color(0xFFB0BEC5),
                    fontSize = 11.sp
                )
            }
        }
    }
}
