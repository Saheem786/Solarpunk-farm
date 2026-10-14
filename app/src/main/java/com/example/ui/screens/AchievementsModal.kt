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
import com.example.ui.components.MedievalProgressBar
import com.example.ui.theme.*

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
        AchievementItem("ach_power_up", "Mystic Pulse", "Construct your first Arcane Circle or Wind-Crystal.", unlockedAchievementIds.contains("ach_power_up"), Icons.Default.AutoFixHigh),
        AchievementItem("ach_grid_master", "Ley Line Master", "Connect 20 buildings to the mystic ley lines.", unlockedAchievementIds.contains("ach_grid_master"), Icons.Default.Stream, 8, 20),
        AchievementItem("ach_ecologist", "Realm Protector", "Reach 90% overall realm vitality.", unlockedAchievementIds.contains("ach_ecologist"), Icons.Default.Favorite, 75, 90),
        AchievementItem("ach_zoologist", "Beast Master", "Discover and observe all 15 noble beasts.", unlockedAchievementIds.contains("ach_zoologist"), Icons.Default.Pets, 9, 15),
        AchievementItem("ach_explorer", "World Traveler", "Discover all 15 Points of Interest across 3 regions.", unlockedAchievementIds.contains("ach_explorer"), Icons.Default.Explore, 6, 15),
        AchievementItem("ach_storyteller", "Legendary Bard", "Complete all 12 royal quests.", unlockedAchievementIds.contains("ach_storyteller"), Icons.Default.HistoryEdu, 4, 12),
        AchievementItem("ach_mayor", "Lord of the Keep", "Host 8 NPCs in your keep with 80%+ loyalty.", unlockedAchievementIds.contains("ach_mayor"), Icons.Default.Groups, 3, 8),
        AchievementItem("ach_fisherman", "Master Angler", "Catch 100 fish from royal rivers and ponds.", unlockedAchievementIds.contains("ach_fisherman"), Icons.Default.Sailing, 18, 100),
        AchievementItem("ach_master_chef", "Head Chef", "Cook 50 wholesome feasts at the hearth.", unlockedAchievementIds.contains("ach_master_chef"), Icons.Default.Restaurant, 12, 50),
        AchievementItem("ach_architect", "Grand Architect", "Place 100 ancient structures and mana conduits.", unlockedAchievementIds.contains("ach_architect"), Icons.Default.Foundation, 24, 100),
        AchievementItem("ach_researcher", "Archmage", "Unlock all knowledge from the Scroll of Wisdom.", unlockedAchievementIds.contains("ach_researcher"), Icons.Default.MenuBook, 5, 20),
        AchievementItem("ach_survivor", "Survivor of Seasons", "Survive 50 in-game days.", unlockedAchievementIds.contains("ach_survivor"), Icons.Default.WbSunny, 12, 50),
        AchievementItem("ach_veteran", "Veteran Explorer", "Survive 100 in-game days.", unlockedAchievementIds.contains("ach_veteran"), Icons.Default.MilitaryTech, 12, 100),
        AchievementItem("ach_millionaire", "Royal Treasurer", "Earn 10,000 Gold Crowns through trade.", unlockedAchievementIds.contains("ach_millionaire"), Icons.Default.MonetizationOn, 1450, 10000),
        AchievementItem("ach_eco_warrior", "Nature's Knight", "Plant 500 sacred trees in blighted lands.", unlockedAchievementIds.contains("ach_eco_warrior"), Icons.Default.Forest, 82, 500),
        AchievementItem("ach_new_dawn", "Golden Age", "Restore the Kingdom and trigger the Golden Age ending.", unlockedAchievementIds.contains("ach_new_dawn"), Icons.Default.AutoAwesome),
        AchievementItem("ach_perfectionist", "Living Legend", "Achieve 100% total realm restoration.", unlockedAchievementIds.contains("ach_perfectionist"), Icons.Default.EmojiEvents)
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
                .clip(RoundedCornerShape(12.dp))
                .border(3.dp, AgedGold, RoundedCornerShape(12.dp))
                .testTag("modal_achievements"),
            color = Parchment
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
                                .background(Brush.radialGradient(listOf(MedievalCrimson, MedievalCrimsonDark))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Achievements",
                                tint = Parchment,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "HEROIC FEATS & LEGENDS",
                                color = MedievalCrimson,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = MedievalSharpFontFamily,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Earned $unlockedCount / 20 badges",
                                color = MedievalTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MedievalCrimson.copy(alpha = 0.1f))
                            .testTag("btn_close_achievements_modal")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MedievalCrimson
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                MedievalProgressBar(
                    progress = (unlockedCount.toFloat() / 20f).coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth().height(14.dp)
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
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isUnlocked) MedievalCrimson.copy(alpha = 0.1f) else Color(0x11000000)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (item.isUnlocked) AgedGold else Bronze.copy(alpha = 0.5f)
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
                    .background(if (item.isUnlocked) MedievalCrimson else Color(0x33000000)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = if (item.isUnlocked) Parchment else MedievalTextSecondary.copy(alpha = 0.5f),
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
                        color = if (item.isUnlocked) MedievalCrimson else MedievalTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MedievalSharpFontFamily
                    )

                    if (item.isUnlocked) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AgedGold
                        ) {
                            Text(
                                text = "HONORED",
                                color = MedievalCrimsonDark,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (item.progressMax > 1) {
                        Text(
                            text = "${item.progressCurrent}/${item.progressMax}",
                            color = Bronze,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.description,
                    color = MedievalTextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}
