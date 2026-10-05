package com.example.game3d.player

import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class ThirdPersonCamera(
    var targetX: Float = 0.0f,
    var targetY: Float = 0.0f,
    var targetZ: Float = 0.0f
) {
    var yawDeg: Float = 45.0f // Orbit around Y axis
    var pitchDeg: Float = 24.0f // Elevation angle (15-30 degrees)
    var distance: Float = 7.5f // Default distance (6-9 meters)

    val minDistance = 3.0f
    val maxDistance = 12.0f
    val minPitch = 12.0f
    val maxPitch = 60.0f

    fun updateTarget(targetPlayerX: Float, targetPlayerY: Float, targetPlayerZ: Float, deltaSec: Float) {
        // Smooth target follow interpolation
        val lerpFactor = min(1.0f, 12.0f * deltaSec)
        targetX += (targetPlayerX - targetX) * lerpFactor
        targetY += (targetPlayerY + 1.25f - targetY) * lerpFactor
        targetZ += (targetPlayerZ - targetZ) * lerpFactor
    }

    fun rotate(deltaYaw: Float, deltaPitch: Float) {
        yawDeg = (yawDeg + deltaYaw) % 360.0f
        if (yawDeg < 0.0f) yawDeg += 360.0f
        pitchDeg = max(minPitch, min(maxPitch, pitchDeg + deltaPitch))
    }

    fun zoom(zoomDelta: Float) {
        distance = max(minDistance, min(maxDistance, distance + zoomDelta))
    }

    fun getCameraPosition(): Triple<Float, Float, Float> {
        val yawRad = Math.toRadians(yawDeg.toDouble())
        val pitchRad = Math.toRadians(pitchDeg.toDouble())

        val camX = targetX + (distance * cos(pitchRad) * sin(yawRad)).toFloat()
        val camY = targetY + (distance * sin(pitchRad)).toFloat()
        val camZ = targetZ + (distance * cos(pitchRad) * cos(yawRad)).toFloat()

        return Triple(camX, camY, camZ)
    }
}
