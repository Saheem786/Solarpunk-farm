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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.PlotEntity
import com.example.data.model.CropStage
import com.example.data.model.CropType
import com.example.ui.FarmViewModel
import com.example.ui.components.SolarpunkProgressBar
import com.example.ui.game3d.CropPlantModal
import com.example.ui.theme.BrassGold
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SunGold

@Composable
fun AgricultureScreen(
    viewModel: FarmViewModel,
    modifier: Modifier = Modifier
) {
    val plots by viewModel.plots.collectAsStateWithLifecycle()
    var selectedPlotForPlant by remember { mutableStateOf<PlotEntity?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Plots Grid, 1 = Crop Catalog

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF091416))
            .testTag("agriculture_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Permaculture & Hydroponics",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Regenerative soil management & vertical growth columns",
                        color = Color(0xFF80CBC4),
                        fontSize = 12.sp
                    )
                }

                // Hydrate All Button
                Button(
                    onClick = {
                        plots.forEach { plot ->
                            if (plot.cropType != null && plot.moisture < 0.8f) {
                                viewModel.waterPlot(plot.id)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CleanCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("water_all_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = Color(0xFF091215),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Hydrate All",
                        color = Color(0xFF091215),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Navigation Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF112326))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TabButton(
                    label = "Active Plots (${plots.count { it.cropType != null }}/${plots.size})",
                    isSelected = selectedTab == 0,
                    testTag = "tab_plots",
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    label = "Crop Encyclopedia",
                    isSelected = selectedTab == 1,
                    testTag = "tab_crops",
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTab == 0) {
                // Active Plots Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(plots) { plot ->
                        PlotCard(
                            plot = plot,
                            onWater = { viewModel.waterPlot(plot.id) },
                            onFertilize = { viewModel.fertilizePlot(plot.id) },
                            onHarvest = { viewModel.harvestPlot(plot.id) },
                            onPlant = { selectedPlotForPlant = plot }
                        )
                    }
                }
            } else {
                // Crop Encyclopedia Catalog
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(CropType.values()) { crop ->
                        CropCatalogCard(
                            crop = crop,
                            onBuySeeds = { viewModel.buySeed(crop, 5) }
                        )
                    }
                }
            }
        }

        // Crop Planting Modal
        if (selectedPlotForPlant != null) {
            CropPlantModal(
                plot = selectedPlotForPlant,
                onPlantCrop = { crop ->
                    selectedPlotForPlant?.let { viewModel.plantCrop(it.id, crop) }
                    selectedPlotForPlant = null
                },
                onDismiss = { selectedPlotForPlant = null }
            )
        }
    }
}

@Composable
private fun PlotCard(
    plot: PlotEntity,
    onWater: () -> Unit,
    onFertilize: () -> Unit,
    onHarvest: () -> Unit,
    onPlant: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .testTag("plot_card_${plot.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13292C)),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (plot.stage == CropStage.HARVEST_READY) SunGold else Color(0x3300E676)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${plot.plotType.displayName} #${plot.id + 1}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                if (plot.cropType != null) {
                    Text(
                        text = plot.stage.name,
                        color = if (plot.stage == CropStage.HARVEST_READY) SunGold else SolarEmerald,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (plot.cropType != null) {
                val crop = plot.cropType
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(crop.primaryColor).copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFlorist,
                            contentDescription = null,
                            tint = Color(crop.primaryColor),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = crop.displayName,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Yield: +${crop.harvestYield} • +${crop.energyBonusKwh} kWh",
                            color = Color(0xFFB0BEC5),
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Growth Progress
                Text(
                    text = "Growth: ${(plot.progress * 100).toInt()}%",
                    color = Color(0xFFB0BEC5),
                    fontSize = 10.sp
                )
                SolarpunkProgressBar(
                    progress = plot.progress,
                    color = if (plot.stage == CropStage.HARVEST_READY) SunGold else SolarEmerald,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Moisture Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Moisture",
                        color = Color(0xFF80D8FF),
                        fontSize = 10.sp
                    )
                    Text(
                        text = "${(plot.moisture * 100).toInt()}%",
                        color = Color(0xFF80D8FF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                SolarpunkProgressBar(
                    progress = plot.moisture,
                    color = CleanCyan,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action Button
                if (plot.stage == CropStage.HARVEST_READY) {
                    Button(
                        onClick = onHarvest,
                        colors = ButtonDefaults.buttonColors(containerColor = SunGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .testTag("harvest_plot_${plot.id}")
                    ) {
                        Text(
                            text = "Harvest Crop",
                            color = Color(0xFF091215),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = onWater,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .testTag("water_plot_${plot.id}")
                        ) {
                            Text(text = "Water", fontSize = 10.sp, color = CleanCyan)
                        }
                        OutlinedButton(
                            onClick = onFertilize,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .testTag("fertilize_plot_${plot.id}")
                        ) {
                            Text(text = "Compost", fontSize = 10.sp, color = SolarEmerald)
                        }
                    }
                }
            } else {
                // Empty Plot Call-to-action
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0D1D1F))
                        .clickable { onPlant() }
                        .testTag("empty_plot_${plot.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = SolarEmerald,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sow Seeds",
                            color = SolarEmerald,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CropCatalogCard(
    crop: CropType,
    onBuySeeds: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("crop_catalog_${crop.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF12282B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3380CBC4))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(crop.primaryColor), Color(crop.secondaryColor))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = null,
                    tint = Color(0xFF091215),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = crop.displayName,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = crop.description,
                    color = Color(0xFFB0BEC5),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "⏱ ${(crop.growthDurationSec).toInt()}s",
                        color = SunGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "⚡ +${crop.energyBonusKwh} kWh",
                        color = CleanCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "💰 ${crop.baseSellPrice} Coins",
                        color = SolarEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Button(
                onClick = onBuySeeds,
                colors = ButtonDefaults.buttonColors(containerColor = SolarEmerald),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("buy_5_seeds_${crop.name.lowercase()}")
            ) {
                Text(
                    text = "5x (${crop.seedCost * 5}🪙)",
                    color = Color(0xFF091215),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TabButton(
    label: String,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) SolarEmerald else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color(0xFF091215) else Color(0xFFB0BEC5),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
