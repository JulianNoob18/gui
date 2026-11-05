package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GuiDarkColorScheme = darkColorScheme(
    primary = GuiPrimaryCyan,
    onPrimary = GuiBackground,
    primaryContainer = GuiFaceplateSecondary,
    onPrimaryContainer = GuiBrightHighlight,
    secondary = GuiBrightHighlight,
    onSecondary = GuiBackground,
    background = GuiBackground,
    onBackground = GuiOnSurface,
    surface = GuiSurfaceDark,
    onSurface = GuiOnSurface,
    surfaceVariant = GuiSurfaceVariant,
    onSurfaceVariant = GuiOnSurfaceMuted,
    error = GuiRed,
    onError = GuiBackground
)

@Composable
fun GuiTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GuiDarkColorScheme,
        typography = Typography,
        content = content
    )
}
