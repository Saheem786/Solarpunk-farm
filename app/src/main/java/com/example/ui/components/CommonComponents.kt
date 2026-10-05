package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.LocalFlorist
import com.example.data.local.PlotEntity
import com.example.data.model.CropStage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.data.local.GameStateEntity
import com.example.data.model.PlayerTool
import com.example.data.model.WeatherType
import com.example.ui.NotificationMessage
import com.example.ui.theme.BrassGold
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SunGold
import kotlin.math.roundToInt

@Composable
fun TopGameStatsBar(
    state: GameStateEntity?,
    onAdvanceTimeClick: () -> Unit,
    plots: List<PlotEntity> = emptyList(),
    ecosystemScore: Int? = null,
    modifier: Modifier = Modifier
) {
    if (state == null) return

    val hour = state.gameTimeHour
    val totalMinutes = (hour * 60).toInt()
    val h = (totalMinutes / 60) % 24
    val m = totalMinutes % 60
    val ampm = if (h < 12) "AM" else "PM"
    val displayH = if (h == 0) 12 else if (h > 12) h - 12 else h
    val timeFormatted = String.format("Day %d • %02d:%02d %s", state.gameTimeDay, displayH, m, ampm)
    val isNight = h < 6 || h >= 20

    val weather = state.currentWeather
    val weatherIcon = when (weather) {
        WeatherType.RAINY_STORM -> Icons.Default.WaterDrop
        WeatherType.CLOUDY_OVERCAST -> Icons.Default.Cloud
        WeatherType.WIND_GALE -> Icons.Default.Air
        WeatherType.HEATWAVE -> Icons.Default.Whatshot
        WeatherType.MISTY_NEBULA -> Icons.Default.Cloud
        WeatherType.SUNNY_CLEAR -> Icons.Default.WbSunny
        WeatherType.STORM -> Icons.Default.Bolt
    }
    val weatherTint = when (weather) {
        WeatherType.RAINY_STORM -> Color(0xFF00E5FF)
        WeatherType.CLOUDY_OVERCAST -> Color(0xFFB0BEC5)
        WeatherType.WIND_GALE -> Color(0xFF80DEEA)
        WeatherType.HEATWAVE -> Color(0xFFFFB300)
        WeatherType.MISTY_NEBULA -> Color(0xFF80CBC4)
        WeatherType.SUNNY_CLEAR -> SunGold
        WeatherType.STORM -> Color(0xFFFFEB3B)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Day / Time Dial (Clickable to Fast-Forward +2h)
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onAdvanceTimeClick() }
                    .testTag("stat_time_button"),
                color = Color(0xDD112224),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SolarEmerald.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isNight) Icons.Default.Nightlight else Icons.Default.WbSunny,
                        contentDescription = "Day/Night Dial",
                        tint = if (isNight) CleanCyan else SunGold,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = timeFormatted,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 2. Weather Status Pill (Icon + Solar % + Wind Speed)
            val isSolarGenerating = h in 7..16
            val solarPercent = if (!isSolarGenerating) 0 else ((weather.solarMultiplier * 100).roundToInt())
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("stat_weather_pill"),
                color = Color(0xDD112224),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, weatherTint.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = weatherIcon,
                        contentDescription = weather.displayName,
                        tint = weatherTint,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${weather.displayName.split(" ")[0]} • Solar $solarPercent%",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // 3. Sol Coins Pill
            StatPill(
                icon = Icons.Default.MonetizationOn,
                iconTint = SunGold,
                label = "${state.solCoins}",
                testTag = "stat_coins"
            )

            // 4. Battery Power Gauge Pill
            val chargePercent = ((state.batteryChargeKwh / state.batteryMaxCapacityKwh) * 100).roundToInt()
            StatPill(
                icon = Icons.Default.Bolt,
                iconTint = CleanCyan,
                label = "${state.batteryChargeKwh.toInt()} kWh",
                secondaryLabel = "$chargePercent%",
                testTag = "stat_energy"
            )
        }

        // Crops Ready & Ecosystem Score Row (with leaf icon)
        val readyCropsCount = plots.count { it.stage == CropStage.HARVEST_READY }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ecosystem Health Pill
            val ecoScore = (ecosystemScore ?: state.ecoPrestige).coerceIn(0, 100)
            val ecoColor = when {
                ecoScore < 50 -> Color(0xFFE57373) // red
                ecoScore <= 75 -> Color(0xFFFFD54F) // yellow/gold
                else -> Color(0xFF81C784) // green
            }
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("hud_ecosystem_score"),
                color = Color(0xCC112224),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ecoColor.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = "Ecosystem Health",
                        tint = ecoColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Ecosystem: $ecoScore%",
                        color = ecoColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // Crops Ready Pill
            val glowColor = if (readyCropsCount > 0) Color(0xFF00E676) else Color(0xFF90A4AE)
            val borderColor = if (readyCropsCount > 0) Color(0xFF00E676).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.2f)
            val backgroundColor = if (readyCropsCount > 0) Color(0x3300E676) else Color(0xCC112224)
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("hud_crops_ready"),
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFlorist,
                        contentDescription = "Crops Ready",
                        tint = glowColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Crops Ready: $readyCropsCount",
                        color = if (readyCropsCount > 0) Color(0xFFB9F6CA) else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
fun StatPill(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    secondaryLabel: String? = null,
    testTag: String = ""
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .testTag(testTag),
        color = Color(0xCC112224),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x22FFFFFF))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (secondaryLabel != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = secondaryLabel,
                    color = iconTint,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ToolSelectorDock(
    selectedTool: PlayerTool,
    onSelectTool: (PlayerTool) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, SolarEmerald.copy(alpha = 0.35f), RoundedCornerShape(24.dp)),
        color = Color(0xEE0B1A1C),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerTool.values().forEach { tool ->
                val isSelected = tool == selectedTool
                val bgBrush = if (isSelected) {
                    Brush.verticalGradient(listOf(SolarEmerald.copy(alpha = 0.85f), SolarEmerald.copy(alpha = 0.4f)))
                } else {
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(bgBrush)
                        .border(
                            width = if (isSelected) 1.5.dp else 0.dp,
                            color = if (isSelected) SunGold else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onSelectTool(tool) }
                        .testTag("tool_${tool.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (tool) {
                        PlayerTool.HAND -> Icons.Default.Pets
                        PlayerTool.WATER_CAN -> Icons.Default.Cloud
                        PlayerTool.FERTILIZER -> Icons.Default.Eco
                        PlayerTool.SOLAR_WRENCH -> Icons.Default.Bolt
                        PlayerTool.SHEARS -> Icons.Default.Star
                        PlayerTool.SEED_POUCH -> Icons.Default.MonetizationOn
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = tool.displayName,
                        tint = if (isSelected) Color.White else Color(0xFFB0BEC5),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SolarpunkNotificationBanner(
    notification: NotificationMessage?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = notification != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        if (notification != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("notification_banner"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFA122E28)),
                border = androidx.compose.foundation.BorderStroke(1.dp, SolarEmerald.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SolarEmerald.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SolarEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = notification.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = notification.description,
                            color = Color(0xFFB0BEC5),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SolarpunkProgressBar(
    progress: Float,
    color: Color = SolarEmerald,
    modifier: Modifier = Modifier
) {
    val clamped = progress.coerceIn(0.0f, 1.0f)
    Box(
        modifier = modifier
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF263238))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(clamped)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(color.copy(alpha = 0.7f), color)
                    )
                )
        )
    }
}

@Composable
fun SurvivalStatsHUD(
    state: GameStateEntity?,
    onEatClick: () -> Unit,
    onDrinkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state == null) return

    val health = state.health.coerceIn(0f, state.maxHealth)
    val hunger = state.hunger.coerceIn(0f, state.maxHunger)
    val thirst = state.thirst.coerceIn(0f, state.maxThirst)
    val stamina = state.stamina.coerceIn(0f, state.maxStamina)

    val healthFrac = (health / state.maxHealth).coerceIn(0f, 1f)
    val hungerFrac = (hunger / state.maxHunger).coerceIn(0f, 1f)
    val thirstFrac = (thirst / state.maxThirst).coerceIn(0f, 1f)
    val staminaFrac = (stamina / state.maxStamina).coerceIn(0f, 1f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .testTag("survival_stats_hud"),
        color = Color(0xDD0D1B1E),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3300E676))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 4 Mini Stat Bars Grid
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Health Bar
                SurvivalStatItem(
                    icon = Icons.Default.Favorite,
                    iconTint = Color(0xFFFF5252),
                    label = "HP",
                    valueText = "${health.toInt()}",
                    fraction = healthFrac,
                    barColor = Color(0xFFFF5252),
                    modifier = Modifier.weight(1f),
                    testTag = "stat_bar_health"
                )

                // 2. Hunger Bar
                SurvivalStatItem(
                    icon = Icons.Default.Restaurant,
                    iconTint = Color(0xFFFF9800),
                    label = "Food",
                    valueText = "${hunger.toInt()}%",
                    fraction = hungerFrac,
                    barColor = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f),
                    testTag = "stat_bar_hunger"
                )

                // 3. Thirst Bar
                SurvivalStatItem(
                    icon = Icons.Default.WaterDrop,
                    iconTint = Color(0xFF00E5FF),
                    label = "Water",
                    valueText = "${thirst.toInt()}%",
                    fraction = thirstFrac,
                    barColor = Color(0xFF00E5FF),
                    modifier = Modifier.weight(1f),
                    testTag = "stat_bar_thirst"
                )

                // 4. Stamina Bar
                SurvivalStatItem(
                    icon = Icons.Default.DirectionsRun,
                    iconTint = Color(0xFF00E676),
                    label = "Stamina",
                    valueText = "${stamina.toInt()}%",
                    fraction = staminaFrac,
                    barColor = Color(0xFF00E676),
                    modifier = Modifier.weight(1f),
                    testTag = "stat_bar_stamina"
                )
            }
        }
    }
}

@Composable
private fun SurvivalStatItem(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    valueText: String,
    fraction: Float,
    barColor: Color,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Column(
        modifier = modifier.testTag(testTag),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = valueText,
                color = if (fraction < 0.25f) Color(0xFFFF5252) else Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        // Micro Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF1E282D))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(barColor.copy(alpha = 0.75f), barColor)
                        )
                    )
            )
        }
    }
}
