package com.example.can301_cw.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
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

private val SkyBlueColorScheme = lightColorScheme(
    primary = SkyBluePrimary,
    secondary = SkyBlueSecondary,
    tertiary = SkyBlueTertiary,
    background = SkyBlueBackground,
    surface = SkyBlueSurface,
    surfaceContainer = SkyBlueSurface,
    surfaceVariant = SkyBlueSecondary.copy(alpha = 0.3f),
    secondaryContainer = SkyBlueContainer
)

private val CherryBlossomColorScheme = lightColorScheme(
    primary = CherryBlossomPrimary,
    secondary = CherryBlossomSecondary,
    tertiary = CherryBlossomTertiary,
    background = CherryBlossomBackground,
    surface = CherryBlossomSurface,
    surfaceContainer = CherryBlossomSurface,
    surfaceVariant = CherryBlossomSecondary.copy(alpha = 0.3f),
    secondaryContainer = CherryBlossomContainer
)

private val StoneropGreenColorScheme = lightColorScheme(
    primary = StoneropGreenPrimary,
    secondary = StoneropGreenSecondary,
    tertiary = StoneropGreenTertiary,
    background = StoneropGreenBackground,
    surface = StoneropGreenSurface,
    surfaceContainer = StoneropGreenSurface,
    surfaceVariant = StoneropGreenSecondary.copy(alpha = 0.3f),
    secondaryContainer = StoneropGreenContainer
)

enum class AppTheme {
    Blue, Yellow, Green, SkyBlue, CherryBlossom, StoneropGreen, Custom
}

private fun generateCustomColorScheme(primaryColor: Color): ColorScheme {
    // Convert Compose Color to HSV for intelligent color manipulation
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(primaryColor.toArgb(), hsv)

    val hue = hsv[0]        // 0-360
    val saturation = hsv[1] // 0-1
    val brightness = hsv[2] // 0-1

    // Generate secondary color at 60 degrees offset (complementary triad)
    val secondaryColor = Color.hsv(
        hue = (hue + 60) % 360,
        saturation = saturation * 0.9f,
        value = brightness * 0.95f
    )

    // Generate tertiary color at 120 degrees offset (split complementary)
    val tertiaryColor = Color.hsv(
        hue = (hue + 120) % 360,
        saturation = saturation * 0.85f,
        value = brightness * 0.9f
    )

    // Background color
    val backgroundColor = Color.hsv(
        hue = hue,
        saturation = saturation * 0.1f,
        value = 0.995f
    )

    // Surface color
    val surfaceColor = Color.hsv(
        hue = hue,
        saturation = saturation * 0.11f,
        value = 0.992f
    )

    // Container color
    val containerColor = Color.hsv(
        hue = hue,
        saturation = saturation * 0.4f,
        value = 0.92f
    )

    return lightColorScheme(
        primary = primaryColor,
        secondary = secondaryColor,
        tertiary = tertiaryColor,
        background = backgroundColor,
        surface = surfaceColor,
        surfaceContainer = surfaceColor,
        surfaceVariant = secondaryColor.copy(alpha = 0.3f),
        secondaryContainer = containerColor
    )
}

@Composable
fun CAN301_CWTheme(
    appTheme: AppTheme = AppTheme.Blue,
    customPrimaryColor: Color? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        appTheme == AppTheme.Custom && customPrimaryColor != null -> {
            generateCustomColorScheme(customPrimaryColor)
        }
        appTheme == AppTheme.Blue -> BlueColorScheme
        appTheme == AppTheme.Yellow -> YellowColorScheme
        appTheme == AppTheme.Green -> GreenColorScheme
        appTheme == AppTheme.SkyBlue -> SkyBlueColorScheme
        appTheme == AppTheme.CherryBlossom -> CherryBlossomColorScheme
        appTheme == AppTheme.StoneropGreen -> StoneropGreenColorScheme
        else -> BlueColorScheme  // Fallback to Blue
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