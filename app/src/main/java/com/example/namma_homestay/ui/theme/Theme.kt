package com.example.namma_homestay.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = LeafGreen,
    secondary = Turmeric,
    tertiary = Clay,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = Color(0xFF263029),
    primaryContainer = Color(0xFF375B47),
    secondaryContainer = Color(0xFF594820),
    errorContainer = Color(0xFF6B2D27),
    onPrimary = Color(0xFF07150D),
    onSecondary = Color(0xFF1D1504),
    onTertiary = Color.White,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    secondary = Clay,
    tertiary = Turmeric,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceWarm,
    primaryContainer = Color(0xFFDDEBDD),
    secondaryContainer = Color(0xFFFFE2D2),
    errorContainer = Color(0xFFFFDAD4),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color(0xFF3F2E00),
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight
)

@Composable
fun NammaHomestayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Enforce brand color
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
