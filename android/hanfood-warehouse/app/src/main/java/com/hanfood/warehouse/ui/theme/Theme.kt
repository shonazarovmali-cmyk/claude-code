package com.hanfood.warehouse.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = BrandGreen,
    onPrimary = Color.White,
    primaryContainer = BrandGreenLight,
    onPrimaryContainer = BrandGreenDark,
    secondary = BrandGold,
    onSecondary = Color.White,
    secondaryContainer = BrandGoldLight,
    onSecondaryContainer = BrandInk,
    tertiary = BrandNavy,
    onTertiary = Color.White,
    background = AppBackground,
    onBackground = TextPrimaryLight,
    surface = AppSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = AppSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = BrandGreen.copy(alpha = 0.28f),
    error = DangerRed,
    errorContainer = DangerRed.copy(alpha = 0.13f),
    onErrorContainer = DangerRed
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7FCDA4),
    onPrimary = BrandGreenDark,
    primaryContainer = BrandGreenDark,
    onPrimaryContainer = Color(0xFFBFE9D3),
    secondary = BrandGoldLight,
    onSecondary = BrandInk,
    secondaryContainer = Color(0xFF4A3B1E),
    onSecondaryContainer = BrandGoldLight,
    tertiary = Color(0xFF6FA8CC),
    onTertiary = BrandInk,
    background = CharcoalBackground,
    onBackground = TextPrimaryDark,
    surface = CharcoalSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = CharcoalSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0xFF7FCDA4).copy(alpha = 0.3f),
    error = Color(0xFFE0836A),
    errorContainer = Color(0xFF4A2A20),
    onErrorContainer = Color(0xFFF3C6B8)
)

@Composable
fun HanFoodTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HanFoodTypography,
        content = content
    )
}
