package com.example.data.repository

import com.example.data.local.ContractEntity
import com.example.data.local.EnergyNodeEntity
import com.example.data.local.FarmDao
import com.example.data.local.GameStateEntity
import com.example.data.local.InventoryEntity
import com.example.data.local.LivestockEntity
import com.example.data.local.PlotEntity
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

    val gameState: Flow<GameStateEntity?> = dao.getGameState()
    val plots: Flow<List<PlotEntity>> = dao.getAllPlots()
    val energyNodes: Flow<List<EnergyNodeEntity>> = dao.getAllEnergyNodes()
    val livestock: Flow<List<LivestockEntity>> = dao.getAllLivestock()
    val inventory: Flow<List<InventoryEntity>> = dao.getAllInventory()
    val contracts: Flow<List<ContractEntity>> = dao.getAllContracts()

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
                droneHarvesterUnlocked = false
            )
            dao.saveGameState(initialState)

            // Seed 12 Farm Plots in a 3x4 Solarpunk grid
            val defaultPlots = mutableListOf<PlotEntity>()
            val plotLayout = listOf(
                Pair(PlotType.PERMACULTURE_BED, CropType.SOLAR_SUNFLOWER),
                Pair(PlotType.PERMACULTURE_BED, CropType.TERRACED_WHEAT),
                Pair(PlotType.BIO_DOME, CropType.BIOLUMINESCENT_MUSHROOM),
                Pair(PlotType.HYDROPONIC_TOWER, CropType.SKY_SPIRULINA),
                Pair(PlotType.PERMACULTURE_BED, CropType.CYBER_BERRIES),
                Pair(PlotType.PERMACULTURE_BED, CropType.NITRO_BEANS),
                Pair(PlotType.SOLAR_SOIL_PATCH, CropType.SOLAR_CORN),
                Pair(PlotType.HYDROPONIC_TOWER, CropType.HYDROPONIC_MELON),
                Pair(PlotType.PERMACULTURE_BED, null),
                Pair(PlotType.PERMACULTURE_BED, null),
                Pair(PlotType.BIO_DOME, null),
                Pair(PlotType.SOLAR_SOIL_PATCH, null)
            )

            plotLayout.forEachIndexed { index, pair ->
                val row = index / 4
                val col = index % 4
                val posX = (col - 1.5f) * 6.5f - 4.0f
                val posZ = (row - 1.0f) * 6.5f - 6.0f
                val isSeeded = pair.second != null
                defaultPlots.add(
                    PlotEntity(
                        id = index,
                        plotType = pair.first,
                        cropType = pair.second,
                        stage = if (isSeeded) CropStage.VEGETATIVE else CropStage.EMPTY,
                        progress = if (isSeeded) 0.55f else 0.0f,
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
                InventoryEntity("seed_sunflower", "Solar Sunflower Seeds", ItemCategory.SEEDS, 5, 15),
                InventoryEntity("seed_mushroom", "Biolum Spore Pack", ItemCategory.SEEDS, 4, 25),
                InventoryEntity("seed_wheat", "Golden Wheat Grains", ItemCategory.SEEDS, 6, 18),
                InventoryEntity("seed_spirulina", "Sky Spirulina Culture", ItemCategory.SEEDS, 4, 12),
                InventoryEntity("seed_berries", "Cyber Berry Cuttings", ItemCategory.SEEDS, 3, 30),
                InventoryEntity("fertilizer_bio", "Bio-Compost Serum", ItemCategory.TOOL, 8, 10),
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

    /**
     * Simulation tick for game world loop.
     * Updates time, weather, crop growth, energy generation, animal wander and production.
     */
    suspend fun gameTick(deltaSec: Float) {
        val state = dao.getGameState().firstOrNull() ?: return
        val plotsList = dao.getAllPlots().firstOrNull() ?: emptyList()
        val energyList = dao.getAllEnergyNodes().firstOrNull() ?: emptyList()
        val animalsList = dao.getAllLivestock().firstOrNull() ?: emptyList()

        // 1. Advance Game Time (1 real sec = 2 game minutes; 24 game hours = 12 real minutes)
        val timeAdvanceHours = (deltaSec / 60.0f) * 2.0f
        var newHour = state.gameTimeHour + timeAdvanceHours
        var newDay = state.gameTimeDay
        if (newHour >= 24.0f) {
            newHour -= 24.0f
            newDay += 1
        }

        // 2. Weather Shifts (Every few minutes randomly modulate weather)
        var currentWeather = state.currentWeather
        if (Random.nextFloat() < 0.005f * deltaSec) {
            val weatherValues = WeatherType.values()
            currentWeather = weatherValues[Random.nextInt(weatherValues.size)]
        }

        // Determine Solar & Wind Factors
        val phase = TimeOfDayPhase.values().find {
            newHour >= it.startHour && newHour < it.endHour
        } ?: TimeOfDayPhase.ZENITH
        val solarMultiplier = phase.solarIntensity * currentWeather.solarMultiplier
        val windMultiplier = currentWeather.windMultiplier

        // 3. Clean Energy Generation Calculation
        var totalGeneratedKwh = 0.0f
        var totalCapacity = 0.0f
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
        val maxCap = max(100.0f, totalCapacity + 50.0f)
        val newCharge = min(maxCap, state.batteryChargeKwh + totalGeneratedKwh)

        dao.saveGameState(
            state.copy(
                gameTimeHour = newHour,
                gameTimeDay = newDay,
                currentWeather = currentWeather,
                batteryChargeKwh = newCharge,
                batteryMaxCapacityKwh = maxCap
            )
        )

        // 4. Update Crop Plots
        val updatedPlots = plotsList.map { plot ->
            if (plot.cropType != null && plot.stage != CropStage.HARVEST_READY && plot.stage != CropStage.WITHERED) {
                val crop = plot.cropType
                // Auto rain or soil moisture
                val hasWater = plot.moisture > 0.1f || currentWeather.autoWaterRain
                val hasSun = solarMultiplier >= (crop.sunNeed * 0.3f) || plot.plotType == PlotType.BIO_DOME

                val moistureDrain = if (currentWeather == WeatherType.HEATWAVE) 0.015f else 0.006f
                val newMoisture = if (currentWeather.autoWaterRain) 1.0f else max(0.0f, plot.moisture - moistureDrain * deltaSec)

                if (hasWater && hasSun) {
                    val growthRate = (1.0f / crop.growthDurationSec) * (1.0f + plot.compostLevel * 0.5f)
                    val newProgress = min(1.0f, plot.progress + growthRate * deltaSec)
                    val newStage = when {
                        newProgress >= 1.0f -> CropStage.HARVEST_READY
                        newProgress >= 0.75f -> CropStage.FLOWERING
                        newProgress >= 0.45f -> CropStage.VEGETATIVE
                        newProgress >= 0.15f -> CropStage.SPROUT
                        else -> CropStage.SEEDLING
                    }
                    plot.copy(progress = newProgress, stage = newStage, moisture = newMoisture)
                } else {
                    plot.copy(moisture = newMoisture)
                }
            } else {
                plot
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
