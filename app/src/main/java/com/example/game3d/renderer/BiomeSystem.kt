package com.example.game3d.renderer

import com.example.data.model.BiomeType
import com.example.data.model.PointOfInterestType
import kotlin.math.sqrt

/**
 * Biome and Discovery System managing:
 * - 3 Connected Biomes: Green Valley (center), Deep Forest (north), Wetland & River (south)
 * - 400m x 400m World grid chunking & Fog of War exploration (20x20m chunks)
 * - Dynamic Point of Interest proximity discovery & rewards
 * - Biome-specific terrain drag & movement modifier
 */
object BiomeSystem {

    const val WORLD_MIN_X = -200.0f
    const val WORLD_MAX_X = 200.0f
    const val WORLD_MIN_Z = -200.0f
    const val WORLD_MAX_Z = 200.0f
    const val CHUNK_SIZE = 20.0f
    const val GRID_CHUNKS = 20 // 20x20 = 400 chunks

    /**
     * Determines active biome from player's Z coordinate.
     */
    fun getBiome(z: Float): BiomeType {
        return BiomeType.fromPosition(z)
    }

    /**
     * Converts world (X, Z) to chunk index coordinates (0..19, 0..19).
     */
    fun worldToChunk(x: Float, z: Float): Pair<Int, Int> {
        val chunkX = (((x - WORLD_MIN_X) / CHUNK_SIZE).toInt()).coerceIn(0, GRID_CHUNKS - 1)
        val chunkZ = (((z - WORLD_MIN_Z) / CHUNK_SIZE).toInt()).coerceIn(0, GRID_CHUNKS - 1)
        return Pair(chunkX, chunkZ)
    }

    /**
     * Converts chunk coordinate back to world center point.
     */
    fun chunkToWorldCenter(chunkX: Int, chunkZ: Int): Pair<Float, Float> {
        val wx = WORLD_MIN_X + (chunkX + 0.5f) * CHUNK_SIZE
        val wz = WORLD_MIN_Z + (chunkZ + 0.5f) * CHUNK_SIZE
        return Pair(wx, wz)
    }

    /**
     * Uncovers chunks within sight radius (35m) around the player.
     * Returns newly discovered chunk keys (e.g. "9_10").
     */
    fun uncoverChunksAround(
        playerX: Float,
        playerZ: Float,
        existingChunks: Set<String>,
        radiusMeters: Float = 35.0f
    ): Set<String> {
        val updated = existingChunks.toMutableSet()
        val (pcx, pcz) = worldToChunk(playerX, playerZ)
        val chunkRadius = ((radiusMeters / CHUNK_SIZE).toInt() + 1).coerceAtLeast(1)

        for (dx in -chunkRadius..chunkRadius) {
            for (dz in -chunkRadius..chunkRadius) {
                val cx = (pcx + dx).coerceIn(0, GRID_CHUNKS - 1)
                val cz = (pcz + dz).coerceIn(0, GRID_CHUNKS - 1)
                val (wx, wz) = chunkToWorldCenter(cx, cz)
                val dist = sqrt((playerX - wx) * (playerX - wx) + (playerZ - wz) * (playerZ - wz))
                if (dist <= radiusMeters + (CHUNK_SIZE * 0.5f)) {
                    updated.add("${cx}_${cz}")
                }
            }
        }
        return updated
    }

    /**
     * Checks if player is near any undiscovered Point of Interest.
     * Returns the POI if within discovery distance (14m), null otherwise.
     */
    fun checkPoiDiscovery(
        playerX: Float,
        playerZ: Float,
        discoveredPoiIds: Set<String>,
        discoveryRadius: Float = 14.0f
    ): PointOfInterestType? {
        for (poi in PointOfInterestType.values()) {
            if (discoveredPoiIds.contains(poi.id)) continue
            val dist = sqrt((playerX - poi.worldX) * (playerX - poi.worldX) + (playerZ - poi.worldZ) * (playerZ - poi.worldZ))
            if (dist <= discoveryRadius) {
                return poi
            }
        }
        return null
    }

    /**
     * Evaluates speed multiplier based on terrain type / biome.
     * Deep marshland / water slows down movement; dry roads / grass provide full speed.
     */
    fun getMovementSpeedMultiplier(x: Float, z: Float): Float {
        // Wetland marsh riverbed check
        if (z in -145.0f..-65.0f) {
            // Near river flow
            val distToRiverCenter = kotlin.math.abs(z - (-100.0f))
            if (distToRiverCenter < 14.0f) {
                // Crossing river shallows / marsh mud
                return 0.72f
            }
        }
        return 1.0f
    }
}
