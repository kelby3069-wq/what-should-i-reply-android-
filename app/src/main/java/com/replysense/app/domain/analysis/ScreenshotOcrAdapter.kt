package com.replysense.app.domain.analysis

import android.graphics.Bitmap
import com.replysense.app.domain.intelligence.ocr.OcrProcessor

class ScreenshotOcrAdapter(
    private val ocrProcessor: OcrProcessor
) {

    suspend fun extractLines(bitmap: Bitmap): List<String> {
        return ocrProcessor.process(bitmap).lines.map { it.text }
    }
}
