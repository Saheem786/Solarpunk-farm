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
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.AutoAwesome
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
import com.example.ui.components.MedievalProgressBar
import com.example.ui.theme.*

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
                .clip(RoundedCornerShape(12.dp))
                .border(3.dp, AgedGold, RoundedCornerShape(12.dp))
                .testTag("modal_journal_notebook"),
            color = Parchment
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
                                .background(Brush.radialGradient(listOf(MedievalCrimson, MedievalCrimsonDark))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = "Chronicle",
                                tint = Parchment,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "KINGDOM CHRONICLES & LEGENDS",
                                color = MedievalCrimson,
                                fontFamily = MedievalSharpFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Tasks, secret lands, ancient history, and forgotten designs",
                                color = MedievalTextSecondary,
                                fontFamily = AlmendraFontFamily,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MedievalCrimson.copy(alpha = 0.15f))
                            .testTag("btn_close_journal_modal")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MedievalCrimson
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sub-Tabs Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val tabs = listOf("Quests", "Lands", "Legends", "Designs")
                    tabs.forEachIndexed { index, label ->
                        val isSelected = selectedTab == index
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedTab = index }
                                .testTag("btn_journal_subtab_$index"),
                            color = if (isSelected) MedievalCrimson else AgedGold.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) AgedGold else AgedGold.copy(alpha = 0.4f)
                            )
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Parchment else MedievalTextPrimary,
                                    fontFamily = MedievalSharpFontFamily,
                                    fontSize = 11.sp,
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
                mission.isCompleted -> ForestMoss
                mission.isUnlocked -> AgedGold
                else -> IronGrey.copy(alpha = 0.5f)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mission_card_${mission.id}"),
                colors = CardDefaults.cardColors(containerColor = Color(0x0A2D1B0D)),
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
                                tint = if (mission.isCompleted) ForestMoss else if (mission.isUnlocked) AgedGoldDark else IronGrey,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Quest ${mission.missionNumber}: ${mission.title}",
                                color = MedievalCrimson,
                                fontFamily = MedievalSharpFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AgedGold.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "+${mission.rpReward} Essence",
                                color = MedievalCrimson,
                                fontFamily = MedievalSharpFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = mission.description,
                        color = MedievalTextPrimary,
                        fontFamily = AlmendraFontFamily,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Objective: ${mission.objectiveDescription} (${mission.currentProgress}/${mission.targetProgress})",
                        color = MedievalCrimsonDark,
                        fontFamily = MedievalSharpFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    MedievalProgressBar(
                        progress = (mission.currentProgress.toFloat() / mission.targetProgress.toFloat()).coerceIn(0f, 1f),
                        color = if (mission.isCompleted) ForestMoss else MedievalCrimson,
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
                    containerColor = if (poi.isDiscovered) ForestMoss.copy(alpha = 0.08f) else Color(0x052D1B0D)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (poi.isDiscovered) ForestMoss else IronGrey.copy(alpha = 0.4f)
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
                            tint = if (poi.isDiscovered) MedievalCrimson else IronGrey,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (poi.isDiscovered) poi.name else "??? Hidden Realm",
                                color = if (poi.isDiscovered) MedievalCrimson else MedievalTextSecondary,
                                fontFamily = MedievalSharpFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (poi.isDiscovered) poi.description else "Venture into ${poi.biome.displayName} to reveal this location.",
                                color = MedievalTextPrimary.copy(alpha = 0.8f),
                                fontFamily = AlmendraFontFamily,
                                fontSize = 11.sp,
                                maxLines = 2
                            )
                        }
                    }

                    if (poi.isDiscovered) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ForestMoss.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ForestMoss)
                        ) {
                            Text(
                                text = "REVEALED",
                                color = ForestMoss,
                                fontFamily = MedievalSharpFontFamily,
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
                colors = CardDefaults.cardColors(containerColor = Color(0x0A2D1B0D)),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (log.isUnlocked) AgedGold else IronGrey.copy(alpha = 0.3f)
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
                                imageVector = if (log.category == "TERMINAL") Icons.Default.AutoFixHigh else Icons.Default.Description,
                                contentDescription = log.title,
                                tint = if (log.isUnlocked) MedievalCrimson else IronGrey,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (log.isUnlocked) log.title else "Forbidden Manuscript",
                                color = MedievalCrimson,
                                fontFamily = MedievalSharpFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = if (log.isUnlocked) "By ${log.author}" else "SEALED",
                            color = MedievalTextSecondary,
                            fontFamily = AlmendraFontFamily,
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (log.isUnlocked) {
                        Text(
                            text = log.textContent,
                            color = MedievalTextPrimary,
                            fontFamily = AlmendraFontFamily,
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic
                        )
                    } else {
                        Text(
                            text = "Find ancient scrolls and decypher stone inscriptions across the realms to unlock this forbidden knowledge.",
                            color = MedievalTextSecondary,
                            fontFamily = AlmendraFontFamily,
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
                text = "No designs found yet.\nMaster the alchemy tree or discover lost ruins to find forgotten plans!",
                color = MedievalTextSecondary,
                fontFamily = AlmendraFontFamily,
                fontSize = 14.sp,
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
                    colors = CardDefaults.cardColors(containerColor = AgedGold.copy(alpha = 0.05f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AgedGold.copy(alpha = 0.6f))
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
                                .background(AgedGold.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Construction,
                                contentDescription = item.title,
                                tint = AgedGoldDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                color = MedievalCrimson,
                                fontFamily = MedievalSharpFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.description,
                                color = MedievalTextPrimary.copy(alpha = 0.8f),
                                fontFamily = AlmendraFontFamily,
                                fontSize = 11.5.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ForestMoss.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ForestMoss)
                        ) {
                            Text(
                                text = "DESIGNED",
                                color = ForestMoss,
                                fontFamily = MedievalSharpFontFamily,
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
    "tech_water_purification" -> BlueprintDisplayItem(bpId, "Holy Water Filter", "Filters raw river water into purified sacred drinking water.")
    "tech_better_tools" -> BlueprintDisplayItem(bpId, "Steel Axe & Steel Pickaxe", "Reinforced steel tools for 2x faster timber & stone harvesting.")
    "tech_crop_rotation" -> BlueprintDisplayItem(bpId, "Druidic Permaculture", "Moon-blessed soil management boosting crop growth speed by +10%.")
    "tech_basic_cooking" -> BlueprintDisplayItem(bpId, "Grand Stone Hearth", "Stone hearth structure & 5 legendary recipes to restore health and hunger.")
    "tech_advanced_solar" -> BlueprintDisplayItem(bpId, "Ancient Mana Circle", "Stone circles that gather mana from ley lines generating 25 Mana/day.")
    "tech_irrigation_systems" -> BlueprintDisplayItem(bpId, "Stone Aqueducts", "Carved stone waterways for automated field irrigation.")
    "tech_animal_breeding" -> BlueprintDisplayItem(bpId, "Noble Breeding Sanctuary", "Sacred breeding program allowing chickens and cows to reproduce every 5 days.")
    "tech_herbal_medicine" -> BlueprintDisplayItem(bpId, "Healer's Sanctum", "Apothecary structure brewing herbal cures and passively healing the realm's citizens.")
    "tech_hydro_power" -> BlueprintDisplayItem(bpId, "Great Water Wheel", "Massive oak water wheel generating 35 Mana/day from the island's streams.")
    "tech_electric_vehicles" -> BlueprintDisplayItem(bpId, "Royal Carriage", "High-speed noble carriage for rapid transit across the kingdom.")
    "tech_automation" -> BlueprintDisplayItem(bpId, "Clockwork Golems", "Runically animated golems for automatic seeding and harvesting.")
    "tech_advanced_greenhouse" -> BlueprintDisplayItem(bpId, "Crystal Conservatory", "Prismatic glasshouse doubling crop yield through captured light.")
    "tech_smart_grid" -> BlueprintDisplayItem(bpId, "Ley Line Stabilizer", "Runic mana-balancing system reducing energy leakage by 20%.")
    "tech_drone_network" -> BlueprintDisplayItem(bpId, "Royal Falcon Aviary", "Network of trained falcons conducting aerial biome mapping and tracking.")
    "tech_geothermal_power" -> BlueprintDisplayItem(bpId, "Volcanic Power Forge", "Tap the earth's fiery core producing 80 Mana/day eternal power.")
    "tech_vertical_farming" -> BlueprintDisplayItem(bpId, "Grand Hanging Gardens", "10x crop yield in compact footprint using divine tiered irrigation.")
    "tech_fusion_research" -> BlueprintDisplayItem(bpId, "Aetheric Reactor", "Bound shard of pure Aether generating 200 Mana/day endgame power.")
    "tech_new_dawn" -> BlueprintDisplayItem(bpId, "Eternal Kingdom Blueprint", "The master blueprint to crown your eternal kingdom.")
    else -> BlueprintDisplayItem(bpId, bpId.replace("tech_", "").replace("_", " ").capitalize(), "Unlocked Medieval blueprint.")
}
