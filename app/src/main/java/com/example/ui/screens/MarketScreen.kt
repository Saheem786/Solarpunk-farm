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
import androidx.compose.material.icons.filled.LocalShipping
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
import com.example.data.model.CropType
import androidx.compose.material.icons.filled.LocalFlorist
import com.example.ui.FarmViewModel
import com.example.ui.components.MedievalProgressBar
import com.example.ui.theme.*

@Composable
fun MarketScreen(
    viewModel: FarmViewModel,
    modifier: Modifier = Modifier
) {
    val contracts by viewModel.contracts.collectAsStateWithLifecycle()
    val inventory by viewModel.inventory.collectAsStateWithLifecycle()
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()

    val hour = gameState?.gameTimeHour ?: 8.0f
    val isOpen = hour >= 8.0f && hour < 18.0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1A0F0A)) // Deep Wood
            .padding(16.dp)
            .testTag("market_screen"),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header
        item {
            Column {
                Text(
                    text = "TRADING POST & MERCHANT GUILD",
                    color = AgedGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MedievalSharpFontFamily
                )
                Text(
                    text = "Fulfill royal commissions and trade exotic goods across the realm",
                    color = MedievalTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Closed Banner
        if (!isOpen) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("market_closed_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3E1F1F)),
                    border = androidx.compose.foundation.BorderStroke(2.dp, MedievalCrimson)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📜 MARKET CLOSED",
                            color = MedievalCrimson,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MedievalSharpFontFamily
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "The Merchant Guild is closed until dawn (8:00 AM — 6:00 PM).\nPlease return during business hours to fulfill commissions.",
                            color = Parchment,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        // Wholesale City Contracts Section
        item {
            Text(
                text = "KING'S COMMISSIONS (${contracts.size})",
                color = AgedGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MedievalSharpFontFamily
            )
        }

        items(contracts) { contract ->
            val inventoryCount = inventory.find { it.itemId == contract.requiredItemId }?.quantity ?: 0
            ContractCard(
                contract = contract,
                availableCount = inventoryCount,
                isOpen = isOpen,
                onClaim = { viewModel.claimContract(contract.id) }
            )
        }

        // Buy Seeds Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "PURCHASE SACRED SEEDS",
                color = AgedGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MedievalSharpFontFamily
            )
        }

        val seedCropTypes = listOf(
            CropType.WHEAT,
            CropType.CORN,
            CropType.TOMATO,
            CropType.CARROT,
            CropType.HERBS
        )

        items(seedCropTypes) { crop ->
            BuySeedCard(
                crop = crop,
                isOpen = isOpen,
                playerCoins = gameState?.solCoins ?: 0,
                onBuy = { viewModel.buySeed(crop, 1) }
            )
        }

        // Direct Trade Sell Terminal
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "TRADE WITH TRAVELING MERCHANTS",
                color = AgedGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MedievalSharpFontFamily
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
                    isOpen = isOpen,
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
    isOpen: Boolean,
    onClaim: () -> Unit
) {
    val isComplete = availableCount >= contract.requiredQty
    val progress = (availableCount.toFloat() / contract.requiredQty.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("contract_${contract.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Parchment),
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            if (contract.isClaimed) Bronze else if (isComplete) AgedGold else Bronze.copy(alpha = 0.5f)
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
                                if (contract.isClaimed) Bronze.copy(alpha = 0.2f) else AgedGold.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (contract.isClaimed) Icons.Default.CheckCircle else Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = if (contract.isClaimed) Bronze else MedievalCrimson,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = contract.title,
                            color = MedievalCrimson,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MedievalSharpFontFamily
                        )
                        Text(
                            text = "Commissioned by: ${contract.buyerName}",
                            color = MedievalTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                if (contract.isClaimed) {
                    Surface(
                        color = Bronze.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Delivered",
                            color = Bronze,
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
                    color = MedievalTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (contract.isClaimed) "Completed" else "$availableCount / ${contract.requiredQty}",
                    color = if (contract.isClaimed || isComplete) Bronze else MedievalCrimson,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            MedievalProgressBar(
                progress = if (contract.isClaimed) 1.0f else progress,
                modifier = Modifier.fillMaxWidth().height(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reward: ${contract.rewardCoins}🪙 + ${contract.rewardEco} Realm Prestige",
                    color = Bronze,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                if (!contract.isClaimed) {
                    Button(
                        onClick = onClaim,
                        enabled = isComplete && isOpen,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MedievalCrimson,
                            disabledContainerColor = Color(0x33000000)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("claim_contract_${contract.id}")
                    ) {
                        Text(
                            text = if (!isOpen) "Guild Closed" else if (isComplete) "Dispatch Caravan" else "Gather Goods",
                            color = if (isComplete && isOpen) Parchment else MedievalTextSecondary,
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
    isOpen: Boolean,
    onSellOne: () -> Unit,
    onSellAll: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("market_sell_${item.itemId}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Parchment),
        border = androidx.compose.foundation.BorderStroke(1.dp, Bronze.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MedievalCrimson.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${item.quantity}x",
                    color = MedievalCrimson,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    color = MedievalCrimson,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MedievalSharpFontFamily
                )
                Text(
                    text = "Value: ${item.sellValue} 🪙 each (Total: ${item.sellValue * item.quantity} 🪙)",
                    color = Bronze,
                    fontSize = 11.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onSellOne,
                    enabled = isOpen,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MedievalCrimson.copy(alpha = 0.1f),
                        disabledContainerColor = Color(0x11000000)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("sell_one_${item.itemId}")
                ) {
                    Text(text = "1x", color = if (isOpen) MedievalCrimson else MedievalTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onSellAll,
                    enabled = isOpen,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MedievalCrimson,
                        disabledContainerColor = Color(0x33000000)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("sell_all_${item.itemId}")
                ) {
                    Text(text = "All", color = if (isOpen) Parchment else MedievalTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun BuySeedCard(
    crop: CropType,
    isOpen: Boolean,
    playerCoins: Int,
    onBuy: () -> Unit
) {
    val canAfford = playerCoins >= crop.seedCost
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("market_buy_seed_${crop.name.lowercase()}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Parchment),
        border = androidx.compose.foundation.BorderStroke(1.dp, AgedGold.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(crop.primaryColor).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFlorist,
                    contentDescription = null,
                    tint = Color(crop.primaryColor),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${crop.displayName} Seeds",
                    color = MedievalCrimson,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MedievalSharpFontFamily
                )
                Text(
                    text = "Cost: ${crop.seedCost} 🪙 • Growth Time: ${crop.growthDays} Days",
                    color = Bronze,
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = onBuy,
                enabled = isOpen && canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MedievalCrimson,
                    disabledContainerColor = Color(0x33000000)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_buy_seed_${crop.name.lowercase()}")
            ) {
                Text(
                    text = if (!canAfford) "Need Gold" else "Buy 1x",
                    color = if (isOpen && canAfford) Parchment else MedievalTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
