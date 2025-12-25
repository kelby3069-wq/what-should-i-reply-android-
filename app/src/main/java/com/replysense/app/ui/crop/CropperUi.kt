package com.replysense.app.ui.crop

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

/**
 * Visual-only crop overlay:
 * - Dims outside crop rect
 * - Border + corner handles
 * - Draggable crop rect (move only)
 *
 * NOTE: This does NOT change your underlying crop logic yet.
 * Wire it to your existing crop rect state by passing rect in/out.
 */
@Composable
fun PremiumCropOverlay(
    modifier: Modifier = Modifier,
    // Rect is normalized to [0..1] in both axes (relative to image bounds)
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

                            // Move-only drag in normalized space
                            val w = size.width
                            val h = size.height
                            if (w <= 0f || h <= 0f) return@detectDragGestures

                            val dx = dragAmount.x / w
                            val dy = dragAmount.y / h

                            val newLeft = rect.left + dx
                            val newTop = rect.top + dy
                            val newRight = rect.right + dx
                            val newBottom = rect.bottom + dy

                            // Clamp to [0..1]
                            val width = rect.width
                            val height = rect.height

                            val clampedLeft = newLeft.coerceIn(0f, 1f - width)
                            val clampedTop = newTop.coerceIn(0f, 1f - height)
                            val clampedRect = Rect(
                                left = clampedLeft,
                                top = clampedTop,
                                right = clampedLeft + width,
                                bottom = clampedTop + height
                            )

                            onRectChange(clampedRect)
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
            val dim = Color.Black.copy(alpha = 0.55f)
            drawRect(dim)
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
                    // vertical
                    drawLine(
                        color = gridColor,
                        start = Offset(crop.left + thirdW * i, crop.top),
                        end = Offset(crop.left + thirdW * i, crop.bottom),
                        strokeWidth = 2f
                    )
                    // horizontal
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
                // L shape
                drawLine(handleColor, Offset(x, y), Offset(x + dx * handleSize, y), handleStroke)
                drawLine(handleColor, Offset(x, y), Offset(x, y + dy * handleSize), handleStroke)
            }

            corner(crop.left, crop.top, +1f, +1f)
            corner(crop.right, crop.top, -1f, +1f)
            corner(crop.left, crop.bottom, +1f, -1f)
            corner(crop.right, crop.bottom, -1f, -1f)
        }

        // Bottom controls (visual)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.80f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cancel", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(16.dp))
                    Text("Reset", style = MaterialTheme.typography.labelLarge)
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.92f),
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 6.dp
            ) {
                Box(Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
                    Text("Done", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
