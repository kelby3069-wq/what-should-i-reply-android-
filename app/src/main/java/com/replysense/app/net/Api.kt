package com.replysense.app.net

/**
 * ReplySense baseline stub.
 * Networking disabled until baseline build is stable.
 */
object Api {

    fun isEnabled(): Boolean = false

    fun sendPrompt(prompt: String): Result<String> {
        return Result.failure(
            IllegalStateException("Networking disabled in baseline build")
        )
    }
}
