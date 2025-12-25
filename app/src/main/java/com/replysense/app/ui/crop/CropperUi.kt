package com.replysense.app.ui.crop

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

@Composable
fun PremiumCropOverlay(
    modifier: Modifier = Modifier,
    rect: Rect,
    onRectChange: (Rect) -> Unit,
    showGrid: Boolean = true
) {
    val haptics = LocalHapticFeedback.current
    var dragging by remember { mutableStateOf(false) }

    val borderAlpha by animateFloatAsState(
        targetValue = if (dragging) 1f else 0.85f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "borderAlpha"
    )

    Box(modifier = modifier.fillMaxSize()) {

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            dragging = true
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        onDragEnd = { dragging = false },
                        onDragCancel = { dragging = false },
                        onDrag = { change, drag ->
                            change.consume()

                            val dx = drag.x / size.width
                            val dy = drag.y / size.height

                            val w = rect.width
                            val h = rect.height

                            val left = (rect.left + dx).coerceIn(0f, 1f - w)
                            val top = (rect.top + dy).coerceIn(0f, 1f - h)

                            onRectChange(
                                Rect(
                                    left,
                                    top,
                                    left + w,
                                    top + h
                                )
                            )
                        }
                    )
                }
        ) {
            val crop = Rect(
                rect.left * size.width,
                rect.top * size.height,
                rect.right * size.width,
                rect.bottom * size.height
            )

            // Dim outside
            drawRect(Color.Black.copy(alpha = 0.55f))

            // Punch hole
            drawRect(
                color = Color.Transparent,
                topLeft = Offset(crop.left, crop.top),
                size = Size(crop.width, crop.height),
                blendMode = BlendMode.Clear
            )

            // Border
            drawRoundRect(
                color = Color.White.copy(alpha = borderAlpha),
                topLeft = Offset(crop.left, crop.top),
                size = Size(crop.width, crop.height),
                cornerRadius = CornerRadius(18f, 18f),
                style = Stroke(width = 3f)
            )

            // Grid
            if (showGrid) {
                val thirdW = crop.width / 3f
                val thirdH = crop.height / 3f
                val gridColor = Color.White.copy(alpha = 0.22f)

                for (i in 1..2) {
                    drawLine(
                        gridColor,
                        Offset(crop.left + thirdW * i, crop.top),
                        Offset(crop.left + thirdW * i, crop.bottom),
                        strokeWidth = 2f
                    )
                    drawLine(
                        gridColor,
                        Offset(crop.left, crop.top + thirdH * i),
                        Offset(crop.right, crop.top + thirdH * i),
                        strokeWidth = 2f
                    )
                }
            }

            // Corner handles
            val handle = 22f
            val stroke = 6f

            fun corner(x: Float, y: Float, dx: Float, dy: Float) {
                drawLine(Color.White, Offset(x, y), Offset(x + dx * handle, y), stroke)
                drawLine(Color.White, Offset(x, y), Offset(x, y + dy * handle), stroke)
            }

            corner(crop.left, crop.top, 1f, 1f)
            corner(crop.right, crop.top, -1f, 1f)
            corner(crop.left, crop.bottom, 1f, -1f)
            corner(crop.right, crop.bottom, -1f, -1f)
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 14.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            tonalElevation = 2.dp
        ) {
            Text(
                "Drag to move",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
