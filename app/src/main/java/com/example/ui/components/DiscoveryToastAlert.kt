package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SunGold
import kotlinx.coroutines.delay

/**
 * Data representation of a Discovery Zone Alert.
 */
data class DiscoveryAlertData(
    val id: Long = System.currentTimeMillis(),
    val title: String, // e.g. "Discovered: Deep Forest" or "Discovered: Abandoned Ranger Tower"
    val subtitle: String, // e.g. "Towering pine canopy & wildlife haven (+50 🪙, +10 Eco)"
    val categoryLabel: String = "NEW BIOME DISCOVERED",
    val icon: ImageVector = Icons.Default.Info,
    val accentColor: Color = Color(0xFFFFD54F),
    val rewardCoins: Int = 0,
    val rewardItemName: String? = null
)

/**
 * High-Polish Solarpunk HUD Discovery Toast & Alert Notification.
 * Triggered whenever the player enters a new discovery zone (Biome or POI) for the first time.
 * Features:
 * - Smooth entrance spring physics + subtle glow pulse
 * - Ambient animated shimmering gradient border
 * - Glowing discovery category badge with spark icons
 * - Auto-dismiss countdown bar & tap to view on Map
 */
@Composable
fun DiscoveryToastAlert(
    alert: DiscoveryAlertData?,
    onDismiss: () -> Unit,
    onViewMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "discovery_alert_anim")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    AnimatedVisibility(
        visible = alert != null,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn() + scaleIn(initialScale = 0.88f),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(280)
        ) + fadeOut() + scaleOut(targetScale = 0.92f),
        modifier = modifier
    ) {
        if (alert != null) {
            val accent = alert.accentColor

            Surface(
                modifier = Modifier
                    .widthIn(min = 300.dp, max = 460.dp)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = accent)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = androidx.compose.material3.ripple(bounded = true, radius = 240.dp)
                    ) { onViewMap() }
                    .testTag("hud_discovery_toast_alert"),
                color = Color(0xF608171B),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    Brush.horizontalGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.4f),
                            accent,
                            CleanCyan,
                            accent.copy(alpha = 0.4f)
                        ),
                        startX = shimmerOffset * 500f,
                        endX = shimmerOffset * 500f + 300f
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 10.dp, top = 10.dp, bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Discovery Icon with Pulsing Halo Badge
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .scale(glowPulse)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(accent.copy(alpha = 0.35f), Color.Transparent)
                                    )
                                )
                                .border(1.dp, accent.copy(alpha = 0.8f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = alert.icon,
                                contentDescription = "Discovery",
                                tint = accent,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Middle Content: Category Tag, Title, Subtitle
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            // Category Tag
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = SunGold,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = alert.categoryLabel.uppercase(),
                                    color = SunGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            // Main Title: "Discovered: [Name]"
                            Text(
                                text = alert.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.3.sp,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            // Subtitle description & rewards
                            Text(
                                text = alert.subtitle,
                                color = CleanCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2,
                                lineHeight = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Right Action: Map Pill & Close Button
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(26.dp)
                                    .testTag("btn_dismiss_discovery_toast")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = Color(0xAAFFFFFF),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Surface(
                                color = Color(0x3300E676),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, SolarEmerald.copy(alpha = 0.8f))
                            ) {
                                Text(
                                    text = "TAP TO MAP",
                                    color = SolarEmerald,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
