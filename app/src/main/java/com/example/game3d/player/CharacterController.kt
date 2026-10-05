package com.example.game3d.player

import com.example.game3d.physics.Collider
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class CharacterMovementResult(
    val posX: Float,
    val posZ: Float,
    val hasCollided: Boolean,
    val velocityX: Float,
    val velocityZ: Float
)

/**
 * High-performance CharacterController component with optimized collision bounds
 * (Radius = 0.4 units, Height = 1.8 units).
 * Features surface tangent sliding, collision normal projection, and continuous clipping prevention.
 */
class CharacterController(
    val radius: Float = 0.40f,
    val height: Float = 1.80f,
    val skinWidth: Float = 0.02f,
    val stepOffset: Float = 0.30f,
    val slopeLimitDeg: Float = 45.0f
) {

    /**
     * Moves the character by displacement vector (motionX, motionZ),
     * sliding smoothly along obstacle surfaces and preventing any clipping against colliders.
     */
    fun move(
        currentX: Float,
        currentZ: Float,
        motionX: Float,
        motionZ: Float,
        colliders: List<Collider>,
        minBoundX: Float = -198.0f,
        maxBoundX: Float = 198.0f,
        minBoundZ: Float = -198.0f,
        maxBoundZ: Float = 198.0f
    ): CharacterMovementResult {
        val effRadius = radius + skinWidth
        var newX = currentX
        var newZ = currentZ
        var collided = false

        var remainX = motionX
        var remainZ = motionZ

        // 1. Multi-pass surface tangent sliding solver (handles up to 3 surface deflections per tick)
        for (iteration in 0 until 3) {
            val remainDistSq = remainX * remainX + remainZ * remainZ
            if (remainDistSq < 0.000001f) break

            val targetX = (newX + remainX).coerceIn(minBoundX + effRadius, maxBoundX - effRadius)
            val targetZ = (newZ + remainZ).coerceIn(minBoundZ + effRadius, maxBoundZ - effRadius)

            // Find first contacting collider in sweep
            var hitNormal: Pair<Float, Float>? = null

            for (collider in colliders) {
                val normal = collider.getCollisionNormal(targetX, targetZ, effRadius)
                if (normal != null) {
                    hitNormal = normal
                    collided = true
                    break
                }
            }

            if (hitNormal == null) {
                // Unobstructed step
                newX = targetX
                newZ = targetZ
                break
            } else {
                // Contact occurred: project motion along surface tangent
                val (nx, nz) = hitNormal
                val dot = remainX * nx + remainZ * nz

                if (dot < 0f) {
                    // Deflect motion parallel to the surface: V_tangent = V - (V · N) * N
                    val slideX = remainX - nx * dot
                    val slideZ = remainZ - nz * dot

                    // Move along free axis component first
                    val candX = (newX + slideX).coerceIn(minBoundX + effRadius, maxBoundX - effRadius)
                    val candZ = (newZ + slideZ).coerceIn(minBoundZ + effRadius, maxBoundZ - effRadius)

                    var blockedSlide = false
                    for (collider in colliders) {
                        if (collider.checkCollision(candX, candZ, effRadius)) {
                            blockedSlide = true
                            break
                        }
                    }

                    if (!blockedSlide) {
                        newX = candX
                        newZ = candZ
                        break
                    } else {
                        // Try single axis sliding along X or Z
                        val testX = (newX + slideX).coerceIn(minBoundX + effRadius, maxBoundX - effRadius)
                        var blockedX = false
                        for (collider in colliders) {
                            if (collider.checkCollision(testX, newZ, effRadius)) {
                                blockedX = true
                                break
                            }
                        }
                        if (!blockedX) newX = testX

                        val testZ = (newZ + slideZ).coerceIn(minBoundZ + effRadius, maxBoundZ - effRadius)
                        var blockedZ = false
                        for (collider in colliders) {
                            if (collider.checkCollision(newX, testZ, effRadius)) {
                                blockedZ = true
                                break
                            }
                        }
                        if (!blockedZ) newZ = testZ

                        remainX = slideX * 0.5f
                        remainZ = slideZ * 0.5f
                    }
                } else {
                    // Moving away from surface
                    newX = targetX
                    newZ = targetZ
                    break
                }
            }
        }

        // 2. Continuous penetration resolution / push-out passes to strictly prevent any clipping
        for (pass in 0 until 3) {
            var anyPush = false
            for (collider in colliders) {
                val push = collider.getPushOut(newX, newZ, effRadius)
                if (push != null) {
                    newX += push.first
                    newZ += push.second
                    anyPush = true
                    collided = true
                }
            }
            if (!anyPush) break
        }

        // 3. World map boundary clamping
        newX = newX.coerceIn(minBoundX + effRadius, maxBoundX - effRadius)
        newZ = newZ.coerceIn(minBoundZ + effRadius, maxBoundZ - effRadius)

        val actualVelX = newX - currentX
        val actualVelZ = newZ - currentZ

        return CharacterMovementResult(
            posX = newX,
            posZ = newZ,
            hasCollided = collided,
            velocityX = actualVelX,
            velocityZ = actualVelZ
        )
    }

    /**
     * Checks if a point with character controller radius overlaps any colliders in the world.
     */
    fun checkOverlap(
        testPosX: Float,
        testPosZ: Float,
        colliders: List<Collider>
    ): Boolean {
        val effRadius = radius + skinWidth
        for (collider in colliders) {
            if (collider.checkCollision(testPosX, testPosZ, effRadius)) {
                return true
            }
        }
        return false
    }
}
