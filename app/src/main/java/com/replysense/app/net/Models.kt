package com.replysense.app.net

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatMessage(
    @Json(name = "role")
    val role: String,
    @Json(name = "content")
    val content: String
)

@JsonClass(generateAdapter = true)
data class OpenAiChatRequest(
    @Json(name = "model")
    val model: String,
    @Json(name = "messages")
    val messages: List<ChatMessage>
)

@JsonClass(generateAdapter = true)
data class OpenAiChatResponse(
    @Json(name = "choices")
    val choices: List<Choice>
) {
    @JsonClass(generateAdapter = true)
    data class Choice(
        @Json(name = "message")
        val message: ChatMessage
    )
}
