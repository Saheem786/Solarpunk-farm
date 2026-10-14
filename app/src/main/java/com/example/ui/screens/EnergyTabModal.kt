package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.EnergyNodeEntity
import com.example.data.local.PlacedBuildingEntity
import com.example.data.model.BuildableType
import com.example.data.model.DeviceType
import com.example.data.model.EnergyGridSummary
import com.example.data.model.EnergyNodeType
import com.example.data.model.EnergyResearchTech
import com.example.data.model.PowerPriority
import com.example.ui.FarmViewModel
import com.example.ui.components.MedievalProgressBar
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun EnergyTabModal(
    viewModel: FarmViewModel,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(3.dp, AgedGold, RoundedCornerShape(12.dp))
                .testTag("energy_tab_modal"),
            color = Parchment,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Modal Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MedievalCrimson.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = MedievalCrimson,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ARCANE ESSENCE & POWER WEAVING",
                                color = MedievalCrimson,
                                fontFamily = MedievalSharpFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Generation, Storage, and Master Grid Flow",
                                color = MedievalTextSecondary,
                                fontFamily = AlmendraFontFamily,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_close_energy_tab")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MedievalCrimson,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Main Reusable Content
                EnergyGridManagementContent(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun EnergyGridManagementContent(
    viewModel: FarmViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val energyNodes by viewModel.energyNodes.collectAsStateWithLifecycle()
    val placedBuildings by viewModel.placedBuildings.collectAsStateWithLifecycle()
    val energySummary by viewModel.energySummary.collectAsStateWithLifecycle()
    val devicePriorities by viewModel.devicePriorities.collectAsStateWithLifecycle()
    val unlockedTechs by viewModel.unlockedTechs.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableStateOf("NET_BALANCE") }

    val tabs = listOf(
        Pair("NET_BALANCE", "Flow"),
        Pair("GENERATION", "Creation"),
        Pair("STORAGE", "Vessels"),
        Pair("PRIORITIES", "Wards"),
        Pair("GRID_MAP", "Scroll"),
        Pair("RESEARCH", "Alchemy")
    )

    Column(
        modifier = modifier
            .background(Parchment)
            .testTag("energy_grid_management_content")
    ) {
        // Sub-Tab Switcher Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tabs.forEach { (key, label) ->
                val isSelected = activeSubTab == key
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { activeSubTab = key }
                        .testTag("energy_tab_${key.lowercase()}"),
                    color = if (isSelected) MedievalCrimson else AgedGold.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) AgedGold else AgedGold.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Parchment else MedievalTextPrimary,
                        fontFamily = MedievalSharpFontFamily,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Section Content
        when (activeSubTab) {
            "NET_BALANCE" -> NetBalanceSection(energySummary, gameState?.batteryChargeKwh ?: 0f, gameState?.batteryMaxCapacityKwh ?: 100f)
            "GENERATION" -> GenerationSection(energyNodes, placedBuildings, energySummary)
            "STORAGE" -> StorageSection(placedBuildings, energyNodes, energySummary)
            "PRIORITIES" -> PrioritiesSection(energySummary, devicePriorities) { dev, prio ->
                viewModel.setDevicePriority(dev, prio)
            }
            "GRID_MAP" -> GridMapSection(energyNodes, placedBuildings, energySummary)
            "RESEARCH" -> ResearchSection(gameState?.ecoPrestige ?: 0, unlockedTechs) { techId, rp ->
                viewModel.unlockEnergyTech(techId, rp)
            }
        }
    }
}

@Composable
private fun NetBalanceSection(
    summary: com.example.data.model.EnergyGridSummary,
    batteryCharge: Float,
    batteryMax: Float
) {
    val chargePercent = ((batteryCharge / (if (batteryMax > 0f) batteryMax else 100f)) * 100).roundToInt().coerceIn(0, 100)
    val isNetPositive = summary.netHourlyKwh > 0.0f
    val isBalanced = kotlin.math.abs(summary.netHourlyKwh) <= 0.05f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Battery Flash Alert if low
        if (summary.batteryPercent < 0.20f) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("energy_low_banner"),
                    color = MedievalCrimson.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MedievalCrimson)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MedievalCrimson,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "CRITICAL: Mana Below 20%!",
                                color = MedievalCrimson,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = MedievalSharpFontFamily
                            )
                            Text(
                                text = "Non-essential rituals disabled by priority. Gather more mana.",
                                color = MedievalTextPrimary,
                                fontSize = 10.sp,
                                fontFamily = AlmendraFontFamily
                            )
                        }
                    }
                }
            }
        }

        // Hero Battery & Net Flow Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("net_balance_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Parchment),
                border = androidx.compose.foundation.BorderStroke(2.dp, AgedGold.copy(alpha = 0.8f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isNetPositive) ForestMoss.copy(alpha = 0.2f) else if (isBalanced) AgedGold.copy(alpha = 0.2f) else MedievalCrimson.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isNetPositive) Icons.Default.ArrowUpward else if (isBalanced) Icons.Default.Remove else Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = if (isNetPositive) ForestMoss else if (isBalanced) AgedGoldDark else MedievalCrimson,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isNetPositive) "Net Surplus • Mana Gathering" else if (isBalanced) "Balanced Mana Flow" else "Net Deficit • Mana Draining",
                                    color = if (isNetPositive) ForestMoss else if (isBalanced) AgedGoldDark else MedievalCrimson,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = MedievalSharpFontFamily
                                )
                                Text(
                                    text = "Net Flow: ${if (summary.netHourlyKwh >= 0) "+" else ""}${String.format("%.1f", summary.netHourlyKwh)} kWh/hr",
                                    color = MedievalTextPrimary,
                                    fontSize = 11.sp,
                                    fontFamily = AlmendraFontFamily
                                )
                            }
                        }

                        Text(
                            text = "$chargePercent%",
                            color = MedievalCrimson,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = MedievalSharpFontFamily
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    MedievalProgressBar(
                        progress = summary.batteryPercent,
                        modifier = Modifier.fillMaxWidth().height(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Stored: ${summary.currentStoredKwh.toInt()} / ${summary.totalStorageCapacityKwh.toInt()} kWh",
                            color = MedievalTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AlmendraFontFamily
                        )
                        if (!isNetPositive && !isBalanced && summary.hoursUntilEmpty < 900f) {
                            Text(
                                text = "Empty in: ${String.format("%.1f", summary.hoursUntilEmpty)} hrs",
                                color = MedievalCrimson,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AlmendraFontFamily
                            )
                        } else if (isNetPositive) {
                            Text(
                                text = "Status: Healthy Storage",
                                color = ForestMoss,
                                fontSize = 11.sp,
                                fontFamily = AlmendraFontFamily
                            )
                        }
                    }
                }
            }
        }

        // Summary Metric Tiles
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "Total Generation",
                    value = "${String.format("%.1f", summary.totalGenerationKwhPerDay)} kWh/day",
                    sub = "Current: ${String.format("%.1f", summary.currentGenerationKwhPerHour)} kW",
                    color = AgedGoldDark,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Total Consumption",
                    value = "${String.format("%.1f", summary.totalHourlyConsumptionKwh * 24f)} kWh/day",
                    sub = "Current: ${String.format("%.1f", summary.totalHourlyConsumptionKwh)} kW",
                    color = RoyalBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "Connected Grid",
                    value = "${summary.powerSourcesCount} Sources • ${summary.batteryUnitsCount} Bats",
                    sub = "${summary.powerPolesCount} Power Poles Active",
                    color = ForestMoss,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Devices Powered",
                    value = "${summary.activeDevicesCount} / ${summary.totalDevicesCount} Online",
                    sub = if (summary.unpoweredDevicesCount > 0) "${summary.unpoweredDevicesCount} Low Power Off" else "All Powered",
                    color = if (summary.unpoweredDevicesCount > 0) MedievalCrimson else ForestMoss,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    sub: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp)),
        color = Parchment,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                color = MedievalTextSecondary,
                fontSize = 10.sp,
                fontFamily = AlmendraFontFamily
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = MedievalTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MedievalSharpFontFamily
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sub,
                color = color,
                fontSize = 10.sp,
                fontFamily = AlmendraFontFamily
            )
        }
    }
}

@Composable
private fun GenerationSection(
    energyNodes: List<EnergyNodeEntity>,
    placedBuildings: List<PlacedBuildingEntity>,
    summary: com.example.data.model.EnergyGridSummary
) {
    val sources = mutableListOf<com.example.data.model.PowerSourceInfo>()

    // Nodes
    energyNodes.forEach { node ->
        when (node.nodeType) {
            EnergyNodeType.PHOTOVOLTAIC_ARRAY -> {
                sources.add(
                    com.example.data.model.PowerSourceInfo(
                        name = "Arcane Mirror Array (Level ${node.level})",
                        typeName = "Sun-Focusing Glass",
                        generationKwhPerDay = 15.0f * node.efficiency,
                        currentGenerationKw = 1.5f * node.efficiency,
                        status = "Operating • Sunlit Hours Only",
                        isOperating = true,
                        location = "East Watch"
                    )
                )
            }
            EnergyNodeType.VERTICAL_WIND_TURBINE -> {
                sources.add(
                    com.example.data.model.PowerSourceInfo(
                        name = "Aero-Spire Wind-Crystal (Level ${node.level})",
                        typeName = "Gale-Bound Crystal",
                        generationKwhPerDay = 18.0f * node.efficiency,
                        currentGenerationKw = 0.75f * node.efficiency,
                        status = "Operating 24/7",
                        isOperating = true,
                        location = "Gale Ridge"
                    )
                )
            }
            EnergyNodeType.BIOGAS_DIGESTER -> {
                sources.add(
                    com.example.data.model.PowerSourceInfo(
                        name = "Earthen Mana Vat (Level ${node.level})",
                        typeName = "Primal Fermentation",
                        generationKwhPerDay = 12.0f * node.efficiency,
                        currentGenerationKw = 0.50f * node.efficiency,
                        status = "Operating 24/7",
                        isOperating = true,
                        location = "The Glade"
                    )
                )
            }
            else -> {}
        }
    }

    // Placed Buildings
    placedBuildings.forEach { b ->
        when (b.buildingType) {
            BuildableType.SOLAR_PANEL -> {
                sources.add(
                    com.example.data.model.PowerSourceInfo(
                        name = "Mystic Stone Circle #${b.id}",
                        typeName = "Mana Gathering Ritual",
                        generationKwhPerDay = 10.0f,
                        currentGenerationKw = 1.0f,
                        status = "Gathers Mana 7 AM - 5 PM (10 Mana/day)",
                        isOperating = true,
                        location = "X: ${b.posX.toInt()}, Z: ${b.posZ.toInt()}"
                    )
                )
            }
            BuildableType.WINDMILL -> {
                sources.add(
                    com.example.data.model.PowerSourceInfo(
                        name = "Aeolian Spire #${b.id}",
                        typeName = "Kinetic Air Essence",
                        generationKwhPerDay = 15.0f,
                        currentGenerationKw = 0.625f,
                        status = "Operating 24/7 (Breezes never cease)",
                        isOperating = true,
                        location = "X: ${b.posX.toInt()}, Z: ${b.posZ.toInt()}"
                    )
                )
            }
            BuildableType.HYDRO_GENERATOR -> {
                sources.add(
                    com.example.data.model.PowerSourceInfo(
                        name = "Oak Water Wheel #${b.id}",
                        typeName = "Hydro Essence Harvest",
                        generationKwhPerDay = 20.0f,
                        currentGenerationKw = 0.833f,
                        status = "Operating 24/7 continuous stream current",
                        isOperating = true,
                        location = "Near River/Stream"
                    )
                )
            }
            BuildableType.ADVANCED_SOLAR -> {
                sources.add(
                    com.example.data.model.PowerSourceInfo(
                        name = "Crystal Sun Obelisk #${b.id}",
                        typeName = "Prismatic Sun-Tracking",
                        generationKwhPerDay = 30.0f,
                        currentGenerationKw = 3.6f,
                        status = "Light-Focusing Active (+20% efficiency bonus)",
                        isOperating = true,
                        location = "X: ${b.posX.toInt()}, Z: ${b.posZ.toInt()}"
                    )
                )
            }
            BuildableType.BIOGAS_GENERATOR -> {
                sources.add(
                    com.example.data.model.PowerSourceInfo(
                        name = "Alchemical Compost Vat #${b.id}",
                        typeName = "Earth Essence Fermentation",
                        generationKwhPerDay = 12.0f,
                        currentGenerationKw = 0.50f,
                        status = "Operating 24/7 (Consumes 1 compost/day)",
                        isOperating = true,
                        location = "Compost / Livestock Pen"
                    )
                )
            }
            BuildableType.GEOTHERMAL_VENT -> {
                sources.add(
                    com.example.data.model.PowerSourceInfo(
                        name = "Volcanic Power Forge #${b.id}",
                        typeName = "Deep Thermal Earth Core",
                        generationKwhPerDay = 40.0f,
                        currentGenerationKw = 1.667f,
                        status = "Operating 24/7 continuous base-load",
                        isOperating = true,
                        location = "Mountain Thermal Hotspot"
                    )
                )
            }
            else -> {}
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mana Generation Sources (${sources.size})",
                    color = MedievalCrimson,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MedievalSharpFontFamily
                )
                Text(
                    text = "Total: ${String.format("%.1f", summary.totalGenerationKwhPerDay)} mana/day",
                    color = AgedGoldDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MedievalSharpFontFamily
                )
            }
        }

        if (sources.isEmpty()) {
            item {
                Text(
                    text = "No mana sources gathered yet. Build Solar Arrays, Windspires, or Water Wheels in Build Mode!",
                    color = MedievalTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = AlmendraFontFamily,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }

        items(sources) { src ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                color = Parchment,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AgedGold.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(AgedGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = AgedGoldDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = src.name,
                            color = MedievalCrimson,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MedievalSharpFontFamily
                        )
                        Text(
                            text = "${src.typeName} • ${src.location}",
                            color = MedievalTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = AlmendraFontFamily
                        )
                        Text(
                            text = src.status,
                            color = ForestMoss,
                            fontSize = 9.5.sp,
                            fontFamily = AlmendraFontFamily
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${String.format("%.1f", src.generationKwhPerDay)} m/d",
                            color = AgedGoldDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MedievalSharpFontFamily
                        )
                        Text(
                            text = "${String.format("%.2f", src.currentGenerationKw)} m/hr",
                            color = MedievalCrimson,
                            fontSize = 10.sp,
                            fontFamily = AlmendraFontFamily
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StorageSection(
    placedBuildings: List<PlacedBuildingEntity>,
    energyNodes: List<EnergyNodeEntity>,
    summary: com.example.data.model.EnergyGridSummary
) {
    val batteries = mutableListOf<com.example.data.model.BatteryUnitInfo>()

    // Farmhouse base battery
    batteries.add(
        com.example.data.model.BatteryUnitInfo(
            name = "Citadel Central Mana Vessel",
            capacityKwh = 50.0f,
            currentChargeKwh = kotlin.math.min(50.0f, summary.currentStoredKwh),
            maxChargeRateKw = 5.0f,
            status = "Ancient Runically-Bound Basin"
        )
    )

    placedBuildings.forEach { b ->
        when (b.buildingType) {
            BuildableType.BASIC_BATTERY -> {
                batteries.add(
                    com.example.data.model.BatteryUnitInfo(
                        name = "Basic Battery #${b.id}",
                        capacityKwh = 50.0f,
                        currentChargeKwh = 50.0f * summary.batteryPercent,
                        maxChargeRateKw = 5.0f,
                        status = "2x2 Compact Cabinet • 5 kW Rate"
                    )
                )
            }
            BuildableType.ADVANCED_BATTERY -> {
                batteries.add(
                    com.example.data.model.BatteryUnitInfo(
                        name = "Advanced Battery #${b.id}",
                        capacityKwh = 150.0f,
                        currentChargeKwh = 150.0f * summary.batteryPercent,
                        maxChargeRateKw = 15.0f,
                        status = "3x3 Multi-Cell Cabinet • 15 kW Rate"
                    )
                )
            }
            BuildableType.BATTERY_BANK -> {
                batteries.add(
                    com.example.data.model.BatteryUnitInfo(
                        name = "Industrial Battery Bank #${b.id}",
                        capacityKwh = 500.0f,
                        currentChargeKwh = 500.0f * summary.batteryPercent,
                        maxChargeRateKw = 50.0f,
                        status = "5x5 Substation • Dual Cooling Fans • 50 kW Rate"
                    )
                )
            }
            BuildableType.STORAGE -> {
                batteries.add(
                    com.example.data.model.BatteryUnitInfo(
                        name = "Shed Arcane Storage",
                        capacityKwh = 60.0f,
                        currentChargeKwh = 60.0f * summary.batteryPercent,
                        maxChargeRateKw = 5.0f,
                        status = "Auxiliary Mana Storage"
                    )
                )
            }
            else -> {}
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mana Storage Vessels (${batteries.size})",
                    color = MedievalCrimson,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MedievalSharpFontFamily
                )
                Text(
                    text = "Total: ${summary.currentStoredKwh.toInt()}/${summary.totalStorageCapacityKwh.toInt()} m",
                    color = AgedGoldDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MedievalSharpFontFamily
                )
            }
        }

        items(batteries) { bat ->
            val batPct = (bat.currentChargeKwh / bat.capacityKwh).coerceIn(0f, 1f)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                color = Parchment,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AgedGold.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(AgedGold.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BatteryChargingFull,
                                    contentDescription = null,
                                    tint = AgedGoldDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = bat.name,
                                    color = MedievalCrimson,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = MedievalSharpFontFamily
                                )
                                Text(
                                    text = bat.status,
                                    color = MedievalTextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = AlmendraFontFamily
                                )
                            }
                        }

                        Text(
                            text = "${bat.currentChargeKwh.toInt()}/${bat.capacityKwh.toInt()} m",
                            color = AgedGoldDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MedievalSharpFontFamily
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    MedievalProgressBar(
                        progress = batPct,
                        modifier = Modifier.fillMaxWidth().height(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PrioritiesSection(
    summary: com.example.data.model.EnergyGridSummary,
    devicePriorities: Map<DeviceType, PowerPriority>,
    onSetPriority: (DeviceType, PowerPriority) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Mystic Wards & Ritual Priorities",
                    color = MedievalCrimson,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MedievalSharpFontFamily
                )
                Text(
                    text = "When mana drops below threshold, lower priority rituals fade automatically to protect sacred vessels.",
                    color = MedievalTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = AlmendraFontFamily
                )
            }
        }

        // Priority Legend
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PowerPriority.values().forEach { prio ->
                    Surface(
                        color = Color(prio.colorHex).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(prio.colorHex))
                    ) {
                        Text(
                            text = "${prio.displayName}: >${(prio.thresholdPercent * 100).toInt()}%",
                            color = Color(prio.colorHex),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // List of Devices
        items(DeviceType.values()) { device ->
            val currentPrio = devicePriorities[device] ?: device.defaultPriority
            val isPowered = summary.batteryPercent >= currentPrio.thresholdPercent && summary.currentStoredKwh > 0.05f

            DevicePriorityRow(
                device = device,
                currentPriority = currentPrio,
                isPowered = isPowered,
                onSetPriority = { onSetPriority(device, it) }
            )
        }
    }
}

@Composable
private fun DevicePriorityRow(
    device: DeviceType,
    currentPriority: PowerPriority,
    isPowered: Boolean,
    onSetPriority: (PowerPriority) -> Unit
) {
    var dropdownOpen by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .testTag("device_row_${device.id}"),
        color = Parchment,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isPowered) ForestMoss.copy(alpha = 0.4f) else MedievalCrimson.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = device.displayName,
                        color = MedievalCrimson,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MedievalSharpFontFamily
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = if (isPowered) ForestMoss.copy(alpha = 0.1f) else MedievalCrimson.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isPowered) "ACTIVE" else "FADED (LOW MANA)",
                            color = if (isPowered) ForestMoss else MedievalCrimson,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MedievalSharpFontFamily,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = "${device.tierLabel} • ${device.consumptionKwhPerHour} mana/hr",
                    color = MedievalTextSecondary,
                    fontSize = 9.5.sp,
                    fontFamily = AlmendraFontFamily
                )
            }

            Box {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { dropdownOpen = true }
                        .testTag("prio_select_${device.id}"),
                    color = Color(currentPriority.colorHex).copy(alpha = 0.25f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(currentPriority.colorHex)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentPriority.displayName,
                            color = Color(currentPriority.colorHex),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color(currentPriority.colorHex),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = dropdownOpen,
                    onDismissRequest = { dropdownOpen = false }
                ) {
                    PowerPriority.values().forEach { prio ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${prio.displayName} (>${(prio.thresholdPercent * 100).toInt()}%)",
                                    color = Color(prio.colorHex),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            },
                            onClick = {
                                onSetPriority(prio)
                                dropdownOpen = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GridMapSection(
    energyNodes: List<EnergyNodeEntity>,
    placedBuildings: List<PlacedBuildingEntity>,
    summary: com.example.data.model.EnergyGridSummary
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ancient Ley Line & Mana Map",
                color = MedievalCrimson,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MedievalSharpFontFamily
            )
            Text(
                text = "Reach: 15 spans/ward",
                color = AgedGoldDark,
                fontSize = 10.sp,
                fontFamily = AlmendraFontFamily
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Visual 2D Grid Canvas
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, AgedGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
            color = Color(0xFF1A1A1A) // Deep Night
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height
                val centerX = canvasW / 2.0f
                val centerY = canvasH / 2.0f
                val scale = 3.6f

                // Draw radar grid rings
                for (r in 1..4) {
                    drawCircle(
                        color = Color(0x1A00E5FF),
                        radius = r * 30.0f * (scale / 4f),
                        center = Offset(centerX, centerY),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                    )
                }

                // Collect points
                data class Pt(val x: Float, val z: Float, val isSource: Boolean, val isBat: Boolean, val isPole: Boolean, val label: String)
                val pts = mutableListOf<Pt>()
                pts.add(Pt(6f, 0f, isSource = false, isBat = true, isPole = false, label = "Farmhouse"))
                pts.add(Pt(0f, 14f, isSource = false, isBat = false, isPole = false, label = "Workshop"))

                energyNodes.forEach {
                    pts.add(Pt(it.posX, it.posZ, isSource = it.nodeType != EnergyNodeType.BATTERY_STORAGE_BANK, isBat = it.nodeType == EnergyNodeType.BATTERY_STORAGE_BANK, isPole = false, label = it.nodeType.displayName.take(5)))
                }

                placedBuildings.forEach { b ->
                    val isSource = b.buildingType in listOf(BuildableType.SOLAR_PANEL, BuildableType.WINDMILL, BuildableType.HYDRO_GENERATOR, BuildableType.ADVANCED_SOLAR, BuildableType.BIOGAS_GENERATOR, BuildableType.GEOTHERMAL_VENT)
                    val isBat = b.buildingType in listOf(BuildableType.BASIC_BATTERY, BuildableType.ADVANCED_BATTERY, BuildableType.BATTERY_BANK)
                    val isPole = b.buildingType == BuildableType.POWER_POLE
                    if (isSource || isBat || isPole || b.buildingType == BuildableType.WATER_PURIFIER || b.buildingType == BuildableType.GREENHOUSE) {
                        pts.add(Pt(b.posX, b.posZ, isSource, isBat, isPole, b.buildingType.displayName.take(5)))
                    }
                }

                // Draw glowing power lines between nodes within 15 units
                for (i in 0 until pts.size) {
                    for (j in i + 1 until pts.size) {
                        val p1 = pts[i]
                        val p2 = pts[j]
                        val dx = p2.x - p1.x
                        val dz = p2.z - p1.z
                        val dist = kotlin.math.sqrt(dx * dx + dz * dz)
                        if (dist <= 15.0f) {
                            val c1 = Offset(centerX + p1.x * scale, centerY + p1.z * scale)
                            val c2 = Offset(centerX + p2.x * scale, centerY + p2.z * scale)
                            drawLine(
                                color = AgedGold.copy(alpha = 0.65f),
                                start = c1,
                                end = c2,
                                strokeWidth = 2.0f
                            )
                        }
                    }
                }

                // Draw nodes
                pts.forEach { p ->
                    val c = Offset(centerX + p.x * scale, centerY + p.z * scale)
                    val nodeColor = if (p.isSource) AgedGold else if (p.isBat) MedievalCrimson else if (p.isPole) Bronze else ForestMoss
                    drawCircle(
                        color = nodeColor,
                        radius = if (p.isPole) 3.5f else 5.5f,
                        center = c
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MapLegendItem(color = AgedGold, label = "Mana Well")
            MapLegendItem(color = MedievalCrimson, label = "Mana Vessel")
            MapLegendItem(color = Bronze, label = "Mystic Ward")
            MapLegendItem(color = ForestMoss, label = "Sacred Grove")
        }
    }
}

@Composable
private fun MapLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = MedievalTextSecondary,
            fontSize = 9.sp,
            fontFamily = AlmendraFontFamily
        )
    }
}

@Composable
private fun ResearchSection(
    currentEcoRp: Int,
    unlockedTechs: Set<String>,
    onUnlock: (String, Int) -> Unit
) {
    val techs = listOf(
        EnergyResearchTech(
            id = "tech_smart_grid",
            title = "Ley Line Attunement",
            requiredRp = 200,
            description = "Master runic stabilization for zero-loss mana routing across the kingdom's conduits.",
            unlockEffect = "Enables automated priority switching & zero-loss grid routing",
            isUnlocked = unlockedTechs.contains("tech_smart_grid")
        ),
        EnergyResearchTech(
            id = "tech_adv_battery",
            title = "Great Mana Vessel",
            requiredRp = 100,
            description = "Construct massive reinforced stone basins infused with crystalline mana-reactive salts.",
            unlockEffect = "Unlocks Industrial Mana Vessel blueprint (500 Mana, 50 Rate)",
            isUnlocked = unlockedTechs.contains("tech_adv_battery")
        ),
        EnergyResearchTech(
            id = "tech_geothermal",
            title = "Deep Core Magma Tap",
            requiredRp = 250,
            description = "Harness the eternal heat of the earth's core for uninterrupted arcane power.",
            unlockEffect = "Unlocks Volcanic Power Forge (40 Mana/day continuous)",
            isUnlocked = unlockedTechs.contains("tech_geothermal")
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Alchemy & Arcane R&D",
                        color = MedievalCrimson,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MedievalSharpFontFamily
                    )
                    Text(
                        text = "Spend Prestige (RP) earned from sacred groves & beasts",
                        color = MedievalTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = AlmendraFontFamily
                    )
                }
                Surface(
                    color = ForestMoss.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ForestMoss)
                ) {
                    Text(
                        text = "RP: $currentEcoRp",
                        color = ForestMoss,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MedievalSharpFontFamily,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        items(techs) { tech ->
            val canAfford = currentEcoRp >= tech.requiredRp

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .testTag("research_${tech.id}"),
                color = Parchment,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (tech.isUnlocked) ForestMoss else if (canAfford) AgedGoldDark else AgedGold.copy(alpha = 0.2f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tech.title,
                            color = MedievalCrimson,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MedievalSharpFontFamily
                        )
                        if (tech.isUnlocked) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = ForestMoss, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "UNLOCKED",
                                    color = ForestMoss,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = MedievalSharpFontFamily
                                )
                            }
                        } else {
                            Text(
                                text = "${tech.requiredRp} RP",
                                color = if (canAfford) AgedGoldDark else MedievalTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = MedievalSharpFontFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tech.description,
                        color = MedievalTextPrimary,
                        fontSize = 10.sp,
                        fontFamily = AlmendraFontFamily
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "★ ${tech.unlockEffect}",
                        color = ForestMoss,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = AlmendraFontFamily
                    )

                    if (!tech.isUnlocked) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onUnlock(tech.id, tech.requiredRp) },
                            enabled = canAfford,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .testTag("btn_unlock_${tech.id}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canAfford) MedievalCrimson else AgedGold.copy(alpha = 0.2f),
                                contentColor = if (canAfford) Parchment else MedievalTextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = if (canAfford) "Transmute Knowledge" else "Need ${tech.requiredRp - currentEcoRp} More RP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = MedievalSharpFontFamily
                            )
                        }
                    }
                }
            }
        }
    }
}
