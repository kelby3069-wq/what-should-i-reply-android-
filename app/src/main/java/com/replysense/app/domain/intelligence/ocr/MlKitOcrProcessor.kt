package com.replysense.app.domain.intelligence.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MlKitOcrProcessor : OcrProcessor {

    private val recognizer = TextRecognition.getClient(
        TextRecognizerOptions.DEFAULT_OPTIONS
    )

    override suspend fun process(bitmap: Bitmap): OcrResult =
        suspendCancellableCoroutine { continuation ->

            val image = InputImage.fromBitmap(bitmap, 0)

            recognizer.process(image)
                .addOnSuccessListener { result: Text ->

                    val lines = mutableListOf<OcrLine>()

                    for (block: Text.TextBlock in result.textBlocks) {
                        for (line: Text.Line in block.lines) {
                            lines.add(
                                OcrLine(
                                    text = line.text.trim(),
                                    confidence = line.confidence ?: 0.85f
                                )
                            )
                        }
                    }

                    continuation.resume(OcrResult(lines))
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
}
