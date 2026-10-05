package com.example.game3d.renderer

import android.opengl.GLES20
import android.opengl.Matrix
import com.example.data.local.EnergyNodeEntity
import com.example.data.local.LivestockEntity
import com.example.data.local.PlacedBuildingEntity
import com.example.data.local.PlotEntity
import com.example.data.model.BuildableType
import com.example.data.model.WeatherType
import com.example.game3d.opengl.GLMesh
import com.example.game3d.opengl.GLModelBuilder
import com.example.game3d.opengl.GLShader
import com.example.game3d.opengl.models.Animal3DModels
import com.example.game3d.opengl.models.Building3DModels
import com.example.game3d.opengl.models.Crop3DModels
import com.example.game3d.opengl.models.Environment3DModels
import com.example.game3d.opengl.models.Player3DModel
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
    val position = Vector3(0f, 6f, 10f)
    val target = Vector3(0f, 1.25f, 0f)
    val up = Vector3(0f, 1f, 0f)

    var near: Float = 0.5f
    var far: Float = 150.0f

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
    val canAfford: Boolean = true
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

    private var isInitialized: Boolean = false

    // Obstacle coordinates for camera occlusion prevention
    private val obstacles = listOf(
        Pair(6.0f, 0.0f),    // Farmhouse
        Pair(-12.0f, 10.0f), // Barn
        Pair(0.0f, 14.0f),   // Workshop
        Pair(-14.0f, -14.0f) // Market
    )

    fun create() {
        if (isInitialized) return

        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glCullFace(GLES20.GL_BACK)

        val sh = GLShader()
        sh.init()
        shader = sh
        modelBatch.init(sh)

        playerModel = Player3DModel()
        animalModels = Animal3DModels()
        buildingModels = Building3DModels()
        cropModels = Crop3DModels()
        environmentModels = Environment3DModels()

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
        animTimeSec: Float,
        hour: Float = 12.0f,
        weather: WeatherType = WeatherType.SUNNY_CLEAR
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
        val targetX = player?.posX ?: 0.0f
        val targetY = (player?.posY ?: 0.0f) + 1.25f
        val targetZ = player?.posZ ?: 0.0f

        val yawDeg = thirdPersonCamera?.yawDeg ?: 45.0f
        val pitchDeg = (thirdPersonCamera?.pitchDeg ?: 24.0f).coerceIn(12.0f, 65.0f)
        var desiredDist = (thirdPersonCamera?.distance ?: 7.5f).coerceIn(3.0f, 12.0f)

        val yawRad = Math.toRadians(yawDeg.toDouble())
        val pitchRad = Math.toRadians(pitchDeg.toDouble())

        // Camera collision avoidance against building bounding spheres
        for (obstacle in obstacles) {
            val ox = obstacle.first
            val oz = obstacle.second
            val toOx = ox - targetX
            val toOz = oz - targetZ
            val obstacleDist = kotlin.math.sqrt(toOx * toOx + toOz * toOz)
            if (obstacleDist < desiredDist + 3.0f) {
                desiredDist = min(desiredDist, max(3.0f, obstacleDist - 1.5f))
            }
        }

        val camX = targetX + (desiredDist * cos(pitchRad) * sin(yawRad)).toFloat()
        val camY = targetY + (desiredDist * sin(pitchRad)).toFloat()
        val camZ = targetZ + (desiredDist * cos(pitchRad) * cos(yawRad)).toFloat()

        camera.setPosition(camX, camY, camZ)
        camera.lookAt(targetX, targetY, targetZ)
        camera.update()

        // 4. Begin ModelBatch Pipeline
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
                canAfford = ghostBuilding.canAfford
            )
        }

        // C. Draw Plots & 3D Crops across Growth Stages
        cropModels?.drawPlots(sh, camera.viewMatrix, camera.projectionMatrix, plots, animTimeSec)

        // D. Draw 3D Livestock (Cows, Sheep, Chickens, Pigs, Goats, Beehive)
        animalModels?.drawLivestock(sh, camera.viewMatrix, camera.projectionMatrix, livestock, animTimeSec)

        // E. Draw 3D Player Humanoid Character
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
                isMoving = player.isMoving
            )
        }

        modelBatch.end()
    }

    fun dispose() {
        isInitialized = false
    }
}
