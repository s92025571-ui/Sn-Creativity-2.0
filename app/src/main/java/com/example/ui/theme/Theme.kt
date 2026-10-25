package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

enum class AppThemeMode(val storageKey: String, val displayName: String) {
    NEON_DARK("neon_dark", "Neon Dark"),
    CREATIVE_LIGHT("creative_light", "Creative Light");

    companion object {
        fun fromKey(key: String?): AppThemeMode =
            entries.firstOrNull { it.storageKey == key } ?: NEON_DARK
    }
}

data class ExtendedThemeTokens(
    val isNeonDark: Boolean,
    val glowPrimary: Color,
    val glowSecondary: Color,
    val cardBorder: Color,
    val subtleGridLine: Color,
    val statusConnected: Color,
    val statusWarning: Color
)

val LocalExtendedTheme = staticCompositionLocalOf {
    ExtendedThemeTokens(
        isNeonDark = true,
        glowPrimary = NeonPurplePrimary,
        glowSecondary = CyberMagentaTertiary,
        cardBorder = NeonDarkOutlineVariant,
        subtleGridLine = Color(0x24B84DFF),
        statusConnected = StatusConnectedGreen,
        statusWarning = StatusWarningAmber
    )
}

private val NeonDarkColorScheme = darkColorScheme(
    primary = NeonPurplePrimary,
    onPrimary = NeonPurpleOnPrimary,
    primaryContainer = NeonPurpleContainer,
    onPrimaryContainer = NeonPurpleOnContainer,
    secondary = ElectricVioletSecondary,
    onSecondary = ElectricVioletOnSecondary,
    secondaryContainer = ElectricVioletContainer,
    onSecondaryContainer = ElectricVioletOnContainer,
    tertiary = CyberMagentaTertiary,
    onTertiary = CyberMagentaOnTertiary,
    tertiaryContainer = CyberMagentaContainer,
    onTertiaryContainer = CyberMagentaOnContainer,
    background = NeonDarkBackground,
    onBackground = NeonDarkOnBackground,
    surface = NeonDarkSurface,
    onSurface = NeonDarkOnSurface,
    surfaceVariant = NeonDarkSurfaceVariant,
    onSurfaceVariant = NeonDarkOnSurfaceVariant,
    surfaceContainer = NeonDarkSurfaceContainer,
    surfaceContainerHigh = NeonDarkSurfaceHigh,
    outline = NeonDarkOutline,
    outlineVariant = NeonDarkOutlineVariant,
    error = StatusErrorRed
)

private val CreativeLightColorScheme = lightColorScheme(
    primary = CreativePurplePrimary,
    onPrimary = CreativePurpleOnPrimary,
    primaryContainer = CreativePurpleContainer,
    onPrimaryContainer = CreativePurpleOnContainer,
    secondary = CreativeVioletSecondary,
    onSecondary = CreativeVioletOnSecondary,
    secondaryContainer = CreativeVioletContainer,
    onSecondaryContainer = CreativeVioletOnContainer,
    tertiary = CreativeOrchidTertiary,
    onTertiary = CreativeOrchidOnTertiary,
    tertiaryContainer = CreativeOrchidContainer,
    onTertiaryContainer = CreativeOrchidOnContainer,
    background = CreativeLightBackground,
    onBackground = CreativeLightOnBackground,
    surface = CreativeLightSurface,
    onSurface = CreativeLightOnSurface,
    surfaceVariant = CreativeLightSurfaceVariant,
    onSurfaceVariant = CreativeLightOnSurfaceVariant,
    surfaceContainer = CreativeLightSurfaceContainer,
    surfaceContainerHigh = CreativeLightSurfaceHigh,
    outline = CreativeLightOutline,
    outlineVariant = CreativeLightOutlineVariant,
    error = Color(0xFFB3261E)
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun SNCreativityTheme(
    themeMode: AppThemeMode = AppThemeMode.NEON_DARK,
    content: @Composable () -> Unit
) {
    val isDark = themeMode == AppThemeMode.NEON_DARK
    val colorScheme = if (isDark) NeonDarkColorScheme else CreativeLightColorScheme
    val extendedTokens = if (isDark) {
        ExtendedThemeTokens(
            isNeonDark = true,
            glowPrimary = NeonPurplePrimary,
            glowSecondary = CyberMagentaTertiary,
            cardBorder = NeonPurplePrimary.copy(alpha = 0.34f),
            subtleGridLine = NeonPurplePrimary.copy(alpha = 0.08f),
            statusConnected = StatusConnectedGreen,
            statusWarning = StatusWarningAmber
        )
    } else {
        ExtendedThemeTokens(
            isNeonDark = false,
            glowPrimary = CreativePurplePrimary,
            glowSecondary = CreativeOrchidTertiary,
            cardBorder = CreativePurplePrimary.copy(alpha = 0.22f),
            subtleGridLine = CreativePurplePrimary.copy(alpha = 0.05f),
            statusConnected = StatusConnectedGreenLight,
            statusWarning = StatusWarningAmberLight
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !isDark
                controller.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(LocalExtendedTheme provides extendedTokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}
