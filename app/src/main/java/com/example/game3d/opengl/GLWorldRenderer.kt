package com.example.game3d.opengl

import android.opengl.GLSurfaceView
import com.example.data.local.EnergyNodeEntity
import com.example.data.local.LivestockEntity
import com.example.data.local.PlotEntity
import com.example.game3d.player.ThirdPersonCamera
import com.example.game3d.player.ThirdPersonPlayer
import com.example.game3d.renderer.GameRenderer
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
    @Volatile var livestockRef: List<LivestockEntity> = emptyList()
    @Volatile var animTimeSec: Float = 0.0f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        gameRenderer.create()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        gameRenderer.resize(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        gameRenderer.render(
            player = playerRef,
            thirdPersonCamera = cameraRef,
            lightingState = lightingRef,
            plots = plotsRef,
            energyNodes = energyNodesRef,
            livestock = livestockRef,
            animTimeSec = animTimeSec
        )
    }
}
