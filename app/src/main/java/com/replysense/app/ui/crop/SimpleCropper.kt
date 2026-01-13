package com.replysense.app.ui.crop

import android.graphics.Bitmap

/**
 * Minimal hook: replace with a full cropper later.
 * For now, returns the original bitmap.
 */
object SimpleCropper {
    fun crop(bitmap: Bitmap): Bitmap = bitmap
}
