package com.eventpro.admin.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EventProColorScheme = lightColorScheme(
    primary = Navy900,
    onPrimary = Color.White,
    primaryContainer = Navy800,
    onPrimaryContainer = Navy300,
    secondary = Slate600,
    onSecondary = Color.White,
    secondaryContainer = Slate200,
    onSecondaryContainer = Slate600,
    surface = SurfaceBase,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    inversePrimary = InversePrimary,
    outline = OutlineColor,
    outlineVariant = OutlineVariant,
    error = ErrorRed,
    errorContainer = ErrorContainer,
)

@Composable
fun EventProTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EventProColorScheme,
        typography = EventProTypography,
        shapes = EventProShapes,
        content = content
    )
}
