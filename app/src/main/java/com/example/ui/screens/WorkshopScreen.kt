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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Science
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
import com.example.data.local.InventoryEntity
import com.example.data.model.ItemCategory
import com.example.ui.FarmViewModel
import com.example.ui.theme.*

data class CraftingRecipeItem(
    val id: String,
    val name: String,
    val description: String,
    val category: ItemCategory,
    val sellValue: Int,
    val energyCostKwh: Float,
    val ingredients: Map<String, Pair<String, Int>> // itemId -> (ItemName, RequiredQty)
)

@Composable
fun WorkshopScreen(
    viewModel: FarmViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val inventory by viewModel.inventory.collectAsStateWithLifecycle()
    val batteryCharge = gameState?.batteryChargeKwh ?: 0f

    val recipes = listOf(
        CraftingRecipeItem(
            id = "bio_fuel",
            name = "Essence of Sun",
            description = "High-energy sacred distillate from golden grains and solar sunflowers.",
            category = ItemCategory.CRAFTED,
            sellValue = 160,
            energyCostKwh = 5.0f,
            ingredients = mapOf(
                "harvest_terraced_wheat" to Pair("Golden Grain", 2),
                "harvest_solar_sunflower" to Pair("Solar Sunflower", 1)
            )
        ),
        CraftingRecipeItem(
            id = "solar_fabric",
            name = "Silk of the Heavens",
            description = "Luminous photovoltaic textile woven with sheared solar wool.",
            category = ItemCategory.CRAFTED,
            sellValue = 220,
            energyCostKwh = 4.0f,
            ingredients = mapOf(
                "solar_wool" to Pair("Solar Wool", 2)
            )
        ),
        CraftingRecipeItem(
            id = "fertilizer_bio",
            name = "Alchemical Growth Salve",
            description = "Concentrated elixir for accelerating the growth of sacred crops.",
            category = ItemCategory.TOOL,
            sellValue = 45,
            energyCostKwh = 3.0f,
            ingredients = mapOf(
                "harvest_nitro_beans" to Pair("Bio-Nitro Beans", 2),
                "harvest_bioluminescent_mushroom" to Pair("Biolum Spores", 1)
            )
        ),
        CraftingRecipeItem(
            id = "kombucha_elixir",
            name = "Elixir of Rejuvenation",
            description = "Probiotic brew crafted with medicinal moss and wild forest honey.",
            category = ItemCategory.CRAFTED,
            sellValue = 180,
            energyCostKwh = 3.5f,
            ingredients = mapOf(
                "harvest_sky_spirulina" to Pair("Medicinal Moss", 2),
                "solar_honey" to Pair("Forest Honey", 1)
            )
        ),
        CraftingRecipeItem(
            id = "power_cell",
            name = "Mystic Power Stone",
            description = "Arcane storage stone infused with cursed berries and pure solar essence.",
            category = ItemCategory.TECH_UPGRADE,
            sellValue = 350,
            energyCostKwh = 10.0f,
            ingredients = mapOf(
                "harvest_cyber_berries" to Pair("Cursed Berries", 2),
                "bio_fuel" to Pair("Essence of Sun", 1)
            )
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1A0F0A)) // Deep Wood
            .padding(16.dp)
            .testTag("workshop_screen"),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header
        item {
            Column {
                Text(
                    text = "MASTER'S FORGE & ALCHEMY LAB",
                    color = AgedGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MedievalSharpFontFamily
                )
                Text(
                    text = "Craft legendary artifacts, sacred fabrics & power stones using ancient mana",
                    color = MedievalTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Energy Availability Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Parchment,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, AgedGold)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = MedievalCrimson,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Mana & Essence Available",
                                color = MedievalCrimson,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = MedievalSharpFontFamily
                            )
                            Text(
                                text = "Ancient rituals require mana essence to manifest",
                                color = MedievalTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Text(
                        text = "${batteryCharge.toInt()} Mana",
                        color = MedievalCrimson,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        item {
            Text(
                text = "ANCIENT SCHEMATICS (${recipes.size})",
                color = AgedGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MedievalSharpFontFamily
            )
        }

        items(recipes) { recipe ->
            RecipeCard(
                recipe = recipe,
                inventory = inventory,
                availableBattery = batteryCharge,
                onCraft = {
                    val consumedMap = recipe.ingredients.mapValues { it.value.second }
                    viewModel.craftItem(
                        resultId = recipe.id,
                        resultName = recipe.name,
                        category = recipe.category,
                        sellVal = recipe.sellValue,
                        consumedIngredients = consumedMap,
                        energyKwhCost = recipe.energyCostKwh
                    )
                }
            )
        }
    }
}

@Composable
private fun RecipeCard(
    recipe: CraftingRecipeItem,
    inventory: List<InventoryEntity>,
    availableBattery: Float,
    onCraft: () -> Unit
) {
    val hasEnoughEnergy = availableBattery >= recipe.energyCostKwh
    var hasAllIngredients = true

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recipe_${recipe.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Parchment),
        border = androidx.compose.foundation.BorderStroke(2.dp, AgedGold.copy(alpha = 0.5f))
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
                            .background(MedievalCrimson.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = MedievalCrimson,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = recipe.name,
                            color = MedievalCrimson,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MedievalSharpFontFamily
                        )
                        Text(
                            text = "Value: ${recipe.sellValue} 🪙 • Mana: ${recipe.energyCostKwh} units",
                            color = Bronze,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = recipe.description,
                color = MedievalTextPrimary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Ingredients List
            Text(
                text = "REQUIRED MATERIALS:",
                color = Bronze,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            recipe.ingredients.forEach { (itemId, req) ->
                val currentCount = inventory.find { it.itemId == itemId }?.quantity ?: 0
                val isSufficient = currentCount >= req.second
                if (!isSufficient) hasAllIngredients = false

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "• ${req.first}",
                        color = MedievalTextPrimary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "$currentCount / ${req.second}",
                        color = if (isSufficient) Bronze else MedievalCrimson,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val canCraft = hasAllIngredients && hasEnoughEnergy

            Button(
                onClick = onCraft,
                enabled = canCraft,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MedievalCrimson,
                    disabledContainerColor = Color(0x33000000)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("craft_btn_${recipe.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Handyman,
                    contentDescription = null,
                    tint = if (canCraft) Parchment else MedievalTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (!hasEnoughEnergy) "Insufficient Mana (${recipe.energyCostKwh} units)"
                           else if (!hasAllIngredients) "Missing Materials"
                           else "Commence Ritual",
                    color = if (canCraft) Parchment else MedievalTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
