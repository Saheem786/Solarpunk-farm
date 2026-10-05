package com.example.game3d.opengl.models

import android.opengl.GLES20
import android.opengl.Matrix
import com.example.game3d.opengl.GLMesh
import com.example.game3d.opengl.GLModelBuilder
import com.example.game3d.opengl.GLShader
import kotlin.math.cos
import kotlin.math.sin

enum class PlayerActionAnim {
    NONE,
    HARVESTING,
    WATERING,
    PLANTING,
    FISHING,
    DRINKING
}

class Player3DModel {

    private val headMesh: GLMesh
    private val hatMesh: GLMesh
    private val torsoMesh: GLMesh
    private val armMesh: GLMesh
    private val legMesh: GLMesh
    private val bootMesh: GLMesh
    private val shadowMesh: GLMesh
    private val fishingRodMesh: GLMesh

    private val modelMatrix = FloatArray(16)
    private val mvMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val normalMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)
    private val partMatrix = FloatArray(16)

    init {
        val builder = GLModelBuilder()

        // 1. Head (Peach Skin: 0.95, 0.76, 0.62)
        builder.reset()
        builder.addBox(0f, 0f, 0f, 0.44f, 0.44f, 0.44f, 0.96f, 0.78f, 0.64f)
        // Hair (Brown: 0.35, 0.22, 0.12)
        builder.addBox(0f, 0.16f, -0.04f, 0.48f, 0.20f, 0.46f, 0.36f, 0.22f, 0.12f)
        headMesh = builder.build()

        // 2. Straw Hat / Solar Cap (Golden Straw: 0.92, 0.82, 0.38)
        builder.reset()
        builder.addCylinder(0f, 0f, 0f, 0.48f, 0.06f, 10, 0.92f, 0.82f, 0.38f) // Brim
        builder.addCylinder(0f, 0.06f, 0f, 0.28f, 0.18f, 8, 0.88f, 0.76f, 0.32f) // Crown
        // Emerald Solar Ribbon
        builder.addCylinder(0f, 0.07f, 0f, 0.29f, 0.05f, 8, 0.00f, 0.90f, 0.48f)
        hatMesh = builder.build()

        // 3. Torso (Farmer Overalls + Cyan Solar Shirt: 0.05, 0.75, 0.85 & Denim Blue: 0.15, 0.32, 0.55)
        builder.reset()
        // Shirt Upper
        builder.addBox(0f, 0.28f, 0f, 0.62f, 0.32f, 0.36f, 0.08f, 0.72f, 0.82f)
        // Overalls Lower & Suspenders
        builder.addBox(0f, -0.08f, 0f, 0.64f, 0.44f, 0.38f, 0.18f, 0.35f, 0.60f)
        // Brass Buckles
        builder.addBox(-0.16f, 0.08f, 0.20f, 0.08f, 0.08f, 0.03f, 0.98f, 0.82f, 0.15f)
        builder.addBox(0.16f, 0.08f, 0.20f, 0.08f, 0.08f, 0.03f, 0.98f, 0.82f, 0.15f)
        // Bio-pouch on hip
        builder.addBox(0.33f, -0.06f, 0.05f, 0.12f, 0.22f, 0.18f, 0.48f, 0.32f, 0.18f)
        torsoMesh = builder.build()

        // 4. Arm (Shoulder down: 0.56m long, 0.18m wide)
        builder.reset()
        builder.addBox(0f, -0.24f, 0f, 0.18f, 0.52f, 0.18f, 0.08f, 0.72f, 0.82f)
        // Hand
        builder.addBox(0f, -0.54f, 0f, 0.16f, 0.16f, 0.16f, 0.96f, 0.78f, 0.64f)
        armMesh = builder.build()

        // 5. Leg (Hip down: 0.62m long, 0.22m wide)
        builder.reset()
        builder.addBox(0f, -0.28f, 0f, 0.22f, 0.56f, 0.24f, 0.18f, 0.35f, 0.60f)
        legMesh = builder.build()

        // 6. Boot (Sturdy Leather Work Boot: 0.28, 0.16, 0.08)
        builder.reset()
        builder.addBox(0f, -0.08f, 0.06f, 0.24f, 0.18f, 0.34f, 0.28f, 0.16f, 0.08f)
        bootMesh = builder.build()

        // 7. Drop Shadow Disk (Dark Translucent Oval on Ground)
        builder.reset()
        builder.addCylinder(0f, 0.02f, 0f, 0.55f, 0.01f, 10, 0.04f, 0.08f, 0.06f, 0.55f)
        shadowMesh = builder.build()

        // 8. Handheld Fishing Rod
        builder.reset()
        builder.addCylinder(0f, 0.95f, 0f, 0.022f, 2.1f, 6, 0.45f, 0.30f, 0.16f)
        builder.addCylinder(0f, 0.12f, 0.06f, 0.055f, 0.08f, 6, 0.85f, 0.72f, 0.25f) // reel
        fishingRodMesh = builder.build()
    }

    fun draw(
        shader: GLShader,
        viewMatrix: FloatArray,
        projMatrix: FloatArray,
        posX: Float,
        posY: Float,
        posZ: Float,
        rotationDeg: Float,
        walkPhase: Float,
        isMoving: Boolean,
        isRunning: Boolean = false,
        animTime: Float = 0f,
        actionAnim: PlayerActionAnim = PlayerActionAnim.NONE,
        actionProgress: Float = 0f
    ) {
        // Kinematic Walking / Running Swing Calculations
        val swingAmp = when {
            isMoving && isRunning -> 38.0f
            isMoving -> 24.0f
            else -> 0.0f
        }
        val legLeftAngle = sin(walkPhase) * swingAmp
        val legRightAngle = -sin(walkPhase) * swingAmp

        // Arm swing & Action Overrides
        var armLeftAngle = -sin(walkPhase) * swingAmp * 0.9f
        var armRightAngle = sin(walkPhase) * swingAmp * 0.9f
        var torsoPitch = 0.0f

        // Procedural Idle Breathing
        val idleBreath = if (!isMoving) sin(animTime * 2.8f) * 0.015f else 0.0f
        val bodyBobY = (if (isMoving) kotlin.math.abs(sin(walkPhase * 2f)) * (if (isRunning) 0.09f else 0.05f) else 0.0f) + idleBreath

        // Action Pose Blending (Harvest bend down, Watering arm tilt)
        if (actionProgress > 0f) {
            val blend = sin(actionProgress * Math.PI.toFloat())
            when (actionAnim) {
                PlayerActionAnim.HARVESTING, PlayerActionAnim.PLANTING -> {
                    torsoPitch = 35.0f * blend
                    armLeftAngle = 45.0f * blend
                    armRightAngle = 45.0f * blend
                }
                PlayerActionAnim.WATERING -> {
                    armRightAngle = 65.0f * blend
                    armLeftAngle = -15.0f * blend
                    torsoPitch = 10.0f * blend
                }
                PlayerActionAnim.FISHING -> {
                    armRightAngle = 65.0f
                    armLeftAngle = 35.0f
                    torsoPitch = 5.0f
                }
                PlayerActionAnim.DRINKING -> {
                    armRightAngle = 85.0f * blend
                    torsoPitch = -8.0f * blend
                }
                PlayerActionAnim.NONE -> {}
            }
        } else if (actionAnim == PlayerActionAnim.FISHING) {
            armRightAngle = 65.0f
            armLeftAngle = 35.0f
            torsoPitch = 5.0f
        }

        // Base Root Transform
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, posX, posY, posZ)

        // 1. Draw Drop Shadow Disk on Terrain
        renderMesh(shader, shadowMesh, modelMatrix, viewMatrix, projMatrix)

        // Apply Player Yaw Orientation
        Matrix.rotateM(modelMatrix, 0, rotationDeg, 0f, 1f, 0f)

        // 2. Torso (Y base = 0.98m)
        Matrix.setIdentityM(partMatrix, 0)
        Matrix.multiplyMM(partMatrix, 0, modelMatrix, 0, partMatrix, 0)
        Matrix.translateM(partMatrix, 0, 0f, 0.98f + bodyBobY, 0f)
        if (torsoPitch != 0f) {
            Matrix.rotateM(partMatrix, 0, torsoPitch, 1f, 0f, 0f)
        }
        renderMesh(shader, torsoMesh, partMatrix, viewMatrix, projMatrix)

        // 3. Head & Hat (Y base = 1.56m)
        Matrix.setIdentityM(partMatrix, 0)
        Matrix.multiplyMM(partMatrix, 0, modelMatrix, 0, partMatrix, 0)
        Matrix.translateM(partMatrix, 0, 0f, 1.56f + bodyBobY, 0f)
        if (torsoPitch != 0f) {
            Matrix.rotateM(partMatrix, 0, torsoPitch * 0.7f, 1f, 0f, 0f)
        }
        renderMesh(shader, headMesh, partMatrix, viewMatrix, projMatrix)

        Matrix.translateM(partMatrix, 0, 0f, 0.22f, 0f)
        renderMesh(shader, hatMesh, partMatrix, viewMatrix, projMatrix)

        // 4. Left Arm (Pivot at X = -0.38m, Y = 1.25m)
        Matrix.setIdentityM(partMatrix, 0)
        Matrix.multiplyMM(partMatrix, 0, modelMatrix, 0, partMatrix, 0)
        Matrix.translateM(partMatrix, 0, -0.38f, 1.25f + bodyBobY, 0f)
        Matrix.rotateM(partMatrix, 0, armLeftAngle, 1f, 0f, 0f)
        renderMesh(shader, armMesh, partMatrix, viewMatrix, projMatrix)

        // 5. Right Arm (Pivot at X = +0.38m, Y = 1.25m)
        Matrix.setIdentityM(partMatrix, 0)
        Matrix.multiplyMM(partMatrix, 0, modelMatrix, 0, partMatrix, 0)
        Matrix.translateM(partMatrix, 0, 0.38f, 1.25f + bodyBobY, 0f)
        Matrix.rotateM(partMatrix, 0, armRightAngle, 1f, 0f, 0f)
        renderMesh(shader, armMesh, partMatrix, viewMatrix, projMatrix)

        // 6. Left Leg & Boot (Pivot at X = -0.16m, Y = 0.74m)
        Matrix.setIdentityM(partMatrix, 0)
        Matrix.multiplyMM(partMatrix, 0, modelMatrix, 0, partMatrix, 0)
        Matrix.translateM(partMatrix, 0, -0.16f, 0.74f, 0f)
        Matrix.rotateM(partMatrix, 0, legLeftAngle, 1f, 0f, 0f)
        renderMesh(shader, legMesh, partMatrix, viewMatrix, projMatrix)

        Matrix.translateM(partMatrix, 0, 0f, -0.58f, 0f)
        renderMesh(shader, bootMesh, partMatrix, viewMatrix, projMatrix)

        // 7. Right Leg & Boot (Pivot at X = +0.16m, Y = 0.74m)
        Matrix.setIdentityM(partMatrix, 0)
        Matrix.multiplyMM(partMatrix, 0, modelMatrix, 0, partMatrix, 0)
        Matrix.translateM(partMatrix, 0, 0.16f, 0.74f, 0f)
        Matrix.rotateM(partMatrix, 0, legRightAngle, 1f, 0f, 0f)
        renderMesh(shader, legMesh, partMatrix, viewMatrix, projMatrix)

        Matrix.translateM(partMatrix, 0, 0f, -0.58f, 0f)
        renderMesh(shader, bootMesh, partMatrix, viewMatrix, projMatrix)
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
        GLES20.glUniform1f(shader.uShininessLoc, 8.0f)

        mesh.draw(shader)
    }
}
