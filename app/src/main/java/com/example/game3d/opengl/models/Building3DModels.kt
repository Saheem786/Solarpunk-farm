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
    val wellMesh: GLMesh
    val compostBinMesh: GLMesh
    val chickenCoopMesh: GLMesh

    // Phase 3: Water, Irrigation & Fishing Meshes
    val rainBarrelMesh: GLMesh
    val waterFilterMesh: GLMesh
    val waterPurifierMesh: GLMesh
    val irrigationPipeMesh: GLMesh
    val irrigationNodeMesh: GLMesh
    val waterStorageMesh: GLMesh
    val fishingBobberMesh: GLMesh

    // Phase 4: Energy Grid, Batteries & Power Generators
    val hydroGeneratorMesh: GLMesh
    val hydroWaterWheelMesh: GLMesh
    val advancedSolarMesh: GLMesh
    val biogasGeneratorMesh: GLMesh
    val geothermalVentMesh: GLMesh
    val basicBatteryMesh: GLMesh
    val advancedBatteryMesh: GLMesh
    val batteryBankMesh: GLMesh
    val powerPoleMesh: GLMesh
    val powerCableSegmentMesh: GLMesh

    // Phase 5: NPC Settlement Buildings
    val npcCabinMesh: GLMesh
    val bunkhouseMesh: GLMesh
    val kitchenMesh: GLMesh
    val medicStationMesh: GLMesh
    val researchLabMesh: GLMesh

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

        // 1. FARM HOUSE / CABIN (Height = 3.5 units)
        builder.reset()
        // Stone Foundation
        builder.addBox(0f, 0.15f, 0f, 7.4f, 0.30f, 5.2f, 0.48f, 0.48f, 0.50f)
        // Timber/Clapboard Walls
        builder.addBox(0f, 1.30f, 0f, 7.0f, 2.00f, 4.8f, 0.92f, 0.88f, 0.78f)
        // Pitched Roof
        builder.addRoofPrism(0f, 2.30f, 0f, 7.6f, 1.20f, 5.4f, 0.82f, 0.38f, 0.22f)
        // Chimney
        builder.addBox(2.2f, 3.10f, -0.8f, 0.70f, 1.00f, 0.70f, 0.65f, 0.28f, 0.22f)
        // Front Door & Windows
        builder.addBox(0f, 0.90f, 2.42f, 1.10f, 1.50f, 0.08f, 0.42f, 0.26f, 0.16f)
        builder.addBox(-1.8f, 1.25f, 2.42f, 1.20f, 0.80f, 0.06f, 0.98f, 0.92f, 0.50f)
        builder.addBox(1.8f, 1.25f, 2.42f, 1.20f, 0.80f, 0.06f, 0.98f, 0.92f, 0.50f)
        // Covered Porch
        builder.addBox(0f, 0.15f, 3.30f, 5.4f, 0.30f, 1.8f, 0.55f, 0.38f, 0.24f)
        builder.addCylinder(-2.4f, 0.30f, 4.0f, 0.12f, 1.60f, 6, 0.88f, 0.85f, 0.78f)
        builder.addCylinder(2.4f, 0.30f, 4.0f, 0.12f, 1.60f, 6, 0.88f, 0.85f, 0.78f)
        builder.addRoofPrism(0f, 1.90f, 3.30f, 5.6f, 0.60f, 2.0f, 0.82f, 0.38f, 0.22f)
        farmhouseMesh = builder.build()

        // 2. RUSTIC BARN (Height = 3.8 units)
        builder.reset()
        builder.addBox(0f, 1.10f, 0f, 7.2f, 2.20f, 5.6f, 0.78f, 0.20f, 0.18f)
        builder.addBox(-3.55f, 1.10f, -2.75f, 0.25f, 2.25f, 0.25f, 0.95f, 0.95f, 0.95f)
        builder.addBox(3.55f, 1.10f, -2.75f, 0.25f, 2.25f, 0.25f, 0.95f, 0.95f, 0.95f)
        builder.addBox(-3.55f, 1.10f, 2.75f, 0.25f, 2.25f, 0.25f, 0.95f, 0.95f, 0.95f)
        builder.addBox(3.55f, 1.10f, 2.75f, 0.25f, 2.25f, 0.25f, 0.95f, 0.95f, 0.95f)
        builder.addRoofPrism(0f, 2.20f, 0f, 7.6f, 1.60f, 6.0f, 0.28f, 0.30f, 0.34f)
        builder.addBox(0f, 1.00f, 2.82f, 2.4f, 2.00f, 0.08f, 0.95f, 0.95f, 0.95f)
        barnMesh = builder.build()

        // 3. ARTISAN ECO-WORKSHOP (Height = 3.5 units)
        builder.reset()
        builder.addBox(0f, 1.10f, 0f, 5.8f, 2.20f, 4.6f, 0.88f, 0.92f, 0.88f)
        builder.addRoofPrism(0f, 2.20f, 0f, 6.2f, 1.30f, 5.0f, 0.10f, 0.65f, 0.85f)
        builder.addBox(0f, 1.00f, -2.32f, 2.4f, 2.00f, 0.06f, 0.40f, 0.50f, 0.55f)
        workshopMesh = builder.build()

        // 4. GREENHOUSE / BIO-DOME (Height = 3.0 units)
        builder.reset()
        // Low concrete wall base
        builder.addBox(0f, 0.15f, 0f, 5.2f, 0.30f, 4.4f, 0.45f, 0.50f, 0.52f)
        // Geodesic Glass Arches / Dome (Translucent Cyan: 0.15, 0.80, 0.90)
        builder.addBox(0f, 1.15f, 0f, 4.8f, 1.70f, 4.0f, 0.15f, 0.80f, 0.90f)
        // Metal Structural Frame Trusses (Emerald: 0.0, 0.85, 0.45)
        builder.addBox(-2.38f, 1.15f, 0f, 0.12f, 1.75f, 4.02f, 0.00f, 0.85f, 0.45f)
        builder.addBox(2.38f, 1.15f, 0f, 0.12f, 1.75f, 4.02f, 0.00f, 0.85f, 0.45f)
        builder.addBox(0f, 1.15f, -1.98f, 4.82f, 1.75f, 0.12f, 0.00f, 0.85f, 0.45f)
        builder.addBox(0f, 1.15f, 1.98f, 4.82f, 1.75f, 0.12f, 0.00f, 0.85f, 0.45f)
        // Slanted Glass Roof Cap
        builder.addRoofPrism(0f, 2.00f, 0f, 5.0f, 1.00f, 4.2f, 0.20f, 0.88f, 0.95f)
        // Interior Hydroponic Planters
        builder.addBox(-1.2f, 0.45f, 0f, 1.2f, 0.30f, 2.8f, 0.22f, 0.75f, 0.25f)
        builder.addBox(1.2f, 0.45f, 0f, 1.2f, 0.30f, 2.8f, 0.22f, 0.75f, 0.25f)
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

        // 9. WATER WELL
        builder.reset()
        builder.addBox(0f, 0.40f, 0f, 2.0f, 0.80f, 2.0f, 0.50f, 0.50f, 0.50f)
        builder.addBox(-0.8f, 1.30f, 0f, 0.15f, 1.80f, 0.15f, 0.60f, 0.40f, 0.20f)
        builder.addBox(0.8f, 1.30f, 0f, 0.15f, 1.80f, 0.15f, 0.60f, 0.40f, 0.20f)
        builder.addRoofPrism(0f, 2.20f, 0f, 2.4f, 0.70f, 1.8f, 0.82f, 0.38f, 0.22f)
        wellMesh = builder.build()

        // 10. COMPOST BIN
        builder.reset()
        builder.addBox(0f, 0.50f, 0.90f, 1.8f, 1.00f, 0.10f, 0.45f, 0.35f, 0.25f)
        builder.addBox(0f, 0.50f, -0.90f, 1.8f, 1.00f, 0.10f, 0.45f, 0.35f, 0.25f)
        builder.addBox(0.90f, 0.50f, 0f, 0.10f, 1.00f, 1.8f, 0.45f, 0.35f, 0.25f)
        builder.addBox(-0.90f, 0.50f, 0f, 0.10f, 1.00f, 1.8f, 0.45f, 0.35f, 0.25f)
        builder.addBox(0f, 0.30f, 0f, 1.6f, 0.60f, 1.6f, 0.25f, 0.18f, 0.10f)
        compostBinMesh = builder.build()

        // 11. CHICKEN COOP
        builder.reset()
        // Wooden base posts
        builder.addBox(-0.55f, 0.15f, -0.55f, 0.12f, 0.30f, 0.12f, 0.45f, 0.30f, 0.15f)
        builder.addBox(0.55f, 0.15f, -0.55f, 0.12f, 0.30f, 0.12f, 0.45f, 0.30f, 0.15f)
        builder.addBox(-0.55f, 0.15f, 0.55f, 0.12f, 0.30f, 0.12f, 0.45f, 0.30f, 0.15f)
        builder.addBox(0.55f, 0.15f, 0.55f, 0.12f, 0.30f, 0.12f, 0.45f, 0.30f, 0.15f)
        // Red Barn-wood Hen House
        builder.addBox(0f, 0.75f, 0f, 1.35f, 0.90f, 1.35f, 0.82f, 0.22f, 0.18f)
        // Pitched Roof
        builder.addRoofPrism(0f, 1.30f, 0f, 1.55f, 0.45f, 1.55f, 0.22f, 0.48f, 0.45f)
        // Little chicken door & wooden ramp
        builder.addBox(0f, 0.55f, 0.69f, 0.36f, 0.45f, 0.04f, 0.35f, 0.22f, 0.12f)
        builder.addBox(0f, 0.16f, 0.95f, 0.30f, 0.04f, 0.60f, 0.62f, 0.45f, 0.28f)
        chickenCoopMesh = builder.build()

        // 12. RAIN BARREL
        builder.reset()
        // Wooden Staves Barrel
        builder.addCylinder(0f, 0.55f, 0f, 0.52f, 1.10f, 12, 0.48f, 0.32f, 0.18f)
        // Iron Metal Hoops
        builder.addCylinder(0f, 0.25f, 0f, 0.54f, 0.05f, 12, 0.22f, 0.24f, 0.26f)
        builder.addCylinder(0f, 0.85f, 0f, 0.54f, 0.05f, 12, 0.22f, 0.24f, 0.26f)
        // Water Basin Surface
        builder.addCylinder(0f, 1.05f, 0f, 0.48f, 0.04f, 12, 0.12f, 0.65f, 0.88f)
        // Brass Spigot Tap
        builder.addCylinder(0f, 0.25f, 0.56f, 0.04f, 0.14f, 6, 0.85f, 0.68f, 0.25f)
        rainBarrelMesh = builder.build()

        // 13. WATER FILTER
        builder.reset()
        // Ceramic filtration vessel
        builder.addCylinder(0f, 0.40f, 0f, 0.42f, 0.70f, 10, 0.82f, 0.84f, 0.82f)
        // Upper funnel chamber
        builder.addCylinder(0f, 0.90f, 0f, 0.46f, 0.40f, 10, 0.25f, 0.65f, 0.70f)
        // Clean water spigot
        builder.addCylinder(0f, 0.20f, 0.45f, 0.035f, 0.12f, 6, 0.85f, 0.72f, 0.20f)
        waterFilterMesh = builder.build()

        // 14. WATER PURIFIER (High-tech UV Solarpunk Purifier)
        builder.reset()
        // Alloy Base & Stand
        builder.addBox(0f, 0.15f, 0f, 1.10f, 0.30f, 1.10f, 0.32f, 0.36f, 0.40f)
        // Glass Sterilization Cylinder
        builder.addCylinder(0f, 0.90f, 0f, 0.38f, 1.20f, 10, 0.15f, 0.85f, 0.95f)
        // Glowing UV Core Lamp
        builder.addCylinder(0f, 0.90f, 0f, 0.14f, 1.10f, 8, 0.40f, 0.95f, 1.0f)
        // Micro-solar canopy on top
        builder.addBox(0f, 1.55f, 0f, 0.80f, 0.08f, 0.80f, 0.10f, 0.65f, 0.85f)
        waterPurifierMesh = builder.build()

        // 15. IRRIGATION PIPE
        builder.reset()
        // Copper Pipe Segment laying flat on ground
        builder.addBox(0f, 0.06f, 0f, 0.12f, 0.12f, 1.0f, 0.76f, 0.44f, 0.24f)
        // Pipe coupling collar
        builder.addBox(0f, 0.06f, 0f, 0.16f, 0.16f, 0.18f, 0.88f, 0.72f, 0.28f)
        irrigationPipeMesh = builder.build()

        // 16. IRRIGATION NODE (Sprinkler)
        builder.reset()
        // Ground spike & riser pipe
        builder.addCylinder(0f, 0.22f, 0f, 0.05f, 0.44f, 6, 0.76f, 0.44f, 0.24f)
        // Brass sprinkler head
        builder.addCylinder(0f, 0.46f, 0f, 0.12f, 0.08f, 8, 0.95f, 0.80f, 0.25f)
        // Dual spray nozzles
        builder.addBox(0f, 0.46f, 0f, 0.32f, 0.04f, 0.06f, 0.92f, 0.75f, 0.20f)
        irrigationNodeMesh = builder.build()

        // 17. WATER STORAGE SHED (Large Cistern)
        builder.reset()
        // Large Corrugated Steel Cistern Tank
        builder.addCylinder(0f, 1.10f, 0f, 1.25f, 2.20f, 14, 0.18f, 0.50f, 0.68f)
        // Dome Lid
        builder.addCone(0f, 2.20f, 0f, 1.30f, 0.40f, 14, 0.22f, 0.55f, 0.72f)
        // Inspection hatch
        builder.addCylinder(0.45f, 2.35f, 0f, 0.28f, 0.12f, 8, 0.85f, 0.90f, 0.92f)
        // Water Level Gauge Tube
        builder.addCylinder(1.26f, 1.10f, 0f, 0.04f, 1.80f, 6, 0.10f, 0.90f, 1.0f)
        waterStorageMesh = builder.build()

        // 18. FISHING BOBBER
        builder.reset()
        // Red Top Hemisphere
        builder.addSphere(0f, 0.06f, 0f, 0.12f, 6, 8, 0.95f, 0.18f, 0.18f)
        // White Bottom Hemisphere
        builder.addSphere(0f, -0.04f, 0f, 0.12f, 6, 8, 0.95f, 0.95f, 0.95f)
        // Little Antenna
        builder.addCylinder(0f, 0.12f, 0f, 0.015f, 0.10f, 4, 1.0f, 0.90f, 0.20f)
        fishingBobberMesh = builder.build()

        // 19. SMALL HYDRO GENERATOR BASE
        builder.reset()
        // Stone foundation flume with water channel
        builder.addBox(0f, 0.20f, 0f, 2.8f, 0.40f, 2.0f, 0.38f, 0.42f, 0.44f)
        builder.addBox(-0.6f, 0.15f, 0f, 1.4f, 0.10f, 2.02f, 0.12f, 0.65f, 0.88f) // Flowing water flume
        // Turbine generator housing
        builder.addBox(0.65f, 0.85f, 0f, 1.10f, 1.10f, 1.40f, 0.18f, 0.52f, 0.58f)
        builder.addCylinder(0.65f, 1.45f, 0f, 0.18f, 0.35f, 8, 0.85f, 0.75f, 0.30f) // Brass heat sink
        builder.addBox(1.22f, 0.85f, 0f, 0.05f, 0.30f, 0.30f, 0.00f, 0.95f, 0.45f) // Green power LED
        hydroGeneratorMesh = builder.build()

        // 19B. HYDRO WATER WHEEL (Spinning rotor)
        builder.reset()
        // Wheel hub axle
        builder.addCylinder(0f, 0f, 0f, 0.12f, 1.20f, 8, 0.48f, 0.35f, 0.22f)
        // 8 Water paddles radiating outward
        for (i in 0 until 8) {
            val angleRad = (i * Math.PI / 4.0).toFloat()
            val px = kotlin.math.cos(angleRad) * 0.55f
            val py = kotlin.math.sin(angleRad) * 0.55f
            builder.addBox(px, py, 0f, 0.30f, 0.05f, 1.0f, 0.62f, 0.45f, 0.28f)
        }
        hydroWaterWheelMesh = builder.build()

        // 20. ADVANCED SOLAR ARRAY (4x4 with tracking gimbal)
        builder.reset()
        // Foundation pad
        builder.addBox(0f, 0.12f, 0f, 3.8f, 0.24f, 3.8f, 0.45f, 0.48f, 0.50f)
        // Central tracking pedestal
        builder.addCylinder(0f, 0.60f, 0f, 0.24f, 0.80f, 10, 0.28f, 0.32f, 0.36f)
        builder.addSphere(0f, 1.05f, 0f, 0.28f, 8, 8, 0.85f, 0.70f, 0.25f) // Dual-axis gimbal ball
        // Actuator hydraulic struts
        builder.addCylinder(-0.6f, 0.60f, -0.6f, 0.06f, 0.75f, 6, 0.65f, 0.68f, 0.72f)
        builder.addCylinder(0.6f, 0.60f, 0.6f, 0.06f, 0.75f, 6, 0.65f, 0.68f, 0.72f)
        // 4 Large high-efficiency photovoltaic glass panels
        builder.addBox(-0.95f, 1.25f, -0.95f, 1.75f, 0.08f, 1.75f, 0.08f, 0.42f, 0.78f)
        builder.addBox(0.95f, 1.25f, -0.95f, 1.75f, 0.08f, 1.75f, 0.08f, 0.42f, 0.78f)
        builder.addBox(-0.95f, 1.25f, 0.95f, 1.75f, 0.08f, 1.75f, 0.08f, 0.42f, 0.78f)
        builder.addBox(0.95f, 1.25f, 0.95f, 1.75f, 0.08f, 1.75f, 0.08f, 0.42f, 0.78f)
        // Gold tracking contact trims
        builder.addBox(0f, 1.27f, 0f, 3.82f, 0.03f, 0.08f, 1.0f, 0.85f, 0.20f)
        builder.addBox(0f, 1.27f, 0f, 0.08f, 0.03f, 3.82f, 1.0f, 0.85f, 0.20f)
        advancedSolarMesh = builder.build()

        // 21. BIOGAS GENERATOR (Dome tank with pipes)
        builder.reset()
        // Cylindrical tank base
        builder.addCylinder(0f, 0.50f, 0f, 1.45f, 1.00f, 14, 0.42f, 0.45f, 0.48f)
        // Emerald green dome lid
        builder.addSphere(0f, 1.00f, 0f, 1.48f, 10, 14, 0.15f, 0.65f, 0.35f)
        // Feedstock hopper chute
        builder.addBox(-1.20f, 0.60f, 0f, 0.55f, 0.75f, 0.55f, 0.52f, 0.38f, 0.22f)
        // Gas output pipe & dial gauge
        builder.addCylinder(0f, 1.95f, 0f, 0.08f, 0.50f, 8, 0.85f, 0.70f, 0.25f)
        builder.addSphere(0f, 2.20f, 0f, 0.14f, 6, 8, 0.95f, 0.95f, 0.95f)
        biogasGeneratorMesh = builder.build()

        // 22. GEOTHERMAL VENT (High-temperature steam turbine)
        builder.reset()
        // Basalt rock base
        builder.addBox(0f, 0.25f, 0f, 3.8f, 0.50f, 3.8f, 0.22f, 0.20f, 0.24f)
        // Deep geothermal borehole casing
        builder.addCylinder(0f, 0.90f, 0f, 0.65f, 1.10f, 12, 0.32f, 0.34f, 0.38f)
        // High-temperature heat exchanger turbine block
        builder.addBox(0.9f, 1.05f, 0f, 1.6f, 1.40f, 1.8f, 0.65f, 0.38f, 0.25f)
        // Dual steam exhaust chimneys with glowing heat vents
        builder.addCylinder(-0.6f, 2.10f, -0.6f, 0.20f, 2.20f, 8, 0.45f, 0.48f, 0.52f)
        builder.addCylinder(-0.6f, 2.10f, 0.6f, 0.20f, 2.20f, 8, 0.45f, 0.48f, 0.52f)
        builder.addCylinder(-0.6f, 3.25f, -0.6f, 0.24f, 0.10f, 8, 1.0f, 0.55f, 0.20f) // Heat ring
        builder.addCylinder(-0.6f, 3.25f, 0.6f, 0.24f, 0.10f, 8, 1.0f, 0.55f, 0.20f)
        geothermalVentMesh = builder.build()

        // 23. BASIC BATTERY (2x2 cabinet with glowing blue indicator)
        builder.reset()
        // Compact cabinet
        builder.addBox(0f, 0.85f, 0f, 1.70f, 1.70f, 1.30f, 0.25f, 0.28f, 0.32f)
        builder.addBox(0f, 0.85f, 0.66f, 1.30f, 1.30f, 0.04f, 0.12f, 0.15f, 0.18f) // Recessed front bezel
        // Glowing blue vertical charge meter
        builder.addBox(0f, 0.85f, 0.69f, 0.20f, 1.00f, 0.03f, 0.00f, 0.85f, 1.00f)
        basicBatteryMesh = builder.build()

        // 24. ADVANCED BATTERY (3x3 with multiple cells & digital readout)
        builder.reset()
        // Medium high-density cabinet
        builder.addBox(0f, 1.10f, 0f, 2.60f, 2.20f, 1.80f, 0.18f, 0.22f, 0.26f)
        // 4 Glowing cell modules
        builder.addBox(-0.65f, 1.40f, 0.91f, 0.90f, 0.45f, 0.04f, 0.00f, 0.90f, 0.85f)
        builder.addBox(0.65f, 1.40f, 0.91f, 0.90f, 0.45f, 0.04f, 0.00f, 0.90f, 0.85f)
        builder.addBox(-0.65f, 0.75f, 0.91f, 0.90f, 0.45f, 0.04f, 0.00f, 0.90f, 0.85f)
        builder.addBox(0.65f, 0.75f, 0.91f, 0.90f, 0.45f, 0.04f, 0.00f, 0.90f, 0.85f)
        // Central LED diagnostic display
        builder.addBox(0f, 1.85f, 0.91f, 1.80f, 0.22f, 0.04f, 0.20f, 0.85f, 1.00f)
        advancedBatteryMesh = builder.build()

        // 25. BATTERY BANK (5x5 industrial substation with cooling fans)
        builder.reset()
        // Container block
        builder.addBox(0f, 1.30f, 0f, 4.40f, 2.60f, 3.40f, 0.22f, 0.26f, 0.30f)
        // Top cooling fan housings
        builder.addCylinder(-1.20f, 2.70f, 0f, 0.65f, 0.25f, 12, 0.40f, 0.45f, 0.50f)
        builder.addCylinder(1.20f, 2.70f, 0f, 0.65f, 0.25f, 12, 0.40f, 0.45f, 0.50f)
        // High-voltage busbars and warning signs
        builder.addBox(0f, 1.30f, 1.72f, 3.80f, 1.60f, 0.06f, 0.10f, 0.12f, 0.15f)
        builder.addBox(0f, 1.30f, 1.76f, 3.20f, 0.40f, 0.04f, 0.00f, 0.95f, 0.85f) // Main power strip
        batteryBankMesh = builder.build()

        // 26. POWER POLE (1x1 wooden transmission pole with insulators)
        builder.reset()
        // Tall wooden pole
        builder.addCylinder(0f, 2.10f, 0f, 0.12f, 4.20f, 8, 0.45f, 0.32f, 0.18f)
        // Horizontal crossarm
        builder.addBox(0f, 3.85f, 0f, 2.20f, 0.12f, 0.12f, 0.55f, 0.40f, 0.24f)
        // 3 Ceramic insulators (White)
        builder.addCylinder(-0.95f, 4.05f, 0f, 0.07f, 0.25f, 6, 0.92f, 0.92f, 0.95f)
        builder.addCylinder(0f, 4.05f, 0f, 0.07f, 0.25f, 6, 0.92f, 0.92f, 0.95f)
        builder.addCylinder(0.95f, 4.05f, 0f, 0.07f, 0.25f, 6, 0.92f, 0.92f, 0.95f)
        // Transformer can
        builder.addCylinder(0.24f, 3.20f, 0f, 0.18f, 0.55f, 8, 0.40f, 0.45f, 0.50f)
        powerPoleMesh = builder.build()

        // 27. POWER CABLE SEGMENT (Glowing blue electrical line)
        builder.reset()
        builder.addCylinder(0f, 0f, 0f, 0.045f, 1.0f, 6, 0.15f, 0.80f, 1.0f)
        powerCableSegmentMesh = builder.build()

        // 28. NPC CABIN (3x3 units, cozy wooden cottage with porch and 2 warm beds)
        builder.reset()
        // Stone foundation
        builder.addBox(0f, 0.15f, 0f, 3.4f, 0.30f, 3.4f, 0.45f, 0.45f, 0.48f)
        // Timber walls
        builder.addBox(0f, 1.25f, 0f, 3.1f, 1.90f, 3.1f, 0.72f, 0.52f, 0.35f)
        // Slanted cedar shake roof
        builder.addRoofPrism(0f, 2.25f, 0f, 3.5f, 1.10f, 3.5f, 0.40f, 0.28f, 0.18f)
        // Chimney
        builder.addBox(0.9f, 2.60f, -0.6f, 0.45f, 1.00f, 0.45f, 0.55f, 0.25f, 0.20f)
        // Front door with brass handle
        builder.addBox(0f, 0.80f, 1.57f, 0.75f, 1.30f, 0.06f, 0.38f, 0.24f, 0.15f)
        // Warm glowing window
        builder.addBox(-0.9f, 1.20f, 1.57f, 0.65f, 0.65f, 0.04f, 1.00f, 0.90f, 0.40f)
        // Flower box under window
        builder.addBox(-0.9f, 0.78f, 1.62f, 0.75f, 0.15f, 0.18f, 0.20f, 0.65f, 0.30f)
        // Porch steps
        builder.addBox(0f, 0.08f, 1.85f, 1.10f, 0.16f, 0.55f, 0.50f, 0.38f, 0.25f)
        npcCabinMesh = builder.build()

        // 29. BUNKHOUSE (5x5 units, 6 beds communal lodge)
        builder.reset()
        // Broad foundation
        builder.addBox(0f, 0.18f, 0f, 5.4f, 0.36f, 5.4f, 0.38f, 0.40f, 0.42f)
        // Forest green timber siding
        builder.addBox(0f, 1.50f, 0f, 5.0f, 2.30f, 5.0f, 0.22f, 0.45f, 0.32f)
        // Multi-dormer pitched roof
        builder.addRoofPrism(0f, 2.70f, 0f, 5.6f, 1.50f, 5.6f, 0.35f, 0.25f, 0.20f)
        // Dormer window on roof
        builder.addBox(0f, 3.10f, 1.60f, 1.20f, 0.80f, 1.10f, 0.90f, 0.85f, 0.50f)
        // Welcoming double entrance doors
        builder.addBox(0f, 0.90f, 2.52f, 1.40f, 1.50f, 0.08f, 0.45f, 0.30f, 0.18f)
        // Dual front picture windows
        builder.addBox(-1.6f, 1.30f, 2.52f, 0.90f, 0.80f, 0.05f, 1.00f, 0.88f, 0.45f)
        builder.addBox(1.6f, 1.30f, 2.52f, 0.90f, 0.80f, 0.05f, 1.00f, 0.88f, 0.45f)
        // Front veranda railing & deck
        builder.addBox(0f, 0.10f, 2.95f, 3.20f, 0.20f, 0.90f, 0.48f, 0.35f, 0.22f)
        bunkhouseMesh = builder.build()

        // 30. KITCHEN (4x4 units, solarpunk cooking & dining hub)
        builder.reset()
        // Terracotta foundation
        builder.addBox(0f, 0.15f, 0f, 4.4f, 0.30f, 4.4f, 0.65f, 0.38f, 0.25f)
        // Warm clay/timber walls
        builder.addBox(0f, 1.30f, 0f, 4.0f, 2.00f, 4.0f, 0.85f, 0.72f, 0.55f)
        // Slanted clay-tiled roof
        builder.addRoofPrism(0f, 2.35f, 0f, 4.5f, 1.10f, 4.5f, 0.75f, 0.35f, 0.20f)
        // Rooftop solar parabolic cooker
        builder.addCylinder(1.2f, 3.10f, 0f, 0.70f, 0.25f, 12, 0.85f, 0.90f, 0.95f)
        // Serving counter hatch (Open window)
        builder.addBox(-0.5f, 1.05f, 2.02f, 1.80f, 0.90f, 0.08f, 0.18f, 0.20f, 0.22f)
        builder.addBox(-0.5f, 0.60f, 2.20f, 2.00f, 0.10f, 0.40f, 0.55f, 0.38f, 0.25f) // Serving counter shelf
        // Entrance door
        builder.addBox(1.30f, 0.85f, 2.02f, 0.80f, 1.45f, 0.08f, 0.42f, 0.28f, 0.16f)
        // Cooking chimney vent
        builder.addCylinder(-1.2f, 2.80f, -0.8f, 0.22f, 1.20f, 8, 0.45f, 0.48f, 0.50f)
        kitchenMesh = builder.build()

        // 31. MEDIC STATION (3x3 units, white clinical infirmary with solar cross)
        builder.reset()
        // Sterile foundation
        builder.addBox(0f, 0.15f, 0f, 3.4f, 0.30f, 3.4f, 0.75f, 0.80f, 0.85f)
        // White exterior walls
        builder.addBox(0f, 1.35f, 0f, 3.0f, 2.10f, 3.0f, 0.95f, 0.96f, 0.98f)
        // Mint solar flat roof with skylight
        builder.addBox(0f, 2.45f, 0f, 3.3f, 0.20f, 3.3f, 0.15f, 0.75f, 0.65f)
        builder.addBox(0f, 2.60f, 0f, 1.2f, 0.15f, 1.2f, 0.40f, 0.85f, 0.95f) // Skylight
        // Red medical cross on front facade
        builder.addBox(0f, 1.80f, 1.52f, 0.55f, 0.18f, 0.04f, 0.95f, 0.18f, 0.18f)
        builder.addBox(0f, 1.80f, 1.52f, 0.18f, 0.55f, 0.04f, 0.95f, 0.18f, 0.18f)
        // Sliding clinic glass door
        builder.addBox(0f, 0.75f, 1.52f, 0.90f, 1.35f, 0.06f, 0.60f, 0.85f, 0.92f)
        // Medicine cabinet window
        builder.addBox(-0.95f, 1.25f, 1.52f, 0.55f, 0.65f, 0.04f, 0.85f, 0.95f, 1.00f)
        medicStationMesh = builder.build()

        // 32. RESEARCH LAB (4x4 units, high-tech solarpunk facility)
        builder.reset()
        // High-tech composite foundation
        builder.addBox(0f, 0.20f, 0f, 4.4f, 0.40f, 4.4f, 0.25f, 0.30f, 0.35f)
        // Cleanroom white and titanium walls
        builder.addBox(0f, 1.45f, 0f, 4.0f, 2.10f, 4.0f, 0.90f, 0.94f, 0.98f)
        // Hemispherical glazed solar skylight dome
        builder.addSphere(0f, 2.50f, 0f, 1.40f, 10, 14, 0.10f, 0.78f, 0.90f)
        // Communication & telemetry antenna mast
        builder.addCylinder(1.4f, 3.40f, 1.4f, 0.04f, 1.80f, 6, 0.85f, 0.85f, 0.90f)
        builder.addSphere(1.4f, 4.30f, 1.4f, 0.12f, 6, 8, 0.00f, 0.95f, 0.85f)
        // Air filtration intake unit
        builder.addBox(-1.4f, 1.60f, -2.05f, 0.80f, 0.80f, 0.25f, 0.40f, 0.45f, 0.50f)
        // Cleanroom glass entry airlock
        builder.addBox(0f, 0.85f, 2.03f, 1.10f, 1.55f, 0.08f, 0.15f, 0.75f, 0.85f)
        researchLabMesh = builder.build()
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
                BuildableType.WELL -> {
                    renderMesh(shader, shadowSmallMesh, modelMatrix, viewMatrix, projMatrix)
                    Matrix.rotateM(modelMatrix, 0, building.rotationDeg, 0f, 1f, 0f)
                    renderMesh(shader, wellMesh, modelMatrix, viewMatrix, projMatrix)
                }
                BuildableType.FENCE -> {
                    renderMesh(shader, shadowSmallMesh, modelMatrix, viewMatrix, projMatrix)
                    Matrix.rotateM(modelMatrix, 0, building.rotationDeg, 0f, 1f, 0f)
                    renderMesh(shader, fenceSegmentMesh, modelMatrix, viewMatrix, projMatrix)
                }
                BuildableType.COMPOST_BIN -> {
                    renderMesh(shader, shadowSmallMesh, modelMatrix, viewMatrix, projMatrix)
                    Matrix.rotateM(modelMatrix, 0, building.rotationDeg, 0f, 1f, 0f)
                    renderMesh(shader, compostBinMesh, modelMatrix, viewMatrix, projMatrix)
                }
                BuildableType.RAIN_BARREL -> {
                    renderMesh(shader, shadowSmallMesh, modelMatrix, viewMatrix, projMatrix)
                    Matrix.rotateM(modelMatrix, 0, building.rotationDeg, 0f, 1f, 0f)
                    renderMesh(shader, rainBarrelMesh, modelMatrix, viewMatrix, projMatrix)
                }
                BuildableType.WATER_FILTER -> {
                    renderMesh(shader, shadowSmallMesh, modelMatrix, viewMatrix, projMatrix)
                    Matrix.rotateM(modelMatrix, 0, building.rotationDeg, 0f, 1f, 0f)
                    renderMesh(shader, waterFilterMesh, modelMatrix, viewMatrix, projMatrix)
                }
                BuildableType.WATER_PURIFIER -> {
                    renderMesh(shader, shadowSmallMesh, modelMatrix, viewMatrix, projMatrix)
                    Matrix.rotateM(modelMatrix, 0, building.rotationDeg, 0f, 1f, 0f)
                    renderMesh(shader, waterPurifierMesh, modelMatrix, viewMatrix, projMatrix)
                }
                BuildableType.IRRIGATION_PIPE -> {
                    Matrix.rotateM(modelMatrix, 0, building.rotationDeg, 0f, 1f, 0f)
                    renderMesh(shader, irrigationPipeMesh, modelMatrix, viewMatrix, projMatrix)
                }
                BuildableType.IRRIGATION_NODE -> {
                    Matrix.rotateM(modelMatrix, 0, building.rotationDeg, 0f, 1f, 0f)
                    renderMesh(shader, irrigationNodeMesh, modelMatrix, viewMatrix, projMatrix)
                }
                BuildableType.WATER_STORAGE_SHED -> {
                    renderMesh(shader, shadowMediumMesh, modelMatrix, viewMatrix, projMatrix)
                    Matrix.rotateM(modelMatrix, 0, building.rotationDeg, 0f, 1f, 0f)
                    renderMesh(shader, waterStorageMesh, modelMatrix, viewMatrix, projMatrix)
                }
                BuildableType.HYDRO_GENERATOR -> {
                    renderMesh(shader, shadowMediumMesh, modelMatrix, viewMatrix, projMatrix)
                    Matrix.rotateM(modelMatrix, 0, building.rotationDeg, 0f, 1f, 0f)
                    renderMesh(shader, hydroGeneratorMesh, modelMatrix, viewMatrix, projMatrix)

                    // Spinning Hydro Water Wheel
                    Matrix.setIdentityM(partMatrix, 0)
                    Matrix.multiplyMM(partMatrix, 0, modelMatrix, 0, partMatrix, 0)
                    Matrix.translateM(partMatrix, 0, -0.6f, 0.70f, 0f)
                    val wheelSpin = (animTime * 140.0f) % 360.0f
                    Matrix.rotateM(partMatrix, 0, wheelSpin, 0f, 0f, 1f)
                    renderMesh(shader, hydroWaterWheelMesh, partMatrix, viewMatrix, projMatrix)
                }
                BuildableType.ADVANCED_SOLAR -> {
                    renderMesh(shader, shadowLargeMesh, modelMatrix, viewMatrix, projMatrix)
                    // Solar tracking slow rotation
                    val sunTrackingTilt = sin(animTime * 0.5f) * 12.0f
                    Matrix.rotateM(modelMatrix, 0, building.rotationDeg + sunTrackingTilt, 0f, 1f, 0f)
                    renderMesh(shader, advancedSolarMesh, modelMatrix, viewMatrix, projMatrix)
                }
                BuildableType.BIOGAS_GENERATOR -> {
                    renderMesh(shader, shadowMediumMesh, modelMatrix, viewMatrix, projMatrix)
                    Matrix.rotateM(modelMatrix, 0, building.rotationDeg, 0f, 1f, 0f)
                    renderMesh(shader, biogasGeneratorMesh, modelMatrix, viewMatrix, projMatrix)
                }
                BuildableType.GEOTHERMAL_VENT -> {
                    renderMesh(shader, shadowLargeMesh, modelMatrix, viewMatrix, projMatrix)
                    Matrix.rotateM(modelMatrix, 0, building.rotationDeg, 0f, 1f, 0f)
                    renderMesh(shader, geothermalVentMesh, modelMatrix, viewMatrix, projMatrix)
                }
                BuildableType.BASIC_BATTERY -> {
                    renderMesh(shader, shadowSmallMesh, modelMatrix, viewMatrix, projMatrix)
                    Matrix.rotateM(modelMatrix, 0, building.rotationDeg, 0f, 1f, 0f)
                    renderMesh(shader, basicBatteryMesh, modelMatrix, viewMatrix, projMatrix)
                }
                BuildableType.ADVANCED_BATTERY -> {
                    renderMesh(shader, shadowMediumMesh, modelMatrix, viewMatrix, projMatrix)
                    Matrix.rotateM(modelMatrix, 0, building.rotationDeg, 0f, 1f, 0f)
                    renderMesh(shader, advancedBatteryMesh, modelMatrix, viewMatrix, projMatrix)
                }
                BuildableType.BATTERY_BANK -> {
                    renderMesh(shader, shadowLargeMesh, modelMatrix, viewMatrix, projMatrix)
                    Matrix.rotateM(modelMatrix, 0, building.rotationDeg, 0f, 1f, 0f)
                    renderMesh(shader, batteryBankMesh, modelMatrix, viewMatrix, projMatrix)
                }
                BuildableType.POWER_POLE -> {
                    renderMesh(shader, shadowSmallMesh, modelMatrix, viewMatrix, projMatrix)
                    Matrix.rotateM(modelMatrix, 0, building.rotationDeg, 0f, 1f, 0f)
                    renderMesh(shader, powerPoleMesh, modelMatrix, viewMatrix, projMatrix)
                }
                BuildableType.NPC_CABIN -> {
                    renderMesh(shader, shadowMediumMesh, modelMatrix, viewMatrix, projMatrix)
                    Matrix.rotateM(modelMatrix, 0, building.rotationDeg, 0f, 1f, 0f)
                    renderMesh(shader, npcCabinMesh, modelMatrix, viewMatrix, projMatrix)
                }
                BuildableType.BUNKHOUSE -> {
                    renderMesh(shader, shadowLargeMesh, modelMatrix, viewMatrix, projMatrix)
                    Matrix.rotateM(modelMatrix, 0, building.rotationDeg, 0f, 1f, 0f)
                    renderMesh(shader, bunkhouseMesh, modelMatrix, viewMatrix, projMatrix)
                }
                BuildableType.KITCHEN -> {
                    renderMesh(shader, shadowMediumMesh, modelMatrix, viewMatrix, projMatrix)
                    Matrix.rotateM(modelMatrix, 0, building.rotationDeg, 0f, 1f, 0f)
                    renderMesh(shader, kitchenMesh, modelMatrix, viewMatrix, projMatrix)
                }
                BuildableType.MEDIC_STATION -> {
                    renderMesh(shader, shadowMediumMesh, modelMatrix, viewMatrix, projMatrix)
                    Matrix.rotateM(modelMatrix, 0, building.rotationDeg, 0f, 1f, 0f)
                    renderMesh(shader, medicStationMesh, modelMatrix, viewMatrix, projMatrix)
                }
                BuildableType.WORKSHOP -> {
                    renderMesh(shader, shadowMediumMesh, modelMatrix, viewMatrix, projMatrix)
                    Matrix.rotateM(modelMatrix, 0, building.rotationDeg, 0f, 1f, 0f)
                    renderMesh(shader, workshopMesh, modelMatrix, viewMatrix, projMatrix)
                }
                BuildableType.RESEARCH_LAB -> {
                    renderMesh(shader, shadowLargeMesh, modelMatrix, viewMatrix, projMatrix)
                    Matrix.rotateM(modelMatrix, 0, building.rotationDeg, 0f, 1f, 0f)
                    renderMesh(shader, researchLabMesh, modelMatrix, viewMatrix, projMatrix)
                }
            }
        }

        // 7. Draw Power Grid Glowing Lines between Connected Nodes (Within 15 Units)
        drawPowerGridLines(shader, viewMatrix, projMatrix, energyNodes, placedBuildings, animTime)

        // 8. Draw Animal Pen Perimeter Fences
        drawFenceLine(shader, viewMatrix, projMatrix, -16f, 2f, -8f, 2f)
        drawFenceLine(shader, viewMatrix, projMatrix, -8f, 2f, -8f, 8f)
        drawFenceLine(shader, viewMatrix, projMatrix, -16f, 2f, -16f, 14f)

        // 9. Draw Chicken Coop at X = 4.5, Z = 4.0
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 4.5f, 0.0f, 4.0f)
        renderMesh(shader, shadowSmallMesh, modelMatrix, viewMatrix, projMatrix)
        renderMesh(shader, chickenCoopMesh, modelMatrix, viewMatrix, projMatrix)
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
        isValid: Boolean
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
            BuildableType.WELL -> wellMesh
            BuildableType.FENCE -> fenceSegmentMesh
            BuildableType.COMPOST_BIN -> compostBinMesh
            BuildableType.RAIN_BARREL -> rainBarrelMesh
            BuildableType.WATER_FILTER -> waterFilterMesh
            BuildableType.WATER_PURIFIER -> waterPurifierMesh
            BuildableType.IRRIGATION_PIPE -> irrigationPipeMesh
            BuildableType.IRRIGATION_NODE -> irrigationNodeMesh
            BuildableType.WATER_STORAGE_SHED -> waterStorageMesh
            BuildableType.HYDRO_GENERATOR -> hydroGeneratorMesh
            BuildableType.ADVANCED_SOLAR -> advancedSolarMesh
            BuildableType.BIOGAS_GENERATOR -> biogasGeneratorMesh
            BuildableType.GEOTHERMAL_VENT -> geothermalVentMesh
            BuildableType.BASIC_BATTERY -> basicBatteryMesh
            BuildableType.ADVANCED_BATTERY -> advancedBatteryMesh
            BuildableType.BATTERY_BANK -> batteryBankMesh
            BuildableType.POWER_POLE -> powerPoleMesh
            BuildableType.NPC_CABIN -> npcCabinMesh
            BuildableType.BUNKHOUSE -> bunkhouseMesh
            BuildableType.KITCHEN -> kitchenMesh
            BuildableType.MEDIC_STATION -> medicStationMesh
            BuildableType.WORKSHOP -> workshopMesh
            BuildableType.RESEARCH_LAB -> researchLabMesh
        }

        val eR = if (isValid) 0.0f else 0.85f
        val eG = if (isValid) 0.85f else 0.15f
        val eB = if (isValid) 0.70f else 0.10f

        renderGhostMesh(shader, mesh, modelMatrix, viewMatrix, projMatrix, eR, eG, eB)
    }

    private fun drawPowerGridLines(
        shader: GLShader,
        viewMatrix: FloatArray,
        projMatrix: FloatArray,
        energyNodes: List<EnergyNodeEntity>,
        placedBuildings: List<PlacedBuildingEntity>,
        animTime: Float
    ) {
        data class GridPoint(val x: Float, val y: Float, val z: Float, val id: String)
        val points = mutableListOf<GridPoint>()

        // Farmhouse base connection point
        points.add(GridPoint(6.0f, 1.8f, 0.0f, "house"))
        // Artisan workshop
        points.add(GridPoint(0.0f, 1.8f, 14.0f, "workshop"))

        // Add energy nodes
        for (node in energyNodes) {
            points.add(GridPoint(node.posX, 1.5f, node.posZ, "en_${node.id}"))
        }

        // Add placed buildings with power relevance
        for (b in placedBuildings) {
            val isPowerEntity = when (b.buildingType) {
                BuildableType.SOLAR_PANEL,
                BuildableType.WINDMILL,
                BuildableType.HYDRO_GENERATOR,
                BuildableType.ADVANCED_SOLAR,
                BuildableType.BIOGAS_GENERATOR,
                BuildableType.GEOTHERMAL_VENT,
                BuildableType.BASIC_BATTERY,
                BuildableType.ADVANCED_BATTERY,
                BuildableType.BATTERY_BANK,
                BuildableType.POWER_POLE,
                BuildableType.WATER_PURIFIER,
                BuildableType.GREENHOUSE,
                BuildableType.KITCHEN,
                BuildableType.MEDIC_STATION,
                BuildableType.WORKSHOP,
                BuildableType.RESEARCH_LAB -> true
                else -> false
            }
            if (isPowerEntity) {
                val wireHeight = if (b.buildingType == BuildableType.POWER_POLE) 3.85f else 1.2f
                points.add(GridPoint(b.posX, wireHeight, b.posZ, "pb_${b.id}"))
            }
        }

        if (points.size < 2) return

        // Pulsing glowing blue lines
        val pulse = (sin(animTime * 6.0f) * 0.35f + 0.65f).coerceIn(0.2f, 1.0f)
        val emissiveG = 0.85f * pulse
        val emissiveB = 1.0f * pulse

        // Connect nodes within 15 units. Each node connects to up to 2 closest neighbors within 15m
        val drawnConnections = mutableSetOf<String>()
        for (i in 0 until points.size) {
            val p1 = points[i]
            val neighbors = mutableListOf<Pair<GridPoint, Float>>()
            for (j in 0 until points.size) {
                if (i == j) continue
                val p2 = points[j]
                val dx = p2.x - p1.x
                val dy = p2.y - p1.y
                val dz = p2.z - p1.z
                val dist = kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
                if (dist <= 15.0f) {
                    neighbors.add(Pair(p2, dist))
                }
            }
            neighbors.sortBy { it.second }
            for (k in 0 until kotlin.math.min(2, neighbors.size)) {
                val p2 = neighbors[k].first
                val pairKey = if (p1.id < p2.id) "${p1.id}__${p2.id}" else "${p2.id}__${p1.id}"
                if (drawnConnections.add(pairKey)) {
                    val dx = p2.x - p1.x
                    val dy = p2.y - p1.y
                    val dz = p2.z - p1.z
                    val dist = kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
                    if (dist > 0.1f) {
                        Matrix.setIdentityM(modelMatrix, 0)
                        Matrix.translateM(modelMatrix, 0, (p1.x + p2.x) * 0.5f, (p1.y + p2.y) * 0.5f, (p1.z + p2.z) * 0.5f)
                        val angleY = Math.toDegrees(kotlin.math.atan2(dx.toDouble(), dz.toDouble())).toFloat()
                        val horizDist = kotlin.math.sqrt(dx * dx + dz * dz)
                        val pitch = Math.toDegrees(-kotlin.math.atan2(dy.toDouble(), horizDist.toDouble())).toFloat()
                        Matrix.rotateM(modelMatrix, 0, angleY, 0f, 1f, 0f)
                        Matrix.rotateM(modelMatrix, 0, pitch, 1f, 0f, 0f)
                        Matrix.scaleM(modelMatrix, 0, 1.0f, 1.0f, dist)
                        renderGhostMesh(shader, powerCableSegmentMesh, modelMatrix, viewMatrix, projMatrix, 0.0f, emissiveG, emissiveB)
                    }
                }
            }
        }
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
