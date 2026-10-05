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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
 * Top-Left HUD Mini-Map (135x135 px).
 * Features:
 * - Real-time top-down radar view around player (radius ~70m)
 * - Color-coded Biome zones (Green Valley, Deep Forest, Wetland)
 * - River stream & Wetland river flow
 * - Player Blue Beacon with pulsating radar ripple & heading orientation
 * - Landmark / POI icons and discovery state
 * - Tap to open Full World Map Modal
 */
@Composable
fun MiniMapHUD(
    playerX: Float,
    playerZ: Float,
    playerAngleDeg: Float,
    discoveredPois: Set<String>,
    discoveredChunks: Set<String>,
    currentBiome: BiomeType,
    plots: List<PlotEntity> = emptyList(),
    placedBuildings: List<PlacedBuildingEntity> = emptyList(),
    energyNodes: List<EnergyNodeEntity> = emptyList(),
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "minimap_pulse")
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_pulse"
    )

    Surface(
        modifier = modifier
            .size(118.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = androidx.compose.material3.ripple(bounded = true, radius = 60.dp)
            ) { onClick() }
            .testTag("hud_mini_map"),
        color = Color(0xF2081518),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            Brush.linearGradient(listOf(SolarEmerald, CleanCyan.copy(alpha = 0.6f)))
        ),
        shadowElevation = 8.dp
    ) {
        Box(modifier = Modifier.size(118.dp)) {
            Canvas(modifier = Modifier.size(118.dp)) {
                val canvasW = size.width
                val canvasH = size.height
                val center = Offset(canvasW * 0.5f, canvasH * 0.5f)

                // Scale: 1 canvas pixel = 0.9 world meters (~106m visible window)
                val meterToPx = 0.95f

                // World to MiniMap coordinate transform centered on Player
                fun worldToMap(wx: Float, wz: Float): Offset {
                    val dx = (wx - playerX) * meterToPx
                    val dz = (wz - playerZ) * meterToPx
                    // Map +X is right, Map +Y is up (which corresponds to world North: -Z)
                    return Offset(center.x + dx, center.y - dz)
                }

                // 1. Draw Biome Background Bands
                // Deep Forest (North, Z > 40)
                val forestBottom = worldToMap(0f, 40f).y
                if (forestBottom > 0) {
                    drawRect(
                        color = Color(0xFF133E18),
                        topLeft = Offset(0f, 0f),
                        size = Size(canvasW, forestBottom.coerceAtMost(canvasH))
                    )
                }

                // Green Valley (Center, -40 <= Z <= 40)
                val valleyTop = worldToMap(0f, 40f).y.coerceAtLeast(0f)
                val valleyBottom = worldToMap(0f, -40f).y.coerceAtMost(canvasH)
                if (valleyBottom > valleyTop) {
                    drawRect(
                        color = Color(0xFF2E6B34),
                        topLeft = Offset(0f, valleyTop),
                        size = Size(canvasW, valleyBottom - valleyTop)
                    )
                }

                // Wetland & River (South, Z < -40)
                val wetlandTop = worldToMap(0f, -40f).y
                if (wetlandTop < canvasH) {
                    drawRect(
                        color = Color(0xFF00564B),
                        topLeft = Offset(0f, wetlandTop.coerceAtLeast(0f)),
                        size = Size(canvasW, canvasH - wetlandTop.coerceAtLeast(0f))
                    )
                }

                // 2. Draw Wide Wetland River (East-to-West, Z = -100)
                val riverCenterY = worldToMap(0f, -100f).y
                val riverHeightPx = 12f * meterToPx
                if (riverCenterY + riverHeightPx > 0 && riverCenterY - riverHeightPx < canvasH) {
                    drawRect(
                        color = Color(0xFF0288D1),
                        topLeft = Offset(0f, riverCenterY - riverHeightPx * 0.5f),
                        size = Size(canvasW, riverHeightPx)
                    )
                }

                // 3. Draw Farm Stream (East tributary)
                val streamP1 = worldToMap(17f, 20f)
                val streamP2 = worldToMap(16f, -25f)
                drawLine(
                    color = Color(0xFF29B6F6),
                    start = streamP1,
                    end = streamP2,
                    strokeWidth = 3.5f
                )

                // 4. Draw Farm Plots & Static Buildings
                // Homestead
                val homePos = worldToMap(6f, 0f)
                drawCircle(color = SunGold, radius = 3.5f, center = homePos)

                // Barn
                val barnPos = worldToMap(-12f, 10f)
                drawCircle(color = Color(0xFFFF7043), radius = 3.2f, center = barnPos)

                // Workshop
                val workshopPos = worldToMap(0f, 14f)
                drawCircle(color = CleanCyan, radius = 3.0f, center = workshopPos)

                // Plots
                for (plot in plots) {
                    val p = worldToMap(plot.posX, plot.posZ)
                    drawRect(
                        color = if (plot.cropType != null) SolarEmerald else Color(0xFF6D4C41),
                        topLeft = Offset(p.x - 1.8f, p.y - 1.8f),
                        size = Size(3.6f, 3.6f)
                    )
                }

                // 5. Draw Discovered POIs
                for (poi in PointOfInterestType.values()) {
                    val isDiscovered = discoveredPois.contains(poi.id)
                    val poiPos = worldToMap(poi.worldX, poi.worldZ)
                    if (poiPos.x in -10f..canvasW + 10f && poiPos.y in -10f..canvasH + 10f) {
                        val poiColor = when (poi.biome) {
                            BiomeType.DEEP_FOREST -> if (isDiscovered) Color(0xFFFFD54F) else Color(0x66FFFFFF)
                            BiomeType.WETLAND -> if (isDiscovered) Color(0xFF00E5FF) else Color(0x66FFFFFF)
                            BiomeType.GREEN_VALLEY -> if (isDiscovered) Color(0xFF00E676) else Color(0x66FFFFFF)
                        }
                        drawCircle(color = poiColor, radius = if (isDiscovered) 4.0f else 2.5f, center = poiPos)
                        if (isDiscovered) {
                            drawCircle(color = Color.White, radius = 1.5f, center = poiPos)
                        }
                    }
                }

                // 6. Draw Fog of War Mask (Check surrounding chunks)
                val (pcx, pcz) = BiomeSystem.worldToChunk(playerX, playerZ)
                for (dx in -4..4) {
                    for (dz in -4..4) {
                        val cx = (pcx + dx).coerceIn(0, BiomeSystem.GRID_CHUNKS - 1)
                        val cz = (pcz + dz).coerceIn(0, BiomeSystem.GRID_CHUNKS - 1)
                        val key = "${cx}_${cz}"
                        if (!discoveredChunks.contains(key)) {
                            val (wx, wz) = BiomeSystem.chunkToWorldCenter(cx, cz)
                            val centerPt = worldToMap(wx, wz)
                            val cSize = BiomeSystem.CHUNK_SIZE * meterToPx
                            drawRect(
                                color = Color(0xAA081014),
                                topLeft = Offset(centerPt.x - cSize * 0.5f, centerPt.y - cSize * 0.5f),
                                size = Size(cSize, cSize)
                            )
                        }
                    }
                }

                // 7. Draw Player Blue Dot with Radar Ripple & Facing Direction
                // Pulsating ripple ring
                drawCircle(
                    color = CleanCyan.copy(alpha = (1f - (radarPulse - 4f) / 14f).coerceIn(0f, 0.6f)),
                    radius = radarPulse,
                    center = center,
                    style = Stroke(width = 1.5f)
                )

                // Glowing blue center beacon
                drawCircle(
                    color = CleanCyan,
                    radius = 4.5f,
                    center = center
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.0f,
                    center = center
                )

                // Player Heading Arrow
                val angleRad = Math.toRadians(playerAngleDeg.toDouble() - 90.0).toFloat()
                val arrowLen = 9.0f
                val tipX = center.x + cos(angleRad) * arrowLen
                val tipY = center.y - sin(angleRad) * arrowLen
                drawLine(
                    color = Color.White,
                    start = center,
                    end = Offset(tipX, tipY),
                    strokeWidth = 2.0f
                )

                // 8. Compass Overlay Crosshairs & North Marker
                drawCircle(
                    color = Color(0x3300E676),
                    radius = canvasW * 0.48f,
                    center = center,
                    style = Stroke(width = 1.0f)
                )
            }

            // North "N" Indicator
            Text(
                text = "N",
                color = SunGold,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 2.dp)
            )

            // Current Biome Tag at Bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 3.dp)
                    .background(Color(0xDD0B1C20), RoundedCornerShape(6.dp))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = currentBiome.displayName,
                    color = when (currentBiome) {
                        BiomeType.DEEP_FOREST -> Color(0xFF81C784)
                        BiomeType.WETLAND -> CleanCyan
                        BiomeType.GREEN_VALLEY -> SolarEmerald
                    },
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}
