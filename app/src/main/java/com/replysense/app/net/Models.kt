package com.replysense.app.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReplyRequest(
    val text: String,

    /**
     * Use "auto" to let the Worker choose vibe.
     * Use a concrete vibe string (e.g., "friendly") if user selects one.
     */
    val vibe: String,

    val context: String? = null,
    val platform: String? = "android"
)

@Serializable
data class ReplyOption(
    val text: String,
    val label: String? = null,
    val vibe: String? = null
)

/**
 * Supports multiple possible Worker response shapes:
 * - { "replies": [ {text...}, ... ] }
 * - { "options": [ ... ] }
 * - { "results": [ ... ] }
 * - { "replyOptions": [ ... ] }
 */
@Serializable
data class ReplyResponse(
    val replies: List<ReplyOption> = emptyList(),
    val options: List<ReplyOption> = emptyList(),
    val results: List<ReplyOption> = emptyList(),
    @SerialName("replyOptions")
    val replyOptions: List<ReplyOption> = emptyList(),
) {
    fun allOptions(): List<ReplyOption> =
        listOf(replies, options, results, replyOptions).firstOrNull { it.isNotEmpty() } ?: emptyList()
}
