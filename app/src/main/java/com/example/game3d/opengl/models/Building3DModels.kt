package com.example.game3d.opengl.models

import android.opengl.GLES20
import android.opengl.Matrix
import com.example.data.local.EnergyNodeEntity
import com.example.data.model.EnergyNodeType
import com.example.game3d.opengl.GLMesh
import com.example.game3d.opengl.GLModelBuilder
import com.example.game3d.opengl.GLShader

class Building3DModels {

    val farmhouseMesh: GLMesh
    val barnMesh: GLMesh
    val workshopMesh: GLMesh
    val turbineBladeMesh: GLMesh
    val marketStallMesh: GLMesh
    val solarArrayMesh: GLMesh
    val batteryHubMesh: GLMesh
    val fenceSegmentMesh: GLMesh

    private val modelMatrix = FloatArray(16)
    private val mvMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val normalMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)
    private val partMatrix = FloatArray(16)

    init {
        val builder = GLModelBuilder()

        // 1. FARM HOUSE (Located at X=6.0, Z=0.0)
        // Dimensions: 7.2m wide, 5.0m deep, 5.2m high
        builder.reset()
        // Stone Foundation (Y = 0..0.4)
        builder.addBox(0f, 0.20f, 0f, 7.4f, 0.40f, 5.2f, 0.48f, 0.48f, 0.50f)
        // Timber/Clapboard Walls (Y = 0.4..3.2) - Warm Sand / Cream
        builder.addBox(0f, 1.80f, 0f, 7.0f, 2.80f, 4.8f, 0.92f, 0.88f, 0.78f)
        // Pitched Shingle Roof (Y = 3.2..5.4) - Warm Terracotta Orange
        builder.addRoofPrism(0f, 3.20f, 0f, 7.6f, 2.20f, 5.4f, 0.82f, 0.38f, 0.22f)
        // Chimney (Brick Red)
        builder.addBox(2.2f, 4.50f, -0.8f, 0.70f, 1.80f, 0.70f, 0.65f, 0.28f, 0.22f)
        // Front Door (Dark Oak Wood)
        builder.addBox(0f, 1.25f, 2.42f, 1.10f, 2.10f, 0.08f, 0.42f, 0.26f, 0.16f)
        // Brass Door Knob
        builder.addBox(0.40f, 1.20f, 2.47f, 0.08f, 0.08f, 0.04f, 0.96f, 0.82f, 0.20f)
        // Front Windows (Warm Golden Glow)
        builder.addBox(-1.8f, 1.70f, 2.42f, 1.20f, 1.20f, 0.06f, 0.98f, 0.92f, 0.50f)
        builder.addBox(1.8f, 1.70f, 2.42f, 1.20f, 1.20f, 0.06f, 0.98f, 0.92f, 0.50f)
        // Window Crossframes
        builder.addBox(-1.8f, 1.70f, 2.46f, 1.24f, 0.10f, 0.04f, 0.32f, 0.22f, 0.14f)
        builder.addBox(-1.8f, 1.70f, 2.46f, 0.10f, 1.24f, 0.04f, 0.32f, 0.22f, 0.14f)
        builder.addBox(1.8f, 1.70f, 2.46f, 1.24f, 0.10f, 0.04f, 0.32f, 0.22f, 0.14f)
        builder.addBox(1.8f, 1.70f, 2.46f, 0.10f, 1.24f, 0.04f, 0.32f, 0.22f, 0.14f)
        // Covered Porch & Wooden Steps (Y = 0..2.6, Z = 2.4..4.2)
        builder.addBox(0f, 0.18f, 3.30f, 5.4f, 0.36f, 1.8f, 0.55f, 0.38f, 0.24f) // Porch Deck
        builder.addCylinder(-2.4f, 0.36f, 4.0f, 0.12f, 2.30f, 6, 0.88f, 0.85f, 0.78f) // Post Left
        builder.addCylinder(2.4f, 0.36f, 4.0f, 0.12f, 2.30f, 6, 0.88f, 0.85f, 0.78f)  // Post Right
        builder.addRoofPrism(0f, 2.60f, 3.30f, 5.6f, 0.90f, 2.0f, 0.82f, 0.38f, 0.22f) // Porch Roof
        farmhouseMesh = builder.build()

        // 2. RUSTIC BARN (Located at X=-12.0, Z=10.0)
        // Dimensions: 9.5m wide, 7.5m deep, 6.8m high
        builder.reset()
        // Red Barn Siding (Y = 0..3.6)
        builder.addBox(0f, 1.80f, 0f, 9.2f, 3.60f, 7.2f, 0.78f, 0.20f, 0.18f)
        // White Corner Trims
        builder.addBox(-4.55f, 1.80f, -3.55f, 0.30f, 3.65f, 0.30f, 0.95f, 0.95f, 0.95f)
        builder.addBox(4.55f, 1.80f, -3.55f, 0.30f, 3.65f, 0.30f, 0.95f, 0.95f, 0.95f)
        builder.addBox(-4.55f, 1.80f, 3.55f, 0.30f, 3.65f, 0.30f, 0.95f, 0.95f, 0.95f)
        builder.addBox(4.55f, 1.80f, 3.55f, 0.30f, 3.65f, 0.30f, 0.95f, 0.95f, 0.95f)
        // Gambrel / Pitched Roof (Y = 3.6..6.8) - Charcoal Slate
        builder.addRoofPrism(0f, 3.60f, 0f, 9.8f, 3.20f, 7.6f, 0.28f, 0.30f, 0.34f)
        // Large Barn Sliding Doors (+Z)
        builder.addBox(0f, 1.50f, 3.62f, 3.2f, 3.00f, 0.10f, 0.95f, 0.95f, 0.95f)
        builder.addBox(-0.75f, 1.50f, 3.68f, 1.4f, 2.80f, 0.06f, 0.72f, 0.18f, 0.16f)
        builder.addBox(0.75f, 1.50f, 3.68f, 1.4f, 2.80f, 0.06f, 0.72f, 0.18f, 0.16f)
        // Loft Window (+Z)
        builder.addBox(0f, 4.20f, 3.62f, 1.20f, 1.20f, 0.08f, 0.96f, 0.90f, 0.50f)
        // Weather Vane Spire
        builder.addCylinder(0f, 6.80f, 0f, 0.06f, 0.90f, 4, 0.92f, 0.78f, 0.20f)
        barnMesh = builder.build()

        // 3. ARTISAN ECO-WORKSHOP (Located at X=0.0, Z=14.0)
        // Dimensions: 6.8m wide, 5.6m deep, 4.8m high
        builder.reset()
        // Clean White / Sage Eco Walls
        builder.addBox(0f, 1.60f, 0f, 6.6f, 3.20f, 5.4f, 0.88f, 0.92f, 0.88f)
        // Slanted Solar-Glass Roof
        builder.addRoofPrism(0f, 3.20f, 0f, 7.0f, 1.60f, 5.8f, 0.10f, 0.65f, 0.85f)
        // Workshop Garage Roller Door (-Z)
        builder.addBox(0f, 1.30f, -2.72f, 3.0f, 2.60f, 0.08f, 0.40f, 0.50f, 0.55f)
        // Solar Crafting Workbench Outside
        builder.addBox(2.2f, 0.50f, 3.20f, 1.8f, 0.90f, 0.90f, 0.52f, 0.35f, 0.22f)
        workshopMesh = builder.build()

        // Turbine Blade (Length = 2.4m)
        builder.reset()
        builder.addBox(0f, 1.20f, 0f, 0.22f, 2.40f, 0.04f, 0.95f, 0.95f, 0.95f)
        turbineBladeMesh = builder.build()

        // 4. SOL CITY MARKET STALL (Located at X=-14.0, Z=-14.0)
        builder.reset()
        // Wooden Counter Frame
        builder.addBox(0f, 0.55f, 0f, 3.6f, 1.10f, 1.6f, 0.58f, 0.40f, 0.24f)
        // 4 Canopy Posts
        builder.addCylinder(-1.6f, 0f, -0.7f, 0.08f, 2.60f, 6, 0.45f, 0.30f, 0.18f)
        builder.addCylinder(1.6f, 0f, -0.7f, 0.08f, 2.60f, 6, 0.45f, 0.30f, 0.18f)
        builder.addCylinder(-1.6f, 0f, 0.7f, 0.08f, 2.60f, 6, 0.45f, 0.30f, 0.18f)
        builder.addCylinder(1.6f, 0f, 0.7f, 0.08f, 2.60f, 6, 0.45f, 0.30f, 0.18f)
        // Striped Awning Canopy (Emerald Green & Sun Gold: 0.0, 0.9, 0.45 & 1.0, 0.85, 0.15)
        builder.addRoofPrism(0f, 2.50f, 0f, 4.0f, 0.80f, 2.2f, 0.00f, 0.85f, 0.45f)
        // Produce Crates on Counter
        builder.addBox(-0.9f, 1.22f, 0f, 0.80f, 0.30f, 0.60f, 0.88f, 0.25f, 0.15f) // Tomato crate
        builder.addBox(0.9f, 1.22f, 0f, 0.80f, 0.30f, 0.60f, 0.98f, 0.82f, 0.10f)  // Corn crate
        marketStallMesh = builder.build()

        // 5. SOLAR PHOTOVOLTAIC ARRAY (Energy Node)
        builder.reset()
        // Metal support truss
        builder.addBox(0f, 0.60f, 0f, 0.20f, 1.20f, 0.20f, 0.55f, 0.58f, 0.62f)
        builder.addBox(-1.4f, 0.60f, 0f, 0.15f, 1.20f, 0.15f, 0.55f, 0.58f, 0.62f)
        builder.addBox(1.4f, 0.60f, 0f, 0.15f, 1.20f, 0.15f, 0.55f, 0.58f, 0.62f)
        // Angled Photovoltaic Panels (Deep Cyan/Blue Silicon Cells: 0.08, 0.35, 0.68)
        builder.addBox(0f, 1.40f, 0f, 3.4f, 0.08f, 2.2f, 0.08f, 0.38f, 0.72f)
        // Solar Grid Lines
        builder.addBox(0f, 1.45f, 0f, 3.42f, 0.02f, 0.04f, 0.00f, 0.90f, 0.90f)
        solarArrayMesh = builder.build()

        // 6. BATTERY STORAGE BANK (Energy Node)
        builder.reset()
        // Sleek Eco-Battery Enclosure
        builder.addBox(0f, 0.90f, 0f, 1.8f, 1.80f, 1.2f, 0.85f, 0.90f, 0.88f)
        // Glowing Charge Level Indicators (Emerald: 0.0, 0.95, 0.45)
        builder.addBox(0f, 1.10f, 0.61f, 1.2f, 0.15f, 0.02f, 0.00f, 0.95f, 0.45f)
        builder.addBox(0f, 0.80f, 0.61f, 1.2f, 0.15f, 0.02f, 0.00f, 0.95f, 0.45f)
        builder.addBox(0f, 0.50f, 0.61f, 1.2f, 0.15f, 0.02f, 0.00f, 0.95f, 0.45f)
        batteryHubMesh = builder.build()

        // 7. POST-AND-RAIL WOODEN FENCE SEGMENT (Length = 3.0m, Height = 1.1m)
        builder.reset()
        // 2 Posts
        builder.addCylinder(-1.45f, 0f, 0f, 0.09f, 1.15f, 6, 0.58f, 0.42f, 0.26f)
        builder.addCylinder(1.45f, 0f, 0f, 0.09f, 1.15f, 6, 0.58f, 0.42f, 0.26f)
        // 2 Horizontal Rails
        builder.addBox(0f, 0.85f, 0f, 2.90f, 0.12f, 0.08f, 0.68f, 0.48f, 0.30f)
        builder.addBox(0f, 0.45f, 0f, 2.90f, 0.12f, 0.08f, 0.68f, 0.48f, 0.30f)
        fenceSegmentMesh = builder.build()
    }

    fun drawBuildings(
        shader: GLShader,
        viewMatrix: FloatArray,
        projMatrix: FloatArray,
        energyNodes: List<EnergyNodeEntity>,
        animTime: Float
    ) {
        // 1. Draw Farmhouse at X = 6.0, Z = 0.0
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 6.0f, 0.0f, 0.0f)
        Matrix.rotateM(modelMatrix, 0, -90.0f, 0f, 1f, 0f) // Facing West towards player
        renderMesh(shader, farmhouseMesh, modelMatrix, viewMatrix, projMatrix)

        // 2. Draw Rustic Barn at X = -12.0, Z = 10.0
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, -12.0f, 0.0f, 10.0f)
        Matrix.rotateM(modelMatrix, 0, 0.0f, 0f, 1f, 0f)
        renderMesh(shader, barnMesh, modelMatrix, viewMatrix, projMatrix)

        // 3. Draw Artisan Eco-Workshop at X = 0.0, Z = 14.0
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, 14.0f)
        renderMesh(shader, workshopMesh, modelMatrix, viewMatrix, projMatrix)

        // 4. Draw Sol City Market Trading Stall at X = -14.0, Z = -14.0
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, -14.0f, 0.0f, -14.0f)
        Matrix.rotateM(modelMatrix, 0, 45.0f, 0f, 1f, 0f)
        renderMesh(shader, marketStallMesh, modelMatrix, viewMatrix, projMatrix)

        // 5. Draw Clean Energy Nodes
        for (node in energyNodes) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, node.posX, node.posY, node.posZ)

            when (node.nodeType) {
                EnergyNodeType.PHOTOVOLTAIC_ARRAY -> {
                    renderMesh(shader, solarArrayMesh, modelMatrix, viewMatrix, projMatrix)
                }
                EnergyNodeType.BATTERY_STORAGE_BANK -> {
                    renderMesh(shader, batteryHubMesh, modelMatrix, viewMatrix, projMatrix)
                }
                EnergyNodeType.VERTICAL_WIND_TURBINE -> {
                    // Mast
                    Matrix.setIdentityM(partMatrix, 0)
                    Matrix.multiplyMM(partMatrix, 0, modelMatrix, 0, partMatrix, 0)
                    renderMesh(shader, batteryHubMesh, partMatrix, viewMatrix, projMatrix)

                    // 3 Spinning Blades (Rotation around Z axis)
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
                    renderMesh(shader, solarArrayMesh, modelMatrix, viewMatrix, projMatrix)
                }
            }
        }

        // 6. Draw Animal Pen Perimeter Fences around (X: -16..-8, Z: 2..14)
        drawFenceLine(shader, viewMatrix, projMatrix, -16f, 2f, -8f, 2f)
        drawFenceLine(shader, viewMatrix, projMatrix, -8f, 2f, -8f, 8f)
        drawFenceLine(shader, viewMatrix, projMatrix, -16f, 2f, -16f, 14f)
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
