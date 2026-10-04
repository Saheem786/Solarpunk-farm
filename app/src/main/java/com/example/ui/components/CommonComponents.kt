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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
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
    modifier: Modifier = Modifier
) {
    if (state == null) return

    val hour = state.gameTimeHour
    val totalMinutes = (hour * 60).toInt()
    val h = (totalMinutes / 60) % 24
    val m = totalMinutes % 60
    val ampm = if (h < 12) "AM" else "PM"
    val displayH = if (h == 0) 12 else if (h > 12) h - 12 else h
    val timeFormatted = String.format("%02d:%02d %s", displayH, m, ampm)
    val isNight = h < 6 || h >= 20

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sol Coins Pill
        StatPill(
            icon = Icons.Default.MonetizationOn,
            iconTint = SunGold,
            label = "${state.solCoins}",
            testTag = "stat_coins"
        )

        // Battery Power Gauge Pill
        val chargePercent = ((state.batteryChargeKwh / state.batteryMaxCapacityKwh) * 100).roundToInt()
        StatPill(
            icon = Icons.Default.Bolt,
            iconTint = CleanCyan,
            label = "${state.batteryChargeKwh.toInt()}/${state.batteryMaxCapacityKwh.toInt()} kWh",
            secondaryLabel = "$chargePercent%",
            testTag = "stat_energy"
        )

        // Eco Prestige Pill
        StatPill(
            icon = Icons.Default.Eco,
            iconTint = SolarEmerald,
            label = "${state.ecoPrestige}",
            testTag = "stat_eco"
        )

        // Time / Weather Dial (Clickable to Fast-Forward)
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable { onAdvanceTimeClick() }
                .testTag("stat_time_button"),
            color = Color(0xCC112224),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SolarEmerald.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isNight) Icons.Default.Nightlight else Icons.Default.WbSunny,
                    contentDescription = "Day/Night Dial",
                    tint = if (isNight) CleanCyan else SunGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = timeFormatted,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
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
