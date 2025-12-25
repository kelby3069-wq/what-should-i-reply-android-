package com.replysense.app.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReplyRequest(
    val text: String,

    /**
     * If you support auto-vibe on the Worker:
     * - Either send vibe="auto"
     * - Or omit vibe entirely (make it nullable) and infer server-side.
     *
     * This client uses vibe="auto" when auto mode is enabled.
     */
    val vibe: String,

    val context: String? = null,
    val platform: String? = "android",

    /**
     * Optional: if your Worker supports constraining auto-vibe.
     * Safe even if ignored (we ignoreUnknownKeys on responses; requests are just JSON).
     */
    @SerialName("vibesAllowed")
    val vibesAllowed: List<String>? = null
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
