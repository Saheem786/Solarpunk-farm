package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.WeatherType
import com.example.ui.FarmViewModel
import com.example.ui.components.MedievalProgressBar
import com.example.ui.theme.*

data class FarmMilestone(
    val title: String,
    val description: String,
    val isAchieved: Boolean,
    val reward: String
)

@Composable
fun BusinessDashboardScreen(
    viewModel: FarmViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val plots by viewModel.plots.collectAsStateWithLifecycle()
    val energyNodes by viewModel.energyNodes.collectAsStateWithLifecycle()
    val livestock by viewModel.livestock.collectAsStateWithLifecycle()

    val totalHarvests = gameState?.totalHarvests ?: 0
    val carbonOffset = gameState?.carbonOffsetKg ?: 150f
    val ecoPrestige = gameState?.ecoPrestige ?: 100
    val currentWeather = gameState?.currentWeather ?: WeatherType.SUNNY_CLEAR

    val milestones = listOf(
        FarmMilestone(
            title = "First Bounty",
            description = "Harvest 5 sacred crops from the land",
            isAchieved = totalHarvests >= 5,
            reward = "+50 Prestige"
        ),
        FarmMilestone(
            title = "Mana Mastery",
            description = "Construct 4 ancient mana gathering or storage circles",
            isAchieved = energyNodes.size >= 4,
            reward = "+80 Prestige"
        ),
        FarmMilestone(
            title = "Beast Master",
            description = "Care for 4 happy regenerative creatures",
            isAchieved = livestock.size >= 4 && livestock.all { it.happiness >= 80f },
            reward = "+100 Prestige"
        ),
        FarmMilestone(
            title = "Nature's Chosen",
            description = "Restore the balance of nature across the islands",
            isAchieved = carbonOffset >= 200f,
            reward = "Royal Nature Seal"
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1A0F0A)) // Deep Wood
            .padding(16.dp)
            .testTag("business_dashboard_screen"),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header
        item {
            Column {
                Text(
                    text = "KINGDOM PROSPERITY & TRIBUTES",
                    color = AgedGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MedievalSharpFontFamily
                )
                Text(
                    text = "Resource tracking, royal prestige & environmental milestones of the realm",
                    color = MedievalTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Reputation Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("net_zero_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Parchment),
                border = androidx.compose.foundation.BorderStroke(2.dp, AgedGold)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(MedievalCrimson.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Park,
                                    contentDescription = null,
                                    tint = MedievalCrimson,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Balance: Restored",
                                    color = MedievalCrimson,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = MedievalSharpFontFamily
                                )
                                Text(
                                    text = "Royal Prestige: ${ecoPrestige / 50 + 1}",
                                    color = Bronze,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Surface(
                            color = MedievalCrimson.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MedievalCrimson)
                        ) {
                            Text(
                                text = "LEGENDARY FAVOR",
                                color = MedievalCrimson,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontFamily = MedievalSharpFontFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricBlock(
                            label = "Nature Balance",
                            value = "${carbonOffset.toInt()} units",
                            color = MedievalCrimson
                        )
                        MetricBlock(
                            label = "Ancient Mana",
                            value = "${(gameState?.batteryChargeKwh ?: 0f).toInt()} units",
                            color = Bronze
                        )
                        MetricBlock(
                            label = "Royal Tributes",
                            value = "$totalHarvests units",
                            color = AgedGold
                        )
                    }
                }
            }
        }

        // Weather Simulator / Atmospheric Control
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weather_controller_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Parchment),
                border = androidx.compose.foundation.BorderStroke(1.dp, AgedGold.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "DIVINE ATMOSPHERE CONTROL",
                        color = MedievalCrimson,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MedievalSharpFontFamily
                    )
                    Text(
                        text = "Current State: ${currentWeather.displayName}",
                        color = MedievalTextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        WeatherType.values().forEach { weather ->
                            val isSelected = weather == currentWeather
                            val icon = when (weather) {
                                WeatherType.SUNNY_CLEAR -> Icons.Default.WbSunny
                                WeatherType.CLOUDY_OVERCAST -> Icons.Default.Cloud
                                WeatherType.HEATWAVE -> Icons.Default.Whatshot
                                WeatherType.RAINY_STORM -> Icons.Default.Cloud
                                WeatherType.WIND_GALE -> Icons.Default.Air
                                WeatherType.MISTY_NEBULA -> Icons.Default.AutoAwesome
                                WeatherType.STORM -> Icons.Default.AutoFixHigh
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MedievalCrimson else Bronze.copy(alpha = 0.2f))
                                    .clickable { viewModel.changeWeather(weather) }
                                    .padding(vertical = 8.dp)
                                    .testTag("weather_btn_${weather.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = weather.displayName,
                                    tint = if (isSelected) Parchment else MedievalCrimson,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Milestones
        item {
            Text(
                text = "LEGENDARY DEEDS & ACCOLADES",
                color = AgedGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MedievalSharpFontFamily
            )
        }

        items(milestones) { milestone ->
            MilestoneCard(milestone = milestone)
        }
    }
}

@Composable
private fun MetricBlock(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = color,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = MedievalSharpFontFamily
        )
        Text(
            text = label,
            color = MedievalTextSecondary,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun MilestoneCard(milestone: FarmMilestone) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("milestone_${milestone.title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Parchment),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (milestone.isAchieved) AgedGold else Bronze.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        if (milestone.isAchieved) MedievalCrimson.copy(alpha = 0.1f) else Bronze.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = if (milestone.isAchieved) MedievalCrimson else Bronze,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = milestone.title,
                    color = if (milestone.isAchieved) MedievalCrimson else MedievalTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MedievalSharpFontFamily
                )
                Text(
                    text = milestone.description,
                    color = MedievalTextSecondary,
                    fontSize = 11.sp
                )
            }

            Text(
                text = if (milestone.isAchieved) "Unlocked ✨" else milestone.reward,
                color = if (milestone.isAchieved) Bronze else MedievalCrimson,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
