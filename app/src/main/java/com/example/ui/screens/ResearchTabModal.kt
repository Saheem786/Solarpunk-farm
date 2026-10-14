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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
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
import com.example.ui.theme.*

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
                .clip(RoundedCornerShape(12.dp))
                .border(3.dp, AgedGold, RoundedCornerShape(12.dp))
                .testTag("modal_research_tree"),
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
                                imageVector = Icons.Default.Science,
                                contentDescription = "Alchemy",
                                tint = Parchment,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ALCHEMY & ANCIENT WISDOM",
                                color = MedievalCrimson,
                                fontFamily = MedievalSharpFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Master the secrets of the elements and ancient crafts",
                                color = MedievalTextSecondary,
                                fontFamily = AlmendraFontFamily,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // RP Counter Badge
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0x33CD7F32), // Bronze-ish
                            border = androidx.compose.foundation.BorderStroke(1.dp, AgedGold)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Science,
                                    contentDescription = "Essence",
                                    tint = MedievalCrimson,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$researchPoints Essence",
                                    color = MedievalCrimson,
                                    fontFamily = MedievalSharpFontFamily,
                                    fontSize = 13.sp,
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
                                .background(MedievalCrimson.copy(alpha = 0.15f))
                                .testTag("btn_close_research_modal")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MedievalCrimson
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
                            1 -> "I: Basic Crafts"
                            2 -> "II: Advanced Alchemy"
                            3 -> "III: Masterwork"
                            4 -> "IV: Grand Enchanter"
                            else -> "V: Ancient Legacy"
                        }
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedTier = t }
                                .testTag("btn_research_tier_$t"),
                            color = if (isSelected) MedievalCrimson else Color(0x222D1B0D),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) AgedGold else AgedGold.copy(alpha = 0.4f)
                            )
                        ) {
                            Text(
                                text = tierName,
                                color = if (isSelected) Parchment else MedievalTextPrimary,
                                fontFamily = MedievalSharpFontFamily,
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
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, AgedGold, RoundedCornerShape(12.dp)),
                color = Parchment
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = node.title,
                        color = MedievalCrimson,
                        fontFamily = MedievalSharpFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Essence Required: ${node.rpCost}",
                        color = MedievalTextSecondary,
                        fontFamily = AlmendraFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = node.description,
                        color = MedievalTextPrimary,
                        fontFamily = AlmendraFontFamily,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Unlocks: ${node.unlockFeature}",
                        color = ForestMoss,
                        fontFamily = AlmendraFontFamily,
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
                            colors = ButtonDefaults.buttonColors(containerColor = IronGrey),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Dismiss", color = Color.White, fontFamily = MedievalSharpFontFamily)
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
                                colors = ButtonDefaults.buttonColors(containerColor = MedievalCrimson),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (canAfford) "Master This Wisdom" else "Insufficient Essence",
                                    color = Parchment,
                                    fontFamily = MedievalSharpFontFamily,
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
        node.isUnlocked -> ForestMoss
        node.isAvailable -> AgedGold
        else -> IronGrey.copy(alpha = 0.5f)
    }

    val cardBg = when {
        node.isUnlocked -> ForestMoss.copy(alpha = 0.08f)
        node.isAvailable -> Color(0x0A2D1B0D)
        else -> Color(0x052D1B0D)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
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
                                node.isUnlocked -> ForestMoss.copy(alpha = 0.2f)
                                node.isAvailable -> AgedGold.copy(alpha = 0.2f)
                                else -> IronGrey.copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (node.isUnlocked) Icons.Default.CheckCircle else if (node.isAvailable) Icons.Default.Science else Icons.Default.Lock,
                        contentDescription = node.title,
                        tint = when {
                            node.isUnlocked -> ForestMoss
                            node.isAvailable -> AgedGoldDark
                            else -> IronGrey
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = node.title,
                        color = MedievalCrimson,
                        fontFamily = MedievalSharpFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = node.description,
                        color = MedievalTextPrimary.copy(alpha = 0.8f),
                        fontFamily = AlmendraFontFamily,
                        fontSize = 12.sp,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Unlocks: ${node.unlockFeature}",
                        color = MedievalCrimsonLight,
                        fontFamily = AlmendraFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (node.isUnlocked) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ForestMoss.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ForestMoss)
                ) {
                    Text(
                        text = "MASTERED",
                        color = ForestMoss,
                        fontFamily = MedievalSharpFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                Button(
                    onClick = onUnlock,
                    enabled = node.isAvailable && canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MedievalCrimson,
                        disabledContainerColor = IronGrey.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${node.rpCost}",
                        color = if (node.isAvailable && canAfford) Parchment else MedievalTextPrimary.copy(alpha = 0.5f),
                        fontFamily = MedievalSharpFontFamily,
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
            title = "Sacred Water Filter",
            rpCost = 10,
            description = "Construct sand & charcoal filters blessed with ancient salts to purify the island's springs.",
            unlockFeature = "Holy Water Filter",
            isUnlocked = unlockedTechs.contains("tech_water_purification"),
            isAvailable = true
        ),
        ResearchTreeNode(
            id = "tech_better_tools",
            tier = 1,
            title = "Mastercraft Forging",
            rpCost = 15,
            description = "Forge reinforced steel axes and pickaxes for 2x faster resource gathering.",
            unlockFeature = "Steel Axe & Steel Pickaxe",
            isUnlocked = unlockedTechs.contains("tech_better_tools"),
            isAvailable = true
        ),
        ResearchTreeNode(
            id = "tech_crop_rotation",
            tier = 1,
            title = "Druidic Permaculture",
            rpCost = 20,
            description = "Observe lunar cycles across your fields. Crops grow +10% faster through moon-blessed soil.",
            unlockFeature = "+10% Crop Growth Speed",
            isUnlocked = unlockedTechs.contains("tech_crop_rotation"),
            isAvailable = true
        ),
        ResearchTreeNode(
            id = "tech_basic_cooking",
            tier = 1,
            title = "Artisan Hearth",
            rpCost = 15,
            description = "Build grand stone ovens and discover legendary medieval culinary recipes.",
            unlockFeature = "Grand Hearth & 5 Recipes",
            isUnlocked = unlockedTechs.contains("tech_basic_cooking"),
            isAvailable = true
        ),

        // TIER 2
        ResearchTreeNode(
            id = "tech_advanced_solar",
            tier = 2,
            title = "Mystic Mana Circles",
            rpCost = 30,
            description = "Construct ancient stone circles that gather mana from the ley lines, generating 25 Mana/day.",
            unlockFeature = "Ancient Mana Circle",
            isUnlocked = unlockedTechs.contains("tech_advanced_solar"),
            isAvailable = unlockedTechs.contains("tech_water_purification")
        ),
        ResearchTreeNode(
            id = "tech_irrigation_systems",
            tier = 2,
            title = "Aqueduct Engineering",
            rpCost = 35,
            description = "Carve stone aqueducts and clay pipes for automated water distribution across your realm.",
            unlockFeature = "Stone Aqueducts",
            isUnlocked = unlockedTechs.contains("tech_irrigation_systems"),
            isAvailable = unlockedTechs.contains("tech_crop_rotation")
        ),
        ResearchTreeNode(
            id = "tech_animal_breeding",
            tier = 2,
            title = "Beast Mastery",
            rpCost = 40,
            description = "Establish noble breeding lineages for your livestock. Animals reproduce every 5 days.",
            unlockFeature = "Noble Breeding Program",
            isUnlocked = unlockedTechs.contains("tech_animal_breeding"),
            isAvailable = unlockedTechs.contains("tech_basic_cooking")
        ),
        ResearchTreeNode(
            id = "tech_herbal_medicine",
            tier = 2,
            title = "Apothecary Arts",
            rpCost = 30,
            description = "Brew botanical elixirs and establish a healer's sanctum for your people.",
            unlockFeature = "Healer's Sanctum",
            isUnlocked = unlockedTechs.contains("tech_herbal_medicine"),
            isAvailable = unlockedTechs.contains("tech_water_purification")
        ),

        // TIER 3
        ResearchTreeNode(
            id = "tech_hydro_power",
            tier = 3,
            title = "Grand Water Wheels",
            rpCost = 60,
            description = "Harvest the spirit of the streams with massive oak water wheels generating 35 Mana/day.",
            unlockFeature = "Great Water Wheel",
            isUnlocked = unlockedTechs.contains("tech_hydro_power"),
            isAvailable = unlockedTechs.contains("tech_advanced_solar")
        ),
        ResearchTreeNode(
            id = "tech_electric_vehicles",
            tier = 3,
            title = "Noble Steeds & Carriages",
            rpCost = 80,
            description = "Breed faster warhorses and craft sturdy royal carriages for rapid island transit.",
            unlockFeature = "Royal Carriage",
            isUnlocked = unlockedTechs.contains("tech_electric_vehicles"),
            isAvailable = unlockedTechs.contains("tech_advanced_solar")
        ),
        ResearchTreeNode(
            id = "tech_automation",
            tier = 3,
            title = "Golem Labor",
            rpCost = 100,
            description = "Animate clay golems to automate crop seeding and harvesting through ancient runes.",
            unlockFeature = "Seeding & Harvesting Golems",
            isUnlocked = unlockedTechs.contains("tech_automation"),
            isAvailable = unlockedTechs.contains("tech_irrigation_systems")
        ),
        ResearchTreeNode(
            id = "tech_advanced_greenhouse",
            tier = 3,
            title = "Crystal Conservatory",
            rpCost = 70,
            description = "Enchanted glass glasshouse using captured sunbeams to boost crop yield by 2x.",
            unlockFeature = "Crystal Conservatory",
            isUnlocked = unlockedTechs.contains("tech_advanced_greenhouse"),
            isAvailable = unlockedTechs.contains("tech_irrigation_systems")
        ),

        // TIER 4
        ResearchTreeNode(
            id = "tech_smart_grid",
            tier = 4,
            title = "Ley Line Resonance",
            rpCost = 150,
            description = "Harmonize the island's mana flow to reduce energy leakage by 20% through runic stabilization.",
            unlockFeature = "Ley Line Stabilization (-20% Loss)",
            isUnlocked = unlockedTechs.contains("tech_smart_grid"),
            isAvailable = unlockedTechs.contains("tech_hydro_power")
        ),
        ResearchTreeNode(
            id = "tech_drone_network",
            tier = 4,
            title = "Falconer's Watch",
            rpCost = 200,
            description = "Train a network of swift falcons to conduct aerial mapping and track island wildlife.",
            unlockFeature = "Royal Falcon Mapping",
            isUnlocked = unlockedTechs.contains("tech_drone_network"),
            isAvailable = unlockedTechs.contains("tech_automation")
        ),
        ResearchTreeNode(
            id = "tech_full_automation",
            tier = 4,
            title = "Clockwork Perfection",
            rpCost = 300,
            description = "Master intricate clockwork systems, allowing your islanders to work 20% faster.",
            unlockFeature = "+20% Islander Efficiency",
            isUnlocked = unlockedTechs.contains("tech_full_automation"),
            isAvailable = unlockedTechs.contains("tech_automation")
        ),
        ResearchTreeNode(
            id = "tech_ecological_mastery",
            tier = 4,
            title = "Realm Sovereignty",
            rpCost = 400,
            description = "Perform grand rituals of restoration to double the island's natural health recovery speed.",
            unlockFeature = "2x Realm Health Recovery",
            isUnlocked = unlockedTechs.contains("tech_ecological_mastery"),
            isAvailable = unlockedTechs.contains("tech_advanced_greenhouse")
        ),

        // TIER 5
        ResearchTreeNode(
            id = "tech_geothermal_power",
            tier = 5,
            title = "Forge of the Core",
            rpCost = 250,
            description = "Tap deep volcanic vents to harness the earth's fury for constant 80 Mana/day baseload power.",
            unlockFeature = "Volcanic Power Forge",
            isUnlocked = unlockedTechs.contains("tech_geothermal_power"),
            isAvailable = unlockedTechs.contains("tech_smart_grid")
        ),
        ResearchTreeNode(
            id = "tech_vertical_farming",
            tier = 5,
            title = "Hanging Gardens",
            rpCost = 300,
            description = "Construct mystical hanging gardens yielding 10x crop output in divine tiered footprints.",
            unlockFeature = "Grand Hanging Gardens",
            isUnlocked = unlockedTechs.contains("tech_vertical_farming"),
            isAvailable = unlockedTechs.contains("tech_full_automation")
        ),
        ResearchTreeNode(
            id = "tech_fusion_research",
            tier = 5,
            title = "Aetheric Core",
            rpCost = 500,
            description = "Bind a shard of pure Aether into a compact containment field producing 200 Mana/day unlimited power.",
            unlockFeature = "Endgame Aetheric Reactor",
            isUnlocked = unlockedTechs.contains("tech_fusion_research"),
            isAvailable = unlockedTechs.contains("tech_smart_grid")
        ),
        ResearchTreeNode(
            id = "tech_new_dawn",
            tier = 5,
            title = "The Eternal Kingdom",
            rpCost = 600,
            description = "Establish the Eternal Kingdom and trigger the coronation story ending.",
            unlockFeature = "Kingdom Rebuilt (Coronation)",
            isUnlocked = unlockedTechs.contains("tech_new_dawn"),
            isAvailable = unlockedTechs.contains("tech_ecological_mastery") && unlockedTechs.contains("tech_fusion_research")
        )
    )
}
