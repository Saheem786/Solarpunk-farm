package com.example.game3d.renderer

import android.opengl.GLES20
import android.opengl.Matrix
import com.example.data.local.EnergyNodeEntity
import com.example.data.local.LivestockEntity
import com.example.data.local.NpcEntity
import com.example.data.local.PlacedBuildingEntity
import com.example.data.local.PlotEntity
import com.example.data.model.BuildableType
import com.example.data.model.EnergyNodeType
import com.example.data.model.WeatherType
import com.example.game3d.opengl.GLMesh
import com.example.game3d.opengl.GLModelBuilder
import com.example.game3d.opengl.GLShader
import com.example.game3d.opengl.models.Animal3DModels
import com.example.game3d.opengl.models.Building3DModels
import com.example.game3d.opengl.models.Crop3DModels
import com.example.game3d.opengl.models.Environment3DModels
import com.example.game3d.opengl.models.Npc3DModels
import com.example.game3d.opengl.models.Player3DModel
import com.example.game3d.opengl.models.FarmWorldLayout
import com.example.game3d.player.ThirdPersonCamera
import com.example.game3d.player.ThirdPersonPlayer
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * 3D Vector utility representation.
 */
data class Vector3(var x: Float = 0f, var y: Float = 0f, var z: Float = 0f) {
    fun set(nx: Float, ny: Float, nz: Float): Vector3 {
        x = nx; y = ny; z = nz
        return this
    }
}

/**
 * 3D Color representation for environment and lights.
 */
data class ColorRGB(val r: Float, val g: Float, val b: Float, val a: Float = 1.0f)

/**
 * Directional Light for 3D environment lighting.
 */
data class DirectionalLight(
    var color: ColorRGB = ColorRGB(1f, 0.95f, 0.85f),
    var direction: Vector3 = Vector3(0.6f, 0.8f, 0.4f)
)

/**
 * Ambient Light for 3D environment lighting.
 */
data class AmbientLight(
    var color: ColorRGB = ColorRGB(0.35f, 0.42f, 0.50f),
    var intensity: Float = 0.45f
)

/**
 * 3D Environment managing lighting conditions, celestial states, and shadow parameters.
 */
class Environment {
    var directionalLight: DirectionalLight = DirectionalLight()
    var ambientLight: AmbientLight = AmbientLight()
    var skyHorizonColor: ColorRGB = ColorRGB(0.45f, 0.72f, 0.92f)
    var skyTopColor: ColorRGB = ColorRGB(0.18f, 0.45f, 0.85f)
    var isNight: Boolean = false

    fun updateFromLightingState(state: LightingState?) {
        if (state == null) return
        val lDirX = state.lightDirX
        val lDirY = max(0.2f, state.lightDirY)
        val lDirZ = state.lightDirZ
        directionalLight.direction.set(lDirX, lDirY, lDirZ)

        val dirInt = state.directLightIntensity
        directionalLight.color = ColorRGB(
            state.directionalLightColor.red * dirInt,
            state.directionalLightColor.green * dirInt,
            state.directionalLightColor.blue * dirInt
        )

        ambientLight.intensity = state.ambientIntensity
        ambientLight.color = ColorRGB(
            state.ambientColor.red,
            state.ambientColor.green,
            state.ambientColor.blue
        )

        skyHorizonColor = ColorRGB(
            state.skyHorizonColor.red,
            state.skyHorizonColor.green,
            state.skyHorizonColor.blue
        )
        skyTopColor = ColorRGB(
            state.skyTopColor.red,
            state.skyTopColor.green,
            state.skyTopColor.blue
        )
        isNight = state.isNight
    }
}

/**
 * PerspectiveCamera computing view and projection matrices for 3D rendering.
 */
class PerspectiveCamera(
    var fieldOfView: Float = 60.0f,
    var viewportWidth: Float = 1080.0f,
    var viewportHeight: Float = 1920.0f
) {
    val position = Vector3(0f, 2f, -5.5f)
    val target = Vector3(0f, 1.1f, 0f)
    val up = Vector3(0f, 1f, 0f)

    var near: Float = 0.2f
    var far: Float = 250.0f

    val projectionMatrix = FloatArray(16)
    val viewMatrix = FloatArray(16)
    val combinedMatrix = FloatArray(16)

    fun update() {
        val aspect = viewportWidth / max(1.0f, viewportHeight)
        Matrix.perspectiveM(projectionMatrix, 0, fieldOfView, aspect, near, far)
        Matrix.setLookAtM(
            viewMatrix, 0,
            position.x, position.y, position.z,
            target.x, target.y, target.z,
            up.x, up.y, up.z
        )
        Matrix.multiplyMM(combinedMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    fun lookAt(tx: Float, ty: Float, tz: Float) {
        target.set(tx, ty, tz)
    }

    fun setPosition(px: Float, py: Float, pz: Float) {
        position.set(px, py, pz)
    }
}

/**
 * 3D Model Instance holding a mesh reference, transform matrix, and material properties.
 */
class ModelInstance(val mesh: GLMesh, val transform: FloatArray = FloatArray(16)) {
    init {
        Matrix.setIdentityM(transform, 0)
    }
}

/**
 * ModelBatch coordinating GPU shader passes, uniform uploads, and mesh drawing.
 */
class ModelBatch {
    private var shader: GLShader? = null
    private val mvMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val normalMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)

    fun init(glShader: GLShader) {
        this.shader = glShader
    }

    fun begin(camera: PerspectiveCamera, environment: Environment) {
        val sh = shader ?: return
        sh.use()

        // Upload Directional Light Uniforms
        val lightDir = environment.directionalLight.direction
        GLES20.glUniform3f(sh.uLightDirLoc, lightDir.x, lightDir.y, lightDir.z)

        val lCol = environment.directionalLight.color
        GLES20.glUniform3f(sh.uLightColorLoc, lCol.r, lCol.g, lCol.b)

        // Upload Ambient Light Uniforms
        val ambCol = environment.ambientLight.color
        val ambInt = environment.ambientLight.intensity
        GLES20.glUniform3f(sh.uAmbientColorLoc, ambCol.r * ambInt, ambCol.g * ambInt, ambCol.b * ambInt)
    }

    fun render(instance: ModelInstance, camera: PerspectiveCamera, emission: Vector3? = null, shininess: Float = 8.0f) {
        val sh = shader ?: return

        Matrix.multiplyMM(mvMatrix, 0, camera.viewMatrix, 0, instance.transform, 0)
        Matrix.multiplyMM(mvpMatrix, 0, camera.projectionMatrix, 0, mvMatrix, 0)

        Matrix.invertM(tempMatrix, 0, mvMatrix, 0)
        Matrix.transposeM(normalMatrix, 0, tempMatrix, 0)

        GLES20.glUniformMatrix4fv(sh.uMVPMatrixLoc, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(sh.uMVMatrixLoc, 1, false, mvMatrix, 0)
        GLES20.glUniformMatrix4fv(sh.uNormalMatrixLoc, 1, false, normalMatrix, 0)

        if (emission != null) {
            GLES20.glUniform3f(sh.uEmissionColorLoc, emission.x, emission.y, emission.z)
        } else {
            GLES20.glUniform3f(sh.uEmissionColorLoc, 0f, 0f, 0f)
        }
        GLES20.glUniform1f(sh.uShininessLoc, shininess)

        instance.mesh.draw(sh)
    }

    fun end() {
        // Finalize batch
    }
}

data class GhostBuildingState(
    val type: BuildableType,
    val posX: Float,
    val posY: Float = 0.0f,
    val posZ: Float,
    val rotationDeg: Float = 0.0f,
    val canAfford: Boolean = true,
    val isValid: Boolean = true,
    val reasonMessage: String = "Ready to place"
)

/**
 * GameRenderer: Complete 3D Environment Renderer for the Solarpunk Farm World.
 * Implements PerspectiveCamera, ModelBatch, Environment, and procedural 3D model structures.
 */
class GameRenderer {

    val camera: PerspectiveCamera = PerspectiveCamera(fieldOfView = 60.0f)
    val environment: Environment = Environment()
    val modelBatch: ModelBatch = ModelBatch()

    private var shader: GLShader? = null
    var playerModel: Player3DModel? = null
        private set
    var animalModels: Animal3DModels? = null
        private set
    var buildingModels: Building3DModels? = null
        private set
    var cropModels: Crop3DModels? = null
        private set
    var environmentModels: Environment3DModels? = null
        private set
    var npcModels: Npc3DModels? = null
        private set
    private var skybox: Skybox3D? = null
    private var lensFlare: GLLensFlareRenderer? = null

    companion object {
        const val DEBUG_3D = false
        const val DEBUG_3D_SCENE = false
    }

    private var isInitialized: Boolean = false
    private var lastAnimTimeSec: Float = 0.0f

    fun create() {
        if (isInitialized) return

        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glCullFace(GLES20.GL_BACK)

        val sh = GLShader()
        sh.init()
        shader = sh
        if (sh.programId == 0) {
            android.util.Log.e("GameRenderer", "FATAL: GLShader initialization failed. programId == 0")
            isInitialized = false
            return
        }
        modelBatch.init(sh)

        playerModel = Player3DModel()
        animalModels = Animal3DModels()
        buildingModels = Building3DModels()
        cropModels = Crop3DModels()
        environmentModels = Environment3DModels()
        npcModels = Npc3DModels()
        skybox = Skybox3D()
        lensFlare = GLLensFlareRenderer()
        lensFlare?.init()

        isInitialized = true
    }

    fun resize(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        camera.viewportWidth = width.toFloat()
        camera.viewportHeight = height.toFloat()
        camera.update()
    }

    fun render(
        player: ThirdPersonPlayer?,
        thirdPersonCamera: ThirdPersonCamera?,
        lightingState: LightingState?,
        plots: List<PlotEntity>,
        energyNodes: List<EnergyNodeEntity>,
        placedBuildings: List<PlacedBuildingEntity> = emptyList(),
        ghostBuilding: GhostBuildingState? = null,
        livestock: List<LivestockEntity>,
        npcs: List<NpcEntity> = emptyList(),
        animTimeSec: Float,
        hour: Float = 12.0f,
        weather: WeatherType = WeatherType.SUNNY_CLEAR,
        particleSystem: com.example.game3d.particles.ParticleSystem3D? = null,
        actionAnim: com.example.game3d.opengl.models.PlayerActionAnim = com.example.game3d.opengl.models.PlayerActionAnim.NONE,
        actionProgress: Float = 0.0f,
        screenShake: Float = 0.0f
    ) {
        if (!isInitialized) {
            create()
        }
        val sh = shader ?: return

        // 1. Update Environment Lighting
        environment.updateFromLightingState(lightingState)

        // 2. Clear Frame with Dynamic Celestial Sky Color
        val sky = environment.skyHorizonColor
        GLES20.glClearColor(sky.r, sky.g, sky.b, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // 3. Compute Third-Person Camera Targeting & Collision Pullback
        val shakeX = if (screenShake > 0f) sin(animTimeSec * 45.0f) * screenShake * 0.25f else 0f
        val shakeY = if (screenShake > 0f) cos(animTimeSec * 35.0f) * screenShake * 0.25f else 0f

        val targetX = (player?.posX ?: 0.0f) + shakeX
        val targetY = ((player?.posY ?: 0.0f) + 1.1f) + shakeY
        val targetZ = (player?.posZ ?: 0.0f)

        val yawDeg = thirdPersonCamera?.yawDeg ?: 180.0f
        val pitchDeg = (thirdPersonCamera?.pitchDeg ?: 22.0f).coerceIn(12.0f, 65.0f)

        val yawRad = Math.toRadians(yawDeg.toDouble())
        val pitchRad = Math.toRadians(pitchDeg.toDouble())

        // 1. Calculate deltaSec based on animTimeSec
        val deltaSec = if (lastAnimTimeSec == 0.0f) 0.016f else (animTimeSec - lastAnimTimeSec).coerceIn(0.001f, 0.1f)
        lastAnimTimeSec = animTimeSec

        // 2. Camera direction unit vector
        val dirX = (cos(pitchRad) * sin(yawRad)).toFloat()
        val dirY = (sin(pitchRad)).toFloat()
        val dirZ = (cos(pitchRad) * cos(yawRad)).toFloat()

        // 3. Desired distance chosen by player (clamped between 3.5 and 8.0 units, default is 5.5)
        val desiredDist = (thirdPersonCamera?.desiredDistance ?: 5.5f).coerceIn(3.5f, 8.0f)

        // 4. Perform raycast sampling to find maximum allowed distance without clipping any solid structure
        var maxAllowedDist = desiredDist
        val step = 0.40f
        var d = 0.6f // Start 0.6m away from player to avoid self-clipping
        while (d <= desiredDist) {
            val sx = targetX + d * dirX
            val sy = targetY + d * dirY
            val sz = targetZ + d * dirZ

            if (checkCameraCollision(sx, sy, sz, placedBuildings, energyNodes)) {
                maxAllowedDist = max(3.5f, d - 0.4f)
                break
            }
            d += step
        }

        // 5. Interpolate actual camera distance towards the allowed collision distance smoothly over 0.2s
        if (thirdPersonCamera != null) {
            val lerpFactor = (deltaSec / 0.2f).coerceIn(0.0f, 1.0f)
            thirdPersonCamera.distance += (maxAllowedDist - thirdPersonCamera.distance) * lerpFactor
        }

        val currentDist = thirdPersonCamera?.distance ?: maxAllowedDist
        val camX = targetX + (currentDist * dirX)
        val camY = targetY + (currentDist * dirY)
        val camZ = targetZ + (currentDist * dirZ)

        camera.setPosition(camX, camY, camZ)
        camera.lookAt(targetX, targetY, targetZ)
        camera.update()

        // 4. Render Dynamic 24-Hour Skybox
        skybox?.render(
            viewMatrix = camera.viewMatrix,
            projectionMatrix = camera.projectionMatrix,
            lightingState = lightingState,
            hour = hour,
            animTimeSec = animTimeSec
        )

        // 5. Begin ModelBatch Pipeline
        modelBatch.begin(camera, environment)

        // A. Draw Environment (Terrain, Roads, River, Bridge, Pond, Trees, Grass, Wildlife)
        environmentModels?.drawEnvironment(sh, camera.viewMatrix, camera.projectionMatrix, animTimeSec, hour, weather)

        // B. Draw Buildings (Farmhouse, Barn, Workshop, Market, Solar Arrays, Wind Turbine, Placed Buildings, Fences)
        buildingModels?.drawBuildings(sh, camera.viewMatrix, camera.projectionMatrix, energyNodes, placedBuildings, animTimeSec)

        // B2. Draw Ghost Holographic Preview if in Build Mode
        if (ghostBuilding != null) {
            buildingModels?.drawGhostPreview(
                shader = sh,
                viewMatrix = camera.viewMatrix,
                projMatrix = camera.projectionMatrix,
                buildingType = ghostBuilding.type,
                posX = ghostBuilding.posX,
                posY = ghostBuilding.posY,
                posZ = ghostBuilding.posZ,
                rotationDeg = ghostBuilding.rotationDeg,
                animTime = animTimeSec,
                isValid = ghostBuilding.isValid
            )
        }

        // C. Draw Plots & 3D Crops across Growth Stages
        cropModels?.drawPlots(sh, camera.viewMatrix, camera.projectionMatrix, plots, animTimeSec)

        // D. Draw 3D Livestock (Cows, Sheep, Chickens, Pigs, Goats, Beehive)
        animalModels?.drawLivestock(sh, camera.viewMatrix, camera.projectionMatrix, livestock, animTimeSec)

        // D2. Draw 3D Settlement NPC Survivors
        npcModels?.drawNpcs(sh, camera.viewMatrix, camera.projectionMatrix, npcs, camX, camZ, animTimeSec)

        // E. Draw 3D Player Humanoid Character with Procedural Animation
        if (player != null) {
            playerModel?.draw(
                shader = sh,
                viewMatrix = camera.viewMatrix,
                projMatrix = camera.projectionMatrix,
                posX = player.posX,
                posY = player.posY,
                posZ = player.posZ,
                rotationDeg = player.orientationAngleDeg,
                walkPhase = player.walkAnimPhase,
                isMoving = player.isMoving,
                isRunning = player.currentSpeed > 7.0f,
                animTime = animTimeSec,
                actionAnim = actionAnim,
                actionProgress = actionProgress
            )
        }

        // F. Update & Render 3D Particle Effects (Footstep dust, water droplets, harvest leaves, coin sparkles)
        particleSystem?.update(deltaSec)
        particleSystem?.render(sh, camera.viewMatrix, camera.projectionMatrix)

        modelBatch.end()

        // 6. Render Screen-Space Optical Lens Flare
        lensFlare?.render(
            viewMatrix = camera.viewMatrix,
            projectionMatrix = camera.projectionMatrix,
            camX = camX,
            camY = camY,
            camZ = camZ,
            viewportWidth = camera.viewportWidth,
            viewportHeight = camera.viewportHeight,
            hour = hour,
            weather = weather,
            lightingState = lightingState,
            animTimeSec = animTimeSec
        )
    }

    private fun checkCameraCollision(
        x: Float,
        y: Float,
        z: Float,
        placedBuildings: List<PlacedBuildingEntity>,
        energyNodes: List<EnergyNodeEntity>
    ): Boolean {
        // 1. Terrain/Ground collision
        if (y < 0.2f) {
            return true
        }

        // 2. Authoritative Static Buildings from FarmWorldLayout
        for (b in FarmWorldLayout.staticBuildings) {
            if (b.isCircular) {
                val dx = x - b.x
                val dz = z - b.z
                if (dx * dx + dz * dz < (b.radius + 0.4f) * (b.radius + 0.4f) && y <= b.height) {
                    return true
                }
            } else {
                val halfW = (b.width / 2.0f) + 0.4f
                val halfD = (b.depth / 2.0f) + 0.4f
                if (x >= b.x - halfW && x <= b.x + halfW &&
                    z >= b.z - halfD && z <= b.z + halfD &&
                    y <= b.height) {
                    return true
                }
            }
        }

        // 3. Authoritative Trees from FarmWorldLayout
        for (tree in FarmWorldLayout.treePositions) {
            val dx = x - tree.x
            val dz = z - tree.z
            val distSq = dx * dx + dz * dz
            if (distSq > 4.00f) continue
            if (y <= 5.0f) {
                return true
            }
        }

        // 8. Placed Buildings with fast bounding box pruning
        for (building in placedBuildings) {
            val size = when (building.buildingType) {
                BuildableType.CABIN -> 4.0f
                BuildableType.GREENHOUSE -> 5.0f
                BuildableType.SOLAR_PANEL -> 2.0f
                BuildableType.WINDMILL -> 3.0f
                BuildableType.STORAGE -> 3.0f
                BuildableType.WELL -> 2.0f
                BuildableType.FENCE -> 1.0f
                BuildableType.COMPOST_BIN -> 2.0f
                BuildableType.RAIN_BARREL -> 1.5f
                BuildableType.WATER_FILTER -> 1.8f
                BuildableType.WATER_PURIFIER -> 2.2f
                BuildableType.IRRIGATION_PIPE -> 1.0f
                BuildableType.IRRIGATION_NODE -> 1.0f
                BuildableType.WATER_STORAGE_SHED -> 3.5f
                BuildableType.HYDRO_GENERATOR -> 3.2f
                BuildableType.ADVANCED_SOLAR -> 4.2f
                BuildableType.BIOGAS_GENERATOR -> 3.2f
                BuildableType.GEOTHERMAL_VENT -> 4.2f
                BuildableType.BASIC_BATTERY -> 2.2f
                BuildableType.ADVANCED_BATTERY -> 3.2f
                BuildableType.BATTERY_BANK -> 5.0f
                BuildableType.POWER_POLE -> 1.0f
                BuildableType.NPC_CABIN -> 3.2f
                BuildableType.BUNKHOUSE -> 5.2f
                BuildableType.KITCHEN -> 4.2f
                BuildableType.MEDIC_STATION -> 3.2f
                BuildableType.WORKSHOP -> 3.2f
                BuildableType.RESEARCH_LAB -> 4.2f
            }
            val halfSize = size / 2.0f
            val height = when (building.buildingType) {
                BuildableType.CABIN -> 3.5f
                BuildableType.GREENHOUSE -> 3.0f
                BuildableType.SOLAR_PANEL -> 1.5f
                BuildableType.WINDMILL -> 4.5f
                BuildableType.STORAGE -> 2.5f
                BuildableType.WELL -> 2.2f
                BuildableType.FENCE -> 1.15f
                BuildableType.COMPOST_BIN -> 1.5f
                BuildableType.RAIN_BARREL -> 1.4f
                BuildableType.WATER_FILTER -> 1.6f
                BuildableType.WATER_PURIFIER -> 2.2f
                BuildableType.IRRIGATION_PIPE -> 0.3f
                BuildableType.IRRIGATION_NODE -> 0.8f
                BuildableType.WATER_STORAGE_SHED -> 2.8f
                BuildableType.HYDRO_GENERATOR -> 2.5f
                BuildableType.ADVANCED_SOLAR -> 2.2f
                BuildableType.BIOGAS_GENERATOR -> 2.6f
                BuildableType.GEOTHERMAL_VENT -> 4.0f
                BuildableType.BASIC_BATTERY -> 2.0f
                BuildableType.ADVANCED_BATTERY -> 2.5f
                BuildableType.BATTERY_BANK -> 3.2f
                BuildableType.POWER_POLE -> 4.2f
                BuildableType.NPC_CABIN -> 3.2f
                BuildableType.BUNKHOUSE -> 4.0f
                BuildableType.KITCHEN -> 3.2f
                BuildableType.MEDIC_STATION -> 3.0f
                BuildableType.WORKSHOP -> 3.0f
                BuildableType.RESEARCH_LAB -> 3.8f
            }
            if (building.buildingType == BuildableType.FENCE || building.buildingType == BuildableType.IRRIGATION_PIPE || building.buildingType == BuildableType.POWER_POLE) {
                continue
            }
            
            // Fast bounding box check
            if (x < building.posX - halfSize || x > building.posX + halfSize ||
                z < building.posZ - halfSize || z > building.posZ + halfSize) {
                continue
            }
            
            if (y <= height) {
                return true
            }
        }

        // 9. Energy Generation Nodes with fast bounding box pruning
        for (node in energyNodes) {
            val size = when (node.nodeType) {
                EnergyNodeType.PHOTOVOLTAIC_ARRAY -> 3.0f
                EnergyNodeType.VERTICAL_WIND_TURBINE -> 2.0f
                EnergyNodeType.BATTERY_STORAGE_BANK -> 2.0f
                EnergyNodeType.BIOGAS_DIGESTER -> 3.0f
                else -> 2.5f
            }
            val halfSize = size / 2.0f
            val height = 2.5f
            
            // Fast bounding box check
            if (x < node.posX - halfSize || x > node.posX + halfSize ||
                z < node.posZ - halfSize || z > node.posZ + halfSize) {
                continue
            }
            
            if (y <= height) {
                return true
            }
        }

        return false
    }

    fun dispose() {
        lensFlare?.dispose()
        lensFlare = null
        isInitialized = false
    }
}
