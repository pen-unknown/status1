package com.example.ui.theme

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

private val DarkColorScheme =
    darkColorScheme(
        primary = DarkPrimary,
        onPrimary = Color.Black,
        primaryContainer = Color(0xFF00382C),
        onPrimaryContainer = Color(0xFF73F9D1),
        secondary = DarkSecondary,
        onSecondary = Color.Black,
        background = DarkBackground,
        surface = DarkSurface,
        surfaceVariant = DarkSurfaceVariant,
        onBackground = DarkOnSurface,
        onSurface = DarkOnSurface,
        onSurfaceVariant = DarkOnSurfaceVariant
    )

private val LightColorScheme =
    lightColorScheme(
        primary = LightPrimary,
        onPrimary = Color.White,
        primaryContainer = WhatsAppMint,
        onPrimaryContainer = LightPrimary,
        secondary = LightSecondary,
        onSecondary = Color.White,
        background = LightBackground,
        surface = LightSurface,
        surfaceVariant = LightSurfaceVariant,
        onBackground = LightOnSurface,
        onSurface = LightOnSurface,
        onSurfaceVariant = LightOnSurfaceVariant
    )

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our signature WhatsApp colors for brand consistency
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
