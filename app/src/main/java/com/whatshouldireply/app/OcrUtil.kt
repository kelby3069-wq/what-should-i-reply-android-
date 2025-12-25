package com.whatshouldireply.app

import android.content.Context
import android.net.Uri

/**
 * OCR is intentionally disabled for MVP build stability.
 *
 * (Old implementation used Google ML Kit. We'll add it back only if needed.)
 */
object OcrUtil {

    fun isSupported(): Boolean = false

    /**
     * Returns null because OCR is disabled.
     */
    suspend fun recognizeText(
        context: Context,
        imageUri: Uri
    ): String? = null
}
