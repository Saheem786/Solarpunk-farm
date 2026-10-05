package com.example.ui.game3d

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SunGold

data class QuickActionItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val action: () -> Unit
)

@Composable
fun QuickActionWheelModal(
    onEat: () -> Unit,
    onDrink: () -> Unit,
    onRest: () -> Unit,
    onOpenBuild: () -> Unit,
    onOpenInventory: () -> Unit,
    onOpenResearch: () -> Unit,
    onOpenJournal: () -> Unit,
    onOpenEnergy: () -> Unit,
    onDismiss: () -> Unit
) {
    val items = listOf(
        QuickActionItem("Eat", Icons.Default.Restaurant, Color(0xFFFF9800), onEat),
        QuickActionItem("Drink", Icons.Default.WaterDrop, Color(0xFF00E5FF), onDrink),
        QuickActionItem("Rest", Icons.Default.Bed, Color(0xFF00E676), onRest),
        QuickActionItem("Build", Icons.Default.Handyman, SunGold, onOpenBuild),
        QuickActionItem("Bag", Icons.Default.ShoppingBag, CleanCyan, onOpenInventory),
        QuickActionItem("Research", Icons.Default.Science, Color(0xFFE040FB), onOpenResearch),
        QuickActionItem("Journal", Icons.Default.Book, SunGold, onOpenJournal),
        QuickActionItem("Energy", Icons.Default.Bolt, SolarEmerald, onOpenEnergy)
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .size(320.dp)
                .clip(CircleShape)
                .border(2.dp, CleanCyan, CircleShape)
                .testTag("quick_action_radial_wheel"),
            color = Color(0xF209181A)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Center Dismiss Button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0x44FFFFFF))
                        .border(1.dp, Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Wheel",
                        tint = Color.White
                    )
                }

                // Grid or Ring Layout of 8 Actions
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ActionBubble(items[0], onDismiss)
                        ActionBubble(items[1], onDismiss)
                        ActionBubble(items[2], onDismiss)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ActionBubble(items[7], onDismiss)
                        Spacer(modifier = Modifier.width(60.dp))
                        ActionBubble(items[3], onDismiss)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ActionBubble(items[6], onDismiss)
                        ActionBubble(items[5], onDismiss)
                        ActionBubble(items[4], onDismiss)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionBubble(item: QuickActionItem, onDismiss: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                item.action()
                onDismiss()
            }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(item.color.copy(alpha = 0.25f))
                .border(1.dp, item.color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = item.color,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = item.title,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
