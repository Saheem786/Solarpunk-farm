package com.example.game3d.particles

import android.opengl.GLES20
import android.opengl.Matrix
import com.example.game3d.opengl.GLMesh
import com.example.game3d.opengl.GLModelBuilder
import com.example.game3d.opengl.GLShader
import kotlin.math.cos
import kotlin.math.sin

/**
 * LibGDX-inspired 3D Particle Type categories.
 */
enum class ParticleType {
    DUST_PUFF,
    GRASS_FLECK,
    WATER_DROPLET,
    PLANT_SPROUT,
    HARVEST_LEAF,
    COIN_SPARKLE,
    BUILD_DUST_RING
}

/**
 * Individual pooled Particle with kinematic physics, scaling curve,
 * color interpolation, and lifetime progression.
 */
class LibGDXParticle3D {
    var posX: Float = 0f
    var posY: Float = 0f
    var posZ: Float = 0f

    var velX: Float = 0f
    var velY: Float = 0f
    var velZ: Float = 0f

    var accelX: Float = 0f
    var accelY: Float = -2.5f
    var accelZ: Float = 0f

    var damping: Float = 0.98f
    var rotationDeg: Float = 0f
    var rotSpeedDeg: Float = 0f

    var startR: Float = 1f
    var startG: Float = 1f
    var startB: Float = 1f
    var endR: Float = 1f
    var endG: Float = 1f
    var endB: Float = 1f

    var startScale: Float = 0.1f
    var endScale: Float = 0.0f

    var maxLifeSec: Float = 0.5f
    var ageSec: Float = 0f
    var type: ParticleType = ParticleType.DUST_PUFF
    var isAlive: Boolean = false

    fun reset() {
        posX = 0f; posY = 0f; posZ = 0f
        velX = 0f; velY = 0f; velZ = 0f
        accelX = 0f; accelY = -2.5f; accelZ = 0f
        damping = 0.98f
        rotationDeg = 0f
        rotSpeedDeg = 0f
        ageSec = 0f
        maxLifeSec = 0.5f
        isAlive = false
    }

    fun update(deltaSec: Float) {
        if (!isAlive) return
        ageSec += deltaSec
        if (ageSec >= maxLifeSec) {
            isAlive = false
            return
        }

        // Kinematics
        velX += accelX * deltaSec
        velY += accelY * deltaSec
        velZ += accelZ * deltaSec

        velX *= damping
        velZ *= damping

        posX += velX * deltaSec
        posY += velY * deltaSec
        posZ += velZ * deltaSec

        rotationDeg += rotSpeedDeg * deltaSec

        // Floor collision response
        if (posY < 0.02f) {
            posY = 0.02f
            if (type == ParticleType.WATER_DROPLET) {
                velY = -velY * 0.35f
                velX *= 0.6f
                velZ *= 0.6f
            } else {
                velY = 0f
                velX *= 0.7f
                velZ *= 0.7f
            }
        }
    }
}

/**
 * LibGDX-inspired Object Pool to eliminate GC allocations during active particle bursts.
 */
class ParticlePool(capacity: Int = 256) {
    private val pool = Array(capacity) { LibGDXParticle3D() }
    private var freeIndex = capacity - 1

    fun obtain(): LibGDXParticle3D? {
        if (freeIndex < 0) return null
        val p = pool[freeIndex]
        freeIndex--
        p.reset()
        p.isAlive = true
        return p
    }

    fun free(p: LibGDXParticle3D) {
        if (freeIndex < pool.size - 1) {
            p.isAlive = false
            freeIndex++
            pool[freeIndex] = p
        }
    }
}

/**
 * LibGDX-inspired 3D Particle System managing emitters for:
 * - Walking dust puffs & grass flecks
 * - Irrigation water splash droplets
 * - Botanical seed sprout bursts
 * - Harvest leaf flurries & coin sparkles
 */
class ParticleSystem3D(maxActiveParticles: Int = 180) {

    private val pool = ParticlePool(maxActiveParticles)
    private val activeParticles = ArrayList<LibGDXParticle3D>(maxActiveParticles)

    // Pre-built low poly meshes for different particle shapes
    private val boxMesh: GLMesh
    private val diamondMesh: GLMesh

    private val modelMatrix = FloatArray(16)
    private val mvMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val normalMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)

    init {
        val builder = GLModelBuilder()
        builder.addBox(0f, 0f, 0f, 0.10f, 0.10f, 0.10f, 1.0f, 1.0f, 1.0f)
        boxMesh = builder.build()

        builder.reset()
        builder.addCone(0f, -0.05f, 0f, 0.08f, 0.12f, 4, 1.0f, 1.0f, 1.0f)
        diamondMesh = builder.build()
    }

    /**
     * Spawns dust puffs for walking on dirt soil or grass flecks on meadow grass.
     */
    fun spawnFootstep(x: Float, z: Float, onDirt: Boolean) {
        val burstCount = if (onDirt) 4 else 3
        for (i in 0 until burstCount) {
            val p = pool.obtain() ?: break
            val angle = (Math.random() * Math.PI * 2.0).toFloat()
            val speed = (Math.random() * 0.45 + 0.15).toFloat()

            p.posX = x + (Math.random() * 0.22 - 0.11).toFloat()
            p.posY = 0.04f
            p.posZ = z + (Math.random() * 0.22 - 0.11).toFloat()

            p.velX = cos(angle) * speed
            p.velY = (Math.random() * 0.35 + 0.15).toFloat()
            p.velZ = sin(angle) * speed

            p.accelY = -1.2f
            p.damping = 0.88f
            p.rotSpeedDeg = (Math.random() * 120 - 60).toFloat()
            p.type = if (onDirt) ParticleType.DUST_PUFF else ParticleType.GRASS_FLECK

            if (onDirt) {
                // Billowy earth dust: warm ochre to soft sand
                p.startR = 0.78f; p.startG = 0.62f; p.startB = 0.42f
                p.endR = 0.55f; p.endG = 0.45f; p.endB = 0.32f
                p.startScale = 0.09f
                p.endScale = 0.02f
                p.maxLifeSec = 0.38f
            } else {
                // Meadow grass: vibrant emerald flecks
                p.startR = 0.28f; p.startG = 0.88f; p.startB = 0.35f
                p.endR = 0.18f; p.endG = 0.58f; p.endB = 0.22f
                p.startScale = 0.07f
                p.endScale = 0.01f
                p.maxLifeSec = 0.30f
            }

            activeParticles.add(p)
        }
    }

    /**
     * Spawns parabolic water splash droplets when irrigating soil plots.
     */
    fun spawnWaterSplash(x: Float, y: Float, z: Float) {
        val count = 10
        for (i in 0 until count) {
            val p = pool.obtain() ?: break
            val angle = (Math.random() * Math.PI * 2.0).toFloat()
            val speed = (Math.random() * 0.95 + 0.35).toFloat()

            p.posX = x + (Math.random() * 0.3 - 0.15).toFloat()
            p.posY = y + 0.08f
            p.posZ = z + (Math.random() * 0.3 - 0.15).toFloat()

            p.velX = cos(angle) * speed
            p.velY = (Math.random() * 1.1 + 0.55).toFloat()
            p.velZ = sin(angle) * speed

            p.accelY = -4.8f // Strong gravity pull for water arcs
            p.damping = 0.96f
            p.type = ParticleType.WATER_DROPLET

            // Azure / Cyan hydration water
            p.startR = 0.15f; p.startG = 0.85f; p.startB = 1.0f
            p.endR = 0.05f; p.endG = 0.55f; p.endB = 0.92f
            p.startScale = 0.08f
            p.endScale = 0.03f
            p.maxLifeSec = 0.52f

            activeParticles.add(p)
        }
    }

    /**
     * Spawns upward flourishing green sprout particles when sowing seeds in soil.
     */
    fun spawnPlantSprout(x: Float, y: Float, z: Float) {
        val count = 7
        for (i in 0 until count) {
            val p = pool.obtain() ?: break
            val angle = (Math.random() * Math.PI * 2.0).toFloat()
            val speed = (Math.random() * 0.38 + 0.12).toFloat()

            p.posX = x + (Math.random() * 0.2 - 0.1).toFloat()
            p.posY = y + 0.05f
            p.posZ = z + (Math.random() * 0.2 - 0.1).toFloat()

            p.velX = cos(angle) * speed
            p.velY = (Math.random() * 0.65 + 0.35).toFloat()
            p.velZ = sin(angle) * speed

            p.accelY = -1.0f // Gentle upward float
            p.damping = 0.92f
            p.rotSpeedDeg = (Math.random() * 160 - 80).toFloat()
            p.type = ParticleType.PLANT_SPROUT

            // Lush sprout lime-green glow
            p.startR = 0.40f; p.startG = 0.98f; p.startB = 0.32f
            p.endR = 0.22f; p.endG = 0.72f; p.endB = 0.20f
            p.startScale = 0.06f
            p.endScale = 0.11f // Grows slightly before fading
            p.maxLifeSec = 0.48f

            activeParticles.add(p)
        }
    }

    /**
     * Spawns whirling foliage leaves when crops are harvested.
     */
    fun spawnHarvestLeaves(x: Float, y: Float, z: Float) {
        val count = 12
        for (i in 0 until count) {
            val p = pool.obtain() ?: break
            val angle = (Math.random() * Math.PI * 2.0).toFloat()
            val speed = (Math.random() * 1.1 + 0.4).toFloat()

            p.posX = x
            p.posY = y + 0.25f
            p.posZ = z

            p.velX = cos(angle) * speed
            p.velY = (Math.random() * 1.3 + 0.7).toFloat()
            p.velZ = sin(angle) * speed

            p.accelY = -2.0f
            p.damping = 0.94f
            p.rotSpeedDeg = (Math.random() * 240 - 120).toFloat()
            p.type = ParticleType.HARVEST_LEAF

            p.startR = 0.30f; p.startG = 0.94f; p.startB = 0.42f
            p.endR = 0.85f; p.endG = 0.80f; p.endB = 0.20f
            p.startScale = 0.11f
            p.endScale = 0.02f
            p.maxLifeSec = 0.65f

            activeParticles.add(p)
        }
    }

    /**
     * Spawns golden sparkling particles on market trades and coin gains.
     */
    fun spawnCoinSparkles(x: Float, y: Float, z: Float) {
        val count = 10
        for (i in 0 until count) {
            val p = pool.obtain() ?: break
            val angle = (Math.random() * Math.PI * 2.0).toFloat()
            val speed = (Math.random() * 0.75 + 0.25).toFloat()

            p.posX = x
            p.posY = y + 0.65f
            p.posZ = z

            p.velX = cos(angle) * speed
            p.velY = (Math.random() * 0.95 + 0.45).toFloat()
            p.velZ = sin(angle) * speed

            p.accelY = -1.5f
            p.damping = 0.95f
            p.rotSpeedDeg = (Math.random() * 300 - 150).toFloat()
            p.type = ParticleType.COIN_SPARKLE

            p.startR = 1.0f; p.startG = 0.88f; p.startB = 0.18f
            p.endR = 0.95f; p.endG = 0.55f; p.endB = 0.08f
            p.startScale = 0.09f
            p.endScale = 0.02f
            p.maxLifeSec = 0.58f

            activeParticles.add(p)
        }
    }

    /**
     * Spawns expanding ground dust shockwave ring when placing buildings.
     */
    fun spawnBuildDustRing(x: Float, z: Float, radius: Float = 1.8f) {
        val count = 16
        for (i in 0 until count) {
            val p = pool.obtain() ?: break
            val angle = (i.toFloat() / count.toFloat()) * Math.PI.toFloat() * 2f
            val px = x + cos(angle) * radius
            val pz = z + sin(angle) * radius

            p.posX = px
            p.posY = 0.06f
            p.posZ = pz

            p.velX = cos(angle) * 0.95f
            p.velY = 0.35f
            p.velZ = sin(angle) * 0.95f

            p.accelY = -1.8f
            p.damping = 0.90f
            p.rotSpeedDeg = (Math.random() * 100 - 50).toFloat()
            p.type = ParticleType.BUILD_DUST_RING

            p.startR = 0.75f; p.startG = 0.72f; p.startB = 0.64f
            p.endR = 0.45f; p.endG = 0.42f; p.endB = 0.38f
            p.startScale = 0.15f
            p.endScale = 0.03f
            p.maxLifeSec = 0.52f

            activeParticles.add(p)
        }
    }

    /**
     * Updates all active particles and returns finished ones to pool.
     */
    fun update(deltaSec: Float) {
        val iterator = activeParticles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.update(deltaSec)
            if (!p.isAlive) {
                pool.free(p)
                iterator.remove()
            }
        }
    }

    /**
     * Renders active 3D particles with color interpolation and scale curves.
     */
    fun render(shader: GLShader, viewMatrix: FloatArray, projMatrix: FloatArray) {
        if (activeParticles.isEmpty()) return

        for (p in activeParticles) {
            val lifeRatio = (p.ageSec / p.maxLifeSec).coerceIn(0f, 1f)
            val currentScale = p.startScale + (p.endScale - p.startScale) * lifeRatio

            val r = p.startR + (p.endR - p.startR) * lifeRatio
            val g = p.startG + (p.endG - p.startG) * lifeRatio
            val b = p.startB + (p.endB - p.startB) * lifeRatio

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, p.posX, p.posY, p.posZ)
            if (p.rotationDeg != 0f) {
                Matrix.rotateM(modelMatrix, 0, p.rotationDeg, 0f, 1f, 0f)
            }
            Matrix.scaleM(modelMatrix, 0, currentScale, currentScale, currentScale)

            Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, mvMatrix, 0)

            Matrix.invertM(tempMatrix, 0, mvMatrix, 0)
            Matrix.transposeM(normalMatrix, 0, tempMatrix, 0)

            GLES20.glUniformMatrix4fv(shader.uMVPMatrixLoc, 1, false, mvpMatrix, 0)
            GLES20.glUniformMatrix4fv(shader.uMVMatrixLoc, 1, false, mvMatrix, 0)
            GLES20.glUniformMatrix4fv(shader.uNormalMatrixLoc, 1, false, normalMatrix, 0)
            GLES20.glUniform3f(shader.uEmissionColorLoc, r * 0.6f, g * 0.6f, b * 0.6f)
            GLES20.glUniform1f(shader.uShininessLoc, 6.0f)

            if (p.type == ParticleType.PLANT_SPROUT || p.type == ParticleType.HARVEST_LEAF) {
                diamondMesh.draw(shader)
            } else {
                boxMesh.draw(shader)
            }
        }
    }
}
