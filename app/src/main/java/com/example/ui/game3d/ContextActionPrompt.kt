package com.example.ui.game3d

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game3d.interaction.InteractionPrompt
import com.example.game3d.interaction.InteractionTargetType
import com.example.ui.theme.BrassGold
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SunGold
import com.example.ui.theme.SunGoldLight

@Composable
fun ContextActionPrompt(
    prompt: InteractionPrompt?,
    onActionClick: () -> Unit,
    onSecondaryActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = prompt != null,
        enter = scaleIn(),
        exit = scaleOut(),
        modifier = modifier
    ) {
        if (prompt != null) {
            Surface(
                modifier = Modifier
                    .widthIn(min = 260.dp, max = 440.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .clickable { onActionClick() }
                    .testTag("action_prompt_button"),
                color = Color(0xF20A2320),
                shape = RoundedCornerShape(26.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(listOf(SolarEmerald, SunGold))
                ),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = when (prompt.targetType) {
                        InteractionTargetType.PLOT -> Icons.Default.LocalFlorist
                        InteractionTargetType.LIVESTOCK -> Icons.Default.Favorite
                        InteractionTargetType.ENERGY_NODE -> Icons.Default.Build
                        InteractionTargetType.WORKSHOP_BUILDING -> Icons.Default.Handyman
                        InteractionTargetType.MARKET_STALL -> Icons.Default.Store
                        InteractionTargetType.WATER_SOURCE -> Icons.Default.WaterDrop
                        InteractionTargetType.WATER_BUILDING -> Icons.Default.WaterDrop
                        InteractionTargetType.CAMPFIRE -> Icons.Default.Eco
                        InteractionTargetType.FARMHOUSE -> Icons.Default.Eco
                        InteractionTargetType.NPC -> Icons.Default.Person
                        InteractionTargetType.NONE -> Icons.Default.Eco
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(SunGold, SolarEmerald))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color(0xFF091215),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = prompt.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = prompt.subtitle,
                            color = SunGoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }

                    if (prompt.secondaryActionTitle != null && onSecondaryActionClick != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onSecondaryActionClick() }
                                .testTag("btn_secondary_action"),
                            color = Color(0x4400E5FF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CleanCyan),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = prompt.secondaryActionTitle,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
