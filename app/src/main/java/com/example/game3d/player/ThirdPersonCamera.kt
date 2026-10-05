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
    // Current interpolated camera parameters
    var yawDeg: Float = 45.0f // Orbit around Y axis
    var pitchDeg: Float = 24.0f // Elevation angle (15-30 degrees)
    var distance: Float = 5.0f // Default distance

    // Target values for smooth damping
    var desiredYawDeg: Float = 45.0f
    var desiredPitchDeg: Float = 24.0f
    var desiredDistance: Float = 5.0f

    val minDistance = 2.0f
    val maxDistance = 6.0f
    val minPitch = 12.0f
    val maxPitch = 60.0f

    fun updateTarget(targetPlayerX: Float, targetPlayerY: Float, targetPlayerZ: Float, deltaSec: Float) {
        // Smooth target follow interpolation (exponential easing)
        val posLerpFactor = min(1.0f, 10.0f * deltaSec)
        targetX += (targetPlayerX - targetX) * posLerpFactor
        targetY += (targetPlayerY + 1.25f - targetY) * posLerpFactor
        targetZ += (targetPlayerZ - targetZ) * posLerpFactor

        // Smooth rotation & zoom damping
        val rotLerpFactor = min(1.0f, 14.0f * deltaSec)

        // Handle yaw wrap-around interpolation
        var yawDiff = (desiredYawDeg - yawDeg) % 360.0f
        if (yawDiff > 180.0f) yawDiff -= 360.0f
        if (yawDiff < -180.0f) yawDiff += 360.0f
        yawDeg = (yawDeg + yawDiff * rotLerpFactor) % 360.0f
        if (yawDeg < 0.0f) yawDeg += 360.0f

        pitchDeg += (desiredPitchDeg - pitchDeg) * rotLerpFactor
    }

    fun rotate(deltaYaw: Float, deltaPitch: Float) {
        desiredYawDeg = (desiredYawDeg + deltaYaw) % 360.0f
        if (desiredYawDeg < 0.0f) desiredYawDeg += 360.0f
        desiredPitchDeg = max(minPitch, min(maxPitch, desiredPitchDeg + deltaPitch))
    }

    fun zoom(zoomDelta: Float) {
        desiredDistance = max(minDistance, min(maxDistance, desiredDistance + zoomDelta))
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
