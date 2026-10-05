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
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Work
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.NpcEntity
import com.example.data.model.NpcActivity
import com.example.data.model.NpcRole
import com.example.data.model.SettlementStats
import com.example.ui.FarmViewModel
import com.example.ui.components.SolarpunkProgressBar
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SunGold

@Composable
fun SanctuaryTabModal(
    viewModel: FarmViewModel,
    onDismiss: () -> Unit
) {
    val npcs by viewModel.npcs.collectAsStateWithLifecycle()
    val stats by viewModel.settlementStats.collectAsStateWithLifecycle()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, SolarEmerald.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                .testTag("modal_sanctuary_settlement"),
            color = Color(0xFA09181A)
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
                                .background(SolarEmerald.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = "Sanctuary",
                                tint = SolarEmerald,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Sanctuary Settlement",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${stats.totalPopulation} Survivors • ${stats.occupiedBeds}/${stats.totalBeds} Housing Beds",
                                color = CleanCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_sanctuary_modal")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Settlement Top Quick Stats Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickStatPill(
                        title = "Morale",
                        value = "${stats.overallMorale.toInt()}%",
                        color = if (stats.overallMorale > 60f) SolarEmerald else Color(0xFFFF5252),
                        icon = Icons.Default.Favorite,
                        modifier = Modifier.weight(1f)
                    )
                    QuickStatPill(
                        title = "Food Stock",
                        value = "${stats.foodStockUnits} u",
                        color = if (stats.isStarving) Color(0xFFFF5252) else SunGold,
                        icon = Icons.Default.Restaurant,
                        modifier = Modifier.weight(1f)
                    )
                    QuickStatPill(
                        title = "Clean Water",
                        value = "${stats.cleanWaterStockUnits} u",
                        color = if (stats.isDehydrated) Color(0xFFFF5252) else CleanCyan,
                        icon = Icons.Default.WaterDrop,
                        modifier = Modifier.weight(1f)
                    )
                    QuickStatPill(
                        title = "Beds",
                        value = "${stats.occupiedBeds}/${stats.totalBeds}",
                        color = if (stats.isHousingDeficit) Color(0xFFFF9800) else Color(0xFFBA68C8),
                        icon = Icons.Default.Bed,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (npcs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = Color(0x66FFFFFF),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Survivors in Settlement Yet",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Grow 3 crops and reach Day 5 for your first survivor arrival (Farmer Arjun)!",
                                color = Color(0xFFB0BEC5),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(npcs, key = { it.id }) { npc ->
                            NpcRosterCard(
                                npc = npc,
                                onTalkClick = { viewModel.openNpcDialogue(npc) },
                                onAssignRole = { role -> viewModel.assignNpcRole(npc.id, role) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStatPill(
    title: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
        color = Color(0x33102A2E),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = title,
                    color = Color(0xFFB0BEC5),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun NpcRosterCard(
    npc: NpcEntity,
    onTalkClick: () -> Unit,
    onAssignRole: (NpcRole) -> Unit
) {
    var showRoleMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("npc_card_${npc.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xDD11282C)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(npc.appearanceColor).copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(npc.appearanceColor).copy(alpha = 0.3f))
                            .border(1.5.dp, Color(npc.appearanceColor), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val roleIcon = when (npc.role) {
                            NpcRole.FARMER -> Icons.Default.Eco
                            NpcRole.ENGINEER -> Icons.Default.Bolt
                            NpcRole.BUILDER -> Icons.Default.Construction
                            NpcRole.RESEARCHER -> Icons.Default.Science
                            NpcRole.MEDIC -> Icons.Default.MedicalServices
                        }
                        Icon(
                            imageVector = roleIcon,
                            contentDescription = npc.role.displayName,
                            tint = Color(npc.appearanceColor),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = npc.name,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(npc.appearanceColor).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(npc.appearanceColor))
                            ) {
                                Text(
                                    text = npc.role.displayName,
                                    color = Color(npc.appearanceColor),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "Lvl ${npc.skillLevel} ${npc.role.title} • ${npc.trait.displayName}",
                            color = Color(0xFFB0BEC5),
                            fontSize = 11.sp
                        )
                    }
                }

                Button(
                    onClick = onTalkClick,
                    colors = ButtonDefaults.buttonColors(containerColor = SolarEmerald),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_talk_npc_${npc.id}")
                ) {
                    Text("Talk", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Current Activity & Speech Bubble
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Activity: ${npc.currentActivity.displayName}",
                    color = CleanCyan,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Morale: ${npc.morale.toInt()}%",
                    color = if (npc.morale > 50f) SolarEmerald else Color(0xFFFF5252),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            SolarpunkProgressBar(
                progress = npc.morale / 100f,
                color = if (npc.morale > 50f) SolarEmerald else Color(0xFFFF5252),
                modifier = Modifier.fillMaxWidth()
            )

            if (!npc.speechBubble.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = Color(0x3300E5FF),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "\"${npc.speechBubble}\"",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
