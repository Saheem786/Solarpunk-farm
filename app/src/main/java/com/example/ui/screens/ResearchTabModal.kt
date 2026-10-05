package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ResearchTreeNode
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SunGold

@Composable
fun ResearchTabModal(
    researchPoints: Int,
    unlockedTechs: Set<String>,
    onUnlockTech: (String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTier by remember { mutableStateOf(1) }
    var selectedNodeForDetails by remember { mutableStateOf<ResearchTreeNode?>(null) }

    val allTechNodes = remember(unlockedTechs) {
        getResearchTreeCatalog(unlockedTechs)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, CleanCyan.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                .testTag("modal_research_tree"),
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
                                .background(Brush.radialGradient(listOf(CleanCyan, SolarEmerald))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = "Research Tree",
                                tint = Color(0xFF091215),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "RESEARCH & TECHNOLOGY TREE",
                                color = CleanCyan,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Unlock closed-loop technology & eco-infrastructure",
                                color = Color(0xFF90A4AE),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // RP Counter Badge
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0x3300E5FF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CleanCyan)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Science,
                                    contentDescription = "RP",
                                    tint = CleanCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$researchPoints RP",
                                    color = SunGold,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0x33FFFFFF))
                                .testTag("btn_close_research_modal")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tier Selector Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (t in 1..5) {
                        val isSelected = selectedTier == t
                        val tierName = when (t) {
                            1 -> "Tier 1: Foundation"
                            2 -> "Tier 2: Intermediate"
                            3 -> "Tier 3: Advanced"
                            4 -> "Tier 4: Eco Civilization"
                            else -> "Tier 5: Mastery"
                        }
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedTier = t }
                                .testTag("btn_research_tier_$t"),
                            color = if (isSelected) CleanCyan else Color(0x221E3A3A),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) SunGold else Color(0x4400E5FF)
                            )
                        ) {
                            Text(
                                text = tierName,
                                color = if (isSelected) Color(0xFF091215) else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Active Tier Nodes List
                val nodesInTier = allTechNodes.filter { it.tier == selectedTier }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(nodesInTier, key = { it.id }) { node ->
                        ResearchNodeCard(
                            node = node,
                            userRp = researchPoints,
                            onSelectNode = { selectedNodeForDetails = node },
                            onUnlock = { onUnlockTech(node.id, node.rpCost) }
                        )
                    }
                }
            }
        }
    }

    // Node Details Dialog Modal
    if (selectedNodeForDetails != null) {
        val node = selectedNodeForDetails!!
        val canAfford = researchPoints >= node.rpCost
        Dialog(onDismissRequest = { selectedNodeForDetails = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(20.dp))
                    .border(2.dp, SunGold, RoundedCornerShape(20.dp)),
                color = Color(0xFD0B1B1E)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = node.title,
                        color = SunGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Cost: ${node.rpCost} Research Points",
                        color = CleanCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = node.description,
                        color = Color(0xFFCFD8DC),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Unlocks: ${node.unlockFeature}",
                        color = SolarEmerald,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { selectedNodeForDetails = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Close", color = Color.White)
                        }
                        if (!node.isUnlocked && node.isAvailable) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (canAfford) {
                                        onUnlockTech(node.id, node.rpCost)
                                        selectedNodeForDetails = null
                                    }
                                },
                                enabled = canAfford,
                                colors = ButtonDefaults.buttonColors(containerColor = SolarEmerald),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (canAfford) "Unlock Now" else "Need More RP",
                                    color = Color(0xFF091215),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResearchNodeCard(
    node: ResearchTreeNode,
    userRp: Int,
    onSelectNode: () -> Unit,
    onUnlock: () -> Unit
) {
    val canAfford = userRp >= node.rpCost
    val cardBorder = when {
        node.isUnlocked -> SolarEmerald
        node.isAvailable -> SunGold
        else -> Color(0xFF546E7A)
    }

    val cardBg = when {
        node.isUnlocked -> Color(0x1F00E676)
        node.isAvailable -> Color(0x1FFFFD54)
        else -> Color(0x1A263238)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onSelectNode() }
            .testTag("node_card_${node.id}"),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                node.isUnlocked -> SolarEmerald.copy(alpha = 0.3f)
                                node.isAvailable -> SunGold.copy(alpha = 0.3f)
                                else -> Color(0x3337474F)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (node.isUnlocked) Icons.Default.CheckCircle else if (node.isAvailable) Icons.Default.Science else Icons.Default.Lock,
                        contentDescription = node.title,
                        tint = when {
                            node.isUnlocked -> SolarEmerald
                            node.isAvailable -> SunGold
                            else -> Color(0xFF90A4AE)
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = node.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = node.description,
                        color = Color(0xFFB0BEC5),
                        fontSize = 11.sp,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Unlocks: ${node.unlockFeature}",
                        color = CleanCyan,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (node.isUnlocked) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SolarEmerald.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SolarEmerald)
                ) {
                    Text(
                        text = "UNLOCKED",
                        color = SolarEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            } else {
                Button(
                    onClick = onUnlock,
                    enabled = node.isAvailable && canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SunGold,
                        disabledContainerColor = Color(0xFF37474F)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${node.rpCost} RP",
                        color = if (node.isAvailable && canAfford) Color(0xFF091215) else Color(0xFFB0BEC5),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

fun getResearchTreeCatalog(unlockedTechs: Set<String>): List<ResearchTreeNode> {
    return listOf(
        // TIER 1
        ResearchTreeNode(
            id = "tech_water_purification",
            tier = 1,
            title = "Water Purification",
            rpCost = 10,
            description = "Construct sand & charcoal water filters to eliminate bacteria and raw water pathogens.",
            unlockFeature = "Water Filter Structure",
            isUnlocked = unlockedTechs.contains("tech_water_purification"),
            isAvailable = true
        ),
        ResearchTreeNode(
            id = "tech_better_tools",
            tier = 1,
            title = "Better Tools",
            rpCost = 15,
            description = "Forge reinforced iron axes and pickaxes for 2x faster resource gathering.",
            unlockFeature = "Iron Axe & Iron Pickaxe",
            isUnlocked = unlockedTechs.contains("tech_better_tools"),
            isAvailable = true
        ),
        ResearchTreeNode(
            id = "tech_crop_rotation",
            tier = 1,
            title = "Crop Rotation",
            rpCost = 20,
            description = "Optimize nitrogen fixing across permaculture plots. Crops grow +10% faster.",
            unlockFeature = "+10% Crop Growth Speed",
            isUnlocked = unlockedTechs.contains("tech_crop_rotation"),
            isAvailable = true
        ),
        ResearchTreeNode(
            id = "tech_basic_cooking",
            tier = 1,
            title = "Basic Cooking",
            rpCost = 15,
            description = "Craft electric cooktops and artisan culinary recipes.",
            unlockFeature = "Cooking Station & 5 Recipes",
            isUnlocked = unlockedTechs.contains("tech_basic_cooking"),
            isAvailable = true
        ),

        // TIER 2
        ResearchTreeNode(
            id = "tech_advanced_solar",
            tier = 2,
            title = "Advanced Solar",
            rpCost = 30,
            description = "Dual-axis tracking photovoltaic glass arrays generating 25 kWh/day.",
            unlockFeature = "Advanced Solar Array",
            isUnlocked = unlockedTechs.contains("tech_advanced_solar"),
            isAvailable = unlockedTechs.contains("tech_water_purification")
        ),
        ResearchTreeNode(
            id = "tech_irrigation_systems",
            tier = 2,
            title = "Irrigation Systems",
            rpCost = 35,
            description = "Lay subsurface drip irrigation pipes and automated water distribution nodes.",
            unlockFeature = "Irrigation Pipes & Nodes",
            isUnlocked = unlockedTechs.contains("tech_irrigation_systems"),
            isAvailable = unlockedTechs.contains("tech_crop_rotation")
        ),
        ResearchTreeNode(
            id = "tech_animal_breeding",
            tier = 2,
            title = "Animal Breeding",
            rpCost = 40,
            description = "Nutritional sanctuary breeding programs. Animals reproduce every 5 days.",
            unlockFeature = "Livestock Breeding Program",
            isUnlocked = unlockedTechs.contains("tech_animal_breeding"),
            isAvailable = unlockedTechs.contains("tech_basic_cooking")
        ),
        ResearchTreeNode(
            id = "tech_herbal_medicine",
            tier = 2,
            title = "Herbal Medicine",
            rpCost = 30,
            description = "Brew botanical teas and establish an infirmary medical station.",
            unlockFeature = "Medical Station Structure",
            isUnlocked = unlockedTechs.contains("tech_herbal_medicine"),
            isAvailable = unlockedTechs.contains("tech_water_purification")
        ),

        // TIER 3
        ResearchTreeNode(
            id = "tech_hydro_power",
            tier = 3,
            title = "Hydro Power",
            rpCost = 60,
            description = "Harvest kinetic stream energy with micro-hydro water wheels generating 35 kWh/day.",
            unlockFeature = "Small Hydro Generator",
            isUnlocked = unlockedTechs.contains("tech_hydro_power"),
            isAvailable = unlockedTechs.contains("tech_advanced_solar")
        ),
        ResearchTreeNode(
            id = "tech_electric_vehicles",
            tier = 3,
            title = "Electric Vehicles",
            rpCost = 80,
            description = "Assemble electric utility carts and cargo e-bikes for fast transit.",
            unlockFeature = "Electric Utility Cart",
            isUnlocked = unlockedTechs.contains("tech_electric_vehicles"),
            isAvailable = unlockedTechs.contains("tech_advanced_solar")
        ),
        ResearchTreeNode(
            id = "tech_automation",
            tier = 3,
            title = "Automation",
            rpCost = 100,
            description = "Automate crop seeding and harvesting through cybernetic farm machinery.",
            unlockFeature = "Auto-Harvesters & Auto-Planters",
            isUnlocked = unlockedTechs.contains("tech_automation"),
            isAvailable = unlockedTechs.contains("tech_irrigation_systems")
        ),
        ResearchTreeNode(
            id = "tech_advanced_greenhouse",
            tier = 3,
            title = "Advanced Greenhouse",
            rpCost = 70,
            description = "Climate-controlled glass bio-dome boosting crop yield by 2x.",
            unlockFeature = "Climate Greenhouse",
            isUnlocked = unlockedTechs.contains("tech_advanced_greenhouse"),
            isAvailable = unlockedTechs.contains("tech_irrigation_systems")
        ),

        // TIER 4
        ResearchTreeNode(
            id = "tech_smart_grid",
            tier = 4,
            title = "Smart Grid",
            rpCost = 150,
            description = "Micro-inverter power balancing reduces line loss and energy waste by 20%.",
            unlockFeature = "Smart Power Management (-20% Loss)",
            isUnlocked = unlockedTechs.contains("tech_smart_grid"),
            isAvailable = unlockedTechs.contains("tech_hydro_power")
        ),
        ResearchTreeNode(
            id = "tech_drone_network",
            tier = 4,
            title = "Drone Network",
            rpCost = 200,
            description = "Autonomous scout drones conduct aerial mapping and wildlife tracking.",
            unlockFeature = "Scout Drone Aerial Tracking",
            isUnlocked = unlockedTechs.contains("tech_drone_network"),
            isAvailable = unlockedTechs.contains("tech_automation")
        ),
        ResearchTreeNode(
            id = "tech_full_automation",
            tier = 4,
            title = "Full Automation",
            rpCost = 300,
            description = "Closed-loop farm systems allow NPCs to work 20% faster.",
            unlockFeature = "+20% NPC Work Efficiency",
            isUnlocked = unlockedTechs.contains("tech_full_automation"),
            isAvailable = unlockedTechs.contains("tech_automation")
        ),
        ResearchTreeNode(
            id = "tech_ecological_mastery",
            tier = 4,
            title = "Ecological Mastery",
            rpCost = 400,
            description = "Regenerative soil microbial inoculants double ecosystem health recovery speed.",
            unlockFeature = "2x Ecosystem Health Recovery",
            isUnlocked = unlockedTechs.contains("tech_ecological_mastery"),
            isAvailable = unlockedTechs.contains("tech_advanced_greenhouse")
        ),

        // TIER 5
        ResearchTreeNode(
            id = "tech_geothermal_power",
            tier = 5,
            title = "Geothermal Power",
            rpCost = 250,
            description = "Tap deep subterranean thermal vents for constant 80 kWh/day zero-emission baseload power.",
            unlockFeature = "Geothermal Power Vent Structure",
            isUnlocked = unlockedTechs.contains("tech_geothermal_power"),
            isAvailable = unlockedTechs.contains("tech_smart_grid")
        ),
        ResearchTreeNode(
            id = "tech_vertical_farming",
            tier = 5,
            title = "Vertical Farming",
            rpCost = 300,
            description = "Aeroponic vertical nutrient towers yielding 10x crop output in compact footprints.",
            unlockFeature = "Vertical Aeroponic Tower",
            isUnlocked = unlockedTechs.contains("tech_vertical_farming"),
            isAvailable = unlockedTechs.contains("tech_full_automation")
        ),
        ResearchTreeNode(
            id = "tech_fusion_research",
            tier = 5,
            title = "Fusion Research",
            rpCost = 500,
            description = "Compact aneutronic micro-fusion reactor producing 200 kWh/day unlimited power.",
            unlockFeature = "Endgame Micro-Fusion Reactor",
            isUnlocked = unlockedTechs.contains("tech_fusion_research"),
            isAvailable = unlockedTechs.contains("tech_smart_grid")
        ),
        ResearchTreeNode(
            id = "tech_new_dawn",
            tier = 5,
            title = "New Dawn",
            rpCost = 600,
            description = "Rebuild human eco-civilization and trigger the cinematic story ending.",
            unlockFeature = "Civilization Rebuilt (Ending Sequence)",
            isUnlocked = unlockedTechs.contains("tech_new_dawn"),
            isAvailable = unlockedTechs.contains("tech_ecological_mastery") && unlockedTechs.contains("tech_fusion_research")
        )
    )
}
