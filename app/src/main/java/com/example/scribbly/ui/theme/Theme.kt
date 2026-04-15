package com.example.scribbly.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = LightBackground, // Custom override for simpler UI
    secondary = DarkBackground,
    tertiary = DarkText,
    background = DarkBackground,
    surface = DarkBackground,
    onBackground = DarkText,
    onSurface = DarkText
)

private val LightColorScheme = lightColorScheme(
    primary = DarkBackground,
    secondary = LightBackground,
    tertiary = LightText,
    background = LightBackground,
    surface = LightBackground,
    onBackground = LightText,
    onSurface = LightText
)

@Composable
fun ScribblyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val neumorphicColors = if (darkTheme) {
        NeumorphicColors(
            background = DarkBackground,
            text = DarkText,
            shadowLight = DarkShadowLight,
            shadowDark = DarkShadowDark,
            isLight = false
        )
    } else {
        NeumorphicColors(
            background = LightBackground,
            text = LightText,
            shadowLight = LightShadowLight,
            shadowDark = LightShadowDark,
            isLight = true
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalNeumorphicColors provides neumorphicColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}