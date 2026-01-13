package com.replysense.app.data.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object MlKitOcrRunner {

    suspend fun recognizeLines(bitmap: Bitmap): List<String> =
        suspendCoroutine { cont ->

            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(
                TextRecognizerOptions.DEFAULT_OPTIONS
            )

            recognizer.process(image)
                .addOnSuccessListener { result ->
                    val lines = result.textBlocks
                        .flatMap { block -> block.lines }
                        .map { line -> line.text }

                    cont.resume(lines)
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
        }
}
