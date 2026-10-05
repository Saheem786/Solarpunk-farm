package com.example.game3d.physics

import com.example.data.local.EnergyNodeEntity
import com.example.data.local.PlacedBuildingEntity
import com.example.data.local.PlotEntity
import com.example.data.model.BuildableType
import com.example.data.model.EnergyNodeType
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Axis-Aligned Bounding Box for fast broadphase collision culling.
 */
data class BoundingBox2D(
    val minX: Float,
    val minZ: Float,
    val maxX: Float,
    val maxZ: Float
) {
    fun overlaps(px: Float, pz: Float, radius: Float): Boolean {
        return px + radius >= minX && px - radius <= maxX &&
               pz + radius >= minZ && pz - radius <= maxZ
    }
}

/**
 * Common interface for 3D/2.5D physical environment colliders.
 */
sealed interface Collider {
    val name: String
    val bounds: BoundingBox2D

    /**
     * Checks if a cylinder with given radius centered at (px, pz) intersects this collider.
     */
    fun checkCollision(px: Float, pz: Float, radius: Float): Boolean

    /**
     * Returns the unit collision normal (nx, nz) pointing outward from the obstacle surface,
     * or null if no contact.
     */
    fun getCollisionNormal(px: Float, pz: Float, radius: Float): Pair<Float, Float>?

    /**
     * Calculates push-out vector to separate the player cylinder from this collider.
     * Returns Pair(pushX, pushZ) or null if not overlapping.
     */
    fun getPushOut(px: Float, pz: Float, radius: Float): Pair<Float, Float>?
}

/**
 * Oriented or Axis-Aligned Box Collider (e.g. Houses, Greenhouses, Farm Plots, Sheds).
 */
data class BoxCollider(
    val centerX: Float,
    val centerZ: Float,
    val sizeX: Float,
    val sizeZ: Float,
    val rotationDeg: Float = 0.0f,
    override val name: String = "BoxCollider"
) : Collider {

    private val halfX = sizeX / 2.0f
    private val halfZ = sizeZ / 2.0f
    private val rotRad = Math.toRadians(rotationDeg.toDouble()).toFloat()
    private val cosRot = cos(rotRad)
    private val sinRot = sin(rotRad)

    override val bounds: BoundingBox2D by lazy {
        val maxExtent = sqrt(halfX * halfX + halfZ * halfZ)
        BoundingBox2D(
            minX = centerX - maxExtent,
            minZ = centerZ - maxExtent,
            maxX = centerX + maxExtent,
            maxZ = centerZ + maxExtent
        )
    }

    override fun checkCollision(px: Float, pz: Float, radius: Float): Boolean {
        if (!bounds.overlaps(px, pz, radius)) return false

        // Transform player position into local coordinate space of the box
        val dx = px - centerX
        val dz = pz - centerZ
        val localX = dx * cosRot + dz * sinRot
        val localZ = -dx * sinRot + dz * cosRot

        val closestX = localX.coerceIn(-halfX, halfX)
        val closestZ = localZ.coerceIn(-halfZ, halfZ)

        val distX = localX - closestX
        val distZ = localZ - closestZ
        return (distX * distX + distZ * distZ) < (radius * radius)
    }

    override fun getCollisionNormal(px: Float, pz: Float, radius: Float): Pair<Float, Float>? {
        if (!bounds.overlaps(px, pz, radius)) return null

        val dx = px - centerX
        val dz = pz - centerZ
        val localX = dx * cosRot + dz * sinRot
        val localZ = -dx * sinRot + dz * cosRot

        val closestX = localX.coerceIn(-halfX, halfX)
        val closestZ = localZ.coerceIn(-halfZ, halfZ)

        val distX = localX - closestX
        val distZ = localZ - closestZ
        val distSq = distX * distX + distZ * distZ

        if (distSq >= radius * radius) return null

        val dist = sqrt(distSq)
        val localNx: Float
        val localNz: Float

        if (dist > 0.0001f) {
            localNx = distX / dist
            localNz = distZ / dist
        } else {
            // Inside box: find closest face normal
            val dRight = halfX - localX
            val dLeft = localX - (-halfX)
            val dTop = halfZ - localZ
            val dBottom = localZ - (-halfZ)
            val minD = minOf(dRight, dLeft, dTop, dBottom)
            when (minD) {
                dRight -> { localNx = 1f; localNz = 0f }
                dLeft -> { localNx = -1f; localNz = 0f }
                dTop -> { localNx = 0f; localNz = 1f }
                else -> { localNx = 0f; localNz = -1f }
            }
        }

        // Transform normal back to world space
        val worldNx = localNx * cosRot - localNz * sinRot
        val worldNz = localNx * sinRot + localNz * cosRot
        return Pair(worldNx, worldNz)
    }

    override fun getPushOut(px: Float, pz: Float, radius: Float): Pair<Float, Float>? {
        if (!bounds.overlaps(px, pz, radius)) return null

        val dx = px - centerX
        val dz = pz - centerZ
        val localX = dx * cosRot + dz * sinRot
        val localZ = -dx * sinRot + dz * cosRot

        val closestX = localX.coerceIn(-halfX, halfX)
        val closestZ = localZ.coerceIn(-halfZ, halfZ)

        val distX = localX - closestX
        val distZ = localZ - closestZ
        val distSq = distX * distX + distZ * distZ

        if (distSq >= radius * radius) {
            return null
        }

        val dist = sqrt(distSq)
        val localPushX: Float
        val localPushZ: Float

        if (dist > 0.0001f) {
            val penetration = radius - dist
            localPushX = (distX / dist) * penetration
            localPushZ = (distZ / dist) * penetration
        } else {
            val dRight = halfX - localX
            val dLeft = localX - (-halfX)
            val dTop = halfZ - localZ
            val dBottom = localZ - (-halfZ)
            val minD = minOf(dRight, dLeft, dTop, dBottom)
            when (minD) {
                dRight -> { localPushX = dRight + radius; localPushZ = 0f }
                dLeft -> { localPushX = -(dLeft + radius); localPushZ = 0f }
                dTop -> { localPushX = 0f; localPushZ = dTop + radius }
                else -> { localPushX = 0f; localPushZ = -(dBottom + radius) }
            }
        }

        val worldPushX = localPushX * cosRot - localPushZ * sinRot
        val worldPushZ = localPushX * sinRot + localPushZ * cosRot
        return Pair(worldPushX, worldPushZ)
    }
}

/**
 * Cylinder Collider (e.g. Tree Trunks, Water Wells, Silos, Market Stalls).
 */
data class CylinderCollider(
    val centerX: Float,
    val centerZ: Float,
    val radius: Float,
    override val name: String = "CylinderCollider"
) : Collider {

    override val bounds: BoundingBox2D = BoundingBox2D(
        minX = centerX - radius,
        minZ = centerZ - radius,
        maxX = centerX + radius,
        maxZ = centerZ + radius
    )

    override fun checkCollision(px: Float, pz: Float, playerRadius: Float): Boolean {
        if (!bounds.overlaps(px, pz, playerRadius)) return false
        val dx = px - centerX
        val dz = pz - centerZ
        val totalR = radius + playerRadius
        return (dx * dx + dz * dz) < (totalR * totalR)
    }

    override fun getCollisionNormal(px: Float, pz: Float, playerRadius: Float): Pair<Float, Float>? {
        if (!bounds.overlaps(px, pz, playerRadius)) return null
        val dx = px - centerX
        val dz = pz - centerZ
        val distSq = dx * dx + dz * dz
        val totalR = radius + playerRadius
        if (distSq >= totalR * totalR) return null

        val dist = sqrt(distSq)
        return if (dist > 0.0001f) {
            Pair(dx / dist, dz / dist)
        } else {
            Pair(1f, 0f)
        }
    }

    override fun getPushOut(px: Float, pz: Float, playerRadius: Float): Pair<Float, Float>? {
        if (!bounds.overlaps(px, pz, playerRadius)) return null
        val dx = px - centerX
        val dz = pz - centerZ
        val distSq = dx * dx + dz * dz
        val totalR = radius + playerRadius

        if (distSq >= totalR * totalR) {
            return null
        }

        val dist = sqrt(distSq)
        return if (dist > 0.0001f) {
            val penetration = totalR - dist
            Pair((dx / dist) * penetration, (dz / dist) * penetration)
        } else {
            Pair(totalR, 0f)
        }
    }
}

/**
 * Line Segment Collider (e.g. Fence Sections, Railings).
 */
data class SegmentCollider(
    val ax: Float,
    val az: Float,
    val bx: Float,
    val bz: Float,
    val thickness: Float = 0.12f,
    override val name: String = "SegmentCollider"
) : Collider {

    override val bounds: BoundingBox2D = BoundingBox2D(
        minX = min(ax, bx) - thickness,
        minZ = min(az, bz) - thickness,
        maxX = max(ax, bx) + thickness,
        maxZ = max(az, bz) + thickness
    )

    override fun checkCollision(px: Float, pz: Float, playerRadius: Float): Boolean {
        if (!bounds.overlaps(px, pz, playerRadius)) return false
        val dist = distanceToPoint(px, pz)
        return dist < (thickness + playerRadius)
    }

    override fun getCollisionNormal(px: Float, pz: Float, playerRadius: Float): Pair<Float, Float>? {
        if (!bounds.overlaps(px, pz, playerRadius)) return null
        val totalR = thickness + playerRadius
        val vx = bx - ax
        val vz = bz - az
        val wx = px - ax
        val wz = pz - az
        val vLenSq = vx * vx + vz * vz

        val t = if (vLenSq > 0.0001f) {
            ((wx * vx + wz * vz) / vLenSq).coerceIn(0.0f, 1.0f)
        } else 0.0f

        val closestX = ax + t * vx
        val closestZ = az + t * vz
        val dx = px - closestX
        val dz = pz - closestZ
        val distSq = dx * dx + dz * dz

        if (distSq >= totalR * totalR) return null

        val dist = sqrt(distSq)
        return if (dist > 0.0001f) {
            Pair(dx / dist, dz / dist)
        } else {
            val segLen = sqrt(vLenSq)
            val nx = if (segLen > 0.0001f) -vz / segLen else 1f
            val nz = if (segLen > 0.0001f) vx / segLen else 0f
            Pair(nx, nz)
        }
    }

    override fun getPushOut(px: Float, pz: Float, playerRadius: Float): Pair<Float, Float>? {
        if (!bounds.overlaps(px, pz, playerRadius)) return null
        val totalR = thickness + playerRadius
        val vx = bx - ax
        val vz = bz - az
        val wx = px - ax
        val wz = pz - az
        val vLenSq = vx * vx + vz * vz

        val t = if (vLenSq > 0.0001f) {
            ((wx * vx + wz * vz) / vLenSq).coerceIn(0.0f, 1.0f)
        } else 0.0f

        val closestX = ax + t * vx
        val closestZ = az + t * vz
        val dx = px - closestX
        val dz = pz - closestZ
        val distSq = dx * dx + dz * dz

        if (distSq >= totalR * totalR) {
            return null
        }

        val dist = sqrt(distSq)
        return if (dist > 0.0001f) {
            val penetration = totalR - dist
            Pair((dx / dist) * penetration, (dz / dist) * penetration)
        } else {
            val segLen = sqrt(vLenSq)
            val nx = if (segLen > 0.0001f) -vz / segLen else 1f
            val nz = if (segLen > 0.0001f) vx / segLen else 0f
            Pair(nx * totalR, nz * totalR)
        }
    }

    private fun distanceToPoint(px: Float, pz: Float): Float {
        val vx = bx - ax
        val vz = bz - az
        val wx = px - ax
        val wz = pz - az
        val vLenSq = vx * vx + vz * vz
        if (vLenSq < 0.0001f) {
            val dx = px - ax
            val dz = pz - az
            return sqrt(dx * dx + dz * dz)
        }
        val t = ((wx * vx + wz * vz) / vLenSq).coerceIn(0.0f, 1.0f)
        val cx = ax + t * vx
        val cz = az + t * vz
        val dx = px - cx
        val dz = pz - cz
        return sqrt(dx * dx + dz * dz)
    }
}

/**
 * World Environment Collider Factory & Cache.
 */
object WorldColliderBuilder {

    // 21 World Trees across farm
    private val staticTreePositions = listOf(
        Pair(-18.0f, -12.0f), Pair(-22.0f, -6.0f), Pair(-16.0f, -2.0f), Pair(-22.0f, 4.0f),
        Pair(-18.0f, 12.0f), Pair(-22.0f, 18.0f), Pair(-14.0f, 22.0f), Pair(-6.0f, 22.0f),
        Pair(2.0f, 22.0f), Pair(10.0f, 22.0f), Pair(18.0f, 22.0f), Pair(10.0f, 6.0f),
        Pair(12.0f, 14.0f), Pair(-6.0f, -16.0f), Pair(4.0f, -18.0f), Pair(-2.0f, -22.0f),
        Pair(12.0f, -14.5f), Pair(6.0f, -11.0f), Pair(16.0f, -4.0f), Pair(18.0f, 4.0f),
        Pair(18.0f, -18.0f)
    )

    // Default Animal Pen & Garden Fence Sections
    private val staticFences = listOf(
        SegmentCollider(-16.0f, 2.0f, -8.0f, 2.0f, 0.14f, "Fence_NorthPen"),
        SegmentCollider(-8.0f, 2.0f, -8.0f, 8.0f, 0.14f, "Fence_EastPen"),
        SegmentCollider(-16.0f, 2.0f, -16.0f, 14.0f, 0.14f, "Fence_WestPen"),
        // Wooden Footbridge Railings (X=16.0, Z=0.0)
        SegmentCollider(13.6f, -1.35f, 18.4f, -1.35f, 0.12f, "Bridge_NorthRailing"),
        SegmentCollider(13.6f, 1.35f, 18.4f, 1.35f, 0.12f, "Bridge_SouthRailing")
    )

    // Static World Buildings
    private val staticBuildings = listOf(
        // 1. House / Cabin (Main Farmhouse at 6.0, 0.0 - 7.4m x 5.2m)
        BoxCollider(6.0f, 0.0f, 7.4f, 5.2f, 0f, "House_Farmhouse"),

        // 2. Rustic Barn at -12.0, 10.0 - 7.6m x 6.0m
        BoxCollider(-12.0f, 10.0f, 7.6f, 6.0f, 0f, "Barn"),

        // 3. Artisan Eco-Workshop at 0.0, 14.0 - 6.2m x 5.0m
        BoxCollider(0.0f, 14.0f, 6.2f, 5.0f, 0f, "Workshop"),

        // 4. Market Stall at -14.0, -14.0 - Cylinder radius 1.8m
        CylinderCollider(-14.0f, -14.0f, 1.8f, "MarketStall"),

        // 5. Drinking Water Well at 8.0, -3.5 - Cylinder radius 0.95m
        CylinderCollider(8.0f, -3.5f, 0.95f, "StoneWell"),

        // 6. Chicken Coop at 4.5, 4.0 - 1.6m x 1.6m
        BoxCollider(4.5f, 4.0f, 1.6f, 1.6f, 0f, "ChickenCoop")
    )

    /**
     * Builds complete collider list for the entire game world including static assets, plots, and placed buildings.
     */
    fun buildColliders(
        plots: List<PlotEntity>,
        placedBuildings: List<PlacedBuildingEntity>,
        energyNodes: List<EnergyNodeEntity>
    ): List<Collider> {
        val list = ArrayList<Collider>(64)

        // 1. Static Buildings
        list.addAll(staticBuildings)

        // 2. Trees (All 21 World Trees with solid trunk colliders)
        for ((idx, pos) in staticTreePositions.withIndex()) {
            list.add(CylinderCollider(pos.first, pos.second, 0.45f, "Tree_$idx"))
        }

        // 3. Default Fences & Bridge Railings
        list.addAll(staticFences)

        // 4. Farm Plots (Solid 2.0m x 2.0m Permaculture Soil Beds to prevent clipping)
        if (plots.isNotEmpty()) {
            for (plot in plots) {
                list.add(BoxCollider(plot.posX, plot.posZ, 2.0f, 2.0f, 0f, "FarmPlot_${plot.id}"))
            }
        } else {
            // Default 6-plot grid
            for (index in 0 until 6) {
                val row = index / 3
                val col = index % 3
                val posX = -4.0f + (col - 1.0f) * 4.5f
                val posZ = -6.0f + (row - 0.5f) * 4.5f
                list.add(BoxCollider(posX, posZ, 2.0f, 2.0f, 0f, "FarmPlot_$index"))
            }
        }

        // 5. Player Placed Buildings (Cabins, Greenhouses, Windmills, etc.)
        for (b in placedBuildings) {
            when (b.buildingType) {
                BuildableType.CABIN -> {
                    list.add(BoxCollider(b.posX, b.posZ, 7.4f, 5.2f, b.rotationDeg, "Placed_Cabin_${b.id}"))
                }
                BuildableType.GREENHOUSE -> {
                    // Greenhouse Box Collider: 5.2m x 4.4m
                    list.add(BoxCollider(b.posX, b.posZ, 5.2f, 4.4f, b.rotationDeg, "Placed_Greenhouse_${b.id}"))
                }
                BuildableType.SOLAR_PANEL -> {
                    list.add(BoxCollider(b.posX, b.posZ, 3.4f, 2.2f, b.rotationDeg, "Placed_SolarPanel_${b.id}"))
                }
                BuildableType.STORAGE -> {
                    list.add(BoxCollider(b.posX, b.posZ, 2.0f, 2.0f, b.rotationDeg, "Placed_Storage_${b.id}"))
                }
                BuildableType.WINDMILL -> {
                    list.add(CylinderCollider(b.posX, b.posZ, 1.2f, "Placed_Windmill_${b.id}"))
                }
                BuildableType.WELL -> {
                    list.add(CylinderCollider(b.posX, b.posZ, 1.0f, "Placed_Well_${b.id}"))
                }
                BuildableType.COMPOST_BIN -> {
                    list.add(BoxCollider(b.posX, b.posZ, 1.8f, 1.8f, b.rotationDeg, "Placed_Compost_${b.id}"))
                }
                BuildableType.FENCE -> {
                    val rotRad = Math.toRadians(b.rotationDeg.toDouble())
                    val sinRot = sin(rotRad).toFloat()
                    val cosRot = cos(rotRad).toFloat()
                    val ax = b.posX - 1.45f * sinRot
                    val az = b.posZ - 1.45f * cosRot
                    val bx = b.posX + 1.45f * sinRot
                    val bz = b.posZ + 1.45f * cosRot
                    list.add(SegmentCollider(ax, az, bx, bz, 0.14f, "Placed_Fence_${b.id}"))
                }
                BuildableType.RAIN_BARREL -> {
                    list.add(CylinderCollider(b.posX, b.posZ, 0.75f, "Placed_RainBarrel_${b.id}"))
                }
                BuildableType.WATER_FILTER -> {
                    list.add(BoxCollider(b.posX, b.posZ, 1.2f, 1.2f, b.rotationDeg, "Placed_WaterFilter_${b.id}"))
                }
                BuildableType.WATER_PURIFIER -> {
                    list.add(BoxCollider(b.posX, b.posZ, 1.8f, 1.8f, b.rotationDeg, "Placed_WaterPurifier_${b.id}"))
                }
                BuildableType.IRRIGATION_PIPE -> {
                    // Pipe is flat on the ground, walkable
                }
                BuildableType.IRRIGATION_NODE -> {
                    list.add(CylinderCollider(b.posX, b.posZ, 0.45f, "Placed_IrrigationNode_${b.id}"))
                }
                BuildableType.WATER_STORAGE_SHED -> {
                    list.add(BoxCollider(b.posX, b.posZ, 3.2f, 2.6f, b.rotationDeg, "Placed_WaterStorageShed_${b.id}"))
                }
                BuildableType.HYDRO_GENERATOR -> {
                    list.add(BoxCollider(b.posX, b.posZ, 2.8f, 2.0f, b.rotationDeg, "Placed_Hydro_${b.id}"))
                }
                BuildableType.ADVANCED_SOLAR -> {
                    list.add(BoxCollider(b.posX, b.posZ, 3.8f, 3.8f, b.rotationDeg, "Placed_AdvSolar_${b.id}"))
                }
                BuildableType.BIOGAS_GENERATOR -> {
                    list.add(CylinderCollider(b.posX, b.posZ, 1.45f, "Placed_Biogas_${b.id}"))
                }
                BuildableType.GEOTHERMAL_VENT -> {
                    list.add(BoxCollider(b.posX, b.posZ, 3.8f, 3.8f, b.rotationDeg, "Placed_Geothermal_${b.id}"))
                }
                BuildableType.BASIC_BATTERY -> {
                    list.add(BoxCollider(b.posX, b.posZ, 1.8f, 1.4f, b.rotationDeg, "Placed_BasicBattery_${b.id}"))
                }
                BuildableType.ADVANCED_BATTERY -> {
                    list.add(BoxCollider(b.posX, b.posZ, 2.6f, 1.8f, b.rotationDeg, "Placed_AdvBattery_${b.id}"))
                }
                BuildableType.BATTERY_BANK -> {
                    list.add(BoxCollider(b.posX, b.posZ, 4.4f, 3.4f, b.rotationDeg, "Placed_BatteryBank_${b.id}"))
                }
                BuildableType.POWER_POLE -> {
                    list.add(CylinderCollider(b.posX, b.posZ, 0.35f, "Placed_PowerPole_${b.id}"))
                }
                BuildableType.NPC_CABIN -> {
                    list.add(BoxCollider(b.posX, b.posZ, 3.2f, 3.2f, b.rotationDeg, "Placed_NpcCabin_${b.id}"))
                }
                BuildableType.BUNKHOUSE -> {
                    list.add(BoxCollider(b.posX, b.posZ, 5.2f, 5.2f, b.rotationDeg, "Placed_Bunkhouse_${b.id}"))
                }
                BuildableType.KITCHEN -> {
                    list.add(BoxCollider(b.posX, b.posZ, 4.2f, 4.2f, b.rotationDeg, "Placed_Kitchen_${b.id}"))
                }
                BuildableType.MEDIC_STATION -> {
                    list.add(BoxCollider(b.posX, b.posZ, 3.2f, 3.2f, b.rotationDeg, "Placed_MedicStation_${b.id}"))
                }
                BuildableType.WORKSHOP -> {
                    list.add(BoxCollider(b.posX, b.posZ, 3.2f, 3.2f, b.rotationDeg, "Placed_Workshop_${b.id}"))
                }
                BuildableType.RESEARCH_LAB -> {
                    list.add(BoxCollider(b.posX, b.posZ, 4.2f, 4.2f, b.rotationDeg, "Placed_ResearchLab_${b.id}"))
                }
            }
        }

        // 6. Clean Energy Nodes
        for (node in energyNodes) {
            when (node.nodeType) {
                EnergyNodeType.PHOTOVOLTAIC_ARRAY -> {
                    list.add(BoxCollider(node.posX, node.posZ, 3.4f, 2.2f, 0f, "EnergyNode_${node.id}"))
                }
                EnergyNodeType.VERTICAL_WIND_TURBINE -> {
                    list.add(CylinderCollider(node.posX, node.posZ, 1.2f, "TurbineNode_${node.id}"))
                }
                EnergyNodeType.BATTERY_STORAGE_BANK -> {
                    list.add(BoxCollider(node.posX, node.posZ, 2.0f, 1.4f, 0f, "BatteryNode_${node.id}"))
                }
                else -> {
                    list.add(BoxCollider(node.posX, node.posZ, 2.5f, 2.5f, 0f, "Node_${node.id}"))
                }
            }
        }

        return list
    }
}
