package com.replysense.app.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.replysense.app.cropBitmapNormalized
import kotlin.math.abs

/**
 * Simple crop dialog:
 * - Shows the image
 * - Draggable crop rectangle (move + resize via corners)
 * - Returns a cropped bitmap
 */
@Composable
fun CropperDialog(
    title: String,
    bitmap: Bitmap,
    onCancel: () -> Unit,
    onConfirm: (Bitmap) -> Unit
) {
    // Normalized crop rect
    var leftN by remember { mutableFloatStateOf(0.05f) }
    var topN by remember { mutableFloatStateOf(0.15f) }
    var rightN by remember { mutableFloatStateOf(0.95f) }
    var bottomN by remember { mutableFloatStateOf(0.90f) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(title) },
        text = {
            Column(Modifier.fillMaxWidth()) {
                Text("Drag box to chat area. Drag corners to resize.")
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .background(Color(0xFF111111))
                ) {
                    CropCanvas(
                        bitmap = bitmap,
                        leftN = leftN, topN = topN, rightN = rightN, bottomN = bottomN,
                        onRectChange = { l, t, r, b ->
                            leftN = l
                            topN = t
                            rightN = r
                            bottomN = b
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val cropped = cropBitmapNormalized(bitmap, leftN, topN, rightN, bottomN)
                onConfirm(cropped)
            }) { Text("Use crop") }
        },
        dismissButton = {
            OutlinedButton(onClick = onCancel) { Text("Cancel") }
        }
    )
}

@Composable
private fun CropCanvas(
    bitmap: Bitmap,
    leftN: Float,
    topN: Float,
    rightN: Float,
    bottomN: Float,
    onRectChange: (Float, Float, Float, Float) -> Unit
) {
    // Handles: top-left and bottom-right
    val handleRadius = 14f

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { /* no-op */ },
                    onDrag = { change, dragAmount ->
                        change.consume()

                        // Canvas size in px
                        val w = size.width.coerceAtLeast(1f)
                        val h = size.height.coerceAtLeast(1f)

                        // Convert current rect to px
                        val left = leftN * w
                        val top = topN * h
                        val right = rightN * w
                        val bottom = bottomN * h

                        val tl = Offset(left, top)
                        val br = Offset(right, bottom)

                        val p = change.position
                        val nearTL = dist(p, tl) <= handleRadius * 2
                        val nearBR = dist(p, br) <= handleRadius * 2

                        val dx = dragAmount.x
                        val dy = dragAmount.y

                        var newLeft = left
                        var newTop = top
                        var newRight = right
                        var newBottom = bottom

                        when {
                            nearTL -> {
                                newLeft = (left + dx).coerceIn(0f, right - 30f)
                                newTop = (top + dy).coerceIn(0f, bottom - 30f)
                            }
                            nearBR -> {
                                newRight = (right + dx).coerceIn(left + 30f, w)
                                newBottom = (bottom + dy).coerceIn(top + 30f, h)
                            }
                            // Drag whole rect if grabbing inside
                            p.x in left..right && p.y in top..bottom -> {
                                val rectW = right - left
                                val rectH = bottom - top
                                newLeft = (left + dx).coerceIn(0f, w - rectW)
                                newTop = (top + dy).coerceIn(0f, h - rectH)
                                newRight = newLeft + rectW
                                newBottom = newTop + rectH
                            }
                        }

                        // Back to normalized
                        onRectChange(
                            (newLeft / w).coerceIn(0f, 1f),
                            (newTop / h).coerceIn(0f, 1f),
                            (newRight / w).coerceIn(0f, 1f),
                            (newBottom / h).coerceIn(0f, 1f)
                        )
                    }
                )
            }
    ) {
        // Draw image scaled to canvas
        drawImage(bitmap.asImageBitmap())

        val w = size.width
        val h = size.height

        val left = leftN * w
        val top = topN * h
        val right = rightN * w
        val bottom = bottomN * h

        // Dim outside area
        drawRect(Color(0x88000000), size = size)
        drawRect(
            Color.Transparent,
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
            blendMode = androidx.compose.ui.graphics.BlendMode.Clear
        )

        // Crop rect border
        drawRect(
            color = Color.White,
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
            style = Stroke(width = 4f)
        )

        // Handles
        drawCircle(Color.White, radius = 10f, center = Offset(left, top))
        drawCircle(Color.White, radius = 10f, center = Offset(right, bottom))
    }
}

private fun dist(a: Offset, b: Offset): Float = abs(a.x - b.x) + abs(a.y - b.y)
