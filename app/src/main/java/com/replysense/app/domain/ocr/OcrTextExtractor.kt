package com.replysense.app.domain.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * OCR extractor using ML Kit.
 *
 * Domain-safe:
 * - No UI imports
 * - Coroutine-friendly
 * - Deterministic output
 */
class OcrTextExtractor {

    private val recognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractLines(bitmap: Bitmap): List<OcrLine> =
        suspendCancellableCoroutine { cont: CancellableContinuation<List<OcrLine>> ->
            val image = InputImage.fromBitmap(bitmap, 0)

            recognizer.process(image)
                .addOnSuccessListener { result ->
                    val lines: List<OcrLine> = result.textBlocks
                        .flatMap { it.lines }
                        .map { line ->
                            OcrLine(
                                text = line.text,
                                boundingBox = line.boundingBox
                            )
                        }

                    cont.resume(lines)
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
        }
}
