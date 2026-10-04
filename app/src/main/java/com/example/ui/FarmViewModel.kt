package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ContractEntity
import com.example.data.local.EnergyNodeEntity
import com.example.data.local.GameStateEntity
import com.example.data.local.InventoryEntity
import com.example.data.local.LivestockEntity
import com.example.data.local.PlotEntity
import com.example.data.model.CropType
import com.example.data.model.EnergyNodeType
import com.example.data.model.ItemCategory
import com.example.data.model.LivestockType
import com.example.data.model.PlayerTool
import com.example.data.model.TimeOfDayPhase
import com.example.data.model.WeatherType
import com.example.data.repository.FarmRepository
import com.example.game3d.audio.SpatialLivestockAudioSystem
import com.example.game3d.interaction.InteractionPrompt
import com.example.game3d.interaction.InteractionSystem
import com.example.game3d.interaction.InteractionTargetType
import com.example.game3d.player.PlayerInputState
import com.example.game3d.player.ThirdPersonCamera
import com.example.game3d.player.ThirdPersonPlayer
import com.example.game3d.renderer.DayNightLightingSystem
import com.example.game3d.renderer.LightingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class NotificationMessage(
    val title: String,
    val description: String,
    val icon: String = "info"
)

class FarmViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = FarmRepository(db.farmDao())
    val audioSystem = SpatialLivestockAudioSystem(viewModelScope)

    val gameState: StateFlow<GameStateEntity?> = repository.gameState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val plots: StateFlow<List<PlotEntity>> = repository.plots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val energyNodes: StateFlow<List<EnergyNodeEntity>> = repository.energyNodes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val livestock: StateFlow<List<LivestockEntity>> = repository.livestock
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventory: StateFlow<List<InventoryEntity>> = repository.inventory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contracts: StateFlow<List<ContractEntity>> = repository.contracts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 3D Player & Camera Objects
    val player = ThirdPersonPlayer(0.0f, 0.0f, 0.0f)
    val camera = ThirdPersonCamera(0.0f, 0.0f, 0.0f)

    // Player Inputs & Interactive State
    private val _inputState = MutableStateFlow(PlayerInputState())
    val inputState: StateFlow<PlayerInputState> = _inputState.asStateFlow()

    private val _selectedTool = MutableStateFlow(PlayerTool.HAND)
    val selectedTool: StateFlow<PlayerTool> = _selectedTool.asStateFlow()

    private val _currentPrompt = MutableStateFlow<InteractionPrompt?>(null)
    val currentPrompt: StateFlow<InteractionPrompt?> = _currentPrompt.asStateFlow()

    private val _lightingState = MutableStateFlow(
        DayNightLightingSystem.calculateLighting(8.5f, WeatherType.SUNNY_CLEAR)
    )
    val lightingState: StateFlow<LightingState> = _lightingState.asStateFlow()

    private val _activeModal = MutableStateFlow<String?>(null)
    val activeModal: StateFlow<String?> = _activeModal.asStateFlow()

    private val _selectedPlotForModal = MutableStateFlow<PlotEntity?>(null)
    val selectedPlotForModal: StateFlow<PlotEntity?> = _selectedPlotForModal.asStateFlow()

    private val _selectedAnimalForModal = MutableStateFlow<LivestockEntity?>(null)
    val selectedAnimalForModal: StateFlow<LivestockEntity?> = _selectedAnimalForModal.asStateFlow()

    private val _bannerNotification = MutableStateFlow<NotificationMessage?>(null)
    val bannerNotification: StateFlow<NotificationMessage?> = _bannerNotification.asStateFlow()

    private val _animTime = MutableStateFlow(0.0f)
    val animTime: StateFlow<Float> = _animTime.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.initializeDefaultDataIfEmpty()
        }
        startGameLoop()
    }

    private fun startGameLoop() {
        viewModelScope.launch(Dispatchers.Default) {
            var lastTime = System.nanoTime()
            while (isActive) {
                val now = System.nanoTime()
                val deltaSec = ((now - lastTime) / 1_000_000_000.0f).coerceIn(0.001f, 0.1f)
                lastTime = now

                _animTime.value += deltaSec

                // 1. Update Player Movement
                player.update(_inputState.value, camera.yawDeg, deltaSec)

                // 2. Update Camera Target
                camera.updateTarget(player.posX, player.posY, player.posZ, deltaSec)

                // 3. Proximity Interaction Check
                val pList = plots.value
                val eList = energyNodes.value
                val aList = livestock.value
                _currentPrompt.value = InteractionSystem.findNearestInteraction(
                    player.posX,
                    player.posZ,
                    pList,
                    eList,
                    aList
                )

                // 4. Update Day/Night Lighting
                val hour = gameState.value?.gameTimeHour ?: 8.5f
                val weather = gameState.value?.currentWeather ?: WeatherType.SUNNY_CLEAR
                _lightingState.value = DayNightLightingSystem.calculateLighting(hour, weather)

                // 5. Game Simulation Tick (Run every ~500ms for db performance)
                repository.gameTick(deltaSec)

                delay(16) // ~60fps loop
            }
        }
    }

    fun setJoystickMove(x: Float, z: Float) {
        _inputState.value = _inputState.value.copy(moveX = x, moveZ = z)
    }

    fun setSprinting(sprint: Boolean) {
        _inputState.value = _inputState.value.copy(isSprinting = sprint)
    }

    fun rotateCamera(deltaYaw: Float, deltaPitch: Float) {
        camera.rotate(deltaYaw, deltaPitch)
    }

    fun zoomCamera(deltaZoom: Float) {
        camera.zoom(deltaZoom)
    }

    fun selectTool(tool: PlayerTool) {
        _selectedTool.value = tool
    }

    fun openModal(modalName: String?) {
        _activeModal.value = modalName
    }

    fun showNotification(title: String, desc: String, icon: String = "info") {
        viewModelScope.launch {
            _bannerNotification.value = NotificationMessage(title, desc, icon)
            delay(3500)
            _bannerNotification.value = null
        }
    }

    fun onContextActionButton() {
        val prompt = _currentPrompt.value ?: return
        when (prompt.targetType) {
            InteractionTargetType.PLOT -> {
                val plot = plots.value.find { it.id == prompt.targetId }
                if (plot != null) {
                    if (plot.cropType == null) {
                        _selectedPlotForModal.value = plot
                        openModal("plant_crop")
                    } else if (plot.stage == com.example.data.model.CropStage.HARVEST_READY) {
                        harvestPlot(plot.id)
                    } else if (plot.moisture < 0.4f) {
                        waterPlot(plot.id)
                    } else {
                        fertilizePlot(plot.id)
                    }
                }
            }
            InteractionTargetType.LIVESTOCK -> {
                val animal = livestock.value.find { it.id == prompt.targetId }
                if (animal != null) {
                    if (animal.readyToHarvest) {
                        collectAnimal(animal.id)
                    } else if (animal.hunger > 40.0f) {
                        feedAnimal(animal.id)
                    } else {
                        petAnimal(animal.id)
                    }
                }
            }
            InteractionTargetType.ENERGY_NODE -> {
                openModal("energy_screen")
            }
            InteractionTargetType.WORKSHOP_BUILDING -> {
                openModal("workshop_screen")
            }
            InteractionTargetType.MARKET_STALL -> {
                openModal("market_screen")
            }
            InteractionTargetType.NONE -> {}
        }
    }

    fun plantCrop(plotId: Int, cropType: CropType) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.plantCrop(plotId, cropType, plots.value)
            if (success) {
                audioSystem.playPlantSeedSound()
                showNotification("Sowed Seeds", "Planted ${cropType.displayName} in Plot #${plotId + 1}", "eco")
                _activeModal.value = null
            } else {
                showNotification("Cannot Plant", "Insufficient Sol Coins or seeds for ${cropType.displayName}", "warning")
            }
        }
    }

    fun waterPlot(plotId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.waterPlot(plotId, plots.value)
            audioSystem.playWaterSound()
            showNotification("Hydrated Plot", "Eco-sprinkler hydrated Plot #${plotId + 1}", "water_drop")
        }
    }

    fun fertilizePlot(plotId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.fertilizePlot(plotId, plots.value)
            if (success) {
                audioSystem.playSolarHumSound()
                showNotification("Soil Enriched", "Bio-Nutrient Serum applied to Plot #${plotId + 1}", "compost")
            } else {
                showNotification("No Fertilizer", "Purchase Bio-Compost Serum in the Eco Shop", "warning")
            }
        }
    }

    fun harvestPlot(plotId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val crop = repository.harvestPlot(plotId, plots.value)
            if (crop != null) {
                audioSystem.playHarvestSparkleSound()
                showNotification("Harvested!", "Collected +${crop.harvestYield} ${crop.displayName} (+${crop.energyBonusKwh} kWh clean energy)", "star")
            }
        }
    }

    fun petAnimal(animalId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.petLivestock(animalId, livestock.value)
            audioSystem.playPetPurrSound()
            showNotification("Affection", "Pet animal (+2 Eco Prestige)", "favorite")
        }
    }

    fun feedAnimal(animalId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.feedLivestock(animalId, livestock.value)
            if (success) {
                audioSystem.playPetPurrSound()
                showNotification("Fed Animal", "Fed organic grain (+20% Happiness)", "restaurant")
            } else {
                showNotification("No Grain", "Harvest Golden Wheat or buy grain to feed animals", "warning")
            }
        }
    }

    fun collectAnimal(animalId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val product = repository.collectLivestockProduct(animalId, livestock.value)
            if (product != null) {
                audioSystem.playHarvestSparkleSound()
                showNotification("Collected Product", "Obtained 1x $product", "shopping_bag")
            }
        }
    }

    fun buyEnergyNode(type: EnergyNodeType) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.buyEnergyNode(type)
            if (success) {
                audioSystem.playSolarHumSound()
                showNotification("Built Energy Node", "Constructed ${type.displayName}", "bolt")
            } else {
                showNotification("Insufficient Coins", "Need ${type.buildCostCoins} Sol Coins to build ${type.displayName}", "warning")
            }
        }
    }

    fun upgradeEnergyNode(nodeId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.upgradeEnergyNode(nodeId)
            if (success) {
                audioSystem.playSolarHumSound()
                showNotification("Node Upgraded", "Clean energy output increased by +35%", "arrow_upward")
            } else {
                showNotification("Cannot Upgrade", "Insufficient Sol Coins to upgrade", "warning")
            }
        }
    }

    fun buyLivestock(type: LivestockType) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.buyLivestock(type)
            if (success) {
                audioSystem.playSheepBaa()
                showNotification("Adopted Animal", "Welcomed a new ${type.displayName} to sanctuary", "pets")
            } else {
                showNotification("Insufficient Coins", "Need ${type.purchaseCostCoins} Sol Coins", "warning")
            }
        }
    }

    fun sellItem(itemId: String, qty: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.sellItem(itemId, qty)
            if (success) {
                audioSystem.playCoinEarned()
                showNotification("Sold Item", "Earned Sol Coins from trade", "monetization_on")
            }
        }
    }

    fun buySeed(cropType: CropType, qty: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.buySeed(cropType, qty)
            if (success) {
                audioSystem.playCoinEarned()
                showNotification("Purchased Seeds", "Bought $qty ${cropType.displayName} Seeds", "shopping_cart")
            } else {
                showNotification("Insufficient Coins", "Need ${cropType.seedCost * qty} Sol Coins", "warning")
            }
        }
    }

    fun claimContract(contractId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.claimContract(contractId)
            if (success) {
                audioSystem.playCraftSuccess()
                showNotification("Contract Fulfilled!", "Wholesale Zeppelins dispatched. Bonus coins & Eco Prestige awarded!", "verified")
            } else {
                showNotification("Contract Incomplete", "Produce required items in inventory to fulfill contract", "warning")
            }
        }
    }

    fun craftItem(
        resultId: String,
        resultName: String,
        category: ItemCategory,
        sellVal: Int,
        consumedIngredients: Map<String, Int>,
        energyKwhCost: Float
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.craftItem(
                resultId,
                resultName,
                category,
                sellVal,
                consumedIngredients,
                energyKwhCost
            )
            if (success) {
                audioSystem.playCraftSuccess()
                showNotification("Crafted Successfully!", "Created $resultName in Artisan Workshop", "build")
            } else {
                showNotification("Crafting Failed", "Check battery charge ($energyKwhCost kWh required) and ingredient supplies", "warning")
            }
        }
    }

    fun advanceTimeOfDay(hours: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            val state = gameState.value ?: return@launch
            var newHour = (state.gameTimeHour + hours)
            var newDay = state.gameTimeDay
            if (newHour >= 24.0f) {
                newHour -= 24.0f
                newDay += 1
            }
            repository.saveGameState(state.copy(gameTimeHour = newHour, gameTimeDay = newDay))
            showNotification("Time Shift", "Advanced time to ${formatGameTime(newHour)}", "schedule")
        }
    }

    fun changeWeather(weather: WeatherType) {
        viewModelScope.launch(Dispatchers.IO) {
            val state = gameState.value ?: return@launch
            repository.saveGameState(state.copy(currentWeather = weather))
            showNotification("Weather Modulation", "Atmospheric shift: ${weather.displayName}", "cloud")
        }
    }

    private fun formatGameTime(hourFloat: Float): String {
        val totalMinutes = (hourFloat * 60).toInt()
        val h = (totalMinutes / 60) % 24
        val m = totalMinutes % 60
        val ampm = if (h < 12) "AM" else "PM"
        val displayH = if (h == 0) 12 else if (h > 12) h - 12 else h
        return String.format("%02d:%02d %s", displayH, m, ampm)
    }
}
