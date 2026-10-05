package com.example.game3d.opengl.models

import android.opengl.GLES20
import android.opengl.Matrix
import com.example.data.local.LivestockEntity
import com.example.data.model.LivestockType
import com.example.game3d.opengl.GLMesh
import com.example.game3d.opengl.GLModelBuilder
import com.example.game3d.opengl.GLShader
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class Animal3DModels {

    // Cow Meshes
    private val cowBodyMesh: GLMesh
    private val cowHeadMesh: GLMesh
    private val cowLegMesh: GLMesh

    // Sheep Meshes
    private val sheepBodyMesh: GLMesh
    private val sheepHeadMesh: GLMesh
    private val sheepLegMesh: GLMesh

    // Beehive Mesh
    private val beehiveMesh: GLMesh

    // Chicken Mesh
    private val chickenMesh: GLMesh

    // Shared Drop Shadow Mesh
    private val shadowMesh: GLMesh

    // Matrix Scratchpads
    private val modelMatrix = FloatArray(16)
    private val mvMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val normalMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)
    private val partMatrix = FloatArray(16)

    init {
        val builder = GLModelBuilder()

        // 1. COW (Meadow Cow)
        // Body (1.85m long, 0.88m high, 0.92m wide - White with Black spots)
        builder.reset()
        builder.addBox(0f, 0.95f, 0f, 0.92f, 0.88f, 1.85f, 0.95f, 0.95f, 0.95f)
        // Black spots
        builder.addBox(0.47f, 1.0f, 0.2f, 0.05f, 0.45f, 0.55f, 0.12f, 0.12f, 0.15f)
        builder.addBox(-0.47f, 0.9f, -0.3f, 0.05f, 0.40f, 0.50f, 0.12f, 0.12f, 0.15f)
        builder.addBox(0f, 1.4f, 0.1f, 0.60f, 0.05f, 0.60f, 0.12f, 0.12f, 0.15f)
        // Pink Udder
        builder.addBox(0f, 0.48f, -0.35f, 0.42f, 0.22f, 0.42f, 0.96f, 0.72f, 0.76f)
        cowBodyMesh = builder.build()

        // Cow Head (Head, Pink Snout, Horns, Ears)
        builder.reset()
        builder.addBox(0f, 0.15f, 0.35f, 0.52f, 0.52f, 0.52f, 0.92f, 0.92f, 0.92f)
        // Pink Snout & Nostrils
        builder.addBox(0f, -0.05f, 0.64f, 0.44f, 0.26f, 0.22f, 0.96f, 0.74f, 0.78f)
        builder.addBox(-0.12f, -0.02f, 0.76f, 0.08f, 0.08f, 0.02f, 0.25f, 0.15f, 0.18f)
        builder.addBox(0.12f, -0.02f, 0.76f, 0.08f, 0.08f, 0.02f, 0.25f, 0.15f, 0.18f)
        // Horns (Cream/Ivory)
        builder.addCone(-0.28f, 0.38f, 0.30f, 0.08f, 0.28f, 6, 0.95f, 0.90f, 0.78f)
        builder.addCone(0.28f, 0.38f, 0.30f, 0.08f, 0.28f, 6, 0.95f, 0.90f, 0.78f)
        // Ears
        builder.addBox(-0.35f, 0.24f, 0.25f, 0.22f, 0.10f, 0.08f, 0.92f, 0.92f, 0.92f)
        builder.addBox(0.35f, 0.24f, 0.25f, 0.22f, 0.10f, 0.08f, 0.92f, 0.92f, 0.92f)
        cowHeadMesh = builder.build()

        // Cow Leg (0.60m high, 0.18m wide)
        builder.reset()
        builder.addBox(0f, -0.30f, 0f, 0.18f, 0.60f, 0.18f, 0.92f, 0.92f, 0.92f)
        builder.addBox(0f, -0.58f, 0f, 0.20f, 0.12f, 0.20f, 0.18f, 0.18f, 0.20f) // Hoof
        cowLegMesh = builder.build()

        // 2. SHEEP / ALPACA (Solar Sheep & Cloud Alpaca)
        // Fluffy Golden-tinted Wool Body (1.35m long, 0.76m high, 0.88m wide)
        builder.reset()
        builder.addBox(0f, 0.75f, 0f, 0.88f, 0.76f, 1.35f, 0.96f, 0.94f, 0.86f)
        // Solar sheen patches
        builder.addBox(0f, 1.15f, 0f, 0.70f, 0.08f, 1.10f, 0.98f, 0.90f, 0.40f)
        sheepBodyMesh = builder.build()

        // Sheep Head (Dark charcoal face)
        builder.reset()
        builder.addBox(0f, 0.10f, 0.26f, 0.38f, 0.38f, 0.44f, 0.25f, 0.24f, 0.28f)
        // Fluffy wool cap
        builder.addBox(0f, 0.32f, 0.22f, 0.42f, 0.16f, 0.36f, 0.96f, 0.94f, 0.86f)
        // Droopy Ears
        builder.addBox(-0.25f, 0.12f, 0.16f, 0.18f, 0.08f, 0.08f, 0.22f, 0.20f, 0.24f)
        builder.addBox(0.25f, 0.12f, 0.16f, 0.18f, 0.08f, 0.08f, 0.22f, 0.20f, 0.24f)
        sheepHeadMesh = builder.build()

        // Sheep Leg
        builder.reset()
        builder.addBox(0f, -0.22f, 0f, 0.14f, 0.46f, 0.14f, 0.25f, 0.24f, 0.28f)
        sheepLegMesh = builder.build()

        // 3. BEEHIVE & DRONE
        builder.reset()
        // Wooden Hive Box
        builder.addBox(0f, 0.45f, 0f, 0.70f, 0.65f, 0.70f, 0.78f, 0.52f, 0.24f)
        builder.addRoofPrism(0f, 0.78f, 0f, 0.82f, 0.26f, 0.82f, 0.58f, 0.36f, 0.16f)
        // Base post
        builder.addCylinder(0f, 0f, 0f, 0.10f, 0.45f, 6, 0.45f, 0.30f, 0.15f)
        beehiveMesh = builder.build()

        // 3b. LOW-POLY CHICKEN (Height = 0.4 units)
        builder.reset()
        // White Body
        builder.addBox(0f, 0.20f, 0f, 0.26f, 0.30f, 0.36f, 0.98f, 0.96f, 0.90f)
        // Red Comb
        builder.addBox(0f, 0.38f, 0.10f, 0.05f, 0.10f, 0.12f, 0.95f, 0.15f, 0.10f)
        // Beak
        builder.addCone(0f, 0.26f, 0.19f, 0.06f, 0.12f, 4, 0.98f, 0.70f, 0.15f)
        // Yellow legs
        builder.addCylinder(-0.06f, 0f, 0f, 0.02f, 0.10f, 4, 0.98f, 0.70f, 0.15f)
        builder.addCylinder(0.06f, 0f, 0f, 0.02f, 0.10f, 4, 0.98f, 0.70f, 0.15f)
        chickenMesh = builder.build()

        // 4. Drop Shadow Disk
        builder.reset()
        builder.addCylinder(0f, 0.02f, 0f, 0.65f, 0.01f, 10, 0.04f, 0.08f, 0.06f, 0.5f)
        shadowMesh = builder.build()
    }

    fun drawLivestock(
        shader: GLShader,
        viewMatrix: FloatArray,
        projMatrix: FloatArray,
        animals: List<LivestockEntity>,
        animTime: Float
    ) {
        for (animal in animals) {
            drawSingleAnimal(shader, viewMatrix, projMatrix, animal, animTime)
        }
    }

    private fun drawSingleAnimal(
        shader: GLShader,
        viewMatrix: FloatArray,
        projMatrix: FloatArray,
        animal: LivestockEntity,
        animTime: Float
    ) {
        // Calculate orientation angle towards target
        val dx = animal.targetX - animal.posX
        val dz = animal.targetZ - animal.posZ
        val isWalking = (dx * dx + dz * dz) > 0.15f
        val facingAngleDeg = if (isWalking) {
            Math.toDegrees(atan2(dx.toDouble(), dz.toDouble())).toFloat()
        } else {
            ((animal.id * 73) % 360).toFloat()
        }

        val walkCycle = if (isWalking) (animTime * 6.5f + animal.id * 1.5f) else 0.0f
        val legSwing = sin(walkCycle) * 22.0f
        val headBob = sin(animTime * 2.0f + animal.id) * 4.0f

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, animal.posX, animal.posY, animal.posZ)

        // 1. Draw Drop Shadow
        renderMesh(shader, shadowMesh, modelMatrix, viewMatrix, projMatrix)

        // Orient animal
        Matrix.rotateM(modelMatrix, 0, facingAngleDeg, 0f, 1f, 0f)

        when (animal.type) {
            LivestockType.CYBER_BOVINE -> {
                // Cow Body
                renderMesh(shader, cowBodyMesh, modelMatrix, viewMatrix, projMatrix)

                // Head
                Matrix.setIdentityM(partMatrix, 0)
                Matrix.multiplyMM(partMatrix, 0, modelMatrix, 0, partMatrix, 0)
                Matrix.translateM(partMatrix, 0, 0f, 1.15f, 0.75f)
                Matrix.rotateM(partMatrix, 0, headBob, 1f, 0f, 0f)
                renderMesh(shader, cowHeadMesh, partMatrix, viewMatrix, projMatrix)

                // 4 Legs
                drawLeg(shader, cowLegMesh, modelMatrix, viewMatrix, projMatrix, -0.32f, 0.58f, 0.55f, legSwing)
                drawLeg(shader, cowLegMesh, modelMatrix, viewMatrix, projMatrix, 0.32f, 0.58f, 0.55f, -legSwing)
                drawLeg(shader, cowLegMesh, modelMatrix, viewMatrix, projMatrix, -0.32f, 0.58f, -0.55f, -legSwing)
                drawLeg(shader, cowLegMesh, modelMatrix, viewMatrix, projMatrix, 0.32f, 0.58f, -0.55f, legSwing)
            }
            LivestockType.SOLAR_SHEEP -> {
                renderMesh(shader, sheepBodyMesh, modelMatrix, viewMatrix, projMatrix)

                Matrix.setIdentityM(partMatrix, 0)
                Matrix.multiplyMM(partMatrix, 0, modelMatrix, 0, partMatrix, 0)
                Matrix.translateM(partMatrix, 0, 0f, 0.85f, 0.55f)
                Matrix.rotateM(partMatrix, 0, headBob, 1f, 0f, 0f)
                renderMesh(shader, sheepHeadMesh, partMatrix, viewMatrix, projMatrix)

                drawLeg(shader, sheepLegMesh, modelMatrix, viewMatrix, projMatrix, -0.28f, 0.44f, 0.42f, legSwing)
                drawLeg(shader, sheepLegMesh, modelMatrix, viewMatrix, projMatrix, 0.28f, 0.44f, 0.42f, -legSwing)
                drawLeg(shader, sheepLegMesh, modelMatrix, viewMatrix, projMatrix, -0.28f, 0.44f, -0.42f, -legSwing)
                drawLeg(shader, sheepLegMesh, modelMatrix, viewMatrix, projMatrix, 0.28f, 0.44f, -0.42f, legSwing)
            }
            LivestockType.MEADOW_ALPACA -> {
                renderMesh(shader, sheepBodyMesh, modelMatrix, viewMatrix, projMatrix)

                Matrix.setIdentityM(partMatrix, 0)
                Matrix.multiplyMM(partMatrix, 0, modelMatrix, 0, partMatrix, 0)
                Matrix.translateM(partMatrix, 0, 0f, 0.85f, 0.55f)
                Matrix.rotateM(partMatrix, 0, headBob, 1f, 0f, 0f)
                renderMesh(shader, sheepHeadMesh, partMatrix, viewMatrix, projMatrix)

                drawLeg(shader, sheepLegMesh, modelMatrix, viewMatrix, projMatrix, -0.28f, 0.44f, 0.42f, legSwing)
                drawLeg(shader, sheepLegMesh, modelMatrix, viewMatrix, projMatrix, 0.28f, 0.44f, 0.42f, -legSwing)
                drawLeg(shader, sheepLegMesh, modelMatrix, viewMatrix, projMatrix, -0.28f, 0.44f, -0.42f, -legSwing)
                drawLeg(shader, sheepLegMesh, modelMatrix, viewMatrix, projMatrix, 0.28f, 0.44f, -0.42f, legSwing)
            }
            LivestockType.ROBO_BEE_POLLINATOR -> {
                renderMesh(shader, beehiveMesh, modelMatrix, viewMatrix, projMatrix)
            }
            LivestockType.CHICKEN -> {
                val bob = if (isWalking) sin(animTime * 10f) * 0.03f else 0.0f
                Matrix.translateM(modelMatrix, 0, 0f, bob, 0f)
                renderMesh(shader, chickenMesh, modelMatrix, viewMatrix, projMatrix)
            }
        }
    }

    private fun drawLeg(
        shader: GLShader,
        legMesh: GLMesh,
        parentMatrix: FloatArray,
        vMatrix: FloatArray,
        pMatrix: FloatArray,
        x: Float,
        y: Float,
        z: Float,
        swingDeg: Float
    ) {
        Matrix.setIdentityM(partMatrix, 0)
        Matrix.multiplyMM(partMatrix, 0, parentMatrix, 0, partMatrix, 0)
        Matrix.translateM(partMatrix, 0, x, y, z)
        Matrix.rotateM(partMatrix, 0, swingDeg, 1f, 0f, 0f)
        renderMesh(shader, legMesh, partMatrix, vMatrix, pMatrix)
    }

    private fun renderMesh(
        shader: GLShader,
        mesh: GLMesh,
        mMatrix: FloatArray,
        vMatrix: FloatArray,
        pMatrix: FloatArray
    ) {
        Matrix.multiplyMM(mvMatrix, 0, vMatrix, 0, mMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, pMatrix, 0, mvMatrix, 0)

        Matrix.invertM(tempMatrix, 0, mvMatrix, 0)
        Matrix.transposeM(normalMatrix, 0, tempMatrix, 0)

        GLES20.glUniformMatrix4fv(shader.uMVPMatrixLoc, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(shader.uMVMatrixLoc, 1, false, mvMatrix, 0)
        GLES20.glUniformMatrix4fv(shader.uNormalMatrixLoc, 1, false, normalMatrix, 0)
        GLES20.glUniform3f(shader.uEmissionColorLoc, 0f, 0f, 0f)
        GLES20.glUniform1f(shader.uShininessLoc, 6.0f)

        mesh.draw(shader)
    }
}
