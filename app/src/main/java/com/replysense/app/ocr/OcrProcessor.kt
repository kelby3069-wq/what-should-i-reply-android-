package com.replysense.app.domain.intelligence.ocr

import android.graphics.Bitmap

data class OcrLine(
    val text: String,
    val confidence: Float
)

data class OcrResult(
    val lines: List<OcrLine>
)

interface OcrProcessor {
    suspend fun process(bitmap: Bitmap): OcrResult
}
