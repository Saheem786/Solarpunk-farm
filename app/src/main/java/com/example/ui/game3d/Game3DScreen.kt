package com.example.ui.game3d

import android.opengl.GLSurfaceView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.game3d.opengl.GLWorldRenderer
import com.example.ui.FarmViewModel
import com.example.ui.components.SolarpunkNotificationBanner
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
    val inputState by viewModel.inputState.collectAsStateWithLifecycle()
    val selectedTool by viewModel.selectedTool.collectAsStateWithLifecycle()
    val currentPrompt by viewModel.currentPrompt.collectAsStateWithLifecycle()
    val lightingState by viewModel.lightingState.collectAsStateWithLifecycle()
    val activeModal by viewModel.activeModal.collectAsStateWithLifecycle()
    val selectedPlotForModal by viewModel.selectedPlotForModal.collectAsStateWithLifecycle()
    val bannerNotification by viewModel.bannerNotification.collectAsStateWithLifecycle()
    val animTime by viewModel.animTime.collectAsStateWithLifecycle()

    val glRenderer = remember { GLWorldRenderer() }

    // Synchronize latest state with GLWorldRenderer
    glRenderer.playerRef = viewModel.player
    glRenderer.cameraRef = viewModel.camera
    glRenderer.lightingRef = lightingState
    glRenderer.plotsRef = plots
    glRenderer.energyNodesRef = energyNodes
    glRenderer.livestockRef = livestock
    glRenderer.animTimeSec = animTime

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
                onAdvanceTimeClick = { viewModel.advanceTimeOfDay(2.0f) }
            )
            SolarpunkNotificationBanner(
                notification = bannerNotification
            )
        }

        // 4. Quick Actions Floating Column (Right Top)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 70.dp, end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Inventory Bag Button
            QuickActionCircleButton(
                icon = Icons.Default.Inventory2,
                label = "Bag",
                color = CleanCyan,
                testTag = "btn_inventory",
                onClick = { viewModel.openModal("inventory") }
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

        // 5. Context Action Prompt (Bottom Right, above dock)
        ContextActionPrompt(
            prompt = currentPrompt,
            onActionClick = { viewModel.onContextActionButton() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 16.dp)
        )

        // 6. Bottom Controls: Virtual Joystick & Tool Selector Dock
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
