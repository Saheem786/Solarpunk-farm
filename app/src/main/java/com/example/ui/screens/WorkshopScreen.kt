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
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SunGold

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
            name = "Purified Bio-Fuel",
            description = "High-energy clean fuel distilled from golden grains and solar sunflowers.",
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
            name = "Solar-Weave Fabric",
            description = "Flexible photovoltaic textile woven with sheared solar wool.",
            category = ItemCategory.CRAFTED,
            sellValue = 220,
            energyCostKwh = 4.0f,
            ingredients = mapOf(
                "solar_wool" to Pair("Solar Wool", 2)
            )
        ),
        CraftingRecipeItem(
            id = "fertilizer_bio",
            name = "Bio-Compost Serum",
            description = "Nutrient-dense liquid fertilizer for accelerated crop growth.",
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
            name = "Organic Kombucha Elixir",
            description = "Fermented probiotic beverage brewed with spirulina and solar honey.",
            category = ItemCategory.CRAFTED,
            sellValue = 180,
            energyCostKwh = 3.5f,
            ingredients = mapOf(
                "harvest_sky_spirulina" to Pair("Sky Spirulina", 2),
                "solar_honey" to Pair("Solar Honey", 1)
            )
        ),
        CraftingRecipeItem(
            id = "power_cell",
            name = "Solid-State Power Cell",
            description = "High-density clean storage core crafted from cyber berries and bio-fuel.",
            category = ItemCategory.TECH_UPGRADE,
            sellValue = 350,
            energyCostKwh = 10.0f,
            ingredients = mapOf(
                "harvest_cyber_berries" to Pair("Cyber Berries", 2),
                "bio_fuel" to Pair("Purified Bio-Fuel", 1)
            )
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A1416))
            .padding(16.dp)
            .testTag("workshop_screen"),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header
        item {
            Column {
                Text(
                    text = "Artisan Eco-Workshop",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Craft bio-materials, solar fabrics & power cells with clean energy",
                    color = Color(0xFF80D8FF),
                    fontSize = 12.sp
                )
            }
        }

        // Energy Availability Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF10272B),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CleanCyan.copy(alpha = 0.4f))
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
                            tint = CleanCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Grid Power Available",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Each craft cycle consumes battery reserve",
                                color = Color(0xFFB0BEC5),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Text(
                        text = "${batteryCharge.toInt()} kWh",
                        color = CleanCyan,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        item {
            Text(
                text = "Crafting Blueprints (${recipes.size})",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF12272A)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3300E676))
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
                            .background(SolarEmerald.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = SolarEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = recipe.name,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Value: ${recipe.sellValue} 🪙 • Energy: ${recipe.energyCostKwh} kWh",
                            color = SunGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = recipe.description,
                color = Color(0xFFB0BEC5),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Ingredients List
            Text(
                text = "Required Materials:",
                color = Color(0xFF80CBC4),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
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
                        color = Color.White,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "$currentCount / ${req.second}",
                        color = if (isSufficient) SolarEmerald else Color(0xFFFF8A80),
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
                    containerColor = SolarEmerald,
                    disabledContainerColor = Color(0xFF263238)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("craft_btn_${recipe.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Handyman,
                    contentDescription = null,
                    tint = if (canCraft) Color(0xFF091215) else Color(0xFF78909C),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (!hasEnoughEnergy) "Insufficient Power (${recipe.energyCostKwh} kWh)"
                           else if (!hasAllIngredients) "Missing Ingredients"
                           else "Craft in Workshop",
                    color = if (canCraft) Color(0xFF091215) else Color(0xFF90A4AE),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
