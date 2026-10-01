package com.langoa.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme

private val LangoaDarkColorScheme = darkColorScheme(
    primary = LangoaGreen,
    onPrimary = LangoaOnPrimary,
    primaryContainer = LangoaGreenDark,
    onPrimaryContainer = LangoaGreenLight,
    secondary = LangoaAmber,
    onSecondary = LangoaOnSecondary,
    secondaryContainer = LangoaAmberDark,
    onSecondaryContainer = LangoaAmberLight,
    tertiary = LangoaBlue,
    onTertiary = LangoaOnPrimary,
    tertiaryContainer = LangoaBlueDark,
    onTertiaryContainer = LangoaBlueLight,
    background = LangoaBackground,
    onBackground = LangoaOnBackground,
    surface = LangoaSurface,
    onSurface = LangoaOnSurface,
    surfaceVariant = LangoaSurfaceVariant,
    onSurfaceVariant = LangoaOnSurface,
    error = LangoaError,
    onError = LangoaOnError
)

private val LangoaLightColorScheme = lightColorScheme(
    primary = LangoaGreen,
    onPrimary = LangoaOnPrimary,
    primaryContainer = LangoaGreenLight,
    onPrimaryContainer = LangoaGreenDark,
    secondary = LangoaAmber,
    onSecondary = LangoaOnSecondary,
    secondaryContainer = LangoaAmberLight,
    onSecondaryContainer = LangoaAmberDark,
    tertiary = LangoaBlue,
    onTertiary = LangoaOnPrimary,
    tertiaryContainer = LangoaBlueLight,
    onTertiaryContainer = LangoaBlueDark,
    background = LangoaBackground,
    onBackground = LangoaOnBackground,
    surface = LangoaSurface,
    onSurface = LangoaOnSurface,
    surfaceVariant = LangoaSurfaceVariant,
    onSurfaceVariant = LangoaOnSurface,
    error = LangoaError,
    onError = LangoaOnError
)

@Composable
fun LangoaTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) LangoaDarkColorScheme else LangoaLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LangoaTypography,
        content = content
    )
}
