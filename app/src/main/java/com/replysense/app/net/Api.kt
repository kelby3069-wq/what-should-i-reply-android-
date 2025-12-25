package com.replysense.app.net

/**
 * ReplySense baseline stub.
 *
 * Networking is disabled until after:
 * - Gradle
 * - Compose
 * - ML Kit OCR
 * are fully stable.
 */
object Api {

    fun isEnabled(): Boolean = false

    fun sendPrompt(prompt: String): Result<String> {
        return Result.failure(
            IllegalStateException("Networking disabled in baseline build")
        )
    }
}
