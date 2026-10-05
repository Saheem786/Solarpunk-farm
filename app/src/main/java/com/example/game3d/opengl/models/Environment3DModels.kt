package com.example.game3d.opengl.models

import android.opengl.GLES20
import android.opengl.Matrix
import com.example.data.model.WeatherType
import com.example.game3d.opengl.GLMesh
import com.example.game3d.opengl.GLModelBuilder
import com.example.game3d.opengl.GLShader
import kotlin.math.cos
import kotlin.math.sin

class Environment3DModels {

    val groundMesh: GLMesh
    val rainGroundMesh: GLMesh
    val roadMesh: GLMesh
    val pondMesh: GLMesh
    val riverMesh: GLMesh
    val woodenBridgeMesh: GLMesh
    val wellMesh: GLMesh
    val lilyPadMesh: GLMesh
    val pineTreeMesh: GLMesh
    val oakTreeMesh: GLMesh
    val cherryTreeMesh: GLMesh
    val willowTreeMesh: GLMesh
    val bushMesh: GLMesh
    val flowerPatchMesh: GLMesh
    val grassTuftMesh: GLMesh
    val treeShadowMesh: GLMesh

    // Celestial & Atmospheric Meshes
    val sunMesh: GLMesh
    val moonMesh: GLMesh
    val lightningBoltMesh: GLMesh

    // Ambient Wildlife Meshes
    val birdBodyMesh: GLMesh
    val birdWingMesh: GLMesh
    val butterflyMesh: GLMesh

    private val modelMatrix = FloatArray(16)
    private val mvMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val normalMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)
    private val partMatrix = FloatArray(16)

    // Tree Placements across farm: (X, Z, TreeType: 0=Pine, 1=Oak, 2=Cherry Blossom, 3=Willow)
    private val treePositions = listOf(
        // North & West Boundary Forest
        Triple(-18.0f, -12.0f, 0), // Pine
        Triple(-22.0f, -6.0f, 1),  // Oak
        Triple(-16.0f, -2.0f, 2),  // Cherry Blossom
        Triple(-22.0f, 4.0f, 0),   // Pine
        Triple(-18.0f, 12.0f, 1),  // Oak
        Triple(-22.0f, 18.0f, 0),  // Pine
        Triple(-14.0f, 22.0f, 1),  // Oak

        // South Boundary Forest
        Triple(-6.0f, 22.0f, 0),   // Pine
        Triple(2.0f, 22.0f, 2),    // Cherry Blossom
        Triple(10.0f, 22.0f, 1),   // Oak
        Triple(18.0f, 22.0f, 0),   // Pine

        // Farmyard & Garden Accents
        Triple(10.0f, 6.0f, 2),    // Cherry Blossom near Farmhouse
        Triple(12.0f, 14.0f, 1),   // Oak near Workshop
        Triple(-6.0f, -16.0f, 2),  // Cherry Blossom near Market
        Triple(4.0f, -18.0f, 0),   // Pine near North Path
        Triple(-2.0f, -22.0f, 1),  // Oak

        // Riverside & Pond Willows
        Triple(12.0f, -14.5f, 3),  // Weeping Willow at Pond Shore
        Triple(6.0f, -11.0f, 3),   // Weeping Willow
        Triple(16.0f, -4.0f, 3),   // Weeping Willow at River
        Triple(18.0f, 4.0f, 3),    // Weeping Willow at Bridge East
        Triple(18.0f, -18.0f, 0)   // Pine at River North
    )

    // 3D Grass Tufts Placements
    private val grassPositions = listOf(
        Pair(-4.0f, 2.0f), Pair(-6.5f, 3.5f), Pair(-3.5f, 5.0f),
        Pair(4.0f, 4.0f), Pair(6.0f, 3.0f), Pair(8.0f, 2.0f),
        Pair(-2.0f, -2.0f), Pair(-5.0f, -4.0f), Pair(-3.0f, -8.0f),
        Pair(2.0f, -5.0f), Pair(5.0f, -4.0f), Pair(4.0f, -8.0f),
        Pair(-10.0f, -8.0f), Pair(-12.0f, -4.0f), Pair(-8.0f, 14.0f),
        Pair(6.0f, 14.0f), Pair(8.0f, 10.0f), Pair(-14.0f, 2.0f),
        Pair(7.0f, -7.0f), Pair(11.0f, -5.0f), Pair(13.0f, -8.0f),
        Pair(14.0f, 2.0f), Pair(14.0f, -2.0f), Pair(8.0f, 18.0f)
    )

    init {
        val builder = GLModelBuilder()

        // 0. TREE DROP SHADOW DISK
        builder.reset()
        builder.addCylinder(0f, 0.015f, 0f, 2.2f, 0.01f, 12, 0.05f, 0.10f, 0.07f, 0.55f)
        treeShadowMesh = builder.build()

        // 1. TERRAIN (Lush Green Farm Grass Field - 64m x 64m)
        builder.reset()
        // Central vibrant meadow grass
        builder.addBox(0f, -0.10f, 0f, 64.0f, 0.20f, 64.0f, 0.28f, 0.68f, 0.26f)
        // Outer boundary earth rim
        builder.addBox(0f, -0.15f, 0f, 72.0f, 0.10f, 72.0f, 0.22f, 0.52f, 0.20f)
        groundMesh = builder.build()

        // 2. COBBLESTONE FARM PATHWAYS
        builder.reset()
        // North-South Central Main Street
        builder.addBox(0f, 0.02f, 0f, 3.2f, 0.04f, 32.0f, 0.72f, 0.68f, 0.62f)
        // East-West Farmhouse Lane (to bridge)
        builder.addBox(7.0f, 0.02f, 0f, 14.0f, 0.04f, 2.8f, 0.72f, 0.68f, 0.62f)
        // West Barn Lane
        builder.addBox(-6.0f, 0.02f, 6.0f, 12.0f, 0.04f, 2.6f, 0.72f, 0.68f, 0.62f)
        // Market Diagonal Lane
        builder.addBox(-7.0f, 0.02f, -7.0f, 14.0f, 0.04f, 2.6f, 0.72f, 0.68f, 0.62f)
        roadMesh = builder.build()

        // 3. WATER POND (Located at X=10.0, Z=-10.0, Radius = 4.8m)
        builder.reset()
        // Stone Shoreline Rim
        builder.addCylinder(0f, 0.01f, 0f, 4.9f, 0.14f, 16, 0.52f, 0.50f, 0.48f)
        // Shimmering Blue Water Surface
        builder.addCylinder(0f, 0.07f, 0f, 4.5f, 0.04f, 16, 0.12f, 0.72f, 0.92f, 0.92f)
        pondMesh = builder.build()

        // 4. WINDING SCENIC RIVER (East Boundary)
        builder.reset()
        // North-East River Segment
        builder.addBox(17.0f, 0.01f, -16.0f, 4.2f, 0.12f, 12.0f, 0.50f, 0.48f, 0.46f)
        builder.addBox(17.0f, 0.06f, -16.0f, 3.6f, 0.04f, 12.0f, 0.10f, 0.68f, 0.90f)
        // Central River Segment (under bridge)
        builder.addBox(16.0f, 0.01f, 0f, 4.4f, 0.12f, 16.0f, 0.50f, 0.48f, 0.46f)
        builder.addBox(16.0f, 0.06f, 0f, 3.8f, 0.04f, 16.0f, 0.10f, 0.68f, 0.90f)
        // South-East River Segment
        builder.addBox(18.0f, 0.01f, 14.0f, 4.6f, 0.12f, 14.0f, 0.50f, 0.48f, 0.46f)
        builder.addBox(18.0f, 0.06f, 14.0f, 4.0f, 0.04f, 14.0f, 0.10f, 0.68f, 0.90f)
        riverMesh = builder.build()

        // 5. WOODEN ARCHED FOOTBRIDGE (Across River at X=16.0, Z=0.0)
        builder.reset()
        // Bridge Deck Planks
        builder.addBox(0f, 0.35f, 0f, 4.8f, 0.16f, 2.8f, 0.65f, 0.42f, 0.24f)
        // Bridge Railings
        builder.addBox(0f, 0.85f, -1.35f, 5.0f, 0.10f, 0.12f, 0.55f, 0.35f, 0.20f)
        builder.addBox(0f, 0.85f, 1.35f, 5.0f, 0.10f, 0.12f, 0.55f, 0.35f, 0.20f)
        // Vertical Railing Posts
        for (i in -2..2) {
            builder.addBox(i * 1.1f, 0.55f, -1.35f, 0.12f, 0.65f, 0.12f, 0.50f, 0.30f, 0.16f)
            builder.addBox(i * 1.1f, 0.55f, 1.35f, 0.12f, 0.65f, 0.12f, 0.50f, 0.30f, 0.16f)
        }
        woodenBridgeMesh = builder.build()

        // 6. STONE DRINKING WELL (Near Farmhouse)
        builder.reset()
        // Stone Circular Rim & Base
        builder.addCylinder(0f, 0.45f, 0f, 0.95f, 0.90f, 10, 0.55f, 0.55f, 0.58f)
        // Water Surface inside well
        builder.addCylinder(0f, 0.65f, 0f, 0.75f, 0.10f, 10, 0.12f, 0.72f, 0.92f)
        // Two Wooden Roof Support Posts
        builder.addCylinder(-0.85f, 0f, 0f, 0.08f, 2.20f, 6, 0.48f, 0.32f, 0.18f)
        builder.addCylinder(0.85f, 0f, 0f, 0.08f, 2.20f, 6, 0.48f, 0.32f, 0.18f)
        // Pitched Timber Canopy Roof
        builder.addRoofPrism(0f, 2.30f, 0f, 2.20f, 0.70f, 1.80f, 0.65f, 0.42f, 0.22f)
        // Wooden Bucket with Rope
        builder.addCylinder(0f, 1.10f, 0f, 0.16f, 0.24f, 6, 0.45f, 0.30f, 0.16f)
        wellMesh = builder.build()

        // 7. LILY PADS & WATER BLOSSOMS
        builder.reset()
        builder.addCylinder(0f, 0.085f, 0f, 0.45f, 0.02f, 8, 0.15f, 0.65f, 0.25f)
        builder.addBox(0f, 0.12f, 0f, 0.16f, 0.08f, 0.16f, 0.98f, 0.55f, 0.75f) // Pink Lotus
        lilyPadMesh = builder.build()

        // 7. LOW-POLY PINE TREE (Height = 6.2m)
        builder.reset()
        builder.addCylinder(0f, 0f, 0f, 0.28f, 1.80f, 6, 0.42f, 0.26f, 0.14f)
        builder.addCone(0f, 1.40f, 0f, 1.90f, 2.00f, 7, 0.12f, 0.44f, 0.18f)
        builder.addCone(0f, 2.60f, 0f, 1.50f, 1.80f, 7, 0.15f, 0.52f, 0.22f)
        builder.addCone(0f, 3.80f, 0f, 1.10f, 1.60f, 7, 0.18f, 0.60f, 0.25f)
        pineTreeMesh = builder.build()

        // 8. LOW-POLY OAK / BROADLEAF TREE (Height = 5.5m)
        builder.reset()
        builder.addCylinder(0f, 0f, 0f, 0.35f, 2.00f, 6, 0.46f, 0.30f, 0.16f)
        builder.addSphere(0f, 3.60f, 0f, 1.85f, 6, 8, 0.22f, 0.68f, 0.24f)
        builder.addSphere(-0.75f, 3.20f, 0.4f, 1.25f, 5, 7, 0.26f, 0.74f, 0.28f)
        builder.addSphere(0.75f, 3.30f, -0.4f, 1.25f, 5, 7, 0.24f, 0.70f, 0.26f)
        oakTreeMesh = builder.build()

        // 9. CHERRY BLOSSOM TREE (Pink & Cream Flower Canopy)
        builder.reset()
        builder.addCylinder(0f, 0f, 0f, 0.32f, 1.90f, 6, 0.48f, 0.32f, 0.20f)
        builder.addSphere(0f, 3.40f, 0f, 1.75f, 6, 8, 0.98f, 0.68f, 0.78f) // Soft Sakura Pink
        builder.addSphere(-0.7f, 3.10f, 0.3f, 1.20f, 5, 7, 0.96f, 0.78f, 0.84f)
        builder.addSphere(0.7f, 3.20f, -0.3f, 1.20f, 5, 7, 0.95f, 0.60f, 0.72f)
        cherryTreeMesh = builder.build()

        // 10. WEEPING WILLOW TREE (Drooping Graceful Foliage)
        builder.reset()
        builder.addCylinder(0f, 0f, 0f, 0.38f, 2.20f, 6, 0.44f, 0.28f, 0.18f)
        builder.addSphere(0f, 3.80f, 0f, 1.90f, 6, 8, 0.35f, 0.75f, 0.30f)
        builder.addCone(0f, 1.60f, 0f, 2.40f, 2.50f, 8, 0.28f, 0.68f, 0.25f) // Drooping canopy
        willowTreeMesh = builder.build()

        // 11. BUSH CLUSTERS
        builder.reset()
        builder.addSphere(0f, 0.55f, 0f, 0.75f, 5, 7, 0.18f, 0.58f, 0.22f)
        builder.addSphere(-0.45f, 0.45f, 0.2f, 0.55f, 5, 6, 0.22f, 0.64f, 0.25f)
        builder.addSphere(0.45f, 0.45f, -0.2f, 0.55f, 5, 6, 0.20f, 0.60f, 0.24f)
        bushMesh = builder.build()

        // 12. FLOWER PATCHES (Vibrant Wildflowers)
        builder.reset()
        builder.addBox(-0.3f, 0.20f, -0.2f, 0.16f, 0.16f, 0.16f, 0.95f, 0.20f, 0.20f) // Red
        builder.addBox(0.3f, 0.20f, -0.2f, 0.16f, 0.16f, 0.16f, 0.98f, 0.85f, 0.15f)  // Gold
        builder.addBox(0f, 0.22f, 0.3f, 0.16f, 0.16f, 0.16f, 0.75f, 0.25f, 0.85f)   // Purple
        flowerPatchMesh = builder.build()

        // 13. 3D DENSE GRASS TUFTS (Crossed Blades with Sun-Kissed Tips)
        builder.reset()
        builder.addBox(0f, 0.22f, 0f, 0.08f, 0.44f, 0.48f, 0.22f, 0.78f, 0.24f)
        builder.addBox(0f, 0.22f, 0f, 0.48f, 0.44f, 0.08f, 0.32f, 0.84f, 0.28f)
        builder.addBox(0.12f, 0.18f, 0.12f, 0.36f, 0.36f, 0.06f, 0.85f, 0.90f, 0.35f) // Golden blade
        grassTuftMesh = builder.build()

        // 14. 3D BIRD (Body + Wings)
        builder.reset()
        // Sleek White/Azure Bird Body
        builder.addBox(0f, 0f, 0f, 0.22f, 0.18f, 0.55f, 0.95f, 0.96f, 0.98f)
        builder.addCone(0f, 0f, 0.32f, 0.08f, 0.20f, 4, 0.98f, 0.70f, 0.15f) // Beak
        birdBodyMesh = builder.build()

        builder.reset()
        // Bird Wing Plane
        builder.addBox(0.40f, 0f, 0f, 0.80f, 0.04f, 0.35f, 0.88f, 0.92f, 0.98f)
        birdWingMesh = builder.build()

        // 15. 3D BUTTERFLY (Monarch / Solarpunk Cyan Flutter)
        builder.reset()
        // Tiny Body
        builder.addBox(0f, 0f, 0f, 0.06f, 0.06f, 0.22f, 0.18f, 0.15f, 0.15f)
        // Colorful Flapping Wings
        builder.addBox(-0.20f, 0.02f, 0f, 0.35f, 0.02f, 0.26f, 0.98f, 0.55f, 0.12f) // Orange Wing
        builder.addBox(0.20f, 0.02f, 0f, 0.35f, 0.02f, 0.26f, 0.00f, 0.85f, 0.95f)  // Cyan Wing
        butterflyMesh = builder.build()

        // 16. RAIN-WET GROUND OVERLAY (Darkened Muddy Earth)
        builder.reset()
        builder.addBox(0f, -0.09f, 0f, 64.2f, 0.01f, 64.2f, 0.14f, 0.32f, 0.15f)
        rainGroundMesh = builder.build()

        // 17. GLOWING CELESTIAL SUN SPHERE
        builder.reset()
        builder.addSphere(0f, 0f, 0f, 2.6f, 8, 10, 1.0f, 0.94f, 0.40f)
        sunMesh = builder.build()

        // 18. GLOWING CELESTIAL MOON SPHERE
        builder.reset()
        builder.addSphere(0f, 0f, 0f, 2.1f, 8, 10, 0.88f, 0.92f, 1.0f)
        // Soft craters
        builder.addSphere(-0.5f, 0.4f, 1.8f, 0.4f, 4, 5, 0.70f, 0.75f, 0.85f)
        builder.addSphere(0.6f, -0.3f, 1.7f, 0.5f, 4, 5, 0.70f, 0.75f, 0.85f)
        moonMesh = builder.build()

        // 19. STORM LIGHTNING BOLT MESH
        builder.reset()
        builder.addBox(0f, 18.0f, 0f, 0.35f, 5.0f, 0.35f, 0.90f, 0.96f, 1.0f)
        builder.addBox(-1.2f, 13.5f, 0f, 0.35f, 5.0f, 0.35f, 0.90f, 0.96f, 1.0f)
        builder.addBox(0.8f, 8.5f, 0f, 0.30f, 5.5f, 0.30f, 0.90f, 0.96f, 1.0f)
        builder.addBox(-0.5f, 3.5f, 0f, 0.25f, 5.0f, 0.25f, 0.90f, 0.96f, 1.0f)
        lightningBoltMesh = builder.build()
    }

    fun drawEnvironment(
        shader: GLShader,
        viewMatrix: FloatArray,
        projMatrix: FloatArray,
        animTime: Float = 0.0f,
        hour: Float = 12.0f,
        weather: WeatherType = WeatherType.SUNNY_CLEAR
    ) {
        // 1. Terrain Ground
        Matrix.setIdentityM(modelMatrix, 0)
        renderMesh(shader, groundMesh, modelMatrix, viewMatrix, projMatrix)

        // Wet / Dark Ground Overlay during Rain or Storm
        if (weather == WeatherType.RAINY_STORM || weather == WeatherType.STORM) {
            Matrix.setIdentityM(modelMatrix, 0)
            renderMesh(shader, rainGroundMesh, modelMatrix, viewMatrix, projMatrix)
        }

        // 2. Cobblestone Pathways
        Matrix.setIdentityM(modelMatrix, 0)
        renderMesh(shader, roadMesh, modelMatrix, viewMatrix, projMatrix)

        // 3. Winding Scenic River
        Matrix.setIdentityM(modelMatrix, 0)
        renderMesh(shader, riverMesh, modelMatrix, viewMatrix, projMatrix)

        // 4. Wooden Footbridge across River
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 16.0f, 0f, 0f)
        renderMesh(shader, woodenBridgeMesh, modelMatrix, viewMatrix, projMatrix)

        // 5. Water Pond at X = 10.0, Z = -10.0 with Lily Pads
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 10.0f, 0f, -10.0f)
        renderMesh(shader, pondMesh, modelMatrix, viewMatrix, projMatrix)

        // 5b. Stone Drinking Water Well (Near Farmhouse at X=8.0, Z=-3.5)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 8.0f, 0f, -3.5f)
        renderMesh(shader, treeShadowMesh, modelMatrix, viewMatrix, projMatrix)
        renderMesh(shader, wellMesh, modelMatrix, viewMatrix, projMatrix)

        // Floating Lily Pads on Pond
        val lilyOffsets = listOf(
            Pair(-1.8f, 1.2f),
            Pair(1.4f, -1.6f),
            Pair(-1.2f, -1.8f),
            Pair(2.2f, 1.0f)
        )
        for (off in lilyOffsets) {
            Matrix.setIdentityM(modelMatrix, 0)
            val wobbleY = sin(animTime * 2.0f + off.first) * 0.01f
            Matrix.translateM(modelMatrix, 0, 10.0f + off.first, wobbleY, -10.0f + off.second)
            renderMesh(shader, lilyPadMesh, modelMatrix, viewMatrix, projMatrix)
        }

        // 6. Scatter Trees with Ground Shadows
        for (tree in treePositions) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, tree.first, 0f, tree.second)

            // Draw Tree Ground Shadow
            renderMesh(shader, treeShadowMesh, modelMatrix, viewMatrix, projMatrix)

            // Draw Tree Mesh
            val mesh = when (tree.third) {
                0 -> pineTreeMesh
                1 -> oakTreeMesh
                2 -> cherryTreeMesh
                else -> willowTreeMesh
            }
            renderMesh(shader, mesh, modelMatrix, viewMatrix, projMatrix)
        }

        // 7. Scatter 3D Dense Grass Tufts
        for (pos in grassPositions) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, pos.first, 0f, pos.second)
            renderMesh(shader, grassTuftMesh, modelMatrix, viewMatrix, projMatrix)
        }

        // 8. Scatter Bushes & Flowers around farm borders
        val decorPositions = listOf(
            Pair(4.0f, 3.2f),
            Pair(7.8f, -2.5f),
            Pair(-10.0f, 13.5f),
            Pair(2.5f, 13.0f),
            Pair(-12.0f, -12.0f),
            Pair(14.0f, 8.0f),
            Pair(8.0f, -14.0f)
        )
        for (pos in decorPositions) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, pos.first, 0f, pos.second)
            renderMesh(shader, bushMesh, modelMatrix, viewMatrix, projMatrix)
            renderMesh(shader, flowerPatchMesh, modelMatrix, viewMatrix, projMatrix)
        }

        // 9. Ambient Wildlife: Soaring Birds overhead
        drawAmbientBirds(shader, viewMatrix, projMatrix, animTime)

        // 10. Ambient Wildlife: Fluttering Butterflies
        drawAmbientButterflies(shader, viewMatrix, projMatrix, animTime)

        // 11. Celestial Sun (Moves across sky based on hour: rises at 6, zenith at 12, sets at 18)
        val sunAngle = ((hour - 6.0f) / 12.0f) * Math.PI.toFloat()
        val sunX = cos(sunAngle) * 38.0f
        val sunY = sin(sunAngle) * 28.0f
        val sunZ = sin(sunAngle * 0.5f) * 12.0f
        if (sunY > -4.0f) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, sunX, sunY, sunZ)
            renderMesh(shader, sunMesh, modelMatrix, viewMatrix, projMatrix, emissionR = 0.98f, emissionG = 0.90f, emissionB = 0.35f)
        }

        // 12. Celestial Moon (Opposite side of orbit at night: rises at 18, zenith at midnight, sets at 6)
        val moonAngle = sunAngle + Math.PI.toFloat()
        val moonX = cos(moonAngle) * 38.0f
        val moonY = sin(moonAngle) * 28.0f
        val moonZ = sin(moonAngle * 0.5f) * 12.0f
        if (moonY > -4.0f) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, moonX, moonY, moonZ)
            renderMesh(shader, moonMesh, modelMatrix, viewMatrix, projMatrix, emissionR = 0.70f, emissionG = 0.85f, emissionB = 1.0f)
        }

        // 13. Lightning Flashes during Storm
        if (weather == WeatherType.STORM) {
            val flashCycle = (animTime * 1.6f) % 4.0f
            if (flashCycle < 0.18f) {
                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, -10.0f, 0f, -14.0f)
                renderMesh(shader, lightningBoltMesh, modelMatrix, viewMatrix, projMatrix, emissionR = 1.0f, emissionG = 1.0f, emissionB = 1.0f)
            }
        }
    }

    private fun drawAmbientBirds(
        shader: GLShader,
        viewMatrix: FloatArray,
        projMatrix: FloatArray,
        animTime: Float
    ) {
        // 3 Birds flying in sweeping orbital sky paths
        for (i in 0 until 3) {
            val angle = animTime * 0.45f + (i * 2.094f) // 120 deg apart
            val radius = 14.0f + (i * 2.5f)
            val bx = cos(angle) * radius
            val bz = sin(angle) * radius
            val by = 9.5f + sin(animTime * 1.2f + i) * 1.5f
            val headingDeg = Math.toDegrees(-angle.toDouble() + Math.PI / 2.0).toFloat()

            val wingFlap = sin(animTime * 12.0f + i * 2.0f) * 28.0f

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, bx, by, bz)
            Matrix.rotateM(modelMatrix, 0, headingDeg, 0f, 1f, 0f)

            // Bird Body
            renderMesh(shader, birdBodyMesh, modelMatrix, viewMatrix, projMatrix)

            // Left Wing
            Matrix.setIdentityM(partMatrix, 0)
            Matrix.multiplyMM(partMatrix, 0, modelMatrix, 0, partMatrix, 0)
            Matrix.rotateM(partMatrix, 0, wingFlap, 0f, 0f, 1f)
            renderMesh(shader, birdWingMesh, partMatrix, viewMatrix, projMatrix)

            // Right Wing
            Matrix.setIdentityM(partMatrix, 0)
            Matrix.multiplyMM(partMatrix, 0, modelMatrix, 0, partMatrix, 0)
            Matrix.rotateM(partMatrix, 0, 180.0f, 0f, 1f, 0f)
            Matrix.rotateM(partMatrix, 0, wingFlap, 0f, 0f, 1f)
            renderMesh(shader, birdWingMesh, partMatrix, viewMatrix, projMatrix)
        }
    }

    private fun drawAmbientButterflies(
        shader: GLShader,
        viewMatrix: FloatArray,
        projMatrix: FloatArray,
        animTime: Float
    ) {
        val butterflySpawns = listOf(
            Triple(4.5f, 3.5f, 0.0f),
            Triple(7.5f, -2.2f, 1.5f),
            Triple(-4.5f, -6.5f, 3.0f),
            Triple(-10.5f, 13.0f, 4.5f),
            Triple(10.5f, -9.0f, 2.0f)
        )

        for (spawn in butterflySpawns) {
            val t = animTime + spawn.third
            val flutterX = spawn.first + sin(t * 1.8f) * 1.2f
            val flutterZ = spawn.second + cos(t * 1.4f) * 1.2f
            val flutterY = 0.65f + sin(t * 3.5f) * 0.35f
            val wingFlap = sin(t * 18.0f) * 45.0f

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, flutterX, flutterY, flutterZ)
            Matrix.rotateM(modelMatrix, 0, sin(t) * 40.0f, 0f, 1f, 0f)

            renderMesh(shader, butterflyMesh, modelMatrix, viewMatrix, projMatrix)
        }
    }

    private fun renderMesh(
        shader: GLShader,
        mesh: GLMesh,
        mMatrix: FloatArray,
        vMatrix: FloatArray,
        pMatrix: FloatArray,
        emissionR: Float = 0f,
        emissionG: Float = 0f,
        emissionB: Float = 0f
    ) {
        Matrix.multiplyMM(mvMatrix, 0, vMatrix, 0, mMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, pMatrix, 0, mvMatrix, 0)

        Matrix.invertM(tempMatrix, 0, mvMatrix, 0)
        Matrix.transposeM(normalMatrix, 0, tempMatrix, 0)

        GLES20.glUniformMatrix4fv(shader.uMVPMatrixLoc, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(shader.uMVMatrixLoc, 1, false, mvMatrix, 0)
        GLES20.glUniformMatrix4fv(shader.uNormalMatrixLoc, 1, false, normalMatrix, 0)
        GLES20.glUniform3f(shader.uEmissionColorLoc, emissionR, emissionG, emissionB)
        GLES20.glUniform1f(shader.uShininessLoc, 4.0f)

        mesh.draw(shader)
    }
}

