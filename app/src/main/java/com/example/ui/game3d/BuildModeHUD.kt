package com.example.ui.game3d

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.local.InventoryEntity
import com.example.data.model.BuildableType
import com.example.game3d.renderer.GhostBuildingState
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SunGold

@Composable
fun BuildModeHUD(
    selectedType: BuildableType,
    rotationDeg: Float,
    ghostState: GhostBuildingState?,
    gameState: GameStateEntity?,
    inventory: List<InventoryEntity>,
    onSelectType: (BuildableType) -> Unit,
    onRotate: () -> Unit,
    onConfirmPlace: () -> Unit,
    onExitBuildMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coins = gameState?.solCoins ?: 0
    val materialItem = inventory.find { it.itemId == selectedType.requiredMaterialId }
    val materialQty = materialItem?.quantity ?: 0
    val hasEnoughCoins = coins >= selectedType.costCoins
    val hasEnoughMaterials = selectedType.requiredMaterialQty == 0 || materialQty >= selectedType.requiredMaterialQty
    val canBuild = hasEnoughCoins && hasEnoughMaterials

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .border(1.dp, SolarEmerald.copy(alpha = 0.6f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .testTag("build_mode_hud"),
        color = Color(0xF209191C),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
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
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(SolarEmerald)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Solarpunk Architecture Mode",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rotate Button
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onRotate() }
                            .testTag("btn_rotate_building"),
                        color = Color(0xFF143034),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CleanCyan.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.RotateRight,
                                contentDescription = "Rotate",
                                tint = CleanCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${rotationDeg.toInt()}°",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Exit Build Mode Button
                    IconButton(
                        onClick = onExitBuildMode,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("btn_exit_build_mode")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Exit Build Mode",
                            tint = Color(0xFFFF8A80),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Category Tabs
            var selectedCategory by remember { mutableStateOf("All") }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("All", "⚡ Power & Batteries", "💧 Water & Irrigation", "🏡 Homestead").forEach { cat ->
                    val isCatActive = selectedCategory == cat
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedCategory = cat }
                            .testTag("build_cat_${cat.take(3).lowercase()}"),
                        color = if (isCatActive) SolarEmerald.copy(alpha = 0.25f) else Color(0xFF142428),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isCatActive) SolarEmerald else Color(0x3300E676)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (isCatActive) Color(0xFFB9F6CA) else Color(0xFFB0BEC5),
                            fontSize = 11.sp,
                            fontWeight = if (isCatActive) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filtered buildings list
            val filteredBuildings = BuildableType.values().filter { bType ->
                when (selectedCategory) {
                    "⚡ Power & Batteries" -> bType in listOf(
                        BuildableType.SOLAR_PANEL,
                        BuildableType.WINDMILL,
                        BuildableType.HYDRO_GENERATOR,
                        BuildableType.ADVANCED_SOLAR,
                        BuildableType.BIOGAS_GENERATOR,
                        BuildableType.GEOTHERMAL_VENT,
                        BuildableType.BASIC_BATTERY,
                        BuildableType.ADVANCED_BATTERY,
                        BuildableType.BATTERY_BANK,
                        BuildableType.POWER_POLE
                    )
                    "💧 Water & Irrigation" -> bType in listOf(
                        BuildableType.WELL,
                        BuildableType.RAIN_BARREL,
                        BuildableType.WATER_FILTER,
                        BuildableType.WATER_PURIFIER,
                        BuildableType.IRRIGATION_PIPE,
                        BuildableType.IRRIGATION_NODE,
                        BuildableType.WATER_STORAGE_SHED
                    )
                    "🏡 Homestead" -> bType in listOf(
                        BuildableType.CABIN,
                        BuildableType.GREENHOUSE,
                        BuildableType.STORAGE,
                        BuildableType.FENCE,
                        BuildableType.COMPOST_BIN
                    )
                    else -> true
                }
            }

            // Building Selection Scrollable Carousel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filteredBuildings.forEach { bType ->
                    val isSelected = bType == selectedType
                    val icon = getBuildingIcon(bType)

                    Card(
                        modifier = Modifier
                            .width(135.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectType(bType) }
                            .testTag("build_type_${bType.name.lowercase()}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xEE1A433E) else Color(0xAA0D2326)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) SolarEmerald else Color(0x3300E676)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = Color(bType.previewColor),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "${bType.costCoins}🪙",
                                    color = SunGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = bType.displayName,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = if (bType.requiredMaterialQty == 0) "Size: ${bType.size.toInt()}x${bType.size.toInt()}" else "${bType.requiredMaterialQty}x ${bType.materialName}",
                                color = Color(0xFFB0BEC5),
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = bType.gameplayEffect,
                                color = CleanCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Place Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cost summary & Material status
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (selectedType.requiredMaterialQty == 0) "Cost: ${selectedType.costCoins} 🪙 (Size: ${selectedType.size.toInt()}x${selectedType.size.toInt()})" else "Cost: ${selectedType.costCoins} 🪙 + ${selectedType.requiredMaterialQty}x ${selectedType.materialName}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (selectedType.requiredMaterialQty == 0) "You have: $coins 🪙" else "You have: $coins 🪙 | $materialQty ${selectedType.materialName}",
                        color = if (canBuild) Color(0xFF81C784) else Color(0xFFFF8A80),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Confirm Place Button
                Button(
                    onClick = onConfirmPlace,
                    enabled = canBuild,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canBuild) SolarEmerald else Color(0xFF455A64),
                        disabledContainerColor = Color(0xFF263238)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_confirm_place_building")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (canBuild) Color(0xFF091215) else Color(0xFF78909C),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (canBuild) "Place Structure" else "Not Enough Money",
                        color = if (canBuild) Color(0xFF091215) else Color(0xFF90A4AE),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun getBuildingIcon(type: BuildableType): ImageVector {
    return when (type) {
        BuildableType.CABIN -> Icons.Default.Home
        BuildableType.GREENHOUSE -> Icons.Default.Eco
        BuildableType.SOLAR_PANEL -> Icons.Default.Bolt
        BuildableType.WINDMILL -> Icons.Default.Air
        BuildableType.STORAGE -> Icons.Default.Storage
        BuildableType.WELL -> Icons.Default.WaterDrop
        BuildableType.FENCE -> Icons.Default.Build
        BuildableType.COMPOST_BIN -> Icons.Default.Refresh
        BuildableType.RAIN_BARREL -> Icons.Default.WaterDrop
        BuildableType.WATER_FILTER -> Icons.Default.Refresh
        BuildableType.WATER_PURIFIER -> Icons.Default.Bolt
        BuildableType.IRRIGATION_PIPE -> Icons.Default.Build
        BuildableType.IRRIGATION_NODE -> Icons.Default.Eco
        BuildableType.WATER_STORAGE_SHED -> Icons.Default.Storage
        BuildableType.HYDRO_GENERATOR -> Icons.Default.WaterDrop
        BuildableType.ADVANCED_SOLAR -> Icons.Default.Bolt
        BuildableType.BIOGAS_GENERATOR -> Icons.Default.Eco
        BuildableType.GEOTHERMAL_VENT -> Icons.Default.Bolt
        BuildableType.BASIC_BATTERY -> Icons.Default.Storage
        BuildableType.ADVANCED_BATTERY -> Icons.Default.Storage
        BuildableType.BATTERY_BANK -> Icons.Default.Storage
        BuildableType.POWER_POLE -> Icons.Default.Build
        BuildableType.NPC_CABIN -> Icons.Default.Home
        BuildableType.BUNKHOUSE -> Icons.Default.Home
        BuildableType.KITCHEN -> Icons.Default.Refresh
        BuildableType.MEDIC_STATION -> Icons.Default.Build
        BuildableType.WORKSHOP -> Icons.Default.Build
        BuildableType.RESEARCH_LAB -> Icons.Default.Eco
    }
}
