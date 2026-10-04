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

    fun update(input: PlayerInputState, cameraYawDeg: Float, deltaSec: Float) {
        val inputMagnitude = sqrt(input.moveX * input.moveX + input.moveZ * input.moveZ)
        if (inputMagnitude > 0.05f) {
            isMoving = true
            val speedFactor = if (input.isSprinting) 11.0f else 6.5f
            currentSpeed = speedFactor * min(1.0f, inputMagnitude)

            // Calculate movement relative to camera angle
            val inputAngleRad = atan2(input.moveX, input.moveZ)
            val cameraAngleRad = Math.toRadians(cameraYawDeg.toDouble()).toFloat()
            val totalAngleRad = inputAngleRad + cameraAngleRad

            val dx = kotlin.math.sin(totalAngleRad) * currentSpeed * deltaSec
            val dz = kotlin.math.cos(totalAngleRad) * currentSpeed * deltaSec

            posX = max(minBoundX, min(maxBoundX, posX + dx))
            posZ = max(minBoundZ, min(maxBoundZ, posZ + dz))

            orientationAngleDeg = Math.toDegrees(totalAngleRad.toDouble()).toFloat()
            walkAnimPhase = (walkAnimPhase + deltaSec * (if (input.isSprinting) 12.0f else 8.0f)) % (2.0f * Math.PI.toFloat())
        } else {
            isMoving = false
            currentSpeed = 0.0f
            // Settle walk phase smoothly towards neutral
            walkAnimPhase = 0.0f
        }
    }
}
