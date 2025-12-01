package com.example.can301_cw.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = GreyPrimary,
    secondary = GreySecondary,
    tertiary = GreyTertiary,
    secondaryContainer = DarkContainer
)

private val BlueColorScheme = lightColorScheme(
    primary = BluePrimary,
    secondary = BlueSecondary,
    tertiary = BlueTertiary,
    background = BlueBackground,
    surface = BlueSurface,
    surfaceContainer = BlueSurface, // For TabBar/Navigation
    surfaceVariant = BlueSecondary.copy(alpha = 0.3f), // For inactive states
    secondaryContainer = BlueContainer
)

private val YellowColorScheme = lightColorScheme(
    primary = YellowPrimary,
    secondary = YellowSecondary,
    tertiary = YellowTertiary,
    background = YellowBackground,
    surface = YellowSurface,
    surfaceContainer = YellowSurface,
    surfaceVariant = YellowSecondary.copy(alpha = 0.3f),
    secondaryContainer = YellowContainer
)

private val GreenColorScheme = lightColorScheme(
    primary = GreenPrimary,
    secondary = GreenSecondary,
    tertiary = GreenTertiary,
    background = GreenBackground,
    surface = GreenSurface,
    surfaceContainer = GreenSurface,
    surfaceVariant = GreenSecondary.copy(alpha = 0.3f),
    secondaryContainer = GreenContainer
)

enum class AppTheme {
    Blue, Yellow, Green
}

@Composable
fun CAN301_CWTheme(
    appTheme: AppTheme = AppTheme.Blue,
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> when(appTheme) {
            AppTheme.Blue -> BlueColorScheme
            AppTheme.Yellow -> YellowColorScheme
            AppTheme.Green -> GreenColorScheme
        }
    }

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