package com.example.ui.game3d

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SunGold
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun VirtualJoystick(
    onMove: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var thumbOffset by remember { mutableStateOf(Offset.Zero) }
    val baseRadius = 60.dp
    val thumbRadius = 26.dp

    Box(
        modifier = modifier
            .size(130.dp)
            .testTag("virtual_joystick")
            .pointerInput(Unit) {
                val maxDistance = 55.dp.toPx()
                detectDragGestures(
                    onDragStart = { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dragVector = offset - center
                        val dist = dragVector.getDistance()
                        val clampedDist = dist.coerceAtMost(maxDistance)
                        val angle = atan2(dragVector.y, dragVector.x)
                        val clampedOffset = Offset(
                            cos(angle) * clampedDist,
                            sin(angle) * clampedDist
                        )
                        thumbOffset = clampedOffset
                        val normX = (clampedOffset.x / maxDistance).coerceIn(-1.0f, 1.0f)
                        val normZ = (-clampedOffset.y / maxDistance).coerceIn(-1.0f, 1.0f)
                        Log.d("VirtualJoystick", "onDragStart: x=$normX, z=$normZ")
                        onMove(normX, normZ)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = thumbOffset + dragAmount
                        val dist = newOffset.getDistance()
                        val clampedDist = dist.coerceAtMost(maxDistance)
                        val angle = atan2(newOffset.y, newOffset.x)
                        val clampedOffset = Offset(
                            cos(angle) * clampedDist,
                            sin(angle) * clampedDist
                        )
                        thumbOffset = clampedOffset
                        val normX = (clampedOffset.x / maxDistance).coerceIn(-1.0f, 1.0f)
                        val normZ = (-clampedOffset.y / maxDistance).coerceIn(-1.0f, 1.0f)
                        Log.d("VirtualJoystick", "onDrag: x=$normX, z=$normZ")
                        onMove(normX, normZ)
                    },
                    onDragEnd = {
                        thumbOffset = Offset.Zero
                        Log.d("VirtualJoystick", "onDragEnd: x=0.0, z=0.0")
                        onMove(0.0f, 0.0f)
                    },
                    onDragCancel = {
                        thumbOffset = Offset.Zero
                        Log.d("VirtualJoystick", "onDragCancel: x=0.0, z=0.0")
                        onMove(0.0f, 0.0f)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)

            // Outer Glow Base Ring
            drawCircle(
                color = Color(0x3300E676),
                radius = 55.dp.toPx(),
                center = center
            )
            drawCircle(
                color = SolarEmerald.copy(alpha = 0.6f),
                radius = 55.dp.toPx(),
                center = center,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Inner Crosshairs
            drawLine(
                color = Color(0x44FFFFFF),
                start = Offset(center.x - 20.dp.toPx(), center.y),
                end = Offset(center.x + 20.dp.toPx(), center.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color(0x44FFFFFF),
                start = Offset(center.x, center.y - 20.dp.toPx()),
                end = Offset(center.x, center.y + 20.dp.toPx()),
                strokeWidth = 1.dp.toPx()
            )

            // Movable Thumb Stick
            val thumbPos = center + thumbOffset
            drawCircle(
                color = Color(0xEE112D29),
                radius = 26.dp.toPx(),
                center = thumbPos
            )
            drawCircle(
                color = SunGold,
                radius = 26.dp.toPx(),
                center = thumbPos,
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = CleanCyan,
                radius = 10.dp.toPx(),
                center = thumbPos
            )
        }
    }
}
