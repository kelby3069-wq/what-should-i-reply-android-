package com.replysense.app.model

/**
 * One message turn in a conversation.
 */
data class ConversationTurn(
    val from: From,
    val text: String,
    val timestampEpochMs: Long = System.currentTimeMillis()
) {
    enum class From { USER, OTHER }
}
