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

    // Farm Livestock Meshes
    private val cowBodyMesh: GLMesh
    private val cowHeadMesh: GLMesh
    private val cowLegMesh: GLMesh
    private val sheepBodyMesh: GLMesh
    private val sheepHeadMesh: GLMesh
    private val sheepLegMesh: GLMesh
    private val beehiveMesh: GLMesh
    private val chickenMesh: GLMesh

    // Deep Forest Wildlife Meshes
    private val deerBodyMesh: GLMesh
    private val deerHeadMesh: GLMesh
    private val deerLegMesh: GLMesh
    private val rabbitMesh: GLMesh
    private val owlMesh: GLMesh
    private val foxWolfMesh: GLMesh

    // Wetland Wildlife Meshes
    private val duckMesh: GLMesh
    private val frogMesh: GLMesh
    private val heronMesh: GLMesh
    private val fishMesh: GLMesh

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
        builder.reset()
        builder.addBox(0f, 0.95f, 0f, 0.92f, 0.88f, 1.85f, 0.95f, 0.95f, 0.95f)
        builder.addBox(0.47f, 1.0f, 0.2f, 0.05f, 0.45f, 0.55f, 0.12f, 0.12f, 0.15f)
        builder.addBox(-0.47f, 0.9f, -0.3f, 0.05f, 0.40f, 0.50f, 0.12f, 0.12f, 0.15f)
        builder.addBox(0f, 1.4f, 0.1f, 0.60f, 0.05f, 0.60f, 0.12f, 0.12f, 0.15f)
        builder.addBox(0f, 0.48f, -0.35f, 0.42f, 0.22f, 0.42f, 0.96f, 0.72f, 0.76f)
        cowBodyMesh = builder.build()

        builder.reset()
        builder.addBox(0f, 0.15f, 0.35f, 0.52f, 0.52f, 0.52f, 0.92f, 0.92f, 0.92f)
        builder.addBox(0f, -0.05f, 0.64f, 0.44f, 0.26f, 0.22f, 0.96f, 0.74f, 0.78f)
        builder.addBox(-0.12f, -0.02f, 0.76f, 0.08f, 0.08f, 0.02f, 0.25f, 0.15f, 0.18f)
        builder.addBox(0.12f, -0.02f, 0.76f, 0.08f, 0.08f, 0.02f, 0.25f, 0.15f, 0.18f)
        builder.addCone(-0.28f, 0.38f, 0.30f, 0.08f, 0.28f, 6, 0.95f, 0.90f, 0.78f)
        builder.addCone(0.28f, 0.38f, 0.30f, 0.08f, 0.28f, 6, 0.95f, 0.90f, 0.78f)
        builder.addBox(-0.35f, 0.24f, 0.25f, 0.22f, 0.10f, 0.08f, 0.92f, 0.92f, 0.92f)
        builder.addBox(0.35f, 0.24f, 0.25f, 0.22f, 0.10f, 0.08f, 0.92f, 0.92f, 0.92f)
        cowHeadMesh = builder.build()

        builder.reset()
        builder.addBox(0f, -0.30f, 0f, 0.18f, 0.60f, 0.18f, 0.92f, 0.92f, 0.92f)
        builder.addBox(0f, -0.58f, 0f, 0.20f, 0.12f, 0.20f, 0.18f, 0.18f, 0.20f)
        cowLegMesh = builder.build()

        // 2. SHEEP / ALPACA
        builder.reset()
        builder.addBox(0f, 0.75f, 0f, 0.88f, 0.76f, 1.35f, 0.96f, 0.94f, 0.86f)
        builder.addBox(0f, 1.15f, 0f, 0.70f, 0.08f, 1.10f, 0.98f, 0.90f, 0.40f)
        sheepBodyMesh = builder.build()

        builder.reset()
        builder.addBox(0f, 0.10f, 0.26f, 0.38f, 0.38f, 0.44f, 0.25f, 0.24f, 0.28f)
        builder.addBox(0f, 0.32f, 0.22f, 0.42f, 0.16f, 0.36f, 0.96f, 0.94f, 0.86f)
        builder.addBox(-0.25f, 0.12f, 0.16f, 0.18f, 0.08f, 0.08f, 0.22f, 0.20f, 0.24f)
        builder.addBox(0.25f, 0.12f, 0.16f, 0.18f, 0.08f, 0.08f, 0.22f, 0.20f, 0.24f)
        sheepHeadMesh = builder.build()

        builder.reset()
        builder.addBox(0f, -0.22f, 0f, 0.14f, 0.46f, 0.14f, 0.25f, 0.24f, 0.28f)
        sheepLegMesh = builder.build()

        // 3. BEEHIVE
        builder.reset()
        builder.addBox(0f, 0.50f, 0f, 0.85f, 0.85f, 0.85f, 0.82f, 0.65f, 0.38f)
        builder.addBox(0f, 0.95f, 0f, 0.95f, 0.10f, 0.95f, 0.58f, 0.42f, 0.22f)
        builder.addBox(0f, 0.35f, 0.44f, 0.30f, 0.06f, 0.04f, 0.12f, 0.10f, 0.08f)
        beehiveMesh = builder.build()

        // 4. CHICKEN
        builder.reset()
        builder.addBox(0f, 0.32f, 0f, 0.34f, 0.36f, 0.44f, 0.98f, 0.96f, 0.92f)
        builder.addBox(0f, 0.54f, 0.16f, 0.22f, 0.24f, 0.22f, 0.98f, 0.96f, 0.92f)
        builder.addBox(0f, 0.70f, 0.16f, 0.06f, 0.12f, 0.18f, 0.95f, 0.20f, 0.20f)
        builder.addCone(0f, 0.52f, 0.30f, 0.06f, 0.12f, 4, 0.98f, 0.75f, 0.10f)
        builder.addBox(0f, 0.46f, 0.12f, 0.06f, 0.10f, 0.12f, 0.95f, 0.20f, 0.20f)
        builder.addBox(0f, 0.42f, -0.26f, 0.12f, 0.22f, 0.18f, 0.85f, 0.80f, 0.75f)
        builder.addBox(-0.10f, 0.07f, 0f, 0.04f, 0.16f, 0.04f, 0.98f, 0.75f, 0.10f)
        builder.addBox(0.10f, 0.07f, 0f, 0.04f, 0.16f, 0.04f, 0.98f, 0.75f, 0.10f)
        chickenMesh = builder.build()

        // 5. DEEP FOREST DEER
        builder.reset()
        // Slender Tan Body
        builder.addBox(0f, 1.15f, 0f, 0.55f, 0.60f, 1.35f, 0.72f, 0.52f, 0.34f)
        builder.addBox(0f, 1.25f, -0.70f, 0.14f, 0.20f, 0.16f, 0.95f, 0.95f, 0.90f) // White tail
        deerBodyMesh = builder.build()

        builder.reset()
        // Graceful Head & Snout
        builder.addBox(0f, 0.25f, 0.30f, 0.32f, 0.34f, 0.42f, 0.74f, 0.54f, 0.36f)
        builder.addBox(0f, 0.12f, 0.55f, 0.20f, 0.18f, 0.20f, 0.32f, 0.22f, 0.16f) // Snout
        // Antlers
        builder.addCylinder(-0.16f, 0.52f, 0.22f, 0.04f, 0.50f, 4, 0.88f, 0.82f, 0.70f)
        builder.addCylinder(0.16f, 0.52f, 0.22f, 0.04f, 0.50f, 4, 0.88f, 0.82f, 0.70f)
        builder.addCylinder(-0.25f, 0.75f, 0.28f, 0.03f, 0.30f, 4, 0.88f, 0.82f, 0.70f)
        builder.addCylinder(0.25f, 0.75f, 0.28f, 0.03f, 0.30f, 4, 0.88f, 0.82f, 0.70f)
        deerHeadMesh = builder.build()

        builder.reset()
        // Long Slender Legs
        builder.addBox(0f, -0.40f, 0f, 0.10f, 0.80f, 0.10f, 0.68f, 0.48f, 0.30f)
        builder.addBox(0f, -0.78f, 0f, 0.12f, 0.08f, 0.12f, 0.22f, 0.18f, 0.14f) // Hoof
        deerLegMesh = builder.build()

        // 6. RABBIT
        builder.reset()
        // Compact hopping body
        builder.addBox(0f, 0.20f, 0f, 0.26f, 0.26f, 0.38f, 0.82f, 0.75f, 0.68f)
        builder.addBox(0f, 0.35f, 0.16f, 0.20f, 0.20f, 0.20f, 0.85f, 0.78f, 0.70f) // Head
        // Long Ears
        builder.addBox(-0.06f, 0.52f, 0.12f, 0.05f, 0.20f, 0.04f, 0.92f, 0.78f, 0.78f)
        builder.addBox(0.06f, 0.52f, 0.12f, 0.05f, 0.20f, 0.04f, 0.92f, 0.78f, 0.78f)
        // Fluffy tail
        builder.addSphere(0f, 0.24f, -0.22f, 0.08f, 4, 5, 0.98f, 0.98f, 0.98f)
        rabbitMesh = builder.build()

        // 7. OWL
        builder.reset()
        builder.addBox(0f, 0.38f, 0f, 0.30f, 0.45f, 0.28f, 0.45f, 0.35f, 0.25f)
        builder.addBox(0f, 0.66f, 0f, 0.28f, 0.24f, 0.26f, 0.48f, 0.38f, 0.28f) // Head
        builder.addSphere(-0.08f, 0.68f, 0.14f, 0.05f, 4, 5, 1.0f, 0.92f, 0.10f) // Glowing Yellow Eyes
        builder.addSphere(0.08f, 0.68f, 0.14f, 0.05f, 4, 5, 1.0f, 0.92f, 0.10f)
        builder.addCone(0f, 0.62f, 0.16f, 0.04f, 0.08f, 4, 0.30f, 0.22f, 0.15f) // Beak
        owlMesh = builder.build()

        // 8. FOX / WOLF
        builder.reset()
        builder.addBox(0f, 0.52f, 0f, 0.42f, 0.42f, 0.95f, 0.88f, 0.42f, 0.18f) // Red-orange fox / gray wolf coat
        builder.addBox(0f, 0.68f, 0.44f, 0.32f, 0.30f, 0.32f, 0.92f, 0.46f, 0.20f) // Head
        builder.addCone(0f, 0.62f, 0.68f, 0.08f, 0.24f, 4, 0.25f, 0.20f, 0.18f) // Snout
        builder.addCone(-0.12f, 0.88f, 0.40f, 0.06f, 0.16f, 4, 0.90f, 0.40f, 0.18f) // Ears
        builder.addCone(0.12f, 0.88f, 0.40f, 0.06f, 0.16f, 4, 0.90f, 0.40f, 0.18f)
        // Bushy Tail
        builder.addBox(0f, 0.62f, -0.65f, 0.18f, 0.18f, 0.55f, 0.95f, 0.50f, 0.20f)
        builder.addBox(0f, 0.62f, -0.92f, 0.14f, 0.14f, 0.18f, 0.98f, 0.98f, 0.95f) // White tail tip
        foxWolfMesh = builder.build()

        // 9. DUCK (Wetland Swimming Duck)
        builder.reset()
        builder.addBox(0f, 0.18f, 0f, 0.28f, 0.26f, 0.48f, 0.22f, 0.48f, 0.32f) // Mallard body
        builder.addBox(0f, 0.36f, 0.18f, 0.18f, 0.20f, 0.18f, 0.12f, 0.55f, 0.38f) // Iridescent Green Head
        builder.addBox(0f, 0.32f, 0.32f, 0.12f, 0.05f, 0.16f, 0.98f, 0.82f, 0.12f) // Yellow Bill
        duckMesh = builder.build()

        // 10. FROG (Wetland Frog)
        builder.reset()
        builder.addBox(0f, 0.10f, 0f, 0.20f, 0.14f, 0.26f, 0.25f, 0.72f, 0.22f)
        builder.addSphere(-0.07f, 0.18f, 0.08f, 0.04f, 4, 4, 0.18f, 0.82f, 0.18f) // Eyes
        builder.addSphere(0.07f, 0.18f, 0.08f, 0.04f, 4, 4, 0.18f, 0.82f, 0.18f)
        frogMesh = builder.build()

        // 11. HERON (Wetland Tall Crane/Heron)
        builder.reset()
        // Slender Body
        builder.addBox(0f, 0.95f, 0f, 0.24f, 0.32f, 0.55f, 0.88f, 0.92f, 0.96f)
        // S-curved Long Neck & Head
        builder.addCylinder(0f, 1.25f, 0.22f, 0.05f, 0.60f, 4, 0.88f, 0.92f, 0.96f)
        builder.addBox(0f, 1.62f, 0.26f, 0.14f, 0.14f, 0.22f, 0.88f, 0.92f, 0.96f)
        builder.addCone(0f, 1.62f, 0.44f, 0.04f, 0.32f, 4, 0.98f, 0.78f, 0.18f) // Long Yellow Beak
        // Tall Stilt Legs
        builder.addCylinder(-0.08f, 0.35f, 0f, 0.03f, 0.70f, 4, 0.35f, 0.30f, 0.25f)
        builder.addCylinder(0.08f, 0.35f, 0f, 0.03f, 0.70f, 4, 0.35f, 0.30f, 0.25f)
        heronMesh = builder.build()

        // 12. SWIMMING FISH
        builder.reset()
        builder.addBox(0f, 0f, 0f, 0.08f, 0.16f, 0.42f, 0.65f, 0.85f, 0.95f) // Silvery Blue Body
        builder.addBox(0f, 0f, -0.26f, 0.02f, 0.20f, 0.14f, 0.95f, 0.75f, 0.35f) // Caudal Fin
        fishMesh = builder.build()

        // 13. DROP SHADOW DISK
        builder.reset()
        builder.addCylinder(0f, 0.01f, 0f, 0.80f, 0.01f, 10, 0.06f, 0.12f, 0.08f, 0.50f)
        shadowMesh = builder.build()
    }

    fun drawLivestock(
        shader: GLShader,
        viewMatrix: FloatArray,
        projMatrix: FloatArray,
        livestock: List<LivestockEntity>,
        animTime: Float
    ) {
        for (animal in livestock) {
            val isWalking = animal.hunger > 30f || (animTime % 8f > 4f)
            val legSwing = if (isWalking) sin(animTime * 6f + animal.id) * 22f else 0.0f
            val headBob = sin(animTime * 2.5f + animal.id) * 4f

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, animal.posX, animal.posY, animal.posZ)

            val dx = animal.targetX - animal.posX
            val dz = animal.targetZ - animal.posZ
            val angleDeg = if (dx * dx + dz * dz > 0.01f) {
                Math.toDegrees(atan2(dx.toDouble(), dz.toDouble())).toFloat()
            } else 0.0f
            Matrix.rotateM(modelMatrix, 0, angleDeg, 0f, 1f, 0f)

            // Draw Animal Drop Shadow
            renderMesh(shader, shadowMesh, modelMatrix, viewMatrix, projMatrix)

            when (animal.type) {
                LivestockType.CYBER_BOVINE -> {
                    renderMesh(shader, cowBodyMesh, modelMatrix, viewMatrix, projMatrix)

                    Matrix.setIdentityM(partMatrix, 0)
                    Matrix.multiplyMM(partMatrix, 0, modelMatrix, 0, partMatrix, 0)
                    Matrix.translateM(partMatrix, 0, 0f, 1.05f, 0.75f)
                    Matrix.rotateM(partMatrix, 0, headBob, 1f, 0f, 0f)
                    renderMesh(shader, cowHeadMesh, partMatrix, viewMatrix, projMatrix)

                    drawLeg(shader, cowLegMesh, modelMatrix, viewMatrix, projMatrix, -0.36f, 0.60f, 0.55f, legSwing)
                    drawLeg(shader, cowLegMesh, modelMatrix, viewMatrix, projMatrix, 0.36f, 0.60f, 0.55f, -legSwing)
                    drawLeg(shader, cowLegMesh, modelMatrix, viewMatrix, projMatrix, -0.36f, 0.60f, -0.55f, -legSwing)
                    drawLeg(shader, cowLegMesh, modelMatrix, viewMatrix, projMatrix, 0.36f, 0.60f, -0.55f, legSwing)
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

        // Draw Biome Wildlife (Deer, Rabbits, Owls, Wolves, Ducks, Frogs, Herons, Fish)
        drawBiomeWildlife(shader, viewMatrix, projMatrix, animTime)
    }

    private fun drawBiomeWildlife(
        shader: GLShader,
        viewMatrix: FloatArray,
        projMatrix: FloatArray,
        animTime: Float
    ) {
        // A. Deep Forest Deer (3)
        val deerPositions = listOf(
            Triple(8.0f, 65.0f, 45f),
            Triple(-22.0f, 95.0f, -30f),
            Triple(28.0f, 130.0f, 110f)
        )
        for ((idx, d) in deerPositions.withIndex()) {
            val grazeBob = sin(animTime * 1.8f + idx * 2f) * 6f
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, d.first, 0f, d.second)
            Matrix.rotateM(modelMatrix, 0, d.third, 0f, 1f, 0f)
            renderMesh(shader, shadowMesh, modelMatrix, viewMatrix, projMatrix)
            renderMesh(shader, deerBodyMesh, modelMatrix, viewMatrix, projMatrix)

            Matrix.setIdentityM(partMatrix, 0)
            Matrix.multiplyMM(partMatrix, 0, modelMatrix, 0, partMatrix, 0)
            Matrix.translateM(partMatrix, 0, 0f, 1.2f, 0.55f)
            Matrix.rotateM(partMatrix, 0, grazeBob, 1f, 0f, 0f)
            renderMesh(shader, deerHeadMesh, partMatrix, viewMatrix, projMatrix)

            drawLeg(shader, deerLegMesh, modelMatrix, viewMatrix, projMatrix, -0.22f, 0.75f, 0.45f, 0f)
            drawLeg(shader, deerLegMesh, modelMatrix, viewMatrix, projMatrix, 0.22f, 0.75f, 0.45f, 0f)
            drawLeg(shader, deerLegMesh, modelMatrix, viewMatrix, projMatrix, -0.22f, 0.75f, -0.45f, 0f)
            drawLeg(shader, deerLegMesh, modelMatrix, viewMatrix, projMatrix, 0.22f, 0.75f, -0.45f, 0f)
        }

        // B. Deep Forest Rabbits (5)
        val rabbitPositions = listOf(
            Pair(-10.0f, 55.0f), Pair(18.0f, 75.0f), Pair(-25.0f, 115.0f), Pair(5.0f, 140.0f), Pair(-15.0f, 160.0f)
        )
        for ((idx, r) in rabbitPositions.withIndex()) {
            val hop = kotlin.math.abs(sin(animTime * 4f + idx * 1.5f)) * 0.12f
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, r.first, hop, r.second)
            renderMesh(shader, rabbitMesh, modelMatrix, viewMatrix, projMatrix)
        }

        // C. Deep Forest Owls (2, on perches)
        val owlPositions = listOf(
            Triple(14.0f, 8.5f, 110.0f), // On Ranger Tower
            Triple(-12.0f, 4.2f, 148.0f)  // On Stone Circle
        )
        for (owl in owlPositions) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, owl.first, owl.second, owl.third)
            renderMesh(shader, owlMesh, modelMatrix, viewMatrix, projMatrix)
        }

        // D. Forest Wolves / Fox
        val foxPos = Pair(-5.0f, 48.0f)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, foxPos.first, 0f, foxPos.second)
        Matrix.rotateM(modelMatrix, 0, 25f, 0f, 1f, 0f)
        renderMesh(shader, foxWolfMesh, modelMatrix, viewMatrix, projMatrix)

        // E. Wetland Swimming Ducks (4)
        val duckPositions = listOf(
            Pair(-8.0f, -98.0f), Pair(12.0f, -102.0f), Pair(28.0f, -95.0f), Pair(-30.0f, -125.0f)
        )
        for ((idx, duck) in duckPositions.withIndex()) {
            val driftX = sin(animTime * 0.5f + idx) * 1.2f
            val bob = sin(animTime * 2.5f + idx) * 0.02f
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, duck.first + driftX, 0.05f + bob, duck.second)
            renderMesh(shader, duckMesh, modelMatrix, viewMatrix, projMatrix)
        }

        // F. Wetland Frogs on Lily Pads (8)
        val frogPositions = listOf(
            Pair(10.0f, -10.0f), Pair(16.0f, -92.0f), Pair(-18.0f, -96.0f), Pair(22.0f, -104.0f),
            Pair(35.0f, -130.0f), Pair(-12.0f, -80.0f), Pair(5.0f, -115.0f), Pair(-28.0f, -105.0f)
        )
        for ((idx, frog) in frogPositions.withIndex()) {
            val jump = kotlin.math.abs(sin(animTime * 2f + idx)) * 0.15f
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, frog.first, 0.06f + jump, frog.second)
            renderMesh(shader, frogMesh, modelMatrix, viewMatrix, projMatrix)
        }

        // G. Wetland Herons (2 in Shallows)
        val heronPositions = listOf(
            Pair(-15.0f, -94.0f), Pair(22.0f, -98.0f)
        )
        for (heron in heronPositions) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, heron.first, 0f, heron.second)
            renderMesh(shader, heronMesh, modelMatrix, viewMatrix, projMatrix)
        }

        // H. Swimming Fish in Wetland River (10)
        for (i in 0 until 10) {
            val fx = -45.0f + i * 10.0f + sin(animTime * 0.8f + i) * 3.0f
            val fz = -98.0f + cos(animTime * 0.6f + i * 2f) * 4.0f
            val swimWiggle = sin(animTime * 8f + i) * 15f
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, fx, 0.02f, fz)
            Matrix.rotateM(modelMatrix, 0, swimWiggle + 90f, 0f, 1f, 0f)
            renderMesh(shader, fishMesh, modelMatrix, viewMatrix, projMatrix)
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
