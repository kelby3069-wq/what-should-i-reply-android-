package com.replysense.app.domain.ocr

import android.graphics.Rect

/**
 * A single OCR line extracted from a screenshot.
 *
 * - text: the recognized line text
 * - boundingBox: optional screen-space bounds from ML Kit
 */
data class OcrLine(
    val text: String,
    val boundingBox: Rect?
)
