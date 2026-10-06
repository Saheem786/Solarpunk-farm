package com.example.ui

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ContractEntity
import com.example.data.local.EnergyNodeEntity
import com.example.data.local.GameStateEntity
import com.example.data.local.InventoryEntity
import com.example.data.local.LivestockEntity
import com.example.data.local.NpcEntity
import com.example.data.local.PlacedBuildingEntity
import com.example.data.local.PlotEntity
import com.example.data.model.BiomeType
import com.example.data.model.BuildableType
import com.example.data.model.CropStage
import com.example.data.model.CropType
import com.example.data.model.DeviceType
import com.example.data.model.EnergyGridSummary
import com.example.data.model.EnergyNodeType
import com.example.data.model.FishType
import com.example.data.model.FishingRodTier
import com.example.data.model.FishingSpotType
import com.example.data.model.ItemCategory
import com.example.data.model.LivestockType
import com.example.data.model.NpcArrivalCandidate
import com.example.data.model.NpcRole
import com.example.data.model.PlayerTool
import com.example.data.model.PointOfInterestType
import com.example.data.model.PowerPriority
import com.example.data.model.SettlementStats
import com.example.data.model.TimeOfDayPhase
import com.example.data.model.WeatherType
import com.example.data.model.*
import com.example.data.repository.FarmRepository
import com.example.data.repository.SaveLoadSystem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.random.Random
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
import com.example.game3d.renderer.BiomeSystem
import com.example.game3d.renderer.DayNightLightingSystem
import com.example.game3d.renderer.GhostBuildingState
import com.example.game3d.renderer.LightingState
import com.example.ui.components.DiscoveryAlertData
import com.example.ui.components.FloatingTextData
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SunGold
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class NotificationMessage(
    val title: String,
    val description: String,
    val icon: String = "info"
)

enum class FishingStage {
    CASTING,
    WAITING,
    BITE,
    REELING,
    SUCCESS,
    ESCAPED
}

data class ActiveFishingSession(
    val spot: FishingSpotType,
    val stage: FishingStage,
    val progress: Float = 0f,
    val tension: Float = 0.5f,
    val sweetSpotCenter: Float = 0.5f,
    val caughtFish: FishType? = null,
    val message: String = ""
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

    // Phase 7: Main Menu, Tutorial, Achievements & Radial Quick Access Wheel StateFlows
    val isMainMenuVisible = MutableStateFlow(true)
    val showTutorialOverlay = MutableStateFlow(false)
    val tutorialStep = MutableStateFlow(1)
    val unlockedAchievements = MutableStateFlow<Set<String>>(setOf("ach_first_steps"))
    val isQuickWheelVisible = MutableStateFlow(false)

    fun closeMainMenu() {
        isMainMenuVisible.value = false
        if (prefs.getBoolean("show_tutorial_first", true)) {
            showTutorialOverlay.value = true
        }
    }

    fun openMainMenu() {
        isMainMenuVisible.value = true
    }

    fun nextTutorialStep() {
        if (tutorialStep.value < 5) {
            tutorialStep.value += 1
            audioSystem.playButtonTap()
        } else {
            skipTutorial()
        }
    }

    fun skipTutorial() {
        showTutorialOverlay.value = false
        prefs.edit().putBoolean("show_tutorial_first", false).apply()
        audioSystem.playBuildingComplete()
        showNotification("Welcome to Solarpunk Farm!", "Explore Green Valley, harvest crops & build solar panels.", "eco")
    }

    fun unlockAchievement(achId: String) {
        if (!unlockedAchievements.value.contains(achId)) {
            val updated = unlockedAchievements.value + achId
            unlockedAchievements.value = updated
            audioSystem.playDiscoverySparkle()
            haptics.vibrateSuccess()
            showNotification("ACHIEVEMENT UNLOCKED!", achId.replace("ach_", "").replace("_", " ").uppercase(), "trophy")
        }
    }

    fun toggleQuickWheel() {
        isQuickWheelVisible.value = !isQuickWheelVisible.value
        audioSystem.playMenuWhoosh()
    }

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

    // Biome Exploration & Discovery State
    val discoveredChunks = MutableStateFlow<Set<String>>(setOf("9_9", "9_10", "10_9", "10_10"))
    val discoveredPois = MutableStateFlow<Set<String>>(setOf("poi_old_farm"))
    val discoveredBiomes = MutableStateFlow<Set<String>>(setOf("green_valley"))
    val currentBiome = MutableStateFlow(BiomeType.GREEN_VALLEY)
    val discoveryAlert = MutableStateFlow<DiscoveryAlertData?>(null)
    private var lastBoundaryWarningTime = 0L

    // Phase 3: Fishing Mechanics & Mini-Game State
    val activeFishingSession = MutableStateFlow<ActiveFishingSession?>(null)

    // Phase 4: Energy Grid & Power Management StateFlows
    val energySummary: StateFlow<EnergyGridSummary> = repository.energySummary
    val devicePriorities: StateFlow<Map<DeviceType, PowerPriority>> = repository.devicePriorities
    val unlockedTechs: StateFlow<Set<String>> = repository.unlockedTechs

    // Phase 5: Settlement & Survivor StateFlows
    val npcs: StateFlow<List<NpcEntity>> = repository.npcs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val pendingArrival: StateFlow<NpcArrivalCandidate?> = repository.pendingArrival
    val settlementStats: StateFlow<SettlementStats> = repository.settlementStats
    val selectedNpcForDialogue = MutableStateFlow<NpcEntity?>(null)

    // Phase 6: Research Tree, Story Missions, Discovery & Terminals StateFlows
    val researchPoints: StateFlow<Int> = gameState.map { it?.researchPoints ?: 20 }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 20
    )
    val storyMissions: StateFlow<List<StoryMission>> = repository.storyMissions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val activeMission: StateFlow<StoryMission?> = storyMissions.map { list ->
        list.find { it.isUnlocked && !it.isCompleted } ?: list.firstOrNull { !it.isCompleted }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val pointsOfInterest: StateFlow<List<PointOfInterestData>> = repository.pointsOfInterest.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val loreEntries: StateFlow<List<LoreEntryData>> = repository.loreEntries.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val terminalLogs: StateFlow<List<TerminalLogData>> = repository.terminalLogs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val storyEndingUnlocked: StateFlow<Boolean> = gameState.map { it?.storyEndingUnlocked ?: false }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )
    val activeTerminalForModal = MutableStateFlow<TerminalLogData?>(null)

    fun hackTerminal(terminalId: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.hackTerminal(terminalId, password)
            viewModelScope.launch(Dispatchers.Main) {
                if (success) {
                    audioSystem.playCraftSuccess()
                    haptics.vibrateSuccess()
                    addFloatingText("Terminal Decrypted! +25 RP", CleanCyan)
                    showNotification("Decrypted Log!", "Gained +25 RP and lore entry.", "terminal")
                    openModal(null)
                } else {
                    audioSystem.playErrorSound()
                    haptics.vibrateError()
                    showNotification("Access Denied", "Incorrect password. Hint: SOLARIS", "warning")
                }
            }
        }
    }

    fun openTerminalModal(terminal: TerminalLogData) {
        activeTerminalForModal.value = terminal
        openModal("terminal_hack")
    }

    fun acceptNpcArrival() {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.acceptPendingArrival()
            viewModelScope.launch(Dispatchers.Main) {
                if (success) {
                    audioSystem.playCraftSuccess()
                    haptics.vibrateSuccess()
                    addFloatingText("Survivor Welcomed!", SolarEmerald)
                    showNotification("Welcome to Sanctuary!", "A new survivor joined your settlement.", "check_circle")
                }
            }
        }
    }

    fun rejectNpcArrival() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.rejectPendingArrival()
            viewModelScope.launch(Dispatchers.Main) {
                haptics.vibrateButtonTap()
                showNotification("Arrival Postponed", "The traveler will return in 3 days.", "info")
            }
        }
    }

    fun interactWithNpc(npcId: Int, optionKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val (success, message) = repository.interactWithNpc(npcId, optionKey)
            viewModelScope.launch(Dispatchers.Main) {
                haptics.vibrateButtonTap()
                if (success) {
                    audioSystem.playButtonTap()
                    showNotification("Dialogue", message, "chat")
                } else {
                    audioSystem.playErrorSound()
                }
            }
        }
    }

    fun assignNpcRole(npcId: Int, newRole: NpcRole) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.assignNpcRole(npcId, newRole)
            viewModelScope.launch(Dispatchers.Main) {
                if (success) {
                    audioSystem.playButtonTap()
                    haptics.vibrateButtonTap()
                    showNotification("Role Assigned", "NPC role updated to ${newRole.displayName}", "work")
                }
            }
        }
    }

    fun openNpcDialogue(npc: NpcEntity) {
        selectedNpcForDialogue.value = npc
        openModal("npc_dialogue")
    }

    fun setDevicePriority(device: DeviceType, priority: PowerPriority) {
        repository.setDevicePriority(device, priority)
        haptics.vibrateButtonTap()
        audioSystem.playButtonTap()
        showNotification("Priority Updated", "${device.displayName} set to ${priority.displayName}", "bolt")
    }

    fun unlockEnergyTech(techId: String, requiredRp: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.unlockEnergyTech(techId, requiredRp)
            viewModelScope.launch(Dispatchers.Main) {
                if (success) {
                    audioSystem.playCraftSuccess()
                    haptics.vibrateSuccess()
                    addFloatingText("Tech Unlocked!", SolarEmerald)
                    showNotification("Research Unlocked!", "Energy grid capability upgraded", "bolt")
                } else {
                    audioSystem.playErrorSound()
                    haptics.vibrateError()
                    showNotification("Not Enough RP", "Earn more Eco Prestige through harvesting & clean energy", "warning")
                }
            }
        }
    }

    init {
        repository.onPowerLowAlert = { message ->
            viewModelScope.launch(Dispatchers.Main) {
                audioSystem.playLowEnergyWarning()
                haptics.vibrateError()
                showNotification("Power Grid Alert", message, "warning")
            }
        }

        // Apply saved volume settings
        audioSystem.masterVolume = masterVolume.value
        audioSystem.musicVolume = musicVolume.value
        audioSystem.sfxVolume = sfxVolume.value
        haptics.vibrationEnabled = vibrationEnabled.value

        viewModelScope.launch(Dispatchers.IO) {
            if (SaveLoadSystem.hasSave(getApplication())) {
                SaveLoadSystem.loadGame(getApplication(), repository.farmDao)
                val savedState = repository.gameState.firstOrNull()
                if (savedState != null) {
                    val px = if (savedState.playerX.isNaN() || savedState.playerX.isInfinite() || kotlin.math.abs(savedState.playerX) > 200f) 0.0f else savedState.playerX
                    val py = if (savedState.playerY.isNaN() || savedState.playerY.isInfinite() || savedState.playerY < 0f || savedState.playerY > 50f) 0.0f else savedState.playerY
                    val pz = if (savedState.playerZ.isNaN() || savedState.playerZ.isInfinite() || kotlin.math.abs(savedState.playerZ) > 200f) 0.0f else savedState.playerZ
                    player.posX = px
                    player.posY = py
                    player.posZ = pz
                    player.orientationAngleDeg = savedState.playerAngle
                    camera.instantReset(px, py, pz, yaw = 180.0f, pitch = 22.0f, dist = 5.5f)
                    android.util.Log.d("FarmViewModel", "LOAD_GAME_SPAWN player=($px,$py,$pz)")

                    val chunks = savedState.discoveredChunks.split(",").filter { it.isNotBlank() }.toSet()
                    if (chunks.isNotEmpty()) discoveredChunks.value = chunks
                    val pois = savedState.discoveredPois.split(",").filter { it.isNotBlank() }.toSet()
                    if (pois.isNotEmpty()) discoveredPois.value = pois
                    val biomes = savedState.discoveredBiomes.split(",").filter { it.isNotBlank() }.toSet()
                    if (biomes.isNotEmpty()) discoveredBiomes.value = biomes
                    currentBiome.value = BiomeType.fromPosition(savedState.playerZ)
                }
            } else {
                repository.initializeDefaultDataIfEmpty()
                player.posX = 0.0f
                player.posY = 0.0f
                player.posZ = 0.0f
                player.orientationAngleDeg = 0.0f
                camera.instantReset(0.0f, 0.0f, 0.0f, yaw = 225.0f, pitch = 22.0f, dist = 5.5f)
                android.util.Log.d("FarmViewModel", "NEW_GAME_SPAWN player=(0.0,0.0,0.0)")
            }
            // Dismiss initial loading after game state is ready
            delay(900)
            isInitialLoading.value = false
        }
        startGameLoop()
        startAutoSaveLoop()
    }

    fun dismissDiscoveryAlert() {
        discoveryAlert.value = null
    }

    fun triggerDiscoveryAlert(alert: DiscoveryAlertData) {
        viewModelScope.launch {
            discoveryAlert.value = alert
            delay(4800)
            if (discoveryAlert.value?.id == alert.id) {
                discoveryAlert.value = null
            }
        }
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
                    playerAngle = player.orientationAngleDeg,
                    discoveredPois = discoveredPois.value.joinToString(","),
                    discoveredChunks = discoveredChunks.value.joinToString(","),
                    currentBiomeId = currentBiome.value.id
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
                playerAngle = player.orientationAngleDeg,
                discoveredPois = discoveredPois.value.joinToString(","),
                discoveredChunks = discoveredChunks.value.joinToString(","),
                currentBiomeId = currentBiome.value.id
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
                val px = if (loadedState.playerX.isNaN() || loadedState.playerX.isInfinite() || kotlin.math.abs(loadedState.playerX) > 200f) 0.0f else loadedState.playerX
                val py = if (loadedState.playerY.isNaN() || loadedState.playerY.isInfinite() || loadedState.playerY < 0f || loadedState.playerY > 50f) 0.0f else loadedState.playerY
                val pz = if (loadedState.playerZ.isNaN() || loadedState.playerZ.isInfinite() || kotlin.math.abs(loadedState.playerZ) > 200f) 0.0f else loadedState.playerZ
                player.posX = px
                player.posY = py
                player.posZ = pz
                player.orientationAngleDeg = loadedState.playerAngle
                camera.instantReset(px, py, pz, yaw = 180.0f, pitch = 22.0f, dist = 5.5f)
                android.util.Log.d("FarmViewModel", "LOAD_GAME_SPAWN player=($px,$py,$pz)")

                val chunks = loadedState.discoveredChunks.split(",").filter { it.isNotBlank() }.toSet()
                if (chunks.isNotEmpty()) discoveredChunks.value = chunks
                val pois = loadedState.discoveredPois.split(",").filter { it.isNotBlank() }.toSet()
                if (pois.isNotEmpty()) discoveredPois.value = pois
                currentBiome.value = BiomeType.fromPosition(loadedState.playerZ)
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
            camera.instantReset(0.0f, 0.0f, 0.0f, yaw = 225.0f, pitch = 22.0f, dist = 5.5f)
            android.util.Log.d("FarmViewModel", "NEW_GAME_SPAWN player=(0.0,0.0,0.0)")
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
                val weatherSpeedMultiplier = if (weather == WeatherType.STORM) 0.85f else 1.0f

                // Biome calculation & terrain drag modifier
                val activeBiome = BiomeType.fromPosition(player.posZ)
                if (currentBiome.value != activeBiome) {
                    currentBiome.value = activeBiome
                }

                // Check if player entered a new Biome zone for the first time
                if (!discoveredBiomes.value.contains(activeBiome.id)) {
                    discoveredBiomes.value = discoveredBiomes.value + activeBiome.id
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.discoverBiome(activeBiome)
                    }
                    audioSystem.playBuildingComplete()
                    haptics.vibrateBuildPlacement()
                    particleSystem.spawnCoinSparkles(player.posX, 1.0f, player.posZ)
                    addFloatingText("+50🪙 Eco Bonus", SunGold)

                    val biomeColor = when (activeBiome) {
                        BiomeType.DEEP_FOREST -> Color(0xFF81C784)
                        BiomeType.WETLAND -> CleanCyan
                        BiomeType.GREEN_VALLEY -> SolarEmerald
                    }

                    triggerDiscoveryAlert(
                        DiscoveryAlertData(
                            title = "Discovered: ${activeBiome.displayName}",
                            subtitle = "${activeBiome.description} (+50 Sol Coins, +10 Eco)",
                            categoryLabel = "NEW BIOME DISCOVERED",
                            icon = androidx.compose.material.icons.Icons.Default.Info,
                            accentColor = biomeColor,
                            rewardCoins = 50
                        )
                    )
                }

                val biomeSpeedMultiplier = BiomeSystem.getMovementSpeedMultiplier(player.posX, player.posZ)
                val effectiveSpeedMultiplier = weatherSpeedMultiplier * biomeSpeedMultiplier

                player.update(
                    input = if (_isBuildMode.value) PlayerInputState() else _inputState.value.copy(isSprinting = effectiveSprinting),
                    cameraYawDeg = camera.yawDeg,
                    deltaSec = deltaSec,
                    speedMultiplier = effectiveSpeedMultiplier,
                    placedBuildings = placedBuildings.value,
                    energyNodes = energyNodes.value,
                    plots = plots.value
                )

                // Fog of War chunk uncovering around player (35m radius)
                val updatedChunks = BiomeSystem.uncoverChunksAround(player.posX, player.posZ, discoveredChunks.value, 35.0f)
                if (updatedChunks.size > discoveredChunks.value.size) {
                    discoveredChunks.value = updatedChunks
                }

                // Point of Interest Proximity Discovery Check
                val undiscoveredPoi = BiomeSystem.checkPoiDiscovery(player.posX, player.posZ, discoveredPois.value, 14.0f)
                if (undiscoveredPoi != null) {
                    discoveredPois.value = discoveredPois.value + undiscoveredPoi.id
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.discoverPoi(undiscoveredPoi)
                    }
                    audioSystem.playBuildingComplete()
                    haptics.vibrateBuildPlacement()
                    particleSystem.spawnCoinSparkles(player.posX, 1.2f, player.posZ)
                    addFloatingText("+${undiscoveredPoi.rewardCoins}🪙 ${undiscoveredPoi.displayName}", SunGold)

                    val poiColor = when (undiscoveredPoi.biome) {
                        BiomeType.DEEP_FOREST -> Color(0xFFFFD54F)
                        BiomeType.WETLAND -> CleanCyan
                        BiomeType.GREEN_VALLEY -> SolarEmerald
                    }

                    triggerDiscoveryAlert(
                        DiscoveryAlertData(
                            title = "Discovered: ${undiscoveredPoi.displayName}",
                            subtitle = "${undiscoveredPoi.discoveryRewardDesc} (+${undiscoveredPoi.rewardCoins} Sol Coins)",
                            categoryLabel = "NEW POINT OF INTEREST",
                            icon = androidx.compose.material.icons.Icons.Default.Star,
                            accentColor = poiColor,
                            rewardCoins = undiscoveredPoi.rewardCoins,
                            rewardItemName = undiscoveredPoi.rewardItemName
                        )
                    )
                }

                // World Boundary Warning when approaching world perimeter (±192m)
                val isNearBoundary = kotlin.math.abs(player.posX) >= 192.0f || kotlin.math.abs(player.posZ) >= 192.0f
                if (isNearBoundary) {
                    val nowMs = System.currentTimeMillis()
                    if (nowMs - lastBoundaryWarningTime > 8000L) {
                        lastBoundaryWarningTime = nowMs
                        showNotification(
                            title = "World Edge",
                            desc = "The world ends here. More areas coming soon.",
                            icon = "warning"
                        )
                    }
                }

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
                _lightingState.value = DayNightLightingSystem.calculateLighting(hour, weather, animTime.value)
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
            camera.desiredPitchDeg = 35.0f
            camera.desiredDistance = 7.0f
            showNotification("Build Mode Active", "Select structure, rotate & place in the sanctuary", "build")
        } else {
            audioSystem.playMenuWhoosh()
            camera.desiredPitchDeg = 22.0f
            camera.desiredDistance = 5.5f
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

        // 1. Hydro Generator: Must be near flowing river or stream (not pond)
        if (type == BuildableType.HYDRO_GENERATOR) {
            val nearStream = kotlin.math.abs(gx - 16.5f) <= 6.0f && gz in -25.0f..25.0f
            val nearRiver = kotlin.math.abs(gz - (-100.0f)) <= 12.0f
            if (!nearStream && !nearRiver) {
                return Pair(false, "Must be near flowing river or stream (river, not pond)")
            }
        }

        // 2. Biogas Generator: Must be near compost bin or animal pen
        if (type == BuildableType.BIOGAS_GENERATOR) {
            val nearCompost = existingBuildings.any {
                it.buildingType == BuildableType.COMPOST_BIN &&
                kotlin.math.sqrt(((gx - it.posX) * (gx - it.posX) + (gz - it.posZ) * (gz - it.posZ)).toDouble()) <= 8.5
            }
            val nearPen = (gx in -22.0f..-4.0f && gz in -4.0f..18.0f)
            if (!nearCompost && !nearPen) {
                return Pair(false, "Must be near compost bin or animal pen")
            }
        }

        // 3. Geothermal Vent: Near mountain region or thermal hotspot
        if (type == BuildableType.GEOTHERMAL_VENT) {
            val nearMountain = gz >= 30.0f
            val nearHotspot = kotlin.math.sqrt(((gx - (-28.0f)) * (gx - (-28.0f)) + (gz - 20.0f) * (gz - 20.0f)).toDouble()) <= 14.0
            if (!nearMountain && !nearHotspot) {
                return Pair(false, "Must be near mountain region or thermal hotspot")
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
                camera.desiredPitchDeg = 22.0f
                camera.desiredDistance = 5.5f
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
                if (selectedTool.value == PlayerTool.FISHING_ROD) {
                    val spot = when (prompt.targetId) {
                        993 -> FishingSpotType.WETLAND_RIVER
                        992 -> FishingSpotType.WETLAND_POND
                        else -> FishingSpotType.GREEN_VALLEY_STREAM
                    }
                    startFishing(spot)
                } else if (prompt.waterTypeKey == "well") {
                    drinkCleanWater("Water Well")
                } else {
                    drinkRawWater(prompt.title)
                }
            }
            InteractionTargetType.WATER_BUILDING -> {
                when (prompt.waterTypeKey) {
                    "barrel" -> drinkCleanWater("Rain Barrel")
                    "filter" -> fillOrCollectFilter(prompt.targetId)
                    "purifier" -> collectFromBuilding(prompt.targetId)
                    "storage" -> collectFromBuilding(prompt.targetId)
                    else -> collectFromBuilding(prompt.targetId)
                }
            }
            InteractionTargetType.CAMPFIRE -> {
                openModal("campfire_modal")
            }
            InteractionTargetType.FARMHOUSE -> {
                restInFarmhouse()
            }
            InteractionTargetType.NPC -> {
                val npc = npcs.value.find { it.id == prompt.targetId }
                if (npc != null) {
                    openNpcDialogue(npc)
                } else {
                    openModal("sanctuary")
                }
            }
            InteractionTargetType.NONE -> {}
        }
    }

    fun onContextSecondaryActionButton() {
        val prompt = _currentPrompt.value ?: return
        haptics.vibrateButtonTap()
        when (prompt.targetType) {
            InteractionTargetType.WATER_SOURCE -> {
                val spot = when (prompt.targetId) {
                    993 -> FishingSpotType.WETLAND_RIVER
                    992 -> FishingSpotType.WETLAND_POND
                    else -> FishingSpotType.GREEN_VALLEY_STREAM
                }
                startFishing(spot)
            }
            InteractionTargetType.CAMPFIRE -> {
                openModal("campfire_modal")
            }
            InteractionTargetType.WATER_BUILDING -> {
                if (prompt.waterTypeKey == "filter") {
                    fillFilter(prompt.targetId)
                } else {
                    collectFromBuilding(prompt.targetId)
                }
            }
            else -> onContextActionButton()
        }
    }

    fun drinkCleanWater(source: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val (success, message) = repository.drinkWater(null, source)
            if (success) {
                audioSystem.playDrinkWater()
                haptics.vibrateButtonTap()
                addFloatingText("+40 Thirst (Clean)", CleanCyan)
                showNotification("Clean Water", message, "water_drop")
            } else {
                audioSystem.playErrorSound()
                showNotification("Cannot Drink", message, "warning")
            }
        }
    }

    fun drinkRawWater(source: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val (success, message) = repository.drinkWater(null, source)
            if (success) {
                audioSystem.playDrinkWater()
                haptics.vibrateError()
                triggerScreenShake(0.35f)
                addFloatingText("+30 Thirst ⚠️ SICK!", Color(0xFF81C784))
                showNotification("Raw Water (Contaminated!)", "Restored +30 Thirst, but you got SICK! Stamina halves, HP drains slowly.", "warning")
            }
        }
    }

    fun fillFilter(buildingId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val (success, message) = repository.fillFilter(buildingId)
            if (success) {
                audioSystem.playWaterSound()
                haptics.vibrateButtonTap()
                showNotification("Water Filter", message, "water_drop")
            } else {
                audioSystem.playErrorSound()
                showNotification("Cannot Fill Filter", message, "warning")
            }
        }
    }

    fun collectFromBuilding(buildingId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val (success, message) = repository.collectFromBuilding(buildingId)
            if (success) {
                audioSystem.playWaterSound()
                haptics.vibrateButtonTap()
                addFloatingText("+Clean Water", CleanCyan)
                showNotification("Collected Water", message, "water_drop")
            } else {
                audioSystem.playErrorSound()
                showNotification("Water Building", message, "warning")
            }
        }
    }

    fun fillOrCollectFilter(buildingId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val state = gameState.value ?: return@launch
            if (state.rawWaterCarried > 0) {
                fillFilter(buildingId)
            } else {
                collectFromBuilding(buildingId)
            }
        }
    }

    fun boilWater() {
        viewModelScope.launch(Dispatchers.IO) {
            val (success, message) = repository.boilWater()
            if (success) {
                audioSystem.playCraftSuccess()
                haptics.vibrateButtonTap()
                addFloatingText("+1 Clean Water", CleanCyan)
                showNotification("Boiled Water", message, "whatshot")
            } else {
                audioSystem.playErrorSound()
                showNotification("Cannot Boil", message, "warning")
            }
        }
    }

    fun cookFish(fishItemId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val (success, message) = repository.cookFish(fishItemId)
            if (success) {
                audioSystem.playCraftSuccess()
                haptics.vibrateButtonTap()
                addFloatingText("+Grilled Fish", SunGold)
                showNotification("Cooked Fish", message, "restaurant")
            } else {
                audioSystem.playErrorSound()
                showNotification("Cannot Cook", message, "warning")
            }
        }
    }

    fun startFishing(spot: FishingSpotType) {
        val state = gameState.value ?: return
        val rodTier = FishingRodTier.fromTier(state.fishingRodTier)
        if (rodTier == FishingRodTier.NONE) {
            showNotification("No Fishing Rod", "Equip or purchase a Fishing Rod from Sol City Market!", "warning")
            return
        }
        selectTool(PlayerTool.FISHING_ROD)
        openModal("fishing_minigame")
        activeFishingSession.value = ActiveFishingSession(spot, FishingStage.CASTING)
        audioSystem.playSelectToolSound()

        viewModelScope.launch {
            delay(1200)
            activeFishingSession.value = activeFishingSession.value?.copy(stage = FishingStage.WAITING)
            val waitSec = Random.nextDouble(rodTier.minWaitSec.toDouble(), rodTier.maxWaitSec.toDouble()).toFloat()
            delay((waitSec * 1000).toLong())
            if (activeFishingSession.value?.stage == FishingStage.WAITING) {
                haptics.vibrateHarvest()
                audioSystem.playWaterSound()
                activeFishingSession.value = activeFishingSession.value?.copy(stage = FishingStage.BITE)
            }
        }
    }

    fun hookFish() {
        val session = activeFishingSession.value ?: return
        if (session.stage == FishingStage.BITE) {
            haptics.vibrateButtonTap()
            audioSystem.playWaterSound()
            activeFishingSession.value = session.copy(stage = FishingStage.REELING, progress = 0.25f, sweetSpotCenter = 0.5f)
        }
    }

    fun reelFishTick(isPressingReel: Boolean) {
        val session = activeFishingSession.value ?: return
        if (session.stage != FishingStage.REELING) return

        val tensionDelta = if (isPressingReel) 0.045f else -0.035f
        val newTension = (session.tension + tensionDelta).coerceIn(0f, 1f)
        val sweetMin = (session.sweetSpotCenter - 0.20f).coerceAtLeast(0f)
        val sweetMax = (session.sweetSpotCenter + 0.20f).coerceAtMost(1f)

        val inSweetSpot = newTension in sweetMin..sweetMax
        val progressDelta = if (inSweetSpot) 0.035f else -0.025f
        val newProgress = (session.progress + progressDelta).coerceIn(0f, 1f)

        val sweetShift = (Random.nextFloat() - 0.5f) * 0.04f
        val newSweetCenter = (session.sweetSpotCenter + sweetShift).coerceIn(0.2f, 0.8f)

        if (newProgress >= 1.0f) {
            viewModelScope.launch(Dispatchers.IO) {
                val rod = FishingRodTier.fromTier(gameState.value?.fishingRodTier ?: 1)
                val (fish, msg) = repository.catchFish(session.spot, rod)
                audioSystem.playCoinEarned()
                haptics.vibrateBuildPlacement()
                activeFishingSession.value = session.copy(
                    stage = FishingStage.SUCCESS,
                    progress = 1.0f,
                    caughtFish = fish,
                    message = msg
                )
                if (fish != null) {
                    addFloatingText("+${fish.displayName}!", SunGold)
                }
            }
        } else if (newProgress <= 0.0f && session.progress > 0.05f) {
            audioSystem.playErrorSound()
            haptics.vibrateError()
            activeFishingSession.value = session.copy(
                stage = FishingStage.ESCAPED,
                message = "The fish got away! The line went slack."
            )
        } else {
            activeFishingSession.value = session.copy(
                tension = newTension,
                progress = newProgress,
                sweetSpotCenter = newSweetCenter
            )
        }
    }

    fun closeFishingModal() {
        activeFishingSession.value = null
        openModal(null)
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
