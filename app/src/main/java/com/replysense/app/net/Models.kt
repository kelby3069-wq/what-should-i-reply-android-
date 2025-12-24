package com.replysense.app.net

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConversationTurn(
    val from: String,
    val text: String
)

@Serializable
data class ReplyRequest(
    val conversation: List<ConversationTurn>,
    val variants: Int = 3,
    val vibe: String = "auto",
    val tone: String = "auto",
    val writingStyle: String = "auto",
    val textQuality: String = "auto",
    val emojiLevel: String = "auto",
    val spiceLevel: String = "auto",
    val age: String = "auto",
    val punctuationPreference: String = "auto",
)

@Serializable
data class ReplyResponse(
    val ok: Boolean? = null,
    val error: String? = null,
    val details: String? = null,

    // common fields you were returning
    val model: String? = null,

    // Newer shape (from your screenshot):
    val request: ReplyRequestEcho? = null,
    val result: ReplyResult? = null,
)

@Serializable
data class ReplyRequestEcho(
    val variants: Int? = null,
    val vibe: String? = null,
    val tone: String? = null,
    val writingStyle: String? = null,
    val emojiLevel: String? = null,
    val textQuality: String? = null,
    val age: String? = null,
    val spiceLevel: String? = null,
    val punctuationPreference: String? = null,
)

@Serializable
data class ReplyResult(
    val vibe: VibeResult? = null,
    val replies: List<String> = emptyList()
)

@Serializable
data class VibeResult(
    val tone: String? = null,
    val writingStyle: String? = null,
    val emojiLevel: String? = null,
    val textQuality: String? = null,
    val spiceLevel: Int? = null,
    @SerialName("notes") val notes: String? = null
)
