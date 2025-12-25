package com.replysense.app.ui

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.replysense.app.cropBitmapNormalized
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Reliable cropper:
 * - Always displays the provided bitmap as the base image.
 * - Drag inside the rect to move.
 * - Drag corners to resize.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropperDialog(
    title: String,
    bitmap: Bitmap,
    onCancel: () -> Unit,
    onConfirm: (Bitmap) -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(title, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Drag the box to the chat area. Drag corners to resize.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))

                // Container that shows the bitmap + overlay crop rect.
                var boxSize by remember { mutableStateOf(IntSize(1, 1)) }

                // Crop rect in NORMALIZED coords (0..1) relative to the displayed image area.
                var rectN by remember {
                    mutableStateOf(
                        Rect(
                            left = 0.12f,
                            top = 0.18f,
                            right = 0.88f,
                            bottom = 0.82f
                        )
                    )
                }

                // What are we dragging?
                var dragMode by remember { mutableStateOf(DragMode.NONE) }
                var dragStartRect by remember { mutableStateOf(rectN) }
                var dragStartPos by remember { mutableStateOf(Offset.Zero) }

                val handleRadiusDp = 14.dp
                val handleRadiusPx = with(LocalDensity.current) { handleRadiusDp.toPx() }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 320.dp)
                        .onSizeChanged { boxSize = it }
                ) {
                    // ✅ This is the key: show the ACTUAL BITMAP, not a screenshot of composables.
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "To crop",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    // Overlay: dim outside + stroke + corner handles
                    CropOverlay(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(boxSize, rectN) {
                                detectDragGestures(
                                    onDragStart = { pos ->
                                        dragStartPos = pos
                                        dragStartRect = rectN
                                        dragMode = hitTestDragMode(
                                            pos = pos,
                                            rectN = rectN,
                                            w = boxSize.width.toFloat(),
                                            h = boxSize.height.toFloat(),
                                            handleRadiusPx = handleRadiusPx
                                        )
                                    },
                                    onDragEnd = { dragMode = DragMode.NONE },
                                    onDragCancel = { dragMode = DragMode.NONE },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        if (boxSize.width <= 1 || boxSize.height <= 1) return@detectDragGestures

                                        val dxN = dragAmount.x / boxSize.width.toFloat()
                                        val dyN = dragAmount.y / boxSize.height.toFloat()

                                        rectN = when (dragMode) {
                                            DragMode.MOVE -> moveRect(dragStartRect, dxN, dyN)
                                            DragMode.TL -> resizeRect(dragStartRect, dxN, dyN, Corner.TL)
                                            DragMode.TR -> resizeRect(dragStartRect, dxN, dyN, Corner.TR)
                                            DragMode.BL -> resizeRect(dragStartRect, dxN, dyN, Corner.BL)
                                            DragMode.BR -> resizeRect(dragStartRect, dxN, dyN, Corner.BR)
                                            DragMode.NONE -> rectN
                                        }.clamp(minSizeN = 0.08f)
                                    }
                                )
                            },
                        rectN = rectN
                    )
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onCancel) { Text("Cancel") }
                    Button(
                        onClick = {
                            // crop using normalized rect against original bitmap
                            val cropped = cropBitmapNormalized(
                                src = bitmap,
                                leftN = rectN.left,
                                topN = rectN.top,
                                rightN = rectN.right,
                                bottomN = rectN.bottom
                            )
                            onConfirm(cropped)
                        }
                    ) { Text("Use crop") }
                }
            }
        }
    }
}

@Composable
private fun CropOverlay(
    modifier: Modifier,
    rectN: Rect
) {
    Box(
        modifier = modifier.drawBehind {
            val w = size.width
            val h = size.height

            val l = rectN.left * w
            val t = rectN.top * h
            val r = rectN.right * w
            val b = rectN.bottom * h

            // Dim outside
            drawRect(Color(0x99000000))
            // Clear center by overdrawing with transparent via native canvas clip
            drawIntoCanvas { canvas ->
                val nc = canvas.nativeCanvas
                val checkpoint = nc.save()
                nc.clipRect(l, t, r, b)
                nc.drawColor(android.graphics.Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)
                nc.restoreToCount(checkpoint)
            }

            // Stroke rect
            drawRect(
                color = Color.White,
                topLeft = Offset(l, t),
                size = androidx.compose.ui.geometry.Size(r - l, b - t),
                style = Stroke(width = 4f)
            )

            // Corner handles
            fun handle(x: Float, y: Float) {
                drawCircle(Color.White, radius = 10f, center = Offset(x, y))
                drawCircle(Color.Black.copy(alpha = 0.25f), radius = 12f, center = Offset(x, y))
            }
            handle(l, t)
            handle(r, t)
            handle(l, b)
            handle(r, b)
        }
    )
}

private enum class DragMode { NONE, MOVE, TL, TR, BL, BR }
private enum class Corner { TL, TR, BL, BR }

private fun hitTestDragMode(
    pos: Offset,
    rectN: Rect,
    w: Float,
    h: Float,
    handleRadiusPx: Float
): DragMode {
    val l = rectN.left * w
    val t = rectN.top * h
    val r = rectN.right * w
    val b = rectN.bottom * h

    fun near(a: Offset, b: Offset): Boolean {
        return abs(a.x - b.x) <= handleRadiusPx && abs(a.y - b.y) <= handleRadiusPx
    }

    val p = pos
    val tl = Offset(l, t)
    val tr = Offset(r, t)
    val bl = Offset(l, b)
    val br = Offset(r, b)

    return when {
        near(p, tl) -> DragMode.TL
        near(p, tr) -> DragMode.TR
        near(p, bl) -> DragMode.BL
        near(p, br) -> DragMode.BR
        p.x in l..r && p.y in t..b -> DragMode.MOVE
        else -> DragMode.NONE
    }
}

private fun moveRect(r: Rect, dxN: Float, dyN: Float): Rect {
    val w = r.width
    val h = r.height
    var left = r.left + dxN
    var top = r.top + dyN
    var right = left + w
    var bottom = top + h

    // clamp to 0..1
    if (left < 0f) { left = 0f; right = w }
    if (top < 0f) { top = 0f; bottom = h }
    if (right > 1f) { right = 1f; left = 1f - w }
    if (bottom > 1f) { bottom = 1f; top = 1f - h }

    return Rect(left, top, right, bottom)
}

private fun resizeRect(r: Rect, dxN: Float, dyN: Float, corner: Corner): Rect {
    var left = r.left
    var top = r.top
    var right = r.right
    var bottom = r.bottom

    when (corner) {
        Corner.TL -> { left += dxN; top += dyN }
        Corner.TR -> { right += dxN; top += dyN }
        Corner.BL -> { left += dxN; bottom += dyN }
        Corner.BR -> { right += dxN; bottom += dyN }
    }

    return Rect(left, top, right, bottom)
}

private fun Rect.clamp(minSizeN: Float): Rect {
    var l = left
    var t = top
    var r = right
    var b = bottom

    // normalize ordering
    if (l > r) { val tmp = l; l = r; r = tmp }
    if (t > b) { val tmp = t; t = b; b = tmp }

    // min size
    val w = r - l
    val h = b - t
    if (w < minSizeN) {
        val mid = (l + r) / 2f
        l = mid - minSizeN / 2f
        r = mid + minSizeN / 2f
    }
    if (h < minSizeN) {
        val mid = (t + b) / 2f
        t = mid - minSizeN / 2f
        b = mid + minSizeN / 2f
    }

    // clamp to 0..1
    l = l.coerceIn(0f, 1f)
    t = t.coerceIn(0f, 1f)
    r = r.coerceIn(0f, 1f)
    b = b.coerceIn(0f, 1f)

    // re-ensure min-size after clamp
    val ww = r - l
    val hh = b - t
    if (ww < minSizeN) {
        r = min(1f, l + minSizeN)
        l = max(0f, r - minSizeN)
    }
    if (hh < minSizeN) {
        b = min(1f, t + minSizeN)
        t = max(0f, b - minSizeN)
    }

    return Rect(l, t, r, b)
}
