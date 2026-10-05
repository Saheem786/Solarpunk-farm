package com.example.ui.game3d

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun CameraTouchArea(
    onRotate: (Float, Float) -> Unit,
    onZoom: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    // Pan X rotates camera Yaw, Pan Y tilts camera Pitch
                    if (pan.x != 0f || pan.y != 0f) {
                        val deltaYaw = pan.x * 0.35f
                        val deltaPitch = -pan.y * 0.25f
                        onRotate(deltaYaw, deltaPitch)
                    }
                    // Pinch zoom
                    if (zoom != 1.0f) {
                        val zoomDelta = (1.0f - zoom) * 6.0f
                        onZoom(zoomDelta)
                    }
                }
            }
    )
}
