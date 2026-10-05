package com.example.ui.game3d

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.EnergyNodeEntity
import com.example.data.local.PlacedBuildingEntity
import com.example.data.local.PlotEntity
import com.example.data.model.BiomeType
import com.example.data.model.PointOfInterestType
import com.example.game3d.renderer.BiomeSystem
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SunGold
import kotlin.math.cos
import kotlin.math.sin

/**
 * Full 400m x 400m Solarpunk Holographic World Map Modal.
 * Supports:
 * - 3 Connected Biomes (Deep Forest, Green Valley, Wetland)
 * - Dynamic Fog of War coverage based on explored 20x20m chunks
 * - Points of Interest discovery tracking & reward details
 * - Real-time Player GPS beacon & orientation
 * - Animal sightings & placed structures markers
 * - Interactive Pan, Pinch-Zoom & Reset controls
 */
@Composable
fun WorldMapModal(
    playerX: Float,
    playerZ: Float,
    playerAngleDeg: Float,
    discoveredPois: Set<String>,
    discoveredChunks: Set<String>,
    currentBiome: BiomeType,
    plots: List<PlotEntity> = emptyList(),
    placedBuildings: List<PlacedBuildingEntity> = emptyList(),
    energyNodes: List<EnergyNodeEntity> = emptyList(),
    onDismiss: () -> Unit
) {
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }
    var selectedPoi by remember { mutableStateOf<PointOfInterestType?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "map_beacon_pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "beacon_pulse"
    )

    val totalChunks = BiomeSystem.GRID_CHUNKS * BiomeSystem.GRID_CHUNKS // 400 chunks
    val exploredPercent = ((discoveredChunks.size.toFloat() / totalChunks.toFloat()) * 100f).toInt().coerceIn(0, 100)
    val poisDiscoveredCount = discoveredPois.size
    val totalPoisCount = PointOfInterestType.values().size

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("world_map_modal"),
            color = Color(0xF80A1518),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, SolarEmerald.copy(alpha = 0.8f))
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val boxWidth = maxWidth
                val boxHeight = maxHeight

                // Map Canvas Area with Touch Gestures (Pan & Pinch-to-Zoom)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                zoomScale = (zoomScale * zoom).coerceIn(0.6f, 3.2f)
                                panOffsetX += pan.x
                                panOffsetY += pan.y
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasW = size.width
                        val canvasH = size.height
                        val center = Offset(canvasW * 0.5f + panOffsetX, canvasH * 0.5f + panOffsetY)

                        // 400m x 400m mapped to canvas: base dimension = min(canvasW, canvasH) * 0.85
                        val baseDimension = kotlin.math.min(canvasW, canvasH) * 0.85f * zoomScale
                        val worldScale = baseDimension / 400.0f // px per world meter

                        fun worldToScreen(wx: Float, wz: Float): Offset {
                            val sx = center.x + wx * worldScale
                            val sy = center.y - wz * worldScale // World +Z is North (screen up: -Y)
                            return Offset(sx, sy)
                        }

                        val halfWorldPx = 200.0f * worldScale

                        // 1. Draw 3 Biome Backgrounds (Total 400m x 400m)
                        // A. Deep Forest (North: Z from +40 to +200)
                        val forestTopLeft = worldToScreen(-200f, 200f)
                        val forestBottomRight = worldToScreen(200f, 40f)
                        drawRect(
                            color = Color(0xFF1B5E20), // Dark Green
                            topLeft = forestTopLeft,
                            size = Size(forestBottomRight.x - forestTopLeft.x, forestBottomRight.y - forestTopLeft.y)
                        )

                        // B. Green Valley (Center: Z from -40 to +40)
                        val valleyTopLeft = worldToScreen(-200f, 40f)
                        val valleyBottomRight = worldToScreen(200f, -40f)
                        drawRect(
                            color = Color(0xFF388E3C), // Light Green
                            topLeft = valleyTopLeft,
                            size = Size(valleyBottomRight.x - valleyTopLeft.x, valleyBottomRight.y - valleyTopLeft.y)
                        )

                        // C. Wetland & River (South: Z from -40 to -200)
                        val wetlandTopLeft = worldToScreen(-200f, -40f)
                        val wetlandBottomRight = worldToScreen(200f, -200f)
                        drawRect(
                            color = Color(0xFF00796B), // Blue-Green / Marsh Teal
                            topLeft = wetlandTopLeft,
                            size = Size(wetlandBottomRight.x - wetlandTopLeft.x, wetlandBottomRight.y - wetlandTopLeft.y)
                        )

                        // 2. Draw Major Water Bodies
                        // Wide Wetland River (Z = -100m, East-to-West, 12m wide)
                        val riverTL = worldToScreen(-200f, -94f)
                        val riverBR = worldToScreen(200f, -106f)
                        drawRect(
                            color = Color(0xFF0288D1),
                            topLeft = riverTL,
                            size = Size(riverBR.x - riverTL.x, riverBR.y - riverTL.y)
                        )

                        // Valley Stream Tributary
                        val streamStart = worldToScreen(18f, 25f)
                        val streamEnd = worldToScreen(16f, -35f)
                        drawLine(
                            color = Color(0xFF29B6F6),
                            start = streamStart,
                            end = streamEnd,
                            strokeWidth = 4.0f * zoomScale
                        )

                        // Farm Pond (10, -10)
                        val pondCenter = worldToScreen(10f, -10f)
                        drawCircle(
                            color = Color(0xFF03A9F4),
                            radius = 5.5f * worldScale,
                            center = pondCenter
                        )

                        // 3. Draw Farm Buildings & Farm Plots
                        // Homestead (6.0, 0.0)
                        val housePos = worldToScreen(6f, 0f)
                        drawRect(
                            color = SunGold,
                            topLeft = Offset(housePos.x - 4f * worldScale, housePos.y - 3f * worldScale),
                            size = Size(8f * worldScale, 6f * worldScale)
                        )

                        // Barn (-12.0, 10.0)
                        val barnPos = worldToScreen(-12f, 10f)
                        drawRect(
                            color = Color(0xFFFF7043),
                            topLeft = Offset(barnPos.x - 4f * worldScale, barnPos.y - 3f * worldScale),
                            size = Size(8f * worldScale, 6f * worldScale)
                        )

                        // Workshop (0.0, 14.0)
                        val wsPos = worldToScreen(0f, 14f)
                        drawRect(
                            color = CleanCyan,
                            topLeft = Offset(wsPos.x - 3f * worldScale, wsPos.y - 2.5f * worldScale),
                            size = Size(6f * worldScale, 5f * worldScale)
                        )

                        // Farm Plots
                        for (plot in plots) {
                            val pos = worldToScreen(plot.posX, plot.posZ)
                            drawRect(
                                color = if (plot.cropType != null) SolarEmerald else Color(0xFF795548),
                                topLeft = Offset(pos.x - 1.2f * worldScale, pos.y - 1.2f * worldScale),
                                size = Size(2.4f * worldScale, 2.4f * worldScale)
                            )
                        }

                        // Placed Buildings
                        for (b in placedBuildings) {
                            val pos = worldToScreen(b.posX, b.posZ)
                            drawCircle(
                                color = Color(0xFFAB47BC),
                                radius = 2.5f * worldScale,
                                center = pos
                            )
                        }

                        // 4. Draw Animal Sightings
                        val animalSightings = listOf(
                            Triple(-10f, -8f, "Meadow Cows"),
                            Triple(-16f, 0f, "Solar Sheep"),
                            Triple(4.5f, 2f, "Free-Range Chickens"),
                            Triple(-4f, -14f, "Robo-Bees"),
                            Triple(8f, 65f, "Forest Deer"),
                            Triple(-10f, 55f, "Forest Rabbits"),
                            Triple(14f, 110f, "Tower Owls"),
                            Triple(-5f, 48f, "Forest Fox"),
                            Triple(-8f, -98f, "Wetland Ducks"),
                            Triple(10f, -10f, "Marsh Frogs"),
                            Triple(-15f, -94f, "Wetland Herons"),
                            Triple(0f, -100f, "Swimming Fish")
                        )
                        for (sighting in animalSightings) {
                            val sPos = worldToScreen(sighting.first, sighting.second)
                            drawCircle(
                                color = Color(0xFFFFEE58),
                                radius = 2.8f * zoomScale,
                                center = sPos
                            )
                        }

                        // 5. Draw Points of Interest
                        for (poi in PointOfInterestType.values()) {
                            val isDiscovered = discoveredPois.contains(poi.id)
                            val poiPos = worldToScreen(poi.worldX, poi.worldZ)

                            val poiColor = when (poi.biome) {
                                BiomeType.DEEP_FOREST -> if (isDiscovered) Color(0xFFFFD54F) else Color(0x66FFFFFF)
                                BiomeType.WETLAND -> if (isDiscovered) Color(0xFF00E5FF) else Color(0x66FFFFFF)
                                BiomeType.GREEN_VALLEY -> if (isDiscovered) Color(0xFF00E676) else Color(0x66FFFFFF)
                            }

                            // POI Outer Halo
                            drawCircle(
                                color = poiColor.copy(alpha = if (isDiscovered) 0.8f else 0.4f),
                                radius = if (isDiscovered) 7f * zoomScale else 4.5f * zoomScale,
                                center = poiPos
                            )
                            drawCircle(
                                color = if (isDiscovered) Color.White else Color(0xAA000000),
                                radius = 3.5f * zoomScale,
                                center = poiPos
                            )
                        }

                        // 6. Draw Fog of War (20x20 Grid covering 400m x 400m)
                        val chunkSizeMeters = BiomeSystem.CHUNK_SIZE
                        for (cx in 0 until BiomeSystem.GRID_CHUNKS) {
                            for (cz in 0 until BiomeSystem.GRID_CHUNKS) {
                                val key = "${cx}_${cz}"
                                if (!discoveredChunks.contains(key)) {
                                    val (wx, wz) = BiomeSystem.chunkToWorldCenter(cx, cz)
                                    val chunkTL = worldToScreen(wx - chunkSizeMeters * 0.5f, wz + chunkSizeMeters * 0.5f)
                                    val chunkBR = worldToScreen(wx + chunkSizeMeters * 0.5f, wz - chunkSizeMeters * 0.5f)
                                    val cW = chunkBR.x - chunkTL.x
                                    val cH = chunkBR.y - chunkTL.y

                                    // Dark fog covering unexplored chunk
                                    drawRect(
                                        color = Color(0xEB0A1215),
                                        topLeft = chunkTL,
                                        size = Size(cW, cH)
                                    )
                                    // Grid line
                                    drawRect(
                                        color = Color(0x2200E676),
                                        topLeft = chunkTL,
                                        size = Size(cW, cH),
                                        style = Stroke(width = 0.8f)
                                    )
                                }
                            }
                        }

                        // 7. Draw World Boundary Perimeter (±200m)
                        val worldTL = worldToScreen(-200f, 200f)
                        val worldBR = worldToScreen(200f, -200f)
                        drawRect(
                            color = Color(0x9900E676),
                            topLeft = worldTL,
                            size = Size(worldBR.x - worldTL.x, worldBR.y - worldTL.y),
                            style = Stroke(width = 2.5f * zoomScale)
                        )

                        // 8. Draw Player Blue Beacon
                        val playerScreenPos = worldToScreen(playerX, playerZ)

                        // Pulsating Radar Wave
                        drawCircle(
                            color = CleanCyan.copy(alpha = (1f - (pulseRadius - 6f) / 18f).coerceIn(0f, 0.7f)),
                            radius = pulseRadius * zoomScale,
                            center = playerScreenPos,
                            style = Stroke(width = 2.0f)
                        )
                        // Player Center Beacon
                        drawCircle(
                            color = CleanCyan,
                            radius = 7f * zoomScale,
                            center = playerScreenPos
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.5f * zoomScale,
                            center = playerScreenPos
                        )

                        // Player Orientation Arrow
                        val angleRad = Math.toRadians(playerAngleDeg.toDouble() - 90.0).toFloat()
                        val arrowLen = 14.0f * zoomScale
                        val tipX = playerScreenPos.x + cos(angleRad) * arrowLen
                        val tipY = playerScreenPos.y - sin(angleRad) * arrowLen
                        drawLine(
                            color = Color.White,
                            start = playerScreenPos,
                            end = Offset(tipX, tipY),
                            strokeWidth = 3.0f * zoomScale
                        )
                    }
                }

                // Top Header Overlay: Title + Biome Exploration Status + Close Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(12.dp),
                    color = Color(0xEE08161A),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SolarEmerald.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Title & Biome Indicator
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = "World Map",
                                tint = SolarEmerald,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "SOLARPUNK SANCTUARY MAP",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Current Biome: ${currentBiome.displayName} • Explored: $exploredPercent%",
                                    color = CleanCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Discovery Stats Pill & Close Button
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color(0x6600E676),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "POIs",
                                        tint = SunGold,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "POIs: $poisDiscoveredCount/$totalPoisCount",
                                        color = SunGold,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("btn_close_world_map")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Map",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Left-Side Biome Legend & POI Checklist Card
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 12.dp, bottom = 12.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    color = Color(0xEE09171A),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3300E676))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "3 CONNECTED BIOMES",
                            color = SunGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFF1B5E20), CircleShape))
                            Text("Deep Forest (North)", color = Color.White, fontSize = 8.5.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFF388E3C), CircleShape))
                            Text("Green Valley (Center)", color = Color.White, fontSize = 8.5.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFF00796B), CircleShape))
                            Text("Wetland & River (South)", color = Color.White, fontSize = 8.5.sp)
                        }
                    }
                }

                // Right-Side Zoom & Reset Controls
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Reset to Player
                    Surface(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable {
                                zoomScale = 1.0f
                                panOffsetX = 0f
                                panOffsetY = 0f
                            }
                            .testTag("btn_map_center_player"),
                        color = Color(0xDD0B2024),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CleanCyan.copy(alpha = 0.7f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Center on Player",
                                tint = CleanCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Zoom In
                    Surface(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable {
                                zoomScale = (zoomScale + 0.3f).coerceAtMost(3.2f)
                            }
                            .testTag("btn_map_zoom_in"),
                        color = Color(0xDD0B2024),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SolarEmerald.copy(alpha = 0.7f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Zoom In",
                                tint = SolarEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Zoom Out
                    Surface(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable {
                                zoomScale = (zoomScale - 0.3f).coerceAtLeast(0.6f)
                            }
                            .testTag("btn_map_zoom_out"),
                        color = Color(0xDD0B2024),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SolarEmerald.copy(alpha = 0.7f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Zoom Out",
                                tint = SolarEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
