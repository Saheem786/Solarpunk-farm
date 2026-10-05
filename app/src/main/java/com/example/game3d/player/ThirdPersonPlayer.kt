package com.example.game3d.player

import com.example.data.local.EnergyNodeEntity
import com.example.data.local.PlacedBuildingEntity
import com.example.data.local.PlotEntity
import com.example.game3d.physics.Collider
import com.example.game3d.physics.WorldColliderBuilder
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
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

    // CharacterController Component (Height = 1.80m, Radius = 0.40m)
    val controller = CharacterController(
        radius = 0.40f,
        height = 1.80f,
        skinWidth = 0.02f,
        stepOffset = 0.30f
    )

    // Farm boundaries
    private val minBoundX = -23.5f
    private val maxBoundX = 23.5f
    private val minBoundZ = -23.5f
    private val maxBoundZ = 23.5f

    fun update(
        input: PlayerInputState,
        cameraYawDeg: Float,
        deltaSec: Float,
        speedMultiplier: Float = 1.0f,
        placedBuildings: List<PlacedBuildingEntity> = emptyList(),
        energyNodes: List<EnergyNodeEntity> = emptyList(),
        plots: List<PlotEntity> = emptyList()
    ) {
        val inputMagnitude = sqrt(input.moveX * input.moveX + input.moveZ * input.moveZ)
        if (inputMagnitude > 0.05f) {
            isMoving = true
            val speedFactor = (if (input.isSprinting) 11.0f else 6.5f) * speedMultiplier
            currentSpeed = speedFactor * min(1.0f, inputMagnitude)

            val yawRad = Math.toRadians(cameraYawDeg.toDouble()).toFloat()

            // Project camera-relative directions onto the ground plane (X-Z)
            val moveDirX = input.moveX * cos(yawRad) - input.moveZ * sin(yawRad)
            val moveDirZ = -input.moveX * sin(yawRad) - input.moveZ * cos(yawRad)

            val moveDirMag = sqrt(moveDirX * moveDirX + moveDirZ * moveDirZ)
            val unitDirX = if (moveDirMag > 0.001f) moveDirX / moveDirMag else 0f
            val unitDirZ = if (moveDirMag > 0.001f) moveDirZ / moveDirMag else 0f

            val motionX = unitDirX * currentSpeed * deltaSec
            val motionZ = unitDirZ * currentSpeed * deltaSec

            // Fetch dynamic world colliders
            val colliders = WorldColliderBuilder.buildColliders(plots, placedBuildings, energyNodes)

            // Execute CharacterController physical movement & sliding resolution
            val moveResult = controller.move(
                currentX = posX,
                currentZ = posZ,
                motionX = motionX,
                motionZ = motionZ,
                colliders = colliders,
                minBoundX = minBoundX,
                maxBoundX = maxBoundX,
                minBoundZ = minBoundZ,
                maxBoundZ = maxBoundZ
            )

            posX = moveResult.posX
            posZ = moveResult.posZ

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
            walkAnimPhase = 0.0f
        }
    }

    /**
     * Public collision query method used by camera or external triggers.
     */
    fun checkCollision(
        x: Float,
        z: Float,
        radius: Float = 0.40f,
        placedBuildings: List<PlacedBuildingEntity> = emptyList(),
        energyNodes: List<EnergyNodeEntity> = emptyList(),
        plots: List<PlotEntity> = emptyList()
    ): Boolean {
        if (x < minBoundX + radius || x > maxBoundX - radius || z < minBoundZ + radius || z > maxBoundZ - radius) {
            return true
        }
        val colliders = WorldColliderBuilder.buildColliders(plots, placedBuildings, energyNodes)
        for (c in colliders) {
            if (c.checkCollision(x, z, radius)) {
                return true
            }
        }
        return false
    }
}
