package com.example.ui.game3d

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.repository.SaveLoadSystem
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SunGold

@Composable
fun SettingsMenuModal(
    masterVolume: Float,
    musicVolume: Float,
    sfxVolume: Float,
    vibrationEnabled: Boolean,
    hudOpacity: Float,
    immersiveMode: Boolean,
    onMasterVolumeChange: (Float) -> Unit,
    onMusicVolumeChange: (Float) -> Unit,
    onSfxVolumeChange: (Float) -> Unit,
    onVibrationToggle: (Boolean) -> Unit,
    onHudOpacityChange: (Float) -> Unit,
    onImmersiveModeToggle: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    onLoadClick: () -> Unit,
    onNewGameClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Audio, 1: Graphics, 2: Controls, 3: Gameplay, 4: Accessibility, 5: Language, 6: Save Slots

    // Audio & Settings State
    var voiceVol by remember { mutableStateOf(0.9f) }
    var subtitlesEnabled by remember { mutableStateOf(true) }
    var subtitleSize by remember { mutableStateOf("Medium") }

    // Graphics State
    var graphicsPreset by remember { mutableStateOf("High") }
    var targetFps by remember { mutableStateOf("60 FPS") }
    var fogEnabled by remember { mutableStateOf(true) }
    var godRaysEnabled by remember { mutableStateOf(true) }

    // Controls State
    var sensitivity by remember { mutableStateOf(0.5f) }
    var invertY by remember { mutableStateOf(false) }
    var joystickSize by remember { mutableStateOf("Normal") }

    // Gameplay State
    var showTutorials by remember { mutableStateOf(true) }
    var showTooltips by remember { mutableStateOf(true) }
    var timeSpeed by remember { mutableStateOf("1x Normal") }
    var difficulty by remember { mutableStateOf("Normal") }

    // Accessibility State
    var colorblindMode by remember { mutableStateOf("None") }
    var uiScale by remember { mutableStateOf("100%") }
    var highContrast by remember { mutableStateOf(false) }
    var reducedMotion by remember { mutableStateOf(false) }

    // Language State
    var selectedLanguage by remember { mutableStateOf("English") }

    // Save Slots State
    var slotsList by remember { mutableStateOf(SaveLoadSystem.getAllSlotsInfo(context)) }
    var slotConfirmAction by remember { mutableStateOf<String?>(null) } // "load_1", "delete_2", etc.

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, SolarEmerald.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                .testTag("settings_menu_modal"),
            color = Color(0xFD09181A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = SunGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SYSTEM & GAME SETTINGS",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x33FFFFFF))
                            .testTag("btn_close_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Selector Header (7 Tabs)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val tabs = listOf("Audio", "Graphics", "Controls", "Gameplay", "Accessibility", "Language", "Save Slots")
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedTab = index }
                                .testTag("btn_settings_tab_$index"),
                            color = if (isSelected) CleanCyan else Color(0x221E3A3A),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) SunGold else Color(0x3300E5FF)
                            )
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) Color(0xFF091215) else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Active Tab Content Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
                        0 -> AudioSettingsTab(
                            masterVol = masterVolume,
                            musicVol = musicVolume,
                            sfxVol = sfxVolume,
                            voiceVol = voiceVol,
                            vibration = vibrationEnabled,
                            subtitles = subtitlesEnabled,
                            subtitleSize = subtitleSize,
                            onMasterChange = onMasterVolumeChange,
                            onMusicChange = onMusicVolumeChange,
                            onSfxChange = onSfxVolumeChange,
                            onVoiceChange = { voiceVol = it },
                            onVibrationChange = onVibrationToggle,
                            onSubtitlesChange = { subtitlesEnabled = it },
                            onSubtitleSizeChange = { subtitleSize = it }
                        )
                        1 -> GraphicsSettingsTab(
                            preset = graphicsPreset,
                            targetFps = targetFps,
                            fog = fogEnabled,
                            godRays = godRaysEnabled,
                            hudOpacity = hudOpacity,
                            onPresetChange = { graphicsPreset = it },
                            onFpsChange = { targetFps = it },
                            onFogChange = { fogEnabled = it },
                            onGodRaysChange = { godRaysEnabled = it },
                            onHudOpacityChange = onHudOpacityChange
                        )
                        2 -> ControlsSettingsTab(
                            sensitivity = sensitivity,
                            invertY = invertY,
                            joystickSize = joystickSize,
                            onSensitivityChange = { sensitivity = it },
                            onInvertYChange = { invertY = it },
                            onJoystickSizeChange = { joystickSize = it }
                        )
                        3 -> GameplaySettingsTab(
                            showTutorials = showTutorials,
                            showTooltips = showTooltips,
                            timeSpeed = timeSpeed,
                            difficulty = difficulty,
                            immersiveMode = immersiveMode,
                            onTutorialsChange = { showTutorials = it },
                            onTooltipsChange = { showTooltips = it },
                            onTimeSpeedChange = { timeSpeed = it },
                            onDifficultyChange = { difficulty = it },
                            onImmersiveModeToggle = onImmersiveModeToggle
                        )
                        4 -> AccessibilitySettingsTab(
                            colorblind = colorblindMode,
                            uiScale = uiScale,
                            highContrast = highContrast,
                            reducedMotion = reducedMotion,
                            onColorblindChange = { colorblindMode = it },
                            onUiScaleChange = { uiScale = it },
                            onHighContrastChange = { highContrast = it },
                            onReducedMotionChange = { reducedMotion = it }
                        )
                        5 -> LanguageSettingsTab(
                            selectedLang = selectedLanguage,
                            onLangChange = { selectedLanguage = it }
                        )
                        else -> SaveSlotsTab(
                            slots = slotsList,
                            onSaveSlot = { slotId ->
                                onSaveClick()
                                slotsList = SaveLoadSystem.getAllSlotsInfo(context)
                            },
                            onLoadSlot = { slotId ->
                                onLoadClick()
                                onDismiss()
                            },
                            onDeleteSlot = { slotId ->
                                SaveLoadSystem.deleteSave(context, slotId)
                                slotsList = SaveLoadSystem.getAllSlotsInfo(context)
                            },
                            onNewGame = onNewGameClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioSettingsTab(
    masterVol: Float,
    musicVol: Float,
    sfxVol: Float,
    voiceVol: Float,
    vibration: Boolean,
    subtitles: Boolean,
    subtitleSize: String,
    onMasterChange: (Float) -> Unit,
    onMusicChange: (Float) -> Unit,
    onSfxChange: (Float) -> Unit,
    onVoiceChange: (Float) -> Unit,
    onVibrationChange: (Boolean) -> Unit,
    onSubtitlesChange: (Boolean) -> Unit,
    onSubtitleSizeChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF132A2D))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            VolumeSliderRow("Master Volume", masterVol, Icons.Default.VolumeUp, onMasterChange)
            Spacer(modifier = Modifier.height(8.dp))
            VolumeSliderRow("Music Volume", musicVol, Icons.Default.GraphicEq, onMusicChange)
            Spacer(modifier = Modifier.height(8.dp))
            VolumeSliderRow("SFX Volume", sfxVol, Icons.Default.VolumeUp, onSfxChange)
            Spacer(modifier = Modifier.height(8.dp))
            VolumeSliderRow("Voice Volume", voiceVol, Icons.Default.RecordVoiceOver, onVoiceChange)

            Spacer(modifier = Modifier.height(12.dp))

            ToggleRow("Haptic Vibration", vibration, Icons.Default.Vibration, onVibrationChange)
            Spacer(modifier = Modifier.height(6.dp))
            ToggleRow("Subtitles Display", subtitles, Icons.Default.Subtitles, onSubtitlesChange)

            if (subtitles) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Subtitle Size", color = Color.White, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Small", "Medium", "Large").forEach { sz ->
                            ChoiceChip(sz, subtitleSize == sz) { onSubtitleSizeChange(sz) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GraphicsSettingsTab(
    preset: String,
    targetFps: String,
    fog: Boolean,
    godRays: Boolean,
    hudOpacity: Float,
    onPresetChange: (String) -> Unit,
    onFpsChange: (String) -> Unit,
    onFogChange: (Boolean) -> Unit,
    onGodRaysChange: (Boolean) -> Unit,
    onHudOpacityChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF132A2D))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Graphics Quality Preset", color = SunGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Low", "Medium", "High", "Ultra", "Auto").forEach { p ->
                    Box(modifier = Modifier.weight(1f)) {
                        ChoiceChip(p, preset == p) { onPresetChange(p) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Target Frame Rate", color = SunGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("30 FPS", "45 FPS", "60 FPS").forEach { fps ->
                    Box(modifier = Modifier.weight(1f)) {
                        ChoiceChip(fps, targetFps == fps) { onFpsChange(fps) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("HUD Opacity (${(hudOpacity * 100).toInt()}%)", color = SunGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Slider(
                value = hudOpacity,
                onValueChange = onHudOpacityChange,
                valueRange = 0.3f..1f,
                colors = SliderDefaults.colors(thumbColor = SunGold, activeTrackColor = SolarEmerald)
            )

            Spacer(modifier = Modifier.height(8.dp))

            ToggleRow("Volumetric Fog (Forest)", fog, Icons.Default.Cloud, onFogChange)
            Spacer(modifier = Modifier.height(6.dp))
            ToggleRow("Sun God Rays & Reflections", godRays, Icons.Default.WbSunny, onGodRaysChange)
        }
    }
}

@Composable
private fun ControlsSettingsTab(
    sensitivity: Float,
    invertY: Boolean,
    joystickSize: String,
    onSensitivityChange: (Float) -> Unit,
    onInvertYChange: (Boolean) -> Unit,
    onJoystickSizeChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF132A2D))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            VolumeSliderRow("Camera Touch Sensitivity", sensitivity, Icons.Default.TouchApp, onSensitivityChange)
            Spacer(modifier = Modifier.height(12.dp))
            ToggleRow("Invert Y-Axis", invertY, Icons.Default.SwapVert, onInvertYChange)
            Spacer(modifier = Modifier.height(12.dp))

            Text("Virtual Joystick Size", color = SunGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Small", "Normal", "Large").forEach { s ->
                    Box(modifier = Modifier.weight(1f)) {
                        ChoiceChip(s, joystickSize == s) { onJoystickSizeChange(s) }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameplaySettingsTab(
    showTutorials: Boolean,
    showTooltips: Boolean,
    timeSpeed: String,
    difficulty: String,
    immersiveMode: Boolean,
    onTutorialsChange: (Boolean) -> Unit,
    onTooltipsChange: (Boolean) -> Unit,
    onTimeSpeedChange: (String) -> Unit,
    onDifficultyChange: (String) -> Unit,
    onImmersiveModeToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF132A2D))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            ToggleRow("Show Tutorial Hints", showTutorials, Icons.Default.Help, onTutorialsChange)
            Spacer(modifier = Modifier.height(6.dp))
            ToggleRow("Show Context Tooltips", showTooltips, Icons.Default.Info, onTooltipsChange)
            Spacer(modifier = Modifier.height(6.dp))
            ToggleRow("Immersive Mode (Hide All HUD)", immersiveMode, Icons.Default.Fullscreen, onImmersiveModeToggle)

            Spacer(modifier = Modifier.height(12.dp))

            Text("Game Difficulty", color = SunGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Peaceful", "Normal", "Eco Master").forEach { d ->
                    Box(modifier = Modifier.weight(1f)) {
                        ChoiceChip(d, difficulty == d) { onDifficultyChange(d) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Day/Night Time Progression", color = SunGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("1x Normal", "2x Fast", "4x Swift").forEach { spd ->
                    Box(modifier = Modifier.weight(1f)) {
                        ChoiceChip(spd, timeSpeed == spd) { onTimeSpeedChange(spd) }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccessibilitySettingsTab(
    colorblind: String,
    uiScale: String,
    highContrast: Boolean,
    reducedMotion: Boolean,
    onColorblindChange: (String) -> Unit,
    onUiScaleChange: (String) -> Unit,
    onHighContrastChange: (Boolean) -> Unit,
    onReducedMotionChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF132A2D))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Colorblind Mode Filter", color = SunGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("None", "Deuteranopia", "Protanopia", "Tritanopia").forEach { c ->
                    Box(modifier = Modifier.weight(1f)) {
                        ChoiceChip(c, colorblind == c) { onColorblindChange(c) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("UI Layout Scaling", color = SunGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("80%", "100%", "120%", "150%").forEach { s ->
                    Box(modifier = Modifier.weight(1f)) {
                        ChoiceChip(s, uiScale == s) { onUiScaleChange(s) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ToggleRow("High Contrast HUD Mode", highContrast, Icons.Default.Visibility, onHighContrastChange)
            Spacer(modifier = Modifier.height(6.dp))
            ToggleRow("Reduced Motion Animations", reducedMotion, Icons.Default.SlowMotionVideo, onReducedMotionChange)
        }
    }
}

@Composable
private fun LanguageSettingsTab(
    selectedLang: String,
    onLangChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF132A2D))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Select Preferred Language", color = SunGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            val languages = listOf(
                "English" to "Default language for UI, dialogue and lore.",
                "Hindi (हिंदी)" to "हिंदी संवाद और कार्य सूची पाठ।",
                "Hinglish" to "Authentic conversational English + Hindi blend."
            )

            languages.forEach { (lang, desc) ->
                val isSel = selectedLang == lang.split(" ")[0]
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onLangChange(lang.split(" ")[0]) },
                    color = if (isSel) SolarEmerald else Color(0x331E3A3A),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) SunGold else Color(0x33FFFFFF))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = lang,
                            color = if (isSel) Color(0xFF091215) else Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = desc,
                            color = if (isSel) Color(0xFF091215).copy(alpha = 0.8f) else Color(0xFFB0BEC5),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SaveSlotsTab(
    slots: List<SaveLoadSystem.SaveSlotInfo>,
    onSaveSlot: (Int) -> Unit,
    onLoadSlot: (Int) -> Unit,
    onDeleteSlot: (Int) -> Unit,
    onNewGame: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        slots.forEach { slot ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF132A2D)),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (slot.exists) CleanCyan else Color(0x33FFFFFF))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Save Slot ${slot.slotId}",
                            color = SunGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (slot.exists) {
                            Text(
                                text = "Day ${slot.day} • $${slot.coins} SolCoins",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Saved: ${slot.formattedDate}",
                                color = Color(0xFF90A4AE),
                                fontSize = 10.sp
                            )
                        } else {
                            Text(
                                text = "Empty Slot (Ready to save)",
                                color = Color(0xFF90A4AE),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { onSaveSlot(slot.slotId) },
                            colors = ButtonDefaults.buttonColors(containerColor = SolarEmerald),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Save", color = Color(0xFF091215), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        if (slot.exists) {
                            Button(
                                onClick = { onLoadSlot(slot.slotId) },
                                colors = ButtonDefaults.buttonColors(containerColor = CleanCyan),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Load", color = Color(0xFF091215), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            IconButton(
                                onClick = { onDeleteSlot(slot.slotId) },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VolumeSliderRow(
    title: String,
    value: Float,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = CleanCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Text(text = "${(value * 100).toInt()}%", color = CleanCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = CleanCyan,
                activeTrackColor = CleanCyan,
                inactiveTrackColor = Color(0xFF1E3C40)
            ),
            modifier = Modifier.height(28.dp)
        )
    }
}

@Composable
private fun ToggleRow(
    title: String,
    checked: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = SunGold, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF091215),
                checkedTrackColor = SolarEmerald
            )
        )
    }
}

@Composable
private fun ChoiceChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) SolarEmerald else Color(0x331E3A3A),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) SunGold else Color(0x22FFFFFF))
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isSelected) Color(0xFF091215) else Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
