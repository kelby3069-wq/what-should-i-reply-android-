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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

/**
 * Visual-only crop overlay:
 * - Dims outside crop rect
 * - Border + grid + corner handles
 * - Drag to move crop rect (no resize yet)
 *
 * rect is normalized [0..1] in both axes.
 */
@Composable
fun PremiumCropOverlay(
    modifier: Modifier = Modifier,
    rect: Rect,
    onRectChange: (Rect) -> Unit,
    showGrid: Boolean = true
) {
    val haptics = LocalHapticFeedback.current
    var isDragging by remember { mutableStateOf(false) }

    val borderAlpha by animateFloatAsState(
        targetValue = if (isDragging) 1f else 0.85f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "borderAlpha"
    )

    Box(modifier = modifier.fillMaxSize()) {

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                // ✅ Required so BlendMode.Clear actually punches a hole (works across devices)
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
                        onDrag = { change, dragAmount ->
                            change.consume()

                            val w = size.width
                            val h = size.height
                            if (w <= 0f || h <= 0f) return@detectDragGestures

                            val dx = dragAmount.x / w
                            val dy = dragAmount.y / h

                            val width = rect.width
                            val height = rect.height

                            // Proposed new rect
                            val newLeft = (rect.left + dx).coerceIn(0f, 1f - width)
                            val newTop = (rect.top + dy).coerceIn(0f, 1f - height)

                            onRectChange(
                                Rect(
                                    left = newLeft,
                                    top = newTop,
                                    right = newLeft + width,
                                    bottom = newTop + height
                                )
                            )
                        }
                    )
                }
        ) {
            val w = size.width
            val h = size.height

            val crop = Rect(
                left = rect.left * w,
                top = rect.top * h,
                right = rect.right * w,
                bottom = rect.bottom * h
            )

            // Dim outside crop
            drawRect(Color.Black.copy(alpha = 0.55f))

            // Clear inside crop
            drawRect(
                color = Color.Transparent,
                topLeft = Offset(crop.left, crop.top),
                size = Size(crop.width, crop.height),
                blendMode = BlendMode.Clear
            )

            // Border
            val borderColor = Color.White.copy(alpha = borderAlpha)
            drawRoundRect(
                color = borderColor,
                topLeft = Offset(crop.left, crop.top),
                size = Size(crop.width, crop.height),
                cornerRadius = CornerRadius(18f, 18f),
                style = Stroke(width = 3f)
            )

            // Grid
            if (showGrid) {
                val gridColor = Color.White.copy(alpha = 0.22f)
                val thirdW = crop.width / 3f
                val thirdH = crop.height / 3f

                for (i in 1..2) {
                    drawLine(
                        color = gridColor,
                        start = Offset(crop.left + thirdW * i, crop.top),
                        end = Offset(crop.left + thirdW * i, crop.bottom),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = gridColor,
                        start = Offset(crop.left, crop.top + thirdH * i),
                        end = Offset(crop.right, crop.top + thirdH * i),
                        strokeWidth = 2f
                    )
                }
            }

            // Corner handles (visual)
            val handleSize = 22f
            val handleStroke = 6f
            val handleColor = Color.White.copy(alpha = 0.95f)

            fun corner(x: Float, y: Float, dx: Float, dy: Float) {
                drawLine(handleColor, Offset(x, y), Offset(x + dx * handleSize, y), handleStroke)
                drawLine(handleColor, Offset(x, y), Offset(x, y + dy * handleSize), handleStroke)
            }

            corner(crop.left, crop.top, +1f, +1f)
            corner(crop.right, crop.top, -1f, +1f)
            corner(crop.left, crop.bottom, +1f, -1f)
            corner(crop.right, crop.bottom, -1f, -1f)
        }

        // Optional small hint chip (subtle premium)
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 14.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = MaterialTheme.shapes.large,
            tonalElevation = 2.dp
        ) {
            Text(
                text = "Drag to move",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
