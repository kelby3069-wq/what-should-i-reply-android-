package com.replysense.app

import android.graphics.Bitmap
import kotlin.math.max
import kotlin.math.min

/**
 * Crop a bitmap using normalized coordinates (0..1).
 * Safe + clamped + always returns a valid bitmap region.
 */
fun cropBitmapNormalized(
    src: Bitmap,
    leftN: Float,
    topN: Float,
    rightN: Float,
    bottomN: Float
): Bitmap {
    val w = src.width.coerceAtLeast(1)
    val h = src.height.coerceAtLeast(1)

    val l = (leftN.coerceIn(0f, 1f) * w).toInt()
    val t = (topN.coerceIn(0f, 1f) * h).toInt()
    val r = (rightN.coerceIn(0f, 1f) * w).toInt()
    val b = (bottomN.coerceIn(0f, 1f) * h).toInt()

    val left = min(l, r).coerceIn(0, w - 1)
    val top = min(t, b).coerceIn(0, h - 1)

    val right = max(l, r).coerceIn(left + 1, w)
    val bottom = max(t, b).coerceIn(top + 1, h)

    val cw = (right - left).coerceAtLeast(1)
    val ch = (bottom - top).coerceAtLeast(1)

    return Bitmap.createBitmap(src, left, top, cw, ch)
}
