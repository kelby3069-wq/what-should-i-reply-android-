package com.replysense.app.ui.crop

import android.graphics.Bitmap
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateRectAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlin.math.*

/**
 * Premium iOS/IG-style crop overlay + micro-motion + haptics + pressed active handle + parallax dim.
 *
 * rect is normalized [0..1] relative to the displayed image rect (ContentScale.Fit).
 */
@Composable
fun CropperUi(
    bitmap: Bitmap,
    rect: Rect,
    onRectChange: (Rect) -> Unit,
    modifier: Modifier = Modifier,
    showGrid: Boolean = true
) {
    val img = remember(bitmap) { bitmap.asImageBitmap() }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            bitmap = img,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        PremiumOverlay(
            bitmapWidth = bitmap.width.toFloat(),
            bitmapHeight = bitmap.height.toFloat(),
            rect = rect,
            onRectChange = onRectChange,
            showGrid = showGrid
        )
    }
}

private enum class DragMode { None, Move, TL, TR, BR, BL }

@Composable
private fun PremiumOverlay(
    bitmapWidth: Float,
    bitmapHeight: Float,
    rect: Rect,
    onRectChange: (Rect) -> Unit,
    showGrid: Boolean
) {
    val haptics = LocalHapticFeedback.current

    var dragging by remember { mutableStateOf(false) }
    var dragMode by remember { mutableStateOf(DragMode.None) }
    var lastMode by remember { mutableStateOf(DragMode.None) }

    // Track drag direction for parallax dim (normalized-ish; we cap it hard)
    var dragVelocity by remember { mutableStateOf(Offset.Zero) }

    // Live rect during drag
    var liveRect by remember { mutableStateOf(rect) }

    // Keep in sync if parent changes rect while idle
    LaunchedEffect(rect) {
        if (!dragging) liveRect = rect
    }

    // Animate settle when not dragging
    val settleRect by animateRectAsState(
        targetValue = if (dragging) liveRect else liveRect.normalized().clamp01().enforceMinSize(0.12f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.82f),
        label = "settleRect"
    )

    // Micro motion states
    val dimAlpha by animateFloatAsState(
        targetValue = if (dragging) 0.60f else 0.56f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "dimAlpha"
    )

    val borderAlpha by animateFloatAsState(
        targetValue = if (dragging) 1f else 0.88f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "borderAlpha"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (dragging) 0.36f else 0.0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "glowAlpha"
    )

    val gridAlpha by animateFloatAsState(
        targetValue = if (dragging) 0.26f else 0.18f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "gridAlpha"
    )

    // Pressed handle factor based on mode (0..1)
    val handlePress by animateFloatAsState(
        targetValue = if (dragging && dragMode.isCorner()) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.78f),
        label = "handlePress"
    )

    // Parallax factor: only when dragging; drives a small offset for the dim layer.
    val parallax by animateFloatAsState(
        targetValue = if (dragging) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = 0.9f),
        label = "parallax"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { pos ->
                        val imgRect = fittedImageRect(
                            containerW = size.width,
                            containerH = size.height,
                            imageW = bitmapWidth,
                            imageH = bitmapHeight
                        )
                        val cropPx = rectNormToPx(settleRect, imgRect)
                        val mode = hitTest(pos, cropPx)

                        dragMode = mode
                        lastMode = mode
                        dragging = (mode != DragMode.None)

                        dragVelocity = Offset.Zero
                        liveRect = settleRect

                        if (dragging) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove) // grab
                        }
                    },
                    onDragEnd = {
                        if (dragging) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove) // release
                        }
                        dragging = false
                        dragMode = DragMode.None
                        lastMode = DragMode.None
                        dragVelocity = Offset.Zero

                        // Push final clamped rect to model
                        val final = liveRect.normalized().clamp01().enforceMinSize(0.12f)
                        liveRect = final
                        onRectChange(final)
                    },
                    onDragCancel = {
                        dragging = false
                        dragMode = DragMode.None
                        lastMode = DragMode.None
                        dragVelocity = Offset.Zero

                        val final = liveRect.normalized().clamp01().enforceMinSize(0.12f)
                        liveRect = final
                        onRectChange(final)
                    },
                    onDrag = { change, drag ->
                        change.consume()

                        val imgRect = fittedImageRect(
                            containerW = size.width,
                            containerH = size.height,
                            imageW = bitmapWidth,
                            imageH = bitmapHeight
                        )

                        if (dragMode == DragMode.None) return@detectDragGestures

                        // Update drag velocity for parallax (smoothed, capped)
                        dragVelocity = Offset(
                            x = (dragVelocity.x * 0.82f + drag.x * 0.18f).coerceIn(-20f, 20f),
                            y = (dragVelocity.y * 0.82f + drag.y * 0.18f).coerceIn(-20f, 20f)
                        )

                        // Allow mode switching mid-gesture by proximity
                        val cropPx = rectNormToPx(liveRect, imgRect)
                        val modeNow = hitTest(change.position, cropPx)
                            .takeIf { it != DragMode.None } ?: dragMode

                        if (modeNow != lastMode) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove) // mode change
                            lastMode = modeNow
                        }
                        dragMode = modeNow

                        val dx = drag.x / imgRect.width
                        val dy = drag.y / imgRect.height

                        val minSize = 0.12f
                        var n = liveRect

                        when (dragMode) {
                            DragMode.Move -> {
                                val w = n.width
                                val h = n.height
                                val nl = (n.left + dx).coerceIn(0f, 1f - w)
                                val nt = (n.top + dy).coerceIn(0f, 1f - h)
                                n = Rect(nl, nt, nl + w, nt + h)
                            }
                            DragMode.TL -> n = Rect(n.left + dx, n.top + dy, n.right, n.bottom)
                            DragMode.TR -> n = Rect(n.left, n.top + dy, n.right + dx, n.bottom)
                            DragMode.BR -> n = Rect(n.left, n.top, n.right + dx, n.bottom + dy)
                            DragMode.BL -> n = Rect(n.left + dx, n.top, n.right, n.bottom + dy)
                            DragMode.None -> Unit
                        }

                        n = n.normalized().clamp01().enforceMinSize(minSize)

                        liveRect = n
                        onRectChange(n)
                    }
                )
            }
    ) {
        val imgRect = fittedImageRect(
            containerW = size.width,
            containerH = size.height,
            imageW = bitmapWidth,
            imageH = bitmapHeight
        )

        val displayRect = if (dragging) liveRect else settleRect
        val crop = rectNormToPx(displayRect, imgRect)

        // ---- Parallax dim (tiny, capped) ----
        // Translate dim layer a few pixels based on drag direction (felt, not seen).
        val parallaxCap = 10f
        val px = (dragVelocity.x * 0.18f).coerceIn(-parallaxCap, parallaxCap) * parallax
        val py = (dragVelocity.y * 0.18f).coerceIn(-parallaxCap, parallaxCap) * parallax

        // Dim whole screen with parallax offset
        withTransform({
            translate(left = px, top = py)
        }) {
            drawRect(Color.Black.copy(alpha = dimAlpha))
        }

        // Hole punch only inside displayed image rect
        clipRect(imgRect.left, imgRect.top, imgRect.right, imgRect.bottom) {
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(crop.left, crop.top),
                size = crop.size,
                cornerRadius = CornerRadius(18f, 18f),
                blendMode = BlendMode.Clear
            )
        }

        // Inner shadow illusion
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.22f),
            topLeft = Offset(crop.left, crop.top),
            size = crop.size,
            cornerRadius = CornerRadius(18f, 18f),
            style = Stroke(width = 10f)
        )

        // Active glow
        if (glowAlpha > 0f) {
            drawRoundRect(
                color = Color(0xFF75A7FF).copy(alpha = glowAlpha),
                topLeft = Offset(crop.left, crop.top),
                size = crop.size,
                cornerRadius = CornerRadius(18f, 18f),
                style = Stroke(width = 10f)
            )
        }

        // Border
        drawRoundRect(
            color = Color.White.copy(alpha = borderAlpha),
            topLeft = Offset(crop.left, crop.top),
            size = crop.size,
            cornerRadius = CornerRadius(18f, 18f),
            style = Stroke(width = 3.5f)
        )

        // Grid
        if (showGrid) {
            val grid = Color.White.copy(alpha = gridAlpha)
            val thirdW = crop.width / 3f
            val thirdH = crop.height / 3f

            for (i in 1..2) {
                drawLine(
                    color = grid,
                    start = Offset(crop.left + thirdW * i, crop.top),
                    end = Offset(crop.left + thirdW * i, crop.bottom),
                    strokeWidth = 2f
                )
                drawLine(
                    color = grid,
                    start = Offset(crop.left, crop.top + thirdH * i),
                    end = Offset(crop.right, crop.top + thirdH * i),
                    strokeWidth = 2f
                )
            }
        }

        // Handles: base values
        val baseLen = 26f
        val baseStroke = 7f
        val pressedExtraLen = 4f
        val pressedExtraStroke = 1.5f

        fun cornerHandle(
            which: DragMode,
            x: Float,
            y: Float,
            dx: Float,
            dy: Float
        ) {
            val isActive = dragging && (dragMode == which)
            val press = if (isActive) handlePress else 0f

            val len = baseLen + pressedExtraLen * press
            val stroke = baseStroke + pressedExtraStroke * press
            val alpha = if (isActive) 1f else 0.96f

            val handleColor = Color.White.copy(alpha = alpha)
            val cap = 2.5f

            drawLine(handleColor, Offset(x - cap * dx, y), Offset(x + dx * len, y), stroke)
            drawLine(handleColor, Offset(x, y - cap * dy), Offset(x, y + dy * len), stroke)
        }

        cornerHandle(DragMode.TL, crop.left, crop.top, +1f, +1f)
        cornerHandle(DragMode.TR, crop.right, crop.top, -1f, +1f)
        cornerHandle(DragMode.BL, crop.left, crop.bottom, +1f, -1f)
        cornerHandle(DragMode.BR, crop.right, crop.bottom, -1f, -1f)

        // Idle center hint
        if (!dragging) {
            val cx = (crop.left + crop.right) / 2f
            val cy = (crop.top + crop.bottom) / 2f
            val w = min(120f, crop.width * 0.45f)
            val line = Color.White.copy(alpha = 0.10f)
            drawLine(line, Offset(cx - w / 2f, cy), Offset(cx + w / 2f, cy), 6f)
        }
    }
}

/* -----------------------------
   Hit testing / Geometry
   ----------------------------- */

private fun DragMode.isCorner(): Boolean =
    this == DragMode.TL || this == DragMode.TR || this == DragMode.BR || this == DragMode.BL

private fun hitTest(pos: Offset, crop: Rect): DragMode {
    val c = crop.corners()
    val hitR = 34f

    return when {
        pos.distTo(c.tl) <= hitR -> DragMode.TL
        pos.distTo(c.tr) <= hitR -> DragMode.TR
        pos.distTo(c.br) <= hitR -> DragMode.BR
        pos.distTo(c.bl) <= hitR -> DragMode.BL
        crop.contains(pos) -> DragMode.Move
        else -> DragMode.None
    }
}

private fun fittedImageRect(containerW: Float, containerH: Float, imageW: Float, imageH: Float): Rect {
    val containerAR = containerW / containerH
    val imageAR = imageW / imageH

    return if (imageAR > containerAR) {
        val drawW = containerW
        val drawH = drawW / imageAR
        val top = (containerH - drawH) / 2f
        Rect(0f, top, drawW, top + drawH)
    } else {
        val drawH = containerH
        val drawW = drawH * imageAR
        val left = (containerW - drawW) / 2f
        Rect(left, 0f, left + drawW, drawH)
    }
}

private fun rectNormToPx(n: Rect, imageRect: Rect): Rect {
    val l = imageRect.left + n.left * imageRect.width
    val t = imageRect.top + n.top * imageRect.height
    val r = imageRect.left + n.right * imageRect.width
    val b = imageRect.top + n.bottom * imageRect.height
    return Rect(l, t, r, b)
}

private data class Corners(val tl: Offset, val tr: Offset, val br: Offset, val bl: Offset)

private fun Rect.corners(): Corners = Corners(
    tl = Offset(left, top),
    tr = Offset(right, top),
    br = Offset(right, bottom),
    bl = Offset(left, bottom)
)

private fun Offset.distTo(o: Offset): Float {
    val dx = x - o.x
    val dy = y - o.y
    return sqrt(dx * dx + dy * dy)
}

private fun Rect.contains(p: Offset): Boolean =
    p.x in left..right && p.y in top..bottom

private fun Rect.normalized(): Rect {
    val l = min(left, right)
    val r = max(left, right)
    val t = min(top, bottom)
    val b = max(top, bottom)
    return Rect(l, t, r, b)
}

private fun Rect.clamp01(): Rect =
    Rect(
        left.coerceIn(0f, 1f),
        top.coerceIn(0f, 1f),
        right.coerceIn(0f, 1f),
        bottom.coerceIn(0f, 1f)
    )

private fun Rect.enforceMinSize(minSize: Float): Rect {
    var l = left
    var t = top
    var r = right
    var b = bottom

    val w = r - l
    val h = b - t

    if (w < minSize) {
        val mid = (l + r) / 2f
        l = (mid - minSize / 2f).coerceIn(0f, 1f - minSize)
        r = l + minSize
    }

    if (h < minSize) {
        val mid = (t + b) / 2f
        t = (mid - minSize / 2f).coerceIn(0f, 1f - minSize)
        b = t + minSize
    }

    return Rect(l, t, r, b).clamp01()
}
