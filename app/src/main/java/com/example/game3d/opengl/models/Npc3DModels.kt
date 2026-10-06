package com.example.game3d.opengl.models

import android.opengl.GLES20
import android.opengl.Matrix
import com.example.data.local.NpcEntity
import com.example.data.model.NpcActivity
import com.example.data.model.NpcRole
import com.example.game3d.opengl.GLMesh
import com.example.game3d.opengl.GLModelBuilder
import com.example.game3d.opengl.GLShader
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 3D Low-Poly Humanoid Models and Procedural Animations for NPC Settlement Survivors.
 * Features distinctive role-based clothing, accessories, tools, LOD distance culling,
 * and dynamic animations for walking, working, socializing, and sleeping.
 */
class Npc3DModels {

    // Common Humanoid Anatomical Meshes
    private val headMesh: GLMesh
    private val hairMesh: GLMesh
    private val shadowMesh: GLMesh
    private val armMesh: GLMesh
    private val legMesh: GLMesh
    private val bootMesh: GLMesh

    // Role-Specific Outfits & Accessory Meshes
    // 1. Farmer (Straw hat, dungarees, garden trowel)
    private val farmerHatMesh: GLMesh
    private val farmerTorsoMesh: GLMesh

    // 2. Engineer (Work cap, tool belt, copper goggles, wrench)
    private val engineerGogglesMesh: GLMesh
    private val engineerTorsoMesh: GLMesh
    private val engineerWrenchMesh: GLMesh

    // 3. Builder (Yellow safety hardhat, high-vis harness, hammer)
    private val builderHardHatMesh: GLMesh
    private val builderTorsoMesh: GLMesh
    private val builderHammerMesh: GLMesh

    // 4. Researcher (Lab coat, spectacles, holographic tablet)
    private val researcherGlassesMesh: GLMesh
    private val researcherTorsoMesh: GLMesh
    private val researcherTabletMesh: GLMesh

    // 5. Medic (Doctor's white coat, red-cross armband, medical satchel)
    private val medicTorsoMesh: GLMesh
    private val medicSatchelMesh: GLMesh

    // Overhead Status & Speech Bubble Billboard Quad
    private val speechBubbleMesh: GLMesh
    private val roleBadgeMesh: GLMesh

    // Matrix scratchpads
    private val modelMatrix = FloatArray(16)
    private val mvMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val normalMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)
    private val partMatrix = FloatArray(16)

    init {
        val builder = GLModelBuilder()

        // 1. Head (Natural skin tones)
        builder.reset()
        builder.addBox(0f, 0f, 0f, 0.40f, 0.40f, 0.40f, 0.95f, 0.77f, 0.62f)
        headMesh = builder.build()

        // 2. Hair (Brown/Chestnut baseline)
        builder.reset()
        builder.addBox(0f, 0.14f, -0.02f, 0.44f, 0.18f, 0.42f, 0.28f, 0.18f, 0.12f)
        hairMesh = builder.build()

        // 3. Common Limbs
        builder.reset()
        builder.addBox(0f, -0.22f, 0f, 0.16f, 0.48f, 0.16f, 0.85f, 0.75f, 0.65f)
        armMesh = builder.build()

        builder.reset()
        builder.addBox(0f, -0.26f, 0f, 0.20f, 0.52f, 0.22f, 0.25f, 0.32f, 0.40f)
        legMesh = builder.build()

        builder.reset()
        builder.addBox(0f, -0.06f, 0.05f, 0.22f, 0.16f, 0.30f, 0.24f, 0.16f, 0.10f)
        bootMesh = builder.build()

        // 4. Drop Shadow Disk
        builder.reset()
        builder.addCylinder(0f, 0.02f, 0f, 0.50f, 0.01f, 10, 0.18f, 0.14f, 0.11f, 0.38f)
        shadowMesh = builder.build()

        // --- Role Outfits ---
        // Farmer: Straw Hat + Green Dungarees
        builder.reset()
        builder.addCylinder(0f, 0f, 0f, 0.46f, 0.05f, 10, 0.90f, 0.80f, 0.38f)
        builder.addCylinder(0f, 0.05f, 0f, 0.26f, 0.16f, 8, 0.85f, 0.75f, 0.32f)
        farmerHatMesh = builder.build()

        builder.reset()
        builder.addBox(0f, 0.26f, 0f, 0.58f, 0.30f, 0.34f, 0.95f, 0.92f, 0.80f) // Cotton shirt
        builder.addBox(0f, -0.08f, 0f, 0.60f, 0.40f, 0.36f, 0.30f, 0.60f, 0.35f) // Green overalls
        builder.addBox(-0.15f, 0.08f, 0.19f, 0.06f, 0.06f, 0.02f, 0.95f, 0.80f, 0.20f) // Brass clasp
        builder.addBox(0.15f, 0.08f, 0.19f, 0.06f, 0.06f, 0.02f, 0.95f, 0.80f, 0.20f)
        farmerTorsoMesh = builder.build()

        // Engineer: Tool belt, orange safety vest, brass goggles
        builder.reset()
        builder.addBox(-0.12f, 0.06f, 0.22f, 0.12f, 0.08f, 0.04f, 0.80f, 0.55f, 0.15f)
        builder.addBox(0.12f, 0.06f, 0.22f, 0.12f, 0.08f, 0.04f, 0.80f, 0.55f, 0.15f)
        builder.addBox(0f, 0.06f, 0.21f, 0.42f, 0.04f, 0.03f, 0.20f, 0.20f, 0.20f) // Strap
        engineerGogglesMesh = builder.build()

        builder.reset()
        builder.addBox(0f, 0.26f, 0f, 0.58f, 0.30f, 0.34f, 0.10f, 0.65f, 0.80f) // Cyan work shirt
        builder.addBox(0f, -0.06f, 0f, 0.60f, 0.38f, 0.36f, 0.85f, 0.45f, 0.10f) // Orange vest
        builder.addBox(0f, -0.22f, 0f, 0.62f, 0.08f, 0.38f, 0.40f, 0.25f, 0.12f) // Tool belt
        builder.addBox(0.32f, -0.20f, 0f, 0.08f, 0.18f, 0.12f, 0.55f, 0.35f, 0.18f) // Holster
        engineerTorsoMesh = builder.build()

        builder.reset()
        builder.addBox(0f, 0f, 0f, 0.05f, 0.36f, 0.04f, 0.70f, 0.75f, 0.80f) // Wrench handle
        builder.addBox(0f, 0.18f, 0f, 0.14f, 0.10f, 0.04f, 0.80f, 0.85f, 0.90f) // Wrench jaw
        engineerWrenchMesh = builder.build()

        // Builder: Yellow hardhat + high-vis harness + hammer
        builder.reset()
        builder.addSphere(0f, 0.10f, 0f, 0.27f, 6, 8, 0.98f, 0.85f, 0.10f) // Yellow helmet
        builder.addBox(0f, 0.02f, 0.12f, 0.32f, 0.05f, 0.22f, 0.95f, 0.80f, 0.10f) // Brim
        builderHardHatMesh = builder.build()

        builder.reset()
        builder.addBox(0f, 0.26f, 0f, 0.58f, 0.30f, 0.34f, 0.88f, 0.88f, 0.20f) // High-vis yellow shirt
        builder.addBox(0f, -0.06f, 0f, 0.60f, 0.38f, 0.36f, 0.30f, 0.35f, 0.42f) // Denim work trousers
        builder.addBox(0f, 0.22f, 0.18f, 0.48f, 0.06f, 0.02f, 0.95f, 0.95f, 0.95f) // Reflective strip
        builderTorsoMesh = builder.build()

        builder.reset()
        builder.addBox(0f, -0.06f, 0f, 0.05f, 0.38f, 0.05f, 0.48f, 0.30f, 0.15f) // Wooden shaft
        builder.addBox(0f, 0.14f, 0f, 0.16f, 0.10f, 0.08f, 0.45f, 0.50f, 0.55f) // Steel head
        builderHammerMesh = builder.build()

        // Researcher: White lab coat, clipboard, spectacles
        builder.reset()
        builder.addBox(-0.11f, 0.04f, 0.21f, 0.10f, 0.08f, 0.02f, 0.20f, 0.20f, 0.20f)
        builder.addBox(0.11f, 0.04f, 0.21f, 0.10f, 0.08f, 0.02f, 0.20f, 0.20f, 0.20f)
        builder.addBox(0f, 0.04f, 0.21f, 0.14f, 0.02f, 0.02f, 0.20f, 0.20f, 0.20f)
        researcherGlassesMesh = builder.build()

        builder.reset()
        builder.addBox(0f, 0.26f, 0f, 0.58f, 0.30f, 0.34f, 0.15f, 0.45f, 0.60f) // Inner cyan shirt
        builder.addBox(0f, -0.06f, 0f, 0.62f, 0.42f, 0.38f, 0.94f, 0.96f, 0.98f) // White lab coat
        builder.addBox(-0.16f, 0.04f, 0.20f, 0.10f, 0.12f, 0.02f, 0.80f, 0.85f, 0.90f) // Pocket
        researcherTorsoMesh = builder.build()

        builder.reset()
        builder.addBox(0f, 0f, 0f, 0.24f, 0.32f, 0.03f, 0.10f, 0.85f, 0.75f) // Holographic slate
        researcherTabletMesh = builder.build()

        // Medic: Clinical white coat, red cross armband, satchel
        builder.reset()
        builder.addBox(0f, 0.26f, 0f, 0.58f, 0.30f, 0.34f, 0.90f, 0.94f, 0.95f) // White tunic
        builder.addBox(0f, -0.06f, 0f, 0.60f, 0.40f, 0.36f, 0.25f, 0.40f, 0.50f) // Navy trousers
        // Red Cross on chest
        builder.addBox(0.15f, 0.20f, 0.18f, 0.12f, 0.04f, 0.02f, 0.95f, 0.15f, 0.15f)
        builder.addBox(0.15f, 0.20f, 0.18f, 0.04f, 0.12f, 0.02f, 0.95f, 0.15f, 0.15f)
        medicTorsoMesh = builder.build()

        builder.reset()
        builder.addBox(0f, 0f, 0f, 0.14f, 0.22f, 0.26f, 0.85f, 0.20f, 0.20f) // Red satchel
        builder.addBox(0.08f, 0f, 0f, 0.02f, 0.08f, 0.08f, 1.0f, 1.0f, 1.0f) // White cross on satchel
        medicSatchelMesh = builder.build()

        // Speech Bubble & Role Badge Billboards
        builder.reset()
        builder.addBox(0f, 0f, 0f, 0.80f, 0.32f, 0.04f, 0.98f, 0.98f, 0.98f) // White bubble body
        builder.addBox(0f, -0.18f, 0f, 0.12f, 0.10f, 0.04f, 0.98f, 0.98f, 0.98f) // Pointer
        speechBubbleMesh = builder.build()

        builder.reset()
        builder.addBox(0f, 0f, 0f, 0.40f, 0.16f, 0.04f, 0.00f, 0.90f, 0.60f) // Cyan role pill
        roleBadgeMesh = builder.build()
    }

    /**
     * Renders all active NPCs with LOD culling, procedural animation,
     * and role-specific apparel.
     */
    fun drawNpcs(
        shader: GLShader,
        viewMatrix: FloatArray,
        projMatrix: FloatArray,
        npcs: List<NpcEntity>,
        camX: Float,
        camZ: Float,
        animTime: Float
    ) {
        if (npcs.isEmpty()) return

        for (npc in npcs) {
            val dx = npc.posX - camX
            val dz = npc.posZ - camZ
            val dist = sqrt((dx * dx + dz * dz).toDouble()).toFloat()

            // LOD Rule: Beyond 80m, cull entirely to preserve framerate
            if (dist > 80.0f) continue

            val isNearby = dist < 32.0f
            drawSingleNpc(
                shader = shader,
                viewMatrix = viewMatrix,
                projMatrix = projMatrix,
                npc = npc,
                isNearby = isNearby,
                animTime = animTime
            )
        }
    }

    private fun drawSingleNpc(
        shader: GLShader,
        viewMatrix: FloatArray,
        projMatrix: FloatArray,
        npc: NpcEntity,
        isNearby: Boolean,
        animTime: Float
    ) {
        val isSleeping = npc.currentActivity == NpcActivity.SLEEPING
        val isWorking = npc.currentActivity == NpcActivity.WORKING
        val isMoving = npc.isWalking

        // Kinematic walking swing
        val walkFreq = 7.5f
        val swingAmp = if (isMoving) 24.0f else 0.0f
        val legLeftAngle = sin(animTime * walkFreq) * swingAmp
        val legRightAngle = -sin(animTime * walkFreq) * swingAmp
        val armLeftAngle = -legLeftAngle * 0.85f
        val armRightAngle = -legRightAngle * 0.85f

        // Work procedural animation modifier
        var workArmOffset = 0f
        var workHeadOffset = 0f
        if (isWorking && !isMoving) {
            val workOsc = sin(animTime * 4.5f)
            when (npc.role) {
                NpcRole.FARMER -> {
                    workArmOffset = workOsc * 25.0f - 15.0f // Tending crops
                    workHeadOffset = 10.0f
                }
                NpcRole.ENGINEER -> {
                    workArmOffset = workOsc * 30.0f // Wrenching
                }
                NpcRole.BUILDER -> {
                    workArmOffset = abs(workOsc) * 45.0f // Hammering down
                }
                NpcRole.RESEARCHER -> {
                    workArmOffset = -35.0f + workOsc * 6.0f // Holding tablet
                    workHeadOffset = 15.0f
                }
                NpcRole.MEDIC -> {
                    workArmOffset = sin(animTime * 3.0f) * 18.0f // Checking vital signs
                }
            }
        }

        // Base transform
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, npc.posX, npc.posY, npc.posZ)

        // Drop shadow on ground (only if not sleeping)
        if (!isSleeping) {
            renderMesh(shader, shadowMesh, modelMatrix, viewMatrix, projMatrix)
        }

        // Rotation & Posture
        if (isSleeping) {
            // Sleeping: lying flat along the floor/bed
            Matrix.translateM(modelMatrix, 0, 0f, 0.22f, 0f)
            Matrix.rotateM(modelMatrix, 0, npc.rotationDeg, 0f, 1f, 0f)
            Matrix.rotateM(modelMatrix, 0, 90.0f, 1f, 0f, 0f)
        } else {
            Matrix.rotateM(modelMatrix, 0, npc.rotationDeg, 0f, 1f, 0f)
            // Subtle breathing bob
            val breatheY = sin(animTime * 2.2f + npc.id) * 0.02f
            Matrix.translateM(modelMatrix, 0, 0f, breatheY, 0f)
        }

        // 1. Torso (Role-Specific)
        Matrix.multiplyMM(tempMatrix, 0, modelMatrix, 0, getIdentity(), 0)
        Matrix.translateM(tempMatrix, 0, 0f, 1.05f, 0f)
        val torsoMesh = when (npc.role) {
            NpcRole.FARMER -> farmerTorsoMesh
            NpcRole.ENGINEER -> engineerTorsoMesh
            NpcRole.BUILDER -> builderTorsoMesh
            NpcRole.RESEARCHER -> researcherTorsoMesh
            NpcRole.MEDIC -> medicTorsoMesh
        }
        renderMesh(shader, torsoMesh, tempMatrix, viewMatrix, projMatrix)

        // 2. Head & Hair
        Matrix.multiplyMM(tempMatrix, 0, modelMatrix, 0, getIdentity(), 0)
        Matrix.translateM(tempMatrix, 0, 0f, 1.55f, 0f)
        if (workHeadOffset != 0f) {
            Matrix.rotateM(tempMatrix, 0, workHeadOffset, 1f, 0f, 0f)
        }
        renderMesh(shader, headMesh, tempMatrix, viewMatrix, projMatrix)
        renderMesh(shader, hairMesh, tempMatrix, viewMatrix, projMatrix)

        // Head Accessories (Hats, Goggles, Hardhats, Glasses)
        if (isNearby) {
            when (npc.role) {
                NpcRole.FARMER -> {
                    Matrix.translateM(tempMatrix, 0, 0f, 0.18f, 0f)
                    renderMesh(shader, farmerHatMesh, tempMatrix, viewMatrix, projMatrix)
                }
                NpcRole.ENGINEER -> {
                    renderMesh(shader, engineerGogglesMesh, tempMatrix, viewMatrix, projMatrix)
                }
                NpcRole.BUILDER -> {
                    Matrix.translateM(tempMatrix, 0, 0f, 0.12f, 0f)
                    renderMesh(shader, builderHardHatMesh, tempMatrix, viewMatrix, projMatrix)
                }
                NpcRole.RESEARCHER -> {
                    renderMesh(shader, researcherGlassesMesh, tempMatrix, viewMatrix, projMatrix)
                }
                NpcRole.MEDIC -> {}
            }
        }

        // 3. Left Arm
        Matrix.multiplyMM(partMatrix, 0, modelMatrix, 0, getIdentity(), 0)
        Matrix.translateM(partMatrix, 0, -0.38f, 1.30f, 0f)
        Matrix.rotateM(partMatrix, 0, armLeftAngle + workArmOffset, 1f, 0f, 0f)
        renderMesh(shader, armMesh, partMatrix, viewMatrix, projMatrix)

        // 4. Right Arm + Handheld Tool
        Matrix.multiplyMM(partMatrix, 0, modelMatrix, 0, getIdentity(), 0)
        Matrix.translateM(partMatrix, 0, 0.38f, 1.30f, 0f)
        Matrix.rotateM(partMatrix, 0, armRightAngle - workArmOffset, 1f, 0f, 0f)
        renderMesh(shader, armMesh, partMatrix, viewMatrix, projMatrix)

        // Handheld tools for nearby LOD
        if (isNearby && isWorking && !isSleeping) {
            when (npc.role) {
                NpcRole.ENGINEER -> {
                    Matrix.translateM(partMatrix, 0, 0f, -0.42f, 0.10f)
                    renderMesh(shader, engineerWrenchMesh, partMatrix, viewMatrix, projMatrix)
                }
                NpcRole.BUILDER -> {
                    Matrix.translateM(partMatrix, 0, 0f, -0.42f, 0.10f)
                    renderMesh(shader, builderHammerMesh, partMatrix, viewMatrix, projMatrix)
                }
                NpcRole.RESEARCHER -> {
                    Matrix.translateM(partMatrix, 0, 0f, -0.38f, 0.14f)
                    Matrix.rotateM(partMatrix, 0, -35.0f, 1f, 0f, 0f)
                    renderMesh(shader, researcherTabletMesh, partMatrix, viewMatrix, projMatrix)
                }
                NpcRole.MEDIC -> {
                    Matrix.translateM(partMatrix, 0, 0.16f, -0.15f, 0f)
                    renderMesh(shader, medicSatchelMesh, partMatrix, viewMatrix, projMatrix)
                }
                NpcRole.FARMER -> {}
            }
        }

        // 5. Left Leg & Boot
        Matrix.multiplyMM(partMatrix, 0, modelMatrix, 0, getIdentity(), 0)
        Matrix.translateM(partMatrix, 0, -0.16f, 0.70f, 0f)
        Matrix.rotateM(partMatrix, 0, legLeftAngle, 1f, 0f, 0f)
        renderMesh(shader, legMesh, partMatrix, viewMatrix, projMatrix)
        Matrix.translateM(partMatrix, 0, 0f, -0.56f, 0f)
        renderMesh(shader, bootMesh, partMatrix, viewMatrix, projMatrix)

        // 6. Right Leg & Boot
        Matrix.multiplyMM(partMatrix, 0, modelMatrix, 0, getIdentity(), 0)
        Matrix.translateM(partMatrix, 0, 0.16f, 0.70f, 0f)
        Matrix.rotateM(partMatrix, 0, legRightAngle, 1f, 0f, 0f)
        renderMesh(shader, legMesh, partMatrix, viewMatrix, projMatrix)
        Matrix.translateM(partMatrix, 0, 0f, -0.56f, 0f)
        renderMesh(shader, bootMesh, partMatrix, viewMatrix, projMatrix)

        // 7. Overhead Speech Bubble / Socializing Indicator
        if (isNearby && !isSleeping && npc.speechBubble != null && npc.speechBubbleTimer > 0f) {
            Matrix.multiplyMM(tempMatrix, 0, modelMatrix, 0, getIdentity(), 0)
            Matrix.translateM(tempMatrix, 0, 0f, 2.25f, 0f)
            val bubbleBob = sin(animTime * 3.5f) * 0.05f
            Matrix.translateM(tempMatrix, 0, 0f, bubbleBob, 0f)
            renderMesh(shader, speechBubbleMesh, tempMatrix, viewMatrix, projMatrix)
        }
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

    private fun getIdentity(): FloatArray {
        val arr = FloatArray(16)
        Matrix.setIdentityM(arr, 0)
        return arr
    }
}
