package com.example.scribbly.ui.theme

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.neumorphicSurface(
    shape: Shape = RoundedCornerShape(16.dp),
    cornerRadius: Dp = 16.dp,
    lightShadowColor: Color,
    darkShadowColor: Color,
    surfaceColor: Color,
    shadowElevation: Dp = 6.dp,
    lightSourceOffset: Dp = 4.dp,
    isPressed: Boolean = false
) = this.then(
    Modifier.drawBehind {
        val elevationPx = shadowElevation.toPx()
        val offsetPx = lightSourceOffset.toPx()
        val radiusPx = cornerRadius.toPx()

        drawIntoCanvas { canvas ->
            val paint = Paint()
            val frameworkPaint = paint.asFrameworkPaint()
            frameworkPaint.color = android.graphics.Color.TRANSPARENT

            if (!isPressed) {
                // Raised Effect
                // Draw dark shadow (bottom right)
                frameworkPaint.setShadowLayer(
                    elevationPx,
                    offsetPx,
                    offsetPx,
                    darkShadowColor.toArgb()
                )
                canvas.drawRoundRect(
                    0f, 0f, size.width, size.height,
                    radiusPx, radiusPx, paint
                )

                // Draw light shadow (top left)
                frameworkPaint.setShadowLayer(
                    elevationPx,
                    -offsetPx,
                    -offsetPx,
                    lightShadowColor.toArgb()
                )
                canvas.drawRoundRect(
                    0f, 0f, size.width, size.height,
                    radiusPx, radiusPx, paint
                )
            } else {
                // Pressed Effect (Inner shadow - simulated via reversed shadows with smaller offset/blur or clip)
                // True inner shadow is tricky in pure canvas without clipping.
                // For simplicity, a pressed state can just reduce elevation drastically or inverse.
                frameworkPaint.setShadowLayer(
                    elevationPx / 2f,
                    offsetPx / 2f,
                    offsetPx / 2f,
                    darkShadowColor.toArgb()
                )
                canvas.drawRoundRect(
                    0f, 0f, size.width, size.height,
                    radiusPx, radiusPx, paint
                )

                frameworkPaint.setShadowLayer(
                    elevationPx / 2f,
                    -offsetPx / 2f,
                    -offsetPx / 2f,
                    lightShadowColor.toArgb()
                )
                canvas.drawRoundRect(
                    0f, 0f, size.width, size.height,
                    radiusPx, radiusPx, paint
                )
            }
        }
    }
).background(color = surfaceColor, shape = shape)
