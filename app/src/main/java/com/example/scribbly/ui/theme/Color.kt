package com.example.scribbly.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LightBackground = Color(0xFFEEF1F5)
val LightText = Color(0xFF1E2125)
val LightShadowLight = Color(0xFFFFFFFF)
val LightShadowDark = Color(0xFFD3D9E2)

val DarkBackground = Color(0xFF232529)
val DarkText = Color(0xFFE5E7EB)
val DarkShadowLight = Color(0xFF2C2F34)
val DarkShadowDark = Color(0xFF1A1C1F)

@Immutable
data class NeumorphicColors(
    val background: Color,
    val text: Color,
    val shadowLight: Color,
    val shadowDark: Color,
    val isLight: Boolean
)

val LocalNeumorphicColors = staticCompositionLocalOf {
    // Default fallback
    NeumorphicColors(
        background = LightBackground,
        text = LightText,
        shadowLight = LightShadowLight,
        shadowDark = LightShadowDark,
        isLight = true
    )
}