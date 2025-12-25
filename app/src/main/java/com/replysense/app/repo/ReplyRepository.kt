package com.replysense.app.repo

/**
 * ReplySense baseline: no persistence, no networking, no domain logic.
 *
 * This repository is intentionally minimal so CI can go green while we stabilize
 * Gradle/Kotlin/Compose + ML Kit OCR.
 *
 * When you're ready, we’ll replace this with real data sources (Room/DataStore/API).
 */
class ReplyRepository {

    data class ReplyItem(
        val id: String,
        val text: String,
        val createdAtEpochMs: Long
    )

    fun getHistory(): List<ReplyItem> = emptyList()

    fun saveReply(text: String): ReplyItem {
        return ReplyItem(
            id = "local-${System.currentTimeMillis()}",
            text = text,
            createdAtEpochMs = System.currentTimeMillis()
        )
    }

    fun clearHistory() {
        // no-op for baseline
    }
}
