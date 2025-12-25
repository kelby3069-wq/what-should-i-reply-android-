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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.replysense.app.cropBitmapNormalized
import kotlin.math.abs

@Composable
fun CropperDialog(
    title: String,
    bitmap: Bitmap,
    onCancel: () -> Unit,
    onConfirm: (Bitmap) -> Unit
) {
    var leftN by remember { mutableFloatStateOf(0.05f) }
    var topN by remember { mutableFloatStateOf(0.15f) }
    var rightN by remember { mutableFloatStateOf(0.95f) }
    var bottomN by remember { mutableFloatStateOf(0.90f) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(title) },
        text = {
            Column {
                Text("Drag box to chat area. Drag corners to resize.")
                Spacer(Modifier.height(10.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .background(Color.Black)
                ) {
                    CropCanvas(
                        bitmap = bitmap,
                        leftN = leftN,
                        topN = topN,
                        rightN = rightN,
                        bottomN = bottomN,
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
                onConfirm(cropBitmapNormalized(bitmap, leftN, topN, rightN, bottomN))
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
    val borderWidth = 4f
    val handleRadius = 10f
    val hitRadius = 30f
    val minSizePx = 36f

    val image = remember(bitmap) { bitmap.asImageBitmap() }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, drag ->
                    change.consume()

                    val w = size.width.min1f()
                    val h = size.height.min1f()

                    val left = leftN * w
                    val top = topN * h
                    val right = rightN * w
                    val bottom = bottomN * h

                    val p = change.position
                    val nearTL = dist(p, Offset(left, top)) <= hitRadius
                    val nearBR = dist(p, Offset(right, bottom)) <= hitRadius
                    val inside = p.x >= left && p.x <= right && p.y >= top && p.y <= bottom

                    var nl = left
                    var nt = top
                    var nr = right
                    var nb = bottom

                    when {
                        nearTL -> {
                            nl = (left + drag.x).coerceIn(0f, right - minSizePx)
                            nt = (top + drag.y).coerceIn(0f, bottom - minSizePx)
                        }
                        nearBR -> {
                            nr = (right + drag.x).coerceIn(left + minSizePx, w)
                            nb = (bottom + drag.y).coerceIn(top + minSizePx, h)
                        }
                        inside -> {
                            val rw = (right - left).min1f().coerceAtLeast(minSizePx)
                            val rh = (bottom - top).min1f().coerceAtLeast(minSizePx)

                            nl = (left + drag.x).coerceIn(0f, w - rw)
                            nt = (top + drag.y).coerceIn(0f, h - rh)
                            nr = nl + rw
                            nb = nt + rh
                        }
                        else -> return@detectDragGestures
                    }

                    onRectChange(
                        (nl / w).coerceIn(0f, 1f),
                        (nt / h).coerceIn(0f, 1f),
                        (nr / w).coerceIn(0f, 1f),
                        (nb / h).coerceIn(0f, 1f)
                    )
                }
            }
    ) {
        // ✅ safest overload: no Int params
        drawImage(image)

        val w = size.width.min1f()
        val h = size.height.min1f()

        val left = leftN * w
        val top = topN * h
        val right = rightN * w
        val bottom = bottomN * h

        // Dim outside
        drawRect(Color(0x88000000), size = size)

        val rectW = (right - left).min1f()
        val rectH = (bottom - top).min1f()

        // Clear crop area
        drawRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(rectW, rectH),
            blendMode = BlendMode.Clear
        )

        // Border
        drawRect(
            color = Color.White,
            topLeft = Offset(left, top),
            size = Size(rectW, rectH),
            style = Stroke(width = borderWidth)
        )

        // Handles
        drawCircle(Color.White, radius = handleRadius, center = Offset(left, top))
        drawCircle(Color.White, radius = handleRadius, center = Offset(right, bottom))
    }
}

/** Manhattan-ish distance (fast, fine for hit testing). */
private fun dist(a: Offset, b: Offset): Float =
    abs(a.x - b.x) + abs(a.y - b.y)

/** Float-only “at least 1f” helper to avoid Int overload traps. */
private fun Float.min1f(): Float = if (this < 1f) 1f else this
