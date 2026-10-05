package com.example.game3d.opengl.models

import android.opengl.GLES20
import android.opengl.Matrix
import com.example.data.local.EnergyNodeEntity
import com.example.data.local.PlacedBuildingEntity
import com.example.data.model.BuildableType
import com.example.data.model.EnergyNodeType
import com.example.game3d.opengl.GLMesh
import com.example.game3d.opengl.GLModelBuilder
import com.example.game3d.opengl.GLShader
import kotlin.math.sin

class Building3DModels {

    val farmhouseMesh: GLMesh
    val barnMesh: GLMesh
    val workshopMesh: GLMesh
    val greenhouseMesh: GLMesh
    val turbineBladeMesh: GLMesh
    val marketStallMesh: GLMesh
    val solarArrayMesh: GLMesh
    val batteryHubMesh: GLMesh
    val fenceSegmentMesh: GLMesh

    // Building Shadow Meshes (Soft Dark Charcoal Ground Contacts)
    val shadowLargeMesh: GLMesh
    val shadowMediumMesh: GLMesh
    val shadowSmallMesh: GLMesh

    private val modelMatrix = FloatArray(16)
    private val mvMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val normalMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)
    private val partMatrix = FloatArray(16)

    init {
        val builder = GLModelBuilder()

        // 0. Building Ground Shadow Meshes
        builder.reset()
        builder.addBox(0f, 0.015f, 0f, 8.4f, 0.01f, 6.4f, 0.05f, 0.10f, 0.08f, 0.55f)
        shadowLargeMesh = builder.build()

        builder.reset()
        builder.addBox(0f, 0.015f, 0f, 5.6f, 0.01f, 4.8f, 0.05f, 0.10f, 0.08f, 0.55f)
        shadowMediumMesh = builder.build()

        builder.reset()
        builder.addBox(0f, 0.015f, 0f, 3.2f, 0.01f, 2.6f, 0.05f, 0.10f, 0.08f, 0.55f)
        shadowSmallMesh = builder.build()

        // 1. FARM HOUSE / CABIN
        builder.reset()
        // Stone Foundation
        builder.addBox(0f, 0.20f, 0f, 7.4f, 0.40f, 5.2f, 0.48f, 0.48f, 0.50f)
        // Timber/Clapboard Walls
        builder.addBox(0f, 1.80f, 0f, 7.0f, 2.80f, 4.8f, 0.92f, 0.88f, 0.78f)
        // Pitched Roof
        builder.addRoofPrism(0f, 3.20f, 0f, 7.6f, 2.20f, 5.4f, 0.82f, 0.38f, 0.22f)
        // Chimney
        builder.addBox(2.2f, 4.50f, -0.8f, 0.70f, 1.80f, 0.70f, 0.65f, 0.28f, 0.22f)
        // Front Door & Windows
        builder.addBox(0f, 1.25f, 2.42f, 1.10f, 2.10f, 0.08f, 0.42f, 0.26f, 0.16f)
        builder.addBox(-1.8f, 1.70f, 2.42f, 1.20f, 1.20f, 0.06f, 0.98f, 0.92f, 0.50f)
        builder.addBox(1.8f, 1.70f, 2.42f, 1.20f, 1.20f, 0.06f, 0.98f, 0.92f, 0.50f)
        // Covered Porch
        builder.addBox(0f, 0.18f, 3.30f, 5.4f, 0.36f, 1.8f, 0.55f, 0.38f, 0.24f)
        builder.addCylinder(-2.4f, 0.36f, 4.0f, 0.12f, 2.30f, 6, 0.88f, 0.85f, 0.78f)
        builder.addCylinder(2.4f, 0.36f, 4.0f, 0.12f, 2.30f, 6, 0.88f, 0.85f, 0.78f)
        builder.addRoofPrism(0f, 2.60f, 3.30f, 5.6f, 0.90f, 2.0f, 0.82f, 0.38f, 0.22f)
        farmhouseMesh = builder.build()

        // 2. RUSTIC BARN
        builder.reset()
        builder.addBox(0f, 1.80f, 0f, 9.2f, 3.60f, 7.2f, 0.78f, 0.20f, 0.18f)
        builder.addBox(-4.55f, 1.80f, -3.55f, 0.30f, 3.65f, 0.30f, 0.95f, 0.95f, 0.95f)
        builder.addBox(4.55f, 1.80f, -3.55f, 0.30f, 3.65f, 0.30f, 0.95f, 0.95f, 0.95f)
        builder.addBox(-4.55f, 1.80f, 3.55f, 0.30f, 3.65f, 0.30f, 0.95f, 0.95f, 0.95f)
        builder.addBox(4.55f, 1.80f, 3.55f, 0.30f, 3.65f, 0.30f, 0.95f, 0.95f, 0.95f)
        builder.addRoofPrism(0f, 3.60f, 0f, 9.8f, 3.20f, 7.6f, 0.28f, 0.30f, 0.34f)
        builder.addBox(0f, 1.50f, 3.62f, 3.2f, 3.00f, 0.10f, 0.95f, 0.95f, 0.95f)
        barnMesh = builder.build()

        // 3. ARTISAN ECO-WORKSHOP
        builder.reset()
        builder.addBox(0f, 1.60f, 0f, 6.6f, 3.20f, 5.4f, 0.88f, 0.92f, 0.88f)
        builder.addRoofPrism(0f, 3.20f, 0f, 7.0f, 1.60f, 5.8f, 0.10f, 0.65f, 0.85f)
        builder.addBox(0f, 1.30f, -2.72f, 3.0f, 2.60f, 0.08f, 0.40f, 0.50f, 0.55f)
        workshopMesh = builder.build()

        // 4. GREENHOUSE / BIO-DOME
        builder.reset()
        // Low concrete wall base
        builder.addBox(0f, 0.25f, 0f, 5.2f, 0.50f, 4.4f, 0.45f, 0.50f, 0.52f)
        // Geodesic Glass Arches / Dome (Translucent Cyan: 0.15, 0.80, 0.90)
        builder.addBox(0f, 1.80f, 0f, 4.8f, 2.60f, 4.0f, 0.15f, 0.80f, 0.90f)
        // Metal Structural Frame Trusses (Emerald: 0.0, 0.85, 0.45)
        builder.addBox(-2.38f, 1.80f, 0f, 0.12f, 2.65f, 4.02f, 0.00f, 0.85f, 0.45f)
        builder.addBox(2.38f, 1.80f, 0f, 0.12f, 2.65f, 4.02f, 0.00f, 0.85f, 0.45f)
        builder.addBox(0f, 1.80f, -1.98f, 4.82f, 2.65f, 0.12f, 0.00f, 0.85f, 0.45f)
        builder.addBox(0f, 1.80f, 1.98f, 4.82f, 2.65f, 0.12f, 0.00f, 0.85f, 0.45f)
        // Slanted Glass Roof Cap
        builder.addRoofPrism(0f, 3.10f, 0f, 5.0f, 1.20f, 4.2f, 0.20f, 0.88f, 0.95f)
        // Interior Hydroponic Planters
        builder.addBox(-1.2f, 0.60f, 0f, 1.2f, 0.40f, 2.8f, 0.22f, 0.75f, 0.25f)
        builder.addBox(1.2f, 0.60f, 0f, 1.2f, 0.40f, 2.8f, 0.22f, 0.75f, 0.25f)
        greenhouseMesh = builder.build()

        // Turbine Blade (Length = 2.4m)
        builder.reset()
        builder.addBox(0f, 1.20f, 0f, 0.22f, 2.40f, 0.04f, 0.95f, 0.95f, 0.95f)
        turbineBladeMesh = builder.build()

        // 5. SOL CITY MARKET STALL
        builder.reset()
        builder.addBox(0f, 0.55f, 0f, 3.6f, 1.10f, 1.6f, 0.58f, 0.40f, 0.24f)
        builder.addCylinder(-1.6f, 0f, -0.7f, 0.08f, 2.60f, 6, 0.45f, 0.30f, 0.18f)
        builder.addCylinder(1.6f, 0f, -0.7f, 0.08f, 2.60f, 6, 0.45f, 0.30f, 0.18f)
        builder.addCylinder(-1.6f, 0f, 0.7f, 0.08f, 2.60f, 6, 0.45f, 0.30f, 0.18f)
        builder.addCylinder(1.6f, 0f, 0.7f, 0.08f, 2.60f, 6, 0.45f, 0.30f, 0.18f)
        builder.addRoofPrism(0f, 2.50f, 0f, 4.0f, 0.80f, 2.2f, 0.00f, 0.85f, 0.45f)
        marketStallMesh = builder.build()

        // 6. SOLAR PHOTOVOLTAIC ARRAY
        builder.reset()
        builder.addBox(0f, 0.60f, 0f, 0.20f, 1.20f, 0.20f, 0.55f, 0.58f, 0.62f)
        builder.addBox(-1.4f, 0.60f, 0f, 0.15f, 1.20f, 0.15f, 0.55f, 0.58f, 0.62f)
        builder.addBox(1.4f, 0.60f, 0f, 0.15f, 1.20f, 0.15f, 0.55f, 0.58f, 0.62f)
        builder.addBox(0f, 1.40f, 0f, 3.4f, 0.08f, 2.2f, 0.08f, 0.38f, 0.72f)
        builder.addBox(0f, 1.45f, 0f, 3.42f, 0.02f, 0.04f, 0.00f, 0.90f, 0.90f)
        solarArrayMesh = builder.build()

        // 7. BATTERY STORAGE BANK / HUB
        builder.reset()
        builder.addBox(0f, 0.90f, 0f, 1.8f, 1.80f, 1.2f, 0.85f, 0.90f, 0.88f)
        builder.addBox(0f, 1.10f, 0.61f, 1.2f, 0.15f, 0.02f, 0.00f, 0.95f, 0.45f)
        builder.addBox(0f, 0.80f, 0.61f, 1.2f, 0.15f, 0.02f, 0.00f, 0.95f, 0.45f)
        builder.addBox(0f, 0.50f, 0.61f, 1.2f, 0.15f, 0.02f, 0.00f, 0.95f, 0.45f)
        batteryHubMesh = builder.build()

        // 8. POST-AND-RAIL WOODEN FENCE
        builder.reset()
        builder.addCylinder(-1.45f, 0f, 0f, 0.09f, 1.15f, 6, 0.58f, 0.42f, 0.26f)
        builder.addCylinder(1.45f, 0f, 0f, 0.09f, 1.15f, 6, 0.58f, 0.42f, 0.26f)
        builder.addBox(0f, 0.85f, 0f, 2.90f, 0.12f, 0.08f, 0.68f, 0.48f, 0.30f)
        builder.addBox(0f, 0.45f, 0f, 2.90f, 0.12f, 0.08f, 0.68f, 0.48f, 0.30f)
        fenceSegmentMesh = builder.build()
    }

    fun drawBuildings(
        shader: GLShader,
        viewMatrix: FloatArray,
        projMatrix: FloatArray,
        energyNodes: List<EnergyNodeEntity>,
        placedBuildings: List<PlacedBuildingEntity>,
        animTime: Float
    ) {
        // 1. Draw Default Farmhouse at X = 6.0, Z = 0.0 with Ground Shadow
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 6.0f, 0.0f, 0.0f)
        renderMesh(shader, shadowLargeMesh, modelMatrix, viewMatrix, projMatrix)
        Matrix.rotateM(modelMatrix, 0, -90.0f, 0f, 1f, 0f)
        renderMesh(shader, farmhouseMesh, modelMatrix, viewMatrix, projMatrix)

        // 2. Draw Rustic Barn at X = -12.0, Z = 10.0 with Ground Shadow
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, -12.0f, 0.0f, 10.0f)
        renderMesh(shader, shadowLargeMesh, modelMatrix, viewMatrix, projMatrix)
        Matrix.rotateM(modelMatrix, 0, 0.0f, 0f, 1f, 0f)
        renderMesh(shader, barnMesh, modelMatrix, viewMatrix, projMatrix)

        // 3. Draw Artisan Eco-Workshop at X = 0.0, Z = 14.0 with Ground Shadow
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, 14.0f)
        renderMesh(shader, shadowLargeMesh, modelMatrix, viewMatrix, projMatrix)
        renderMesh(shader, workshopMesh, modelMatrix, viewMatrix, projMatrix)

        // 4. Draw Sol City Market Trading Stall at X = -14.0, Z = -14.0 with Ground Shadow
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, -14.0f, 0.0f, -14.0f)
        renderMesh(shader, shadowMediumMesh, modelMatrix, viewMatrix, projMatrix)
        Matrix.rotateM(modelMatrix, 0, 45.0f, 0f, 1f, 0f)
        renderMesh(shader, marketStallMesh, modelMatrix, viewMatrix, projMatrix)

        // 5. Draw Clean Energy Nodes with Shadows
        for (node in energyNodes) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, node.posX, node.posY, node.posZ)

            when (node.nodeType) {
                EnergyNodeType.PHOTOVOLTAIC_ARRAY -> {
                    renderMesh(shader, shadowMediumMesh, modelMatrix, viewMatrix, projMatrix)
                    renderMesh(shader, solarArrayMesh, modelMatrix, viewMatrix, projMatrix)
                }
                EnergyNodeType.BATTERY_STORAGE_BANK -> {
                    renderMesh(shader, shadowSmallMesh, modelMatrix, viewMatrix, projMatrix)
                    renderMesh(shader, batteryHubMesh, modelMatrix, viewMatrix, projMatrix)
                }
                EnergyNodeType.VERTICAL_WIND_TURBINE -> {
                    renderMesh(shader, shadowSmallMesh, modelMatrix, viewMatrix, projMatrix)
                    // Mast
                    Matrix.setIdentityM(partMatrix, 0)
                    Matrix.multiplyMM(partMatrix, 0, modelMatrix, 0, partMatrix, 0)
                    renderMesh(shader, batteryHubMesh, partMatrix, viewMatrix, projMatrix)

                    // 3 Spinning Blades
                    val spinAngle = (animTime * 180.0f) % 360.0f
                    for (i in 0..2) {
                        Matrix.setIdentityM(partMatrix, 0)
                        Matrix.multiplyMM(partMatrix, 0, modelMatrix, 0, partMatrix, 0)
                        Matrix.translateM(partMatrix, 0, 0f, 3.8f, 0f)
                        Matrix.rotateM(partMatrix, 0, spinAngle + i * 120.0f, 0f, 0f, 1f)
                        renderMesh(shader, turbineBladeMesh, partMatrix, viewMatrix, projMatrix)
                    }
                }
                else -> {
                    renderMesh(shader, shadowMediumMesh, modelMatrix, viewMatrix, projMatrix)
                    renderMesh(shader, solarArrayMesh, modelMatrix, viewMatrix, projMatrix)
                }
            }
        }

        // 6. Draw Player Placed Buildings with Shadows
        for (building in placedBuildings) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, building.posX, building.posY, building.posZ)

            when (building.buildingType) {
                BuildableType.CABIN -> {
                    renderMesh(shader, shadowLargeMesh, modelMatrix, viewMatrix, projMatrix)
                    Matrix.rotateM(modelMatrix, 0, building.rotationDeg, 0f, 1f, 0f)
                    renderMesh(shader, farmhouseMesh, modelMatrix, viewMatrix, projMatrix)
                }
                BuildableType.GREENHOUSE -> {
                    renderMesh(shader, shadowMediumMesh, modelMatrix, viewMatrix, projMatrix)
                    Matrix.rotateM(modelMatrix, 0, building.rotationDeg, 0f, 1f, 0f)
                    renderMesh(shader, greenhouseMesh, modelMatrix, viewMatrix, projMatrix)
                }
                BuildableType.SOLAR_PANEL -> {
                    renderMesh(shader, shadowMediumMesh, modelMatrix, viewMatrix, projMatrix)
                    Matrix.rotateM(modelMatrix, 0, building.rotationDeg, 0f, 1f, 0f)
                    renderMesh(shader, solarArrayMesh, modelMatrix, viewMatrix, projMatrix)
                }
                BuildableType.STORAGE -> {
                    renderMesh(shader, shadowSmallMesh, modelMatrix, viewMatrix, projMatrix)
                    Matrix.rotateM(modelMatrix, 0, building.rotationDeg, 0f, 1f, 0f)
                    renderMesh(shader, batteryHubMesh, modelMatrix, viewMatrix, projMatrix)
                }
                BuildableType.WINDMILL -> {
                    renderMesh(shader, shadowSmallMesh, modelMatrix, viewMatrix, projMatrix)
                    Matrix.rotateM(modelMatrix, 0, building.rotationDeg, 0f, 1f, 0f)
                    renderMesh(shader, batteryHubMesh, modelMatrix, viewMatrix, projMatrix)
                    val spinAngle = (animTime * 180.0f) % 360.0f
                    for (i in 0..2) {
                        Matrix.setIdentityM(partMatrix, 0)
                        Matrix.multiplyMM(partMatrix, 0, modelMatrix, 0, partMatrix, 0)
                        Matrix.translateM(partMatrix, 0, 0f, 3.8f, 0f)
                        Matrix.rotateM(partMatrix, 0, spinAngle + i * 120.0f, 0f, 0f, 1f)
                        renderMesh(shader, turbineBladeMesh, partMatrix, viewMatrix, projMatrix)
                    }
                }
            }
        }

        // 7. Draw Animal Pen Perimeter Fences
        drawFenceLine(shader, viewMatrix, projMatrix, -16f, 2f, -8f, 2f)
        drawFenceLine(shader, viewMatrix, projMatrix, -8f, 2f, -8f, 8f)
        drawFenceLine(shader, viewMatrix, projMatrix, -16f, 2f, -16f, 14f)
    }

    fun drawGhostPreview(
        shader: GLShader,
        viewMatrix: FloatArray,
        projMatrix: FloatArray,
        buildingType: BuildableType,
        posX: Float,
        posY: Float,
        posZ: Float,
        rotationDeg: Float,
        animTime: Float,
        canAfford: Boolean
    ) {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, posX, posY, posZ)
        Matrix.rotateM(modelMatrix, 0, rotationDeg, 0f, 1f, 0f)

        val pulse = sin(animTime * 4.0f) * 0.04f + 0.98f
        Matrix.scaleM(modelMatrix, 0, pulse, pulse, pulse)

        val mesh = when (buildingType) {
            BuildableType.CABIN -> farmhouseMesh
            BuildableType.GREENHOUSE -> greenhouseMesh
            BuildableType.SOLAR_PANEL -> solarArrayMesh
            BuildableType.STORAGE -> batteryHubMesh
            BuildableType.WINDMILL -> batteryHubMesh
        }

        val eR = if (canAfford) 0.0f else 0.85f
        val eG = if (canAfford) 0.85f else 0.15f
        val eB = if (canAfford) 0.70f else 0.10f

        renderGhostMesh(shader, mesh, modelMatrix, viewMatrix, projMatrix, eR, eG, eB)
    }

    private fun drawFenceLine(
        shader: GLShader,
        viewMatrix: FloatArray,
        projMatrix: FloatArray,
        startX: Float,
        startZ: Float,
        endX: Float,
        endZ: Float
    ) {
        val dx = endX - startX
        val dz = endZ - startZ
        val len = kotlin.math.sqrt(dx * dx + dz * dz)
        val segments = kotlin.math.max(1, (len / 3.0f).toInt())
        val angle = Math.toDegrees(kotlin.math.atan2(dx.toDouble(), dz.toDouble())).toFloat()

        for (i in 0 until segments) {
            val t = (i + 0.5f) / segments
            val x = startX + dx * t
            val z = startZ + dz * t

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, x, 0f, z)
            Matrix.rotateM(modelMatrix, 0, angle + 90.0f, 0f, 1f, 0f)
            renderMesh(shader, fenceSegmentMesh, modelMatrix, viewMatrix, projMatrix)
        }
    }

    private fun renderGhostMesh(
        shader: GLShader,
        mesh: GLMesh,
        mMatrix: FloatArray,
        vMatrix: FloatArray,
        pMatrix: FloatArray,
        emissionR: Float,
        emissionG: Float,
        emissionB: Float
    ) {
        Matrix.multiplyMM(mvMatrix, 0, vMatrix, 0, mMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, pMatrix, 0, mvMatrix, 0)

        Matrix.invertM(tempMatrix, 0, mvMatrix, 0)
        Matrix.transposeM(normalMatrix, 0, tempMatrix, 0)

        GLES20.glUniformMatrix4fv(shader.uMVPMatrixLoc, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(shader.uMVMatrixLoc, 1, false, mvMatrix, 0)
        GLES20.glUniformMatrix4fv(shader.uNormalMatrixLoc, 1, false, normalMatrix, 0)
        GLES20.glUniform3f(shader.uEmissionColorLoc, emissionR, emissionG, emissionB)
        GLES20.glUniform1f(shader.uShininessLoc, 16.0f)

        mesh.draw(shader)
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
