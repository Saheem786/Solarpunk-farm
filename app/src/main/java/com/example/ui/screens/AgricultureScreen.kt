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
import com.example.ui.game3d.CropPlantModal
import com.example.ui.components.MedievalProgressBar
import com.example.ui.theme.*

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
            .background(Color(0xFF1A0F0A)) // Deep Wood
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
                        text = "SACRED GROVES & ROYAL GARDENS",
                        color = AgedGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MedievalSharpFontFamily
                    )
                    Text(
                        text = "Regenerative earth management & ancient growth rituals",
                        color = MedievalTextSecondary,
                        fontSize = 11.sp
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
                    colors = ButtonDefaults.buttonColors(containerColor = MedievalCrimson),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("water_all_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = Parchment,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Bless All",
                        color = Parchment,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = MedievalSharpFontFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Navigation Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x33000000))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TabButton(
                    label = "Fertile Fields (${plots.count { it.cropType != null }}/${plots.size})",
                    isSelected = selectedTab == 0,
                    testTag = "tab_plots",
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    label = "Herbalist's Grimoire",
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
            .clip(RoundedCornerShape(12.dp))
            .testTag("plot_card_${plot.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Parchment),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = if (plot.stage == CropStage.HARVEST_READY) AgedGold else Bronze.copy(alpha = 0.5f)
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
                    color = MedievalCrimson,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MedievalSharpFontFamily
                )
                if (plot.cropType != null) {
                    val stageLabel = when (plot.stage) {
                        CropStage.SEEDLING -> "Seed"
                        CropStage.SPROUT -> "Sprout"
                        CropStage.VEGETATIVE, CropStage.FLOWERING -> "Growing"
                        CropStage.HARVEST_READY -> "Ready"
                        CropStage.WITHERED -> "Withered"
                        CropStage.EMPTY -> "Empty"
                    }
                    Text(
                        text = stageLabel,
                        color = if (plot.stage == CropStage.HARVEST_READY) MedievalCrimson else Bronze,
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
                            .background(Color(crop.primaryColor).copy(alpha = 0.1f)),
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
                            color = MedievalCrimson,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MedievalSharpFontFamily
                        )
                        Text(
                            text = "Yield: +${crop.harvestYield} • +${crop.energyBonusKwh} Mana",
                            color = MedievalTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Growth Progress
                Text(
                    text = "Growth: ${(plot.progress * 100).toInt()}%",
                    color = MedievalTextSecondary,
                    fontSize = 10.sp
                )
                MedievalProgressBar(
                    progress = plot.progress,
                    modifier = Modifier.fillMaxWidth().height(10.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Moisture Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Divine Blessing",
                        color = Bronze,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "${(plot.moisture * 100).toInt()}%",
                        color = Bronze,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                MedievalProgressBar(
                    progress = plot.moisture,
                    modifier = Modifier.fillMaxWidth().height(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action Button
                if (plot.stage == CropStage.HARVEST_READY) {
                    Button(
                        onClick = onHarvest,
                        colors = ButtonDefaults.buttonColors(containerColor = AgedGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .testTag("harvest_plot_${plot.id}")
                    ) {
                        Text(
                            text = "Reap Bounty",
                            color = MedievalCrimsonDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MedievalSharpFontFamily
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = onWater,
                            colors = ButtonDefaults.buttonColors(containerColor = MedievalCrimson.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .testTag("water_plot_${plot.id}")
                        ) {
                            Text(text = "Bless", fontSize = 10.sp, color = MedievalCrimson, fontWeight = FontWeight.Bold, fontFamily = MedievalSharpFontFamily)
                        }
                        Button(
                            onClick = onFertilize,
                            colors = ButtonDefaults.buttonColors(containerColor = MedievalCrimson.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .testTag("fertilize_plot_${plot.id}")
                        ) {
                            Text(text = "Enrich", fontSize = 10.sp, color = MedievalCrimson, fontWeight = FontWeight.Bold, fontFamily = MedievalSharpFontFamily)
                        }
                    }
                }
            } else {
                // Empty Plot Call-to-action
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x11000000))
                        .clickable { onPlant() }
                        .testTag("empty_plot_${plot.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MedievalCrimson,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sow Seeds",
                            color = MedievalCrimson,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MedievalSharpFontFamily
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Parchment),
        border = androidx.compose.foundation.BorderStroke(1.dp, AgedGold.copy(alpha = 0.3f))
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
                    imageVector = Icons.Default.LocalFlorist,
                    contentDescription = null,
                    tint = Parchment,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = crop.displayName,
                    color = MedievalCrimson,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MedievalSharpFontFamily
                )
                Text(
                    text = crop.description,
                    color = MedievalTextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "⏱ ${(crop.growthDurationSec).toInt()}s",
                        color = Bronze,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "✨ +${crop.energyBonusKwh} Mana",
                        color = MedievalCrimson,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "💰 ${crop.baseSellPrice} Coins",
                        color = Bronze,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Button(
                onClick = onBuySeeds,
                colors = ButtonDefaults.buttonColors(containerColor = MedievalCrimson),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("buy_5_seeds_${crop.name.lowercase()}")
            ) {
                Text(
                    text = "5x (${crop.seedCost * 5}🪙)",
                    color = Parchment,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MedievalSharpFontFamily
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
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) MedievalCrimson else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Parchment else MedievalTextSecondary,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontFamily = MedievalSharpFontFamily
        )
    }
}
