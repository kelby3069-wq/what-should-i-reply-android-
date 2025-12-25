package com.replysense.app.net

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    @SerialName("role")
    val role: String,

    @SerialName("content")
    val content: String
)

@Serializable
data class ChatRequest(
    @SerialName("messages")
    val messages: List<ChatMessage>
)

@Serializable
data class ChatResponse(
    @SerialName("reply")
    val reply: String
)
