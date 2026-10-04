package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Compost
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.EnergyNodeEntity
import com.example.data.model.EnergyNodeType
import com.example.ui.FarmViewModel
import com.example.ui.components.SolarpunkProgressBar
import com.example.ui.theme.BrassGold
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SunGold
import kotlin.math.roundToInt

@Composable
fun SolarEnergyScreen(
    viewModel: FarmViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val energyNodes by viewModel.energyNodes.collectAsStateWithLifecycle()

    val batteryCharge = gameState?.batteryChargeKwh ?: 0f
    val batteryMax = gameState?.batteryMaxCapacityKwh ?: 100f
    val chargePercent = ((batteryCharge / batteryMax) * 100).roundToInt().coerceIn(0, 100)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF081215))
            .padding(16.dp)
            .testTag("solar_energy_screen"),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header
        item {
            Column {
                Text(
                    text = "Clean Energy & Storage Grid",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Zero-emission photovoltaic, aero-kinetic, and solid-state storage",
                    color = Color(0xFF80D8FF),
                    fontSize = 12.sp
                )
            }
        }

        // Battery Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("battery_hero_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F262B)),
                border = androidx.compose.foundation.BorderStroke(1.dp, CleanCyan.copy(alpha = 0.5f))
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
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(CleanCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BatteryChargingFull,
                                    contentDescription = null,
                                    tint = CleanCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Solid-State Battery Storage",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Grid Status: 100% Renewable • Net Positive",
                                    color = SolarEmerald,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Text(
                            text = "$chargePercent%",
                            color = CleanCyan,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    SolarpunkProgressBar(
                        progress = batteryCharge / batteryMax,
                        color = CleanCyan,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Stored: ${batteryCharge.toInt()} kWh",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Capacity: ${batteryMax.toInt()} kWh",
                            color = Color(0xFFB0BEC5),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Active Energy Nodes Section
        item {
            Text(
                text = "Active Clean Power Generators (${energyNodes.size})",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(energyNodes) { node ->
            EnergyNodeCard(
                node = node,
                onUpgrade = { viewModel.upgradeEnergyNode(node.id) }
            )
        }

        // Build New Generation Node Shop
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Construct New Energy Facilities",
                color = SunGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(EnergyNodeType.values()) { type ->
            BuildNodeCard(
                type = type,
                onBuild = { viewModel.buyEnergyNode(type) }
            )
        }
    }
}

@Composable
private fun EnergyNodeCard(
    node: EnergyNodeEntity,
    onUpgrade: () -> Unit
) {
    val upgradeCost = node.level * 100
    val icon = when (node.nodeType) {
        EnergyNodeType.PHOTOVOLTAIC_ARRAY -> Icons.Default.WbSunny
        EnergyNodeType.VERTICAL_WIND_TURBINE -> Icons.Default.Air
        EnergyNodeType.BIOGAS_DIGESTER -> Icons.Default.Compost
        EnergyNodeType.BATTERY_STORAGE_BANK -> Icons.Default.Bolt
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("energy_node_${node.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF112529)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3300E5FF))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(node.nodeType.iconColor).copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(node.nodeType.iconColor),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = node.nodeType.displayName,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = SolarEmerald.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Lv ${node.level}",
                            color = SolarEmerald,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Efficiency: ${(node.efficiency * 100).toInt()}% • Location: (${node.posX.toInt()}, ${node.posZ.toInt()})",
                    color = Color(0xFFB0BEC5),
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = onUpgrade,
                colors = ButtonDefaults.buttonColors(containerColor = SunGold),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("upgrade_node_${node.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = Color(0xFF091215),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$upgradeCost🪙",
                    color = Color(0xFF091215),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BuildNodeCard(
    type: EnergyNodeType,
    onBuild: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("build_node_${type.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13292C)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFD54F))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(type.iconColor).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = Color(type.iconColor),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = type.displayName,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = type.description,
                    color = Color(0xFFB0BEC5),
                    fontSize = 11.sp
                )
                if (type.baseOutputKwhPerSec > 0f) {
                    Text(
                        text = "⚡ Base Output: +${type.baseOutputKwhPerSec} kWh/s",
                        color = CleanCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = "🔋 Storage: +${type.baseStorageCapacityKwh} kWh",
                        color = SolarEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Button(
                onClick = onBuild,
                colors = ButtonDefaults.buttonColors(containerColor = SolarEmerald),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("build_btn_${type.name.lowercase()}")
            ) {
                Text(
                    text = "${type.buildCostCoins}🪙",
                    color = Color(0xFF091215),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
