package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrassGold
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SunGold
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Visual Sun/Moon Tracker Dial displaying the orbital trajectory of the Sun and Moon,
 * dawn/dusk horizon transitions, time phase (Dawn, Morning, Midday, Dusk, Night),
 * and current day/time with tactile interactive fast-forward capabilities.
 */
@Composable
fun SunMoonTrackerDial(
    gameTimeHour: Float,
    gameTimeDay: Int,
    onDialClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalMinutes = (gameTimeHour * 60).toInt()
    val h = (totalMinutes / 60) % 24
    val m = totalMinutes % 60
    val ampm = if (h < 12) "AM" else "PM"
    val displayH = if (h == 0) 12 else if (h > 12) h - 12 else h
    val timeFormatted = String.format("%02d:%02d %s", displayH, m, ampm)

    val isDaytime = gameTimeHour in 6.0f..18.0f
    val isDuskOrDawn = gameTimeHour in 5.0f..7.0f || gameTimeHour in 17.5f..19.5f

    // Phase description label
    val phaseName = when {
        gameTimeHour in 5.0f..7.0f -> "Dawn"
        gameTimeHour in 7.0f..11.5f -> "Morning"
        gameTimeHour in 11.5f..14.0f -> "Midday"
        gameTimeHour in 14.0f..17.5f -> "Afternoon"
        gameTimeHour in 17.5f..19.5f -> "Dusk"
        gameTimeHour in 19.5f..23.99f || gameTimeHour in 0.0f..5.0f -> "Night"
        else -> "Day"
    }

    // Normalized progress along the 24h orbital cycle (0.0 to 1.0)
    val dayFraction = (gameTimeHour / 24.0f).coerceIn(0f, 1f)

    val animatedAngle by animateFloatAsState(
        targetValue = dayFraction * 360f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "sun_moon_angle"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = SunGold)
            ) { onDialClick() }
            .testTag("sun_moon_tracker_dial"),
        color = Color(0xF00D2220),
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.2.dp,
            color = if (isDaytime) SunGold.copy(alpha = 0.65f) else CleanCyan.copy(alpha = 0.65f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            // 1. Orbital Celestial Dial Graphic (Canvas)
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .testTag("celestial_orbit_canvas"),
                contentAlignment = Alignment.Center
            ) {
                CelestialDialCanvas(
                    hour = gameTimeHour,
                    isDaytime = isDaytime,
                    angleDeg = animatedAngle
                )
            }

            // 2. Day, Time, and Phase Text Column
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Day $gameTimeDay",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "•",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                    Text(
                        text = timeFormatted,
                        color = if (isDaytime) SunGold else CleanCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = phaseName.uppercase(),
                        color = if (isDuskOrDawn) Color(0xFFFFAB40) else if (isDaytime) Color(0xFFFFF176) else Color(0xFF80DEEA),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "(+2h ⏩)",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 7.5.sp
                    )
                }
            }
        }
    }
}

/**
 * Custom Canvas drawing the rotating celestial disc with Sun rays,
 * Moon crescent, orbital trajectory ring, and glowing atmosphere.
 */
@Composable
private fun CelestialDialCanvas(
    hour: Float,
    isDaytime: Boolean,
    angleDeg: Float
) {
    Canvas(modifier = Modifier.size(34.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = (size.width / 2f) - 3f

        // 1. Sky Dome Background Gradient
        val skyGradient = if (isDaytime) {
            Brush.radialGradient(
                colors = listOf(Color(0xFF4FC3F7), Color(0xFF0277BD), Color(0xFF01579B)),
                center = center,
                radius = radius
            )
        } else {
            Brush.radialGradient(
                colors = listOf(Color(0xFF283593), Color(0xFF1A237E), Color(0xFF0D1B2A)),
                center = center,
                radius = radius
            )
        }

        drawCircle(
            brush = skyGradient,
            radius = radius,
            center = center
        )

        // 2. Orbital Trajectory Ring
        drawCircle(
            color = Color.White.copy(alpha = 0.25f),
            radius = radius * 0.75f,
            center = center,
            style = Stroke(width = 1.2f, cap = StrokeCap.Round)
        )

        // 3. Horizon separator line
        drawLine(
            color = Color.White.copy(alpha = 0.35f),
            start = Offset(center.x - radius, center.y),
            end = Offset(center.x + radius, center.y),
            strokeWidth = 1f
        )

        // 4. Calculate Celestial Body Positions (Sun opposite to Moon)
        // 6 AM is sunrise (left horizon: -PI), 12 PM is zenith (-PI/2), 6 PM is sunset (0), 12 AM is nadir (PI/2)
        val sunRad = ((hour - 6.0f) / 12.0f * PI).toFloat()
        val orbitRadius = radius * 0.75f

        val sunX = center.x + cos(sunRad - (PI / 2.0).toFloat()) * orbitRadius
        val sunY = center.y + sin(sunRad - (PI / 2.0).toFloat()) * orbitRadius

        val moonX = center.x - cos(sunRad - (PI / 2.0).toFloat()) * orbitRadius
        val moonY = center.y - sin(sunRad - (PI / 2.0).toFloat()) * orbitRadius

        // 5. Draw Sun (Golden disc with radiant corona)
        if (hour in 5.0f..19.0f) {
            val sunPos = Offset(sunX, sunY)
            // Sun Glow
            drawCircle(
                color = Color(0x66FFD700),
                radius = 5.5f,
                center = sunPos
            )
            // Sun Disc
            drawCircle(
                color = Color(0xFFFFEE58),
                radius = 3.5f,
                center = sunPos
            )
            // Sun Core
            drawCircle(
                color = Color(0xFFFFF9C4),
                radius = 2.0f,
                center = sunPos
            )
        }

        // 6. Draw Moon (Luminous silver crescent)
        if (hour < 7.0f || hour > 17.0f) {
            val moonPos = Offset(moonX, moonY)
            // Moon Glow
            drawCircle(
                color = Color(0x5500E5FF),
                radius = 4.5f,
                center = moonPos
            )
            // Moon Disc
            drawCircle(
                color = Color(0xFFE0F7FA),
                radius = 3.0f,
                center = moonPos
            )
            // Moon shadow cutout to form crescent
            drawCircle(
                color = Color(0xFF1A237E),
                radius = 2.4f,
                center = Offset(moonPos.x + 1.2f, moonPos.y - 0.8f)
            )
        }

        // 7. Outer Brass/Solar Rim
        drawCircle(
            color = if (isDaytime) Color(0xFFFFD54F) else Color(0xFF80DEEA),
            radius = radius,
            center = center,
            style = Stroke(width = 1.5f)
        )
    }
}
