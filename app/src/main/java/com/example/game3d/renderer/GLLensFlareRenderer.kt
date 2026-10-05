package com.example.game3d.renderer

import android.opengl.GLES20
import android.opengl.Matrix
import com.example.data.model.WeatherType
import com.example.game3d.opengl.GLLensFlareShader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Screen-Space Lens Flare Renderer supporting:
 * - Real-time perspective projection of the celestial sun into screen coordinates
 * - Multi-element optical artifacts: central corona burst, multi-blade starburst rays,
 *   horizontal anamorphic glare streak, chromatic diffraction halo ring, and multi-stage iris ghost discs
 * - Dynamic scaling and smooth 24-hour color shifting across sunrise ambers, noon diamond whites, and sunset magentas
 * - Atmospheric and weather cloud occlusion modulation
 */
class GLLensFlareRenderer {

    private val shader = GLLensFlareShader()
    private var isInitialized = false

    private lateinit var quadVertexBuffer: FloatBuffer
    private val transformMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)

    // Cached projection result vectors
    private val clipPos = FloatArray(4)
    private val worldSunPos = FloatArray(4)

    fun init() {
        if (isInitialized) return
        shader.init()

        // Setup unit quad [-1, 1] vertex coordinates
        val quadVertices = floatArrayOf(
            -1.0f, -1.0f,
             1.0f, -1.0f,
            -1.0f,  1.0f,
             1.0f,  1.0f
        )
        quadVertexBuffer = ByteBuffer.allocateDirect(quadVertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(quadVertices)
                position(0)
            }

        isInitialized = true
    }

    fun render(
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray,
        camX: Float,
        camY: Float,
        camZ: Float,
        viewportWidth: Float,
        viewportHeight: Float,
        hour: Float,
        weather: WeatherType,
        lightingState: LightingState?,
        animTimeSec: Float
    ) {
        if (!isInitialized) {
            init()
        }
        if (shader.programId == 0) return

        // 1. Calculate Celestial Sun Direction Vector (East to West celestial arc)
        val sunAngleRad = ((hour - 6.0f) / 12.0f * PI).toFloat()
        val sunDirX = -cos(sunAngleRad)
        val sunDirY = sin(sunAngleRad)
        val sunDirZ = 0.4f

        // Sun distance placement in world space
        val sunDist = 80.0f
        worldSunPos[0] = camX + sunDirX * sunDist
        worldSunPos[1] = camY + sunDirY * sunDist
        worldSunPos[2] = camZ + sunDirZ * sunDist
        worldSunPos[3] = 1.0f

        // 2. Project Sun Position to Screen-Space (View -> Projection -> Clip Coordinates)
        val viewSun = FloatArray(4)
        Matrix.multiplyMV(viewSun, 0, viewMatrix, 0, worldSunPos, 0)
        Matrix.multiplyMV(clipPos, 0, projectionMatrix, 0, viewSun, 0)

        val clipW = clipPos[3]
        // If sun is behind camera near plane, skip lens flare
        if (clipW <= 0.01f) return

        val ndcX = clipPos[0] / clipW
        val ndcY = clipPos[1] / clipW
        val ndcZ = clipPos[2] / clipW

        // Check depth bounds
        if (ndcZ < -1.2f || ndcZ > 1.2f) return

        // 3. Compute Viewport Distance and Boundary Falloff
        val distFromCenter = sqrt(ndcX * ndcX + ndcY * ndcY)
        // Flare begins fading when sun passes beyond normalized viewport radius 1.15
        val boundaryFade = when {
            distFromCenter < 1.0f -> 1.0f
            distFromCenter > 1.6f -> 0.0f
            else -> 1.0f - ((distFromCenter - 1.0f) / 0.6f)
        }.coerceIn(0.0f, 1.0f)

        if (boundaryFade <= 0.001f) return

        // 4. Compute 24-Hour Day/Night Solar Intensity & Transitions
        val timeFade = when {
            hour in 5.2f..6.8f -> (hour - 5.2f) / 1.6f // Sunrise fadeIn
            hour in 6.8f..17.4f -> 1.0f // Daylight high visibility
            hour in 17.4f..18.8f -> 1.0f - (hour - 17.4f) / 1.4f // Sunset fadeOut
            else -> 0.0f // Night
        }.coerceIn(0.0f, 1.0f)

        if (timeFade <= 0.001f) return

        // 5. Compute Weather & Cloud Density Attenuation
        val weatherFactor = when (weather) {
            WeatherType.SUNNY_CLEAR -> 1.0f
            WeatherType.HEATWAVE -> 1.15f
            WeatherType.WIND_GALE -> 0.95f
            WeatherType.MISTY_NEBULA -> 0.40f
            WeatherType.CLOUDY_OVERCAST -> 0.35f
            WeatherType.RAINY_STORM -> 0.20f
            WeatherType.STORM -> 0.08f
        }

        val cloudCoverage = lightingState?.cloudCoverage ?: 0.25f
        val cloudDensity = lightingState?.cloudDensity ?: 0.85f
        val cloudOcclusion = ((1.0f - cloudCoverage * 0.70f) * (1.0f - cloudDensity * 0.20f)).coerceIn(0.05f, 1.0f)

        val globalFlareAlpha = boundaryFade * timeFade * weatherFactor * cloudOcclusion
        if (globalFlareAlpha <= 0.005f) return

        // 6. Color Palette Transition based on Time of Day
        val isSunrise = hour in 5.2f..8.0f
        val isSunset = hour in 16.5f..18.8f
        val isNoon = hour in 10.5f..14.5f

        val palette = computeFlarePalette(hour, isSunrise, isSunset, isNoon)

        // 7. Setup OpenGL Blending State for Screen-Space Lens Flare
        val aspect = viewportWidth / max(1.0f, viewportHeight)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthMask(false)
        GLES20.glEnable(GLES20.GL_BLEND)
        // Additive blending for luminous optical transmission
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE)

        shader.use()
        GLES20.glUniform1f(shader.uTimeLoc, animTimeSec)

        quadVertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(shader.aPositionLoc)
        GLES20.glVertexAttribPointer(shader.aPositionLoc, 2, GLES20.GL_FLOAT, false, 0, quadVertexBuffer)

        // 8. Render Lens Flare Artifacts along the Optical Axis
        // Optical Axis: vector from Sun (ndcX, ndcY) through Center (0, 0)
        // Element position at optical interpolation t: pos(t) = (1 - t) * SunPos

        // A. Primary Sun Corona Burst (Centered at Sun, t = 0.0)
        val coronaScale = if (isSunrise || isSunset) 0.58f else 0.48f
        drawFlareElement(
            elementType = 0,
            centerX = ndcX,
            centerY = ndcY,
            scaleX = coronaScale,
            scaleY = coronaScale,
            aspect = aspect,
            colorR = palette.coronaR,
            colorG = palette.coronaG,
            colorB = palette.coronaB,
            colorA = 0.85f * globalFlareAlpha,
            param = 1.6f
        )

        // B. Cinematic Anamorphic Horizontal Streak (Centered at Sun, t = 0.0)
        val streakWidth = if (isSunrise || isSunset) 2.2f else 1.5f
        val streakHeight = if (isSunrise || isSunset) 0.16f else 0.10f
        drawFlareElement(
            elementType = 2,
            centerX = ndcX,
            centerY = ndcY,
            scaleX = streakWidth,
            scaleY = streakHeight,
            aspect = aspect,
            colorR = palette.streakR,
            colorG = palette.streakG,
            colorB = palette.streakB,
            colorA = 0.90f * globalFlareAlpha,
            param = 0.0f
        )

        // C. Multi-Point Diffraction Starburst Rays (Centered at Sun, t = 0.0)
        val starburstScale = if (isNoon) 0.70f else 0.55f
        val rayCount = if (isNoon) 12.0f else 8.0f
        drawFlareElement(
            elementType = 1,
            centerX = ndcX,
            centerY = ndcY,
            scaleX = starburstScale,
            scaleY = starburstScale,
            aspect = aspect,
            colorR = palette.starburstR,
            colorG = palette.starburstG,
            colorB = palette.starburstB,
            colorA = 0.75f * globalFlareAlpha,
            param = rayCount
        )

        // D. Chromatic Diffraction Halo Ring (Centered at Sun, t = 0.0 or slightly along axis)
        val haloScale = 0.82f
        drawFlareElement(
            elementType = 3,
            centerX = ndcX * 0.95f,
            centerY = ndcY * 0.95f,
            scaleX = haloScale,
            scaleY = haloScale,
            aspect = aspect,
            colorR = palette.haloR,
            colorG = palette.haloG,
            colorB = palette.haloB,
            colorA = 0.55f * globalFlareAlpha,
            param = 0.14f
        )

        // E. Multiple Multi-Spectral Ghost Discs along Optical Axis
        // Ghost 1: Near Sun (t = 0.35)
        val g1X = ndcX * (1.0f - 0.35f)
        val g1Y = ndcY * (1.0f - 0.35f)
        drawFlareElement(
            elementType = 5,
            centerX = g1X,
            centerY = g1Y,
            scaleX = 0.18f,
            scaleY = 0.18f,
            aspect = aspect,
            colorR = palette.ghost1R,
            colorG = palette.ghost1G,
            colorB = palette.ghost1B,
            colorA = 0.45f * globalFlareAlpha,
            param = 1.2f
        )

        // Ghost 2: Hexagonal Iris (t = 0.65)
        val g2X = ndcX * (1.0f - 0.65f)
        val g2Y = ndcY * (1.0f - 0.65f)
        drawFlareElement(
            elementType = 4,
            centerX = g2X,
            centerY = g2Y,
            scaleX = 0.22f,
            scaleY = 0.22f,
            aspect = aspect,
            colorR = palette.ghost2R,
            colorG = palette.ghost2G,
            colorB = palette.ghost2B,
            colorA = 0.50f * globalFlareAlpha,
            param = 0.0f
        )

        // Ghost 3: Central Spectral Ring (t = 1.0, screen center)
        drawFlareElement(
            elementType = 3,
            centerX = 0.0f,
            centerY = 0.0f,
            scaleX = 0.38f,
            scaleY = 0.38f,
            aspect = aspect,
            colorR = palette.ghost3R,
            colorG = palette.ghost3G,
            colorB = palette.ghost3B,
            colorA = 0.35f * globalFlareAlpha,
            param = 0.10f
        )

        // Ghost 4: Past Center (t = 1.35)
        val g4X = ndcX * (1.0f - 1.35f)
        val g4Y = ndcY * (1.0f - 1.35f)
        drawFlareElement(
            elementType = 4,
            centerX = g4X,
            centerY = g4Y,
            scaleX = 0.26f,
            scaleY = 0.26f,
            aspect = aspect,
            colorR = palette.ghost4R,
            colorG = palette.ghost4G,
            colorB = palette.ghost4B,
            colorA = 0.40f * globalFlareAlpha,
            param = 0.0f
        )

        // Ghost 5: Far Opposite Side Large Orb (t = 1.70)
        val g5X = ndcX * (1.0f - 1.70f)
        val g5Y = ndcY * (1.0f - 1.70f)
        drawFlareElement(
            elementType = 0,
            centerX = g5X,
            centerY = g5Y,
            scaleX = 0.35f,
            scaleY = 0.35f,
            aspect = aspect,
            colorR = palette.ghost5R,
            colorG = palette.ghost5G,
            colorB = palette.ghost5B,
            colorA = 0.30f * globalFlareAlpha,
            param = 1.8f
        )

        // Ghost 6: Small Intense Highlight Behind Sun (t = -0.22)
        val g6X = ndcX * (1.0f - (-0.22f))
        val g6Y = ndcY * (1.0f - (-0.22f))
        drawFlareElement(
            elementType = 0,
            centerX = g6X,
            centerY = g6Y,
            scaleX = 0.08f,
            scaleY = 0.08f,
            aspect = aspect,
            colorR = 1.0f,
            colorG = 0.95f,
            colorB = 0.80f,
            colorA = 0.60f * globalFlareAlpha,
            param = 0.8f
        )

        // 9. Restore Depth Test for standard rendering
        GLES20.glDisableVertexAttribArray(shader.aPositionLoc)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthMask(true)
    }

    private fun drawFlareElement(
        elementType: Int,
        centerX: Float,
        centerY: Float,
        scaleX: Float,
        scaleY: Float,
        aspect: Float,
        colorR: Float,
        colorG: Float,
        colorB: Float,
        colorA: Float,
        param: Float
    ) {
        Matrix.setIdentityM(transformMatrix, 0)
        // Translate in NDC
        Matrix.translateM(transformMatrix, 0, centerX, centerY, 0.0f)
        // Scale with aspect correction to keep circular / square proportions
        Matrix.scaleM(transformMatrix, 0, scaleX / aspect, scaleY, 1.0f)

        GLES20.glUniformMatrix4fv(shader.uTransformLoc, 1, false, transformMatrix, 0)
        GLES20.glUniform1i(shader.uElementTypeLoc, elementType)
        GLES20.glUniform4f(shader.uColorLoc, colorR, colorG, colorB, colorA)
        GLES20.glUniform1f(shader.uParamLoc, param)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    private data class FlarePalette(
        val coronaR: Float, val coronaG: Float, val coronaB: Float,
        val streakR: Float, val streakG: Float, val streakB: Float,
        val starburstR: Float, val starburstG: Float, val starburstB: Float,
        val haloR: Float, val haloG: Float, val haloB: Float,
        val ghost1R: Float, val ghost1G: Float, val ghost1B: Float,
        val ghost2R: Float, val ghost2G: Float, val ghost2B: Float,
        val ghost3R: Float, val ghost3G: Float, val ghost3B: Float,
        val ghost4R: Float, val ghost4G: Float, val ghost4B: Float,
        val ghost5R: Float, val ghost5G: Float, val ghost5B: Float
    )

    private fun computeFlarePalette(
        hour: Float,
        isSunrise: Boolean,
        isSunset: Boolean,
        isNoon: Boolean
    ): FlarePalette {
        return when {
            isSunrise -> {
                // Sunrise ambers, warm fiery peach, gold, and soft morning rose
                FlarePalette(
                    coronaR = 1.00f, coronaG = 0.72f, coronaB = 0.22f,
                    streakR = 1.00f, streakG = 0.48f, streakB = 0.16f,
                    starburstR = 1.00f, starburstG = 0.85f, starburstB = 0.32f,
                    haloR = 1.00f, haloG = 0.60f, haloB = 0.42f,
                    ghost1R = 1.00f, ghost1G = 0.58f, ghost1B = 0.28f,
                    ghost2R = 1.00f, ghost2G = 0.75f, ghost2B = 0.35f,
                    ghost3R = 0.95f, ghost3G = 0.45f, ghost3B = 0.62f,
                    ghost4R = 0.85f, ghost4G = 0.35f, ghost4B = 0.70f,
                    ghost5R = 0.65f, ghost5G = 0.30f, ghost5B = 0.80f
                )
            }
            isSunset -> {
                // Sunset magenta, burnished copper, deep sunset amber, and royal purple
                FlarePalette(
                    coronaR = 1.00f, coronaG = 0.52f, coronaB = 0.25f,
                    streakR = 1.00f, streakG = 0.22f, streakB = 0.42f,
                    starburstR = 1.00f, starburstG = 0.62f, starburstB = 0.18f,
                    haloR = 0.78f, haloG = 0.30f, haloB = 0.85f,
                    ghost1R = 0.98f, ghost1G = 0.35f, ghost1B = 0.45f,
                    ghost2R = 1.00f, ghost2G = 0.60f, ghost2B = 0.20f,
                    ghost3R = 0.80f, ghost3G = 0.25f, ghost3B = 0.75f,
                    ghost4R = 0.60f, ghost4G = 0.20f, ghost4B = 0.85f,
                    ghost5R = 0.40f, ghost5G = 0.15f, ghost5B = 0.75f
                )
            }
            isNoon -> {
                // High Noon brilliant diamond-white, cyan, spectral gold, and blue
                FlarePalette(
                    coronaR = 1.00f, coronaG = 0.98f, coronaB = 0.92f,
                    streakR = 0.85f, streakG = 0.96f, streakB = 1.00f,
                    starburstR = 1.00f, starburstG = 1.00f, starburstB = 0.95f,
                    haloR = 0.42f, haloG = 0.85f, haloB = 1.00f,
                    ghost1R = 0.35f, ghost1G = 0.82f, ghost1B = 1.00f,
                    ghost2R = 1.00f, ghost2G = 0.92f, ghost2B = 0.45f,
                    ghost3R = 0.40f, ghost3G = 0.70f, ghost3B = 1.00f,
                    ghost4R = 0.65f, ghost4G = 0.45f, ghost4B = 0.95f,
                    ghost5R = 0.25f, ghost5G = 0.55f, ghost5B = 0.95f
                )
            }
            else -> {
                // Mid-morning / Afternoon bright warm daylight
                val t = if (hour < 12.0f) (hour - 8.0f) / 2.5f else (16.5f - hour) / 2.0f
                val clampedT = t.coerceIn(0.0f, 1.0f)
                FlarePalette(
                    coronaR = 1.00f,
                    coronaG = 0.82f + 0.16f * clampedT,
                    coronaB = 0.45f + 0.47f * clampedT,
                    streakR = 0.92f + 0.08f * clampedT,
                    streakG = 0.65f + 0.31f * clampedT,
                    streakB = 0.35f + 0.65f * clampedT,
                    starburstR = 1.00f,
                    starburstG = 0.92f + 0.08f * clampedT,
                    starburstB = 0.60f + 0.35f * clampedT,
                    haloR = 0.70f - 0.28f * clampedT,
                    haloG = 0.70f + 0.15f * clampedT,
                    haloB = 0.75f + 0.25f * clampedT,
                    ghost1R = 0.70f - 0.35f * clampedT,
                    ghost1G = 0.70f + 0.12f * clampedT,
                    ghost1B = 0.60f + 0.40f * clampedT,
                    ghost2R = 1.00f,
                    ghost2G = 0.85f + 0.07f * clampedT,
                    ghost2B = 0.40f + 0.05f * clampedT,
                    ghost3R = 0.65f - 0.25f * clampedT,
                    ghost3G = 0.55f + 0.15f * clampedT,
                    ghost3B = 0.85f + 0.15f * clampedT,
                    ghost4R = 0.75f - 0.10f * clampedT,
                    ghost4G = 0.40f + 0.05f * clampedT,
                    ghost5R = 0.50f - 0.25f * clampedT,
                    ghost4B = 0.90f + 0.05f * clampedT,
                    ghost5G = 0.40f + 0.15f * clampedT,
                    ghost5B = 0.88f + 0.07f * clampedT
                )
            }
        }
    }

    fun dispose() {
        if (isInitialized) {
            shader.dispose()
            isInitialized = false
        }
    }
}
