package com.replysense.app.ui.crop

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Premium crop overlay that draws on TOP of the actual bitmap (so you never see the app UI in the crop box).
 *
 * - The image is rendered with Compose Image(bitmap.asImageBitmap()).
 * - The crop rect is maintained in normalized [0..1] coords relative to the displayed image bounds.
 * - You can drag inside to move, drag corners to resize.
 * - Dims outside area, crisp border, corner handles, rule-of-thirds grid.
 */
@Composable
fun CropperUi(
    bitmap: Bitmap,
    requestCropKey: Int,
    onCropped: (Bitmap) -> Unit,
) {
    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }

    // Normalized crop rect (relative to displayed image area)
    var crop by remember {
        mutableStateOf(NormRect(left = 0.12f, top = 0.12f, right = 0.88f, bottom = 0.88f))
    }

    // When requestCropKey increments, generate output bitmap
    LaunchedEffect(requestCropKey) {
        if (requestCropKey == 0) return@LaunchedEffect
        val out = cropBitmapFromNormalized(bitmap, crop)
        onCropped(out)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Draw the bitmap itself (this is the fix for "crop box shows app UI")
        Image(
            bitmap = imageBitmap,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // Overlay + gestures
        CropOverlay(
            bitmap = bitmap,
            crop = crop,
            onCropChange = { crop = it }
        )
    }
}

@Composable
private fun CropOverlay(
    bitmap: Bitmap,
    crop: NormRect,
    onCropChange: (NormRect) -> Unit
) {
    val density = LocalDensity.current

    // Handle sizing
    val handleRadiusPx = with(density) { 10.dp.toPx() }
    val borderPx = with(density) { 2.dp.toPx() }
    val gridPx = with(density) { 1.dp.toPx() }

    var activeDrag by remember { mutableStateOf(DragMode.None) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { pos ->
                        // Determine displayed image rect for hit-testing
                        val imgRect = fittedImageRect(
                            containerW = size.width,
                            containerH = size.height,
                            imageW = bitmap.width.toFloat(),
                            imageH = bitmap.height.toFloat()
                        )

                        val cropRectPx = crop.toPxRect(imgRect)
                        val corners = cropRectPx.corners()

                        activeDrag = when {
                            pos.distTo(corners.tl) <= handleRadiusPx * 1.8f -> DragMode.ResizeTL
                            pos.distTo(corners.tr) <= handleRadiusPx * 1.8f -> DragMode.ResizeTR
                            pos.distTo(corners.br) <= handleRadiusPx * 1.8f -> DragMode.ResizeBR
                            pos.distTo(corners.bl) <= handleRadiusPx * 1.8f -> DragMode.ResizeBL
                            cropRectPx.contains(pos) -> DragMode.Move
                            else -> DragMode.None
                        }
                    },
                    onDragEnd = { activeDrag = DragMode.None },
                    onDragCancel = { activeDrag = DragMode.None },
                    onDrag = { change, dragAmount ->
                        change.consume()

                        val imgRect = fittedImageRect(
                            containerW = size.width,
                            containerH = size.height,
                            imageW = bitmap.width.toFloat(),
                            imageH = bitmap.height.toFloat()
                        )
                        val minSize = 0.10f // 10% min width/height in normalized space
                        val dxN = dragAmount.x / imgRect.width
                        val dyN = dragAmount.y / imgRect.height

                        var n = crop

                        when (activeDrag) {
                            DragMode.Move -> {
                                n = n.offset(dxN, dyN)
                            }
                            DragMode.ResizeTL -> {
                                n = n.copy(left = n.left + dxN, top = n.top + dyN)
                            }
                            DragMode.ResizeTR -> {
                                n = n.copy(right = n.right + dxN, top = n.top + dyN)
                            }
                            DragMode.ResizeBR -> {
                                n = n.copy(right = n.right + dxN, bottom = n.bottom + dyN)
                            }
                            DragMode.ResizeBL -> {
                                n = n.copy(left = n.left + dxN, bottom = n.bottom + dyN)
                            }
                            DragMode.None -> Unit
                        }

                        n = n
                            .normalized()
                            .clamp01()
                            .enforceMinSize(minSize)

                        onCropChange(n)
                    }
                )
            }
    ) {
        val imgRect = fittedImageRect(
            containerW = size.width,
            containerH = size.height,
            imageW = bitmap.width.toFloat(),
            imageH = bitmap.height.toFloat()
        )

        // Dim outside image bounds slightly (premium look)
        drawRect(Color.Black.copy(alpha = 0.35f))

        // Dim outside crop area (but only inside the image bounds)
        val cropRect = crop.toPxRect(imgRect)

        // Clip to image, then draw dim outside crop
        clipRect(imgRect.left, imgRect.top, imgRect.right, imgRect.bottom) {
            // Outside crop dim
            drawRect(
                color = Color.Black.copy(alpha = 0.50f),
                topLeft = Offset(imgRect.left, imgRect.top),
                size = imgRect.size
            )
            // Clear inside crop by drawing with BlendMode.Clear
            drawRect(
                color = Color.Transparent,
                topLeft = Offset(cropRect.left, cropRect.top),
                size = cropRect.size,
                blendMode = BlendMode.Clear
            )
        }

        // Border
        drawRect(
            color = Color.White.copy(alpha = 0.92f),
            topLeft = Offset(cropRect.left, cropRect.top),
            size = cropRect.size,
            style = Stroke(width = borderPx)
        )

        // Rule-of-thirds grid
        val gridColor = Color.White.copy(alpha = 0.28f)
        val thirdW = cropRect.width / 3f
        val thirdH = cropRect.height / 3f

        // vertical
        drawLine(gridColor, Offset(cropRect.left + thirdW, cropRect.top), Offset(cropRect.left + thirdW, cropRect.bottom), strokeWidth = gridPx)
        drawLine(gridColor, Offset(cropRect.left + 2f * thirdW, cropRect.top), Offset(cropRect.left + 2f * thirdW, cropRect.bottom), strokeWidth = gridPx)
        // horizontal
        drawLine(gridColor, Offset(cropRect.left, cropRect.top + thirdH), Offset(cropRect.right, cropRect.top + thirdH), strokeWidth = gridPx)
        drawLine(gridColor, Offset(cropRect.left, cropRect.top + 2f * thirdH), Offset(cropRect.right, cropRect.top + 2f * thirdH), strokeWidth = gridPx)

        // Corner handles (white ring + inner fill for "iOS/IG" feel)
        val corners = cropRect.corners()
        drawHandle(corners.tl, handleRadiusPx)
        drawHandle(corners.tr, handleRadiusPx)
        drawHandle(corners.br, handleRadiusPx)
        drawHandle(corners.bl, handleRadiusPx)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHandle(center: Offset, r: Float) {
    drawCircle(color = Color.White.copy(alpha = 0.95f), radius = r, center = center)
    drawCircle(color = Color.Black.copy(alpha = 0.55f), radius = r * 0.62f, center = center)
}

/** Compute the rect where the image is actually drawn when using ContentScale.Fit. */
private fun fittedImageRect(
    containerW: Float,
    containerH: Float,
    imageW: Float,
    imageH: Float
): Rect {
    val containerAR = containerW / containerH
    val imageAR = imageW / imageH

    return if (imageAR > containerAR) {
        // Image constrained by width
        val drawW = containerW
        val drawH = drawW / imageAR
        val top = (containerH - drawH) / 2f
        Rect(0f, top, drawW, top + drawH)
    } else {
        // Image constrained by height
        val drawH = containerH
        val drawW = drawH * imageAR
        val left = (containerW - drawW) / 2f
        Rect(left, 0f, left + drawW, drawH)
    }
}

private data class NormRect(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    fun normalized(): NormRect {
        val l = min(left, right)
        val r = max(left, right)
        val t = min(top, bottom)
        val b = max(top, bottom)
        return NormRect(l, t, r, b)
    }

    fun clamp01(): NormRect = NormRect(
        left = left.coerceIn(0f, 1f),
        top = top.coerceIn(0f, 1f),
        right = right.coerceIn(0f, 1f),
        bottom = bottom.coerceIn(0f, 1f)
    )

    fun enforceMinSize(minSize: Float): NormRect {
        var l = left
        var r = right
        var t = top
        var b = bottom

        if (r - l < minSize) {
            val mid = (l + r) / 2f
            l = (mid - minSize / 2f).coerceIn(0f, 1f - minSize)
            r = l + minSize
        }
        if (b - t < minSize) {
            val mid = (t + b) / 2f
            t = (mid - minSize / 2f).coerceIn(0f, 1f - minSize)
            b = t + minSize
        }
        return NormRect(l, t, r, b).clamp01()
    }

    fun offset(dx: Float, dy: Float): NormRect {
        val w = right - left
        val h = bottom - top
        var l = (left + dx)
        var t = (top + dy)
        l = l.coerceIn(0f, 1f - w)
        t = t.coerceIn(0f, 1f - h)
        return NormRect(l, t, l + w, t + h)
    }

    fun toPxRect(imageRect: Rect): Rect {
        val l = imageRect.left + left * imageRect.width
        val r = imageRect.left + right * imageRect.width
        val t = imageRect.top + top * imageRect.height
        val b = imageRect.top + bottom * imageRect.height
        return Rect(l, t, r, b)
    }
}

private data class CornerPoints(val tl: Offset, val tr: Offset, val br: Offset, val bl: Offset)

private fun Rect.corners(): CornerPoints = CornerPoints(
    tl = Offset(left, top),
    tr = Offset(right, top),
    br = Offset(right, bottom),
    bl = Offset(left, bottom)
)

private fun Offset.distTo(o: Offset): Float {
    val dx = x - o.x
    val dy = y - o.y
    return kotlin.math.sqrt(dx * dx + dy * dy)
}

private fun Rect.contains(p: Offset): Boolean =
    p.x in left..right && p.y in top..bottom

private enum class DragMode { None, Move, ResizeTL, ResizeTR, ResizeBR, ResizeBL }

/**
 * Crops the original bitmap using normalized crop rect.
 * This uses the *original bitmap* coordinate space, not the displayed rect (correct result).
 */
private fun cropBitmapFromNormalized(src: Bitmap, crop: NormRect): Bitmap {
    val n = crop.normalized().clamp01()
    val x = (n.left * src.width).toInt().coerceIn(0, src.width - 1)
    val y = (n.top * src.height).toInt().coerceIn(0, src.height - 1)
    val w = ((n.right - n.left) * src.width).toInt().coerceIn(1, src.width - x)
    val h = ((n.bottom - n.top) * src.height).toInt().coerceIn(1, src.height - y)
    return Bitmap.createBitmap(src, x, y, w, h)
}
