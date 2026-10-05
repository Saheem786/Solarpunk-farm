package com.example.ui

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ContractEntity
import com.example.data.local.EnergyNodeEntity
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
import com.example.data.model.PlayerTool
import com.example.data.model.TimeOfDayPhase
import com.example.data.model.WeatherType
import com.example.data.repository.FarmRepository
import com.example.data.repository.SaveLoadSystem
import com.example.game3d.audio.HapticFeedbackHelper
import com.example.game3d.audio.SpatialLivestockAudioSystem
import com.example.game3d.interaction.InteractionPrompt
import com.example.game3d.interaction.InteractionSystem
import com.example.game3d.interaction.InteractionTargetType
import com.example.game3d.opengl.models.PlayerActionAnim
import com.example.game3d.particles.ParticleSystem3D
import com.example.game3d.player.PlayerInputState
import com.example.game3d.player.ThirdPersonCamera
import com.example.game3d.player.ThirdPersonPlayer
import com.example.game3d.renderer.DayNightLightingSystem
import com.example.game3d.renderer.GhostBuildingState
import com.example.game3d.renderer.LightingState
import com.example.ui.components.FloatingTextData
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SunGold
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
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
    val repository = FarmRepository(db.farmDao())
    val audioSystem = SpatialLivestockAudioSystem(viewModelScope)
    val haptics = HapticFeedbackHelper(application)
    val particleSystem = ParticleSystem3D()

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

    val placedBuildings: StateFlow<List<PlacedBuildingEntity>> = repository.placedBuildings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ecosystemHealth: StateFlow<Int> = combine(
        livestock,
        plots,
        gameState
    ) { animals, plotList, state ->
        val baseScore = 50
        val animalBonus = (animals.map { it.type }.distinct().size * 5).coerceAtMost(25)
        val hasWateredGrowingCrops = plotList.any { it.cropType != null && it.moisture > 0.4f && it.stage != CropStage.HARVEST_READY && it.stage != CropStage.WITHERED }
        val cropBonus = if (hasWateredGrowingCrops) 10 else 0
        val weather = state?.currentWeather ?: WeatherType.SUNNY_CLEAR
        val weatherBonus = if (weather != WeatherType.STORM && weather != WeatherType.RAINY_STORM) 10 else 0
        val structureBonus = 5

        (baseScore + animalBonus + cropBonus + weatherBonus + structureBonus).coerceIn(0, 100)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 75)

    // Audio & Vibration Settings
    private val prefs = application.getSharedPreferences("eco_farm_settings", Application.MODE_PRIVATE)
    val masterVolume = MutableStateFlow(prefs.getFloat("master_vol", 1.0f))
    val musicVolume = MutableStateFlow(prefs.getFloat("music_vol", 0.8f))
    val sfxVolume = MutableStateFlow(prefs.getFloat("sfx_vol", 0.9f))
    val vibrationEnabled = MutableStateFlow(prefs.getBoolean("vibration_on", true))

    // Floating Texts
    private val _floatingTexts = MutableStateFlow<List<FloatingTextData>>(emptyList())
    val floatingTexts: StateFlow<List<FloatingTextData>> = _floatingTexts.asStateFlow()

    // Action Animations & Screen Polish
    val playerActionAnim = MutableStateFlow(PlayerActionAnim.NONE)
    val playerActionProgress = MutableStateFlow(0.0f)
    val screenShake = MutableStateFlow(0.0f)
    val fadeBlackAlpha = MutableStateFlow(0.0f)
    val isInitialLoading = MutableStateFlow(true)

    // Build Mode State
    private val _isBuildMode = MutableStateFlow(false)
    val isBuildMode: StateFlow<Boolean> = _isBuildMode.asStateFlow()

    private val _selectedBuildType = MutableStateFlow(BuildableType.CABIN)
    val selectedBuildType: StateFlow<BuildableType> = _selectedBuildType.asStateFlow()

    private val _buildRotationDeg = MutableStateFlow(0.0f)
    val buildRotationDeg: StateFlow<Float> = _buildRotationDeg.asStateFlow()

    val ghostBuildingState = MutableStateFlow<GhostBuildingState?>(null)
    private val _ghostOffsetX = MutableStateFlow(0.0f)
    private val _ghostOffsetZ = MutableStateFlow(4.8f)

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

    private val _isFainted = MutableStateFlow(false)
    val isFainted: StateFlow<Boolean> = _isFainted.asStateFlow()

    private val _faintCountdown = MutableStateFlow(5)
    val faintCountdown: StateFlow<Int> = _faintCountdown.asStateFlow()

    private val _animTime = MutableStateFlow(0.0f)
    val animTime: StateFlow<Float> = _animTime.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Apply saved volume settings
        audioSystem.masterVolume = masterVolume.value
        audioSystem.musicVolume = musicVolume.value
        audioSystem.sfxVolume = sfxVolume.value
        haptics.vibrationEnabled = vibrationEnabled.value

        viewModelScope.launch(Dispatchers.IO) {
            if (SaveLoadSystem.hasSave(getApplication())) {
                SaveLoadSystem.loadGame(getApplication(), repository.farmDao)
            } else {
                repository.initializeDefaultDataIfEmpty()
            }
            val savedState = repository.gameState.firstOrNull()
            if (savedState != null) {
                player.posX = savedState.playerX
                player.posY = savedState.playerY
                player.posZ = savedState.playerZ
                player.orientationAngleDeg = savedState.playerAngle
                camera.updateTarget(savedState.playerX, savedState.playerY, savedState.playerZ, 1.0f)
            }
            // Dismiss initial loading after game state is ready
            delay(900)
            isInitialLoading.value = false
        }
        startGameLoop()
        startAutoSaveLoop()
    }

    fun setMasterVolume(vol: Float) {
        masterVolume.value = vol
        audioSystem.masterVolume = vol
        prefs.edit().putFloat("master_vol", vol).apply()
    }

    fun setMusicVolume(vol: Float) {
        musicVolume.value = vol
        audioSystem.musicVolume = vol
        prefs.edit().putFloat("music_vol", vol).apply()
    }

    fun setSfxVolume(vol: Float) {
        sfxVolume.value = vol
        audioSystem.sfxVolume = vol
        prefs.edit().putFloat("sfx_vol", vol).apply()
    }

    fun setVibrationEnabled(enabled: Boolean) {
        vibrationEnabled.value = enabled
        haptics.vibrationEnabled = enabled
        prefs.edit().putBoolean("vibration_on", enabled).apply()
    }

    fun addFloatingText(text: String, color: Color = SolarEmerald) {
        _floatingTexts.value = _floatingTexts.value + FloatingTextData(text = text, color = color)
    }

    fun removeFloatingText(id: Long) {
        _floatingTexts.value = _floatingTexts.value.filter { it.id != id }
    }

    fun triggerPlayerActionAnim(anim: PlayerActionAnim) {
        viewModelScope.launch {
            playerActionAnim.value = anim
            playerActionProgress.value = 1.0f
            var p = 1.0f
            while (p > 0f) {
                delay(20)
                p -= 0.08f
                playerActionProgress.value = p.coerceAtLeast(0f)
            }
            playerActionAnim.value = PlayerActionAnim.NONE
            playerActionProgress.value = 0f
        }
    }

    fun triggerScreenShake(durationSec: Float = 0.4f) {
        viewModelScope.launch {
            screenShake.value = 1.0f
            val stepTime = 30L
            val totalSteps = (durationSec * 1000 / stepTime).toInt().coerceAtLeast(1)
            for (i in 0 until totalSteps) {
                delay(stepTime)
                screenShake.value = (1.0f - (i.toFloat() / totalSteps)).coerceAtLeast(0f)
            }
            screenShake.value = 0f
        }
    }

    private fun startAutoSaveLoop() {
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(120_000) // Auto-save every 2 minutes
                repository.saveGameSnapshot(
                    playerX = player.posX,
                    playerY = player.posY,
                    playerZ = player.posZ,
                    playerAngle = player.orientationAngleDeg
                )
                val success = SaveLoadSystem.saveGame(getApplication(), repository.farmDao)
                if (success) {
                    showNotification("Auto-Saved", "Game progress saved to JSON archive", "save")
                }
            }
        }
    }

    fun executeFullSaveFlow() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveGameSnapshot(
                playerX = player.posX,
                playerY = player.posY,
                playerZ = player.posZ,
                playerAngle = player.orientationAngleDeg
            )
            val success = SaveLoadSystem.saveGame(getApplication(), repository.farmDao)
            viewModelScope.launch(Dispatchers.Main) {
                if (success) {
                    audioSystem.playSaveGame()
                    haptics.vibrateSuccess()
                    addFloatingText("Saved!", SolarEmerald)
                    showNotification("Game Saved", "Your progress, crops & structures are archived", "save")
                } else {
                    audioSystem.playErrorSound()
                    haptics.vibrateError()
                    showNotification("Save Failed", "Could not write archive", "warning")
                }
            }
        }
    }

    fun loadSaveGame() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val success = SaveLoadSystem.loadGame(getApplication(), repository.farmDao)
            delay(600)
            val loadedState = repository.gameState.firstOrNull()
            if (loadedState != null) {
                player.posX = loadedState.playerX
                player.posY = loadedState.playerY
                player.posZ = loadedState.playerZ
                player.orientationAngleDeg = loadedState.playerAngle
                camera.updateTarget(loadedState.playerX, loadedState.playerY, loadedState.playerZ, 1.0f)
            }
            _isLoading.value = false
            viewModelScope.launch(Dispatchers.Main) {
                if (success) {
                    audioSystem.playCraftSuccess()
                    addFloatingText("Save Loaded!", CleanCyan)
                    showNotification("Game Loaded", "Successfully restored your solarpunk farm", "restore")
                } else {
                    audioSystem.playErrorSound()
                    haptics.vibrateError()
                    showNotification("Load Failed", "No save archive found", "warning")
                }
            }
        }
    }

    fun startNewGameFresh() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            SaveLoadSystem.deleteSave(getApplication())
            repository.initializeDefaultDataIfEmpty(forceReset = true)
            player.posX = 0.0f
            player.posY = 0.0f
            player.posZ = 0.0f
            player.orientationAngleDeg = 0.0f
            camera.updateTarget(0.0f, 0.0f, 0.0f, 1.0f)
            delay(600)
            _isLoading.value = false
            viewModelScope.launch(Dispatchers.Main) {
                audioSystem.playCraftSuccess()
                showNotification("Fresh Start", "Welcomed to your new Solarpunk Sanctuary", "eco")
            }
        }
    }

    private fun startGameLoop() {
        viewModelScope.launch {
            var lastTime = System.nanoTime()
            while (isActive) {
                val now = System.nanoTime()
                val deltaSec = ((now - lastTime) / 1_000_000_000.0f).coerceIn(0.001f, 0.1f)
                lastTime = now

                _animTime.value += deltaSec

                // 1. Update Player Movement (Freeze in Build Mode)
                val currentStamina = gameState.value?.stamina ?: 100.0f
                val effectiveSprinting = _inputState.value.isSprinting && currentStamina > 2.0f
                val weather = gameState.value?.currentWeather ?: WeatherType.SUNNY_CLEAR
                val speedMultiplier = if (weather == WeatherType.STORM) 0.85f else 1.0f

                player.update(
                    input = if (_isBuildMode.value) PlayerInputState() else _inputState.value.copy(isSprinting = effectiveSprinting),
                    cameraYawDeg = camera.yawDeg,
                    deltaSec = deltaSec,
                    speedMultiplier = speedMultiplier,
                    placedBuildings = placedBuildings.value,
                    energyNodes = energyNodes.value,
                    plots = plots.value
                )

                // Footstep sound & dust particle emission
                if (player.isMoving && !_isBuildMode.value) {
                    audioSystem.playFootstep(effectiveSprinting)
                    val isDirt = (player.posX < 0f && player.posZ < 0f)
                    particleSystem.spawnFootstep(player.posX, player.posZ, isDirt)
                }

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

                // 4. Update Day/Night Lighting & Music Loop
                val hour = gameState.value?.gameTimeHour ?: 8.5f
                _lightingState.value = DayNightLightingSystem.calculateLighting(hour, weather)
                val isDaytime = hour in 7.0f..19.0f
                audioSystem.updateDayNightMusic(isDaytime)
                audioSystem.updateWeatherAmbience(weather)

                // Ambient Bee Buzzing when near beehives/flowers
                val distToBee = Math.sqrt(((player.posX - 10.0f) * (player.posX - 10.0f) + (player.posZ - (-10.0f)) * (player.posZ - (-10.0f))).toDouble()).toFloat()
                audioSystem.playBeeBuzzNear(distToBee)

                // 5. Update Ghost Building Preview in Build Mode
                if (_isBuildMode.value) {
                    val moveX = _inputState.value.moveX
                    val moveZ = _inputState.value.moveZ
                    val cameraYawRad = Math.toRadians(camera.yawDeg.toDouble())
                    val cosYaw = cos(cameraYawRad).toFloat()
                    val sinYaw = sin(cameraYawRad).toFloat()
                    val dx = (-moveX * cosYaw + moveZ * sinYaw) * 6.5f * deltaSec
                    val dz = (-moveX * sinYaw - moveZ * cosYaw) * 6.5f * deltaSec
                    _ghostOffsetX.value = (_ghostOffsetX.value + dx).coerceIn(-10.0f, 10.0f)
                    _ghostOffsetZ.value = (_ghostOffsetZ.value + dz).coerceIn(-10.0f, 10.0f)

                    val rawGx = player.posX + _ghostOffsetX.value
                    val rawGz = player.posZ + _ghostOffsetZ.value
                    val gx = Math.round(rawGx).toFloat()
                    val gz = Math.round(rawGz).toFloat()

                    val coins = gameState.value?.solCoins ?: 0
                    val (isValid, reason) = checkPlacementValidity(_selectedBuildType.value, gx, gz, coins)

                    ghostBuildingState.value = GhostBuildingState(
                        type = _selectedBuildType.value,
                        posX = gx,
                        posY = 0.0f,
                        posZ = gz,
                        rotationDeg = _buildRotationDeg.value,
                        canAfford = coins >= _selectedBuildType.value.costCoins,
                        isValid = isValid,
                        reasonMessage = reason
                    )
                } else {
                    ghostBuildingState.value = null
                }

                // 6. Game Simulation Tick
                repository.gameTick(deltaSec, player.isMoving, effectiveSprinting)

                // 7. Check if player fainted due to 0 health
                val currentHp = gameState.value?.health ?: 100.0f
                if (currentHp <= 0.0f && !_isFainted.value) {
                    triggerFaintedSequence()
                }

                delay(16) // ~60fps loop
            }
        }
    }

    private fun triggerFaintedSequence() {
        _isFainted.value = true
        _faintCountdown.value = 5
        audioSystem.playLowEnergyWarning()
        haptics.vibrateFaint()
        triggerScreenShake(0.8f)
        viewModelScope.launch {
            for (sec in 5 downTo 1) {
                _faintCountdown.value = sec
                delay(1000)
            }
            repository.respawnAtHouse()
            player.posX = 6.0f
            player.posY = 0.0f
            player.posZ = 0.0f
            camera.updateTarget(6.0f, 0.0f, 0.0f, 1.0f)
            _isFainted.value = false
            showNotification("Restored at Sanctuary", "You woke up safely at the farmhouse", "health")
        }
    }

    fun toggleBuildMode() {
        _isBuildMode.value = !_isBuildMode.value
        haptics.vibrateButtonTap()
        if (_isBuildMode.value) {
            audioSystem.playMenuWhoosh()
            _ghostOffsetX.value = 0.0f
            _ghostOffsetZ.value = 4.8f
            camera.pitchDeg = 38.0f
            camera.distance = 9.0f
            showNotification("Build Mode Active", "Select structure, rotate & place in the sanctuary", "build")
        } else {
            audioSystem.playMenuWhoosh()
            camera.pitchDeg = 24.0f
            camera.distance = 7.5f
        }
    }

    fun selectBuildType(type: BuildableType) {
        _selectedBuildType.value = type
        haptics.vibrateButtonTap()
        audioSystem.playSelectToolSound()
    }

    fun rotateBuilding() {
        _buildRotationDeg.value = (_buildRotationDeg.value + 90.0f) % 360.0f
        haptics.vibrateButtonTap()
        audioSystem.playSelectToolSound()
    }

    fun checkPlacementValidity(type: BuildableType, gx: Float, gz: Float, coins: Int): Pair<Boolean, String> {
        if (coins < type.costCoins) {
            return Pair(false, "Not enough money")
        }
        val dx = gx - player.posX
        val dz = gz - player.posZ
        val dist = Math.sqrt((dx * dx + dz * dz).toDouble()).toFloat()
        if (dist > 10.0f) {
            return Pair(false, "Too far from player (max 10m)")
        }
        val existingBuildings = placedBuildings.value
        val size = type.size
        for (b in existingBuildings) {
            val bdx = gx - b.posX
            val bdz = gz - b.posZ
            val bDist = Math.sqrt((bdx * bdx + bdz * bdz).toDouble()).toFloat()
            val minSeparation = (size + b.buildingType.size) * 0.5f
            if (bDist < minSeparation) {
                return Pair(false, "Overlaps existing ${b.buildingType.displayName}")
            }
        }
        val staticObjects = listOf(
            Pair(6.0f, 0.0f), Pair(-12.0f, 10.0f), Pair(0.0f, 14.0f),
            Pair(-14.0f, -14.0f), Pair(10.0f, -10.0f), Pair(16.0f, 0.0f)
        )
        for (obj in staticObjects) {
            val odx = gx - obj.first
            val odz = gz - obj.second
            val oDist = Math.sqrt((odx * odx + odz * odz).toDouble()).toFloat()
            if (oDist < (size * 0.5f + 3.0f)) {
                return Pair(false, "Too close to permanent landmark")
            }
        }
        return Pair(true, "Valid location")
    }

    fun confirmPlaceBuilding() {
        val ghost = ghostBuildingState.value ?: return
        if (!ghost.isValid) {
            audioSystem.playErrorSound()
            haptics.vibrateError()
            showNotification("Cannot Place", ghost.reasonMessage, "warning")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val (success, message) = repository.placeBuilding(
                type = ghost.type,
                posX = ghost.posX,
                posY = ghost.posY,
                posZ = ghost.posZ,
                rotationDeg = ghost.rotationDeg
            )
            if (success) {
                audioSystem.playBuildPlacement()
                haptics.vibrateBuildPlacement()
                particleSystem.spawnBuildDustRing(ghost.posX, ghost.posZ)
                addFloatingText("Constructed!", SolarEmerald)
                showNotification("Constructed!", message, "check_circle")
                _isBuildMode.value = false
                camera.pitchDeg = 24.0f
                camera.distance = 7.5f
            } else {
                audioSystem.playErrorSound()
                haptics.vibrateError()
                showNotification("Cannot Build", message, "warning")
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
        haptics.vibrateButtonTap()
        audioSystem.playSelectToolSound()
    }

    fun openModal(modalName: String?) {
        _activeModal.value = modalName
        haptics.vibrateButtonTap()
        if (modalName != null) {
            audioSystem.playMenuWhoosh()
        }
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
        haptics.vibrateButtonTap()
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
            InteractionTargetType.WATER_SOURCE -> {
                drink("Fresh Spring Pond")
            }
            InteractionTargetType.FARMHOUSE -> {
                restInFarmhouse()
            }
            InteractionTargetType.NONE -> {}
        }
    }

    fun eat(foodItemId: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val (success, message) = repository.eatFood(foodItemId)
            if (success) {
                audioSystem.playEatFood()
                haptics.vibrateButtonTap()
                addFloatingText("+15 Hunger", SunGold)
                showNotification("Ate Food", message, "restaurant")
            } else {
                audioSystem.playErrorSound()
                haptics.vibrateError()
                showNotification("Cannot Eat", message, "warning")
            }
        }
    }

    fun drink(drinkItemId: String? = null, source: String = "Canteen") {
        viewModelScope.launch(Dispatchers.IO) {
            val (success, message) = repository.drinkWater(drinkItemId, source)
            if (success) {
                audioSystem.playDrinkWater()
                haptics.vibrateButtonTap()
                addFloatingText("+25 Thirst", CleanCyan)
                showNotification("Hydrated", message, "water_drop")
            } else {
                audioSystem.playErrorSound()
                haptics.vibrateError()
                showNotification("Cannot Drink", message, "warning")
            }
        }
    }

    fun restInFarmhouse() {
        viewModelScope.launch(Dispatchers.IO) {
            // Fade to black
            fadeBlackAlpha.value = 1.0f
            delay(500)
            val (success, message) = repository.restAtFarmhouse()
            delay(600)
            fadeBlackAlpha.value = 0.0f
            if (success) {
                audioSystem.playCraftSuccess()
                addFloatingText("Rested & Energized!", SolarEmerald)
                showNotification("Rested", message, "hotel")
            } else {
                showNotification("Cannot Rest", message, "warning")
            }
        }
    }

    fun plantCrop(plotId: Int, cropType: CropType) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.plantCrop(plotId, cropType, plots.value)
            if (success) {
                audioSystem.playPlantSeedSound()
                haptics.vibratePlantSeed()
                val plot = plots.value.find { it.id == plotId }
                if (plot != null) {
                    particleSystem.spawnPlantSprout(plot.posX, 0.1f, plot.posZ)
                }
                triggerPlayerActionAnim(PlayerActionAnim.PLANTING)
                addFloatingText("Sowed ${cropType.displayName}", SolarEmerald)
                showNotification("Sowed Seeds", "Planted ${cropType.displayName} in Plot #${plotId + 1}", "eco")
                _activeModal.value = null
            } else {
                audioSystem.playErrorSound()
                haptics.vibrateError()
                showNotification("Cannot Plant", "Insufficient Sol Coins or seeds for ${cropType.displayName}", "warning")
            }
        }
    }

    fun waterPlot(plotId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.waterPlot(plotId, plots.value)
            audioSystem.playWaterSound()
            haptics.vibrateWaterCrop()
            val plot = plots.value.find { it.id == plotId }
            if (plot != null) {
                particleSystem.spawnWaterSplash(plot.posX, 0.2f, plot.posZ)
            }
            triggerPlayerActionAnim(PlayerActionAnim.WATERING)
            addFloatingText("Watered!", CleanCyan)
            showNotification("Hydrated Plot", "Eco-sprinkler hydrated Plot #${plotId + 1}", "water_drop")
        }
    }

    fun fertilizePlot(plotId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.fertilizePlot(plotId, plots.value)
            if (success) {
                audioSystem.playSolarHumSound()
                haptics.vibrateButtonTap()
                val plot = plots.value.find { it.id == plotId }
                if (plot != null) {
                    particleSystem.spawnPlantSprout(plot.posX, 0.1f, plot.posZ)
                }
                addFloatingText("Soil Enriched!", SunGold)
                showNotification("Soil Enriched", "Bio-Nutrient Serum applied to Plot #${plotId + 1}", "compost")
            } else {
                audioSystem.playErrorSound()
                haptics.vibrateError()
                showNotification("No Fertilizer", "Purchase Bio-Compost Serum in the Eco Shop", "warning")
            }
        }
    }

    fun harvestPlot(plotId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val crop = repository.harvestPlot(plotId, plots.value)
            if (crop != null) {
                audioSystem.playHarvestCrop()
                haptics.vibrateHarvest()
                val plot = plots.value.find { it.id == plotId }
                if (plot != null) {
                    particleSystem.spawnHarvestLeaves(plot.posX, 0.4f, plot.posZ)
                }
                triggerPlayerActionAnim(PlayerActionAnim.HARVESTING)
                addFloatingText("+${crop.harvestYield} ${crop.displayName}", SolarEmerald)
                showNotification("Harvested!", "Collected +${crop.harvestYield} ${crop.displayName}", "star")
            }
        }
    }

    fun petAnimal(animalId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.petLivestock(animalId, livestock.value)
            val animal = livestock.value.find { it.id == animalId }
            if (animal?.type == LivestockType.CHICKEN) {
                audioSystem.playChickenCluck()
            } else if (animal?.type == LivestockType.CYBER_BOVINE) {
                audioSystem.playCowMoo()
            } else {
                audioSystem.playSheepBaa()
            }
            haptics.vibrateButtonTap()
            addFloatingText("Happiness +15%", SunGold)
            showNotification("Affection", "Pet animal (+2 Eco Prestige)", "favorite")
        }
    }

    fun feedAnimal(animalId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.feedLivestock(animalId, livestock.value)
            if (success) {
                audioSystem.playEatFood()
                haptics.vibrateButtonTap()
                addFloatingText("Fed Animal (+20%)", SolarEmerald)
                showNotification("Fed Animal", "Fed organic grain (+20% Happiness)", "restaurant")
            } else {
                audioSystem.playErrorSound()
                haptics.vibrateError()
                showNotification("No Grain", "Harvest Golden Wheat or buy grain to feed animals", "warning")
            }
        }
    }

    fun collectAnimal(animalId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val animal = livestock.value.find { it.id == animalId }
            val product = repository.collectLivestockProduct(animalId, livestock.value)
            if (product != null) {
                if (animal?.type == LivestockType.CHICKEN) {
                    audioSystem.playCollectEgg()
                    addFloatingText("+1 Fresh Egg", SunGold)
                } else if (animal?.type == LivestockType.CYBER_BOVINE) {
                    audioSystem.playMilkCow()
                    addFloatingText("+1 Bio-Milk", CleanCyan)
                } else {
                    audioSystem.playHarvestCrop()
                    addFloatingText("+1 $product", SolarEmerald)
                }
                haptics.vibrateHarvest()
                showNotification("Collected Product", "Obtained 1x $product", "shopping_bag")
            }
        }
    }

    fun buyEnergyNode(type: EnergyNodeType) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.buyEnergyNode(type)
            if (success) {
                audioSystem.playBuildingComplete()
                haptics.vibrateBuildPlacement()
                showNotification("Built Energy Node", "Constructed ${type.displayName}", "bolt")
            } else {
                audioSystem.playErrorSound()
                haptics.vibrateError()
                showNotification("Insufficient Coins", "Need ${type.buildCostCoins} Sol Coins to build ${type.displayName}", "warning")
            }
        }
    }

    fun upgradeEnergyNode(nodeId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.upgradeEnergyNode(nodeId)
            if (success) {
                audioSystem.playBuildingComplete()
                haptics.vibrateBuildPlacement()
                showNotification("Node Upgraded", "Clean energy output increased by +35%", "arrow_upward")
            } else {
                audioSystem.playErrorSound()
                haptics.vibrateError()
                showNotification("Cannot Upgrade", "Insufficient Sol Coins to upgrade", "warning")
            }
        }
    }

    fun buyLivestock(type: LivestockType) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.buyLivestock(type)
            if (success) {
                if (type == LivestockType.CHICKEN) audioSystem.playChickenCluck()
                else if (type == LivestockType.CYBER_BOVINE) audioSystem.playCowMoo()
                else audioSystem.playSheepBaa()
                haptics.vibrateButtonTap()
                showNotification("Adopted Animal", "Welcomed a new ${type.displayName} to sanctuary", "pets")
            } else {
                audioSystem.playErrorSound()
                haptics.vibrateError()
                showNotification("Insufficient Coins", "Need ${type.purchaseCostCoins} Sol Coins", "warning")
            }
        }
    }

    fun sellItem(itemId: String, qty: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = repository.farmDao.getInventoryItem(itemId)
            val earnedCoins = (item?.sellValue ?: 20) * qty
            val success = repository.sellItem(itemId, qty)
            if (success) {
                audioSystem.playCoinEarned()
                haptics.vibrateButtonTap()
                particleSystem.spawnCoinSparkles(player.posX, 0.9f, player.posZ)
                addFloatingText("+$earnedCoins 🪙", SunGold)
                showNotification("Sold Item", "Earned +$earnedCoins Sol Coins from trade", "monetization_on")
            }
        }
    }

    fun consumeInventoryItem(itemId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val isMilk = itemId == "harvest_milk" || itemId.contains("milk") || itemId == "organic_bio-milk"
            val result = if (isMilk) {
                repository.drinkWater(itemId)
            } else {
                repository.eatFood(itemId)
            }
            if (result.first) {
                if (isMilk) audioSystem.playDrinkWater() else audioSystem.playEatFood()
                haptics.vibrateButtonTap()
                addFloatingText(if (isMilk) "+25 Thirst" else "+15 Hunger", if (isMilk) CleanCyan else SunGold)
                showNotification("Consumed Item", result.second, if (isMilk) "water_drop" else "restaurant")
            } else {
                audioSystem.playErrorSound()
                haptics.vibrateError()
                showNotification("Cannot Consume", result.second, "warning")
            }
        }
    }

    fun buySeed(cropType: CropType, qty: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.buySeed(cropType, qty)
            if (success) {
                audioSystem.playCoinEarned()
                haptics.vibrateButtonTap()
                showNotification("Purchased Seeds", "Bought $qty ${cropType.displayName} Seeds", "shopping_cart")
            } else {
                audioSystem.playErrorSound()
                haptics.vibrateError()
                showNotification("Insufficient Coins", "Need ${cropType.seedCost * qty} Sol Coins", "warning")
            }
        }
    }

    fun claimContract(contractId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.claimContract(contractId)
            if (success) {
                audioSystem.playBuildingComplete()
                haptics.vibrateBuildPlacement()
                addFloatingText("Contract Fulfilled! 🪙", SunGold)
                showNotification("Contract Fulfilled!", "Wholesale Zeppelins dispatched. Bonus coins awarded!", "verified")
            } else {
                audioSystem.playErrorSound()
                haptics.vibrateError()
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
                audioSystem.playBuildingComplete()
                haptics.vibrateBuildPlacement()
                addFloatingText("Crafted $resultName", CleanCyan)
                showNotification("Crafted Successfully!", "Created $resultName in Artisan Workshop", "build")
            } else {
                audioSystem.playErrorSound()
                haptics.vibrateError()
                showNotification("Crafting Failed", "Check battery charge ($energyKwhCost kWh required) and ingredient supplies", "warning")
            }
        }
    }

    fun advanceTimeOfDay(hours: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            fadeBlackAlpha.value = 0.8f
            delay(200)
            repository.gameTick(hours * 60.0f)
            delay(200)
            fadeBlackAlpha.value = 0.0f
            val state = gameState.value ?: return@launch
            showNotification("Time Shift", "Advanced time to ${formatGameTime(state.gameTimeHour)}", "schedule")
        }
    }

    fun changeWeather(weather: WeatherType) {
        viewModelScope.launch(Dispatchers.IO) {
            val state = gameState.value ?: return@launch
            repository.saveGameState(state.copy(currentWeather = weather))
            if (weather == WeatherType.STORM) {
                triggerScreenShake(0.6f)
                audioSystem.playThunderCrack()
            }
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

    override fun onCleared() {
        super.onCleared()
        audioSystem.release()
    }
}
