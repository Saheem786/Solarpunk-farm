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
    val isNight: Boolean,
    // Procedural Cloud System Parameters
    val cloudCoverage: Float = 0.25f,
    val cloudDensity: Float = 0.85f,
    val cloudLightModulation: Float = 1.0f,
    val windOffsetU: Float = 0.0f,
    val windOffsetV: Float = 0.0f,
    val cloudColorTop: Color = Color.White,
    val cloudColorBottom: Color = Color(0xFFB0BEC5)
)

object DayNightLightingSystem {

    private data class Keyframe(
        val hour: Float,
        val skyTop: Color,
        val skyHorizon: Color,
        val ambient: Color,
        val directLight: Color,
        val cloudTop: Color,
        val cloudBottom: Color,
        val ambientIntensity: Float,
        val directIntensity: Float,
        val shadowAlpha: Float
    )

    // Continuous 24-hour keyframes with Solarpunk color palette & cloud illumination
    private val keyframes = listOf(
        // 0:00 Midnight
        Keyframe(
            hour = 0.0f,
            skyTop = Color(0xFF070B18),
            skyHorizon = Color(0xFF0E162C),
            ambient = Color(0xFF1B2A4A),
            directLight = Color(0xFF80A8FF),
            cloudTop = Color(0xFF6B7B9E),
            cloudBottom = Color(0xFF141E34),
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
            cloudTop = Color(0xFFFFD1B3),
            cloudBottom = Color(0xFF3D2746),
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
            cloudTop = Color(0xFFFFF0D0),
            cloudBottom = Color(0xFF8D4F5A),
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
            cloudTop = Color(0xFFFFFFFF),
            cloudBottom = Color(0xFFB0BEC5),
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
            cloudTop = Color(0xFFFFFFFF),
            cloudBottom = Color(0xFFCFD8DC),
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
            cloudTop = Color(0xFFFFFDF0),
            cloudBottom = Color(0xFFBDBDBD),
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
            cloudTop = Color(0xFFFFE082),
            cloudBottom = Color(0xFF6A1B9A),
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
            cloudTop = Color(0xFFCE93D8),
            cloudBottom = Color(0xFF2A1140),
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
            cloudTop = Color(0xFF6B7B9E),
            cloudBottom = Color(0xFF141E34),
            ambientIntensity = 0.25f,
            directIntensity = 0.20f,
            shadowAlpha = 0.20f
        )
    )

    fun calculateLighting(hourOfDay: Float, weather: WeatherType, animTimeSec: Float = 0.0f): LightingState {
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
        var cloudTop = lerpColor(k1.cloudTop, k2.cloudTop, t)
        var cloudBottom = lerpColor(k1.cloudBottom, k2.cloudBottom, t)

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

        // Procedural Cloud Baseline & Periodic Drift Simulation
        var baseCloudCoverage = 0.25f
        var cloudDensity = 0.85f
        var windSpeed = 0.035f

        when (weather) {
            WeatherType.SUNNY_CLEAR -> {
                baseCloudCoverage = 0.18f
                cloudDensity = 0.75f
                windSpeed = 0.025f
            }
            WeatherType.CLOUDY_OVERCAST -> {
                baseCloudCoverage = 0.80f
                cloudDensity = 0.95f
                windSpeed = 0.038f
                cloudTop = lerpColor(cloudTop, Color(0xFFCFD8DC), 0.5f)
                cloudBottom = lerpColor(cloudBottom, Color(0xFF546E7A), 0.5f)
            }
            WeatherType.RAINY_STORM -> {
                baseCloudCoverage = 0.92f
                cloudDensity = 1.0f
                windSpeed = 0.075f
                cloudTop = Color(0xFF78909C)
                cloudBottom = Color(0xFF263238)
            }
            WeatherType.HEATWAVE -> {
                baseCloudCoverage = 0.06f
                cloudDensity = 0.50f
                windSpeed = 0.015f
            }
            WeatherType.WIND_GALE -> {
                baseCloudCoverage = 0.50f
                cloudDensity = 0.85f
                windSpeed = 0.140f
            }
            WeatherType.MISTY_NEBULA -> {
                baseCloudCoverage = 0.68f
                cloudDensity = 0.70f
                windSpeed = 0.020f
                cloudTop = Color(0xFFE0F7FA)
                cloudBottom = Color(0xFF4DB6AC)
            }
            WeatherType.STORM -> {
                baseCloudCoverage = 0.96f
                cloudDensity = 1.0f
                windSpeed = 0.120f
                cloudTop = Color(0xFF455A64)
                cloudBottom = Color(0xFF1A1A2E)
            }
        }

        // Periodic drifting cloud wave (clouds naturally form, cluster, and drift across the sky dome)
        val cloudDriftWave = 0.14f * sin(animTimeSec * 0.10f) + 0.07f * cos(animTimeSec * 0.055f + 1.8f)
        val activeCloudCoverage = (baseCloudCoverage + cloudDriftWave).coerceIn(0.02f, 1.0f)

        // 2D Wind Drift Coordinates
        val windU = animTimeSec * windSpeed
        val windV = animTimeSec * (windSpeed * 0.35f)

        // Light Modulation on Ground based on Cloud Density (Dynamic Cloud Shadows)
        val cloudShadowAttenuation = (activeCloudCoverage * 0.50f).coerceIn(0.0f, 0.75f)
        val cloudLightModulation = 1.0f - cloudShadowAttenuation

        // Modulate with weather
        var finalSkyTop = skyTop
        var finalSkyHorizon = skyHorizon
        var finalAmbient = ambient
        var finalAmbInt = ambIntensity
        var finalDirInt = dirIntensity * cloudLightModulation
        var finalShadowAlpha = shadowAlpha * (0.4f + 0.6f * cloudLightModulation)

        when (weather) {
            WeatherType.RAINY_STORM -> {
                finalSkyTop = lerpColor(finalSkyTop, Color(0xFF2C3E50), 0.70f)
                finalSkyHorizon = lerpColor(finalSkyHorizon, Color(0xFF455A64), 0.65f)
                finalDirInt *= 0.30f
                finalAmbInt *= 0.75f
                finalShadowAlpha *= 0.25f
            }
            WeatherType.CLOUDY_OVERCAST -> {
                finalSkyTop = lerpColor(finalSkyTop, Color(0xFF546E7A), 0.55f)
                finalSkyHorizon = lerpColor(finalSkyHorizon, Color(0xFF90A4AE), 0.50f)
                finalDirInt *= 0.55f
                finalAmbInt *= 0.85f
                finalShadowAlpha *= 0.40f
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
            WeatherType.STORM -> {
                finalSkyTop = lerpColor(finalSkyTop, Color(0xFF1A1A2E), 0.85f)
                finalSkyHorizon = lerpColor(finalSkyHorizon, Color(0xFF2C3E50), 0.80f)
                finalDirInt *= 0.15f
                finalAmbInt *= 0.60f
                finalShadowAlpha *= 0.10f
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
            isNight = !isDay,
            cloudCoverage = activeCloudCoverage,
            cloudDensity = cloudDensity,
            cloudLightModulation = cloudLightModulation,
            windOffsetU = windU,
            windOffsetV = windV,
            cloudColorTop = cloudTop,
            cloudColorBottom = cloudBottom
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
