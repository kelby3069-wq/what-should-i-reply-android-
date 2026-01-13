package com.replysense.app.model

data class ConversationTurn(
    val speaker: Speaker,
    val text: String,
    val index: Int
)

enum class Speaker {
    USER,
    OTHER
}
