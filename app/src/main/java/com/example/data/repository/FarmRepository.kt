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
import com.example.data.model.FishType
import com.example.data.model.FishingRodTier
import com.example.data.model.FishingSpotType
import com.example.data.model.ItemCategory
import com.example.data.model.LivestockType
import com.example.data.model.PlotType
import com.example.data.model.TimeOfDayPhase
import com.example.data.model.WaterQuality
import com.example.data.model.WeatherType
import com.example.data.model.DeviceType
import com.example.data.model.PowerPriority
import com.example.data.model.DevicePowerState
import com.example.data.model.EnergyGridSummary
import com.example.data.model.PowerSourceInfo
import com.example.data.model.BatteryUnitInfo
import com.example.data.model.GridMapNode
import com.example.data.model.EnergyResearchTech
import com.example.data.local.*
import com.example.data.model.*
import com.example.game3d.ai.NpcAiController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class FarmRepository(private val dao: FarmDao) {

    private val npcAiController = NpcAiController()

    val farmDao: FarmDao get() = dao

    val gameState: Flow<GameStateEntity?> = dao.getGameState()
    val plots: Flow<List<PlotEntity>> = dao.getAllPlots()
    val energyNodes: Flow<List<EnergyNodeEntity>> = dao.getAllEnergyNodes()
    val livestock: Flow<List<LivestockEntity>> = dao.getAllLivestock()
    val inventory: Flow<List<InventoryEntity>> = dao.getAllInventory()
    val contracts: Flow<List<ContractEntity>> = dao.getAllContracts()
    val placedBuildings: Flow<List<PlacedBuildingEntity>> = dao.getAllPlacedBuildings()
    val npcs: Flow<List<NpcEntity>> = dao.getAllNpcs()
    val npcArrivals: Flow<List<NpcArrivalEntity>> = dao.getAllArrivals()

    private val _pendingArrival = MutableStateFlow<NpcArrivalCandidate?>(null)
    val pendingArrival: StateFlow<NpcArrivalCandidate?> = _pendingArrival.asStateFlow()

    private val _settlementStats = MutableStateFlow<SettlementStats>(
        SettlementStats(
            totalPopulation = 0,
            occupiedBeds = 0,
            totalBeds = 2,
            overallMorale = 100f,
            foodStockUnits = 0,
            cleanWaterStockUnits = 0,
            daysOfFoodRemaining = 0f,
            daysOfWaterRemaining = 0f,
            dailyFoodConsumption = 0,
            dailyWaterConsumption = 0,
            roleBreakdown = emptyMap(),
            averageSkillLevel = 1.0f,
            isHousingDeficit = false,
            isStarving = false,
            isDehydrated = false
        )
    )
    val settlementStats: StateFlow<SettlementStats> = _settlementStats.asStateFlow()

    // Energy Priority & Management StateFlows
    private val _devicePriorities = MutableStateFlow<Map<DeviceType, PowerPriority>>(
        DeviceType.values().associateWith { it.defaultPriority }
    )
    val devicePriorities: StateFlow<Map<DeviceType, PowerPriority>> = _devicePriorities.asStateFlow()

    private val _energySummary = MutableStateFlow(
        EnergyGridSummary(
            totalGenerationKwhPerDay = 35.0f,
            currentGenerationKwhPerHour = 2.5f,
            totalStorageCapacityKwh = 120.0f,
            currentStoredKwh = 65.0f,
            totalHourlyConsumptionKwh = 5.0f,
            netHourlyKwh = -2.5f,
            isCharging = false,
            hoursUntilEmpty = 26.0f,
            batteryPercent = 0.54f,
            activeDevicesCount = 14,
            totalDevicesCount = 14,
            unpoweredDevicesCount = 0,
            powerSourcesCount = 2,
            batteryUnitsCount = 1,
            powerPolesCount = 0
        )
    )
    val energySummary: StateFlow<EnergyGridSummary> = _energySummary.asStateFlow()

    private val _unlockedTechs = MutableStateFlow<Set<String>>(emptySet())
    val unlockedTechs: StateFlow<Set<String>> = _unlockedTechs.asStateFlow()

    var onPowerLowAlert: ((String) -> Unit)? = null
    private var lastDisabledTiers = mutableSetOf<PowerPriority>()

    fun setDevicePriority(device: DeviceType, priority: PowerPriority) {
        val map = _devicePriorities.value.toMutableMap()
        map[device] = priority
        _devicePriorities.value = map
    }

    fun isDevicePowered(device: DeviceType): Boolean {
        val summary = _energySummary.value
        val prio = _devicePriorities.value[device] ?: device.defaultPriority
        return summary.batteryPercent >= prio.thresholdPercent && summary.currentStoredKwh > 0.05f
    }

    suspend fun unlockEnergyTech(techId: String, requiredRp: Int): Boolean {
        val state = dao.getGameState().firstOrNull() ?: return false
        if (state.researchPoints < requiredRp) return false
        if (_unlockedTechs.value.contains(techId)) return false
        _unlockedTechs.value = _unlockedTechs.value + techId
        val updatedState = state.copy(researchPoints = state.researchPoints - requiredRp)
        dao.saveGameState(updatedState)
        dao.insertResearchTechs(listOf(ResearchTechEntity(id = techId, tier = 1, title = techId, rpCost = requiredRp, isUnlocked = true)))

        if (techId == "tech_new_dawn") {
            dao.saveGameState(updatedState.copy(storyEndingUnlocked = true))
        }
        return true
    }

    suspend fun addResearchPoints(amount: Int) {
        val state = dao.getGameState().firstOrNull() ?: return
        dao.saveGameState(state.copy(researchPoints = state.researchPoints + amount))
    }

    suspend fun initializeDefaultDataIfEmpty(forceReset: Boolean = false) {
        val currentState = dao.getGameState().firstOrNull()
        if (currentState == null || forceReset) {
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
                weatherChangeCountdownHours = 6.0f,
                cleanWaterCarried = 4,
                rawWaterCarried = 0,
                maxWaterCarried = 10,
                isSick = false,
                sicknessRemainingHours = 0.0f,
                fishingRodTier = 1,
                fishPopStream = 10,
                fishPopRiver = 10,
                fishPopPond = 10
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

            // Seed Initial Regenerative Livestock (Including 5 Chickens and 2 Cows)
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
                    posX = -8.0f,
                    posY = 0.0f,
                    posZ = -4.0f,
                    targetX = -8.0f,
                    targetZ = -4.0f
                ),
                LivestockEntity(
                    id = 16,
                    type = LivestockType.CYBER_BOVINE,
                    name = "Daisy (Meadow Cow)",
                    happiness = 80.0f,
                    hunger = 20.0f,
                    age = 2,
                    produceProgress = 0.1f,
                    readyToHarvest = false,
                    posX = -10.0f,
                    posY = 0.0f,
                    posZ = -8.0f,
                    targetX = -10.0f,
                    targetZ = -8.0f
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
                ),
                // 5 Chickens spawned near the farmhouse (6.0, 0.0)
                LivestockEntity(
                    id = 10,
                    type = LivestockType.CHICKEN,
                    name = "Clucky",
                    happiness = 90.0f,
                    hunger = 10.0f,
                    age = 1,
                    produceProgress = 0.1f,
                    readyToHarvest = false,
                    posX = 4.5f,
                    posY = 0.0f,
                    posZ = 1.5f,
                    targetX = 4.5f,
                    targetZ = 1.5f
                ),
                LivestockEntity(
                    id = 11,
                    type = LivestockType.CHICKEN,
                    name = "Henrietta",
                    happiness = 95.0f,
                    hunger = 5.0f,
                    age = 1,
                    produceProgress = 0.4f,
                    readyToHarvest = false,
                    posX = 7.5f,
                    posY = 0.0f,
                    posZ = 2.0f,
                    targetX = 7.5f,
                    targetZ = 2.0f
                ),
                LivestockEntity(
                    id = 12,
                    type = LivestockType.CHICKEN,
                    name = "Eggatha",
                    happiness = 85.0f,
                    hunger = 15.0f,
                    age = 2,
                    produceProgress = 0.6f,
                    readyToHarvest = false,
                    posX = 5.0f,
                    posY = 0.0f,
                    posZ = -2.5f,
                    targetX = 5.0f,
                    targetZ = -2.5f
                ),
                LivestockEntity(
                    id = 13,
                    type = LivestockType.CHICKEN,
                    name = "Peep",
                    happiness = 90.0f,
                    hunger = 12.0f,
                    age = 1,
                    produceProgress = 0.3f,
                    readyToHarvest = false,
                    posX = 8.5f,
                    posY = 0.0f,
                    posZ = 1.0f,
                    targetX = 8.5f,
                    targetZ = 1.0f
                ),
                LivestockEntity(
                    id = 14,
                    type = LivestockType.CHICKEN,
                    name = "Penny",
                    happiness = 80.0f,
                    hunger = 18.0f,
                    age = 1,
                    produceProgress = 0.2f,
                    readyToHarvest = false,
                    posX = 6.0f,
                    posY = 0.0f,
                    posZ = 3.5f,
                    targetX = 6.0f,
                    targetZ = 3.5f
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
                InventoryEntity("tool_fishing_rod_basic", "Basic Fishing Rod", ItemCategory.TOOL, 1, 25),
                InventoryEntity("material_hardwood", "Hardwood Timber", ItemCategory.CRAFTED, 6, 15),
                InventoryEntity("material_fiber", "Plant Fiber", ItemCategory.CRAFTED, 8, 10),
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

            // Seed Initial Story Missions (12 Missions)
            val initialMissions = listOf(
                StoryMissionEntity("mission_1", 1, "First Light", 0, 1, isCompleted = false, isUnlocked = true),
                StoryMissionEntity("mission_2", 2, "Seeds of Hope", 0, 5, isCompleted = false, isUnlocked = false),
                StoryMissionEntity("mission_3", 3, "Water of Life", 0, 5, isCompleted = false, isUnlocked = false),
                StoryMissionEntity("mission_4", 4, "The Old Farm", 0, 1, isCompleted = false, isUnlocked = false),
                StoryMissionEntity("mission_5", 5, "Power of the Sun", 0, 1, isCompleted = false, isUnlocked = false),
                StoryMissionEntity("mission_6", 6, "Signals", 0, 1, isCompleted = false, isUnlocked = false),
                StoryMissionEntity("mission_7", 7, "The Researcher", 0, 1, isCompleted = false, isUnlocked = false),
                StoryMissionEntity("mission_8", 8, "Wildlife Returns", 0, 75, isCompleted = false, isUnlocked = false),
                StoryMissionEntity("mission_9", 9, "The Facility", 0, 1, isCompleted = false, isUnlocked = false),
                StoryMissionEntity("mission_10", 10, "Power Grid", 0, 10, isCompleted = false, isUnlocked = false),
                StoryMissionEntity("mission_11", 11, "Community", 0, 5, isCompleted = false, isUnlocked = false),
                StoryMissionEntity("mission_12", 12, "New Dawn", 0, 1, isCompleted = false, isUnlocked = false)
            )
            dao.insertStoryMissions(initialMissions)

            // Seed Initial 15 Points of Interest
            val initialPois = com.example.data.model.PointOfInterestType.values().map { poi ->
                PoiEntity(
                    id = poi.id,
                    name = poi.displayName,
                    biomeId = poi.biome.id,
                    posX = poi.worldX,
                    posZ = poi.worldZ,
                    isDiscovered = (poi.id == "poi_old_farm")
                )
            }
            dao.insertPois(initialPois)

            // Seed Initial 10 Lore Entries
            val initialLore = listOf(
                LoreEntryEntity("lore_1", "Log 01: Arriving at Sector 4", "Previous Settlers", "SETTLER_DIARY", "Humne Socha tha SDZ Solaria city project clean power and perma-culture bed se self-sufficient banega. Par grid trip hone ke baad sab bhag gaye...", isUnlocked = true),
                LoreEntryEntity("lore_2", "Old Farm Diary", "Dadi Solaria", "SETTLER_DIARY", "Soil here in Green Valley is fertile if watered with purified stream water. Crops like wheat and corn respond best to bio-compost...", isUnlocked = true),
                LoreEntryEntity("lore_3", "SDZ Research Log: Closed Loop Microgrid", "Dr. Ananya", "SDZ_SCIENTIFIC_LOG", "Testing solar glass and micro-hydro wheels. Storage efficiency is key to surviving winter storms...", isUnlocked = false),
                LoreEntryEntity("lore_4", "Facility Core Log", "Chief Engineer", "SDZ_SCIENTIFIC_LOG", "Emergency protocol ALPHA-9 initiated. Hydro-electric dam and geothermal vents locked down. Master override password: SOLARIS.", isUnlocked = false),
                LoreEntryEntity("lore_5", "Radio Signal Intercept", "Outpost 3", "RADIO_BROADCAST", "This is Outpost 3 broadcasting on emergency freq 104.5 MHz... If anyone is receiving this signal near Deep Forest, clean energy can rebuild our valley.", isUnlocked = false),
                LoreEntryEntity("lore_6", "Ancient Banyan Inscription", "Eco-Guardians", "SETTLER_DIARY", "Puraane buzurg kehte hain: Har ped ek jeevan hai. Clean soil and water bring back wild birds and deer...", isUnlocked = false),
                LoreEntryEntity("lore_7", "Stone Circle Notes", "Surveyor Marcus", "SDZ_SCIENTIFIC_LOG", "Subterranean magnetic resonance detected. Energy grid efficiency increased by harmonic alignment...", isUnlocked = false),
                LoreEntryEntity("lore_8", "Fisherman's Field Manual", "Old Angler", "SETTLER_DIARY", "Best fishing spots are near the Wetland river bend during evening dusk. Golden lures attract rare bio-fish...", isUnlocked = false),
                LoreEntryEntity("lore_9", "Ranger Survey 2038", "Ranger Dave", "SDZ_SCIENTIFIC_LOG", "Wildlife returning to Northern Green Valley as carbon levels drop below 100 ppm...", isUnlocked = false),
                LoreEntryEntity("lore_10", "Foundational Creed: New Dawn", "Rebuilders Guild", "SETTLER_DIARY", "We didn't just rebuild structures. We rebuilt harmony between human technology and nature.", isUnlocked = false)
            )
            dao.insertLoreEntries(initialLore)

            // Seed Initial 5 Terminal Logs
            val initialTerminals = listOf(
                TerminalLogEntity("term_1", "Security Terminal A1", "Research Facility Entrance", isHacked = true),
                TerminalLogEntity("term_2", "Agronomy Lab Terminal", "Facility Bio-Lab", isHacked = true),
                TerminalLogEntity("term_3", "Geothermal Core Control", "Deep Facility Vault", isHacked = false),
                TerminalLogEntity("term_4", "Drone Network Hub", "Communications Deck", isHacked = true),
                TerminalLogEntity("term_5", "Civilization Archive", "SDZ Command Room", isHacked = false)
            )
            dao.insertTerminalLogs(initialTerminals)
        }
    }

    suspend fun saveGameState(state: GameStateEntity) {
        dao.saveGameState(state)
    }

    suspend fun saveGameSnapshot(
        playerX: Float,
        playerY: Float,
        playerZ: Float,
        playerAngle: Float,
        discoveredPois: String? = null,
        discoveredChunks: String? = null,
        discoveredBiomes: String? = null,
        currentBiomeId: String? = null
    ) {
        val current = dao.getGameState().firstOrNull() ?: return
        dao.saveGameState(
            current.copy(
                playerX = playerX,
                playerY = playerY,
                playerZ = playerZ,
                playerAngle = playerAngle,
                discoveredPois = discoveredPois ?: current.discoveredPois,
                discoveredChunks = discoveredChunks ?: current.discoveredChunks,
                discoveredBiomes = discoveredBiomes ?: current.discoveredBiomes,
                currentBiomeId = currentBiomeId ?: current.currentBiomeId
            )
        )
    }

    suspend fun discoverBiome(biome: com.example.data.model.BiomeType): Boolean {
        val current = dao.getGameState().firstOrNull() ?: return false
        val existingBiomes = current.discoveredBiomes.split(",").filter { it.isNotBlank() }.toSet()
        if (existingBiomes.contains(biome.id)) return false

        val updatedBiomes = (existingBiomes + biome.id).joinToString(",")
        val bonusCoins = 50
        val bonusEco = 10

        dao.saveGameState(
            current.copy(
                solCoins = current.solCoins + bonusCoins,
                ecoPrestige = current.ecoPrestige + bonusEco,
                discoveredBiomes = updatedBiomes,
                currentBiomeId = biome.id
            )
        )
        return true
    }

    suspend fun discoverPoi(poi: com.example.data.model.PointOfInterestType): Boolean {
        val current = dao.getGameState().firstOrNull() ?: return false
        val existingPois = current.discoveredPois.split(",").filter { it.isNotBlank() }.toSet()
        if (existingPois.contains(poi.id)) return false

        val updatedPois = (existingPois + poi.id).joinToString(",")
        val updatedCoins = current.solCoins + poi.rewardCoins
        val updatedEco = current.ecoPrestige + poi.rewardEcoScore
        val rpBonus = if (poi.id == "poi_ancient_tree") 50 else 15
        val updatedRp = current.researchPoints + rpBonus

        // Add reward item to inventory
        val rewardItemId = "reward_${poi.id}"
        val existingItem = dao.getInventoryItem(rewardItemId)
        if (existingItem != null) {
            dao.insertInventory(existingItem.copy(quantity = existingItem.quantity + 1))
        } else {
            dao.insertInventory(
                InventoryEntity(
                    itemId = rewardItemId,
                    name = poi.rewardItemName,
                    category = ItemCategory.CRAFTED,
                    quantity = 1,
                    sellValue = 40
                )
            )
        }

        // Update POI table
        dao.updatePoi(
            PoiEntity(
                id = poi.id,
                name = poi.displayName,
                biomeId = poi.biome.id,
                posX = poi.worldX,
                posZ = poi.worldZ,
                isDiscovered = true
            )
        )

        dao.saveGameState(
            current.copy(
                solCoins = updatedCoins,
                ecoPrestige = updatedEco,
                researchPoints = updatedRp,
                discoveredPois = updatedPois
            )
        )
        return true
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

        val produceItemId = when (animal.type) {
            LivestockType.CHICKEN -> "harvest_egg"
            LivestockType.CYBER_BOVINE -> "harvest_milk"
            else -> animal.type.productProduced.lowercase().replace(" ", "_")
        }
        val produceName = when (animal.type) {
            LivestockType.CHICKEN -> "Egg"
            LivestockType.CYBER_BOVINE -> "Organic Bio-Milk"
            else -> animal.type.productProduced
        }

        val existingItem = dao.getInventoryItem(produceItemId)
        val newQty = (existingItem?.quantity ?: 0) + 1

        dao.insertInventory(
            InventoryEntity(
                itemId = produceItemId,
                name = produceName,
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
        return produceName
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
        if (!isDevicePowered(DeviceType.WORKSHOP)) return false
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
        var hungerBoost = 30.0f
        if (foodItemId != null) {
            val item = dao.getInventoryItem(foodItemId)
            if (item != null && item.quantity > 0) {
                itemName = item.name
                dao.insertInventory(item.copy(quantity = item.quantity - 1))
                if (foodItemId == "harvest_egg" || foodItemId.contains("egg")) {
                    hungerBoost = 15.0f // eggs restore +15 hunger
                }
            } else {
                return Pair(false, "No $foodItemId available")
            }
        } else {
            // Find any harvested edible produce in inventory (prefer eggs)
            val allInv = dao.getAllInventory().firstOrNull() ?: emptyList()
            val eggProduce = allInv.find { it.quantity > 0 && (it.itemId == "harvest_egg" || it.itemId.contains("egg")) }
            val edibleProduce = eggProduce ?: allInv.find { 
                it.quantity > 0 && (it.category == ItemCategory.PRODUCE || it.itemId.startsWith("harvest_")) 
            }
            if (edibleProduce != null) {
                itemName = edibleProduce.name
                dao.insertInventory(edibleProduce.copy(quantity = edibleProduce.quantity - 1))
                if (edibleProduce.itemId == "harvest_egg" || edibleProduce.itemId.contains("egg")) {
                    hungerBoost = 15.0f
                }
            } else {
                return Pair(false, "No food/eggs in inventory")
            }
        }

        val newHunger = min(state.maxHunger, state.hunger + hungerBoost)
        val newHealth = min(state.maxHealth, state.health + 10.0f)
        val newStamina = min(state.maxStamina, state.stamina + 15.0f)

        dao.saveGameState(
            state.copy(
                hunger = newHunger,
                health = newHealth,
                stamina = newStamina
            )
        )
        return Pair(true, "Ate $itemName (+$hungerBoost Hunger, +10 HP)")
    }

    suspend fun drinkWater(drinkItemId: String? = null, source: String = "Canteen"): Pair<Boolean, String> {
        val state = dao.getGameState().firstOrNull() ?: return Pair(false, "Game state unavailable")
        
        var drinkName = source
        var thirstBoost = 40.0f
        
        if (drinkItemId != null) {
            val item = dao.getInventoryItem(drinkItemId)
            if (item != null && item.quantity > 0) {
                drinkName = item.name
                dao.insertInventory(item.copy(quantity = item.quantity - 1))
                if (drinkItemId == "harvest_organic bio-milk" || drinkItemId.contains("milk")) {
                    thirstBoost = 25.0f // milk restores +25 thirst
                }
            } else {
                return Pair(false, "No $drinkItemId available")
            }
        } else {
            // Check if player has organic milk in inventory to drink first
            val allInv = dao.getAllInventory().firstOrNull() ?: emptyList()
            val milkItem = allInv.find { it.quantity > 0 && (it.itemId.contains("milk")) }
            if (milkItem != null) {
                drinkName = milkItem.name
                dao.insertInventory(milkItem.copy(quantity = milkItem.quantity - 1))
                thirstBoost = 25.0f
            }
        }

        val newThirst = min(state.maxThirst, state.thirst + thirstBoost)
        val newStamina = min(state.maxStamina, state.stamina + 15.0f)

        dao.saveGameState(
            state.copy(
                thirst = newThirst,
                stamina = newStamina
            )
        )
        return Pair(true, "Drank $drinkName (+$thirstBoost Thirst)")
    }

    suspend fun drinkCleanWater(sourceName: String = "Well"): Pair<Boolean, String> {
        val state = dao.getGameState().firstOrNull() ?: return Pair(false, "Game state unavailable")
        val thirstBoost = 40.0f
        val newThirst = min(state.maxThirst, state.thirst + thirstBoost)
        val newStamina = min(state.maxStamina, state.stamina + 20.0f)
        dao.saveGameState(
            state.copy(
                thirst = newThirst,
                stamina = newStamina
            )
        )
        return Pair(true, "Drank Clean Water from $sourceName (+40 Thirst)")
    }

    suspend fun drinkRawWater(sourceName: String = "Stream"): Pair<Boolean, String> {
        val state = dao.getGameState().firstOrNull() ?: return Pair(false, "Game state unavailable")
        val thirstBoost = 30.0f
        val newThirst = min(state.maxThirst, state.thirst + thirstBoost)
        // Causes Sickness for 2 game hours
        dao.saveGameState(
            state.copy(
                thirst = newThirst,
                isSick = true,
                sicknessRemainingHours = 2.0f
            )
        )
        return Pair(true, "Drank Raw Water from $sourceName (+30 Thirst) ⚠️ Contaminated! You got Sick for 2 hrs.")
    }

    suspend fun drinkPurifiedWater(sourceName: String = "Purifier"): Pair<Boolean, String> {
        val state = dao.getGameState().firstOrNull() ?: return Pair(false, "Game state unavailable")
        val thirstBoost = 45.0f
        val newThirst = min(state.maxThirst, state.thirst + thirstBoost)
        val newStamina = min(state.maxStamina, state.stamina + 25.0f)
        val newHealth = min(state.maxHealth, state.health + 5.0f)
        dao.saveGameState(
            state.copy(
                thirst = newThirst,
                stamina = newStamina,
                health = newHealth
            )
        )
        return Pair(true, "Drank Purified Water (+45 Thirst, +5 HP)")
    }

    suspend fun drinkCarriedWater(isClean: Boolean): Pair<Boolean, String> {
        val state = dao.getGameState().firstOrNull() ?: return Pair(false, "Game state unavailable")
        if (isClean) {
            if (state.cleanWaterCarried <= 0) return Pair(false, "No clean water carried")
            val newThirst = min(state.maxThirst, state.thirst + 40.0f)
            dao.saveGameState(
                state.copy(
                    thirst = newThirst,
                    cleanWaterCarried = state.cleanWaterCarried - 1
                )
            )
            return Pair(true, "Drank Clean Water from Flask (+40 Thirst)")
        } else {
            if (state.rawWaterCarried <= 0) return Pair(false, "No raw water carried")
            val newThirst = min(state.maxThirst, state.thirst + 30.0f)
            dao.saveGameState(
                state.copy(
                    thirst = newThirst,
                    rawWaterCarried = state.rawWaterCarried - 1,
                    isSick = true,
                    sicknessRemainingHours = 2.0f
                )
            )
            return Pair(true, "Drank Raw Water from Flask (+30 Thirst) ⚠️ Contaminated! You got Sick.")
        }
    }

    suspend fun collectWater(isRaw: Boolean, sourceName: String): Pair<Boolean, String> {
        val state = dao.getGameState().firstOrNull() ?: return Pair(false, "Game state unavailable")
        val currentCarried = state.cleanWaterCarried + state.rawWaterCarried
        if (currentCarried >= state.maxWaterCarried) {
            return Pair(false, "Water flask full (${state.maxWaterCarried}/${state.maxWaterCarried})")
        }
        val newClean = if (isRaw) state.cleanWaterCarried else state.cleanWaterCarried + 1
        val newRaw = if (isRaw) state.rawWaterCarried + 1 else state.rawWaterCarried
        dao.saveGameState(
            state.copy(
                cleanWaterCarried = newClean,
                rawWaterCarried = newRaw
            )
        )
        val waterType = if (isRaw) "Raw Water" else "Clean Water"
        return Pair(true, "Collected 1 unit of $waterType from $sourceName (${newClean + newRaw}/${state.maxWaterCarried})")
    }

    suspend fun boilWater(): Pair<Boolean, String> {
        val state = dao.getGameState().firstOrNull() ?: return Pair(false, "Game state unavailable")
        if (state.rawWaterCarried <= 0) {
            return Pair(false, "No raw water carried to boil")
        }
        dao.saveGameState(
            state.copy(
                rawWaterCarried = state.rawWaterCarried - 1,
                cleanWaterCarried = min(state.maxWaterCarried, state.cleanWaterCarried + 1)
            )
        )
        return Pair(true, "Boiled 1 unit of Raw Water → Clean Water!")
    }

    suspend fun fillFilter(buildingId: Int): Pair<Boolean, String> {
        val state = dao.getGameState().firstOrNull() ?: return Pair(false, "Game state unavailable")
        if (state.rawWaterCarried <= 0) {
            return Pair(false, "No raw water carried to fill filter")
        }
        val buildings = dao.getAllPlacedBuildings().firstOrNull() ?: emptyList()
        val filter = buildings.find { it.id == buildingId } ?: return Pair(false, "Filter not found")
        val depositAmount = state.rawWaterCarried
        val newStored = min(50.0f, filter.waterStored + depositAmount)
        dao.updatePlacedBuilding(filter.copy(waterStored = newStored))
        dao.saveGameState(state.copy(rawWaterCarried = 0))
        return Pair(true, "Deposited $depositAmount raw water into Water Filter (${newStored.toInt()}/50)")
    }

    suspend fun collectFromBuilding(buildingId: Int): Pair<Boolean, String> {
        val state = dao.getGameState().firstOrNull() ?: return Pair(false, "Game state unavailable")
        val currentCarried = state.cleanWaterCarried + state.rawWaterCarried
        if (currentCarried >= state.maxWaterCarried) {
            return Pair(false, "Water flask full (${state.maxWaterCarried}/${state.maxWaterCarried})")
        }
        val buildings = dao.getAllPlacedBuildings().firstOrNull() ?: emptyList()
        val b = buildings.find { it.id == buildingId } ?: return Pair(false, "Building not found")
        if (b.waterStored < 1.0f) {
            return Pair(false, "No water stored in ${b.buildingType.displayName}")
        }
        val maxCanTake = state.maxWaterCarried - currentCarried
        val takeAmount = min(maxCanTake.toFloat(), b.waterStored).toInt()
        if (takeAmount <= 0) return Pair(false, "Flask is full")

        dao.updatePlacedBuilding(b.copy(waterStored = b.waterStored - takeAmount))
        dao.saveGameState(state.copy(cleanWaterCarried = state.cleanWaterCarried + takeAmount))
        return Pair(true, "Collected $takeAmount clean water from ${b.buildingType.displayName}")
    }

    suspend fun cureSickness(cureMethod: String = "Medicine"): Pair<Boolean, String> {
        val state = dao.getGameState().firstOrNull() ?: return Pair(false, "Game state unavailable")
        dao.saveGameState(
            state.copy(
                isSick = false,
                sicknessRemainingHours = 0.0f,
                health = min(state.maxHealth, state.health + 20.0f)
            )
        )
        return Pair(true, "Cured Sickness via $cureMethod!")
    }

    suspend fun drinkHerbalTea(): Pair<Boolean, String> {
        val state = dao.getGameState().firstOrNull() ?: return Pair(false, "Game state unavailable")
        if (state.cleanWaterCarried <= 0) {
            return Pair(false, "Need 1 unit Clean Water to brew Herbal Tea")
        }
        val herbItem = dao.getInventoryItem("seed_herbs") ?: dao.getInventoryItem("harvest_herbs")
        if (herbItem != null && herbItem.quantity > 0) {
            dao.insertInventory(herbItem.copy(quantity = herbItem.quantity - 1))
        }
        dao.saveGameState(
            state.copy(
                cleanWaterCarried = state.cleanWaterCarried - 1,
                thirst = min(state.maxThirst, state.thirst + 40.0f),
                health = min(state.maxHealth, state.health + 25.0f),
                isSick = false,
                sicknessRemainingHours = 0.0f
            )
        )
        return Pair(true, "Brewed & Drank Herbal Tea! (+40 Thirst, +25 HP, Sickness Cured)")
    }

    suspend fun catchFish(spot: FishingSpotType, rodTier: FishingRodTier): Pair<FishType?, String> {
        val state = dao.getGameState().firstOrNull() ?: return Pair(null, "No game state")
        val currentPop = when (spot) {
            FishingSpotType.GREEN_VALLEY_STREAM -> state.fishPopStream
            FishingSpotType.WETLAND_RIVER -> state.fishPopRiver
            FishingSpotType.WETLAND_POND -> state.fishPopPond
        }
        if (currentPop <= 0) {
            return Pair(null, "No fish here. The waters are depleted for today! (Regens 1/day)")
        }

        val newStreamPop = if (spot == FishingSpotType.GREEN_VALLEY_STREAM) currentPop - 1 else state.fishPopStream
        val newRiverPop = if (spot == FishingSpotType.WETLAND_RIVER) currentPop - 1 else state.fishPopRiver
        val newPondPop = if (spot == FishingSpotType.WETLAND_POND) currentPop - 1 else state.fishPopPond

        val rareRoll = Random.nextInt(100)
        val rareThreshold = 5 + rodTier.rareBonusPct
        val caughtFish = when {
            rareRoll < rareThreshold -> FishType.GOLDEN_FISH
            rareRoll < 25 -> FishType.CARP
            rareRoll < 50 -> FishType.CATFISH
            rareRoll < 75 -> FishType.BASS
            else -> FishType.TROUT
        }

        val doubleCatch = rodTier.doubleCatchChance > 0f && Random.nextFloat() < rodTier.doubleCatchChance
        val catchQty = if (doubleCatch) 2 else 1

        val itemId = "fish_${caughtFish.name.lowercase()}"
        val existing = dao.getInventoryItem(itemId)
        val newQty = (existing?.quantity ?: 0) + catchQty
        dao.insertInventory(
            InventoryEntity(
                itemId = itemId,
                name = caughtFish.displayName,
                category = ItemCategory.HARVEST,
                quantity = newQty,
                sellValue = caughtFish.sellPrice
            )
        )

        dao.saveGameState(
            state.copy(
                fishPopStream = newStreamPop,
                fishPopRiver = newRiverPop,
                fishPopPond = newPondPop,
                solCoins = state.solCoins + 10,
                ecoPrestige = state.ecoPrestige + 5
            )
        )

        val doubleMsg = if (doubleCatch) " 🌟 DOUBLE CATCH! (2x ${caughtFish.displayName})" else ""
        return Pair(caughtFish, "Caught $catchQty ${caughtFish.displayName}! (Sell: $${caughtFish.sellPrice})$doubleMsg")
    }

    suspend fun cookFish(fishItemId: String): Pair<Boolean, String> {
        val item = dao.getInventoryItem(fishItemId) ?: return Pair(false, "No fish to cook")
        if (item.quantity <= 0) return Pair(false, "No fish available")
        val cookedItemId = "food_cooked_${fishItemId.removePrefix("fish_")}"
        val cookedName = "Grilled ${item.name}"
        dao.insertInventory(item.copy(quantity = item.quantity - 1))
        val existingCooked = dao.getInventoryItem(cookedItemId)
        val newQty = (existingCooked?.quantity ?: 0) + 1
        dao.insertInventory(
            InventoryEntity(
                itemId = cookedItemId,
                name = cookedName,
                category = ItemCategory.PRODUCE,
                quantity = newQty,
                sellValue = item.sellValue + 15
            )
        )
        return Pair(true, "Cooked $cookedName at campfire (+Safe to eat, restores hunger)")
    }

    suspend fun eatFish(fishItemId: String, isRaw: Boolean): Pair<Boolean, String> {
        val state = dao.getGameState().firstOrNull() ?: return Pair(false, "Game state unavailable")
        val item = dao.getInventoryItem(fishItemId) ?: return Pair(false, "No fish available")
        if (item.quantity <= 0) return Pair(false, "No fish in inventory")

        dao.insertInventory(item.copy(quantity = item.quantity - 1))
        val hungerBoost = if (isRaw) 20.0f else 35.0f
        var sickApplied = false
        if (isRaw && Random.nextFloat() < 0.10f) {
            sickApplied = true
        }

        dao.saveGameState(
            state.copy(
                hunger = min(state.maxHunger, state.hunger + hungerBoost),
                isSick = if (sickApplied) true else state.isSick,
                sicknessRemainingHours = if (sickApplied) 2.0f else state.sicknessRemainingHours
            )
        )
        val sickMsg = if (sickApplied) " ⚠️ Raw fish caused sickness!" else ""
        return Pair(true, "Ate ${item.name} (+$hungerBoost Hunger)$sickMsg")
    }

    suspend fun upgradeFishingRod(): Pair<Boolean, String> {
        val state = dao.getGameState().firstOrNull() ?: return Pair(false, "No game state")
        val currentTier = state.fishingRodTier
        val nextTier = when (currentTier) {
            0 -> FishingRodTier.BASIC
            1 -> FishingRodTier.IMPROVED
            2 -> FishingRodTier.MASTER
            else -> null
        } ?: return Pair(false, "Already at Master Rod tier!")

        if (state.solCoins < nextTier.upgradeCost) {
            return Pair(false, "Need $${nextTier.upgradeCost} 🪙 to upgrade to ${nextTier.displayName}")
        }

        dao.saveGameState(
            state.copy(
                solCoins = state.solCoins - nextTier.upgradeCost,
                fishingRodTier = nextTier.tier
            )
        )
        return Pair(true, "Upgraded to ${nextTier.displayName}!")
    }

    suspend fun runAutoIrrigation(): Int {
        if (!isDevicePowered(DeviceType.WATER_PUMP)) {
            return 0
        }
        val plots = dao.getAllPlots().firstOrNull() ?: emptyList()
        val buildings = dao.getAllPlacedBuildings().firstOrNull() ?: emptyList()
        val nodes = buildings.filter { it.buildingType == BuildableType.IRRIGATION_NODE }
        if (nodes.isEmpty()) return 0

        var hasInfiniteWell = buildings.any { it.buildingType == BuildableType.WELL }
        var wateredCount = 0

        val updatedPlots = mutableListOf<PlotEntity>()
        val updatedBuildings = buildings.toMutableList()

        for (node in nodes) {
            var waterAvailable = hasInfiniteWell
            if (!waterAvailable) {
                val waterSource = updatedBuildings.find {
                    (it.buildingType == BuildableType.RAIN_BARREL ||
                     it.buildingType == BuildableType.WATER_PURIFIER ||
                     it.buildingType == BuildableType.WATER_STORAGE_SHED ||
                     it.buildingType == BuildableType.WATER_FILTER) && it.waterStored >= 5.0f
                }
                if (waterSource != null) {
                    val idx = updatedBuildings.indexOf(waterSource)
                    updatedBuildings[idx] = waterSource.copy(waterStored = waterSource.waterStored - 5.0f)
                    waterAvailable = true
                }
            }

            if (waterAvailable) {
                for (plot in plots) {
                    val dx = plot.posX - node.posX
                    val dz = plot.posZ - node.posZ
                    val dist = kotlin.math.sqrt(dx * dx + dz * dz)
                    if (dist <= 3.5f && plot.moisture < 0.9f) {
                        updatedPlots.add(plot.copy(moisture = 1.0f, autoIrrigated = true))
                        wateredCount++
                    }
                }
            }
        }

        if (updatedPlots.isNotEmpty()) {
            dao.insertPlots(updatedPlots)
        }
        dao.insertPlacedBuildings(updatedBuildings)
        return wateredCount
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

        // Deduct coins only (no materials required)
        val newCoins = state.solCoins - type.costCoins

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
        val extraCapacity = if (type == BuildableType.STORAGE) 50.0f else 0.0f
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
                isSick = false,
                sicknessRemainingHours = 0.0f,
                gameTimeHour = newHour,
                gameTimeDay = newDay
            )
        )
        return Pair(true, "Rested at Farmhouse (+Full Stamina, +30 HP, Sickness Cured)")
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

        // 3. Sickness & Survival Stats Simulation
        val isSick = state.isSick
        var sicknessRemaining = state.sicknessRemainingHours
        var currentIsSick = isSick
        if (isSick) {
            sicknessRemaining = max(0.0f, sicknessRemaining - timeAdvanceHours)
            if (sicknessRemaining <= 0.0f) {
                currentIsSick = false
            }
        }

        // A. Hunger: decreases by 1 every 30 seconds (1/30 per sec)
        val hungerDrainRate = 1.0f / 30.0f
        val newHunger = max(0.0f, state.hunger - hungerDrainRate * deltaSec)

        // B. Thirst: decreases by 1 every 20 seconds (1/20 per sec), 2x faster during HEATWAVE
        val thirstMultiplier = if (currentWeather == WeatherType.HEATWAVE) 2.0f else 1.0f
        val thirstDrainRate = (1.0f / 20.0f) * thirstMultiplier
        val newThirst = max(0.0f, state.thirst - thirstDrainRate * deltaSec)

        // C. Stamina:
        // Sickness causes stamina to drain 2x faster and regen is halved
        val staminaDrainMultiplier = if (currentIsSick) 2.0f else 1.0f
        val staminaRegenRate = if (currentIsSick) 1.0f else 2.0f
        val newStamina = when {
            isSprinting -> max(0.0f, state.stamina - (3.0f * staminaDrainMultiplier) * deltaSec)
            isMoving -> max(0.0f, state.stamina - (1.0f * staminaDrainMultiplier) * deltaSec)
            else -> min(state.maxStamina, state.stamina + staminaRegenRate * deltaSec)
        }

        // D. Health:
        // Sickness drops health by 1 every 30 sec
        val sicknessDamage = if (currentIsSick) (1.0f / 30.0f) * deltaSec else 0.0f
        var newHealth = max(0.0f, state.health - sicknessDamage)
        if (newHunger <= 0.0f || newThirst <= 0.0f) {
            val starvationDmg = if (newHunger <= 0.0f) (1.0f / 10.0f) else 0.0f
            val dehydrationDmg = if (newThirst <= 0.0f) (2.0f / 10.0f) else 0.0f
            newHealth = max(0.0f, newHealth - (starvationDmg + dehydrationDmg) * deltaSec)
        } else if (newHunger >= 70.0f && newThirst >= 70.0f && newHealth < state.maxHealth && !currentIsSick) {
            newHealth = min(state.maxHealth, newHealth + 0.5f * deltaSec)
        }

        // Determine Solar & Wind Factors
        // Solar panels generate energy only during day (7:00 to 17:00)
        val isDay = newHour in 7.0f..17.0f
        val solarMultiplier = if (isDay) currentWeather.solarMultiplier else 0.0f
        // Windmill works 24/7 but slower at night (30% less, so 0.70x multiplier)
        val isNightTime = newHour >= 19.0f || newHour < 5.0f
        val windMultiplier = if (isNightTime) (currentWeather.windMultiplier * 0.70f) else currentWeather.windMultiplier

        // 4. Clean Energy Generation & Battery Storage Calculation
        var totalGenKwhPerDay = 0.0f
        var currentGenKw = 0.0f
        var totalCapacity = 50.0f // Farmhouse base battery: 50 kWh
        var totalMaxChargeRateKw = 5.0f // Base charge rate

        var powerSourcesCount = 0
        var batteryUnitsCount = 1 // Base battery
        var powerPolesCount = 0

        val placedList = dao.getAllPlacedBuildings().firstOrNull() ?: emptyList()

        // Energy Nodes
        energyList.forEach { node ->
            if (node.isActive) {
                when (node.nodeType) {
                    EnergyNodeType.PHOTOVOLTAIC_ARRAY -> {
                        powerSourcesCount++
                        val daily = 15.0f * node.efficiency
                        totalGenKwhPerDay += daily
                        currentGenKw += (daily / 10.0f) * solarMultiplier
                    }
                    EnergyNodeType.VERTICAL_WIND_TURBINE -> {
                        powerSourcesCount++
                        val daily = 18.0f * node.efficiency
                        totalGenKwhPerDay += daily
                        currentGenKw += (daily / 24.0f) * windMultiplier
                    }
                    EnergyNodeType.BIOGAS_DIGESTER -> {
                        powerSourcesCount++
                        val daily = 12.0f * node.efficiency
                        totalGenKwhPerDay += daily
                        currentGenKw += (daily / 24.0f)
                    }
                    EnergyNodeType.BATTERY_STORAGE_BANK -> {
                        batteryUnitsCount++
                        totalCapacity += node.nodeType.baseStorageCapacityKwh * node.level
                        totalMaxChargeRateKw += 10.0f * node.level
                    }
                }
            }
        }

        // Placed Buildings Energy Sources & Batteries
        placedList.forEach { building ->
            when (building.buildingType) {
                BuildableType.SOLAR_PANEL -> {
                    powerSourcesCount++
                    totalGenKwhPerDay += 10.0f
                    // 10 kWh/day during daylight 7 AM - 5 PM (10 hrs) => 1.0 kW
                    currentGenKw += 1.0f * solarMultiplier
                }
                BuildableType.WINDMILL -> {
                    powerSourcesCount++
                    totalGenKwhPerDay += 15.0f
                    // 15 kWh/day 24/7 (30% less at night)
                    currentGenKw += (15.0f / 24.0f) * windMultiplier
                }
                BuildableType.HYDRO_GENERATOR -> {
                    powerSourcesCount++
                    totalGenKwhPerDay += 20.0f
                    // 20 kWh/day 24/7 continuous
                    currentGenKw += (20.0f / 24.0f)
                }
                BuildableType.ADVANCED_SOLAR -> {
                    powerSourcesCount++
                    totalGenKwhPerDay += 30.0f
                    // 30 kWh/day during daylight + 20% tracking efficiency bonus => 3.6 kW
                    currentGenKw += 3.6f * solarMultiplier
                }
                BuildableType.BIOGAS_GENERATOR -> {
                    powerSourcesCount++
                    totalGenKwhPerDay += 12.0f
                    // 12 kWh/day 24/7 continuous
                    currentGenKw += (12.0f / 24.0f)
                }
                BuildableType.GEOTHERMAL_VENT -> {
                    powerSourcesCount++
                    totalGenKwhPerDay += 40.0f
                    // 40 kWh/day 24/7 continuous industrial base-load
                    currentGenKw += (40.0f / 24.0f)
                }
                BuildableType.BASIC_BATTERY -> {
                    batteryUnitsCount++
                    totalCapacity += 50.0f
                    totalMaxChargeRateKw += 5.0f
                }
                BuildableType.ADVANCED_BATTERY -> {
                    batteryUnitsCount++
                    totalCapacity += 150.0f
                    totalMaxChargeRateKw += 15.0f
                }
                BuildableType.BATTERY_BANK -> {
                    batteryUnitsCount++
                    totalCapacity += 500.0f
                    totalMaxChargeRateKw += 50.0f
                }
                BuildableType.STORAGE -> {
                    totalCapacity += 60.0f
                    totalMaxChargeRateKw += 5.0f
                }
                BuildableType.POWER_POLE -> {
                    powerPolesCount++
                }
                else -> {}
            }
        }

        // Priority-Based Device Power Allocation
        val currentStored = state.batteryChargeKwh
        val batteryPct = if (totalCapacity > 0f) (currentStored / totalCapacity).coerceIn(0f, 1f) else 0f

        var totalHourlyConsumption = 0.0f
        var activeDevicesCount = 0
        var unpoweredDevicesCount = 0

        for (dev in DeviceType.values()) {
            val prio = _devicePriorities.value[dev] ?: dev.defaultPriority
            val isPowered = (batteryPct >= prio.thresholdPercent) && currentStored > 0.05f
            if (isPowered) {
                activeDevicesCount++
                totalHourlyConsumption += dev.consumptionKwhPerHour
            } else {
                unpoweredDevicesCount++
            }
        }

        // Check if any priority tier just got disabled
        for (tier in PowerPriority.values()) {
            if (batteryPct < tier.thresholdPercent && !lastDisabledTiers.contains(tier)) {
                lastDisabledTiers.add(tier)
                onPowerLowAlert?.invoke("Power low — ${tier.displayName} devices disabled")
            } else if (batteryPct >= tier.thresholdPercent && lastDisabledTiers.contains(tier)) {
                lastDisabledTiers.remove(tier)
                onPowerLowAlert?.invoke("Power restored — ${tier.displayName} devices active")
            }
        }

        // Net Energy Balance
        val netHourlyFlowKw = currentGenKw - totalHourlyConsumption
        val isCharging = netHourlyFlowKw > 0.0f
        val chargeDelta = if (isCharging) {
            min(totalMaxChargeRateKw * timeAdvanceHours, netHourlyFlowKw * timeAdvanceHours)
        } else {
            netHourlyFlowKw * timeAdvanceHours
        }
        val newCharge = (currentStored + chargeDelta).coerceIn(0.0f, totalCapacity)
        val hoursUntilEmpty = if (netHourlyFlowKw < -0.01f) {
            newCharge / kotlin.math.abs(netHourlyFlowKw)
        } else {
            999.0f
        }

        // Update Energy Grid Summary Flow
        _energySummary.value = EnergyGridSummary(
            totalGenerationKwhPerDay = totalGenKwhPerDay,
            currentGenerationKwhPerHour = currentGenKw,
            totalStorageCapacityKwh = totalCapacity,
            currentStoredKwh = newCharge,
            totalHourlyConsumptionKwh = totalHourlyConsumption,
            netHourlyKwh = netHourlyFlowKw,
            isCharging = isCharging,
            hoursUntilEmpty = hoursUntilEmpty,
            batteryPercent = if (totalCapacity > 0f) newCharge / totalCapacity else 0f,
            activeDevicesCount = activeDevicesCount,
            totalDevicesCount = DeviceType.values().size,
            unpoweredDevicesCount = unpoweredDevicesCount,
            powerSourcesCount = powerSourcesCount,
            batteryUnitsCount = batteryUnitsCount,
            powerPolesCount = powerPolesCount
        )

        dao.saveGameState(
            state.copy(
                gameTimeHour = newHour,
                gameTimeDay = newDay,
                currentWeather = currentWeather,
                weatherChangeCountdownHours = weatherCountdown,
                batteryChargeKwh = newCharge,
                batteryMaxCapacityKwh = totalCapacity,
                hunger = newHunger,
                thirst = newThirst,
                stamina = newStamina,
                health = newHealth,
                isSick = currentIsSick,
                sicknessRemainingHours = sicknessRemaining
            )
        )

        // 4B. Simulate NPC Settlement Survivors (Arrivals, Schedules, Duties, Needs & Morale)
        simulateNpcs(
            deltaSec = deltaSec,
            timeAdvanceHours = timeAdvanceHours,
            newHour = newHour,
            newDay = newDay,
            playerX = state.playerX,
            playerZ = state.playerZ,
            plotsList = plotsList,
            placedList = placedList
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

        // Rain Barrels fill at 20 units/hour during rain (holds up to 100)
        // Water Filter automatically purifies raw water (1 unit / 3 game min = 20 units/hour)
        // Water Purifier automatically purifies water (requires energy from UV purifier device)
        var buildingsChanged = false
        val newPlacedBuildings = placedList.map { b ->
            when (b.buildingType) {
                BuildableType.RAIN_BARREL -> {
                    if (isRaining && b.waterStored < 100.0f) {
                        buildingsChanged = true
                        b.copy(waterStored = min(100.0f, b.waterStored + 20.0f * timeAdvanceHours))
                    } else b
                }
                BuildableType.WATER_FILTER -> {
                    if (b.waterStored < 50.0f) {
                        buildingsChanged = true
                        b.copy(waterStored = min(50.0f, b.waterStored + 20.0f * timeAdvanceHours))
                    } else b
                }
                BuildableType.WATER_PURIFIER -> {
                    val isPurifierPowered = isDevicePowered(DeviceType.WATER_PURIFIER)
                    if (isPurifierPowered && b.waterStored < 100.0f) {
                        buildingsChanged = true
                        b.copy(waterStored = min(100.0f, b.waterStored + 100.0f * timeAdvanceHours))
                    } else b
                }
                BuildableType.WATER_STORAGE_SHED -> {
                    b
                }
                else -> b
            }
        }
        if (buildingsChanged) {
            dao.insertPlacedBuildings(newPlacedBuildings)
        }

        if (crossed6AM) {
            val beeNodes = animalsList.filter { it.type == LivestockType.ROBO_BEE_POLLINATOR }
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
                            (dx * dx + dz * dz) < 25.0f
                        } || plot.plotType == PlotType.BIO_DOME
                        val heaterPowered = isDevicePowered(DeviceType.GREENHOUSE_HEATER)
                        val greenhouseBonus = if (isNearGreenhouse && heaterPowered) 1.3f else 1.0f

                        // Bees effect: crops within 5 units of bee colony grow 20% faster
                        val isNearBees = beeNodes.any { b ->
                            val dx = plot.posX - b.posX
                            val dz = plot.posZ - b.posZ
                            (dx * dx + dz * dz) <= 25.0f // 5 units radius
                        }
                        val beeBonus = if (isNearBees) 1.20f else 1.0f
                        
                        val increment = 1.0f * rainBonus * greenhouseBonus * beeBonus
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

        // 6:00 AM Daily Irrigation and Fish Population Regeneration
        if (crossed6AM) {
            runAutoIrrigation()
            val cur = dao.getGameState().firstOrNull()
            if (cur != null) {
                dao.saveGameState(
                    cur.copy(
                        fishPopStream = min(10, cur.fishPopStream + 1),
                        fishPopRiver = min(10, cur.fishPopRiver + 1),
                        fishPopPond = min(10, cur.fishPopPond + 1)
                    )
                )
            }
        }

        // 5. Update Livestock (State machine: wander, flee, produce creation)
        val playerX = state.playerX
        val playerZ = state.playerZ

        val coopX = 4.5f
        val coopZ = 4.0f

        val updatedAnimals = animalsList.map { animal ->
            var newProdProgress = animal.produceProgress
            var isReady = animal.readyToHarvest

            when (animal.type) {
                LivestockType.CHICKEN -> {
                    // Lay 1 egg every game day (at 6:00 AM, max 3 eggs)
                    if (crossed6AM) {
                        newProdProgress = min(3.0f, animal.produceProgress + 1.0f)
                        isReady = newProdProgress >= 1.0f
                    }
                }
                LivestockType.CYBER_BOVINE -> {
                    // Produce milk every 2 game days (produce rate scaled)
                    val milkRate = (1.0f / (animal.type.produceIntervalSec * 2.0f)) * (animal.happiness / 100.0f)
                    newProdProgress = min(1.0f, animal.produceProgress + milkRate * deltaSec)
                    isReady = newProdProgress >= 1.0f
                }
                else -> {
                    val produceRate = (1.0f / animal.type.produceIntervalSec) * (animal.happiness / 100.0f)
                    newProdProgress = min(1.0f, animal.produceProgress + produceRate * deltaSec)
                    isReady = newProdProgress >= 1.0f
                }
            }

            var posX = animal.posX
            var posZ = animal.posZ
            var targetX = animal.targetX
            var targetZ = animal.targetZ

            val distToPlayer = kotlin.math.sqrt((posX - playerX) * (posX - playerX) + (posZ - playerZ) * (posZ - playerZ))

            // AI Distance Culling (Performance optimization)
            if (distToPlayer <= 30.0f) {
                // Full AI Simulation
                val fleeThreshold = if (animal.type == LivestockType.CHICKEN) 3.0f else if (animal.type == LivestockType.CYBER_BOVINE) 4.0f else 2.5f
                val isFleeing = distToPlayer < fleeThreshold

                if (isFleeing && distToPlayer > 0.05f) {
                    // Run away from player in opposite direction
                    val fleeDirX = (posX - playerX) / distToPlayer
                    val fleeDirZ = (posZ - playerZ) / distToPlayer
                    targetX = (posX + fleeDirX * 3.5f).coerceIn(-22.0f, 22.0f)
                    targetZ = (posZ + fleeDirZ * 3.5f).coerceIn(-22.0f, 22.0f)

                    // Chickens stay within 8 units of coop
                    if (animal.type == LivestockType.CHICKEN) {
                        val dCoop = kotlin.math.sqrt((targetX - coopX) * (targetX - coopX) + (targetZ - coopZ) * (targetZ - coopZ))
                        if (dCoop > 8.0f) {
                            targetX = coopX + (targetX - coopX) / dCoop * 7.5f
                            targetZ = coopZ + (targetZ - coopZ) / dCoop * 7.5f
                        }
                    }

                    // Move faster when fleeing
                    posX += (targetX - posX) * 0.45f * deltaSec
                    posZ += (targetZ - posZ) * 0.45f * deltaSec
                } else {
                    // Normal wandering
                    if (Random.nextFloat() < 0.25f * deltaSec) {
                        if (animal.type == LivestockType.CHICKEN) {
                            // Wander within 8 units of coop
                            val angle = Random.nextFloat() * 6.283f
                            val rad = Random.nextFloat() * 7.5f
                            targetX = coopX + kotlin.math.cos(angle) * rad
                            targetZ = coopZ + kotlin.math.sin(angle) * rad
                        } else {
                            targetX = (animal.posX + (Random.nextFloat() * 6.0f - 3.0f)).coerceIn(-22.0f, 22.0f)
                            targetZ = (animal.posZ + (Random.nextFloat() * 6.0f - 3.0f)).coerceIn(-22.0f, 22.0f)
                        }
                    }
                    val wanderSpeed = if (animal.type == LivestockType.CYBER_BOVINE) 0.08f else 0.15f
                    posX += (targetX - posX) * wanderSpeed * deltaSec
                    posZ += (targetZ - posZ) * wanderSpeed * deltaSec
                }
            } else if (distToPlayer <= 80.0f) {
                // Reduced update rate
                posX += (targetX - posX) * 0.05f * deltaSec
                posZ += (targetZ - posZ) * 0.05f * deltaSec
            }

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

    /**
     * Phase 5: NPC Survivor Settlement Simulation
     * Handles Progressive Arrival Triggers, Daily Schedules, Duties,
     * Food/Water Needs, Housing Capacity, Morale, Wandering AI, and Speech Bubbles.
     */
    suspend fun simulateNpcs(
        deltaSec: Float,
        timeAdvanceHours: Float,
        newHour: Float,
        newDay: Int,
        playerX: Float,
        playerZ: Float,
        plotsList: List<PlotEntity>,
        placedList: List<PlacedBuildingEntity>
    ) {
        val npcList = dao.getAllNpcs().firstOrNull() ?: emptyList()
        val arrivalsList = dao.getAllArrivals().firstOrNull() ?: emptyList()
        val invList = dao.getAllInventory().firstOrNull() ?: emptyList()
        val livestockList = dao.getAllLivestock().firstOrNull() ?: emptyList()

        // 1. NPC Progressive Arrival Triggers
        if (_pendingArrival.value == null) {
            val existingRoles = npcList.map { it.role }.toSet()
            val acceptedRoles = arrivalsList.filter { it.status == "ACCEPTED" }.map { it.role }.toSet()
            val cooldownRoles = arrivalsList.filter { it.status == "REJECTED_COOLDOWN" && it.cooldownDaysRemaining > 0 }.map { it.role }.toSet()

            var candidateRole: NpcRole? = null
            var greeting = ""
            var bgStory = ""
            var candName = ""
            var candTrait = NpcPersonalityTrait.CHEERFUL

            // Trigger 1: Farmer — Day 5, >= 3 growing crops
            val growingCrops = plotsList.count { it.cropType != null }
            if (newDay >= 5 && !existingRoles.contains(NpcRole.FARMER) && !acceptedRoles.contains(NpcRole.FARMER) && !cooldownRoles.contains(NpcRole.FARMER) && growingCrops >= 3) {
                candidateRole = NpcRole.FARMER
                candName = if (Random.nextBoolean()) "Arjun" else "Mira"
                greeting = "Hello traveler! I saw your healthy crops from the valley road. Mind if I join your sanctuary?"
                bgStory = "Master agronomist with expertise in permaculture & soil regeneration."
                candTrait = NpcPersonalityTrait.HARDWORKING
            }
            // Trigger 2: Engineer — Day 10, >= 2 power sources
            else if (newDay >= 10 && !existingRoles.contains(NpcRole.ENGINEER) && !acceptedRoles.contains(NpcRole.ENGINEER) && !cooldownRoles.contains(NpcRole.ENGINEER)) {
                val powerSources = placedList.count {
                    it.buildingType == BuildableType.SOLAR_PANEL ||
                    it.buildingType == BuildableType.WINDMILL ||
                    it.buildingType == BuildableType.HYDRO_GENERATOR ||
                    it.buildingType == BuildableType.ADVANCED_SOLAR ||
                    it.buildingType == BuildableType.BIOGAS_GENERATOR ||
                    it.buildingType == BuildableType.GEOTHERMAL_VENT
                }
                if (powerSources >= 2) {
                    candidateRole = NpcRole.ENGINEER
                    candName = if (Random.nextBoolean()) "Kabir" else "Dev"
                    greeting = "Impressive grid you're building! I can tune your micro-inverters and boost efficiency by 5%."
                    bgStory = "Electrical grid engineer specializing in solarpunk renewable storage."
                    candTrait = NpcPersonalityTrait.SERIOUS
                }
            }
            // Trigger 3: Builder — Day 15, >= 5 placed buildings
            else if (newDay >= 15 && !existingRoles.contains(NpcRole.BUILDER) && !acceptedRoles.contains(NpcRole.BUILDER) && !cooldownRoles.contains(NpcRole.BUILDER) && placedList.size >= 5) {
                candidateRole = NpcRole.BUILDER
                candName = if (Random.nextBoolean()) "Zara" else "Kaelen"
                greeting = "Your settlement is growing fast! Give me a hammer and I'll speed up construction by 50%."
                bgStory = "Architect and timber fabricator skilled in bamboo & bio-timber structures."
                candTrait = NpcPersonalityTrait.CHEERFUL
            }
            // Trigger 4: Researcher — Day 20
            else if (newDay >= 20 && !existingRoles.contains(NpcRole.RESEARCHER) && !acceptedRoles.contains(NpcRole.RESEARCHER) && !cooldownRoles.contains(NpcRole.RESEARCHER)) {
                candidateRole = NpcRole.RESEARCHER
                candName = if (Random.nextBoolean()) "Priya" else "Nisha"
                greeting = "I've been analyzing the ecological revival in this biome. I can generate +2 Research Points daily for your lab!"
                bgStory = "Environmental scientist researching closed-loop ecosystems."
                candTrait = NpcPersonalityTrait.CURIOUS
            }
            // Trigger 5: Medic — Day 30 or 5+ NPCs
            else if (newDay >= 30 && !existingRoles.contains(NpcRole.MEDIC) && !acceptedRoles.contains(NpcRole.MEDIC) && !cooldownRoles.contains(NpcRole.MEDIC)) {
                candidateRole = NpcRole.MEDIC
                candName = if (Random.nextBoolean()) "Elena" else "Marcus"
                greeting = "Greetings! I carry medical supplies and herbal teas. I can heal you and cure any sickness instantly."
                bgStory = "Field physician with deep knowledge of botanical medicine."
                candTrait = NpcPersonalityTrait.KIND
            }

            if (candidateRole != null) {
                _pendingArrival.value = NpcArrivalCandidate(
                    role = candidateRole,
                    name = candName,
                    age = Random.nextInt(22, 38),
                    trait = candTrait,
                    greetingQuote = greeting,
                    arrivalDay = newDay,
                    backgroundStory = bgStory
                )
            }
        }

        // Decrement cooldowns on day change
        if (newHour < 0.1f) {
            val updatedArrivals = arrivalsList.map { arr ->
                if (arr.status == "REJECTED_COOLDOWN" && arr.cooldownDaysRemaining > 0) {
                    arr.copy(cooldownDaysRemaining = arr.cooldownDaysRemaining - 1)
                } else arr
            }
            if (updatedArrivals.isNotEmpty()) {
                dao.insertArrival(updatedArrivals.first())
            }
        }

        // 2. Compute Settlement Housing Capacity & Resource Stocks
        val farmhouseBeds = 2
        val cabinCount = placedList.count { it.buildingType == BuildableType.NPC_CABIN }
        val bunkhouseCount = placedList.count { it.buildingType == BuildableType.BUNKHOUSE }
        val totalBeds = farmhouseBeds + (cabinCount * 2) + (bunkhouseCount * 6)
        val occupiedBeds = npcList.count { it.assignedBedId != null }

        // Food stock in inventory (harvest items + cooked food)
        val foodItems = invList.filter { it.category == ItemCategory.PRODUCE || it.itemId.startsWith("harvest_") || it.itemId.startsWith("cooked_") }
        val totalFoodStock = foodItems.sumOf { it.quantity }

        // Water stock in water buildings & inventory
        val waterInSheds = placedList.filter { it.buildingType == BuildableType.WATER_STORAGE_SHED || it.buildingType == BuildableType.RAIN_BARREL }.sumOf { it.waterStored.toInt() }
        val waterItems = invList.filter { it.itemId == "clean_water" || it.itemId == "herbal_tea" }.sumOf { it.quantity }
        val totalWaterStock = waterInSheds + waterItems

        val dailyFoodCons = npcList.size * 3
        val dailyWaterCons = npcList.size * 2
        val daysOfFood = if (dailyFoodCons > 0) totalFoodStock.toFloat() / dailyFoodCons else 999f
        val daysOfWater = if (dailyWaterCons > 0) totalWaterStock.toFloat() / dailyWaterCons else 999f

        val avgMorale = if (npcList.isNotEmpty()) npcList.map { it.morale }.average().toFloat() else 100f
        val avgSkill = if (npcList.isNotEmpty()) npcList.map { it.skillLevel.toFloat() }.average().toFloat() else 1.0f
        val roleMap = npcList.groupingBy { it.role }.eachCount()

        _settlementStats.value = SettlementStats(
            totalPopulation = npcList.size,
            occupiedBeds = occupiedBeds,
            totalBeds = totalBeds,
            overallMorale = avgMorale,
            foodStockUnits = totalFoodStock,
            cleanWaterStockUnits = totalWaterStock,
            daysOfFoodRemaining = daysOfFood,
            daysOfWaterRemaining = daysOfWater,
            dailyFoodConsumption = dailyFoodCons,
            dailyWaterConsumption = dailyWaterCons,
            roleBreakdown = roleMap,
            averageSkillLevel = avgSkill,
            isHousingDeficit = npcList.size > totalBeds,
            isStarving = totalFoodStock < dailyFoodCons && npcList.isNotEmpty(),
            isDehydrated = totalWaterStock < dailyWaterCons && npcList.isNotEmpty()
        )

        if (npcList.isEmpty()) return

        // 3. Update NPC Schedules, Duties, Needs, Pathfinding & States via NpcAiController
        val currentBiome = when {
            playerX > 35.0f -> BiomeType.WETLAND
            playerX < -45.0f -> BiomeType.DEEP_FOREST
            else -> BiomeType.GREEN_VALLEY
        }

        val aiResult = npcAiController.tickAll(
            npcs = npcList,
            deltaSec = deltaSec,
            gameHour = newHour,
            plots = plotsList,
            placedBuildings = placedList,
            livestock = livestockList,
            currentBiome = currentBiome
        )

        val gameState = dao.getGameState().firstOrNull()
        if (aiResult.healedPlayerHp > 0.01f && gameState != null && gameState.health < gameState.maxHealth) {
            dao.saveGameState(gameState.copy(health = min(gameState.maxHealth, gameState.health + aiResult.healedPlayerHp)))
        }

        dao.insertNpcs(aiResult.updatedNpcs)
    }

    suspend fun acceptPendingArrival(): Boolean {
        val candidate = _pendingArrival.value ?: return false
        val state = dao.getGameState().firstOrNull() ?: return false

        val newNpc = NpcEntity(
            name = candidate.name,
            role = candidate.role,
            age = candidate.age,
            skillLevel = 1,
            trait = candidate.trait,
            appearanceColor = candidate.role.primaryColorHex,
            morale = 80.0f,
            hunger = 10.0f,
            thirst = 10.0f,
            posX = 35.0f,
            posZ = -20.0f,
            targetX = 6.0f,
            targetZ = 2.0f
        )
        dao.insertNpc(newNpc)

        dao.insertArrival(
            NpcArrivalEntity(
                role = candidate.role,
                name = candidate.name,
                dayTrigger = candidate.arrivalDay,
                status = "ACCEPTED"
            )
        )

        dao.saveGameState(state.copy(ecoPrestige = state.ecoPrestige + 50))
        _pendingArrival.value = null
        return true
    }

    suspend fun rejectPendingArrival(): Boolean {
        val candidate = _pendingArrival.value ?: return false
        dao.insertArrival(
            NpcArrivalEntity(
                role = candidate.role,
                name = candidate.name,
                dayTrigger = candidate.arrivalDay,
                status = "REJECTED_COOLDOWN",
                cooldownDaysRemaining = 3
            )
        )
        _pendingArrival.value = null
        return true
    }

    suspend fun interactWithNpc(npcId: Int, optionKey: String): Pair<Boolean, String> {
        val npcsList = dao.getAllNpcs().firstOrNull() ?: emptyList()
        val npc = npcsList.find { it.id == npcId } ?: return Pair(false, "NPC not found")

        when (optionKey) {
            "HOW_ARE_YOU" -> {
                val updated = npc.copy(
                    morale = min(100f, npc.morale + 5f),
                    relationshipToPlayer = min(100, npc.relationshipToPlayer + 5),
                    speechBubble = "Feeling great! Thanks for asking, friend.",
                    speechBubbleTimer = 4.0f
                )
                dao.updateNpc(updated)
                return Pair(true, "${npc.name}: \"I'm feeling good! Our settlement is thriving. Morale +5\"")
            }
            "NEED_ANYTHING" -> {
                val needMsg = when {
                    npc.hunger > 50f -> "We could use more harvested food in storage."
                    npc.thirst > 50f -> "Clean drinking water would be wonderful."
                    npc.morale < 50f -> "A bit of rest and decorations near my cabin would help."
                    else -> "All good here! Everything is running smoothly."
                }
                val updated = npc.copy(
                    speechBubble = needMsg,
                    speechBubbleTimer = 4.0f
                )
                dao.updateNpc(updated)
                return Pair(true, "${npc.name}: \"$needMsg\"")
            }
            "TELL_ME_ABOUT_YOURSELF" -> {
                val traitInfo = "${npc.trait.displayName} trait (${npc.trait.description})"
                return Pair(true, "${npc.name} (${npc.role.displayName}, Age ${npc.age}): \"${npc.role.description}. $traitInfo\"")
            }
            "DISMISS" -> {
                dao.deleteNpc(npc.id)
                return Pair(true, "Dismissed ${npc.name} from the settlement.")
            }
            else -> return Pair(false, "Unknown option")
        }
    }

    suspend fun assignNpcRole(npcId: Int, newRole: NpcRole): Boolean {
        val npcsList = dao.getAllNpcs().firstOrNull() ?: return false
        val npc = npcsList.find { it.id == npcId } ?: return false
        dao.updateNpc(npc.copy(role = newRole, appearanceColor = newRole.primaryColorHex))
        return true
    }

    // Phase 6: Story Missions, POIs, Lore, Terminals Flows & Methods
    val storyMissions: Flow<List<com.example.data.model.StoryMission>> = dao.getAllStoryMissions().map { list ->
        list.map { entity ->
            com.example.data.model.StoryMission(
                id = entity.id,
                missionNumber = entity.missionNumber,
                title = entity.title,
                description = getMissionDescription(entity.missionNumber),
                objectiveDescription = getMissionObjectiveDesc(entity.missionNumber),
                currentProgress = entity.currentProgress,
                targetProgress = entity.targetProgress,
                rpReward = getMissionRpReward(entity.missionNumber),
                rewardSummary = getMissionRewardSummary(entity.missionNumber),
                autoStartDay = getMissionAutoStartDay(entity.missionNumber),
                isCompleted = entity.isCompleted,
                isUnlocked = entity.isUnlocked,
                isEndingMission = (entity.missionNumber == 12)
            )
        }
    }

    val pointsOfInterest: Flow<List<com.example.data.model.PointOfInterestData>> = dao.getAllPois().map { list ->
        list.map { entity ->
            val modelPoi = com.example.data.model.PointOfInterestType.values().find { it.id == entity.id }
            com.example.data.model.PointOfInterestData(
                id = entity.id,
                name = entity.name,
                biome = modelPoi?.biome ?: com.example.data.model.BiomeType.GREEN_VALLEY,
                posX = entity.posX,
                posZ = entity.posZ,
                description = modelPoi?.discoveryRewardDesc ?: "Discoverable landmark",
                rpReward = if (entity.id == "poi_ancient_tree") 50 else 15,
                isDiscovered = entity.isDiscovered,
                rewardSummary = modelPoi?.discoveryRewardTitle ?: "Lore & Rewards"
            )
        }
    }

    val loreEntries: Flow<List<com.example.data.model.LoreEntryData>> = dao.getAllLoreEntries().map { list ->
        list.map { entity ->
            com.example.data.model.LoreEntryData(
                id = entity.id,
                title = entity.title,
                author = entity.author,
                category = entity.category,
                textContent = entity.textContent,
                isUnlocked = entity.isUnlocked
            )
        }
    }

    val terminalLogs: Flow<List<com.example.data.model.TerminalLogData>> = dao.getAllTerminalLogs().map { list ->
        list.map { entity ->
            val passwords = mapOf("term_3" to "SOLARIS", "term_5" to "SOLARIS")
            com.example.data.model.TerminalLogData(
                id = entity.id,
                terminalName = entity.terminalName,
                locationName = entity.locationName,
                logText = getTerminalLogText(entity.id),
                isHacked = entity.isHacked,
                requiresPassword = passwords.containsKey(entity.id),
                passwordAnswer = passwords[entity.id] ?: "SOLARIS"
            )
        }
    }

    suspend fun hackTerminal(terminalId: String, password: String): Boolean {
        val list = dao.getAllTerminalLogs().firstOrNull() ?: emptyList()
        val terminal = list.find { it.id == terminalId } ?: return false
        val correctPassword = if (terminalId == "term_3" || terminalId == "term_5") "SOLARIS" else ""
        if (password.trim().equals(correctPassword, ignoreCase = true) || !terminalId.endsWith("3") && !terminalId.endsWith("5")) {
            dao.updateTerminalLog(terminal.copy(isHacked = true))
            addResearchPoints(25)
            return true
        }
        return false
    }

    suspend fun updateMissionProgress(missionId: String, currentProgress: Int): Boolean {
        val missions = dao.getAllStoryMissions().firstOrNull() ?: emptyList()
        val mission = missions.find { it.id == missionId } ?: return false
        val target = mission.targetProgress
        val isDone = currentProgress >= target
        val updated = mission.copy(currentProgress = currentProgress.coerceAtMost(target), isCompleted = isDone)
        dao.updateStoryMission(updated)

        if (isDone && !mission.isCompleted) {
            val rpReward = getMissionRpReward(mission.missionNumber)
            addResearchPoints(rpReward)
            // Unlock next mission
            val nextMission = missions.find { it.missionNumber == mission.missionNumber + 1 }
            if (nextMission != null) {
                dao.updateStoryMission(nextMission.copy(isUnlocked = true))
            }
            if (mission.missionNumber == 12) {
                val state = dao.getGameState().firstOrNull()
                if (state != null) {
                    dao.saveGameState(state.copy(storyEndingUnlocked = true))
                }
            }
        }
        return true
    }

    private fun getMissionDescription(num: Int): String = when (num) {
        1 -> "You've arrived in Sector 4. Build a campfire to survive the first cold night."
        2 -> "Establish basic crop beds to feed yourself and early settlement visitors."
        3 -> "Construct a UV water purification filter and process clean drinking water."
        4 -> "Locate the historic Solarpunk homestead in Green Valley."
        5 -> "Build a solar panel array and high-capacity battery bank to power the farm."
        6 -> "Locate and calibrate the abandoned radio antenna in Deep Forest."
        7 -> "Welcome the clean-tech Researcher NPC and construct a Research Lab."
        8 -> "Restore ecosystem health to 75% across the valley through clean energy & permaculture."
        9 -> "Explore the encrypted SDZ Research Facility hidden inside Deep Forest."
        10 -> "Extend transmission poles and connect 10 farm buildings to the microgrid."
        11 -> "Welcome 5 survivors into Sanctuary and maintain 80%+ settlement morale."
        12 -> "Reach 90% Ecosystem Health, 8 Survivors, and 500 kWh battery storage to rebuild civilization."
        else -> "Rebuild humanity's sustainable future."
    }

    private fun getMissionObjectiveDesc(num: Int): String = when (num) {
        1 -> "Build Campfire (0/1)"
        2 -> "Plant Crops (0/5)"
        3 -> "Purify Water Units (0/5)"
        4 -> "Discover Solarpunk Homestead (0/1)"
        5 -> "Build Solar Array & Battery (0/1)"
        6 -> "Repair Radio Tower (0/1)"
        7 -> "Build Research Lab & Recruit Researcher (0/1)"
        8 -> "Reach 75% Ecosystem Health"
        9 -> "Explore SDZ Facility (0/1)"
        10 -> "Connect 10 Grid Buildings (0/10)"
        11 -> "Maintain 5 High Morale NPCs (0/5)"
        12 -> "Reach 90% Eco Health + 8 NPCs + 500 kWh"
        else -> "Complete Objective"
    }

    private fun getMissionRpReward(num: Int): Int = when (num) {
        1 -> 20; 2 -> 30; 3 -> 40; 4 -> 50; 5 -> 60
        6 -> 80; 7 -> 100; 8 -> 120; 9 -> 150; 10 -> 200
        11 -> 250; 12 -> 500; else -> 20
    }

    private fun getMissionRewardSummary(num: Int): String = when (num) {
        1 -> "20 RP + Campfire Unlocked"
        2 -> "30 RP + 5 Wheat Seeds"
        3 -> "40 RP + Water Filter Tech"
        4 -> "50 RP + Farm Blueprint & Lore Note"
        5 -> "60 RP + Advanced Solar Glass"
        6 -> "80 RP + Researcher Radio Signal"
        7 -> "100 RP + Full Tech Tree Access"
        8 -> "120 RP + Animal Breeding Program"
        9 -> "150 RP + Geothermal Blueprint & 3 Logs"
        10 -> "200 RP + Smart Grid Power Tech"
        11 -> "250 RP + Full Automation"
        12 -> "500 RP + Civilization Ending Sequence"
        else -> "RP Reward"
    }

    private fun getMissionAutoStartDay(num: Int): Int = when (num) {
        1 -> 1; 2 -> 1; 3 -> 3; 4 -> 7; 5 -> 10; 6 -> 15
        7 -> 20; 8 -> 25; 9 -> 30; 10 -> 35; 11 -> 40; 12 -> 50
        else -> 1
    }

    private fun getTerminalLogText(id: String): String = when (id) {
        "term_1" -> "SDZ SECURITY LOG [PASS]: Main power grid offline. Solar tracking arrays standing by."
        "term_2" -> "SDZ AGRONOMY LOG: Closed-loop hydroponics boosted yields by 10x. Permaculture soil microbes healthy."
        "term_3" -> "GEOTHERMAL CORE LOG: Deep subterranean vent tapping constant 80 kWh/day zero-emission power. Code: SOLARIS."
        "term_4" -> "DRONE NETWORK LOG: Scout drone aerial survey active. Wildlife migration patterns restored."
        "term_5" -> "CIVILIZATION ARCHIVE: Eco-village protocol verified. Humanity has successfully rebuilt."
        else -> "Decrypted terminal entry."
    }
}
