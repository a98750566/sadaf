package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SadafColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = ObsidianBg,
    primaryContainer = KeypadOpBg,
    onPrimaryContainer = NeonCyan,
    secondary = NeonRose,
    onSecondary = TextWhite,
    secondaryContainer = KeypadFnBg,
    onSecondaryContainer = NeonRose,
    tertiary = NeonEmerald,
    onTertiary = ObsidianBg,
    background = ObsidianBg,
    onBackground = TextWhite,
    surface = SurfaceDark,
    onSurface = TextWhite,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextMuted,
    outline = BorderSubtle,
    error = NeonOrange
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Professional Engineering Dark Theme for sadaf 💔
    MaterialTheme(
        colorScheme = SadafColorScheme,
        typography = Typography,
        content = content
    )
}
