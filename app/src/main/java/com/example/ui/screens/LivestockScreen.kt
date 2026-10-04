package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.example.data.local.LivestockEntity
import com.example.data.model.LivestockType
import com.example.ui.FarmViewModel
import com.example.ui.components.SolarpunkProgressBar
import com.example.ui.theme.BrassGold
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SunGold

@Composable
fun LivestockScreen(
    viewModel: FarmViewModel,
    modifier: Modifier = Modifier
) {
    val livestock by viewModel.livestock.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF091415))
            .padding(16.dp)
            .testTag("livestock_screen"),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header
        item {
            Column {
                Text(
                    text = "Regenerative Eco-Sanctuary",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Pasture grazers & bio-pollinators living in balance",
                    color = Color(0xFF80CBC4),
                    fontSize = 12.sp
                )
            }
        }

        // Sanctuary Animals List
        item {
            Text(
                text = "Sanctuary Animals (${livestock.size})",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(livestock) { animal ->
            AnimalCard(
                animal = animal,
                onPet = { viewModel.petAnimal(animal.id) },
                onFeed = { viewModel.feedAnimal(animal.id) },
                onCollect = { viewModel.collectAnimal(animal.id) }
            )
        }

        // Adopt New Animal Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Adopt Regenerative Companions",
                color = SunGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(LivestockType.values()) { type ->
            AdoptAnimalCard(
                type = type,
                onAdopt = { viewModel.buyLivestock(type) }
            )
        }
    }
}

@Composable
private fun AnimalCard(
    animal: LivestockEntity,
    onPet: () -> Unit,
    onFeed: () -> Unit,
    onCollect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("animal_card_${animal.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF122629)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (animal.readyToHarvest) SunGold else Color(0x3300E676)
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
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(animal.type.tintColor).copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = Color(animal.type.tintColor),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = animal.name,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Produces: ${animal.type.productProduced} (${animal.type.productSellPrice} 🪙)",
                            color = Color(0xFFB0BEC5),
                            fontSize = 11.sp
                        )
                    }
                }

                if (animal.readyToHarvest) {
                    Surface(
                        color = SunGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SunGold)
                    ) {
                        Text(
                            text = "Ready to Collect",
                            color = SunGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Happiness Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Happiness",
                    color = SolarEmerald,
                    fontSize = 11.sp
                )
                Text(
                    text = "${animal.happiness.toInt()}%",
                    color = SolarEmerald,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            SolarpunkProgressBar(
                progress = animal.happiness / 100f,
                color = SolarEmerald,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Production Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${animal.type.productProduced} Progress",
                    color = CleanCyan,
                    fontSize = 11.sp
                )
                Text(
                    text = "${(animal.produceProgress * 100).toInt()}%",
                    color = CleanCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            SolarpunkProgressBar(
                progress = animal.produceProgress,
                color = CleanCyan,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Interaction Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPet,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("pet_animal_${animal.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFFF80AB),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Pet", fontSize = 11.sp, color = Color.White)
                }

                OutlinedButton(
                    onClick = onFeed,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("feed_animal_${animal.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = SunGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Feed", fontSize = 11.sp, color = Color.White)
                }

                if (animal.readyToHarvest) {
                    Button(
                        onClick = onCollect,
                        colors = ButtonDefaults.buttonColors(containerColor = SunGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(36.dp)
                            .testTag("collect_animal_${animal.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = Color(0xFF091215),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Collect",
                            color = Color(0xFF091215),
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
private fun AdoptAnimalCard(
    type: LivestockType,
    onAdopt: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("adopt_animal_${type.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF112528)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFD54F))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(type.tintColor).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    tint = Color(type.tintColor),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = type.displayName,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = type.description,
                    color = Color(0xFFB0BEC5),
                    fontSize = 11.sp
                )
                Text(
                    text = "Produces ${type.productProduced} (${type.productSellPrice} 🪙)",
                    color = SolarEmerald,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = onAdopt,
                colors = ButtonDefaults.buttonColors(containerColor = SolarEmerald),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("adopt_btn_${type.name.lowercase()}")
            ) {
                Text(
                    text = "${type.purchaseCostCoins}🪙",
                    color = Color(0xFF091215),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
