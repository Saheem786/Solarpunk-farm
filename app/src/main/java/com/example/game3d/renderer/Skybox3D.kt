package com.example.game3d.renderer

import android.opengl.GLES20
import android.opengl.Matrix
import com.example.game3d.opengl.GLMesh
import com.example.game3d.opengl.GLModelBuilder
import com.example.game3d.opengl.GLSkyboxShader
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 3D Skybox Dome executing the smooth 24-hour day/night celestial transition.
 * Transitions smoothly from:
 * - Sunrise glowing ambers & golds (5:00 - 8:00 AM)
 * - Radiant high noon azure blues (11:00 AM - 3:00 PM)
 * - Sunset blazing amber & twilight violet (5:30 - 8:00 PM)
 * - Deep midnight cosmic violets with twinkling stars (8:30 PM - 4:30 AM)
 */
class Skybox3D {

    private val skyMesh: GLMesh
    private val shader = GLSkyboxShader()
    private val viewWithoutTranslation = FloatArray(16)
    private val skyMVPMatrix = FloatArray(16)

    init {
        val builder = GLModelBuilder()
        // Inverted skybox cube/dome spanning outwards
        val size = 50.0f
        builder.addBox(0f, 0f, 0f, size, size, size, 1f, 1f, 1f)
        skyMesh = builder.build()
        shader.init()
    }

    fun render(
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray,
        lightingState: LightingState?,
        hour: Float,
        animTimeSec: Float
    ) {
        if (shader.programId == 0) return

        // 1. Remove camera translation from view matrix so the skybox remains centered on the observer
        System.arraycopy(viewMatrix, 0, viewWithoutTranslation, 0, 16)
        viewWithoutTranslation[12] = 0.0f
        viewWithoutTranslation[13] = 0.0f
        viewWithoutTranslation[14] = 0.0f

        Matrix.multiplyMM(skyMVPMatrix, 0, projectionMatrix, 0, viewWithoutTranslation, 0)

        // 2. Setup Depth & Cull state for Skybox
        GLES20.glDisable(GLES20.GL_CULL_FACE)
        GLES20.glDepthMask(false)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)

        shader.use()

        // 3. Upload MVP Matrix
        GLES20.glUniformMatrix4fv(shader.uMVPMatrixLoc, 1, false, skyMVPMatrix, 0)

        // 4. Calculate Celestial Colors based on 24-Hour Cycle
        val zenithR: Float
        val zenithG: Float
        val zenithB: Float
        val horizonR: Float
        val horizonG: Float
        val horizonB: Float
        val groundR: Float
        val groundG: Float
        val groundB: Float
        val starIntensity: Float

        if (lightingState != null) {
            zenithR = lightingState.skyTopColor.red
            zenithG = lightingState.skyTopColor.green
            zenithB = lightingState.skyTopColor.blue
            horizonR = lightingState.skyHorizonColor.red
            horizonG = lightingState.skyHorizonColor.green
            horizonB = lightingState.skyHorizonColor.blue
            groundR = horizonR * 0.45f
            groundG = horizonG * 0.45f
            groundB = horizonB * 0.45f
            starIntensity = if (lightingState.isNight) 1.0f else {
                when {
                    hour in 19.0f..20.5f -> (hour - 19.0f) / 1.5f
                    hour in 4.5f..6.0f -> 1.0f - (hour - 4.5f) / 1.5f
                    else -> 0.0f
                }
            }
        } else {
            // High noon fallback
            zenithR = 0.05f; zenithG = 0.28f; zenithB = 0.65f
            horizonR = 0.50f; horizonG = 0.82f; horizonB = 0.98f
            groundR = 0.35f; groundG = 0.65f; groundB = 0.80f
            starIntensity = 0.0f
        }

        // 5. Calculate Sun Direction Vector from hour (Rises east at 6 AM, Zenith at 12 PM, Sets west at 6 PM)
        val sunAngleRad = ((hour - 6.0f) / 12.0f * PI).toFloat()
        val sunDirX = -cos(sunAngleRad)
        val sunDirY = sin(sunAngleRad)
        val sunDirZ = 0.4f

        val isSunVisible = hour in 5.5f..18.5f
        val sunColorR = if (hour in 5.5f..7.5f || hour in 17.0f..18.5f) 1.0f else 1.0f
        val sunColorG = if (hour in 5.5f..7.5f || hour in 17.0f..18.5f) 0.65f else 0.95f
        val sunColorB = if (hour in 5.5f..7.5f || hour in 17.0f..18.5f) 0.25f else 0.80f
        val sunGlow = if (isSunVisible) 0.65f else 0.0f

        GLES20.glUniform3f(shader.uZenithColorLoc, zenithR, zenithG, zenithB)
        GLES20.glUniform3f(shader.uHorizonColorLoc, horizonR, horizonG, horizonB)
        GLES20.glUniform3f(shader.uGroundColorLoc, groundR, groundG, groundB)
        GLES20.glUniform3f(shader.uSunDirLoc, sunDirX, sunDirY, sunDirZ)
        GLES20.glUniform3f(shader.uSunColorLoc, sunColorR, sunColorG, sunColorB)
        GLES20.glUniform1f(shader.uSunGlowLoc, sunGlow)
        GLES20.glUniform1f(shader.uStarIntensityLoc, starIntensity)
        GLES20.glUniform1f(shader.uTimeLoc, animTimeSec)

        // Procedural Clouds Uniforms
        val cloudCoverage = lightingState?.cloudCoverage ?: 0.25f
        val cloudDensity = lightingState?.cloudDensity ?: 0.85f
        val windU = lightingState?.windOffsetU ?: (animTimeSec * 0.035f)
        val windV = lightingState?.windOffsetV ?: (animTimeSec * 0.012f)
        val cloudTop = lightingState?.cloudColorTop ?: androidx.compose.ui.graphics.Color.White
        val cloudBottom = lightingState?.cloudColorBottom ?: androidx.compose.ui.graphics.Color(0xFFB0BEC5)

        GLES20.glUniform1f(shader.uCloudCoverageLoc, cloudCoverage)
        GLES20.glUniform1f(shader.uCloudDensityLoc, cloudDensity)
        GLES20.glUniform2f(shader.uWindOffsetLoc, windU, windV)
        GLES20.glUniform3f(shader.uCloudColorTopLoc, cloudTop.red, cloudTop.green, cloudTop.blue)
        GLES20.glUniform3f(shader.uCloudColorBottomLoc, cloudBottom.red, cloudBottom.green, cloudBottom.blue)

        // 6. Draw Skybox Mesh
        skyMesh.vertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(shader.aPositionLoc)
        GLES20.glVertexAttribPointer(
            shader.aPositionLoc,
            3,
            GLES20.GL_FLOAT,
            false,
            0,
            skyMesh.vertexBuffer
        )

        if (skyMesh.indexBuffer != null && skyMesh.indexCount > 0) {
            skyMesh.indexBuffer.position(0)
            GLES20.glDrawElements(
                GLES20.GL_TRIANGLES,
                skyMesh.indexCount,
                GLES20.GL_UNSIGNED_SHORT,
                skyMesh.indexBuffer
            )
        } else {
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, skyMesh.vertexCount)
        }

        GLES20.glDisableVertexAttribArray(shader.aPositionLoc)

        // 7. Restore Depth & Cull State for World Geometry
        GLES20.glDepthMask(true)
        GLES20.glDepthFunc(GLES20.GL_LESS)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glCullFace(GLES20.GL_BACK)
    }
}
