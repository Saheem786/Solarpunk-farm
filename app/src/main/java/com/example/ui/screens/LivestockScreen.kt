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
import com.example.ui.components.MedievalProgressBar
import com.example.ui.theme.*

@Composable
fun LivestockScreen(
    viewModel: FarmViewModel,
    modifier: Modifier = Modifier
) {
    val livestock by viewModel.livestock.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1A0F0A)) // Deep Wood
            .padding(16.dp)
            .testTag("livestock_screen"),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header
        item {
            Column {
                Text(
                    text = "ROYAL MENAGERIE & STABLES",
                    color = AgedGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MedievalSharpFontFamily
                )
                Text(
                    text = "Sacred beasts & noble companions living in harmony with the land",
                    color = MedievalTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Sanctuary Animals List
        item {
            Text(
                text = "BEASTS OF THE REALM (${livestock.size})",
                color = AgedGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MedievalSharpFontFamily
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
                text = "ACQUIRE NOBLE BEASTS",
                color = AgedGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MedievalSharpFontFamily
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Parchment),
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            if (animal.readyToHarvest) AgedGold else Bronze.copy(alpha = 0.5f)
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
                            .background(Color(animal.type.tintColor).copy(alpha = 0.1f)),
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
                            color = MedievalCrimson,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MedievalSharpFontFamily
                        )
                        Text(
                            text = "Yields: ${animal.type.productProduced} (${animal.type.productSellPrice} 🪙)",
                            color = MedievalTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                if (animal.readyToHarvest) {
                    Surface(
                        color = AgedGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AgedGold)
                    ) {
                        Text(
                            text = "Ready to Gather",
                            color = MedievalCrimsonDark,
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
                    text = "Nurture Level",
                    color = Bronze,
                    fontSize = 11.sp
                )
                Text(
                    text = "${animal.happiness.toInt()}%",
                    color = Bronze,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            MedievalProgressBar(
                progress = animal.happiness / 100f,
                modifier = Modifier.fillMaxWidth().height(10.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Production Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${animal.type.productProduced} Progress",
                    color = MedievalCrimson,
                    fontSize = 11.sp
                )
                Text(
                    text = "${(animal.produceProgress * 100).toInt()}%",
                    color = MedievalCrimson,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            MedievalProgressBar(
                progress = animal.produceProgress,
                modifier = Modifier.fillMaxWidth().height(10.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Interaction Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onPet,
                    colors = ButtonDefaults.buttonColors(containerColor = MedievalCrimson.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("pet_animal_${animal.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MedievalCrimson,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Nurture", fontSize = 11.sp, color = MedievalCrimson, fontWeight = FontWeight.Bold, fontFamily = MedievalSharpFontFamily)
                }

                Button(
                    onClick = onFeed,
                    colors = ButtonDefaults.buttonColors(containerColor = MedievalCrimson.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("feed_animal_${animal.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = MedievalCrimson,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Nourish", fontSize = 11.sp, color = MedievalCrimson, fontWeight = FontWeight.Bold, fontFamily = MedievalSharpFontFamily)
                }

                if (animal.readyToHarvest) {
                    Button(
                        onClick = onCollect,
                        colors = ButtonDefaults.buttonColors(containerColor = AgedGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(36.dp)
                            .testTag("collect_animal_${animal.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = MedievalCrimsonDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Gather",
                            color = MedievalCrimsonDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MedievalSharpFontFamily
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Parchment),
        border = androidx.compose.foundation.BorderStroke(1.dp, AgedGold.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(type.tintColor).copy(alpha = 0.1f)),
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
                    color = MedievalCrimson,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MedievalSharpFontFamily
                )
                Text(
                    text = type.description,
                    color = MedievalTextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = "Produces ${type.productProduced} (${type.productSellPrice} 🪙)",
                    color = Bronze,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = onAdopt,
                colors = ButtonDefaults.buttonColors(containerColor = MedievalCrimson),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("adopt_btn_${type.name.lowercase()}")
            ) {
                Text(
                    text = "${type.purchaseCostCoins}🪙",
                    color = Parchment,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MedievalSharpFontFamily
                )
            }
        }
    }
}
