package com.example.ui.game3d

import android.opengl.GLSurfaceView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.data.model.FishingRodTier
import com.example.data.model.WeatherType
import com.example.game3d.opengl.GLWorldRenderer
import com.example.ui.FarmViewModel
import com.example.ui.components.DiscoveryToastAlert
import com.example.ui.components.FloatingTextOverlay
import com.example.ui.components.SolarpunkNotificationBanner
import com.example.ui.components.SurvivalStatsHUD
import com.example.ui.components.ToolSelectorDock
import com.example.ui.components.TopGameStatsBar
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Science
import com.example.data.repository.SaveLoadSystem
import com.example.ui.screens.AchievementsModal
import com.example.ui.screens.EndingSequenceModal
import com.example.ui.screens.EnergyTabModal
import com.example.ui.screens.JournalTabModal
import com.example.ui.screens.NpcDialogueModal
import com.example.ui.screens.ResearchTabModal
import com.example.ui.screens.SanctuaryTabModal
import com.example.ui.screens.TerminalHackModal
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SunGold
import kotlinx.coroutines.launch

@Composable
fun Game3DScreen(
    viewModel: FarmViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val plots by viewModel.plots.collectAsStateWithLifecycle()
    val energyNodes by viewModel.energyNodes.collectAsStateWithLifecycle()
    val livestock by viewModel.livestock.collectAsStateWithLifecycle()
    val inventory by viewModel.inventory.collectAsStateWithLifecycle()
    val placedBuildings by viewModel.placedBuildings.collectAsStateWithLifecycle(emptyList())
    val isBuildMode by viewModel.isBuildMode.collectAsStateWithLifecycle()
    val selectedBuildType by viewModel.selectedBuildType.collectAsStateWithLifecycle()
    val buildRotationDeg by viewModel.buildRotationDeg.collectAsStateWithLifecycle()
    val ghostBuilding by viewModel.ghostBuildingState.collectAsStateWithLifecycle()
    val inputState by viewModel.inputState.collectAsStateWithLifecycle()
    val selectedTool by viewModel.selectedTool.collectAsStateWithLifecycle()
    val currentPrompt by viewModel.currentPrompt.collectAsStateWithLifecycle()
    val lightingState by viewModel.lightingState.collectAsStateWithLifecycle()
    val activeModal by viewModel.activeModal.collectAsStateWithLifecycle()
    val selectedPlotForModal by viewModel.selectedPlotForModal.collectAsStateWithLifecycle()
    val bannerNotification by viewModel.bannerNotification.collectAsStateWithLifecycle()
    val isFainted by viewModel.isFainted.collectAsStateWithLifecycle()
    val faintCountdown by viewModel.faintCountdown.collectAsStateWithLifecycle()
    val animTime by viewModel.animTime.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isInitialLoading by viewModel.isInitialLoading.collectAsStateWithLifecycle()
    val ecosystemHealth by viewModel.ecosystemHealth.collectAsStateWithLifecycle()
    val floatingTexts by viewModel.floatingTexts.collectAsStateWithLifecycle()
    val playerActionAnim by viewModel.playerActionAnim.collectAsStateWithLifecycle()
    val playerActionProgress by viewModel.playerActionProgress.collectAsStateWithLifecycle()
    val screenShake by viewModel.screenShake.collectAsStateWithLifecycle()
    val fadeBlackAlpha by viewModel.fadeBlackAlpha.collectAsStateWithLifecycle()

    val masterVol by viewModel.masterVolume.collectAsStateWithLifecycle()
    val musicVol by viewModel.musicVolume.collectAsStateWithLifecycle()
    val sfxVol by viewModel.sfxVolume.collectAsStateWithLifecycle()
    val vibrationOn by viewModel.vibrationEnabled.collectAsStateWithLifecycle()

    val discoveredChunks by viewModel.discoveredChunks.collectAsStateWithLifecycle()
    val discoveredPois by viewModel.discoveredPois.collectAsStateWithLifecycle()
    val currentBiome by viewModel.currentBiome.collectAsStateWithLifecycle()
    val discoveryAlert by viewModel.discoveryAlert.collectAsStateWithLifecycle()
    val activeFishingSession by viewModel.activeFishingSession.collectAsStateWithLifecycle()
    val energySummary by viewModel.energySummary.collectAsStateWithLifecycle()
    val unlockedTechs by viewModel.unlockedTechs.collectAsStateWithLifecycle()
    val pendingArrival by viewModel.pendingArrival.collectAsStateWithLifecycle()
    val selectedNpcForDialogue by viewModel.selectedNpcForDialogue.collectAsStateWithLifecycle()

    val researchPoints by viewModel.researchPoints.collectAsStateWithLifecycle()
    val storyMissions by viewModel.storyMissions.collectAsStateWithLifecycle()
    val activeMission by viewModel.activeMission.collectAsStateWithLifecycle()
    val pointsOfInterest by viewModel.pointsOfInterest.collectAsStateWithLifecycle()
    val loreEntries by viewModel.loreEntries.collectAsStateWithLifecycle()
    val terminalLogs by viewModel.terminalLogs.collectAsStateWithLifecycle()
    val storyEndingUnlocked by viewModel.storyEndingUnlocked.collectAsStateWithLifecycle()
    val activeTerminalForModal by viewModel.activeTerminalForModal.collectAsStateWithLifecycle()

    val isMainMenuVisible by viewModel.isMainMenuVisible.collectAsStateWithLifecycle()
    val showTutorialOverlay by viewModel.showTutorialOverlay.collectAsStateWithLifecycle()
    val tutorialStep by viewModel.tutorialStep.collectAsStateWithLifecycle()
    val unlockedAchievements by viewModel.unlockedAchievements.collectAsStateWithLifecycle()
    val isQuickWheelVisible by viewModel.isQuickWheelVisible.collectAsStateWithLifecycle()

    var showActionsMenu by remember { mutableStateOf(false) }

    val glRenderer = remember { GLWorldRenderer() }
    var glView by remember { mutableStateOf<GLSurfaceView?>(null) }

    DisposableEffect(glView) {
        glView?.onResume()
        onDispose {
            try {
                glView?.onPause()
            } catch (_: Exception) {}
        }
    }

    // Synchronize latest state with GLWorldRenderer
    glRenderer.playerRef = viewModel.player
    glRenderer.cameraRef = viewModel.camera
    glRenderer.lightingRef = lightingState
    glRenderer.plotsRef = plots
    glRenderer.energyNodesRef = energyNodes
    glRenderer.placedBuildingsRef = placedBuildings
    glRenderer.ghostBuildingRef = ghostBuilding
    glRenderer.livestockRef = livestock
    glRenderer.animTimeSec = animTime
    glRenderer.weatherRef = gameState?.currentWeather ?: WeatherType.SUNNY_CLEAR
    glRenderer.hourRef = gameState?.gameTimeHour ?: 12.0f
    glRenderer.particleSystemRef = viewModel.particleSystem
    glRenderer.actionAnimRef = playerActionAnim
    glRenderer.actionProgressRef = playerActionProgress
    glRenderer.screenShakeRef = screenShake

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("game_3d_screen")
    ) {
        // 1. Hardware Accelerated OpenGL ES 2.0/3.0 3D World View
        AndroidView(
            factory = { ctx ->
                GLSurfaceView(ctx).apply {
                    preserveEGLContextOnPause = true
                    setEGLContextClientVersion(2)
                    setEGLConfigChooser(8, 8, 8, 8, 16, 0)
                    holder.setFormat(android.graphics.PixelFormat.OPAQUE)
                    setRenderer(glRenderer)
                    renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                    glView = this
                }
            },
            onRelease = { view ->
                try {
                    view.onPause()
                } catch (_: Exception) {}
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Camera Touch Drag & Pinch Zoom Area
        CameraTouchArea(
            onRotate = { dy, dp -> viewModel.rotateCamera(dy, dp) },
            onZoom = { dz -> viewModel.zoomCamera(dz) },
            modifier = Modifier.fillMaxSize()
        )

        // 3. HUD Top Bar (Top-Left Day/Time/Weather, Top-Center Survival Bars, Top-Right Money/Energy/Eco)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            TopGameStatsBar(
                state = gameState,
                onAdvanceTimeClick = { viewModel.advanceTimeOfDay(2.0f) },
                plots = plots,
                ecosystemScore = ecosystemHealth,
                energySummary = energySummary,
                researchPoints = researchPoints,
                activeMission = activeMission,
                onEnergyClick = { viewModel.openModal("energy_grid") },
                onResearchClick = { viewModel.openModal("research") },
                onJournalClick = { viewModel.openModal("journal") }
            )
            SolarpunkNotificationBanner(
                notification = bannerNotification
            )
        }

        // 3B. Mini-Map HUD (Top-Left Corner, below Day/Time bar)
        MiniMapHUD(
            playerX = viewModel.player.posX,
            playerZ = viewModel.player.posZ,
            playerAngleDeg = viewModel.player.orientationAngleDeg,
            discoveredPois = discoveredPois,
            discoveredChunks = discoveredChunks,
            currentBiome = currentBiome,
            plots = plots,
            placedBuildings = placedBuildings,
            energyNodes = energyNodes,
            onClick = {
                viewModel.openModal("world_map")
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 42.dp)
        )

        // 4. Primary Right-Side Action Buttons Column (Build, Bag, Save, Menu)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 14.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Build Button (Hammer Icon)
                QuickActionCircleButton(
                    icon = Icons.Default.Construction,
                    label = if (isBuildMode) "Exit" else "Build",
                    color = if (isBuildMode) SunGold else SolarEmerald,
                    active = isBuildMode,
                    testTag = "btn_build_mode",
                    onClick = { viewModel.toggleBuildMode() }
                )

                // 2. Bag Button (Backpack Icon)
                QuickActionCircleButton(
                    icon = Icons.Default.Inventory2,
                    label = "Bag",
                    color = CleanCyan,
                    testTag = "btn_inventory",
                    onClick = { viewModel.openModal("inventory") }
                )

                // 3. Energy Grid Button (Bolt Icon)
                QuickActionCircleButton(
                    icon = Icons.Default.Bolt,
                    label = "Grid",
                    color = CleanCyan,
                    testTag = "btn_energy_grid",
                    onClick = { viewModel.openModal("energy_grid") }
                )

                // 4. Sanctuary Button (People Icon)
                QuickActionCircleButton(
                    icon = Icons.Default.People,
                    label = "Sanctuary",
                    color = SunGold,
                    testTag = "btn_sanctuary",
                    onClick = { viewModel.openModal("sanctuary") }
                )

                // 4. Save Button (Disk Icon)
                QuickActionCircleButton(
                    icon = Icons.Default.Save,
                    label = "Save",
                    color = SolarEmerald,
                    testTag = "btn_manual_save",
                    onClick = { viewModel.executeFullSaveFlow() }
                )

                // 5. Menu Button (Hamburger / Three Dots)
                QuickActionCircleButton(
                    icon = if (showActionsMenu) Icons.Default.Close else Icons.Default.Menu,
                    label = if (showActionsMenu) "Close" else "Menu",
                    color = if (showActionsMenu) SunGold else Color.White,
                    active = showActionsMenu,
                    testTag = "btn_expand_menu",
                    onClick = {
                        showActionsMenu = !showActionsMenu
                        viewModel.audioSystem.playButtonTap()
                    }
                )
            }
        }

        // 5. Slide-In Menu Panel (Opens from the right when Menu is tapped)
        SlideInMenuPanel(
            visible = showActionsMenu,
            isSprinting = inputState.isSprinting,
            isNight = lightingState?.isNight ?: false,
            onEat = {
                viewModel.eat()
                showActionsMenu = false
            },
            onDrink = {
                viewModel.drink()
                showActionsMenu = false
            },
            onToggleWalkSprint = {
                viewModel.setSprinting(!inputState.isSprinting)
            },
            onSleep = {
                viewModel.restInFarmhouse()
                showActionsMenu = false
            },
            onTimeSkip = {
                viewModel.advanceTimeOfDay(2.0f)
                showActionsMenu = false
            },
            onOpenSanctuary = {
                viewModel.openModal("sanctuary")
                showActionsMenu = false
            },
            onOpenResearch = {
                viewModel.openModal("research")
                showActionsMenu = false
            },
            onOpenJournal = {
                viewModel.openModal("journal")
                showActionsMenu = false
            },
            onOpenEnergyGrid = {
                viewModel.openModal("energy_grid")
                showActionsMenu = false
            },
            onOpenMap = {
                viewModel.openModal("world_map")
                showActionsMenu = false
            },
            onOpenSettings = {
                viewModel.openModal("settings_menu")
                showActionsMenu = false
            },
            onDismiss = {
                showActionsMenu = false
            },
            modifier = Modifier.align(Alignment.CenterEnd)
        )

        // 6. Solarpunk Bottom Navigation Bar (Research, Journal, Sanctuary, Grid, Bag)
        if (!isBuildMode) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SolarpunkBottomNavBar(
                    activeModal = activeModal,
                    onOpenResearch = { viewModel.openModal("research") },
                    onOpenJournal = { viewModel.openModal("journal") },
                    onOpenSanctuary = { viewModel.openModal("sanctuary") },
                    onOpenEnergyGrid = { viewModel.openModal("energy_grid") },
                    onOpenInventory = { viewModel.openModal("inventory") }
                )

                Spacer(modifier = Modifier.height(6.dp))

                ContextActionPrompt(
                    prompt = currentPrompt,
                    onActionClick = { viewModel.onContextActionButton() },
                    onSecondaryActionClick = { viewModel.onContextSecondaryActionButton() }
                )
            }
        }

        // 7. Bottom-Left Joystick & Bottom Center Build Dock (if in Build Mode)
        if (isBuildMode) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                // Joystick floating on bottom left so player can position during build mode
                VirtualJoystick(
                    onMove = { x, z -> viewModel.setJoystickMove(x, z) },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 12.dp)
                )

                // Build Mode Control Dock
                BuildModeHUD(
                    selectedType = selectedBuildType,
                    rotationDeg = buildRotationDeg,
                    ghostState = ghostBuilding,
                    gameState = gameState,
                    inventory = inventory,
                    onSelectType = { viewModel.selectBuildType(it) },
                    onRotate = { viewModel.rotateBuilding() },
                    onConfirmPlace = { viewModel.confirmPlaceBuilding() },
                    onExitBuildMode = { viewModel.toggleBuildMode() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp)
                )
            }
        } else {
            // Analog Joystick in bottom-left
            VirtualJoystick(
                onMove = { x, z -> viewModel.setJoystickMove(x, z) },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 12.dp)
            )
        }

        // 7. Modals
        if (activeModal == "energy_grid") {
            EnergyTabModal(
                viewModel = viewModel,
                onDismiss = { viewModel.openModal(null) }
            )
        }

        if (activeModal == "plant_crop") {
            CropPlantModal(
                plot = selectedPlotForModal,
                onPlantCrop = { crop ->
                    selectedPlotForModal?.let { viewModel.plantCrop(it.id, crop) }
                },
                onDismiss = { viewModel.openModal(null) }
            )
        }

        if (activeModal == "inventory") {
            QuickInventoryModal(
                inventory = inventory,
                onSellItem = { id, qty -> viewModel.sellItem(id, qty) },
                onConsumeItem = { id -> viewModel.consumeInventoryItem(id) },
                onOpenMap = { viewModel.openModal("world_map") },
                onDismiss = { viewModel.openModal(null) }
            )
        }

        if (activeModal == "world_map") {
            WorldMapModal(
                playerX = viewModel.player.posX,
                playerZ = viewModel.player.posZ,
                playerAngleDeg = viewModel.player.orientationAngleDeg,
                discoveredPois = discoveredPois,
                discoveredChunks = discoveredChunks,
                currentBiome = currentBiome,
                plots = plots,
                placedBuildings = placedBuildings,
                energyNodes = energyNodes,
                onDismiss = { viewModel.openModal(null) }
            )
        }

        if (activeModal == "settings_menu") {
            SettingsMenuModal(
                masterVolume = masterVol,
                musicVolume = musicVol,
                sfxVolume = sfxVol,
                vibrationEnabled = vibrationOn,
                onMasterVolumeChange = { viewModel.setMasterVolume(it) },
                onMusicVolumeChange = { viewModel.setMusicVolume(it) },
                onSfxVolumeChange = { viewModel.setSfxVolume(it) },
                onVibrationToggle = { viewModel.setVibrationEnabled(it) },
                onSaveClick = { viewModel.executeFullSaveFlow() },
                onLoadClick = { viewModel.loadSaveGame() },
                onNewGameClick = { viewModel.startNewGameFresh() },
                onDismiss = { viewModel.openModal(null) }
            )
        }

        if (activeModal == "campfire_modal") {
            CampfireModal(
                gameState = gameState,
                inventory = inventory,
                onBoilWater = { viewModel.boilWater() },
                onCookFish = { fishId -> viewModel.cookFish(fishId) },
                onBrewTea = {
                    viewModel.viewModelScope.launch {
                        viewModel.repository.drinkHerbalTea()
                        viewModel.showNotification("Herbal Tea", "Cured sickness! Restored HP & Thirst.", "eco")
                        viewModel.addFloatingText("Sickness Cured!", Color(0xFF81C784))
                    }
                },
                onDismiss = { viewModel.openModal(null) }
            )
        }

        if (activeModal == "fishing_minigame" && activeFishingSession != null) {
            val rodTier = FishingRodTier.fromTier(gameState?.fishingRodTier ?: 1)
            FishingMiniGameModal(
                session = activeFishingSession,
                rodTier = rodTier,
                onHookFish = { viewModel.hookFish() },
                onReelTick = { isPressing -> viewModel.reelFishTick(isPressing) },
                onRetry = {
                    activeFishingSession?.spot?.let { viewModel.startFishing(it) }
                },
                onClose = { viewModel.closeFishingModal() }
            )
        }

        if (activeModal == "sanctuary") {
            SanctuaryTabModal(
                viewModel = viewModel,
                onDismiss = { viewModel.openModal(null) }
            )
        }

        if (activeModal == "research") {
            ResearchTabModal(
                researchPoints = researchPoints,
                unlockedTechs = unlockedTechs,
                onUnlockTech = { techId, rpCost -> viewModel.unlockEnergyTech(techId, rpCost) },
                onDismiss = { viewModel.openModal(null) }
            )
        }

        if (activeModal == "journal") {
            JournalTabModal(
                missions = storyMissions,
                pois = pointsOfInterest,
                loreEntries = loreEntries,
                unlockedBlueprints = unlockedTechs.toList(),
                onDismiss = { viewModel.openModal(null) }
            )
        }

        if (activeModal == "terminal_hack" && activeTerminalForModal != null) {
            TerminalHackModal(
                terminal = activeTerminalForModal!!,
                onHackSuccess = { pwd -> viewModel.hackTerminal(activeTerminalForModal!!.id, pwd) },
                onDismiss = { viewModel.openModal(null) }
            )
        }

        if (activeModal == "ending_sequence" || storyEndingUnlocked) {
            EndingSequenceModal(
                onContinueFreePlay = { viewModel.openModal(null) }
            )
        }

        if (activeModal == "npc_dialogue") {
            NpcDialogueModal(
                npc = selectedNpcForDialogue,
                viewModel = viewModel,
                onDismiss = { viewModel.openModal(null) }
            )
        }

        if (activeModal == "achievements") {
            AchievementsModal(
                unlockedAchievementIds = unlockedAchievements,
                onDismiss = { viewModel.openModal(null) }
            )
        }

        if (isQuickWheelVisible) {
            QuickActionWheelModal(
                onEat = { viewModel.eat() },
                onDrink = { viewModel.drink() },
                onRest = { viewModel.restInFarmhouse() },
                onOpenBuild = { viewModel.toggleBuildMode() },
                onOpenInventory = { viewModel.openModal("inventory") },
                onOpenResearch = { viewModel.openModal("research") },
                onOpenJournal = { viewModel.openModal("journal") },
                onOpenEnergy = { viewModel.openModal("energy_grid") },
                onDismiss = { viewModel.toggleQuickWheel() }
            )
        }

        if (showTutorialOverlay && !isMainMenuVisible) {
            TutorialOverlay(
                currentStep = tutorialStep,
                onNextStep = { viewModel.nextTutorialStep() },
                onSkipTutorial = { viewModel.skipTutorial() }
            )
        }

        if (isMainMenuVisible) {
            MainMenuOverlay(
                hasSaveGame = SaveLoadSystem.hasSave(context),
                onContinueGame = { viewModel.closeMainMenu() },
                onNewGame = {
                    viewModel.startNewGameFresh()
                    viewModel.closeMainMenu()
                },
                onOpenSaveSlots = { viewModel.openModal("settings_menu") },
                onOpenAchievements = { viewModel.openModal("achievements") },
                onOpenSettings = { viewModel.openModal("settings_menu") },
                onOpenCredits = {}
            )
        }

        if (pendingArrival != null) {
            val candidate = pendingArrival!!
            Dialog(
                onDismissRequest = { /* forces accept or reject choice */ },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(2.dp, SunGold, RoundedCornerShape(24.dp))
                        .testTag("dialog_npc_arrival"),
                    color = Color(0xFD09181A)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "NPC Arrival",
                                tint = SunGold,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Someone is approaching your farm!",
                                color = SunGold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "${candidate.name} (${candidate.role.title})",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "\"${candidate.greetingQuote}\"",
                            color = CleanCyan,
                            fontSize = 13.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = candidate.backgroundStory,
                            color = Color(0xFFB0BEC5),
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Accept ${candidate.name} into your settlement?",
                            color = SolarEmerald,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { viewModel.rejectNpcArrival() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("btn_reject_npc")
                            ) {
                                Text("Not Now", color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.acceptNpcArrival() },
                                colors = ButtonDefaults.buttonColors(containerColor = SolarEmerald),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("btn_welcome_npc")
                            ) {
                                Text("Welcome!", color = Color(0xFF091215), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Sickness green vignette overlay
        if (gameState?.isSick == true) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x282E7D32))
            )
        }

        // 8. Floating Text FX Overlay
        FloatingTextOverlay(
            floatingTexts = floatingTexts,
            onComplete = { viewModel.removeFloatingText(it) }
        )

        // 9. Day / Sleep Fade To Black Transition
        if (fadeBlackAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(fadeBlackAlpha)
                    .background(Color.Black)
            )
        }

        // 10. Fainted Screen Overlay
        if (isFainted) {
            FaintedOverlay(countdownSec = faintCountdown)
        }

        // 11. Initial & Reload Loading Screen Overlay
        if (isInitialLoading || isLoading) {
            LoadingScreenOverlay(title = if (isInitialLoading) "ECO FARM SIMULATOR" else "SOLARPUNK FARM")
        }
    }
}

@Composable
private fun LoadingScreenOverlay(title: String = "ECO FARM SIMULATOR") {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF091215))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = SolarEmerald,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Regenerative 3D Agriculture & Clean Power",
                color = SunGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(28.dp))
            CircularProgressIndicator(
                color = CleanCyan,
                strokeWidth = 3.dp,
                modifier = Modifier.size(42.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Loading world...",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun QuickActionCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    active: Boolean = false,
    testTag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "btn_scale"
    )

    Surface(
        modifier = Modifier
            .size(48.dp)
            .scale(scale)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(bounded = true, radius = 24.dp)
            ) { onClick() }
            .testTag(testTag),
        color = if (active) Color(0xEE1A3D34) else Color(0xCC112224),
        shape = CircleShape,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(19.dp)
            )
            Text(
                text = label,
                color = Color.White,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SolarpunkBottomNavBar(
    activeModal: String?,
    onOpenResearch: () -> Unit,
    onOpenJournal: () -> Unit,
    onOpenSanctuary: () -> Unit,
    onOpenEnergyGrid: () -> Unit,
    onOpenInventory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, SolarEmerald.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            .testTag("solarpunk_bottom_nav_bar"),
        color = Color(0xF009181A),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Research Tab
            BottomNavItemPill(
                icon = Icons.Default.Science,
                label = "Research",
                color = CleanCyan,
                isActive = activeModal == "research",
                testTag = "nav_tab_research",
                onClick = onOpenResearch
            )

            // 2. Journal Tab
            BottomNavItemPill(
                icon = Icons.Default.Book,
                label = "Journal",
                color = SunGold,
                isActive = activeModal == "journal",
                testTag = "nav_tab_journal",
                onClick = onOpenJournal
            )

            // 3. Sanctuary Tab
            BottomNavItemPill(
                icon = Icons.Default.People,
                label = "Sanctuary",
                color = SunGold,
                isActive = activeModal == "sanctuary",
                testTag = "nav_tab_sanctuary",
                onClick = onOpenSanctuary
            )

            // 4. Energy Grid Tab
            BottomNavItemPill(
                icon = Icons.Default.Bolt,
                label = "Grid",
                color = CleanCyan,
                isActive = activeModal == "energy_grid",
                testTag = "nav_tab_grid",
                onClick = onOpenEnergyGrid
            )

            // 5. Inventory Bag Tab
            BottomNavItemPill(
                icon = Icons.Default.Inventory2,
                label = "Bag",
                color = SolarEmerald,
                isActive = activeModal == "inventory",
                testTag = "nav_tab_bag",
                onClick = onOpenInventory
            )
        }
    }
}

@Composable
private fun BottomNavItemPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    isActive: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(testTag),
        color = if (isActive) color.copy(alpha = 0.25f) else Color.Transparent,
        shape = RoundedCornerShape(14.dp),
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, color) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) color else Color(0xFFB0BEC5),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                color = if (isActive) Color.White else Color(0xFFB0BEC5),
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium
            )
        }
    }
}
