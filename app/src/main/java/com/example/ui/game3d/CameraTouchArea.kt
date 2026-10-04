package com.example.ui.game3d

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun CameraTouchArea(
    onRotate: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // Drag X rotates camera Yaw, Drag Y tilts camera Pitch
                    val deltaYaw = dragAmount.x * 0.35f
                    val deltaPitch = -dragAmount.y * 0.25f
                    onRotate(deltaYaw, deltaPitch)
                }
            }
    )
}
