package com.example.ui.game3d

import android.opengl.GLSurfaceView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
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
                plots = plots
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

        // 4. Quick Actions Floating Column (Right Top)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 110.dp, end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Build Mode Toggle Button
            QuickActionCircleButton(
                icon = Icons.Default.Construction,
                label = if (isBuildMode) "Cancel" else "Build",
                color = if (isBuildMode) SunGold else SolarEmerald,
                active = isBuildMode,
                testTag = "btn_build_mode",
                onClick = { viewModel.toggleBuildMode() }
            )

            // Eat Button
            QuickActionCircleButton(
                icon = Icons.Default.Restaurant,
                label = "Eat",
                color = Color(0xFFFF9800),
                testTag = "btn_eat",
                onClick = { viewModel.eat() }
            )

            // Drink Button
            QuickActionCircleButton(
                icon = Icons.Default.WaterDrop,
                label = "Drink",
                color = Color(0xFF00E5FF),
                testTag = "btn_drink",
                onClick = { viewModel.drink() }
            )

            // Inventory Bag Button
            QuickActionCircleButton(
                icon = Icons.Default.Inventory2,
                label = "Bag",
                color = CleanCyan,
                testTag = "btn_inventory",
                onClick = { viewModel.openModal("inventory") }
            )

            // Manual Save Button
            QuickActionCircleButton(
                icon = Icons.Default.Save,
                label = "Save",
                color = SolarEmerald,
                testTag = "btn_manual_save",
                onClick = { viewModel.manualSave() }
            )

            // Sprint Toggle Button
            QuickActionCircleButton(
                icon = Icons.Default.DirectionsRun,
                label = if (inputState.isSprinting) "Fast" else "Walk",
                color = if (inputState.isSprinting) SunGold else Color.White,
                active = inputState.isSprinting,
                testTag = "btn_sprint",
                onClick = { viewModel.setSprinting(!inputState.isSprinting) }
            )

            // Fast-Forward Time
            QuickActionCircleButton(
                icon = Icons.Default.FastForward,
                label = "+2h",
                color = SolarEmerald,
                testTag = "btn_fast_forward",
                onClick = { viewModel.advanceTimeOfDay(2.0f) }
            )
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
                onDismiss = { viewModel.openModal(null) }
            )
        }

        if (activeModal == "settings_menu") {
            SettingsMenuModal(
                onSaveClick = { viewModel.executeFullSaveFlow() },
                onLoadClick = { viewModel.loadSaveGame() },
                onNewGameClick = { viewModel.startNewGameFresh() },
                onDismiss = { viewModel.openModal(null) }
            )
        }

        // 8. Fainted Screen Overlay
        if (isFainted) {
            FaintedOverlay(countdownSec = faintCountdown)
        }

        // 9. Saving/Loading Progress Screen Overlay
        if (isLoading) {
            LoadingScreenOverlay()
        }
    }
}

@Composable
private fun LoadingScreenOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF091215))
            .clickable(enabled = false) {}, // absorb touch events
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "SOLARPUNK FARM",
                color = SolarEmerald,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Restoring world state...",
                color = SunGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(24.dp))
            androidx.compose.material3.CircularProgressIndicator(
                color = CleanCyan,
                strokeWidth = 3.dp,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Loading...",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp
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
    Surface(
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onClick() }
            .testTag(testTag),
        color = if (active) Color(0xEE1A3D34) else Color(0xCC112224),
        shape = CircleShape,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .size(48.dp)
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
