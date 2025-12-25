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
 * Premium iOS/IG-style crop overlay + micro-motion + haptics:
 * - Image displayed with ContentScale.Fit (never shows app UI in hole)
 * - Dim outside + hole punch via BlendMode.Clear (Offscreen compositing)
 * - Drag inside to move
 * - Drag corner handles to resize (fat hit targets)
 * - Haptics: start / mode-change / release
 * - Micro motion: border/glow/grid fade + subtle settle on release
 *
 * rect is normalized [0..1] relative to the displayed image rect.
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

    // Live (raw) rect while user drags; we drive a "settle" animation on release
    var liveRect by remember { mutableStateOf(rect) }
    LaunchedEffect(rect) {
        // Keep liveRect in sync if parent changes rect
        if (!draggingState) liveRect = rect
    }

    var draggingState by remember { mutableStateOf(false) }
    var dragMode by remember { mutableStateOf(DragMode.None) }
    var lastMode by remember { mutableStateOf(DragMode.None) }

    // Animated rect for settle micro-motion (only visible when not dragging)
    val settleRect by animateRectAsState(
        targetValue = if (draggingState) liveRect else liveRect.normalized().clamp01().enforceMinSize(0.12f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.82f),
        label = "settleRect"
    )

    // Micro motion: dim and border/glow intensity
    val dimAlpha by animateFloatAsState(
        targetValue = if (draggingState) 0.60f else 0.56f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "dimAlpha"
    )

    val borderAlpha by animateFloatAsState(
        targetValue = if (draggingState) 1f else 0.88f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "borderAlpha"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (draggingState) 0.36f else 0.0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "glowAlpha"
    )

    val gridAlpha by animateFloatAsState(
        targetValue = if (draggingState) 0.26f else 0.18f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "gridAlpha"
    )

    val handleAlpha by animateFloatAsState(
        targetValue = if (draggingState) 1f else 0.96f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "handleAlpha"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            // hole-punch reliably
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
                        draggingState = (mode != DragMode.None)

                        if (draggingState) {
                            // start tick
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }

                        // initialize live rect from current rect at drag start
                        liveRect = settleRect
                    },
                    onDragEnd = {
                        if (draggingState) {
                            // release tick
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        draggingState = false
                        dragMode = DragMode.None
                        lastMode = DragMode.None

                        // push a final, clamped rect back up so model stays clean
                        val final = liveRect.normalized().clamp01().enforceMinSize(0.12f)
                        liveRect = final
                        onRectChange(final)
                    },
                    onDragCancel = {
                        draggingState = false
                        dragMode = DragMode.None
                        lastMode = DragMode.None

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

                        // Detect if user's finger drifted close to a different handle while dragging:
                        // (premium trick: allow mode switching without lifting finger)
                        val cropPx = rectNormToPx(liveRect, imgRect)
                        val modeNow = hitTest(change.position, cropPx).takeIf { it != DragMode.None } ?: dragMode

                        if (modeNow != lastMode) {
                            // mode-change tick
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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

                        // Keep it sane while dragging (prevents jumpy clamps)
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

        // Use animated settleRect when idle, liveRect when dragging
        val displayRect = if (draggingState) liveRect else settleRect
        val crop = rectNormToPx(displayRect, imgRect)

        // ---- Dim outside (soft glass) ----
        drawRect(Color.Black.copy(alpha = dimAlpha))

        // Hole punch (rounded corners)
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

        // Glow while active
        if (glowAlpha > 0f) {
            drawRoundRect(
                color = Color(0xFF75A7FF).copy(alpha = glowAlpha),
                topLeft = Offset(crop.left, crop.top),
                size = crop.size,
                cornerRadius = CornerRadius(18f, 18f),
                style = Stroke(width = 10f)
            )
        }

        // Main border
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

        // Corner handles (visual shorter than hit target)
        val handleLen = 26f
        val stroke = 7f
        val handleColor = Color.White.copy(alpha = handleAlpha)

        fun corner(x: Float, y: Float, dx: Float, dy: Float) {
            val cap = 2.5f
            drawLine(handleColor, Offset(x - cap * dx, y), Offset(x + dx * handleLen, y), stroke)
            drawLine(handleColor, Offset(x, y - cap * dy), Offset(x, y + dy * handleLen), stroke)
        }

        corner(crop.left, crop.top, +1f, +1f)
        corner(crop.right, crop.top, -1f, +1f)
        corner(crop.left, crop.bottom, +1f, -1f)
        corner(crop.right, crop.bottom, -1f, -1f)

        // Idle center grab hint
        if (!draggingState) {
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
