package com.replysense.app.domain.conversation

import android.graphics.Rect

/**
 * Normalized, line-level representation of a conversation.
 * Stable IDs enable highlighting and scroll sync later.
 */
data class ConversationLine(
    val id: Int,
    val text: String,
    val boundingBox: Rect?
)
