package com.example.game3d.opengl

import android.opengl.GLSurfaceView
import com.example.data.local.EnergyNodeEntity
import com.example.data.local.LivestockEntity
import com.example.data.local.NpcEntity
import com.example.data.local.PlacedBuildingEntity
import com.example.data.local.PlotEntity
import com.example.data.model.BuildableType
import com.example.data.model.WeatherType
import com.example.game3d.opengl.models.PlayerActionAnim
import com.example.game3d.particles.ParticleSystem3D
import com.example.game3d.player.ThirdPersonCamera
import com.example.game3d.player.ThirdPersonPlayer
import com.example.game3d.renderer.GameRenderer
import com.example.game3d.renderer.GhostBuildingState
import com.example.game3d.renderer.LightingState
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLWorldRenderer : GLSurfaceView.Renderer {

    val gameRenderer: GameRenderer = GameRenderer()

    // Thread-safe Frame Data references
    @Volatile var playerRef: ThirdPersonPlayer? = null
    @Volatile var cameraRef: ThirdPersonCamera? = null
    @Volatile var lightingRef: LightingState? = null
    @Volatile var plotsRef: List<PlotEntity> = emptyList()
    @Volatile var energyNodesRef: List<EnergyNodeEntity> = emptyList()
    @Volatile var placedBuildingsRef: List<PlacedBuildingEntity> = emptyList()
    @Volatile var ghostBuildingRef: GhostBuildingState? = null
    @Volatile var livestockRef: List<LivestockEntity> = emptyList()
    @Volatile var npcsRef: List<NpcEntity> = emptyList()
    @Volatile var animTimeSec: Float = 0.0f
    @Volatile var weatherRef: WeatherType = WeatherType.SUNNY_CLEAR
    @Volatile var hourRef: Float = 12.0f
    @Volatile var particleSystemRef: ParticleSystem3D? = null
    @Volatile var actionAnimRef: PlayerActionAnim = PlayerActionAnim.NONE
    @Volatile var actionProgressRef: Float = 0.0f
    @Volatile var screenShakeRef: Float = 0.0f

    private var lastDiagnosticLogTime = 0L
    private var drawFrameCount = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        gameRenderer.dispose()
        gameRenderer.create()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        gameRenderer.resize(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        drawFrameCount++
        val now = System.currentTimeMillis()
        if (now - lastDiagnosticLogTime > 500L) {
            lastDiagnosticLogTime = now
            val p = playerRef
            android.util.Log.d("DIAGNOSTIC", "RENDER: onDrawFrame count=$drawFrameCount | PLAYER pos=(${p?.posX}, ${p?.posY}, ${p?.posZ}) isMoving=${p?.isMoving} speed=${p?.currentSpeed}")
        }

        gameRenderer.render(
            player = playerRef,
            thirdPersonCamera = cameraRef,
            lightingState = lightingRef,
            plots = plotsRef,
            energyNodes = energyNodesRef,
            placedBuildings = placedBuildingsRef,
            ghostBuilding = ghostBuildingRef,
            livestock = livestockRef,
            npcs = npcsRef,
            animTimeSec = animTimeSec,
            hour = hourRef,
            weather = weatherRef,
            particleSystem = particleSystemRef,
            actionAnim = actionAnimRef,
            actionProgress = actionProgressRef,
            screenShake = screenShakeRef
        )
    }
}
