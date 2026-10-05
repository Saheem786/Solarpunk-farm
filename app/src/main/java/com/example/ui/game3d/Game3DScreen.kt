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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.WeatherType
import com.example.game3d.opengl.GLWorldRenderer
import com.example.ui.FarmViewModel
import com.example.ui.components.FloatingTextOverlay
import com.example.ui.components.SolarpunkNotificationBanner
import com.example.ui.components.SurvivalStatsHUD
import com.example.ui.components.ToolSelectorDock
import com.example.ui.components.TopGameStatsBar
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SunGold

@Composable
fun Game3DScreen(
    viewModel: FarmViewModel,
    modifier: Modifier = Modifier
) {
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

    var showActionsMenu by remember { mutableStateOf(false) }

    val glRenderer = remember { GLWorldRenderer() }
    var glView by remember { mutableStateOf<GLSurfaceView?>(null) }

    DisposableEffect(glView) {
        glView?.onResume()
        onDispose {
            glView?.onPause()
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
                    setEGLContextClientVersion(2)
                    setRenderer(glRenderer)
                    renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                    glView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Camera Touch Drag & Pinch Zoom Area
        CameraTouchArea(
            onRotate = { dy, dp -> viewModel.rotateCamera(dy, dp) },
            onZoom = { dz -> viewModel.zoomCamera(dz) },
            modifier = Modifier.fillMaxSize()
        )

        // 3. HUD Top Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            TopGameStatsBar(
                state = gameState,
                onAdvanceTimeClick = { viewModel.advanceTimeOfDay(2.0f) },
                plots = plots,
                ecosystemScore = ecosystemHealth
            )
            SurvivalStatsHUD(
                state = gameState,
                onEatClick = { viewModel.eat() },
                onDrinkClick = { viewModel.drink() }
            )
            SolarpunkNotificationBanner(
                notification = bannerNotification
            )
        }

        // 4. Streamlined Primary Right-Side Controls & Collapsable Secondary Menu
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 100.dp, end = 12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Secondary Actions Collapsable Panel (Flys in to the left of the main column)
                AnimatedVisibility(
                    visible = showActionsMenu,
                    enter = fadeIn() + scaleIn(initialScale = 0.85f),
                    exit = fadeOut() + scaleOut(targetScale = 0.85f)
                ) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .testTag("secondary_actions_panel"),
                        color = Color(0xF20F2420),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SolarEmerald.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "QUICK ACTIONS",
                                color = SunGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )

                            // Grid / Row layout for secondary action buttons
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Eat
                                QuickActionCircleButton(
                                    icon = Icons.Default.Restaurant,
                                    label = "Eat",
                                    color = Color(0xFFFF9800),
                                    testTag = "btn_menu_eat",
                                    onClick = {
                                        viewModel.eat()
                                        showActionsMenu = false
                                    }
                                )

                                // Drink
                                QuickActionCircleButton(
                                    icon = Icons.Default.WaterDrop,
                                    label = "Drink",
                                    color = Color(0xFF00E5FF),
                                    testTag = "btn_menu_drink",
                                    onClick = {
                                        viewModel.drink()
                                        showActionsMenu = false
                                    }
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Walk / Sprint Toggle
                                QuickActionCircleButton(
                                    icon = Icons.Default.DirectionsRun,
                                    label = if (inputState.isSprinting) "Fast" else "Walk",
                                    color = if (inputState.isSprinting) SunGold else Color.White,
                                    active = inputState.isSprinting,
                                    testTag = "btn_menu_sprint",
                                    onClick = { viewModel.setSprinting(!inputState.isSprinting) }
                                )

                                // Fast-Forward (+2h)
                                QuickActionCircleButton(
                                    icon = Icons.Default.FastForward,
                                    label = "+2h",
                                    color = SolarEmerald,
                                    testTag = "btn_menu_fast_forward",
                                    onClick = {
                                        viewModel.advanceTimeOfDay(2.0f)
                                        showActionsMenu = false
                                    }
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Rest at Farmhouse
                                QuickActionCircleButton(
                                    icon = Icons.Default.Bed,
                                    label = "Rest",
                                    color = CleanCyan,
                                    testTag = "btn_menu_rest",
                                    onClick = {
                                        viewModel.restInFarmhouse()
                                        showActionsMenu = false
                                    }
                                )

                                // Settings Audio & Preferences
                                QuickActionCircleButton(
                                    icon = Icons.Default.Settings,
                                    label = "Settings",
                                    color = SunGold,
                                    testTag = "btn_settings_gear",
                                    onClick = {
                                        viewModel.openModal("settings_menu")
                                        showActionsMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Primary Action Buttons Column (Always Visible)
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Build Mode Toggle
                    QuickActionCircleButton(
                        icon = Icons.Default.Construction,
                        label = if (isBuildMode) "Exit" else "Build",
                        color = if (isBuildMode) SunGold else SolarEmerald,
                        active = isBuildMode,
                        testTag = "btn_build_mode",
                        onClick = { viewModel.toggleBuildMode() }
                    )

                    // 2. Backpack Bag Button
                    QuickActionCircleButton(
                        icon = Icons.Default.Inventory2,
                        label = "Bag",
                        color = CleanCyan,
                        testTag = "btn_inventory",
                        onClick = { viewModel.openModal("inventory") }
                    )

                    // 3. Quick Save Button
                    QuickActionCircleButton(
                        icon = Icons.Default.Save,
                        label = "Save",
                        color = SolarEmerald,
                        testTag = "btn_manual_save",
                        onClick = { viewModel.executeFullSaveFlow() }
                    )

                    // 4. Expandable Menu Toggle Button
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
        }

        // 5. Context Action Prompt (Bottom Right, above dock when not in Build Mode)
        if (!isBuildMode) {
            ContextActionPrompt(
                prompt = currentPrompt,
                onActionClick = { viewModel.onContextActionButton() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 90.dp, end = 16.dp)
            )
        }

        // 6. Bottom Controls: Build Mode HUD OR Virtual Joystick & Tool Selector Dock
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
                        .padding(start = 16.dp, bottom = 180.dp)
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
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Left: Analog Joystick
                VirtualJoystick(
                    onMove = { x, z -> viewModel.setJoystickMove(x, z) },
                    modifier = Modifier.align(Alignment.BottomStart)
                )

                // Center/Right: Tool Selector Dock
                ToolSelectorDock(
                    selectedTool = selectedTool,
                    onSelectTool = { viewModel.selectTool(it) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                )
            }
        }

        // 7. Modals
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
