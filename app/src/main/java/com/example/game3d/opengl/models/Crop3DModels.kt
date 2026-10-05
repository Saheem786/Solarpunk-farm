package com.example.game3d.opengl.models

import android.opengl.GLES20
import android.opengl.Matrix
import com.example.data.local.PlotEntity
import com.example.data.model.CropStage
import com.example.data.model.CropType
import com.example.game3d.opengl.GLMesh
import com.example.game3d.opengl.GLModelBuilder
import com.example.game3d.opengl.GLShader
import kotlin.math.sin

class Crop3DModels {

    val soilBedMesh: GLMesh
    val soilWateredMesh: GLMesh

    // Crop Type Meshes
    val sproutMesh: GLMesh
    val wheatMesh: GLMesh
    val sunflowerMesh: GLMesh
    val cornMesh: GLMesh
    val berryMesh: GLMesh
    val mushroomMesh: GLMesh
    val harvestRingMesh: GLMesh

    private val modelMatrix = FloatArray(16)
    private val mvMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val normalMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)
    private val plantMatrix = FloatArray(16)

    init {
        val builder = GLModelBuilder()

        // 1. Raised Permaculture Soil Bed (2.6m x 2.6m x 0.28m high)
        builder.reset()
        // Wooden frame borders
        builder.addBox(0f, 0.14f, -1.35f, 2.90f, 0.28f, 0.18f, 0.52f, 0.36f, 0.20f)
        builder.addBox(0f, 0.14f, 1.35f, 2.90f, 0.28f, 0.18f, 0.52f, 0.36f, 0.20f)
        builder.addBox(-1.35f, 0.14f, 0f, 0.18f, 0.28f, 2.70f, 0.52f, 0.36f, 0.20f)
        builder.addBox(1.35f, 0.14f, 0f, 0.18f, 0.28f, 2.70f, 0.52f, 0.36f, 0.20f)
        // Rich Brown Loam Soil
        builder.addBox(0f, 0.12f, 0f, 2.55f, 0.24f, 2.55f, 0.40f, 0.26f, 0.14f)
        soilBedMesh = builder.build()

        // Watered Soil Bed (Darker Moist Soil)
        builder.reset()
        builder.addBox(0f, 0.14f, -1.35f, 2.90f, 0.28f, 0.18f, 0.52f, 0.36f, 0.20f)
        builder.addBox(0f, 0.14f, 1.35f, 2.90f, 0.28f, 0.18f, 0.52f, 0.36f, 0.20f)
        builder.addBox(-1.35f, 0.14f, 0f, 0.18f, 0.28f, 2.70f, 0.52f, 0.36f, 0.20f)
        builder.addBox(1.35f, 0.14f, 0f, 0.18f, 0.28f, 2.70f, 0.52f, 0.36f, 0.20f)
        builder.addBox(0f, 0.12f, 0f, 2.55f, 0.24f, 2.55f, 0.22f, 0.15f, 0.08f)
        soilWateredMesh = builder.build()

        // 2. Early Sprout / Seedling (Tiny Green Shoots)
        builder.reset()
        builder.addCone(-0.4f, 0f, -0.4f, 0.12f, 0.35f, 5, 0.35f, 0.85f, 0.20f)
        builder.addCone(0.4f, 0f, -0.4f, 0.12f, 0.35f, 5, 0.35f, 0.85f, 0.20f)
        builder.addCone(-0.4f, 0f, 0.4f, 0.12f, 0.35f, 5, 0.35f, 0.85f, 0.20f)
        builder.addCone(0.4f, 0f, 0.4f, 0.12f, 0.35f, 5, 0.35f, 0.85f, 0.20f)
        sproutMesh = builder.build()

        // 3. Wheat (Terraced Wheat) - Multi-stalk golden sheaf (Height = 1.3m)
        builder.reset()
        for (gx in listOf(-0.5f, 0f, 0.5f)) {
            for (gz in listOf(-0.5f, 0f, 0.5f)) {
                // Stem
                builder.addCylinder(gx, 0f, gz, 0.04f, 1.05f, 4, 0.85f, 0.72f, 0.24f)
                // Golden Grain Head
                builder.addBox(gx, 1.15f, gz, 0.14f, 0.35f, 0.14f, 0.96f, 0.84f, 0.28f)
            }
        }
        wheatMesh = builder.build()

        // 4. Sunflower (Solar Sunflower) - Radiant Yellow Discs (Height = 1.6m)
        builder.reset()
        for (gx in listOf(-0.45f, 0.45f)) {
            for (gz in listOf(-0.45f, 0.45f)) {
                // Sturdy Green Stem
                builder.addCylinder(gx, 0f, gz, 0.06f, 1.40f, 5, 0.20f, 0.65f, 0.15f)
                // Leaves
                builder.addBox(gx + 0.15f, 0.70f, gz, 0.30f, 0.08f, 0.18f, 0.25f, 0.75f, 0.20f)
                builder.addBox(gx - 0.15f, 0.95f, gz, 0.30f, 0.08f, 0.18f, 0.25f, 0.75f, 0.20f)
                // Golden Flower Disc
                builder.addCylinder(gx, 1.45f, gz + 0.08f, 0.38f, 0.08f, 10, 0.98f, 0.82f, 0.10f)
                // Seed Center (Dark Brown)
                builder.addCylinder(gx, 1.47f, gz + 0.08f, 0.20f, 0.09f, 8, 0.40f, 0.22f, 0.10f)
            }
        }
        sunflowerMesh = builder.build()

        // 5. Corn (Solar Corn) - Tall stalks with leaves & cobs (Height = 1.8m)
        builder.reset()
        for (gx in listOf(-0.5f, 0f, 0.5f)) {
            for (gz in listOf(-0.5f, 0.5f)) {
                builder.addCylinder(gx, 0f, gz, 0.06f, 1.75f, 5, 0.22f, 0.68f, 0.18f)
                // Wide leaves
                builder.addBox(gx, 0.80f, gz, 0.65f, 0.06f, 0.16f, 0.28f, 0.76f, 0.22f)
                builder.addBox(gx, 1.25f, gz, 0.55f, 0.06f, 0.16f, 0.28f, 0.76f, 0.22f)
                // Yellow Corn Cob
                builder.addCone(gx + 0.12f, 0.90f, gz, 0.09f, 0.32f, 6, 0.98f, 0.86f, 0.15f)
            }
        }
        cornMesh = builder.build()

        // 6. Berry / Tomato Vine Lattice
        builder.reset()
        // Wooden stake
        builder.addCylinder(0f, 0f, 0f, 0.05f, 1.50f, 5, 0.50f, 0.35f, 0.20f)
        // Green Foliage Bush
        builder.addSphere(0f, 0.85f, 0f, 0.55f, 6, 8, 0.18f, 0.65f, 0.20f)
        // Red Berries / Tomatoes
        builder.addSphere(-0.35f, 0.85f, 0.25f, 0.12f, 4, 6, 0.95f, 0.18f, 0.12f)
        builder.addSphere(0.35f, 0.75f, -0.25f, 0.12f, 4, 6, 0.95f, 0.18f, 0.12f)
        builder.addSphere(0.15f, 1.15f, 0.30f, 0.12f, 4, 6, 0.95f, 0.18f, 0.12f)
        berryMesh = builder.build()

        // 7. Bioluminescent Mushroom / Herb (Glowing Cyan & Violet)
        builder.reset()
        for (gx in listOf(-0.35f, 0.35f)) {
            for (gz in listOf(-0.35f, 0.35f)) {
                // Stalk (Pale Cyan)
                builder.addCylinder(gx, 0f, gz, 0.08f, 0.60f, 6, 0.45f, 0.90f, 0.95f)
                // Mushroom Cap (Glowing Vibrant Cyan)
                builder.addSphere(gx, 0.68f, gz, 0.28f, 6, 8, 0.00f, 0.95f, 0.85f)
            }
        }
        mushroomMesh = builder.build()

        // 8. Harvest-Ready Floating Glow Ring
        builder.reset()
        builder.addCylinder(0f, 1.65f, 0f, 0.85f, 0.06f, 10, 1.00f, 0.88f, 0.20f, 0.75f)
        harvestRingMesh = builder.build()
    }

    fun drawPlots(
        shader: GLShader,
        viewMatrix: FloatArray,
        projMatrix: FloatArray,
        plots: List<PlotEntity>,
        animTime: Float
    ) {
        for (plot in plots) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, plot.posX, plot.posY, plot.posZ)

            // 1. Draw Raised Soil Bed
            val bed = if (plot.moisture > 0.4f) soilWateredMesh else soilBedMesh
            renderMesh(shader, bed, modelMatrix, viewMatrix, projMatrix)

            // 2. Draw Crop Plant if seeded
            if (plot.cropType != null && plot.stage != CropStage.EMPTY) {
                val scale = when (plot.stage) {
                    CropStage.SEEDLING -> 0.45f
                    CropStage.SPROUT -> 0.65f
                    CropStage.VEGETATIVE -> 0.85f
                    CropStage.FLOWERING -> 1.0f
                    CropStage.HARVEST_READY -> 1.05f
                    else -> 0.5f
                }

                // Wind sway
                val sway = sin(animTime * 2.5f + plot.posX) * 3.5f

                Matrix.setIdentityM(plantMatrix, 0)
                Matrix.multiplyMM(plantMatrix, 0, modelMatrix, 0, plantMatrix, 0)
                Matrix.translateM(plantMatrix, 0, 0f, 0.24f, 0f)
                Matrix.scaleM(plantMatrix, 0, scale, scale, scale)
                Matrix.rotateM(plantMatrix, 0, sway, 1f, 0f, 0f)

                val cropMesh = when (plot.cropType) {
                    CropType.TERRACED_WHEAT -> wheatMesh
                    CropType.SOLAR_SUNFLOWER -> sunflowerMesh
                    CropType.SOLAR_CORN -> cornMesh
                    CropType.CYBER_BERRIES -> berryMesh
                    CropType.BIOLUMINESCENT_MUSHROOM -> mushroomMesh
                    else -> wheatMesh
                }

                val isBiolum = plot.cropType == CropType.BIOLUMINESCENT_MUSHROOM
                renderCropMesh(shader, cropMesh, plantMatrix, viewMatrix, projMatrix, isBiolum)

                // 3. Harvest Ready Floating Sheen
                if (plot.stage == CropStage.HARVEST_READY) {
                    val bobY = sin(animTime * 3.0f + plot.id) * 0.08f
                    Matrix.setIdentityM(plantMatrix, 0)
                    Matrix.multiplyMM(plantMatrix, 0, modelMatrix, 0, plantMatrix, 0)
                    Matrix.translateM(plantMatrix, 0, 0f, bobY, 0f)
                    renderCropMesh(shader, harvestRingMesh, plantMatrix, viewMatrix, projMatrix, true)
                }
            }
        }
    }

    private fun renderCropMesh(
        shader: GLShader,
        mesh: GLMesh,
        mMatrix: FloatArray,
        vMatrix: FloatArray,
        pMatrix: FloatArray,
        isGlowing: Boolean
    ) {
        Matrix.multiplyMM(mvMatrix, 0, vMatrix, 0, mMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, pMatrix, 0, mvMatrix, 0)

        Matrix.invertM(tempMatrix, 0, mvMatrix, 0)
        Matrix.transposeM(normalMatrix, 0, tempMatrix, 0)

        GLES20.glUniformMatrix4fv(shader.uMVPMatrixLoc, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(shader.uMVMatrixLoc, 1, false, mvMatrix, 0)
        GLES20.glUniformMatrix4fv(shader.uNormalMatrixLoc, 1, false, normalMatrix, 0)

        if (isGlowing) {
            GLES20.glUniform3f(shader.uEmissionColorLoc, 0.05f, 0.45f, 0.35f)
        } else {
            GLES20.glUniform3f(shader.uEmissionColorLoc, 0f, 0f, 0f)
        }
        GLES20.glUniform1f(shader.uShininessLoc, 4.0f)

        mesh.draw(shader)
    }

    private fun renderMesh(
        shader: GLShader,
        mesh: GLMesh,
        mMatrix: FloatArray,
        vMatrix: FloatArray,
        pMatrix: FloatArray
    ) {
        renderCropMesh(shader, mesh, mMatrix, vMatrix, pMatrix, false)
    }
}
