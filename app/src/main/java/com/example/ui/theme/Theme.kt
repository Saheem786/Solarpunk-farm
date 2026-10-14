package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MedievalCrimson,
    onPrimary = Parchment,
    primaryContainer = MedievalCrimsonDark,
    onPrimaryContainer = MedievalTextLight,
    secondary = AgedGold,
    onSecondary = MedievalDarkBg,
    secondaryContainer = AgedGoldDark,
    onSecondaryContainer = MedievalTextLight,
    tertiary = ForestMoss,
    onTertiary = Parchment,
    background = MedievalDarkBg,
    onBackground = MedievalTextLight,
    surface = MedievalDarkSurface,
    onSurface = MedievalTextLight,
    surfaceVariant = MedievalDarkSurfaceVariant,
    onSurfaceVariant = MedievalTextLight.copy(alpha = 0.7f),
    outline = AgedGold.copy(alpha = 0.5f)
)

private val LightColorScheme = lightColorScheme(
    primary = MedievalCrimsonDark,
    onPrimary = Parchment,
    primaryContainer = MedievalCrimsonLight,
    onPrimaryContainer = MedievalTextPrimary,
    secondary = AgedGoldDark,
    onSecondary = Parchment,
    secondaryContainer = AgedGold,
    onSecondaryContainer = MedievalTextPrimary,
    tertiary = ForestMoss,
    onTertiary = Parchment,
    background = MedievalLightBg,
    onBackground = MedievalTextPrimary,
    surface = MedievalLightSurface,
    onSurface = MedievalTextPrimary,
    surfaceVariant = MedievalLightSurfaceVariant,
    onSurfaceVariant = MedievalTextSecondary,
    outline = AgedGoldDark.copy(alpha = 0.4f)
)

@Composable
fun MedievalIslandTheme(
    darkTheme: Boolean = true, // Medieval theme looks best in dark mode for wood/stone textures
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
