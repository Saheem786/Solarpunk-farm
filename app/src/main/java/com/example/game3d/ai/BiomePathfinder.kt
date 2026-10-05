package com.example.game3d.ai

import com.example.data.local.LivestockEntity
import com.example.data.local.PlacedBuildingEntity
import com.example.data.model.BiomeType
import com.example.data.model.BuildableType
import java.util.PriorityQueue
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Waypoint data structure for 2D position vector.
 */
data class Vector2D(val x: Float, val z: Float) {
    fun distanceTo(other: Vector2D): Float {
        val dx = other.x - x
        val dz = other.z - z
        return sqrt(dx * dx + dz * dz)
    }
}

/**
 * A* Node representation for grid-based pathfinding.
 */
private class AStarNode(
    val gx: Int,
    val gz: Int,
    val worldX: Float,
    val worldZ: Float
) {
    var gScore: Float = Float.MAX_VALUE
    var hScore: Float = 0f
    val fScore: Float get() = gScore + hScore
    var parent: AStarNode? = null
    var isBlocked: Boolean = false
    var costMultiplier: Float = 1.0f

    fun reset() {
        gScore = Float.MAX_VALUE
        hScore = 0f
        fScore
        parent = null
        isBlocked = false
        costMultiplier = 1.0f
    }
}

/**
 * Inter-Biome and Chunk-Terrain A* Pathfinding Controller.
 * Resolves optimal paths across biomes, respects chunk terrain, and avoids collisions
 * with player-placed buildings and dynamic objects (livestock, energy nodes).
 */
class BiomePathfinder {

    companion object {
        const val MIN_WORLD_X = -100.0f
        const val MAX_WORLD_X = 100.0f
        const val MIN_WORLD_Z = -100.0f
        const val MAX_WORLD_Z = 100.0f
        const val GRID_CELL_SIZE = 1.5f // 1.5m per cell grid resolution

        val GRID_COLS = ((MAX_WORLD_X - MIN_WORLD_X) / GRID_CELL_SIZE).toInt() // ~133 cells
        val GRID_ROWS = ((MAX_WORLD_Z - MIN_WORLD_Z) / GRID_CELL_SIZE).toInt() // ~133 cells

        // Inter-Biome Transition Gates / Passes
        val VALLEY_WETLAND_GATE = Vector2D(35.0f, 0.0f)
        val VALLEY_FOREST_GATE = Vector2D(-45.0f, 0.0f)
        val FOREST_WETLAND_PASS = Vector2D(0.0f, 35.0f)

        private const val ARRIVAL_THRESHOLD = 0.5f
        private const val OBSTACLE_AVOIDANCE_MARGIN = 1.2f
    }

    private val nodeGrid: Array<Array<AStarNode>> = Array(GRID_COLS) { gx ->
        Array(GRID_ROWS) { gz ->
            val wx = MIN_WORLD_X + (gx + 0.5f) * GRID_CELL_SIZE
            val wz = MIN_WORLD_Z + (gz + 0.5f) * GRID_CELL_SIZE
            AStarNode(gx, gz, wx, wz)
        }
    }

    /**
     * Primary A* Pathfinding method calculating full collision-free paths between biomes,
     * avoiding player-placed buildings and dynamic livestock/objects.
     */
    fun findPathAStar(
        startX: Float,
        startZ: Float,
        destX: Float,
        destZ: Float,
        placedBuildings: List<PlacedBuildingEntity> = emptyList(),
        dynamicObjects: List<LivestockEntity> = emptyList(),
        startBiome: BiomeType = BiomeType.GREEN_VALLEY,
        targetBiome: BiomeType = BiomeType.GREEN_VALLEY
    ): List<Vector2D> {
        // Reset Grid
        for (col in 0 until GRID_COLS) {
            for (row in 0 until GRID_ROWS) {
                nodeGrid[col][row].reset()
            }
        }

        // 1. Mark Obstacles on Grid
        markPlacedBuildingObstacles(placedBuildings)
        markDynamicObjectObstacles(dynamicObjects)

        val startCol = toGridX(startX)
        val startRow = toGridZ(startZ)
        val destCol = toGridX(destX)
        val destRow = toGridZ(destZ)

        val startNode = nodeGrid[startCol][startRow]
        val goalNode = nodeGrid[destCol][destRow]

        // Ensure start and goal are walkable
        startNode.isBlocked = false
        goalNode.isBlocked = false

        // 2. Execute A* Search
        val openSet = PriorityQueue<AStarNode>(compareBy { it.fScore })
        val closedSet = HashSet<AStarNode>()

        startNode.gScore = 0f
        startNode.hScore = heuristic(startNode, goalNode)
        openSet.add(startNode)

        var foundGoal: AStarNode? = null

        while (openSet.isNotEmpty()) {
            val current = openSet.poll() ?: break

            if (current == goalNode || euclideanDistance(current, goalNode) <= GRID_CELL_SIZE * 1.2f) {
                foundGoal = current
                break
            }

            closedSet.add(current)

            // 8-directional neighbors
            val neighbors = getNeighbors(current)
            for (neighbor in neighbors) {
                if (closedSet.contains(neighbor) || neighbor.isBlocked) continue

                val isDiagonal = neighbor.gx != current.gx && neighbor.gz != current.gz
                val stepCost = if (isDiagonal) 1.414f * GRID_CELL_SIZE else GRID_CELL_SIZE
                val tentativeG = current.gScore + (stepCost * neighbor.costMultiplier)

                if (tentativeG < neighbor.gScore) {
                    neighbor.parent = current
                    neighbor.gScore = tentativeG
                    neighbor.hScore = heuristic(neighbor, goalNode)

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor)
                    }
                }
            }
        }

        // 3. Reconstruct Path & Apply Smoothing (String Pulling)
        if (foundGoal != null) {
            val rawPath = mutableListOf<Vector2D>()
            var curr: AStarNode? = foundGoal
            while (curr != null) {
                rawPath.add(Vector2D(curr.worldX, curr.worldZ))
                curr = curr.parent
            }
            rawPath.reverse()
            return smoothPath(rawPath, placedBuildings)
        }

        // Fallback: direct segment with obstacle bypass
        return listOf(Vector2D(startX, startZ), Vector2D(destX, destZ))
    }

    /**
     * Steers the NPC along calculated waypoints.
     */
    fun stepAlongPath(
        currentX: Float,
        currentZ: Float,
        path: List<Vector2D>,
        speed: Float,
        deltaSec: Float
    ): StepResult {
        if (path.isEmpty()) {
            return StepResult(currentX, currentZ, 0f, isWalking = false, isArrived = true, remainingPath = emptyList())
        }

        val target = path.first()
        val dx = target.x - currentX
        val dz = target.z - currentZ
        val dist = sqrt(dx * dx + dz * dz)

        if (dist <= ARRIVAL_THRESHOLD) {
            val remaining = path.drop(1)
            return if (remaining.isEmpty()) {
                StepResult(target.x, target.z, 0f, isWalking = false, isArrived = true, remainingPath = emptyList())
            } else {
                stepAlongPath(currentX, currentZ, remaining, speed, deltaSec)
            }
        }

        val moveDist = speed * deltaSec
        val stepRatio = (moveDist / dist).coerceAtMost(1.0f)

        val nextX = currentX + dx * stepRatio
        val nextZ = currentZ + dz * stepRatio
        val rotDeg = Math.toDegrees(atan2(dx.toDouble(), dz.toDouble())).toFloat()

        return StepResult(nextX, nextZ, rotDeg, isWalking = true, isArrived = false, remainingPath = path)
    }

    /**
     * Clamps coordinates within the boundaries of the specified biome.
     */
    fun clampToBiomeBounds(x: Float, z: Float, biome: BiomeType): Vector2D {
        val bounds = when (biome) {
            BiomeType.GREEN_VALLEY -> floatArrayOf(-45.0f, 35.0f, -45.0f, 35.0f)
            BiomeType.DEEP_FOREST -> floatArrayOf(-100.0f, -45.0f, -50.0f, 50.0f)
            BiomeType.WETLAND -> floatArrayOf(35.0f, 100.0f, -40.0f, 40.0f)
        }

        val clampedX = x.coerceIn(bounds[0] + 1.0f, bounds[1] - 1.0f)
        val clampedZ = z.coerceIn(bounds[2] + 1.0f, bounds[3] - 1.0f)
        return Vector2D(clampedX, clampedZ)
    }

    private fun markPlacedBuildingObstacles(buildings: List<PlacedBuildingEntity>) {
        for (b in buildings) {
            val radius = getBuildingRadius(b.buildingType) + OBSTACLE_AVOIDANCE_MARGIN
            val minGx = toGridX(b.posX - radius)
            val maxGx = toGridX(b.posX + radius)
            val minGz = toGridZ(b.posZ - radius)
            val maxGz = toGridZ(b.posZ + radius)

            for (gx in minGx..maxGx) {
                for (gz in minGz..maxGz) {
                    if (isValidGrid(gx, gz)) {
                        nodeGrid[gx][gz].isBlocked = true
                    }
                }
            }
        }
    }

    private fun markDynamicObjectObstacles(livestock: List<LivestockEntity>) {
        for (animal in livestock) {
            val radius = 1.0f
            val gx = toGridX(animal.posX)
            val gz = toGridZ(animal.posZ)
            if (isValidGrid(gx, gz)) {
                nodeGrid[gx][gz].costMultiplier = 2.5f // Higher path cost to bypass moving livestock
            }
        }
    }

    private fun smoothPath(rawPath: List<Vector2D>, buildings: List<PlacedBuildingEntity>): List<Vector2D> {
        if (rawPath.size <= 2) return rawPath

        val smoothed = mutableListOf<Vector2D>()
        smoothed.add(rawPath.first())

        var currentIdx = 0
        while (currentIdx < rawPath.size - 1) {
            var nextIdx = rawPath.size - 1
            while (nextIdx > currentIdx + 1) {
                if (hasLineOfSight(smoothed.last(), rawPath[nextIdx], buildings)) {
                    break
                }
                nextIdx--
            }
            smoothed.add(rawPath[nextIdx])
            currentIdx = nextIdx
        }

        return smoothed
    }

    private fun hasLineOfSight(a: Vector2D, b: Vector2D, buildings: List<PlacedBuildingEntity>): Boolean {
        for (bld in buildings) {
            val radius = getBuildingRadius(bld.buildingType)
            val distToSegment = pointToSegmentDistance(Vector2D(bld.posX, bld.posZ), a, b)
            if (distToSegment < radius + 0.8f) {
                return false
            }
        }
        return true
    }

    private fun pointToSegmentDistance(p: Vector2D, a: Vector2D, b: Vector2D): Float {
        val abX = b.x - a.x
        val abZ = b.z - a.z
        val apX = p.x - a.x
        val apZ = p.z - a.z

        val ab2 = abX * abX + abZ * abZ
        if (ab2 == 0f) return a.distanceTo(p)

        val t = ((apX * abX + apZ * abZ) / ab2).coerceIn(0.0f, 1.0f)
        val closestX = a.x + t * abX
        val closestZ = a.z + t * abZ

        val dx = p.x - closestX
        val dz = p.z - closestZ
        return sqrt(dx * dx + dz * dz)
    }

    private fun getBuildingRadius(type: BuildableType): Float {
        return when (type) {
            BuildableType.CABIN -> 2.0f
            BuildableType.BUNKHOUSE -> 3.2f
            BuildableType.GREENHOUSE -> 2.5f
            BuildableType.STORAGE -> 2.2f
            BuildableType.SOLAR_PANEL, BuildableType.ADVANCED_SOLAR -> 1.5f
            BuildableType.WINDMILL -> 1.8f
            BuildableType.HYDRO_GENERATOR, BuildableType.BIOGAS_GENERATOR -> 2.0f
            BuildableType.GEOTHERMAL_VENT -> 2.2f
            BuildableType.KITCHEN, BuildableType.RESEARCH_LAB -> 2.8f
            BuildableType.MEDIC_STATION, BuildableType.WORKSHOP -> 2.2f
            else -> 1.2f
        }
    }

    private fun getNeighbors(node: AStarNode): List<AStarNode> {
        val neighbors = mutableListOf<AStarNode>()
        val directions = arrayOf(
            Pair(0, 1), Pair(0, -1), Pair(1, 0), Pair(-1, 0),
            Pair(1, 1), Pair(1, -1), Pair(-1, 1), Pair(-1, -1)
        )

        for (dir in directions) {
            val ngx = node.gx + dir.first
            val ngz = node.gz + dir.second
            if (isValidGrid(ngx, ngz)) {
                neighbors.add(nodeGrid[ngx][ngz])
            }
        }
        return neighbors
    }

    private fun heuristic(a: AStarNode, b: AStarNode): Float {
        val dx = abs(a.worldX - b.worldX)
        val dz = abs(a.worldZ - b.worldZ)
        return sqrt(dx * dx + dz * dz)
    }

    private fun euclideanDistance(a: AStarNode, b: AStarNode): Float {
        val dx = a.worldX - b.worldX
        val dz = a.worldZ - b.worldZ
        return sqrt(dx * dx + dz * dz)
    }

    private fun toGridX(worldX: Float): Int {
        return ((worldX - MIN_WORLD_X) / GRID_CELL_SIZE).toInt().coerceIn(0, GRID_COLS - 1)
    }

    private fun toGridZ(worldZ: Float): Int {
        return ((worldZ - MIN_WORLD_Z) / GRID_CELL_SIZE).toInt().coerceIn(0, GRID_ROWS - 1)
    }

    private fun isValidGrid(gx: Int, gz: Int): Boolean {
        return gx in 0 until GRID_COLS && gz in 0 until GRID_ROWS
    }

    data class StepResult(
        val x: Float,
        val z: Float,
        val rotationDeg: Float,
        val isWalking: Boolean,
        val isArrived: Boolean,
        val remainingPath: List<Vector2D>
    )
}
