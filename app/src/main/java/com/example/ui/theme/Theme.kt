package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LightEmerald,
    onPrimary = Color.Black,
    primaryContainer = EmeraldGreen,
    onPrimaryContainer = Color.White,
    secondary = BlueSecondary,
    onSecondary = Color.White,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = Color.White,
    onSurface = Color.White,
    error = RedError
)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = VesBlueLight,
    onPrimaryContainer = BluePrimary,
    secondary = EmeraldGreen,
    onSecondary = Color.White,
    secondaryContainer = UsdGreenLight,
    onSecondaryContainer = UsdGreen,
    tertiary = AmberWarning,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = RedError
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
