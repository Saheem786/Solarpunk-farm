package com.example.game3d.opengl.models

import android.opengl.GLES20
import android.opengl.Matrix
import com.example.game3d.opengl.GLMesh
import com.example.game3d.opengl.GLModelBuilder
import com.example.game3d.opengl.GLShader

class Environment3DModels {

    val groundMesh: GLMesh
    val roadMesh: GLMesh
    val pondMesh: GLMesh
    val pineTreeMesh: GLMesh
    val oakTreeMesh: GLMesh
    val bushMesh: GLMesh
    val flowerPatchMesh: GLMesh

    private val modelMatrix = FloatArray(16)
    private val mvMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val normalMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)

    // Fixed Tree Placements across farm
    private val treePositions = listOf(
        Triple(-18.0f, -8.0f, true),  // Pine
        Triple(-16.0f, -4.0f, false), // Oak
        Triple(-20.0f, 6.0f, true),   // Pine
        Triple(-18.0f, 16.0f, false), // Oak
        Triple(10.0f, 16.0f, true),   // Pine
        Triple(16.0f, 16.0f, false),  // Oak
        Triple(18.0f, 8.0f, true),    // Pine
        Triple(16.0f, -14.0f, false), // Oak
        Triple(8.0f, -18.0f, true),   // Pine
        Triple(-4.0f, -18.0f, false), // Oak
        Triple(2.0f, -20.0f, true),   // Pine
        Triple(18.0f, -6.0f, false)   // Oak
    )

    init {
        val builder = GLModelBuilder()

        // 1. TERRAIN (Lush Green Farm Grass Field - 64m x 64m)
        builder.reset()
        // Central vibrant meadow grass
        builder.addBox(0f, -0.10f, 0f, 64.0f, 0.20f, 64.0f, 0.28f, 0.68f, 0.26f)
        // Outer boundary earth rim
        builder.addBox(0f, -0.15f, 0f, 72.0f, 0.10f, 72.0f, 0.22f, 0.52f, 0.20f)
        groundMesh = builder.build()

        // 2. COBBLESTONE FARM PATHWAYS (Interconnecting roads)
        builder.reset()
        // North-South Central Main Street (Z: -16..16, X: -0.5..0.5, Width = 3.2m)
        builder.addBox(0f, 0.02f, 0f, 3.2f, 0.04f, 32.0f, 0.72f, 0.68f, 0.62f)
        // East-West Farmhouse Lane (X: 0..8, Z: 0, Width = 2.8m)
        builder.addBox(4.0f, 0.02f, 0f, 8.0f, 0.04f, 2.8f, 0.72f, 0.68f, 0.62f)
        // West Barn Lane (X: -14..0, Z: 6..10, Width = 2.8m)
        builder.addBox(-6.0f, 0.02f, 6.0f, 12.0f, 0.04f, 2.6f, 0.72f, 0.68f, 0.62f)
        // Market Diagonal Lane (X: -14..0, Z: -14..0)
        builder.addBox(-7.0f, 0.02f, -7.0f, 14.0f, 0.04f, 2.6f, 0.72f, 0.68f, 0.62f)
        roadMesh = builder.build()

        // 3. WATER POND (Located at X=10.0, Z=-10.0, Radius = 4.5m)
        builder.reset()
        // Stone Shoreline Rim
        builder.addCylinder(0f, 0.01f, 0f, 4.6f, 0.12f, 14, 0.52f, 0.50f, 0.48f)
        // Shimmering Blue Water Surface
        builder.addCylinder(0f, 0.06f, 0f, 4.2f, 0.04f, 14, 0.10f, 0.65f, 0.88f, 0.90f)
        pondMesh = builder.build()

        // 4. LOW-POLY PINE TREE (Height = 6.2m)
        builder.reset()
        // Brown Wood Trunk
        builder.addCylinder(0f, 0f, 0f, 0.28f, 1.80f, 6, 0.42f, 0.26f, 0.14f)
        // 3 Conical Foliage Tiers (Dark Forest Green)
        builder.addCone(0f, 1.40f, 0f, 1.90f, 2.00f, 7, 0.12f, 0.44f, 0.18f)
        builder.addCone(0f, 2.60f, 0f, 1.50f, 1.80f, 7, 0.15f, 0.52f, 0.22f)
        builder.addCone(0f, 3.80f, 0f, 1.10f, 1.60f, 7, 0.18f, 0.60f, 0.25f)
        pineTreeMesh = builder.build()

        // 5. LOW-POLY OAK / BROADLEAF TREE (Height = 5.5m)
        builder.reset()
        // Gnarled Trunk
        builder.addCylinder(0f, 0f, 0f, 0.35f, 2.00f, 6, 0.46f, 0.30f, 0.16f)
        // Leafy Canopy Spheres (Vibrant Apple Green)
        builder.addSphere(0f, 3.60f, 0f, 1.85f, 6, 8, 0.22f, 0.68f, 0.24f)
        builder.addSphere(-0.75f, 3.20f, 0.4f, 1.25f, 5, 7, 0.26f, 0.74f, 0.28f)
        builder.addSphere(0.75f, 3.30f, -0.4f, 1.25f, 5, 7, 0.24f, 0.70f, 0.26f)
        oakTreeMesh = builder.build()

        // 6. BUSH CLUSTERS
        builder.reset()
        builder.addSphere(0f, 0.55f, 0f, 0.75f, 5, 7, 0.18f, 0.58f, 0.22f)
        builder.addSphere(-0.45f, 0.45f, 0.2f, 0.55f, 5, 6, 0.22f, 0.64f, 0.25f)
        builder.addSphere(0.45f, 0.45f, -0.2f, 0.55f, 5, 6, 0.20f, 0.60f, 0.24f)
        bushMesh = builder.build()

        // 7. FLOWER PATCHES (Vibrant Wildflowers)
        builder.reset()
        builder.addBox(-0.3f, 0.20f, -0.2f, 0.16f, 0.16f, 0.16f, 0.95f, 0.20f, 0.20f) // Red
        builder.addBox(0.3f, 0.20f, -0.2f, 0.16f, 0.16f, 0.16f, 0.98f, 0.85f, 0.15f)  // Gold
        builder.addBox(0f, 0.22f, 0.3f, 0.16f, 0.16f, 0.16f, 0.75f, 0.25f, 0.85f)   // Purple
        flowerPatchMesh = builder.build()
    }

    fun drawEnvironment(
        shader: GLShader,
        viewMatrix: FloatArray,
        projMatrix: FloatArray
    ) {
        // 1. Terrain Ground
        Matrix.setIdentityM(modelMatrix, 0)
        renderMesh(shader, groundMesh, modelMatrix, viewMatrix, projMatrix)

        // 2. Cobblestone Pathways
        renderMesh(shader, roadMesh, modelMatrix, viewMatrix, projMatrix)

        // 3. Water Pond at X = 10.0, Z = -10.0
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 10.0f, 0f, -10.0f)
        renderMesh(shader, pondMesh, modelMatrix, viewMatrix, projMatrix)

        // 4. Scatter Trees
        for (tree in treePositions) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, tree.first, 0f, tree.second)
            val mesh = if (tree.third) pineTreeMesh else oakTreeMesh
            renderMesh(shader, mesh, modelMatrix, viewMatrix, projMatrix)
        }

        // 5. Scatter Bushes & Flowers around farm borders
        val decorPositions = listOf(
            Pair(4.0f, 3.2f),
            Pair(7.8f, -2.5f),
            Pair(-10.0f, 13.5f),
            Pair(2.5f, 13.0f),
            Pair(-12.0f, -12.0f)
        )
        for (pos in decorPositions) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, pos.first, 0f, pos.second)
            renderMesh(shader, bushMesh, modelMatrix, viewMatrix, projMatrix)
            renderMesh(shader, flowerPatchMesh, modelMatrix, viewMatrix, projMatrix)
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
        GLES20.glUniform1f(shader.uShininessLoc, 4.0f)

        mesh.draw(shader)
    }
}
