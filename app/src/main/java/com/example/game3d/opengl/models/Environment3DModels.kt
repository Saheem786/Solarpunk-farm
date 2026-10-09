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

    // 3 Biome Terrain Ground Meshes
    val valleyGroundMesh: GLMesh
    val forestGroundMesh: GLMesh
    val wetlandGroundMesh: GLMesh
    val worldBoundaryMesh: GLMesh
    val roadMesh: GLMesh
    val pondMesh: GLMesh
    val wetlandPondMesh: GLMesh
    val riverMesh: GLMesh
    val wetlandRiverMesh: GLMesh
    val woodenBridgeMesh: GLMesh
    val wellMesh: GLMesh
    val lilyPadMesh: GLMesh

    // Tree Meshes
    val pineTreeMesh: GLMesh
    val tallForestPineMesh: GLMesh
    val oakTreeMesh: GLMesh
    val ancientOakMesh: GLMesh
    val cherryTreeMesh: GLMesh
    val willowTreeMesh: GLMesh
    val bushMesh: GLMesh
    val flowerPatchMesh: GLMesh
    val grassTuftMesh: GLMesh
    val treeShadowMesh: GLMesh

    // Deep Forest POI & Decor Meshes
    val rangerTowerMesh: GLMesh
    val campsiteMesh: GLMesh
    val stoneCircleMesh: GLMesh
    val mossyRockMesh: GLMesh
    val fallenLogMesh: GLMesh
    val medicinalHerbMesh: GLMesh
    val forestMushroomsMesh: GLMesh

    // Wetland POI & Decor Meshes
    val fishingHutMesh: GLMesh
    val brokenBridgeMesh: GLMesh
    val sunkenBoatMesh: GLMesh
    val cattailsReedsMesh: GLMesh

    // Green Valley POI Mesh
    val caveEntranceMesh: GLMesh

    // Celestial & Atmospheric Meshes
    val sunMesh: GLMesh
    val moonMesh: GLMesh
    val lightningBoltMesh: GLMesh

    // Ambient Wildlife Meshes
    val birdBodyMesh: GLMesh
    val birdWingMesh: GLMesh
    val butterflyMesh: GLMesh
    val beeMesh: GLMesh

    private val modelMatrix = FloatArray(16)
    private val mvMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val normalMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)

    // Tree Placements across 3 Biomes: (X, Z, TreeType: 0=Pine, 1=Oak, 2=Cherry, 3=Willow, 4=Ancient Oak, 5=Tall Pine)
    private val worldTreePositions = FarmWorldLayout.treePositions.map { Triple(it.x, it.z, it.type) }

    init {
        val builder = GLModelBuilder()

        // 0. TREE DROP SHADOW
        builder.reset()
        builder.addCylinder(0f, 0.015f, 0f, 2.2f, 0.01f, 12, 0.05f, 0.10f, 0.07f, 0.55f)
        treeShadowMesh = builder.build()

        // 1. GREEN VALLEY TERRAIN MEADOW (Z: -40 to +40, 400m wide)
        builder.reset()
        builder.addBox(0f, -0.10f, 0f, 400.0f, 0.20f, 80.0f, 0.40f, 0.70f, 0.30f) // Cozy Meadow
        valleyGroundMesh = builder.build()

        // 2. DEEP FOREST TERRAIN FLOOR (Z: +40 to +200, 400m wide)
        builder.reset()
        builder.addBox(0f, -0.09f, 120.0f, 400.0f, 0.20f, 160.0f, 0.20f, 0.40f, 0.20f) // Rich Mossy Earth
        forestGroundMesh = builder.build()

        // 3. WETLAND & MARSH TERRAIN (Z: -200 to -40, 400m wide)
        builder.reset()
        builder.addBox(0f, -0.11f, -120.0f, 400.0f, 0.20f, 160.0f, 0.30f, 0.40f, 0.30f) // Muted Marsh Mud
        wetlandGroundMesh = builder.build()

        // 4. WORLD BOUNDARY CLIFFS & MOUNTAIN PERIMETER (400m x 400m)
        builder.reset()
        // North Wall
        builder.addBox(0f, 6.0f, 202.0f, 410.0f, 12.0f, 14.0f, 0.30f, 0.38f, 0.28f)
        // South Wall
        builder.addBox(0f, 6.0f, -202.0f, 410.0f, 12.0f, 14.0f, 0.25f, 0.35f, 0.32f)
        // West Wall
        builder.addBox(-202.0f, 6.0f, 0f, 14.0f, 12.0f, 410.0f, 0.28f, 0.36f, 0.30f)
        // East Wall
        builder.addBox(202.0f, 6.0f, 0f, 14.0f, 12.0f, 410.0f, 0.28f, 0.36f, 0.30f)
        worldBoundaryMesh = builder.build()

        // 5. COBBLESTONE PATHWAYS & TRAILS
        builder.reset()
        // Central Main Street
        builder.addBox(0f, 0.02f, 0f, 3.2f, 0.04f, 32.0f, 0.72f, 0.68f, 0.62f)
        // East-West Farmhouse Lane
        builder.addBox(7.0f, 0.02f, 0f, 14.0f, 0.04f, 2.8f, 0.72f, 0.68f, 0.62f)
        // West Barn Lane
        builder.addBox(-6.0f, 0.02f, 6.0f, 12.0f, 0.04f, 2.6f, 0.72f, 0.68f, 0.62f)
        // Market Diagonal Lane
        builder.addBox(-7.0f, 0.02f, -7.0f, 14.0f, 0.04f, 2.6f, 0.72f, 0.68f, 0.62f)
        // North Forest Trail (extending north into Deep Forest)
        builder.addBox(0f, 0.02f, 45.0f, 2.4f, 0.04f, 58.0f, 0.58f, 0.52f, 0.42f)
        // South Wetland Trail (extending south towards fishing docks)
        builder.addBox(0f, 0.02f, -45.0f, 2.4f, 0.04f, 58.0f, 0.55f, 0.50f, 0.45f)
        roadMesh = builder.build()

        // 6. WATER POND (Farmstead Pond)
        builder.reset()
        builder.addCylinder(0f, 0.01f, 0f, 4.9f, 0.14f, 16, 0.52f, 0.50f, 0.48f)
        builder.addCylinder(0f, 0.07f, 0f, 4.5f, 0.04f, 16, 0.12f, 0.72f, 0.92f, 0.92f)
        pondMesh = builder.build()

        // 6B. WETLAND POND (Murky, rich wetland marsh pond)
        builder.reset()
        builder.addCylinder(0f, 0.01f, 0f, 6.2f, 0.16f, 16, 0.28f, 0.38f, 0.32f) // muddy bank
        builder.addCylinder(0f, 0.07f, 0f, 5.8f, 0.04f, 16, 0.08f, 0.52f, 0.60f, 0.95f) // murky wetland water
        wetlandPondMesh = builder.build()

        // 7. GREEN VALLEY RIVER (East stream)
        builder.reset()
        builder.addBox(17.0f, 0.01f, -16.0f, 4.2f, 0.12f, 12.0f, 0.50f, 0.48f, 0.46f)
        builder.addBox(17.0f, 0.06f, -16.0f, 3.6f, 0.04f, 12.0f, 0.10f, 0.68f, 0.90f)
        builder.addBox(16.0f, 0.01f, 0f, 4.4f, 0.12f, 16.0f, 0.50f, 0.48f, 0.46f)
        builder.addBox(16.0f, 0.06f, 0f, 3.8f, 0.04f, 16.0f, 0.10f, 0.68f, 0.90f)
        builder.addBox(18.0f, 0.01f, 14.0f, 4.6f, 0.12f, 14.0f, 0.50f, 0.48f, 0.46f)
        builder.addBox(18.0f, 0.06f, 14.0f, 4.0f, 0.04f, 14.0f, 0.10f, 0.68f, 0.90f)
        riverMesh = builder.build()

        // 8. WETLAND WIDE RIVER (East-to-West, 10m wide across 360m)
        builder.reset()
        builder.addBox(0f, 0.01f, -100.0f, 360.0f, 0.16f, 14.0f, 0.35f, 0.48f, 0.42f) // Sandy Riverbed
        builder.addBox(0f, 0.07f, -100.0f, 360.0f, 0.04f, 11.5f, 0.10f, 0.65f, 0.88f) // Flowing Water
        wetlandRiverMesh = builder.build()

        // 9. WOODEN ARCHED FOOTBRIDGE (Green Valley)
        builder.reset()
        builder.addBox(0f, 0.35f, 0f, 4.8f, 0.16f, 2.8f, 0.65f, 0.42f, 0.24f)
        builder.addBox(0f, 0.85f, -1.35f, 5.0f, 0.10f, 0.12f, 0.55f, 0.35f, 0.20f)
        builder.addBox(0f, 0.85f, 1.35f, 5.0f, 0.10f, 0.12f, 0.55f, 0.35f, 0.20f)
        for (i in -2..2) {
            builder.addBox(i * 1.1f, 0.55f, -1.35f, 0.12f, 0.65f, 0.12f, 0.50f, 0.30f, 0.16f)
            builder.addBox(i * 1.1f, 0.55f, 1.35f, 0.12f, 0.65f, 0.12f, 0.50f, 0.30f, 0.16f)
        }
        woodenBridgeMesh = builder.build()

        // 10. STONE DRINKING WELL
        builder.reset()
        builder.addCylinder(0f, 0.45f, 0f, 0.95f, 0.90f, 10, 0.55f, 0.55f, 0.58f)
        builder.addCylinder(0f, 0.65f, 0f, 0.75f, 0.10f, 10, 0.12f, 0.72f, 0.92f)
        builder.addCylinder(-0.85f, 0f, 0f, 0.08f, 2.20f, 6, 0.48f, 0.32f, 0.18f)
        builder.addCylinder(0.85f, 0f, 0f, 0.08f, 2.20f, 6, 0.48f, 0.32f, 0.18f)
        builder.addRoofPrism(0f, 2.30f, 0f, 2.20f, 0.70f, 1.80f, 0.65f, 0.42f, 0.22f)
        wellMesh = builder.build()

        // 11. LILY PADS & LOTUS
        builder.reset()
        builder.addCylinder(0f, 0.085f, 0f, 0.45f, 0.02f, 8, 0.15f, 0.65f, 0.25f)
        builder.addBox(0f, 0.12f, 0f, 0.16f, 0.08f, 0.16f, 0.98f, 0.55f, 0.75f)
        lilyPadMesh = builder.build()

        // 12. STANDARD PINE TREE (Height = 4.8m)
        builder.reset()
        builder.addCylinder(0f, 0f, 0f, 0.18f, 1.50f, 6, 0.40f, 0.25f, 0.15f) // Warm brown trunk
        builder.addCone(0f, 1.20f, 0f, 1.30f, 1.60f, 7, 0.15f, 0.50f, 0.20f)
        builder.addCone(0f, 2.30f, 0f, 1.05f, 1.50f, 7, 0.15f, 0.55f, 0.20f)
        builder.addCone(0f, 3.40f, 0f, 0.75f, 1.40f, 7, 0.15f, 0.60f, 0.20f)
        pineTreeMesh = builder.build()

        // 13. TALL DEEP FOREST PINE (Height = 7.5m)
        builder.reset()
        builder.addCylinder(0f, 0f, 0f, 0.32f, 2.40f, 6, 0.40f, 0.25f, 0.15f)
        builder.addCone(0f, 2.00f, 0f, 2.10f, 2.40f, 8, 0.10f, 0.40f, 0.15f)
        builder.addCone(0f, 3.80f, 0f, 1.70f, 2.20f, 8, 0.12f, 0.50f, 0.20f)
        builder.addCone(0f, 5.50f, 0f, 1.20f, 2.00f, 8, 0.15f, 0.60f, 0.25f)
        tallForestPineMesh = builder.build()

        // 14. OAK TREE (Height = 4.85m)
        builder.reset()
        builder.addCylinder(0f, 0f, 0f, 0.22f, 1.80f, 6, 0.45f, 0.30f, 0.20f)
        builder.addSphere(0f, 3.20f, 0f, 1.40f, 6, 8, 0.25f, 0.65f, 0.30f)
        builder.addSphere(-0.60f, 2.80f, 0.35f, 1.00f, 5, 7, 0.30f, 0.75f, 0.35f)
        builder.addSphere(0.60f, 2.90f, -0.35f, 0.95f, 5, 7, 0.28f, 0.70f, 0.32f)
        oakTreeMesh = builder.build()

        // 15. ANCIENT GIANT OAK (Height = 8.2m)
        builder.reset()
        builder.addCylinder(0f, 0f, 0f, 0.55f, 3.20f, 8, 0.42f, 0.26f, 0.14f)
        builder.addSphere(0f, 5.20f, 0f, 2.80f, 7, 9, 0.16f, 0.52f, 0.18f)
        builder.addSphere(-1.40f, 4.50f, 0.80f, 1.90f, 6, 8, 0.18f, 0.58f, 0.20f)
        builder.addSphere(1.50f, 4.60f, -0.80f, 1.85f, 6, 8, 0.18f, 0.56f, 0.20f)
        ancientOakMesh = builder.build()

        // 16. CHERRY BLOSSOM TREE
        builder.reset()
        builder.addCylinder(0f, 0f, 0f, 0.20f, 1.70f, 6, 0.48f, 0.32f, 0.20f)
        builder.addSphere(0f, 3.10f, 0f, 1.35f, 6, 8, 0.98f, 0.68f, 0.78f)
        builder.addSphere(-0.55f, 2.70f, 0.30f, 0.90f, 5, 7, 0.96f, 0.78f, 0.84f)
        builder.addSphere(0.55f, 2.75f, -0.30f, 0.90f, 5, 7, 0.95f, 0.60f, 0.72f)
        cherryTreeMesh = builder.build()

        // 17. WEEPING WILLOW TREE
        builder.reset()
        builder.addCylinder(0f, 0f, 0f, 0.24f, 1.90f, 6, 0.44f, 0.28f, 0.18f)
        builder.addSphere(0f, 3.40f, 0f, 1.45f, 6, 8, 0.35f, 0.75f, 0.30f)
        builder.addCone(0f, 1.60f, 0f, 1.70f, 2.10f, 8, 0.28f, 0.68f, 0.25f)
        willowTreeMesh = builder.build()

        // 18. BUSH & WILDFLOWERS & GRASS
        builder.reset()
        builder.addSphere(0f, 0.55f, 0f, 0.75f, 5, 7, 0.18f, 0.58f, 0.22f)
        bushMesh = builder.build()

        builder.reset()
        builder.addBox(-0.3f, 0.20f, -0.2f, 0.16f, 0.16f, 0.16f, 0.95f, 0.20f, 0.20f)
        builder.addBox(0.3f, 0.20f, -0.2f, 0.16f, 0.16f, 0.16f, 0.98f, 0.85f, 0.15f)
        flowerPatchMesh = builder.build()

        builder.reset()
        builder.addBox(0f, 0.22f, 0f, 0.08f, 0.44f, 0.48f, 0.22f, 0.78f, 0.24f)
        builder.addBox(0f, 0.22f, 0f, 0.48f, 0.44f, 0.08f, 0.32f, 0.84f, 0.28f)
        grassTuftMesh = builder.build()

        // 19. DEEP FOREST POI: ABANDONED RANGER TOWER (Height = 10m)
        builder.reset()
        // 4 Log Stilts
        builder.addCylinder(-1.5f, 0f, -1.5f, 0.18f, 8.0f, 6, 0.45f, 0.30f, 0.18f)
        builder.addCylinder(1.5f, 0f, -1.5f, 0.18f, 8.0f, 6, 0.45f, 0.30f, 0.18f)
        builder.addCylinder(-1.5f, 0f, 1.5f, 0.18f, 8.0f, 6, 0.45f, 0.30f, 0.18f)
        builder.addCylinder(1.5f, 0f, 1.5f, 0.18f, 8.0f, 6, 0.45f, 0.30f, 0.18f)
        // Cabin Deck Platform
        builder.addBox(0f, 7.90f, 0f, 4.2f, 0.20f, 4.2f, 0.58f, 0.40f, 0.24f)
        // Lookout Cabin Walls
        builder.addBox(0f, 9.20f, 0f, 3.4f, 2.40f, 3.4f, 0.52f, 0.36f, 0.22f)
        // Roof
        builder.addRoofPrism(0f, 10.70f, 0f, 4.4f, 0.90f, 4.4f, 0.65f, 0.45f, 0.26f)
        // Ladder
        builder.addBox(-1.5f, 3.9f, 1.7f, 0.4f, 7.8f, 0.08f, 0.62f, 0.44f, 0.26f)
        rangerTowerMesh = builder.build()

        // 20. DEEP FOREST POI: OLD CAMPSITE (Tent + Campfire + Crates)
        builder.reset()
        // Canvas A-Frame Tent
        builder.addRoofPrism(0f, 1.10f, 0f, 2.8f, 2.0f, 3.2f, 0.72f, 0.65f, 0.50f)
        // Campfire Stone Ring
        builder.addCylinder(3.2f, 0.10f, 0f, 0.95f, 0.20f, 8, 0.48f, 0.46f, 0.44f)
        builder.addCone(3.2f, 0.20f, 0f, 0.50f, 0.60f, 5, 0.98f, 0.55f, 0.15f) // Fire embers
        // Supply Crates
        builder.addBox(-2.2f, 0.45f, 1.8f, 0.85f, 0.85f, 0.85f, 0.58f, 0.42f, 0.26f)
        builder.addBox(-2.2f, 0.35f, 0.8f, 0.70f, 0.70f, 0.70f, 0.52f, 0.36f, 0.22f)
        campsiteMesh = builder.build()

        // 21. DEEP FOREST POI: MYSTERIOUS STONE CIRCLE (6 Megaliths + Altar)
        builder.reset()
        // 6 Megaliths in 6.5m radius circle
        for (i in 0 until 6) {
            val angle = (i * 60.0 * Math.PI / 180.0).toFloat()
            val mx = cos(angle) * 6.5f
            val mz = sin(angle) * 6.5f
            builder.addBox(mx, 1.80f, mz, 0.90f, 3.60f, 0.65f, 0.48f, 0.50f, 0.52f)
            // Emerald Rune Inscription
            builder.addBox(mx * 0.95f, 1.80f, mz * 0.95f, 0.20f, 1.20f, 0.20f, 0.10f, 0.95f, 0.55f)
        }
        // Center Altar Slab
        builder.addBox(0f, 0.45f, 0f, 2.2f, 0.90f, 1.5f, 0.42f, 0.44f, 0.46f)
        stoneCircleMesh = builder.build()

        // 22. DEEP FOREST DECOR (Mossy Rocks, Fallen Logs, Mushrooms, Herbs)
        builder.reset()
        builder.addSphere(0f, 0.75f, 0f, 1.25f, 5, 7, 0.45f, 0.48f, 0.46f)
        builder.addSphere(0f, 1.45f, 0f, 0.90f, 5, 6, 0.22f, 0.62f, 0.24f) // Moss Cap
        mossyRockMesh = builder.build()

        builder.reset()
        builder.addCylinder(0f, 0.35f, 0f, 0.38f, 4.5f, 6, 0.42f, 0.28f, 0.16f) // Fallen Log
        fallenLogMesh = builder.build()

        builder.reset()
        // Glowing Medicinal Herb
        builder.addBox(0f, 0.25f, 0f, 0.35f, 0.50f, 0.35f, 0.20f, 0.98f, 0.45f)
        builder.addSphere(0f, 0.45f, 0f, 0.20f, 4, 5, 0.40f, 1.0f, 0.60f)
        medicinalHerbMesh = builder.build()

        builder.reset()
        // Mushrooms (Chanterelle Gold, Bioluminescent Purple, Red Spotted)
        builder.addCone(-0.25f, 0.15f, 0f, 0.20f, 0.18f, 6, 0.98f, 0.78f, 0.15f)
        builder.addCone(0.25f, 0.20f, 0.15f, 0.25f, 0.22f, 6, 0.70f, 0.25f, 0.95f)
        builder.addCone(0f, 0.18f, -0.25f, 0.22f, 0.20f, 6, 0.95f, 0.15f, 0.15f)
        forestMushroomsMesh = builder.build()

        // 23. WETLAND POI: OLD FISHING HUT & DOCK
        builder.reset()
        // Stilt Shack (4 stilts + cabin)
        builder.addCylinder(-1.4f, 0f, -1.4f, 0.14f, 3.5f, 6, 0.42f, 0.30f, 0.18f)
        builder.addCylinder(1.4f, 0f, -1.4f, 0.14f, 3.5f, 6, 0.42f, 0.30f, 0.18f)
        builder.addCylinder(-1.4f, 0f, 1.4f, 0.14f, 3.5f, 6, 0.42f, 0.30f, 0.18f)
        builder.addCylinder(1.4f, 0f, 1.4f, 0.14f, 3.5f, 6, 0.42f, 0.30f, 0.18f)
        builder.addBox(0f, 3.5f, 0f, 3.6f, 2.2f, 3.6f, 0.52f, 0.38f, 0.24f)
        builder.addRoofPrism(0f, 4.8f, 0f, 4.0f, 0.9f, 4.0f, 0.62f, 0.44f, 0.28f)
        // Fishing Dock extending into water
        builder.addBox(0f, 0.25f, -4.5f, 2.2f, 0.16f, 6.5f, 0.58f, 0.42f, 0.26f)
        fishingHutMesh = builder.build()

        // 24. WETLAND POI: BROKEN TIMBER BRIDGE
        builder.reset()
        // Stone Piers
        builder.addBox(-4.0f, 0.5f, 0f, 1.8f, 1.2f, 3.2f, 0.50f, 0.52f, 0.54f)
        builder.addBox(4.0f, 0.5f, 0f, 1.8f, 1.2f, 3.2f, 0.50f, 0.52f, 0.54f)
        // Fractured Planks
        builder.addBox(-2.5f, 1.0f, 0f, 2.2f, 0.14f, 2.8f, 0.55f, 0.38f, 0.22f)
        builder.addBox(2.5f, 1.0f, 0f, 2.2f, 0.14f, 2.8f, 0.55f, 0.38f, 0.22f)
        brokenBridgeMesh = builder.build()

        // 25. WETLAND POI: SUNKEN SUPPLY BOAT
        builder.reset()
        // Half-submerged wooden skiff
        builder.addBox(0f, 0.15f, 0f, 1.6f, 0.65f, 4.5f, 0.48f, 0.34f, 0.20f)
        builder.addCylinder(1.6f, 0.10f, 0.8f, 0.35f, 0.65f, 6, 0.58f, 0.42f, 0.26f) // Floating Barrel
        builder.addBox(-1.5f, 0.15f, -0.6f, 0.60f, 0.60f, 0.60f, 0.52f, 0.38f, 0.24f) // Floating Crate
        sunkenBoatMesh = builder.build()

        // 26. WETLAND CATTAILS & MARSH REEDS
        builder.reset()
        for (i in -2..2) {
            builder.addCylinder(i * 0.3f, 0.6f, i * 0.2f, 0.03f, 1.2f, 4, 0.22f, 0.55f, 0.25f)
            builder.addCylinder(i * 0.3f, 1.1f, i * 0.2f, 0.07f, 0.4f, 4, 0.45f, 0.28f, 0.15f) // Brown cattail head
        }
        cattailsReedsMesh = builder.build()

        // 27. GREEN VALLEY POI: CRYSTAL RESOURCE CAVE
        builder.reset()
        // Rock Archway
        builder.addSphere(0f, 1.8f, 0f, 3.2f, 6, 8, 0.45f, 0.45f, 0.48f)
        builder.addBox(0f, 1.2f, 0.8f, 2.2f, 2.4f, 1.8f, 0.10f, 0.10f, 0.12f) // Dark interior opening
        // Glowing Quartz Crystal Spikes
        builder.addCone(-1.4f, 0.6f, 1.2f, 0.20f, 1.4f, 5, 0.40f, 0.88f, 1.0f)
        builder.addCone(1.3f, 0.5f, 1.0f, 0.22f, 1.2f, 5, 0.85f, 0.45f, 1.0f)
        caveEntranceMesh = builder.build()

        // 28. CELESTIAL MESHES (Sun, Moon, Lightning)
        builder.reset()
        builder.addSphere(0f, 0f, 0f, 2.6f, 8, 10, 1.0f, 0.94f, 0.40f)
        sunMesh = builder.build()

        builder.reset()
        builder.addSphere(0f, 0f, 0f, 2.1f, 8, 10, 0.88f, 0.92f, 1.0f)
        builder.addSphere(-0.5f, 0.4f, 1.8f, 0.4f, 4, 5, 0.70f, 0.75f, 0.85f)
        builder.addSphere(0.6f, -0.3f, 1.7f, 0.5f, 4, 5, 0.70f, 0.75f, 0.85f)
        moonMesh = builder.build()

        builder.reset()
        builder.addBox(0f, 18.0f, 0f, 0.35f, 5.0f, 0.35f, 0.90f, 0.96f, 1.0f)
        builder.addBox(-1.2f, 13.5f, 0f, 0.35f, 5.0f, 0.35f, 0.90f, 0.96f, 1.0f)
        builder.addBox(0.8f, 8.5f, 0f, 0.30f, 5.5f, 0.30f, 0.90f, 0.96f, 1.0f)
        builder.addBox(-0.5f, 3.5f, 0f, 0.25f, 5.0f, 0.25f, 0.90f, 0.96f, 1.0f)
        lightningBoltMesh = builder.build()

        // 29. BIRDS, BUTTERFLIES, BEES
        builder.reset()
        builder.addBox(0f, 0f, 0f, 0.22f, 0.18f, 0.55f, 0.95f, 0.96f, 0.98f)
        builder.addCone(0f, 0f, 0.32f, 0.08f, 0.20f, 4, 0.98f, 0.70f, 0.15f)
        birdBodyMesh = builder.build()

        builder.reset()
        builder.addBox(0.40f, 0f, 0f, 0.80f, 0.04f, 0.35f, 0.88f, 0.92f, 0.98f)
        birdWingMesh = builder.build()

        builder.reset()
        builder.addBox(0f, 0f, 0f, 0.06f, 0.06f, 0.22f, 0.18f, 0.15f, 0.15f)
        builder.addBox(-0.20f, 0.02f, 0f, 0.35f, 0.02f, 0.26f, 0.98f, 0.55f, 0.12f)
        builder.addBox(0.20f, 0.02f, 0f, 0.35f, 0.02f, 0.26f, 0.00f, 0.85f, 0.95f)
        butterflyMesh = builder.build()

        builder.reset()
        builder.addBox(0f, 0f, 0f, 0.10f, 0.10f, 0.14f, 0.98f, 0.82f, 0.10f)
        builder.addBox(0f, 0f, 0.02f, 0.105f, 0.105f, 0.04f, 0.15f, 0.12f, 0.10f)
        builder.addBox(-0.06f, 0.06f, 0f, 0.10f, 0.01f, 0.08f, 0.90f, 0.95f, 1.0f, 0.85f)
        builder.addBox(0.06f, 0.06f, 0f, 0.10f, 0.01f, 0.08f, 0.90f, 0.95f, 1.0f, 0.85f)
        beeMesh = builder.build()
    }

    fun drawEnvironment(
        shader: GLShader,
        viewMatrix: FloatArray,
        projMatrix: FloatArray,
        animTime: Float = 0.0f,
        hour: Float = 12.0f,
        weather: WeatherType = WeatherType.SUNNY_CLEAR
    ) {
        // 1. 3 Connected Biome Terrains (Green Valley, Deep Forest, Wetland)
        Matrix.setIdentityM(modelMatrix, 0)
        renderMesh(shader, valleyGroundMesh, modelMatrix, viewMatrix, projMatrix)
        renderMesh(shader, forestGroundMesh, modelMatrix, viewMatrix, projMatrix)
        renderMesh(shader, wetlandGroundMesh, modelMatrix, viewMatrix, projMatrix)
        renderMesh(shader, worldBoundaryMesh, modelMatrix, viewMatrix, projMatrix)

        // 2. Pathways & Roads across Biomes
        renderMesh(shader, roadMesh, modelMatrix, viewMatrix, projMatrix)

        // 3. Waterways & Rivers
        renderMesh(shader, riverMesh, modelMatrix, viewMatrix, projMatrix)
        renderMesh(shader, wetlandRiverMesh, modelMatrix, viewMatrix, projMatrix)

        // Green Valley Pond
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 10.0f, 0f, -10.0f)
        renderMesh(shader, pondMesh, modelMatrix, viewMatrix, projMatrix)

        // Wetland Lily Pond (X=20, Z=-120)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 20.0f, 0f, -120.0f)
        renderMesh(shader, wetlandPondMesh, modelMatrix, viewMatrix, projMatrix)

        // Green Valley Stone Well
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 8.0f, 0f, -3.5f)
        renderMesh(shader, wellMesh, modelMatrix, viewMatrix, projMatrix)

        // Green Valley Bridge
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 16.0f, 0f, 0f)
        renderMesh(shader, woodenBridgeMesh, modelMatrix, viewMatrix, projMatrix)

        // 4. Points of Interest across Biomes
        // A. Deep Forest: Ranger Tower (X=14, Z=110)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 14.0f, 0f, 110.0f)
        renderMesh(shader, rangerTowerMesh, modelMatrix, viewMatrix, projMatrix)

        // B. Deep Forest: Old Campsite (X=-32, Z=85)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, -32.0f, 0f, 85.0f)
        renderMesh(shader, campsiteMesh, modelMatrix, viewMatrix, projMatrix)

        // C. Deep Forest: Mysterious Stone Circle (X=-12, Z=148)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, -12.0f, 0f, 148.0f)
        renderMesh(shader, stoneCircleMesh, modelMatrix, viewMatrix, projMatrix, emissionG = 0.35f)

        // D. Wetland: Old Fishing Hut & Dock (X=-24, Z=-95)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, -24.0f, 0f, -95.0f)
        renderMesh(shader, fishingHutMesh, modelMatrix, viewMatrix, projMatrix)

        // E. Wetland: Broken Timber Bridge (X=16, Z=-75)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 16.0f, 0f, -75.0f)
        renderMesh(shader, brokenBridgeMesh, modelMatrix, viewMatrix, projMatrix)

        // F. Wetland: Sunken Supply Boat (X=35, Z=-135)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 35.0f, 0f, -135.0f)
        Matrix.rotateM(modelMatrix, 0, 18.0f, 0f, 0f, 1f) // tilted in marsh
        renderMesh(shader, sunkenBoatMesh, modelMatrix, viewMatrix, projMatrix)

        // G. Green Valley: Crystal Resource Cave (X=-28, Z=20)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, -28.0f, 0f, 20.0f)
        renderMesh(shader, caveEntranceMesh, modelMatrix, viewMatrix, projMatrix, emissionB = 0.30f)

        // 5. Forest & Wetland Details (Mossy Rocks, Fallen Logs, Mushrooms, Herbs, Cattails)
        // Forest Rocks & Logs
        val rockPositions = listOf(Pair(6.0f, 75.0f), Pair(-22.0f, 105.0f), Pair(25.0f, 135.0f), Pair(-18.0f, 160.0f))
        for (pos in rockPositions) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, pos.first, 0f, pos.second)
            renderMesh(shader, mossyRockMesh, modelMatrix, viewMatrix, projMatrix)
        }

        val logPositions = listOf(Pair(-15.0f, 62.0f), Pair(18.0f, 88.0f), Pair(-8.0f, 125.0f))
        for (pos in logPositions) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, pos.first, 0f, pos.second)
            Matrix.rotateM(modelMatrix, 0, 45f, 0f, 1f, 0f)
            renderMesh(shader, fallenLogMesh, modelMatrix, viewMatrix, projMatrix)
        }

        // Glowing Medicinal Herbs in Forest
        val herbPositions = listOf(Pair(2.0f, 68.0f), Pair(-18.0f, 92.0f), Pair(8.0f, 140.0f), Pair(-28.0f, 155.0f))
        for (pos in herbPositions) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, pos.first, 0f, pos.second)
            renderMesh(shader, medicinalHerbMesh, modelMatrix, viewMatrix, projMatrix, emissionG = 0.55f)
        }

        // Mushroom Patches
        val shroomPositions = listOf(Pair(-8.0f, 58.0f), Pair(12.0f, 98.0f), Pair(-16.0f, 138.0f))
        for (pos in shroomPositions) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, pos.first, 0f, pos.second)
            renderMesh(shader, forestMushroomsMesh, modelMatrix, viewMatrix, projMatrix, emissionR = 0.25f, emissionB = 0.40f)
        }

        // Wetland Cattails & Reeds along river
        val reedPositions = listOf(
            Pair(-35.0f, -94.0f), Pair(-18.0f, -106.0f), Pair(5.0f, -93.0f), Pair(25.0f, -107.0f),
            Pair(45.0f, -95.0f), Pair(-10.0f, -12.0f), Pair(14.0f, -8.0f)
        )
        for (pos in reedPositions) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, pos.first, 0f, pos.second)
            renderMesh(shader, cattailsReedsMesh, modelMatrix, viewMatrix, projMatrix)
        }

        // 6. Trees across all 3 Biomes
        for ((idx, tree) in worldTreePositions.withIndex()) {
            val tx = tree.first
            val tz = tree.second
            val tType = tree.third

            // Shadow
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, tx, 0f, tz)
            renderMesh(shader, treeShadowMesh, modelMatrix, viewMatrix, projMatrix)

            val windSway = sin(animTime * 1.5f + idx * 0.4f) * 1.2f
            Matrix.rotateM(modelMatrix, 0, windSway, 0f, 0f, 1f)

            val mesh = when (tType) {
                0 -> pineTreeMesh
                1 -> oakTreeMesh
                2 -> cherryTreeMesh
                3 -> willowTreeMesh
                4 -> ancientOakMesh
                5 -> tallForestPineMesh
                else -> pineTreeMesh
            }
            renderMesh(shader, mesh, modelMatrix, viewMatrix, projMatrix)
        }

        // 7. Celestial Sun & Moon
        val sunAngle = ((hour - 6.0f) / 12.0f) * Math.PI.toFloat()
        val sunX = cos(sunAngle) * 38.0f
        val sunY = sin(sunAngle) * 28.0f
        val sunZ = sin(sunAngle * 0.5f) * 12.0f
        if (sunY > -4.0f) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, sunX, sunY, sunZ)
            renderMesh(shader, sunMesh, modelMatrix, viewMatrix, projMatrix, emissionR = 0.98f, emissionG = 0.90f, emissionB = 0.35f)
        }

        val moonAngle = sunAngle + Math.PI.toFloat()
        val moonX = cos(moonAngle) * 38.0f
        val moonY = sin(moonAngle) * 28.0f
        val moonZ = sin(moonAngle * 0.5f) * 12.0f
        if (moonY > -4.0f) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, moonX, moonY, moonZ)
            renderMesh(shader, moonMesh, modelMatrix, viewMatrix, projMatrix, emissionR = 0.70f, emissionG = 0.75f, emissionB = 0.95f)
        }

        // 8. Birds, Butterflies, Bees
        drawAmbientFloraAndFauna(shader, viewMatrix, projMatrix, animTime)
    }

    private fun drawAmbientFloraAndFauna(
        shader: GLShader,
        viewMatrix: FloatArray,
        projMatrix: FloatArray,
        animTime: Float
    ) {
        // Birds circling in sky
        for (i in 0 until 3) {
            val birdAngle = animTime * 0.35f + (i * 2.09f)
            val bx = cos(birdAngle) * (20.0f + i * 4.0f)
            val bz = sin(birdAngle) * (20.0f + i * 4.0f)
            val by = 14.0f + sin(animTime * 1.5f + i) * 1.5f
            val wingFlap = sin(animTime * 12.0f + i * 2f) * 35.0f

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, bx, by, bz)
            Matrix.rotateM(modelMatrix, 0, -Math.toDegrees(birdAngle.toDouble()).toFloat() - 90f, 0f, 1f, 0f)
            renderMesh(shader, birdBodyMesh, modelMatrix, viewMatrix, projMatrix)

            val leftWing = FloatArray(16)
            Matrix.multiplyMM(leftWing, 0, modelMatrix, 0, leftWing, 0)
            Matrix.rotateM(leftWing, 0, wingFlap, 0f, 0f, 1f)
            renderMesh(shader, birdWingMesh, leftWing, viewMatrix, projMatrix)
        }

        // Butterflies near flowers
        val butterFlyOrigins = listOf(Pair(-4.0f, -2.0f), Pair(6.0f, 4.0f), Pair(-8.0f, 10.0f))
        for ((idx, bPos) in butterFlyOrigins.withIndex()) {
            val flutterX = cos(animTime * 2.5f + idx) * 1.6f
            val flutterZ = sin(animTime * 2.0f + idx) * 1.6f
            val flutterY = 0.45f + kotlin.math.abs(sin(animTime * 4.0f + idx)) * 0.4f
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, bPos.first + flutterX, flutterY, bPos.second + flutterZ)
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
        GLES20.glUniform1f(shader.uShininessLoc, 8.0f)

        mesh.draw(shader)
    }
}
