package com.example.ui.game3d

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.InventoryEntity
import com.example.ui.theme.*

@Composable
fun QuickInventoryModal(
    inventory: List<InventoryEntity>,
    onSellItem: (String, Int) -> Unit,
    onConsumeItem: (String) -> Unit = {},
    onOpenMap: () -> Unit = {},
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(12.dp))
                .border(3.dp, AgedGold, RoundedCornerShape(12.dp))
                .testTag("quick_inventory_modal"),
            color = Parchment,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = MedievalCrimson,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "KINGDOM STASH",
                            color = MedievalCrimson,
                            fontFamily = MedievalSharpFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onDismiss()
                                    onOpenMap()
                                }
                                .testTag("btn_bag_open_map"),
                            color = ForestMoss.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ForestMoss)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Explore,
                                    contentDescription = "Map",
                                    tint = ForestMoss,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Scroll Map",
                                    color = MedievalTextPrimary,
                                    fontFamily = MedievalSharpFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_inventory_modal")
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

                if (inventory.isEmpty() || inventory.all { it.quantity == 0 }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Thy stash is currently empty.\nGather resources or craft items!",
                            color = MedievalTextSecondary,
                            fontFamily = AlmendraFontFamily,
                            fontSize = 15.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(inventory.filter { it.quantity > 0 }) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("inventory_item_${item.itemId}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0x0A2D1B0D)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AgedGold.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MedievalCrimson.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${item.quantity}",
                                            color = MedievalCrimson,
                                            fontFamily = MedievalSharpFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            color = MedievalCrimson,
                                            fontFamily = MedievalSharpFontFamily,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Value: ${item.sellValue} Gold",
                                            color = MedievalTextPrimary,
                                            fontFamily = AlmendraFontFamily,
                                            fontSize = 12.sp
                                        )
                                    }

                                    val isEgg = item.itemId == "harvest_egg" || item.itemId.contains("egg")
                                    val isMilk = item.itemId == "harvest_milk" || item.itemId.contains("milk") || item.itemId == "organic_bio-milk"
                                    val isEdible = isEgg || isMilk || item.itemId.startsWith("harvest_") || item.category == com.example.data.model.ItemCategory.PRODUCE

                                    if (isEdible) {
                                        Button(
                                            onClick = { onConsumeItem(item.itemId) },
                                            colors = ButtonDefaults.buttonColors(containerColor = if (isMilk) RoyalBlue else MedievalCrimson),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.padding(end = 6.dp).testTag("consume_${item.itemId}"),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (isEgg) "Eat" else if (isMilk) "Drink" else "Eat",
                                                color = Parchment,
                                                fontFamily = MedievalSharpFontFamily,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { onSellItem(item.itemId, 1) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AgedGold),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("sell_${item.itemId}"),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Sell,
                                            contentDescription = null,
                                            tint = MedievalTextPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Sell",
                                            color = MedievalTextPrimary,
                                            fontFamily = MedievalSharpFontFamily,
                                            fontSize = 12.sp,
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
    }
}
