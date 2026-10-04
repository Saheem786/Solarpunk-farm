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
    primary = SolarEmerald,
    onPrimary = SolarpunkDarkBg,
    primaryContainer = SolarEmeraldDark,
    onPrimaryContainer = TextWhitePrimary,
    secondary = SunGold,
    onSecondary = SolarpunkDarkBg,
    secondaryContainer = SunGoldDark,
    onSecondaryContainer = TextWhitePrimary,
    tertiary = CleanCyan,
    onTertiary = SolarpunkDarkBg,
    background = SolarpunkDarkBg,
    onBackground = TextWhitePrimary,
    surface = SolarpunkDarkSurface,
    onSurface = TextWhitePrimary,
    surfaceVariant = SolarpunkDarkSurfaceVariant,
    onSurfaceVariant = TextWhiteSecondary,
    outline = SolarEmerald.copy(alpha = 0.35f)
)

private val LightColorScheme = lightColorScheme(
    primary = SolarEmeraldDark,
    onPrimary = TextWhitePrimary,
    primaryContainer = SolarEmeraldLight,
    onPrimaryContainer = TextDarkPrimary,
    secondary = SunGoldDark,
    onSecondary = TextWhitePrimary,
    secondaryContainer = SunGoldLight,
    onSecondaryContainer = TextDarkPrimary,
    tertiary = CleanCyanDark,
    onTertiary = TextWhitePrimary,
    background = SolarpunkLightBg,
    onBackground = TextDarkPrimary,
    surface = SolarpunkLightSurface,
    onSurface = TextDarkPrimary,
    surfaceVariant = SolarpunkLightSurfaceVariant,
    onSurfaceVariant = TextDarkSecondary,
    outline = SolarEmeraldDark.copy(alpha = 0.25f)
)

@Composable
fun SolarpunkFarmTheme(
    darkTheme: Boolean = true, // Solarpunk aesthetic shines best in luminous dark theme
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
