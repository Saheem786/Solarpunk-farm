package com.example.data.repository

import com.example.data.local.ContractEntity
import com.example.data.local.EnergyNodeEntity
import com.example.data.local.FarmDao
import com.example.data.local.GameStateEntity
import com.example.data.local.InventoryEntity
import com.example.data.local.LivestockEntity
import com.example.data.local.PlacedBuildingEntity
import com.example.data.local.PlotEntity
import com.example.data.model.BuildableType
import com.example.data.model.CropStage
import com.example.data.model.CropType
import com.example.data.model.EnergyNodeType
import com.example.data.model.ItemCategory
import com.example.data.model.LivestockType
import com.example.data.model.PlotType
import com.example.data.model.TimeOfDayPhase
import com.example.data.model.WeatherType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

class FarmRepository(private val dao: FarmDao) {

    val farmDao: FarmDao get() = dao

    val gameState: Flow<GameStateEntity?> = dao.getGameState()
    val plots: Flow<List<PlotEntity>> = dao.getAllPlots()
    val energyNodes: Flow<List<EnergyNodeEntity>> = dao.getAllEnergyNodes()
    val livestock: Flow<List<LivestockEntity>> = dao.getAllLivestock()
    val inventory: Flow<List<InventoryEntity>> = dao.getAllInventory()
    val contracts: Flow<List<ContractEntity>> = dao.getAllContracts()
    val placedBuildings: Flow<List<PlacedBuildingEntity>> = dao.getAllPlacedBuildings()

    suspend fun initializeDefaultDataIfEmpty() {
        val currentState = dao.getGameState().firstOrNull()
        if (currentState == null) {
            // Seed initial Game State
            val initialState = GameStateEntity(
                id = 1,
                solCoins = 450,
                ecoPrestige = 100,
                batteryChargeKwh = 65.0f,
                batteryMaxCapacityKwh = 120.0f,
                gameTimeHour = 8.5f,
                gameTimeDay = 1,
                currentWeather = WeatherType.SUNNY_CLEAR,
                playerX = 0.0f,
                playerY = 0.0f,
                playerZ = 0.0f,
                playerAngle = 0.0f,
                totalHarvests = 0,
                carbonOffsetKg = 150.0f,
                autoIrrigationUnlocked = false,
                droneHarvesterUnlocked = false,
                health = 100.0f,
                maxHealth = 100.0f,
                hunger = 90.0f,
                maxHunger = 100.0f,
                thirst = 85.0f,
                maxThirst = 100.0f,
                stamina = 100.0f,
                maxStamina = 100.0f,
                weatherChangeCountdownHours = 6.0f
            )
            dao.saveGameState(initialState)

            // Seed exactly 6 Farm Plots in a 2x3 grid near the greenhouse
            val defaultPlots = mutableListOf<PlotEntity>()
            val plotLayout = listOf(
                Pair(PlotType.PERMACULTURE_BED, CropType.WHEAT),
                Pair(PlotType.PERMACULTURE_BED, CropType.CORN),
                Pair(PlotType.PERMACULTURE_BED, CropType.TOMATO),
                Pair(PlotType.PERMACULTURE_BED, null),
                Pair(PlotType.PERMACULTURE_BED, null),
                Pair(PlotType.PERMACULTURE_BED, null)
            )

            plotLayout.forEachIndexed { index, pair ->
                val row = index / 3
                val col = index % 3
                val posX = -4.0f + (col - 1.0f) * 4.5f
                val posZ = -6.0f + (row - 0.5f) * 4.5f
                val isSeeded = pair.second != null
                defaultPlots.add(
                    PlotEntity(
                        id = index,
                        plotType = pair.first,
                        cropType = pair.second,
                        stage = if (isSeeded) CropStage.VEGETATIVE else CropStage.EMPTY,
                        progress = if (isSeeded) 1.5f else 0.0f,
                        moisture = 0.85f,
                        compostLevel = 0.7f,
                        posX = posX,
                        posY = 0.0f,
                        posZ = posZ,
                        autoIrrigated = false
                    )
                )
            }
            dao.insertPlots(defaultPlots)

            // Seed 4 Clean Energy Generation & Storage Nodes
            val initialEnergyNodes = listOf(
                EnergyNodeEntity(
                    id = 1,
                    nodeType = EnergyNodeType.PHOTOVOLTAIC_ARRAY,
                    level = 1,
                    efficiency = 1.0f,
                    posX = 14.0f,
                    posY = 0.0f,
                    posZ = -10.0f,
                    isActive = true
                ),
                EnergyNodeEntity(
                    id = 2,
                    nodeType = EnergyNodeType.VERTICAL_WIND_TURBINE,
                    level = 1,
                    efficiency = 1.1f,
                    posX = 14.0f,
                    posY = 0.0f,
                    posZ = -2.0f,
                    isActive = true
                ),
                EnergyNodeEntity(
                    id = 3,
                    nodeType = EnergyNodeType.BATTERY_STORAGE_BANK,
                    level = 1,
                    efficiency = 1.0f,
                    posX = 14.0f,
                    posY = 0.0f,
                    posZ = 6.0f,
                    isActive = true
                ),
                EnergyNodeEntity(
                    id = 4,
                    nodeType = EnergyNodeType.BIOGAS_DIGESTER,
                    level = 1,
                    efficiency = 0.9f,
                    posX = 14.0f,
                    posY = 0.0f,
                    posZ = 14.0f,
                    isActive = true
                )
            )
            dao.insertEnergyNodes(initialEnergyNodes)

            // Seed Initial Regenerative Livestock
            val initialLivestock = listOf(
                LivestockEntity(
                    id = 1,
                    type = LivestockType.SOLAR_SHEEP,
                    name = "Aura (Solar Sheep)",
                    happiness = 90.0f,
                    hunger = 15.0f,
                    age = 2,
                    produceProgress = 0.7f,
                    readyToHarvest = false,
                    posX = -12.0f,
                    posY = 0.0f,
                    posZ = 4.0f,
                    targetX = -10.0f,
                    targetZ = 6.0f
                ),
                LivestockEntity(
                    id = 2,
                    type = LivestockType.CYBER_BOVINE,
                    name = "Bessie (Meadow Cow)",
                    happiness = 85.0f,
                    hunger = 25.0f,
                    age = 3,
                    produceProgress = 0.4f,
                    readyToHarvest = false,
                    posX = -14.0f,
                    posY = 0.0f,
                    posZ = 12.0f,
                    targetX = -13.0f,
                    targetZ = 10.0f
                ),
                LivestockEntity(
                    id = 3,
                    type = LivestockType.ROBO_BEE_POLLINATOR,
                    name = "Hive Alpha (Bio-Bees)",
                    happiness = 95.0f,
                    hunger = 10.0f,
                    age = 1,
                    produceProgress = 0.85f,
                    readyToHarvest = false,
                    posX = -4.0f,
                    posY = 1.5f,
                    posZ = -14.0f,
                    targetX = -2.0f,
                    targetZ = -12.0f
                ),
                LivestockEntity(
                    id = 4,
                    type = LivestockType.MEADOW_ALPACA,
                    name = "Nimbus (Cloud Alpaca)",
                    happiness = 88.0f,
                    hunger = 20.0f,
                    age = 2,
                    produceProgress = 0.3f,
                    readyToHarvest = false,
                    posX = -16.0f,
                    posY = 0.0f,
                    posZ = 0.0f,
                    targetX = -14.0f,
                    targetZ = 2.0f
                )
            )
            dao.insertLivestockList(initialLivestock)

            // Seed Initial Inventory Items
            val initialInventory = listOf(
                InventoryEntity("seed_wheat", "Golden Wheat Seeds", ItemCategory.SEEDS, 8, 12),
                InventoryEntity("seed_corn", "Sweet Corn Seeds", ItemCategory.SEEDS, 6, 18),
                InventoryEntity("seed_tomato", "Ruby Tomato Seeds", ItemCategory.SEEDS, 6, 15),
                InventoryEntity("seed_carrot", "Crisp Carrot Seeds", ItemCategory.SEEDS, 6, 14),
                InventoryEntity("seed_herbs", "Aromatic Herb Seeds", ItemCategory.SEEDS, 8, 10),
                InventoryEntity("seed_sunflower", "Solar Sunflower Seeds", ItemCategory.SEEDS, 5, 15),
                InventoryEntity("seed_mushroom", "Biolum Spore Pack", ItemCategory.SEEDS, 4, 25),
                InventoryEntity("material_bio_timber", "Bio-Timber", ItemCategory.CRAFTED, 25, 20),
                InventoryEntity("material_solar_glass", "Solar Glass", ItemCategory.CRAFTED, 20, 25),
                InventoryEntity("material_eco_alloy", "Eco-Alloy", ItemCategory.CRAFTED, 18, 30),
                InventoryEntity("material_bio_polymer", "Bio-Polymer", ItemCategory.CRAFTED, 15, 22),
                InventoryEntity("fertilizer_bio", "Bio-Compost Serum", ItemCategory.TOOL, 10, 10),
                InventoryEntity("solar_wool", "Solar Wool", ItemCategory.PRODUCE, 2, 75),
                InventoryEntity("solar_honey", "Solar Honey", ItemCategory.PRODUCE, 3, 60),
                InventoryEntity("bio_fuel", "Purified Bio-Fuel", ItemCategory.CRAFTED, 1, 120)
            )
            dao.insertInventoryList(initialInventory)

            // Seed Initial City Eco Contracts
            val initialContracts = listOf(
                ContractEntity(
                    id = "contract_01",
                    title = "Sol City Green Bakery Order",
                    buyerName = "Solaria Metropolis",
                    requiredItemId = "harvest_wheat",
                    requiredItemName = "Golden Grain",
                    requiredQty = 6,
                    currentQty = 0,
                    rewardCoins = 380,
                    rewardEco = 60,
                    isClaimed = false
                ),
                ContractEntity(
                    id = "contract_02",
                    title = "Bio-Textile Guild Supply",
                    buyerName = "Neo-Eden Weavers",
                    requiredItemId = "solar_wool",
                    requiredItemName = "Solar Wool",
                    requiredQty = 4,
                    currentQty = 2,
                    rewardCoins = 420,
                    rewardEco = 80,
                    isClaimed = false
                ),
                ContractEntity(
                    id = "contract_03",
                    title = "Clean Energy City Export",
                    buyerName = "Zephyr Sky Port",
                    requiredItemId = "bio_fuel",
                    requiredItemName = "Purified Bio-Fuel",
                    requiredQty = 3,
                    currentQty = 1,
                    rewardCoins = 550,
                    rewardEco = 110,
                    isClaimed = false
                )
            )
            dao.insertContracts(initialContracts)
        }
    }

    suspend fun saveGameState(state: GameStateEntity) {
        dao.saveGameState(state)
    }

    suspend fun updatePlayerPosition(x: Float, y: Float, z: Float, angle: Float) {
        val current = dao.getGameState().firstOrNull() ?: return
        dao.saveGameState(current.copy(playerX = x, playerY = y, playerZ = z, playerAngle = angle))
    }

    suspend fun plantCrop(plotId: Int, cropType: CropType, currentPlots: List<PlotEntity>): Boolean {
        val plot = currentPlots.find { it.id == plotId } ?: return false
        val state = dao.getGameState().firstOrNull() ?: return false
        val seedItemId = "seed_${cropType.name.lowercase()}"
        val seedItem = dao.getInventoryItem(seedItemId)

        // Check if player has seed or buy seed
        if (seedItem != null && seedItem.quantity > 0) {
            dao.insertInventory(seedItem.copy(quantity = seedItem.quantity - 1))
        } else {
            if (state.solCoins < cropType.seedCost) return false
            dao.saveGameState(state.copy(solCoins = state.solCoins - cropType.seedCost))
        }

        dao.updatePlot(
            plot.copy(
                cropType = cropType,
                stage = CropStage.SEEDLING,
                progress = 0.05f,
                moisture = max(plot.moisture, 0.5f)
            )
        )
        return true
    }

    suspend fun waterPlot(plotId: Int, currentPlots: List<PlotEntity>): Boolean {
        val plot = currentPlots.find { it.id == plotId } ?: return false
        dao.updatePlot(plot.copy(moisture = 1.0f))
        return true
    }

    suspend fun fertilizePlot(plotId: Int, currentPlots: List<PlotEntity>): Boolean {
        val plot = currentPlots.find { it.id == plotId } ?: return false
        val fertilizer = dao.getInventoryItem("fertilizer_bio")
        if (fertilizer == null || fertilizer.quantity <= 0) return false
        dao.insertInventory(fertilizer.copy(quantity = fertilizer.quantity - 1))
        dao.updatePlot(plot.copy(compostLevel = min(1.0f, plot.compostLevel + 0.4f)))
        return true
    }

    suspend fun harvestPlot(plotId: Int, currentPlots: List<PlotEntity>): CropType? {
        val plot = currentPlots.find { it.id == plotId } ?: return null
        val crop = plot.cropType ?: return null
        if (plot.stage != CropStage.HARVEST_READY) return null

        val harvestItemId = "harvest_${crop.name.lowercase()}"
        val existingItem = dao.getInventoryItem(harvestItemId)
        val newQty = (existingItem?.quantity ?: 0) + crop.harvestYield

        dao.insertInventory(
            InventoryEntity(
                itemId = harvestItemId,
                name = crop.displayName,
                category = ItemCategory.HARVEST,
                quantity = newQty,
                sellValue = crop.baseSellPrice
            )
        )

        // Reward energy bonus and eco prestige
        val state = dao.getGameState().firstOrNull()
        if (state != null) {
            val newCharge = min(state.batteryMaxCapacityKwh, state.batteryChargeKwh + crop.energyBonusKwh)
            dao.saveGameState(
                state.copy(
                    batteryChargeKwh = newCharge,
                    ecoPrestige = state.ecoPrestige + 15,
                    totalHarvests = state.totalHarvests + 1,
                    carbonOffsetKg = state.carbonOffsetKg + 2.5f
                )
            )
        }

        // Reset plot to empty fertile bed
        dao.updatePlot(
            plot.copy(
                cropType = null,
                stage = CropStage.EMPTY,
                progress = 0.0f,
                moisture = max(0.2f, plot.moisture - 0.25f)
            )
        )
        return crop
    }

    suspend fun feedLivestock(animalId: Int, currentAnimals: List<LivestockEntity>): Boolean {
        val animal = currentAnimals.find { it.id == animalId } ?: return false
        val grain = dao.getInventoryItem("harvest_wheat") ?: dao.getInventoryItem("seed_wheat")
        if (grain == null || grain.quantity <= 0) return false
        dao.insertInventory(grain.copy(quantity = grain.quantity - 1))
        dao.updateLivestock(
            animal.copy(
                hunger = max(0.0f, animal.hunger - 40.0f),
                happiness = min(100.0f, animal.happiness + 20.0f)
            )
        )
        return true
    }

    suspend fun petLivestock(animalId: Int, currentAnimals: List<LivestockEntity>): Boolean {
        val animal = currentAnimals.find { it.id == animalId } ?: return false
        dao.updateLivestock(animal.copy(happiness = min(100.0f, animal.happiness + 10.0f)))
        val state = dao.getGameState().firstOrNull() ?: return true
        dao.saveGameState(state.copy(ecoPrestige = state.ecoPrestige + 2))
        return true
    }

    suspend fun collectLivestockProduct(animalId: Int, currentAnimals: List<LivestockEntity>): String? {
        val animal = currentAnimals.find { it.id == animalId } ?: return null
        if (!animal.readyToHarvest) return null

        val produceItemId = animal.type.productProduced.lowercase().replace(" ", "_")
        val existingItem = dao.getInventoryItem(produceItemId)
        val newQty = (existingItem?.quantity ?: 0) + 1

        dao.insertInventory(
            InventoryEntity(
                itemId = produceItemId,
                name = animal.type.productProduced,
                category = ItemCategory.PRODUCE,
                quantity = newQty,
                sellValue = animal.type.productSellPrice
            )
        )

        dao.updateLivestock(
            animal.copy(
                readyToHarvest = false,
                produceProgress = 0.0f,
                happiness = min(100.0f, animal.happiness + 5.0f)
            )
        )
        return animal.type.productProduced
    }

    suspend fun buyEnergyNode(type: EnergyNodeType): Boolean {
        val state = dao.getGameState().firstOrNull() ?: return false
        if (state.solCoins < type.buildCostCoins) return false

        val nodes = dao.getAllEnergyNodes().firstOrNull() ?: emptyList()
        val index = nodes.size
        val posX = 14.0f
        val posZ = -12.0f + (index * 6.0f)

        dao.saveGameState(
            state.copy(
                solCoins = state.solCoins - type.buildCostCoins,
                ecoPrestige = state.ecoPrestige + 30,
                batteryMaxCapacityKwh = state.batteryMaxCapacityKwh + type.baseStorageCapacityKwh
            )
        )
        dao.insertEnergyNode(
            EnergyNodeEntity(
                nodeType = type,
                level = 1,
                efficiency = 1.0f,
                posX = posX,
                posY = 0.0f,
                posZ = posZ,
                isActive = true
            )
        )
        return true
    }

    suspend fun upgradeEnergyNode(nodeId: Int): Boolean {
        val nodes = dao.getAllEnergyNodes().firstOrNull() ?: return false
        val node = nodes.find { it.id == nodeId } ?: return false
        val state = dao.getGameState().firstOrNull() ?: return false
        val cost = node.level * 100
        if (state.solCoins < cost) return false

        dao.saveGameState(state.copy(solCoins = state.solCoins - cost, ecoPrestige = state.ecoPrestige + 25))
        dao.updateEnergyNode(node.copy(level = node.level + 1, efficiency = node.efficiency + 0.35f))
        return true
    }

    suspend fun buyLivestock(type: LivestockType): Boolean {
        val state = dao.getGameState().firstOrNull() ?: return false
        if (state.solCoins < type.purchaseCostCoins) return false

        val animals = dao.getAllLivestock().firstOrNull() ?: emptyList()
        val index = animals.size
        val posX = -12.0f - (index % 3) * 3.0f
        val posZ = 2.0f + (index * 4.0f)

        dao.saveGameState(state.copy(solCoins = state.solCoins - type.purchaseCostCoins, ecoPrestige = state.ecoPrestige + 40))
        dao.insertLivestock(
            LivestockEntity(
                type = type,
                name = "${type.displayName} #${index + 1}",
                happiness = 85.0f,
                hunger = 20.0f,
                age = 1,
                produceProgress = 0.0f,
                readyToHarvest = false,
                posX = posX,
                posY = 0.0f,
                posZ = posZ,
                targetX = posX + 1.0f,
                targetZ = posZ + 1.0f
            )
        )
        return true
    }

    suspend fun sellItem(itemId: String, qty: Int): Boolean {
        val item = dao.getInventoryItem(itemId) ?: return false
        if (item.quantity < qty) return false
        val state = dao.getGameState().firstOrNull() ?: return false
        val earned = item.sellValue * qty

        dao.insertInventory(item.copy(quantity = item.quantity - qty))
        dao.saveGameState(state.copy(solCoins = state.solCoins + earned, ecoPrestige = state.ecoPrestige + qty * 2))
        return true
    }

    suspend fun buySeed(cropType: CropType, qty: Int): Boolean {
        val state = dao.getGameState().firstOrNull() ?: return false
        val totalCost = cropType.seedCost * qty
        if (state.solCoins < totalCost) return false

        val seedItemId = "seed_${cropType.name.lowercase()}"
        val existing = dao.getInventoryItem(seedItemId)
        val newQty = (existing?.quantity ?: 0) + qty

        dao.saveGameState(state.copy(solCoins = state.solCoins - totalCost))
        dao.insertInventory(
            InventoryEntity(
                itemId = seedItemId,
                name = "${cropType.displayName} Seeds",
                category = ItemCategory.SEEDS,
                quantity = newQty,
                sellValue = cropType.seedCost / 2
            )
        )
        return true
    }

    suspend fun claimContract(contractId: String): Boolean {
        val contractsList = dao.getAllContracts().firstOrNull() ?: return false
        val contract = contractsList.find { it.id == contractId } ?: return false
        if (contract.isClaimed) return false

        val item = dao.getInventoryItem(contract.requiredItemId) ?: return false
        if (item.quantity < contract.requiredQty) return false

        val state = dao.getGameState().firstOrNull() ?: return false
        dao.insertInventory(item.copy(quantity = item.quantity - contract.requiredQty))
        dao.saveGameState(
            state.copy(
                solCoins = state.solCoins + contract.rewardCoins,
                ecoPrestige = state.ecoPrestige + contract.rewardEco,
                carbonOffsetKg = state.carbonOffsetKg + 10.0f
            )
        )
        dao.updateContract(contract.copy(isClaimed = true, currentQty = contract.requiredQty))
        return true
    }

    suspend fun craftItem(
        resultId: String,
        resultName: String,
        category: ItemCategory,
        sellVal: Int,
        consumedIngredients: Map<String, Int>,
        energyKwhCost: Float
    ): Boolean {
        val state = dao.getGameState().firstOrNull() ?: return false
        if (state.batteryChargeKwh < energyKwhCost) return false

        // Check ingredients
        for ((itemId, requiredQty) in consumedIngredients) {
            val item = dao.getInventoryItem(itemId) ?: return false
            if (item.quantity < requiredQty) return false
        }

        // Deduct ingredients
        for ((itemId, requiredQty) in consumedIngredients) {
            val item = dao.getInventoryItem(itemId) ?: continue
            dao.insertInventory(item.copy(quantity = item.quantity - requiredQty))
        }

        // Deduct energy and grant prestige
        dao.saveGameState(
            state.copy(
                batteryChargeKwh = max(0.0f, state.batteryChargeKwh - energyKwhCost),
                ecoPrestige = state.ecoPrestige + 35,
                carbonOffsetKg = state.carbonOffsetKg + 5.0f
            )
        )

        // Add crafted item
        val existing = dao.getInventoryItem(resultId)
        val newQty = (existing?.quantity ?: 0) + 1
        dao.insertInventory(
            InventoryEntity(
                itemId = resultId,
                name = resultName,
                category = category,
                quantity = newQty,
                sellValue = sellVal
            )
        )
        return true
    }

    suspend fun saveGameSnapshot(
        playerX: Float,
        playerY: Float,
        playerZ: Float,
        playerAngle: Float
    ): Boolean {
        val state = dao.getGameState().firstOrNull() ?: return false
        dao.saveGameState(
            state.copy(
                playerX = playerX,
                playerY = playerY,
                playerZ = playerZ,
                playerAngle = playerAngle
            )
        )
        return true
    }

    suspend fun eatFood(foodItemId: String? = null): Pair<Boolean, String> {
        val state = dao.getGameState().firstOrNull() ?: return Pair(false, "Game state unavailable")
        
        var itemName = "Organic Farm Snack"
        if (foodItemId != null) {
            val item = dao.getInventoryItem(foodItemId)
            if (item != null && item.quantity > 0) {
                itemName = item.name
                dao.insertInventory(item.copy(quantity = item.quantity - 1))
            }
        } else {
            // Find any harvested edible produce in inventory
            val edibleProduce = dao.getAllInventory().firstOrNull()?.find { 
                it.quantity > 0 && (it.category == ItemCategory.PRODUCE || it.itemId.startsWith("harvest_")) 
            }
            if (edibleProduce != null) {
                itemName = edibleProduce.name
                dao.insertInventory(edibleProduce.copy(quantity = edibleProduce.quantity - 1))
            }
        }

        val newHunger = min(state.maxHunger, state.hunger + 30.0f)
        val newHealth = min(state.maxHealth, state.health + 10.0f)
        val newStamina = min(state.maxStamina, state.stamina + 15.0f)

        dao.saveGameState(
            state.copy(
                hunger = newHunger,
                health = newHealth,
                stamina = newStamina
            )
        )
        return Pair(true, "Ate $itemName (+30 Hunger, +10 HP)")
    }

    suspend fun drinkWater(source: String = "Canteen"): Pair<Boolean, String> {
        val state = dao.getGameState().firstOrNull() ?: return Pair(false, "Game state unavailable")
        val newThirst = min(state.maxThirst, state.thirst + 40.0f)
        val newStamina = min(state.maxStamina, state.stamina + 15.0f)

        dao.saveGameState(
            state.copy(
                thirst = newThirst,
                stamina = newStamina
            )
        )
        return Pair(true, "Drank from $source (+40 Thirst)")
    }

    suspend fun respawnAtHouse(): Boolean {
        val state = dao.getGameState().firstOrNull() ?: return false
        dao.saveGameState(
            state.copy(
                health = 100.0f,
                hunger = 50.0f,
                thirst = 50.0f,
                stamina = 100.0f,
                playerX = 6.0f,
                playerY = 0.0f,
                playerZ = 0.0f
            )
        )
        return true
    }

    suspend fun canAffordBuilding(type: BuildableType): Pair<Boolean, String> {
        val state = dao.getGameState().firstOrNull() ?: return Pair(false, "No state")
        if (state.solCoins < type.costCoins) {
            return Pair(false, "Need ${type.costCoins} 🪙 (Have ${state.solCoins})")
        }
        val material = dao.getInventoryItem(type.requiredMaterialId)
        val matQty = material?.quantity ?: 0
        if (matQty < type.requiredMaterialQty) {
            return Pair(false, "Need ${type.requiredMaterialQty}x ${type.materialName} (Have $matQty)")
        }
        return Pair(true, "Ready to place")
    }

    suspend fun placeBuilding(
        type: BuildableType,
        posX: Float,
        posY: Float,
        posZ: Float,
        rotationDeg: Float
    ): Pair<Boolean, String> {
        val (canAfford, reason) = canAffordBuilding(type)
        if (!canAfford) return Pair(false, reason)

        val state = dao.getGameState().firstOrNull() ?: return Pair(false, "No state")
        val material = dao.getInventoryItem(type.requiredMaterialId) ?: return Pair(false, "Missing material")

        // Deduct coins & materials
        val newCoins = state.solCoins - type.costCoins
        dao.insertInventory(material.copy(quantity = material.quantity - type.requiredMaterialQty))

        // Insert Placed Building
        dao.insertPlacedBuilding(
            PlacedBuildingEntity(
                buildingType = type,
                posX = posX,
                posY = posY,
                posZ = posZ,
                rotationDeg = rotationDeg
            )
        )

        // Apply immediate state effects
        val extraCapacity = if (type == BuildableType.STORAGE) 60.0f else 0.0f
        dao.saveGameState(
            state.copy(
                solCoins = newCoins,
                ecoPrestige = state.ecoPrestige + 30,
                carbonOffsetKg = state.carbonOffsetKg + 15.0f,
                batteryMaxCapacityKwh = state.batteryMaxCapacityKwh + extraCapacity
            )
        )

        return Pair(true, "Constructed ${type.displayName}!")
    }

    suspend fun restAtFarmhouse(): Pair<Boolean, String> {
        val state = dao.getGameState().firstOrNull() ?: return Pair(false, "Game state unavailable")
        val newStamina = state.maxStamina
        val newHealth = min(state.maxHealth, state.health + 30.0f)
        var newHour = state.gameTimeHour + 1.0f
        var newDay = state.gameTimeDay
        if (newHour >= 24.0f) {
            newHour -= 24.0f
            newDay += 1
        }

        dao.saveGameState(
            state.copy(
                stamina = newStamina,
                health = newHealth,
                gameTimeHour = newHour,
                gameTimeDay = newDay
            )
        )
        return Pair(true, "Rested at Farmhouse (+Full Stamina, +30 HP)")
    }

    /**
     * Simulation tick for game world loop.
     * Updates time, weather, survival stats (hunger, thirst, stamina, health), crop growth, energy generation, animal wander and production.
     */
    suspend fun gameTick(deltaSec: Float, isMoving: Boolean = false, isSprinting: Boolean = false) {
        val state = dao.getGameState().firstOrNull() ?: return
        val plotsList = dao.getAllPlots().firstOrNull() ?: emptyList()
        val energyList = dao.getAllEnergyNodes().firstOrNull() ?: emptyList()
        val animalsList = dao.getAllLivestock().firstOrNull() ?: emptyList()

        // 1. Advance Game Time (1 real minute = 1 game hour => 24 real minutes = 24 game hours)
        val timeAdvanceHours = (deltaSec / 60.0f)
        var newHour = state.gameTimeHour + timeAdvanceHours
        var newDay = state.gameTimeDay
        if (newHour >= 24.0f) {
            newHour -= 24.0f
            newDay += 1
        }

        // 2. Weather Shifts (Changes randomly every 6 game hours)
        var weatherCountdown = state.weatherChangeCountdownHours - timeAdvanceHours
        var currentWeather = state.currentWeather
        if (weatherCountdown <= 0.0f || newHour < state.gameTimeHour) {
            weatherCountdown = 6.0f
            val weatherPool = mutableListOf(
                WeatherType.SUNNY_CLEAR,
                WeatherType.CLOUDY_OVERCAST,
                WeatherType.RAINY_STORM,
                WeatherType.STORM
            )
            val isDaytimeNow = newHour >= 7.0f && newHour < 17.0f
            if (isDaytimeNow && Random.nextFloat() < 0.15f) {
                currentWeather = WeatherType.HEATWAVE
            } else {
                currentWeather = weatherPool[Random.nextInt(weatherPool.size)]
            }
        }

        // 3. Survival Stats Simulation
        // A. Hunger: decreases by 1 every 30 seconds (1/30 per sec)
        val hungerDrainRate = 1.0f / 30.0f
        val newHunger = max(0.0f, state.hunger - hungerDrainRate * deltaSec)

        // B. Thirst: decreases by 1 every 20 seconds (1/20 per sec), 2x faster during HEATWAVE
        val thirstMultiplier = if (currentWeather == WeatherType.HEATWAVE) 2.0f else 1.0f
        val thirstDrainRate = (1.0f / 20.0f) * thirstMultiplier
        val newThirst = max(0.0f, state.thirst - thirstDrainRate * deltaSec)

        // C. Stamina: decreases when walking (1/sec) or running (3/sec), regenerates when idle (2/sec)
        val newStamina = when {
            isSprinting -> max(0.0f, state.stamina - 3.0f * deltaSec)
            isMoving -> max(0.0f, state.stamina - 1.0f * deltaSec)
            else -> min(state.maxStamina, state.stamina + 2.0f * deltaSec)
        }

        // D. Health: When hunger = 0 decreases by 1 every 10s; when thirst = 0 decreases by 2 every 10s
        var newHealth = state.health
        if (newHunger <= 0.0f || newThirst <= 0.0f) {
            val starvationDmg = if (newHunger <= 0.0f) (1.0f / 10.0f) else 0.0f
            val dehydrationDmg = if (newThirst <= 0.0f) (2.0f / 10.0f) else 0.0f
            newHealth = max(0.0f, newHealth - (starvationDmg + dehydrationDmg) * deltaSec)
        } else if (newHunger >= 70.0f && newThirst >= 70.0f && newHealth < state.maxHealth) {
            newHealth = min(state.maxHealth, newHealth + 0.5f * deltaSec)
        }

        // Determine Solar & Wind Factors
        // Solar panels generate energy only during day (7:00 to 17:00)
        val isDay = newHour in 7.0f..17.0f
        val solarMultiplier = if (isDay) currentWeather.solarMultiplier else 0.0f
        // Windmill works 24/7 but slower at night (30% less, so 0.70x multiplier)
        val isNightTime = newHour >= 19.0f || newHour < 5.0f
        val windMultiplier = if (isNightTime) (currentWeather.windMultiplier * 0.70f) else currentWeather.windMultiplier

        // 4. Clean Energy Generation Calculation (Energy Nodes & Placed Buildings)
        var totalGeneratedKwh = 0.0f
        var totalCapacity = 0.0f
        val placedList = dao.getAllPlacedBuildings().firstOrNull() ?: emptyList()

        energyList.forEach { node ->
            if (node.isActive) {
                when (node.nodeType) {
                    EnergyNodeType.PHOTOVOLTAIC_ARRAY -> {
                        totalGeneratedKwh += node.nodeType.baseOutputKwhPerSec * node.efficiency * solarMultiplier * deltaSec
                    }
                    EnergyNodeType.VERTICAL_WIND_TURBINE -> {
                        totalGeneratedKwh += node.nodeType.baseOutputKwhPerSec * node.efficiency * windMultiplier * deltaSec
                    }
                    EnergyNodeType.BIOGAS_DIGESTER -> {
                        totalGeneratedKwh += node.nodeType.baseOutputKwhPerSec * node.efficiency * deltaSec
                    }
                    EnergyNodeType.BATTERY_STORAGE_BANK -> {
                        totalCapacity += node.nodeType.baseStorageCapacityKwh * node.level
                    }
                }
            }
        }

        // Placed Buildings Gameplay Effects
        placedList.forEach { building ->
            when (building.buildingType) {
                BuildableType.SOLAR_PANEL -> {
                    totalGeneratedKwh += 3.5f * solarMultiplier * deltaSec
                }
                BuildableType.WINDMILL -> {
                    totalGeneratedKwh += 2.8f * windMultiplier * deltaSec
                }
                BuildableType.STORAGE -> {
                    totalCapacity += 60.0f
                }
                else -> {}
            }
        }

        val maxCap = max(100.0f, totalCapacity + 50.0f)
        val newCharge = min(maxCap, state.batteryChargeKwh + totalGeneratedKwh)

        dao.saveGameState(
            state.copy(
                gameTimeHour = newHour,
                gameTimeDay = newDay,
                currentWeather = currentWeather,
                weatherChangeCountdownHours = weatherCountdown,
                batteryChargeKwh = newCharge,
                batteryMaxCapacityKwh = maxCap,
                hunger = newHunger,
                thirst = newThirst,
                stamina = newStamina,
                health = newHealth
            )
        )

        // 5. Update Crop Plots with Rain Hydration and Day-Start Growth at 6:00 AM
        val oldHour = state.gameTimeHour
        val crossed6AM = (oldHour < 6.0f && newHour >= 6.0f) || (newHour < oldHour && (oldHour < 6.0f || newHour >= 6.0f))

        val greenhouses = placedList.filter { it.buildingType == BuildableType.GREENHOUSE }
        var updatedPlots = plotsList

        // Rain auto-waters plots in real-time
        val isRaining = currentWeather.autoWaterRain
        if (isRaining) {
            updatedPlots = updatedPlots.map { it.copy(moisture = 1.0f) }
        }

        if (crossed6AM) {
            updatedPlots = updatedPlots.map { plot ->
                if (plot.cropType != null && plot.stage != CropStage.HARVEST_READY && plot.stage != CropStage.WITHERED) {
                    val crop = plot.cropType
                    
                    // Was watered? (moisture > 0.4f)
                    val wasWatered = plot.moisture > 0.4f
                    
                    if (wasWatered) {
                        // Growth rate multiplier
                        val rainBonus = if (isRaining) 1.5f else 1.0f
                        val isNearGreenhouse = greenhouses.any { gh ->
                            val dx = plot.posX - gh.posX
                            val dz = plot.posZ - gh.posZ
                            (dx * dx + dz * dz) < 81.0f
                        } || plot.plotType == PlotType.BIO_DOME
                        val greenhouseBonus = if (isNearGreenhouse) 1.3f else 1.0f
                        
                        val increment = 1.0f * rainBonus * greenhouseBonus
                        val newProgress = min(crop.growthDays.toFloat(), plot.progress + increment)
                        
                        val ratio = newProgress / crop.growthDays.toFloat()
                        val newStage = when {
                            ratio >= 0.99f -> CropStage.HARVEST_READY
                            ratio >= 0.50f -> CropStage.VEGETATIVE // Growing (medium)
                            ratio >= 0.20f -> CropStage.SPROUT      // Sprout (small)
                            else -> CropStage.SEEDLING              // Seed (tiny)
                        }
                        
                        plot.copy(
                            progress = newProgress,
                            stage = newStage,
                            moisture = 0.0f // reset to unwatered at 6:00 AM
                        )
                    } else {
                        // not watered, does not grow, reset moisture to 0
                        plot.copy(moisture = 0.0f)
                    }
                } else {
                    // Empty or ready/withered, reset moisture to 0
                    plot.copy(moisture = 0.0f)
                }
            }
        } else {
            // Gradual evaporation of water during dry hot daytime (7 AM - 5 PM)
            val isHotDay = newHour in 7.0f..17.0f && !isRaining
            if (isHotDay) {
                val moistureMultiplier = if (currentWeather == WeatherType.HEATWAVE) 2.0f else 1.0f
                val moistureDrain = 0.006f * moistureMultiplier * deltaSec
                updatedPlots = updatedPlots.map { plot ->
                    if (plot.cropType != null && plot.stage != CropStage.HARVEST_READY && plot.stage != CropStage.WITHERED) {
                        plot.copy(moisture = max(0.0f, plot.moisture - moistureDrain))
                    } else {
                        plot
                    }
                }
            }
        }
        dao.insertPlots(updatedPlots)

        // 5. Update Livestock (wandering + produce creation)
        val updatedAnimals = animalsList.map { animal ->
            val produceRate = (1.0f / animal.type.produceIntervalSec) * (animal.happiness / 100.0f)
            val newProdProgress = min(1.0f, animal.produceProgress + produceRate * deltaSec)
            val isReady = newProdProgress >= 1.0f

            // Slow wander target update
            var posX = animal.posX
            var posZ = animal.posZ
            var targetX = animal.targetX
            var targetZ = animal.targetZ
            if (Random.nextFloat() < 0.05f * deltaSec) {
                targetX = animal.posX + (Random.nextFloat() * 4.0f - 2.0f)
                targetZ = animal.posZ + (Random.nextFloat() * 4.0f - 2.0f)
            }
            posX += (targetX - posX) * 0.1f * deltaSec
            posZ += (targetZ - posZ) * 0.1f * deltaSec

            animal.copy(
                produceProgress = newProdProgress,
                readyToHarvest = isReady,
                posX = posX,
                posZ = posZ,
                targetX = targetX,
                targetZ = targetZ
            )
        }
        dao.insertLivestockList(updatedAnimals)
    }
}
