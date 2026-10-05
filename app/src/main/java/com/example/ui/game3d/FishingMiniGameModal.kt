package com.example.ui.game3d

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.PhonelinkRing
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FishingRodTier
import com.example.ui.ActiveFishingSession
import com.example.ui.FishingStage
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SunGold
import kotlinx.coroutines.delay

@Composable
fun FishingMiniGameModal(
    session: ActiveFishingSession?,
    rodTier: FishingRodTier,
    onHookFish: () -> Unit,
    onReelTick: (isPressing: Boolean) -> Unit,
    onRetry: () -> Unit,
    onClose: () -> Unit
) {
    if (session == null) return

    var isReelPressed by remember { mutableStateOf(false) }

    // Reeling loop ticker during REELING stage
    LaunchedEffect(session.stage, isReelPressed) {
        if (session.stage == FishingStage.REELING) {
            while (true) {
                delay(60)
                onReelTick(isReelPressed)
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "fishing_pulse")
    val bobberOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bobber_bounce"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC091215))
            .clickable(enabled = false) {}
            .testTag("modal_fishing_minigame"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(min = 360.dp, max = 500.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF70C1E22)),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                Brush.horizontalGradient(listOf(CleanCyan, SolarEmerald))
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header: Spot Name, Rod Tier, Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "🎣", fontSize = 18.sp)
                            Text(
                                text = session.spot.displayName,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Rod: ${rodTier.displayName} • ${session.spot.biome.displayName}",
                            color = CleanCyan,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("btn_close_fishing")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Stage Display
                when (session.stage) {
                    FishingStage.CASTING -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "🎣", fontSize = 42.sp)
                            Text(
                                text = "Casting Line...",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Luminescent solar line flying into the water",
                                color = Color(0xFFB0BEC5),
                                fontSize = 12.sp
                            )
                        }
                    }

                    FishingStage.WAITING -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x3300E5FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🔴",
                                    fontSize = 24.sp,
                                    modifier = Modifier.padding(top = bobberOffset.dp)
                                )
                            }
                            Text(
                                text = "Waiting for a bite...",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Keep your eyes on the bobber! (Wait: ${rodTier.minWaitSec.toInt()}-${rodTier.maxWaitSec.toInt()}s)",
                                color = Color(0xFF90A4AE),
                                fontSize = 11.sp
                            )
                        }
                    }

                    FishingStage.BITE -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "⚠️ FISH ON THE HOOK!",
                                color = Color(0xFFFFD54F),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "TAP HOOK FAST!",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Button(
                                onClick = onHookFish,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("btn_hook_fish")
                            ) {
                                Text(
                                    text = "🎣 STRIKE & HOOK!",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    FishingStage.REELING -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "REELING: KEEP IN THE GREEN!",
                                color = CleanCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Catch Progress Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Catch Progress", color = Color(0xFFB0BEC5), fontSize = 11.sp)
                                Text(text = "${(session.progress * 100).toInt()}%", color = SunGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF1E282D))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(session.progress)
                                        .height(12.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Brush.horizontalGradient(listOf(SunGold, SolarEmerald)))
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Tension Bar with Sweet Spot Zone
                            Text(text = "Line Tension", color = Color(0xFFB0BEC5), fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1A262C))
                                    .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(12.dp))
                            ) {
                                // Sweet spot green zone
                                val sweetStart = (session.sweetSpotCenter - 0.20f).coerceIn(0f, 1f)
                                val sweetWidth = 0.40f.coerceAtMost(1f - sweetStart)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(sweetStart + sweetWidth)
                                        .height(24.dp)
                                        .padding(start = (sweetStart * 300).dp) // approximate visual
                                        .background(Color(0x6600E676))
                                )

                                // Current tension indicator cursor
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(session.tension.coerceIn(0.05f, 0.98f))
                                        .height(24.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .border(1.5.dp, Color(0xFF091215), CircleShape)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Interactive Reel Button (Hold/Tap to pull tension up)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                isReelPressed = true
                                                tryAwaitRelease()
                                                isReelPressed = false
                                            }
                                        )
                                    }
                                    .testTag("btn_reel_interactive"),
                                color = if (isReelPressed) CleanCyan else Color(0x3300E5FF),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(2.dp, CleanCyan)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (isReelPressed) "REELING (PULLING)..." else "HOLD TO REEL LINE",
                                        color = if (isReelPressed) Color(0xFF091215) else Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    FishingStage.SUCCESS -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "🎉", fontSize = 42.sp)
                            Text(
                                text = "Catch Success!",
                                color = SunGold,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            val fish = session.caughtFish
                            if (fish != null) {
                                Text(
                                    text = fish.displayName,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Rarity: ${fish.rarity} • Sell: $${fish.sellPrice} 🪙",
                                    color = CleanCyan,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Restores: +${fish.hungerRestoreCooked.toInt()} Hunger when cooked at campfire",
                                    color = Color(0xFFB0BEC5),
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = session.message,
                                color = SolarEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onRetry,
                                    colors = ButtonDefaults.buttonColors(containerColor = CleanCyan),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_fish_again")
                                ) {
                                    Text(text = "Cast Again", color = Color(0xFF091215), fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = onClose,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_done_fishing")
                                ) {
                                    Text(text = "Done", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    FishingStage.ESCAPED -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "💨", fontSize = 36.sp)
                            Text(
                                text = "The Fish Escaped!",
                                color = Color(0xFFFF8A80),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = session.message,
                                color = Color(0xFFB0BEC5),
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = onRetry,
                                colors = ButtonDefaults.buttonColors(containerColor = CleanCyan),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_try_fishing_again")
                            ) {
                                Text(text = "Try Again", color = Color(0xFF091215), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
