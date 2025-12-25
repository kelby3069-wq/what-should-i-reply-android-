package com.replysense.app.util

import com.replysense.app.model.ConversationTurn

/**
 * MVP parser: splits a pasted transcript into turns.
 *
 * Supported formats:
 * - "You: hi" / "Them: hello"
 * - "Me: ..." / "Other: ..."
 * - Lines with no prefix become OTHER by default.
 */
object ConversationParser {

    fun parse(raw: String): List<ConversationTurn> {
        val lines = raw
            .replace("\r\n", "\n")
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        return lines.map { line ->
            val lower = line.lowercase()

            val from = when {
                lower.startsWith("you:") || lower.startsWith("me:") || lower.startsWith("user:") ->
                    ConversationTurn.From.USER
                lower.startsWith("them:") || lower.startsWith("other:") || lower.startsWith("her:") || lower.startsWith("him:") ->
                    ConversationTurn.From.OTHER
                else -> ConversationTurn.From.OTHER
            }

            val text = line.substringAfter(':', line).trim()
            ConversationTurn(from = from, text = text)
        }
    }
}
