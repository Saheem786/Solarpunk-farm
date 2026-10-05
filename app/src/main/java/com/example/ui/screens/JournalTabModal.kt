package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terminal
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.LoreEntryData
import com.example.data.model.PointOfInterestData
import com.example.data.model.StoryMission
import com.example.ui.components.SolarpunkProgressBar
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SunGold

@Composable
fun JournalTabModal(
    missions: List<StoryMission>,
    pois: List<PointOfInterestData>,
    loreEntries: List<LoreEntryData>,
    unlockedBlueprints: List<String>,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Missions, 1: Discoveries, 2: Lore, 3: Blueprints

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, SunGold.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                .testTag("modal_journal_notebook"),
            color = Color(0xFD09181A)
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
                                .background(Brush.radialGradient(listOf(SunGold, SolarEmerald))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = "Journal",
                                tint = Color(0xFF091215),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SANCTUARY JOURNAL & FIELD LOGS",
                                color = SunGold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Missions, points of interest, SDZ history & unlocked blueprints",
                                color = Color(0xFF90A4AE),
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                            .testTag("btn_close_journal_modal")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sub-Tabs Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val tabs = listOf("Missions", "Discoveries (15)", "Lore & Logs", "Blueprints")
                    tabs.forEachIndexed { index, label ->
                        val isSelected = selectedTab == index
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 3.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedTab = index }
                                .testTag("btn_journal_subtab_$index"),
                            color = if (isSelected) SunGold else Color(0x221E3A3A),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) SolarEmerald else Color(0x4400E5FF)
                            )
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color(0xFF091215) else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Content View
                when (selectedTab) {
                    0 -> MissionsListView(missions)
                    1 -> DiscoveriesListView(pois)
                    2 -> LoreEntriesListView(loreEntries)
                    else -> BlueprintsListView(unlockedBlueprints)
                }
            }
        }
    }
}

@Composable
private fun MissionsListView(missions: List<StoryMission>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(missions, key = { it.id }) { mission ->
            val cardBorder = when {
                mission.isCompleted -> SolarEmerald
                mission.isUnlocked -> SunGold
                else -> Color(0xFF455A64)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mission_card_${mission.id}"),
                colors = CardDefaults.cardColors(containerColor = Color(0x1F1A2C2E)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, cardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (mission.isCompleted) Icons.Default.CheckCircle else if (mission.isUnlocked) Icons.Default.Flag else Icons.Default.Lock,
                                contentDescription = mission.title,
                                tint = if (mission.isCompleted) SolarEmerald else if (mission.isUnlocked) SunGold else Color(0xFF78909C),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Mission ${mission.missionNumber}: ${mission.title}",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x3300E5FF)
                        ) {
                            Text(
                                text = "+${mission.rpReward} RP",
                                color = SunGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = mission.description,
                        color = Color(0xFFCFD8DC),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Objective: ${mission.objectiveDescription} (${mission.currentProgress}/${mission.targetProgress})",
                        color = CleanCyan,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    SolarpunkProgressBar(
                        progress = (mission.currentProgress.toFloat() / mission.targetProgress.toFloat()).coerceIn(0f, 1f),
                        color = if (mission.isCompleted) SolarEmerald else SunGold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DiscoveriesListView(pois: List<PointOfInterestData>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(pois, key = { it.id }) { poi ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("poi_card_${poi.id}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (poi.isDiscovered) Color(0x1F00E676) else Color(0x1A263238)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (poi.isDiscovered) SolarEmerald else Color(0xFF455A64)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (poi.isDiscovered) Icons.Default.Explore else Icons.Default.Lock,
                            contentDescription = poi.name,
                            tint = if (poi.isDiscovered) CleanCyan else Color(0xFF78909C),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (poi.isDiscovered) poi.name else "??? Undiscovered Location",
                                color = if (poi.isDiscovered) Color.White else Color(0xFFB0BEC5),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (poi.isDiscovered) poi.description else "Explore ${poi.biome.displayName} to discover this point of interest.",
                                color = Color(0xFF90A4AE),
                                fontSize = 11.sp,
                                maxLines = 2
                            )
                        }
                    }

                    if (poi.isDiscovered) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SolarEmerald.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SolarEmerald)
                        ) {
                            Text(
                                text = "DISCOVERED",
                                color = SolarEmerald,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoreEntriesListView(entries: List<LoreEntryData>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(entries, key = { it.id }) { log ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lore_card_${log.id}"),
                colors = CardDefaults.cardColors(containerColor = Color(0x1F1A2326)),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (log.isUnlocked) SunGold else Color(0xFF37474F)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (log.category == "TERMINAL") Icons.Default.Terminal else Icons.Default.Description,
                                contentDescription = log.title,
                                tint = if (log.isUnlocked) SunGold else Color(0xFF78909C),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (log.isUnlocked) log.title else "Encrypted Lore Entry",
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = if (log.isUnlocked) "By ${log.author}" else "LOCKED",
                            color = CleanCyan,
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (log.isUnlocked) {
                        Text(
                            text = log.textContent,
                            color = Color(0xFFECEFF1),
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic
                        )
                    } else {
                        Text(
                            text = "Find journal entries & inspect terminal logs across Green Valley, Deep Forest, and Wetlands to decrypt this story archive.",
                            color = Color(0xFF78909C),
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BlueprintsListView(unlockedBlueprints: List<String>) {
    val formattedBlueprints = remember(unlockedBlueprints) {
        unlockedBlueprints.map { bp ->
            getBlueprintDetails(bp)
        }
    }

    if (formattedBlueprints.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No blueprints unlocked yet.\nUnlock technology nodes in the Research Tree or discover world POIs!",
                color = Color(0xFF90A4AE),
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(formattedBlueprints) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("blueprint_card_${item.id}"),
                    colors = CardDefaults.cardColors(containerColor = Color(0x1F00E5FF)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CleanCyan)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(CleanCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Construction,
                                contentDescription = item.title,
                                tint = CleanCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.description,
                                color = Color(0xFFB0BEC5),
                                fontSize = 11.5.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SolarEmerald.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SolarEmerald)
                        ) {
                            Text(
                                text = "UNLOCKED",
                                color = SolarEmerald,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class BlueprintDisplayItem(
    val id: String,
    val title: String,
    val description: String
)

private fun getBlueprintDetails(bpId: String): BlueprintDisplayItem = when (bpId) {
    "tech_water_purification" -> BlueprintDisplayItem(bpId, "Water Filter Building", "Filters raw river water into purified clean drinking water.")
    "tech_better_tools" -> BlueprintDisplayItem(bpId, "Iron Axe & Iron Pickaxe", "Reinforced iron tools for 2x faster timber & stone harvesting.")
    "tech_crop_rotation" -> BlueprintDisplayItem(bpId, "Crop Rotation Technique", "Nitrogen-fixing soil management boosting crop growth speed by +10%.")
    "tech_basic_cooking" -> BlueprintDisplayItem(bpId, "Electric Cooking Station", "Cooktop structure & 5 culinary recipes to restore health and hunger.")
    "tech_advanced_solar" -> BlueprintDisplayItem(bpId, "Advanced Solar Glass Array", "Dual-axis photovoltaic tracking panels generating 25 kWh/day clean energy.")
    "tech_irrigation_systems" -> BlueprintDisplayItem(bpId, "Irrigation Pipes & Nodes", "Subsurface drip irrigation for automated permaculture watering.")
    "tech_animal_breeding" -> BlueprintDisplayItem(bpId, "Livestock Breeding Sanctuary", "Nutritional breeding program allowing chickens and cows to reproduce every 5 days.")
    "tech_herbal_medicine" -> BlueprintDisplayItem(bpId, "Medical Station Infirmary", "Infirmary structure brewing herbal cures and passively healing settlement residents.")
    "tech_hydro_power" -> BlueprintDisplayItem(bpId, "Small Hydro Generator", "Micro-hydro river wheel generating 35 kWh/day baseload hydroelectricity.")
    "tech_electric_vehicles" -> BlueprintDisplayItem(bpId, "Electric Utility Cart", "High-torque e-vehicle for rapid transit across Green Valley & Wetlands.")
    "tech_automation" -> BlueprintDisplayItem(bpId, "Auto-Harvesters & Auto-Planters", "Automated cybernetic machinery for automatic seeding and harvesting.")
    "tech_advanced_greenhouse" -> BlueprintDisplayItem(bpId, "Climate-Controlled Bio-Dome", "Glass greenhouse structure doubling crop yield under climate control.")
    "tech_smart_grid" -> BlueprintDisplayItem(bpId, "Smart Grid Power Inverter", "Micro-inverter power balance system reducing line loss by 20%.")
    "tech_drone_network" -> BlueprintDisplayItem(bpId, "Scout Drone Aerial Tracker", "Autonomous drone hub conducting aerial biome mapping and wildlife tracking.")
    "tech_geothermal_power" -> BlueprintDisplayItem(bpId, "Geothermal Vent Generator", "Deep subterranean thermal vent tap producing 80 kWh/day zero-emission power.")
    "tech_vertical_farming" -> BlueprintDisplayItem(bpId, "Aeroponic Vertical Tower", "10x crop yield in compact footprint using closed-loop nutrient mists.")
    "tech_fusion_research" -> BlueprintDisplayItem(bpId, "Micro-Fusion Reactor", "Compact aneutronic fusion reactor generating 200 kWh/day endgame power.")
    "tech_new_dawn" -> BlueprintDisplayItem(bpId, "New Dawn Civilization Blueprint", "Master eco-city blueprint to rebuild civilization.")
    else -> BlueprintDisplayItem(bpId, bpId.replace("tech_", "").replace("_", " ").capitalize(), "Unlocked Solarpunk blueprint.")
}
