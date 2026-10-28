package com.catherine.nursingprep.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    primaryContainer = SageSoft,
    onPrimaryContainer = SageDeep,
    secondary = Sage,
    onSecondary = Color.White,
    secondaryContainer = SageSoft,
    onSecondaryContainer = SageDeep,
    tertiary = Coral,
    onTertiary = Color.White,
    tertiaryContainer = CoralSoft,
    onTertiaryContainer = CoralDeep,
    background = Color.White,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Cream,
    onSurfaceVariant = MistGray,
    outline = BorderLight,
    error = Color(0xFFDC2626),
)

private val DarkColors = darkColorScheme(
    primary = SageGlow,
    onPrimary = Color(0xFF0B2E1D),
    primaryContainer = Color(0xFF1F3D2C),
    onPrimaryContainer = SageSoft,
    secondary = SageGlow,
    onSecondary = Color(0xFF0B2E1D),
    secondaryContainer = Color(0xFF1F3D2C),
    onSecondaryContainer = SageSoft,
    tertiary = Color(0xFFF0888F),
    onTertiary = Color(0xFF3D1116),
    tertiaryContainer = Color(0xFF4A1F24),
    onTertiaryContainer = Color(0xFFFFD9DC),
    background = NightBg,
    onBackground = NightText,
    surface = NightSurface,
    onSurface = NightText,
    surfaceVariant = NightSurfaceAlt,
    onSurfaceVariant = NightMuted,
    outline = NightBorder,
    error = Color(0xFFF87171),
)

@Composable
fun CathCareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
