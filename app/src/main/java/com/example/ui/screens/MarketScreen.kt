package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.ContractEntity
import com.example.data.local.InventoryEntity
import com.example.ui.FarmViewModel
import com.example.ui.components.SolarpunkProgressBar
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SunGold

@Composable
fun MarketScreen(
    viewModel: FarmViewModel,
    modifier: Modifier = Modifier
) {
    val contracts by viewModel.contracts.collectAsStateWithLifecycle()
    val inventory by viewModel.inventory.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A1315))
            .padding(16.dp)
            .testTag("market_screen"),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header
        item {
            Column {
                Text(
                    text = "Solarpunk Eco-Market & Zeppelin Trading",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Fulfill city wholesale contracts and trade organic goods",
                    color = SunGold,
                    fontSize = 12.sp
                )
            }
        }

        // Wholesale City Contracts Section
        item {
            Text(
                text = "City Green Supply Contracts (${contracts.size})",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(contracts) { contract ->
            val inventoryCount = inventory.find { it.itemId == contract.requiredItemId }?.quantity ?: 0
            ContractCard(
                contract = contract,
                availableCount = inventoryCount,
                onClaim = { viewModel.claimContract(contract.id) }
            )
        }

        // Direct Trade Sell Terminal
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sell Goods to Local Eco-Traders",
                color = SolarEmerald,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        val sellableItems = inventory.filter { it.quantity > 0 }
        if (sellableItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF112427))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No harvest or crafted goods currently in inventory.\nHarvest crops or produce items to trade!",
                            color = Color(0xFF90A4AE),
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(sellableItems) { item ->
                SellItemCard(
                    item = item,
                    onSellOne = { viewModel.sellItem(item.itemId, 1) },
                    onSellAll = { viewModel.sellItem(item.itemId, item.quantity) }
                )
            }
        }
    }
}

@Composable
private fun ContractCard(
    contract: ContractEntity,
    availableCount: Int,
    onClaim: () -> Unit
) {
    val isComplete = availableCount >= contract.requiredQty
    val progress = (availableCount.toFloat() / contract.requiredQty.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("contract_${contract.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13282C)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (contract.isClaimed) Color(0x3300E676) else if (isComplete) SunGold else Color(0x3380D8FF)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            .background(
                                if (contract.isClaimed) SolarEmerald.copy(alpha = 0.2f) else SunGold.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (contract.isClaimed) Icons.Default.CheckCircle else Icons.Default.FlightTakeoff,
                            contentDescription = null,
                            tint = if (contract.isClaimed) SolarEmerald else SunGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = contract.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Buyer: ${contract.buyerName}",
                            color = Color(0xFFB0BEC5),
                            fontSize = 11.sp
                        )
                    }
                }

                if (contract.isClaimed) {
                    Surface(
                        color = SolarEmerald.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Fulfilled",
                            color = SolarEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Order Requirements
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Requires: ${contract.requiredQty}x ${contract.requiredItemName}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (contract.isClaimed) "Delivered" else "$availableCount / ${contract.requiredQty}",
                    color = if (contract.isClaimed || isComplete) SolarEmerald else Color(0xFFFF8A80),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            SolarpunkProgressBar(
                progress = if (contract.isClaimed) 1.0f else progress,
                color = if (isComplete) SunGold else SolarEmerald,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reward: ${contract.rewardCoins}🪙 + ${contract.rewardEco} Eco Prestige",
                    color = SunGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                if (!contract.isClaimed) {
                    Button(
                        onClick = onClaim,
                        enabled = isComplete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SunGold,
                            disabledContainerColor = Color(0xFF263238)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("claim_contract_${contract.id}")
                    ) {
                        Text(
                            text = if (isComplete) "Dispatch Zeppelin" else "Pending Supply",
                            color = if (isComplete) Color(0xFF091215) else Color(0xFF90A4AE),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SellItemCard(
    item: InventoryEntity,
    onSellOne: () -> Unit,
    onSellAll: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("market_sell_${item.itemId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF112529)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x2280CBC4))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SolarEmerald.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${item.quantity}x",
                    color = SolarEmerald,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Value: ${item.sellValue} 🪙 each (Total: ${item.sellValue * item.quantity} 🪙)",
                    color = SunGold,
                    fontSize = 11.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onSellOne,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E463E)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("sell_one_${item.itemId}")
                ) {
                    Text(text = "1x", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onSellAll,
                    colors = ButtonDefaults.buttonColors(containerColor = SolarEmerald),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("sell_all_${item.itemId}")
                ) {
                    Text(text = "All", color = Color(0xFF091215), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
