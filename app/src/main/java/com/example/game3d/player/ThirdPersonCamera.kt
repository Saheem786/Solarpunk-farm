package com.example.game3d.player

import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class ThirdPersonCamera(
    var targetX: Float = 0.0f,
    var targetY: Float = 1.4f,
    var targetZ: Float = 0.0f
) {
    // Current camera parameters
    var yawDeg: Float = 180.0f // Orbit around Y axis (180 deg looks towards +Z)
    var pitchDeg: Float = 18.0f // Elevation angle (12-20 deg)
    var distance: Float = 5.0f // Default distance (4.5 - 6.0 units)

    // Target values for smooth damping
    var desiredYawDeg: Float = 180.0f
    var desiredPitchDeg: Float = 18.0f
    var desiredDistance: Float = 5.0f

    val minDistance = 4.0f
    val maxDistance = 7.0f
    val minPitch = 12.0f
    val maxPitch = 30.0f

    fun instantReset(px: Float, py: Float, pz: Float, yaw: Float = 180.0f, pitch: Float = 18.0f, dist: Float = 5.0f) {
        val validPx = if (px.isNaN() || px.isInfinite()) 0.0f else px
        val validPy = if (py.isNaN() || py.isInfinite()) 0.0f else py
        val validPz = if (pz.isNaN() || pz.isInfinite()) 0.0f else pz

        targetX = validPx
        targetY = validPy + 1.4f
        targetZ = validPz

        yawDeg = yaw
        desiredYawDeg = yaw
        pitchDeg = pitch.coerceIn(minPitch, maxPitch)
        desiredPitchDeg = pitch.coerceIn(minPitch, maxPitch)

        val clampedDist = dist.coerceIn(minDistance, maxDistance)
        distance = clampedDist
        desiredDistance = clampedDist
    }

    fun updateTarget(targetPlayerX: Float, targetPlayerY: Float, targetPlayerZ: Float, deltaSec: Float, playerMoving: Boolean = false, playerOrientationDeg: Float = 0.0f) {
        val validPx = if (targetPlayerX.isNaN() || targetPlayerX.isInfinite()) 0.0f else targetPlayerX
        val validPy = if (targetPlayerY.isNaN() || targetPlayerY.isInfinite()) 0.0f else targetPlayerY
        val validPz = if (targetPlayerZ.isNaN() || targetPlayerZ.isInfinite()) 0.0f else targetPlayerZ

        // Smooth target follow interpolation
        val posLerpFactor = min(1.0f, 12.0f * deltaSec)
        targetX += (validPx - targetX) * posLerpFactor
        targetY += ((validPy + 1.4f) - targetY) * posLerpFactor
        targetZ += (validPz - targetZ) * posLerpFactor

        // Smooth rotation & zoom damping
        val rotLerpFactor = min(1.0f, 14.0f * deltaSec)

        // Handle yaw wrap-around interpolation
        var yawDiff = (desiredYawDeg - yawDeg) % 360.0f
        if (yawDiff > 180.0f) yawDiff -= 360.0f
        if (yawDiff < -180.0f) yawDiff += 360.0f
        yawDeg = (yawDeg + yawDiff * rotLerpFactor) % 360.0f
        if (yawDeg < 0.0f) yawDeg += 360.0f

        pitchDeg += (desiredPitchDeg - pitchDeg) * rotLerpFactor
        distance += (desiredDistance - distance) * rotLerpFactor
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
