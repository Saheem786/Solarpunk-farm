package com.example.ui.game3d

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SunGold

data class TutorialStep(
    val stepIndex: Int,
    val title: String,
    val description: String,
    val actionHint: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun TutorialOverlay(
    currentStep: Int,
    onNextStep: () -> Unit,
    onSkipTutorial: () -> Unit
) {
    val tutorialSteps = remember {
        listOf(
            TutorialStep(
                1,
                "STEP 1: MOVEMENT & EXPLORATION",
                "Use the bottom-left analog joystick to walk around your farm. Touch and drag anywhere on the right side of the screen to rotate camera angle.",
                "Try moving your character around the field!",
                Icons.Default.OpenWith
            ),
            TutorialStep(
                2,
                "STEP 2: RESOURCE GATHERING",
                "Walk near wild crops, trees, or stone outcrops. When prompt appears at the bottom center, tap Harvest, Chop, or Mine.",
                "Gather raw materials to craft & build!",
                Icons.Default.Grass
            ),
            TutorialStep(
                3,
                "STEP 3: FARMING & WATERING",
                "Approach tilled soil plots. Tap 'Plant Seeds' to sow wheat or corn. Keep soil watered using your clean water flask or irrigation pipes.",
                "Water crops daily for maximum yield!",
                Icons.Default.WaterDrop
            ),
            TutorialStep(
                4,
                "STEP 4: CLEAN ENERGY GRID",
                "Open Build Mode or Energy Tab to place solar panels and battery storage. Connect buildings to power irrigation, lights, and automated machines.",
                "Build solar panels to power your farm!",
                Icons.Default.SolarPower
            ),
            TutorialStep(
                5,
                "STEP 5: SETTLEMENT & MISSIONS",
                "Open Research tab to unlock new technology. Check Journal tab for active story missions and discover 15 points of interest across biomes.",
                "Recruit NPCs and rebuild civilization!",
                Icons.Default.AutoAwesome
            )
        )
    }

    val step = tutorialSteps.getOrNull((currentStep - 1).coerceIn(0, 4)) ?: return

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(top = 40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(2.dp, SunGold, RoundedCornerShape(20.dp))
                    .testTag("tutorial_overlay_card"),
                color = Color(0xF209181A)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Brush.radialGradient(listOf(SunGold, SolarEmerald))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = step.icon,
                                    contentDescription = "Tutorial Step",
                                    tint = Color(0xFF091215),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = step.title,
                                color = SunGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Skip Tutorial Button
                        TextButton(
                            onClick = onSkipTutorial,
                            modifier = Modifier.testTag("btn_skip_tutorial")
                        ) {
                            Text(
                                text = "Skip Tutorial",
                                color = Color(0xFFFF8A80),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = step.description,
                        color = Color.White,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x3300E5FF),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CleanCyan)
                    ) {
                        Text(
                            text = "💡 ${step.actionHint}",
                            color = CleanCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Step ${step.stepIndex} of 5",
                            color = Color(0xFF90A4AE),
                            fontSize = 11.sp
                        )

                        Button(
                            onClick = onNextStep,
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("btn_tutorial_next"),
                            colors = ButtonDefaults.buttonColors(containerColor = SolarEmerald),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (step.stepIndex == 5) "Got It! Start Game" else "Next Step",
                                color = Color(0xFF091215),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
