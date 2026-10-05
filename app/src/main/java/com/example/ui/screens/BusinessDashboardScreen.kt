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
import com.example.ui.components.SolarpunkProgressBar
import com.example.ui.theme.BrassGold
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SunGold

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
            title = "First Light Harvest",
            description = "Harvest 5 Solarpunk Permaculture crops",
            isAchieved = totalHarvests >= 5,
            reward = "+50 Eco Prestige"
        ),
        FarmMilestone(
            title = "Clean Energy Independence",
            description = "Build 4 Clean Energy Generation & Storage nodes",
            isAchieved = energyNodes.size >= 4,
            reward = "+80 Eco Prestige"
        ),
        FarmMilestone(
            title = "Sanctuary Guardian",
            description = "Care for 4 happy regenerative animals",
            isAchieved = livestock.size >= 4 && livestock.all { it.happiness >= 80f },
            reward = "+100 Eco Prestige"
        ),
        FarmMilestone(
            title = "Net-Zero Carbon Pioneer",
            description = "Sequester over 200 kg of atmospheric carbon",
            isAchieved = carbonOffset >= 200f,
            reward = "Net-Zero Gold Seal"
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF081316))
            .padding(16.dp)
            .testTag("business_dashboard_screen"),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header
        item {
            Column {
                Text(
                    text = "Sustainability & Eco Ledger",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Carbon sequestration, clean power metrics & environmental milestones",
                    color = SolarEmerald,
                    fontSize = 12.sp
                )
            }
        }

        // Net Zero Hero Rating Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("net_zero_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E2827)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, SolarEmerald.copy(alpha = 0.6f))
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
                                    .background(SolarEmerald.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Park,
                                    contentDescription = null,
                                    tint = SolarEmerald,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Carbon Status: Negative",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Eco Prestige Level: ${ecoPrestige / 50 + 1}",
                                    color = SunGold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Surface(
                            color = SolarEmerald.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SolarEmerald)
                        ) {
                            Text(
                                text = "A++ Rating",
                                color = SolarEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricBlock(
                            label = "CO2 Offset",
                            value = "${carbonOffset.toInt()} kg",
                            color = SolarEmerald
                        )
                        MetricBlock(
                            label = "Clean Power",
                            value = "${(gameState?.batteryChargeKwh ?: 0f).toInt()} kWh",
                            color = CleanCyan
                        )
                        MetricBlock(
                            label = "Harvests",
                            value = "$totalHarvests units",
                            color = SunGold
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
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF102528)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3380D8FF))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Atmosphere & Weather Simulation",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Current: ${currentWeather.displayName} (${currentWeather.description})",
                        color = Color(0xFFB0BEC5),
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
                                WeatherType.STORM -> Icons.Default.Bolt
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) SolarEmerald else Color(0xFF163236))
                                    .clickable { viewModel.changeWeather(weather) }
                                    .padding(vertical = 8.dp)
                                    .testTag("weather_btn_${weather.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = weather.displayName,
                                    tint = if (isSelected) Color(0xFF091215) else Color(0xFFB0BEC5),
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
                text = "Environmental Milestones & Accolades",
                color = SunGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
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
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            color = Color(0xFFB0BEC5),
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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF112427)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (milestone.isAchieved) SolarEmerald else Color(0x22FFFFFF)
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
                        if (milestone.isAchieved) SolarEmerald.copy(alpha = 0.25f) else Color(0x22FFFFFF)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = if (milestone.isAchieved) SolarEmerald else Color(0xFF78909C),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = milestone.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = milestone.description,
                    color = Color(0xFFB0BEC5),
                    fontSize = 11.sp
                )
            }

            Text(
                text = if (milestone.isAchieved) "Unlocked ✨" else milestone.reward,
                color = if (milestone.isAchieved) SolarEmerald else SunGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
