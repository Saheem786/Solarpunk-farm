package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Science
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.NpcEntity
import com.example.data.model.NpcRole
import com.example.ui.FarmViewModel
import com.example.ui.components.MedievalProgressBar
import com.example.ui.theme.*

@Composable
fun NpcDialogueModal(
    npc: NpcEntity?,
    viewModel: FarmViewModel,
    onDismiss: () -> Unit
) {
    if (npc == null) return

    var currentSpeech by remember { mutableStateOf(npc.speechBubble ?: "Greeting, Lord of the Island! How may I serve the kingdom this day?") }
    var showRolePicker by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(12.dp))
                .border(3.dp, AgedGold, RoundedCornerShape(12.dp))
                .testTag("modal_npc_dialogue"),
            color = Parchment
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
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
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(npc.appearanceColor).copy(alpha = 0.15f))
                                .border(2.dp, Color(npc.appearanceColor).copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val icon = when (npc.role) {
                                NpcRole.FARMER -> Icons.Default.LocalFlorist
                                NpcRole.ENGINEER -> Icons.Default.AutoFixHigh
                                NpcRole.BUILDER -> Icons.Default.Construction
                                NpcRole.RESEARCHER -> Icons.Default.MenuBook
                                NpcRole.MEDIC -> Icons.Default.MedicalServices
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = npc.role.displayName,
                                tint = Color(npc.appearanceColor),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = npc.name,
                                color = MedievalCrimson,
                                fontFamily = MedievalSharpFontFamily,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${npc.role.title} • Age ${npc.age}",
                                color = MedievalTextSecondary,
                                fontFamily = AlmendraFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_dialogue_modal")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MedievalCrimson
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Morale & Activity Bar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0x0A2D1B0D)),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AgedGold.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Current Duty: ${npc.currentActivity.displayName}",
                                color = MedievalTextPrimary,
                                fontFamily = AlmendraFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Loyalty: ${npc.morale.toInt()}%",
                                color = if (npc.morale > 50f) ForestMoss else MedievalCrimson,
                                fontFamily = MedievalSharpFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        MedievalProgressBar(
                            progress = npc.morale / 100f,
                            color = if (npc.morale > 50f) ForestMoss else MedievalCrimson,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Dialogue Speech Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AgedGold.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, AgedGold.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "\"$currentSpeech\"",
                        color = MedievalTextPrimary,
                        fontFamily = AlmendraFontFamily,
                        fontSize = 15.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (showRolePicker) {
                    Text(
                        text = "Assign New Duty:",
                        color = MedievalCrimson,
                        fontFamily = MedievalSharpFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        NpcRole.values().forEach { role ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.assignNpcRole(npc.id, role)
                                        showRolePicker = false
                                        onDismiss()
                                    },
                                color = Color(role.primaryColorHex).copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(role.primaryColorHex).copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = role.displayName,
                                    color = MedievalTextPrimary,
                                    fontFamily = AlmendraFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    // Response Options List
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DialogueOptionRow(
                            text = "\"How farest thou this day?\"",
                            testTag = "btn_dialogue_how_are_you",
                            onClick = {
                                viewModel.interactWithNpc(npc.id, "HOW_ARE_YOU")
                                currentSpeech = "My heart is light! I am honored to serve our realm."
                            }
                        )
                        DialogueOptionRow(
                            text = "\"Hast thou need of anything?\"",
                            testTag = "btn_dialogue_need_anything",
                            onClick = {
                                viewModel.interactWithNpc(npc.id, "NEED_ANYTHING")
                                currentSpeech = if (npc.hunger > 50f) "Our stores are low. We require a bountiful harvest." else "All is well in the citadel!"
                            }
                        )
                        DialogueOptionRow(
                            text = "\"Speak of thy past and skills.\"",
                            testTag = "btn_dialogue_tell_me",
                            onClick = {
                                viewModel.interactWithNpc(npc.id, "TELL_ME_ABOUT_YOURSELF")
                                currentSpeech = "I was trained as a ${npc.role.title}. ${npc.role.description}. I am known to be ${npc.trait.displayName}."
                            }
                        )
                        DialogueOptionRow(
                            text = "\"Assign a new duty\"",
                            testTag = "btn_dialogue_assign_role",
                            onClick = { showRolePicker = true }
                        )
                        DialogueOptionRow(
                            text = "\"Banish from the kingdom\"",
                            testTag = "btn_dialogue_dismiss",
                            isDestructive = true,
                            onClick = {
                                viewModel.interactWithNpc(npc.id, "DISMISS")
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogueOptionRow(
    text: String,
    testTag: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .testTag(testTag),
        color = if (isDestructive) MedievalCrimson.copy(alpha = 0.1f) else AgedGold.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDestructive) MedievalCrimson.copy(alpha = 0.5f) else AgedGold.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = null,
                tint = if (isDestructive) MedievalCrimson else AgedGoldDark,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                color = if (isDestructive) MedievalCrimson else MedievalTextPrimary,
                fontFamily = AlmendraFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
