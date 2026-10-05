package com.example.game3d.player

import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class ThirdPersonPlayer(
    var posX: Float = 0.0f,
    var posY: Float = 0.0f,
    var posZ: Float = 0.0f
) {
    var orientationAngleDeg: Float = 0.0f
    var walkAnimPhase: Float = 0.0f
    var isMoving: Boolean = false
    var currentSpeed: Float = 0.0f

    // Farm boundaries
    private val minBoundX = -24.0f
    private val maxBoundX = 24.0f
    private val minBoundZ = -24.0f
    private val maxBoundZ = 24.0f

    fun update(input: PlayerInputState, cameraYawDeg: Float, deltaSec: Float, speedMultiplier: Float = 1.0f) {
        val inputMagnitude = sqrt(input.moveX * input.moveX + input.moveZ * input.moveZ)
        if (inputMagnitude > 0.05f) {
            isMoving = true
            val speedFactor = (if (input.isSprinting) 11.0f else 6.5f) * speedMultiplier
            currentSpeed = speedFactor * min(1.0f, inputMagnitude)

            val yawRad = Math.toRadians(cameraYawDeg.toDouble()).toFloat()

            // Project camera-relative directions onto the ground plane (X-Z)
            // Camera forward unit vector projected on ground: (-sin, -cos)
            // Camera right unit vector projected on ground: (cos, -sin)
            val moveDirX = input.moveX * kotlin.math.cos(yawRad) - input.moveZ * kotlin.math.sin(yawRad)
            val moveDirZ = -input.moveX * kotlin.math.sin(yawRad) - input.moveZ * kotlin.math.cos(yawRad)

            val moveDirMag = sqrt(moveDirX * moveDirX + moveDirZ * moveDirZ)
            val unitDirX = if (moveDirMag > 0.001f) moveDirX / moveDirMag else 0f
            val unitDirZ = if (moveDirMag > 0.001f) moveDirZ / moveDirMag else 0f

            val dx = unitDirX * currentSpeed * deltaSec
            val dz = unitDirZ * currentSpeed * deltaSec

            posX = max(minBoundX, min(maxBoundX, posX + dx))
            posZ = max(minBoundZ, min(maxBoundZ, posZ + dz))

            // Smoothly rotate character to face the direction of movement (Shortest path lerp)
            val targetAngleDeg = Math.toDegrees(atan2(unitDirX.toDouble(), unitDirZ.toDouble())).toFloat()
            var diff = targetAngleDeg - orientationAngleDeg
            while (diff < -180.0f) diff += 360.0f
            while (diff > 180.0f) diff -= 360.0f

            val lerpFactor = (10.0f * deltaSec).coerceIn(0.0f, 1.0f)
            orientationAngleDeg = (orientationAngleDeg + diff * lerpFactor) % 360.0f
            if (orientationAngleDeg < 0.0f) {
                orientationAngleDeg += 360.0f
            }

            walkAnimPhase = (walkAnimPhase + deltaSec * (if (input.isSprinting) 12.0f else 8.0f)) % (2.0f * Math.PI.toFloat())
        } else {
            isMoving = false
            currentSpeed = 0.0f
            // Settle walk phase smoothly towards neutral
            walkAnimPhase = 0.0f
        }
    }
}
