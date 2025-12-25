package com.replysense.app.db

/**
 * ReplySense baseline stub.
 * Replaces Room @Entity during baseline phase.
 */
data class ReplyHistoryEntity(
    val id: String = "",
    val text: String = "",
    val createdAtEpochMs: Long = 0L
)
