package com.example.game3d.renderer

import androidx.compose.ui.graphics.Color
import com.example.data.model.WeatherType
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

data class LightingState(
    val skyTopColor: Color,
    val skyHorizonColor: Color,
    val ambientColor: Color,
    val directionalLightColor: Color,
    val lightDirX: Float,
    val lightDirY: Float,
    val lightDirZ: Float,
    val ambientIntensity: Float,
    val directLightIntensity: Float,
    val shadowAlpha: Float,
    val sunScreenPosX: Float, // Normalized 0..1 for UI display
    val sunScreenPosY: Float,
    val isNight: Boolean
)

object DayNightLightingSystem {

    private data class Keyframe(
        val hour: Float,
        val skyTop: Color,
        val skyHorizon: Color,
        val ambient: Color,
        val directLight: Color,
        val ambientIntensity: Float,
        val directIntensity: Float,
        val shadowAlpha: Float
    )

    // Continuous 24-hour keyframes with Solarpunk color palette
    private val keyframes = listOf(
        // 0:00 Midnight
        Keyframe(
            hour = 0.0f,
            skyTop = Color(0xFF070B18),
            skyHorizon = Color(0xFF0E162C),
            ambient = Color(0xFF1B2A4A),
            directLight = Color(0xFF80A8FF),
            ambientIntensity = 0.25f,
            directIntensity = 0.20f,
            shadowAlpha = 0.20f
        ),
        // 4.5:00 Astronomical Dawn
        Keyframe(
            hour = 4.5f,
            skyTop = Color(0xFF14122C),
            skyHorizon = Color(0xFF4A284E),
            ambient = Color(0xFF382D54),
            directLight = Color(0xFFFFAB91),
            ambientIntensity = 0.35f,
            directIntensity = 0.30f,
            shadowAlpha = 0.30f
        ),
        // 6.0:00 Sunrise
        Keyframe(
            hour = 6.0f,
            skyTop = Color(0xFF1A3B6E),
            skyHorizon = Color(0xFFFF9E43),
            ambient = Color(0xFFE67E22),
            directLight = Color(0xFFFFD54F),
            ambientIntensity = 0.65f,
            directIntensity = 0.70f,
            shadowAlpha = 0.45f
        ),
        // 8.5:00 Golden Morning
        Keyframe(
            hour = 8.5f,
            skyTop = Color(0xFF1565C0),
            skyHorizon = Color(0xFF81D4FA),
            ambient = Color(0xFFE1F5FE),
            directLight = Color(0xFFFFF9C4),
            ambientIntensity = 0.85f,
            directIntensity = 0.90f,
            shadowAlpha = 0.50f
        ),
        // 12.0:00 High Noon Zenith
        Keyframe(
            hour = 12.0f,
            skyTop = Color(0xFF0D47A1),
            skyHorizon = Color(0xFFB3E5FC),
            ambient = Color(0xFFFFFFFF),
            directLight = Color(0xFFFFFDE7),
            ambientIntensity = 1.0f,
            directIntensity = 1.0f,
            shadowAlpha = 0.55f
        ),
        // 15.5:00 Warm Solarpunk Afternoon
        Keyframe(
            hour = 15.5f,
            skyTop = Color(0xFF1976D2),
            skyHorizon = Color(0xFFFFF59D),
            ambient = Color(0xFFFFF9C4),
            directLight = Color(0xFFFFE082),
            ambientIntensity = 0.90f,
            directIntensity = 0.95f,
            shadowAlpha = 0.52f
        ),
        // 18.0:00 Golden Hour Sunset
        Keyframe(
            hour = 18.0f,
            skyTop = Color(0xFF311B92),
            skyHorizon = Color(0xFFFF5722),
            ambient = Color(0xFFFF7043),
            directLight = Color(0xFFFFB300),
            ambientIntensity = 0.65f,
            directIntensity = 0.75f,
            shadowAlpha = 0.48f
        ),
        // 20.0:00 Twilight Dusk
        Keyframe(
            hour = 20.0f,
            skyTop = Color(0xFF1A0E38),
            skyHorizon = Color(0xFF7B1FA2),
            ambient = Color(0xFF4A148C),
            directLight = Color(0xFFBA68C8),
            ambientIntensity = 0.38f,
            directIntensity = 0.35f,
            shadowAlpha = 0.28f
        ),
        // 24.0:00 Midnight Loop
        Keyframe(
            hour = 24.0f,
            skyTop = Color(0xFF070B18),
            skyHorizon = Color(0xFF0E162C),
            ambient = Color(0xFF1B2A4A),
            directLight = Color(0xFF80A8FF),
            ambientIntensity = 0.25f,
            directIntensity = 0.20f,
            shadowAlpha = 0.20f
        )
    )

    fun calculateLighting(hourOfDay: Float, weather: WeatherType): LightingState {
        val normalizedHour = (hourOfDay % 24.0f + 24.0f) % 24.0f

        // Find adjacent keyframes
        var k1 = keyframes[0]
        var k2 = keyframes[1]
        for (i in 0 until keyframes.size - 1) {
            if (normalizedHour >= keyframes[i].hour && normalizedHour <= keyframes[i + 1].hour) {
                k1 = keyframes[i]
                k2 = keyframes[i + 1]
                break
            }
        }

        val span = k2.hour - k1.hour
        val linearT = if (span > 0.001f) (normalizedHour - k1.hour) / span else 0.0f
        // Hermite smoothstep interpolation
        val t = linearT * linearT * (3.0f - 2.0f * linearT)

        // Interpolate colors
        val skyTop = lerpColor(k1.skyTop, k2.skyTop, t)
        val skyHorizon = lerpColor(k1.skyHorizon, k2.skyHorizon, t)
        val ambient = lerpColor(k1.ambient, k2.ambient, t)
        val directLight = lerpColor(k1.directLight, k2.directLight, t)

        val ambIntensity = k1.ambientIntensity + (k2.ambientIntensity - k1.ambientIntensity) * t
        val dirIntensity = k1.directIntensity + (k2.directIntensity - k1.directIntensity) * t
        val shadowAlpha = k1.shadowAlpha + (k2.shadowAlpha - k1.shadowAlpha) * t

        // Sun & Moon Orbital Trajectory
        // Sun reaches zenith at hour 12, sets at hour 18, rises at 6
        val sunAngleRad = ((normalizedHour - 6.0f) / 12.0f) * Math.PI.toFloat()
        val isDay = normalizedHour in 5.5f..19.0f
        val lightDirX = cos(sunAngleRad)
        val lightDirY = max(0.1f, sin(sunAngleRad))
        val lightDirZ = sin(sunAngleRad * 0.5f) * 0.5f

        // Modulate with weather
        var finalSkyTop = skyTop
        var finalSkyHorizon = skyHorizon
        var finalAmbient = ambient
        var finalAmbInt = ambIntensity
        var finalDirInt = dirIntensity
        var finalShadowAlpha = shadowAlpha

        when (weather) {
            WeatherType.RAINY_STORM -> {
                finalSkyTop = lerpColor(finalSkyTop, Color(0xFF2C3E50), 0.65f)
                finalSkyHorizon = lerpColor(finalSkyHorizon, Color(0xFF546E7A), 0.55f)
                finalDirInt *= 0.45f
                finalShadowAlpha *= 0.4f
            }
            WeatherType.HEATWAVE -> {
                finalSkyHorizon = lerpColor(finalSkyHorizon, Color(0xFFFFD54F), 0.35f)
                finalDirInt *= 1.25f
            }
            WeatherType.WIND_GALE -> {
                finalSkyHorizon = lerpColor(finalSkyHorizon, Color(0xFF80DEEA), 0.25f)
            }
            WeatherType.MISTY_NEBULA -> {
                finalSkyHorizon = lerpColor(finalSkyHorizon, Color(0xFFB2EBF2), 0.45f)
                finalDirInt *= 0.65f
                finalAmbInt *= 1.15f
            }
            WeatherType.SUNNY_CLEAR -> {
                // Default crisp Solarpunk
            }
        }

        return LightingState(
            skyTopColor = finalSkyTop,
            skyHorizonColor = finalSkyHorizon,
            ambientColor = finalAmbient,
            directionalLightColor = directLight,
            lightDirX = lightDirX,
            lightDirY = lightDirY,
            lightDirZ = lightDirZ,
            ambientIntensity = finalAmbInt,
            directLightIntensity = finalDirInt,
            shadowAlpha = finalShadowAlpha,
            sunScreenPosX = (normalizedHour / 24.0f),
            sunScreenPosY = max(0.1f, 1.0f - sin(sunAngleRad)),
            isNight = !isDay
        )
    }

    private fun lerpColor(c1: Color, c2: Color, t: Float): Color {
        return Color(
            red = c1.red + (c2.red - c1.red) * t,
            green = c1.green + (c2.green - c1.green) * t,
            blue = c1.blue + (c2.blue - c1.blue) * t,
            alpha = c1.alpha + (c2.alpha - c1.alpha) * t
        )
    }
}
