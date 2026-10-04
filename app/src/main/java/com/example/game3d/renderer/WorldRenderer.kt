package com.example.game3d.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.data.local.EnergyNodeEntity
import com.example.data.local.LivestockEntity
import com.example.data.local.PlotEntity
import com.example.data.model.CropStage
import com.example.data.model.CropType
import com.example.data.model.EnergyNodeType
import com.example.data.model.LivestockType
import com.example.data.model.PlotType
import com.example.data.model.WeatherType
import com.example.game3d.player.ThirdPersonCamera
import com.example.game3d.player.ThirdPersonPlayer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class WorldRenderer {

    // Projection calculation helpers
    private fun project3D(
        wx: Float,
        wy: Float,
        wz: Float,
        camera: ThirdPersonCamera,
        screenWidth: Float,
        screenHeight: Float
    ): Triple<Float, Float, Float>? {
        // Translate relative to camera target
        val dx = wx - camera.targetX
        val dy = wy - camera.targetY
        val dz = wz - camera.targetZ

        val yawRad = Math.toRadians(-camera.yawDeg.toDouble()).toFloat()
        val pitchRad = Math.toRadians(camera.pitchDeg.toDouble()).toFloat()

        // Rotate around Y (yaw)
        val rx = dx * cos(yawRad) - dz * sin(yawRad)
        val rz1 = dx * sin(yawRad) + dz * cos(yawRad)

        // Rotate around X (pitch)
        val ry = dy * cos(pitchRad) + rz1 * sin(pitchRad)
        val depth = -dy * sin(pitchRad) + rz1 * cos(pitchRad) + camera.distance

        if (depth <= 1.0f) return null // Behind camera

        val fovFactor = (screenHeight * 0.95f) / depth
        val screenX = (screenWidth * 0.5f) + rx * fovFactor
        val screenY = (screenHeight * 0.5f) - ry * fovFactor

        return Triple(screenX, screenY, depth)
    }

    fun renderWorld(
        drawScope: DrawScope,
        player: ThirdPersonPlayer,
        camera: ThirdPersonCamera,
        lighting: LightingState,
        weather: WeatherType,
        plots: List<PlotEntity>,
        energyNodes: List<EnergyNodeEntity>,
        livestock: List<LivestockEntity>,
        animTimeSec: Float
    ) {
        val width = drawScope.size.width
        val height = drawScope.size.height

        // 1. Draw Sky Dome Gradient & Stars
        drawSky(drawScope, width, height, lighting, animTimeSec)

        // 2. Draw Distant Solarpunk Arcology Horizon
        drawHorizonCity(drawScope, width, height, camera, lighting)

        // 3. Prepare Render Objects with Depth Sorting
        val renderList = mutableListOf<RenderableObject>()

        // Add Terrain Tiles
        for (gx in -20..20 step 4) {
            for (gz in -20..20 step 4) {
                renderList.add(
                    RenderableObject(
                        worldX = gx.toFloat(),
                        worldY = -0.05f,
                        worldZ = gz.toFloat(),
                        type = RenderType.TERRAIN_TILE
                    )
                )
            }
        }

        // Add Pathways
        renderList.add(RenderableObject(0.0f, 0.0f, 0.0f, RenderType.PATHWAYS))

        // Add Farm Buildings
        renderList.add(RenderableObject(0.0f, 0.0f, 14.0f, RenderType.WORKSHOP_BUILDING))
        renderList.add(RenderableObject(-14.0f, 0.0f, -14.0f, RenderType.MARKET_BUILDING))

        // Add Plots
        for (plot in plots) {
            renderList.add(
                RenderableObject(
                    worldX = plot.posX,
                    worldY = plot.posY,
                    worldZ = plot.posZ,
                    type = RenderType.FARM_PLOT,
                    data = plot
                )
            )
        }

        // Add Energy Nodes
        for (node in energyNodes) {
            renderList.add(
                RenderableObject(
                    worldX = node.posX,
                    worldY = node.posY,
                    worldZ = node.posZ,
                    type = RenderType.ENERGY_NODE,
                    data = node
                )
            )
        }

        // Add Livestock
        for (animal in livestock) {
            renderList.add(
                RenderableObject(
                    worldX = animal.posX,
                    worldY = animal.posY,
                    worldZ = animal.posZ,
                    type = RenderType.LIVESTOCK,
                    data = animal
                )
            )
        }

        // Add Player Avatar
        renderList.add(
            RenderableObject(
                worldX = player.posX,
                worldY = player.posY,
                worldZ = player.posZ,
                type = RenderType.PLAYER_AVATAR
            )
        )

        // Project and Sort from farthest to nearest (Painters algorithm)
        val projectedList = renderList.mapNotNull { obj ->
            val p = project3D(obj.worldX, obj.worldY, obj.worldZ, camera, width, height)
            if (p != null) {
                obj.apply {
                    screenX = p.first
                    screenY = p.second
                    depth = p.third
                }
            } else null
        }.sortedByDescending { it.depth }

        // Render each object in depth order
        for (obj in projectedList) {
            when (obj.type) {
                RenderType.TERRAIN_TILE -> drawTerrainTile(drawScope, obj, camera, lighting, width, height)
                RenderType.PATHWAYS -> drawPathways(drawScope, camera, lighting, width, height)
                RenderType.FARM_PLOT -> (obj.data as? PlotEntity)?.let { drawFarmPlot(drawScope, obj, it, lighting, animTimeSec) }
                RenderType.ENERGY_NODE -> (obj.data as? EnergyNodeEntity)?.let { drawEnergyNode(drawScope, obj, it, lighting, animTimeSec) }
                RenderType.LIVESTOCK -> (obj.data as? LivestockEntity)?.let { drawLivestock(drawScope, obj, it, lighting, animTimeSec) }
                RenderType.PLAYER_AVATAR -> drawPlayer(drawScope, obj, player, lighting, animTimeSec)
                RenderType.WORKSHOP_BUILDING -> drawWorkshopBuilding(drawScope, obj, lighting, animTimeSec)
                RenderType.MARKET_BUILDING -> drawMarketBuilding(drawScope, obj, lighting, animTimeSec)
            }
        }

        // 4. Draw Weather FX & Atmospheric Particles
        drawWeatherParticles(drawScope, width, height, weather, animTimeSec, lighting)
    }

    private fun drawSky(drawScope: DrawScope, w: Float, h: Float, lighting: LightingState, animTime: Float) {
        val skyBrush = Brush.verticalGradient(
            colors = listOf(lighting.skyTopColor, lighting.skyHorizonColor),
            startY = 0.0f,
            endY = h * 0.75f
        )
        drawScope.drawRect(brush = skyBrush, size = Size(w, h))

        // Draw Celestial Stars if Night or Twilight
        if (lighting.isNight || lighting.ambientIntensity < 0.6f) {
            val starAlpha = if (lighting.isNight) 0.85f else (0.6f - lighting.ambientIntensity) * 2.0f
            for (i in 0 until 40) {
                val starX = (sin(i.toDouble() * 37.1) * 0.5 + 0.5).toFloat() * w
                val starY = (cos(i.toDouble() * 19.3) * 0.5 + 0.5).toFloat() * (h * 0.45f)
                val twinkle = (sin((animTime * 3.0f + i * 2.1f).toDouble()) * 0.3 + 0.7).toFloat() * starAlpha
                drawScope.drawCircle(
                    color = Color.White.copy(alpha = min(1.0f, twinkle)),
                    radius = if (i % 5 == 0) 2.2f else 1.2f,
                    center = Offset(starX, starY)
                )
            }
        }

        // Draw Sun or Moon
        val celestialX = lighting.sunScreenPosX * w
        val celestialY = lighting.sunScreenPosY * (h * 0.4f) + (h * 0.08f)

        if (!lighting.isNight) {
            // Radiant Golden Sun
            drawScope.drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFEE58), Color(0xFFFFB300).copy(alpha = 0.6f), Color.Transparent),
                    center = Offset(celestialX, celestialY),
                    radius = 55.0f
                ),
                radius = 55.0f,
                center = Offset(celestialX, celestialY)
            )
            drawScope.drawCircle(
                color = Color(0xFFFFF9C4),
                radius = 18.0f,
                center = Offset(celestialX, celestialY)
            )
        } else {
            // Luminous Silver Moon
            drawScope.drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFE0E0E0), Color(0xFF80D8FF).copy(alpha = 0.4f), Color.Transparent),
                    center = Offset(celestialX, celestialY),
                    radius = 42.0f
                ),
                radius = 42.0f,
                center = Offset(celestialX, celestialY)
            )
            drawScope.drawCircle(
                color = Color(0xFFF5F5F5),
                radius = 14.0f,
                center = Offset(celestialX, celestialY)
            )
        }
    }

    private fun drawHorizonCity(drawScope: DrawScope, w: Float, h: Float, camera: ThirdPersonCamera, lighting: LightingState) {
        val horizonY = h * 0.48f
        val cityColor = lighting.skyHorizonColor.copy(alpha = 0.4f)

        val cityPath = Path().apply {
            moveTo(0f, horizonY)
            // Procedural geometric arcology spires
            val step = w / 16f
            for (i in 0..16) {
                val cx = i * step
                val spireHeight = (sin((i + camera.yawDeg * 0.02).toDouble()) * 25.0 + 35.0).toFloat()
                lineTo(cx, horizonY - spireHeight)
                lineTo(cx + step * 0.6f, horizonY - spireHeight * 0.6f)
            }
            lineTo(w, horizonY)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawScope.drawPath(path = cityPath, color = cityColor)
    }

    private fun drawTerrainTile(
        drawScope: DrawScope,
        obj: RenderableObject,
        camera: ThirdPersonCamera,
        lighting: LightingState,
        w: Float,
        h: Float
    ) {
        val size = 2.0f
        val p1 = project3D(obj.worldX - size, 0f, obj.worldZ - size, camera, w, h) ?: return
        val p2 = project3D(obj.worldX + size, 0f, obj.worldZ - size, camera, w, h) ?: return
        val p3 = project3D(obj.worldX + size, 0f, obj.worldZ + size, camera, w, h) ?: return
        val p4 = project3D(obj.worldX - size, 0f, obj.worldZ + size, camera, w, h) ?: return

        val poly = Path().apply {
            moveTo(p1.first, p1.second)
            lineTo(p2.first, p2.second)
            lineTo(p3.first, p3.second)
            lineTo(p4.first, p4.second)
            close()
        }

        val baseGrass = Color(0xFF1E5238)
        val grassColor = Color(
            red = baseGrass.red * lighting.ambientIntensity * (if ((obj.worldX.toInt() + obj.worldZ.toInt()) % 2 == 0) 1.0f else 0.92f),
            green = baseGrass.green * lighting.ambientIntensity * (if ((obj.worldX.toInt() + obj.worldZ.toInt()) % 2 == 0) 1.0f else 0.92f),
            blue = baseGrass.blue * lighting.ambientIntensity
        )

        drawScope.drawPath(path = poly, color = grassColor)
        drawScope.drawPath(path = poly, color = Color(0x1A00E676), style = Stroke(width = 0.8f))
    }

    private fun drawPathways(
        drawScope: DrawScope,
        camera: ThirdPersonCamera,
        lighting: LightingState,
        w: Float,
        h: Float
    ) {
        val pStart = project3D(0f, 0.01f, -18f, camera, w, h) ?: return
        val pEnd = project3D(0f, 0.01f, 18f, camera, w, h) ?: return
        val pathColor = Color(0xFFD7CCC8).copy(alpha = min(1.0f, lighting.ambientIntensity * 0.8f))

        drawScope.drawLine(
            color = pathColor,
            start = Offset(pStart.first, pStart.second),
            end = Offset(pEnd.first, pEnd.second),
            strokeWidth = (w / (pStart.third * 1.5f)).coerceIn(4f, 22f)
        )
    }

    private fun drawFarmPlot(
        drawScope: DrawScope,
        obj: RenderableObject,
        plot: PlotEntity,
        lighting: LightingState,
        animTime: Float
    ) {
        val scale = (140.0f / obj.depth).coerceIn(6.0f, 50.0f)
        val sx = obj.screenX
        val sy = obj.screenY

        // Draw Plot Shadow
        drawScope.drawOval(
            color = Color.Black.copy(alpha = lighting.shadowAlpha * 0.7f),
            topLeft = Offset(sx - scale * 1.2f, sy - scale * 0.4f),
            size = Size(scale * 2.4f, scale * 1.1f)
        )

        // Draw Plot Base Bed
        when (plot.plotType) {
            PlotType.BIO_DOME -> {
                // Glass Bio Dome Base
                drawScope.drawCircle(
                    color = Color(0xFF455A64),
                    radius = scale * 1.1f,
                    center = Offset(sx, sy)
                )
                // Translucent Geodesic Glass Dome
                drawScope.drawCircle(
                    color = Color(0x6680D8FF),
                    radius = scale * 1.0f,
                    center = Offset(sx, sy - scale * 0.6f)
                )
                drawScope.drawCircle(
                    color = Color(0xAA00E5FF),
                    radius = scale * 1.0f,
                    center = Offset(sx, sy - scale * 0.6f),
                    style = Stroke(width = 2.0f)
                )
            }
            PlotType.HYDROPONIC_TOWER -> {
                // Hydroponic Tower Column
                drawScope.drawRect(
                    color = Color(0xFFE0E0E0),
                    topLeft = Offset(sx - scale * 0.5f, sy - scale * 1.6f),
                    size = Size(scale * 1.0f, scale * 1.7f)
                )
                drawScope.drawRect(
                    color = Color(0xFF00E5FF),
                    topLeft = Offset(sx - scale * 0.4f, sy - scale * 1.5f),
                    size = Size(scale * 0.8f, scale * 0.2f)
                )
            }
            else -> {
                // Raised Permaculture Bed with Moisture Soil Tint
                val soilBase = if (plot.moisture > 0.4f) Color(0xFF3E2723) else Color(0xFF6D4C41)
                drawScope.drawRoundRect(
                    color = Color(0xFF8D6E63),
                    topLeft = Offset(sx - scale * 1.1f, sy - scale * 0.6f),
                    size = Size(scale * 2.2f, scale * 1.3f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
                )
                drawScope.drawRoundRect(
                    color = soilBase,
                    topLeft = Offset(sx - scale * 0.95f, sy - scale * 0.5f),
                    size = Size(scale * 1.9f, scale * 1.1f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
                )
            }
        }

        // Draw Growing Crop Visual
        if (plot.cropType != null && plot.stage != CropStage.EMPTY) {
            val crop = plot.cropType
            val primary = Color(crop.primaryColor)
            val secondary = Color(crop.secondaryColor)

            val plantHeight = when (plot.stage) {
                CropStage.SEEDLING -> scale * 0.4f
                CropStage.SPROUT -> scale * 0.7f
                CropStage.VEGETATIVE -> scale * 1.1f
                CropStage.FLOWERING -> scale * 1.5f
                CropStage.HARVEST_READY -> scale * 1.8f
                CropStage.WITHERED -> scale * 0.5f
                else -> 0f
            }

            // Plant Stem
            drawScope.drawLine(
                color = if (plot.stage == CropStage.WITHERED) Color(0xFF795548) else Color(0xFF00E676),
                start = Offset(sx, sy),
                end = Offset(sx, sy - plantHeight),
                strokeWidth = (scale * 0.18f).coerceAtLeast(2f)
            )

            // Crop Bloom / Head
            if (plot.stage >= CropStage.SPROUT) {
                val headRadius = (plantHeight * 0.35f).coerceAtLeast(4f)
                drawScope.drawCircle(
                    color = primary,
                    radius = headRadius,
                    center = Offset(sx, sy - plantHeight)
                )
                drawScope.drawCircle(
                    color = secondary,
                    radius = headRadius * 0.55f,
                    center = Offset(sx, sy - plantHeight)
                )
            }

            // Harvest Ready Radiant Glow
            if (plot.stage == CropStage.HARVEST_READY) {
                val pulse = (sin((animTime * 6.0f).toDouble()) * 0.25 + 0.75).toFloat()
                drawScope.drawCircle(
                    color = Color(0xFFFFD54F).copy(alpha = 0.5f * pulse),
                    radius = scale * 1.1f * pulse,
                    center = Offset(sx, sy - plantHeight),
                    style = Stroke(width = 3.0f)
                )
            }
        }
    }

    private fun drawEnergyNode(
        drawScope: DrawScope,
        obj: RenderableObject,
        node: EnergyNodeEntity,
        lighting: LightingState,
        animTime: Float
    ) {
        val scale = (150.0f / obj.depth).coerceIn(8.0f, 55.0f)
        val sx = obj.screenX
        val sy = obj.screenY

        when (node.nodeType) {
            EnergyNodeType.PHOTOVOLTAIC_ARRAY -> {
                // Sleek Solar Panel Stand
                drawScope.drawLine(
                    color = Color(0xFF78909C),
                    start = Offset(sx, sy),
                    end = Offset(sx, sy - scale * 1.2f),
                    strokeWidth = scale * 0.2f
                )
                // Angled Blue Glass Panel
                drawScope.drawRect(
                    color = Color(0xFF0288D1),
                    topLeft = Offset(sx - scale * 1.1f, sy - scale * 1.8f),
                    size = Size(scale * 2.2f, scale * 0.8f)
                )
                drawScope.drawRect(
                    color = Color(0xFF80D8FF).copy(alpha = 0.6f),
                    topLeft = Offset(sx - scale * 1.0f, sy - scale * 1.7f),
                    size = Size(scale * 2.0f, scale * 0.6f),
                    style = Stroke(width = 1.5f)
                )
            }
            EnergyNodeType.VERTICAL_WIND_TURBINE -> {
                // Spire Pole
                drawScope.drawLine(
                    color = Color(0xFFCFD8DC),
                    start = Offset(sx, sy),
                    end = Offset(sx, sy - scale * 2.4f),
                    strokeWidth = scale * 0.2f
                )
                // Rotating Helical Blades
                val rotAngle = (animTime * 8.0f * node.efficiency)
                val bladeWidth = cos(rotAngle.toDouble()).toFloat() * scale * 0.9f
                drawScope.drawLine(
                    color = Color(0xFF00E5FF),
                    start = Offset(sx - bladeWidth, sy - scale * 2.2f),
                    end = Offset(sx + bladeWidth, sy - scale * 1.0f),
                    strokeWidth = scale * 0.35f
                )
                drawScope.drawCircle(
                    color = Color(0xFF00B0FF),
                    radius = scale * 0.3f,
                    center = Offset(sx, sy - scale * 2.4f)
                )
            }
            EnergyNodeType.BATTERY_STORAGE_BANK -> {
                // Solid State Power Cell Vault
                drawScope.drawRoundRect(
                    color = Color(0xFF37474F),
                    topLeft = Offset(sx - scale * 0.8f, sy - scale * 1.4f),
                    size = Size(scale * 1.6f, scale * 1.4f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
                )
                // Glowing Neon Level Strip
                drawScope.drawRect(
                    color = Color(0xFF00E676),
                    topLeft = Offset(sx - scale * 0.5f, sy - scale * 1.1f),
                    size = Size(scale * 1.0f, scale * 0.25f)
                )
            }
            EnergyNodeType.BIOGAS_DIGESTER -> {
                // Biomass Vat
                drawScope.drawOval(
                    color = Color(0xFF455A64),
                    topLeft = Offset(sx - scale * 1.0f, sy - scale * 1.5f),
                    size = Size(scale * 2.0f, scale * 1.5f)
                )
                drawScope.drawCircle(
                    color = Color(0xFF81C784),
                    radius = scale * 0.35f,
                    center = Offset(sx, sy - scale * 1.5f)
                )
            }
        }
    }

    private fun drawLivestock(
        drawScope: DrawScope,
        obj: RenderableObject,
        animal: LivestockEntity,
        lighting: LightingState,
        animTime: Float
    ) {
        val scale = (130.0f / obj.depth).coerceIn(6.0f, 45.0f)
        val sx = obj.screenX
        val sy = obj.screenY

        // Breathing/walking bob
        val bob = (sin((animTime * 4.0f + animal.id).toDouble()) * scale * 0.08f).toFloat()

        // Shadow
        drawScope.drawOval(
            color = Color.Black.copy(alpha = lighting.shadowAlpha * 0.6f),
            topLeft = Offset(sx - scale * 0.7f, sy - scale * 0.3f),
            size = Size(scale * 1.4f, scale * 0.6f)
        )

        val tint = Color(animal.type.tintColor)
        when (animal.type) {
            LivestockType.SOLAR_SHEEP -> {
                // Fluffy Cloud Body
                drawScope.drawCircle(
                    color = tint,
                    radius = scale * 0.7f,
                    center = Offset(sx, sy - scale * 0.6f + bob)
                )
                // Head
                drawScope.drawCircle(
                    color = Color(0xFF5D4037),
                    radius = scale * 0.32f,
                    center = Offset(sx + scale * 0.55f, sy - scale * 0.8f + bob)
                )
                // Solar Horns
                drawScope.drawCircle(
                    color = Color(0xFFFFD54F),
                    radius = scale * 0.16f,
                    center = Offset(sx + scale * 0.6f, sy - scale * 1.1f + bob)
                )
            }
            LivestockType.CYBER_BOVINE -> {
                // Sturdy Cow Body
                drawScope.drawRoundRect(
                    color = tint,
                    topLeft = Offset(sx - scale * 0.9f, sy - scale * 1.0f + bob),
                    size = Size(scale * 1.8f, scale * 1.0f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
                )
                // Head
                drawScope.drawRect(
                    color = Color(0xFF424242),
                    topLeft = Offset(sx + scale * 0.7f, sy - scale * 1.2f + bob),
                    size = Size(scale * 0.6f, scale * 0.6f)
                )
            }
            LivestockType.ROBO_BEE_POLLINATOR -> {
                // Hovering Bee Pod
                val hoverY = sy - scale * 1.5f + (sin((animTime * 8.0f).toDouble()) * scale * 0.2f).toFloat()
                drawScope.drawOval(
                    color = Color(0xFFFFD54F),
                    topLeft = Offset(sx - scale * 0.4f, hoverY - scale * 0.3f),
                    size = Size(scale * 0.8f, scale * 0.6f)
                )
                // Translucent Wings
                drawScope.drawCircle(
                    color = Color(0x99E0F7FA),
                    radius = scale * 0.35f,
                    center = Offset(sx, hoverY - scale * 0.4f)
                )
            }
            LivestockType.MEADOW_ALPACA -> {
                // Slender Alpaca Body & Tall Neck
                drawScope.drawOval(
                    color = tint,
                    topLeft = Offset(sx - scale * 0.6f, sy - scale * 0.8f + bob),
                    size = Size(scale * 1.2f, scale * 0.8f)
                )
                drawScope.drawLine(
                    color = tint,
                    start = Offset(sx + scale * 0.4f, sy - scale * 0.6f + bob),
                    end = Offset(sx + scale * 0.4f, sy - scale * 1.6f + bob),
                    strokeWidth = scale * 0.35f
                )
            }
        }

        // Ready to Harvest Indicator
        if (animal.readyToHarvest) {
            drawScope.drawCircle(
                color = Color(0xFF00E5FF),
                radius = scale * 0.35f,
                center = Offset(sx, sy - scale * 1.6f)
            )
            drawScope.drawCircle(
                color = Color.White,
                radius = scale * 0.18f,
                center = Offset(sx, sy - scale * 1.6f)
            )
        }
    }

    private fun drawPlayer(
        drawScope: DrawScope,
        obj: RenderableObject,
        player: ThirdPersonPlayer,
        lighting: LightingState,
        animTime: Float
    ) {
        val scale = (150.0f / obj.depth).coerceIn(8.0f, 60.0f)
        val sx = obj.screenX
        val sy = obj.screenY

        // Shadow
        drawScope.drawOval(
            color = Color.Black.copy(alpha = lighting.shadowAlpha * 0.85f),
            topLeft = Offset(sx - scale * 0.6f, sy - scale * 0.25f),
            size = Size(scale * 1.2f, scale * 0.5f)
        )

        // Walk swing calculations
        val legSwing = if (player.isMoving) sin(player.walkAnimPhase.toDouble()).toFloat() * scale * 0.35f else 0.0f

        // Legs
        drawScope.drawLine(
            color = Color(0xFF263238),
            start = Offset(sx - scale * 0.2f, sy - scale * 0.6f),
            end = Offset(sx - scale * 0.2f + legSwing, sy),
            strokeWidth = scale * 0.2f
        )
        drawScope.drawLine(
            color = Color(0xFF263238),
            start = Offset(sx + scale * 0.2f, sy - scale * 0.6f),
            end = Offset(sx + scale * 0.2f - legSwing, sy),
            strokeWidth = scale * 0.2f
        )

        // Solarpunk Eco Tunic / Torso
        drawScope.drawRoundRect(
            color = Color(0xFF00C853),
            topLeft = Offset(sx - scale * 0.45f, sy - scale * 1.4f),
            size = Size(scale * 0.9f, scale * 0.85f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
        )

        // Brass Solar Trim
        drawScope.drawRect(
            color = Color(0xFFFFD54F),
            topLeft = Offset(sx - scale * 0.35f, sy - scale * 1.3f),
            size = Size(scale * 0.7f, scale * 0.15f)
        )

        // Head
        drawScope.drawCircle(
            color = Color(0xFFFFCC80),
            radius = scale * 0.32f,
            center = Offset(sx, sy - scale * 1.7f)
        )

        // Solarpunk Sun-Hat / Visor
        drawScope.drawOval(
            color = Color(0xFF8D6E63),
            topLeft = Offset(sx - scale * 0.6f, sy - scale * 2.1f),
            size = Size(scale * 1.2f, scale * 0.35f)
        )
        drawScope.drawCircle(
            color = Color(0xFF6D4C41),
            radius = scale * 0.28f,
            center = Offset(sx, sy - scale * 2.05f)
        )
    }

    private fun drawWorkshopBuilding(drawScope: DrawScope, obj: RenderableObject, lighting: LightingState, animTime: Float) {
        val scale = (170.0f / obj.depth).coerceIn(12.0f, 75.0f)
        val sx = obj.screenX
        val sy = obj.screenY

        // Workshop Base
        drawScope.drawRoundRect(
            color = Color(0xFF37474F),
            topLeft = Offset(sx - scale * 1.8f, sy - scale * 2.2f),
            size = Size(scale * 3.6f, scale * 2.2f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
        )
        // Solarpunk Glass Roof
        drawScope.drawRect(
            color = Color(0xFF00E5FF).copy(alpha = 0.7f),
            topLeft = Offset(sx - scale * 1.6f, sy - scale * 2.0f),
            size = Size(scale * 3.2f, scale * 0.6f)
        )
        // Workshop Door / Neon Sign
        drawScope.drawRect(
            color = Color(0xFFFFD54F),
            topLeft = Offset(sx - scale * 0.4f, sy - scale * 1.2f),
            size = Size(scale * 0.8f, scale * 1.2f)
        )
    }

    private fun drawMarketBuilding(drawScope: DrawScope, obj: RenderableObject, lighting: LightingState, animTime: Float) {
        val scale = (170.0f / obj.depth).coerceIn(12.0f, 75.0f)
        val sx = obj.screenX
        val sy = obj.screenY

        // Market Awning
        drawScope.drawRoundRect(
            color = Color(0xFF5D4037),
            topLeft = Offset(sx - scale * 1.5f, sy - scale * 1.6f),
            size = Size(scale * 3.0f, scale * 1.6f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
        )
        // Green & Amber Striped Canopy
        drawScope.drawRect(
            color = Color(0xFF00E676),
            topLeft = Offset(sx - scale * 1.7f, sy - scale * 2.1f),
            size = Size(scale * 3.4f, scale * 0.6f)
        )
        drawScope.drawRect(
            color = Color(0xFFFFD54F),
            topLeft = Offset(sx - scale * 0.7f, sy - scale * 2.1f),
            size = Size(scale * 1.4f, scale * 0.6f)
        )
    }

    private fun drawWeatherParticles(
        drawScope: DrawScope,
        w: Float,
        h: Float,
        weather: WeatherType,
        animTime: Float,
        lighting: LightingState
    ) {
        when (weather) {
            WeatherType.RAINY_STORM -> {
                // Slanted Eco-Rain Droplets
                for (i in 0 until 50) {
                    val rx = ((i * 47.3f + animTime * 350.0f) % w)
                    val ry = ((i * 83.1f + animTime * 650.0f) % h)
                    drawScope.drawLine(
                        color = Color(0x8880D8FF),
                        start = Offset(rx, ry),
                        end = Offset(rx - 8f, ry + 22f),
                        strokeWidth = 2.0f
                    )
                }
            }
            WeatherType.HEATWAVE -> {
                // Golden Solar Shimmer Waves
                for (i in 0 until 20) {
                    val hx = (sin(i.toDouble() * 15.0 + animTime) * 0.5 + 0.5).toFloat() * w
                    val hy = (cos(i.toDouble() * 9.0) * 0.5 + 0.5).toFloat() * h
                    drawScope.drawCircle(
                        color = Color(0x22FFD54F),
                        radius = (sin((animTime * 4.0f + i).toDouble()) * 15.0 + 30.0).toFloat(),
                        center = Offset(hx, hy)
                    )
                }
            }
            WeatherType.WIND_GALE -> {
                // Streamline Wind Gusts
                for (i in 0 until 15) {
                    val gx = ((i * 120.0f + animTime * 400.0f) % (w + 100.0f)) - 50.0f
                    val gy = (h * 0.2f + (i * 35.0f) % (h * 0.6f))
                    drawScope.drawLine(
                        color = Color(0x44E0F7FA),
                        start = Offset(gx, gy),
                        end = Offset(gx + 60.0f, gy + sin(gx * 0.05).toFloat() * 10.0f),
                        strokeWidth = 2.5f
                    )
                }
            }
            WeatherType.MISTY_NEBULA -> {
                // Foggy Mist Wash
                drawScope.drawRect(
                    color = Color(0x26B2EBF2),
                    size = Size(w, h)
                )
            }
            WeatherType.SUNNY_CLEAR -> {
                // Subtle floating pollen sparkles
                for (i in 0 until 12) {
                    val px = ((i * 91.0f + sin(animTime + i) * 30.0f) % w)
                    val py = ((i * 73.0f + cos(animTime + i) * 20.0f) % (h * 0.7f)) + h * 0.2f
                    drawScope.drawCircle(
                        color = Color(0x88FFE082),
                        radius = 2.0f,
                        center = Offset(px.toFloat(), py.toFloat())
                    )
                }
            }
        }
    }

    private enum class RenderType {
        TERRAIN_TILE,
        PATHWAYS,
        FARM_PLOT,
        ENERGY_NODE,
        LIVESTOCK,
        PLAYER_AVATAR,
        WORKSHOP_BUILDING,
        MARKET_BUILDING
    }

    private data class RenderableObject(
        val worldX: Float,
        val worldY: Float,
        val worldZ: Float,
        val type: RenderType,
        val data: Any? = null,
        var screenX: Float = 0f,
        var screenY: Float = 0f,
        var depth: Float = 0f
    )
}
