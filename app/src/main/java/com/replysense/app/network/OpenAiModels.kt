package com.replysense.app.network

import com.squareup.moshi.Json

data class OpenAiRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    @Json(name = "max_tokens")
    val maxTokens: Int,
    val temperature: Double
)

data class OpenAiMessage(
    val role: String,
    val content: String
)

data class OpenAiResponse(
    val choices: List<Choice>
) {
    data class Choice(
        val message: OpenAiMessage
    )
}
